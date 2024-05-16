package com.denizenscript.denizen.nms.v1_20.impl.blocks;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.abstracts.BlockLight;
import com.denizenscript.denizen.nms.v1_20.ReflectionMappingsInfo;
import com.denizenscript.denizen.utilities.blocks.ChunkCoordinate;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacket;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.lighting.LayerLightEventListener;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R4.CraftChunk;
import org.bukkit.craftbukkit.v1_20_R4.block.CraftBlock;
import org.bukkit.util.Vector;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

public class BlockLightImpl extends BlockLight {

    public static final Class LIGHTENGINETHREADED_TASKTYPE = Arrays.stream(ThreadedLevelLightEngine.class.getDeclaredClasses()).filter(c -> c.isEnum()).findFirst().get(); // TaskType
    public static final Object LIGHTENGINETHREADED_TASKTYPE_PRE;

    static {
        Object preObj = null;
        try {
            preObj = ReflectionHelper.getFields(LIGHTENGINETHREADED_TASKTYPE).get(ReflectionMappingsInfo.ThreadedLevelLightEngineTaskType_PRE_UPDATE).get(null);
        }
        catch (Throwable ex) {
            ex.printStackTrace();
        }
        LIGHTENGINETHREADED_TASKTYPE_PRE = preObj;
    }

    public static final MethodHandle LIGHTENGINETHREADED_QUEUERUNNABLE = ReflectionHelper.getMethodHandle(ThreadedLevelLightEngine.class, ReflectionMappingsInfo.ThreadedLevelLightEngine_addTask_method,
            int.class, int.class,  LIGHTENGINETHREADED_TASKTYPE, Runnable.class);

    public static void enqueueRunnable(LevelChunk chunk, Runnable runnable) {
        LevelLightEngine lightEngine = chunk.getLevel().getChunkSource().getLightEngine();
        if (lightEngine instanceof ThreadedLevelLightEngine) {
            ChunkPos coord = chunk.getPos();
            try {
                LIGHTENGINETHREADED_QUEUERUNNABLE.invoke(lightEngine, coord.x, coord.z, LIGHTENGINETHREADED_TASKTYPE_PRE, runnable);
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
            if (!lightsByChunk.containsKey(blockLight.chunkCoord)) {
                lightsByChunk.put(blockLight.chunkCoord, new ArrayList<>());
            }
            lightsByChunk.get(blockLight.chunkCoord).add(blockLight);
        }
        blockLight.intendedLevel = lightLevel;
        blockLight.update(lightLevel, true);
        return blockLight;
    }

    public static void checkIfLightsBrokenByPacket(ClientboundBlockUpdatePacket packet, Level world) {
        try {
            BlockPos pos = packet.getPos();
            int chunkX = pos.getX() >> 4;
            int chunkZ = pos.getZ() >> 4;
            Bukkit.getScheduler().scheduleSyncDelayedTask(NMSHandler.getJavaPlugin(), () -> {
                LevelChunk chunk = world.getChunk(chunkX, chunkZ);
                boolean any = false;
                for (Vector vec : RELATIVE_CHUNKS) {
                    ChunkAccess other = world.getChunk(chunkX + vec.getBlockX(), chunkZ + vec.getBlockZ(), ChunkStatus.FULL, false);
                    if (other instanceof LevelChunk) {
                        List<BlockLight> lights = lightsByChunk.get(new ChunkCoordinate(new CraftChunk((LevelChunk) other)));
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

    public static void checkIfLightsBrokenByPacket(ClientboundLightUpdatePacket packet, Level world) {
        if (doNotCheck) {
            return;
        }
        try {
            int cX = packet.getX();
            int cZ = packet.getZ();
            BitSet bitMask = packet.getLightData().getBlockYMask();
            List<byte[]> blockData = packet.getLightData().getBlockUpdates();
            Bukkit.getScheduler().scheduleSyncDelayedTask(NMSHandler.getJavaPlugin(), () -> {
                ChunkAccess chk = world.getChunk(cX, cZ, ChunkStatus.FULL, false);
                if (!(chk instanceof LevelChunk)) {
                    return;
                }
                List<BlockLight> lights = lightsByChunk.get(new ChunkCoordinate(new CraftChunk((LevelChunk) chk)));
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
                    Bukkit.getScheduler().scheduleSyncDelayedTask(NMSHandler.getJavaPlugin(), () -> sendNearbyChunkUpdates((LevelChunk) chk), 3);
                }
            }, 1);
        }
        catch (Exception ex) {
            Debug.echoError(ex);
        }
    }

    public static boolean doNotCheck = false;

    public boolean checkIfChangedBy(BitSet bitmask, List<byte[]> data) {
        Location blockLoc = block.getLocation();
        int layer = (blockLoc.getBlockY() >> 4) + 1;
        if (!bitmask.get(layer)) {
            return false;
        }
        int found = 0;
        for (int i = 0; i < 16; i++) {
            if (bitmask.get(i)) {
                if (i == layer) {
                    byte[] blocks = data.get(found);
                    DataLayer arr = new DataLayer(blocks);
                    int x = blockLoc.getBlockX() - (chunkCoord.x << 4);
                    int y = blockLoc.getBlockY() % 16;
                    int z = blockLoc.getBlockZ() - (chunkCoord.z << 4);
                    int level = arr.get(x, y, z);
                    return intendedLevel != level;
                }
                found++;
            }
        }
        return false;
    }

    public static void runResetFor(final LevelChunk chunk, final BlockPos pos) {
        Runnable runnable = () -> {
            LevelLightEngine lightEngine = chunk.getLevel().getChunkSource().getLightEngine();
            LayerLightEventListener engineBlock = lightEngine.getLayerListener(LightLayer.BLOCK);
            engineBlock.checkBlock(pos);
        };
        enqueueRunnable(chunk, runnable);
    }

    public static void runSetFor(final LevelChunk chunk, final BlockPos pos, final int level) {
        Runnable runnable = () -> {
            LevelLightEngine lightEngine = chunk.getLevel().getChunkSource().getLightEngine();
            LayerLightEventListener engineBlock = lightEngine.getLayerListener(LightLayer.BLOCK);
            // engineBlock.onBlockEmissionIncrease(pos, level); // TODO: 1.20: ?
        };
        enqueueRunnable(chunk, runnable);
    }

    @Override
    public void reset(boolean updateChunk) {
        runResetFor((LevelChunk) ((CraftChunk) getChunk()).getHandle(ChunkStatus.FULL), ((CraftBlock) block).getPosition());
        if (updateChunk) {
            // This runnable cast is necessary despite what your IDE may claim
            updateTask = Bukkit.getScheduler().runTaskLater(NMSHandler.getJavaPlugin(), (Runnable) this::sendNearbyChunkUpdates, 1);
        }
    }

    @Override
    public void update(int lightLevel, boolean updateChunk) {
        runResetFor((LevelChunk) ((CraftChunk) getChunk()).getHandle(ChunkStatus.FULL), ((CraftBlock) block).getPosition());
        updateTask = Bukkit.getScheduler().runTaskLater(NMSHandler.getJavaPlugin(), () -> {
            updateTask = null;
            runSetFor((LevelChunk) ((CraftChunk) chunk).getHandle(ChunkStatus.FULL), ((CraftBlock) block).getPosition(), lightLevel);
            if (updateChunk) {
                // This runnable cast is necessary despite what your IDE may claim
                updateTask = Bukkit.getScheduler().runTaskLater(NMSHandler.getJavaPlugin(), (Runnable) this::sendNearbyChunkUpdates, 1);
            }
        }, 1);
    }

    public static final Vector[] RELATIVE_CHUNKS = new Vector[] {
            new Vector(0, 0, 0),
            new Vector(-1, 0, 0), new Vector(1, 0, 0), new Vector(0, 0, -1), new Vector(0, 0, 1),
            new Vector(-1, 0, -1), new Vector(-1, 0, 1), new Vector(1, 0, -1), new Vector(1, 0, 1)
    };

    public void sendNearbyChunkUpdates() {
        sendNearbyChunkUpdates((LevelChunk) ((CraftChunk) getChunk()).getHandle(ChunkStatus.FULL));
    }

    public static void sendNearbyChunkUpdates(LevelChunk chunk) {
        ChunkPos pos = chunk.getPos();
        for (Vector vec : RELATIVE_CHUNKS) {
            ChunkAccess other = chunk.getLevel().getChunk(pos.x + vec.getBlockX(), pos.z + vec.getBlockZ(), ChunkStatus.FULL, false);
            if (other instanceof LevelChunk) {
                sendSingleChunkUpdate((LevelChunk) other);
            }
        }
    }

    public static void sendSingleChunkUpdate(LevelChunk chunk) {
        // TODO: 1.20: ?
        /*
        doNotCheck = true;
        LevelLightEngine lightEngine = chunk.getLevel().getChunkSource().getLightEngine();
        ChunkPos pos = chunk.getPos();
        //ClientboundLightUpdatePacket packet = new ClientboundLightUpdatePacket(pos, lightEngine, null, null, true); // TODO: 1.16: should 'trust edges' be true here?
        ((ServerChunkCache) chunk.getLevel().getChunkSource()).chunkMap.getPlayers(pos, false).forEach((player) -> {
            player.connection.send(packet);
        });
        doNotCheck = false;*/
    }
}
