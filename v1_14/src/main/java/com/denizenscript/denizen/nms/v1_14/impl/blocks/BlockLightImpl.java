package com.denizenscript.denizen.nms.v1_14.impl.blocks;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.abstracts.BlockLight;
import com.denizenscript.denizen.nms.util.ReflectionHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.minecraft.server.v1_14_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_14_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_14_R1.block.CraftBlock;
import org.bukkit.util.Vector;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class BlockLightImpl extends BlockLight {

    public static final Field PACKETPLAYOUTLIGHTUPDATE_CHUNKX = ReflectionHelper.getFields(PacketPlayOutLightUpdate.class).get("a");
    public static final Field PACKETPLAYOUTLIGHTUPDATE_CHUNKZ = ReflectionHelper.getFields(PacketPlayOutLightUpdate.class).get("b");
    public static final Field PACKETPLAYOUTLIGHTUPDATE_BLOCKLIGHT_BITMASK = ReflectionHelper.getFields(PacketPlayOutLightUpdate.class).get("d");
    public static final Field PACKETPLAYOUTLIGHTUPDATE_BLOCKLIGHT_DATA = ReflectionHelper.getFields(PacketPlayOutLightUpdate.class).get("h");
    public static final Field PACKETPLAYOUTBLOCKCHANGE_POSITION = ReflectionHelper.getFields(PacketPlayOutBlockChange.class).get("a");

    public static final Class LIGHTENGINETHREADED_UPDATE = LightEngineThreaded.class.getDeclaredClasses()[0];
    public static final Object LIGHTENGINETHREADED_UPDATE_PRE;

    static {
        Object preObj = null;
        try {
            preObj = ReflectionHelper.getFields(LIGHTENGINETHREADED_UPDATE).get("PRE_UPDATE").get(null);
        }
        catch (Throwable ex) {
            ex.printStackTrace();
        }
        LIGHTENGINETHREADED_UPDATE_PRE = preObj;
    }

    public static final MethodHandle LIGHTENGINETHREADED_QUEUERUNNABLE = ReflectionHelper.getMethodHandle(LightEngineThreaded.class, "a",
            int.class, int.class,  LIGHTENGINETHREADED_UPDATE, Runnable.class);

    public static void enqueueRunnable(Chunk chunk, Runnable runnable) {
        LightEngine lightEngine = chunk.e();
        if (lightEngine instanceof LightEngineThreaded) {
            ChunkCoordIntPair coord = chunk.getPos();
            try {
                LIGHTENGINETHREADED_QUEUERUNNABLE.invoke(lightEngine, coord.x, coord.z, LIGHTENGINETHREADED_UPDATE_PRE, runnable);
            }
            catch (Throwable ex) {
                Debug.echoError(ex);
            }
        }
        else {
            runnable.run();
        }
    }

    private BlockLightImpl(Location location, long ticks) {
        super(location, ticks);
    }

    public static BlockLight createLight(Location location, int lightLevel, long ticks) {
        location = location.getBlock().getLocation();
        BlockLight blockLight;
        if (lightsByLocation.containsKey(location)) {
            blockLight = lightsByLocation.get(location);
            if (blockLight.removeTask != null) {
                blockLight.removeTask.cancel();
                blockLight.removeTask = null;
            }
            if (blockLight.updateTask != null) {
                blockLight.updateTask.cancel();
                blockLight.updateTask = null;
            }
            blockLight.removeLater(ticks);
        }
        else {
            blockLight = new BlockLightImpl(location, ticks);
            lightsByLocation.put(location, blockLight);
            if (!lightsByChunk.containsKey(blockLight.chunk)) {
                lightsByChunk.put(blockLight.chunk, new ArrayList<>());
            }
            lightsByChunk.get(blockLight.chunk).add(blockLight);
        }
        blockLight.intendedLevel = lightLevel;
        blockLight.update(lightLevel, true);
        return blockLight;
    }

    public static void checkIfLightsBrokenByPacket(PacketPlayOutBlockChange packet, World world) {
        try {
            BlockPosition pos = (BlockPosition) PACKETPLAYOUTBLOCKCHANGE_POSITION.get(packet);
            int chunkX = pos.getX() >> 4;
            int chunkZ = pos.getZ() >> 4;
            Bukkit.getScheduler().scheduleSyncDelayedTask(NMSHandler.getJavaPlugin(), () -> {
                Chunk chunk = world.getChunkAt(chunkX, chunkZ);
                boolean any = false;
                for (Vector vec : RELATIVE_CHUNKS) {
                    Chunk other = world.getChunkIfLoaded(chunkX + vec.getBlockX(), chunkZ + vec.getBlockZ());
                    if (other != null) {
                        List<BlockLight> lights = lightsByChunk.get(other.bukkitChunk);
                        if (lights != null) {
                            any = true;
                            for (BlockLight light : lights) {
                                Bukkit.getScheduler().scheduleSyncDelayedTask(NMSHandler.getJavaPlugin(), () -> light.update(light.intendedLevel, false), 1);
                            }
                        }
                    }
                }
                if (any) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(NMSHandler.getJavaPlugin(), () -> sendNearbyChunkUpdates(chunk), 3);
                }
            }, 1);
        }
        catch (Exception ex) {
            Debug.echoError(ex);
        }
    }

    public static void checkIfLightsBrokenByPacket(PacketPlayOutLightUpdate packet, World world) {
        if (doNotCheck) {
            return;
        }
        try {
            int cX = PACKETPLAYOUTLIGHTUPDATE_CHUNKX.getInt(packet);
            int cZ = PACKETPLAYOUTLIGHTUPDATE_CHUNKZ.getInt(packet);
            int bitMask = PACKETPLAYOUTLIGHTUPDATE_BLOCKLIGHT_BITMASK.getInt(packet);
            List<byte[]> blockData = (List<byte[]>) PACKETPLAYOUTLIGHTUPDATE_BLOCKLIGHT_DATA.get(packet);
            Bukkit.getScheduler().scheduleSyncDelayedTask(NMSHandler.getJavaPlugin(), () -> {
                Chunk chk = world.getChunkIfLoaded(cX, cZ);
                if (chk == null) {
                    return;
                }
                List<BlockLight> lights = lightsByChunk.get(chk.bukkitChunk);
                if (lights == null) {
                    return;
                }
                boolean any = false;
                for (BlockLight light : lights) {
                    if (((BlockLightImpl) light).checkIfChangedBy(bitMask, blockData)) {
                        Bukkit.getScheduler().scheduleSyncDelayedTask(NMSHandler.getJavaPlugin(), () -> light.update(light.intendedLevel, false), 1);
                        any = true;
                    }
                }
                if (any) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(NMSHandler.getJavaPlugin(), () -> sendNearbyChunkUpdates(chk), 3);
                }
            }, 1);
        }
        catch (Exception ex) {
            Debug.echoError(ex);
        }
    }

    public static boolean doNotCheck = false;

    public boolean checkIfChangedBy(int bitmask, List<byte[]> data) {
        Location blockLoc = block.getLocation();
        int layer = (blockLoc.getBlockY() >> 4) + 1;
        if ((bitmask & (1 << layer)) == 0) {
            return false;
        }
        int found = 0;
        for (int i = 0; i < 16; i++) {
            if ((bitmask & (1 << i)) != 0) {
                if (i == layer) {
                    byte[] blocks = data.get(found);
                    NibbleArray arr = new NibbleArray(blocks);
                    int x = blockLoc.getBlockX() - (chunk.getX() << 4);
                    int y = blockLoc.getBlockY() % 16;
                    int z = blockLoc.getBlockZ() - (chunk.getZ() << 4);
                    int level = arr.a(x, y, z);
                    return intendedLevel != level;
                }
                found++;
            }
        }
        return false;
    }

    public static void runResetFor(final Chunk chunk, final BlockPosition pos) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                LightEngine lightEngine = chunk.e();
                LightEngineBlock engineBlock = (LightEngineBlock) lightEngine.a(EnumSkyBlock.BLOCK);
                engineBlock.a(pos);
            }
        };
        enqueueRunnable(chunk, runnable);
    }

    public static void runSetFor(final Chunk chunk, final BlockPosition pos, final int level) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                LightEngine lightEngine = chunk.e();
                LightEngineBlock engineBlock = (LightEngineBlock) lightEngine.a(EnumSkyBlock.BLOCK);
                engineBlock.a(pos, level);
            }
        };
        enqueueRunnable(chunk, runnable);
    }

    @Override
    public void reset(boolean updateChunk) {
        runResetFor(((CraftChunk) chunk).getHandle(), ((CraftBlock) block).getPosition());
        if (updateChunk) {
            updateTask = Bukkit.getScheduler().runTaskLater(NMSHandler.getJavaPlugin(), (Runnable) this::sendNearbyChunkUpdates, 1);
        }
    }

    @Override
    public void update(int lightLevel, boolean updateChunk) {
        runResetFor(((CraftChunk) chunk).getHandle(), ((CraftBlock) block).getPosition());
        updateTask = Bukkit.getScheduler().runTaskLater(NMSHandler.getJavaPlugin(), () -> {
            updateTask = null;
            runSetFor(((CraftChunk) chunk).getHandle(), ((CraftBlock) block).getPosition(), lightLevel);
            if (updateChunk) {
                updateTask = Bukkit.getScheduler().runTaskLater(NMSHandler.getJavaPlugin(), (Runnable) this::sendNearbyChunkUpdates, 1);
            }
        }, 1);
    }

    public static final Vector[] RELATIVE_CHUNKS = new Vector[] {
            new Vector(0, 0, 0),
            new Vector(-1, 0, 0), new Vector(1, 0, 0), new Vector(0, 0, -1), new Vector(0, 0, 1),
            new Vector(-1, 0, -1), new Vector(-1, 0, 1), new Vector(1, 0, -1), new Vector(1, 0, 1)
    };

    public  void sendNearbyChunkUpdates() {
        sendNearbyChunkUpdates(((CraftChunk) chunk).getHandle());
    }

    public static void sendNearbyChunkUpdates(Chunk chunk) {
        ChunkCoordIntPair pos = chunk.getPos();
        for (Vector vec : RELATIVE_CHUNKS) {
            Chunk other = chunk.getWorld().getChunkIfLoaded(pos.x + vec.getBlockX(), pos.z + vec.getBlockZ());
            if (other != null) {
                sendSingleChunkUpdate(other);
            }
        }
    }

    public static void sendSingleChunkUpdate(Chunk chunk) {
        doNotCheck = true;
        LightEngine lightEngine = chunk.e();
        ChunkCoordIntPair pos = chunk.getPos();
        PacketPlayOutLightUpdate packet = new PacketPlayOutLightUpdate(pos, lightEngine);
        ((WorldServer) chunk.world).getChunkProvider().playerChunkMap.a(pos, false).forEach((player) -> {
            player.playerConnection.sendPacket(packet);
        });
        doNotCheck = false;
    }
}
