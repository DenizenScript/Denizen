package net.aufdemrand.denizen.utilities.blocks;

import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.objects.Duration;
import net.minecraft.server.v1_9_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_9_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_9_R1.CraftWorld;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockLight {

    private static final Method playerChunkMethod;
    private static final Field dirtyCountField;
    private static final Map<Location, BlockLight> lightsByLocation = new HashMap<Location, BlockLight>();
    private static final Map<Chunk, List<BlockLight>> lightsByChunk = new HashMap<Chunk, List<BlockLight>>();
    private static final BukkitTask bukkitTask;
    private static final BlockFace[] adjacentFaces = new BlockFace[]{
            BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN
    };

    static {
        Method pcm = null;
        Field dcf = null;
        try {
            pcm = PlayerChunkMap.class.getDeclaredMethod("a", int.class, int.class, boolean.class);
            pcm.setAccessible(true);
            dcf = pcm.getReturnType().getDeclaredField("dirtyCount");
            dcf.setAccessible(true);
        }
        catch (Exception e) {
            dB.echoError(e);
        }
        playerChunkMethod = pcm;
        dirtyCountField = dcf;
        for (World worlds : Bukkit.getServer().getWorlds()) {
            WorldServer nmsWorld = ((CraftWorld) worlds).getHandle();
            IWorldAccess access = getIWorldAccess(worlds);
            nmsWorld.addIWorldAccess(access);
        }
        bukkitTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Map.Entry<Chunk, List<BlockLight>> entry : lightsByChunk.entrySet()) {
                    Chunk chunk = entry.getKey();
                    if (chunk.bukkitChunk.isLoaded()) {
                        List<BlockLight> blockLights = entry.getValue();
                        if (blockLights.isEmpty()) {
                            continue;
                        }
                        PlayerChunkMap playerChunkMap = blockLights.get(0).worldServer.getPlayerChunkMap();
                        for (BlockLight light : blockLights) {
                            light.reset(false);
                        }
                        updateChunk(chunk, playerChunkMap);
                        for (BlockLight light : blockLights) {
                            light.update(light.cachedLight, false);
                        }
                        updateChunk(chunk, playerChunkMap);
                    }
                }
            }
        }.runTaskTimer(DenizenAPI.getCurrentInstance(), 5, 5);
    }

    private final CraftWorld craftWorld;
    private final WorldServer worldServer;
    private final CraftChunk craftChunk;
    private final Chunk chunk;
    private final Block block;
    private final BlockPosition position;
    private final int originalLight;
    private int currentLight;
    private int cachedLight;
    private BukkitTask removeTask;

    private BlockLight(final Location location, Duration duration) {
        this.craftWorld = (CraftWorld) location.getWorld();
        this.worldServer = craftWorld.getHandle();
        this.craftChunk = (CraftChunk) location.getChunk();
        this.chunk = craftChunk.getHandle();
        this.block = location.getBlock();
        this.position = new BlockPosition(block.getX(), block.getY(), block.getZ());
        this.originalLight = block.getLightLevel();
        this.currentLight = originalLight;
        this.cachedLight = originalLight;
        this.removeLater(duration);
    }

    public static BlockLight createLight(Location location, int lightLevel, Duration duration) {
        location = location.getBlock().getLocation();
        BlockLight blockLight;
        if (lightsByLocation.containsKey(location)) {
            blockLight = lightsByLocation.get(location);
            if (blockLight.removeTask != null) {
                blockLight.removeTask.cancel();
                blockLight.removeTask = null;
            }
            blockLight.reset(true);
            blockLight.removeLater(duration);
        }
        else {
            blockLight = new BlockLight(location, duration);
            lightsByLocation.put(location, blockLight);
            if (!lightsByChunk.containsKey(blockLight.chunk)) {
                lightsByChunk.put(blockLight.chunk, new ArrayList<BlockLight>());
            }
            lightsByChunk.get(blockLight.chunk).add(blockLight);
        }
        blockLight.update(lightLevel, true);
        return blockLight;
    }

    public static void removeLight(Location location) {
        location = location.getBlock().getLocation();
        if (lightsByLocation.containsKey(location)) {
            BlockLight blockLight = lightsByLocation.get(location);
            blockLight.reset(true);
            if (blockLight.removeTask != null) {
                blockLight.removeTask.cancel();
            }
            lightsByLocation.remove(location);
            lightsByChunk.get(blockLight.chunk).remove(blockLight);
            if (lightsByChunk.get(blockLight.chunk).isEmpty()) {
                lightsByChunk.remove(blockLight.chunk);
            }
        }
    }

    private void removeLater(Duration duration) {
        if (duration != null) {
            long ticks = duration.getTicks();
            if (ticks > 0) {
                this.removeTask = new BukkitRunnable() {
                    @Override
                    public void run() {
                        removeTask = null;
                        removeLight(block.getLocation());
                    }
                }.runTaskLater(DenizenAPI.getCurrentInstance(), ticks);
            }
        }
    }

    private void reset(boolean updateChunk) {
        this.update(originalLight, updateChunk);
    }

    private void update(int lightLevel, boolean updateChunk) {
        if (this.currentLight == lightLevel) {
            return;
        }
        else if (this.originalLight == lightLevel) {
            worldServer.c(EnumSkyBlock.BLOCK, position);
        }
        else {
            worldServer.a(EnumSkyBlock.BLOCK, position, lightLevel);
            Block adjacentAir = null;
            for (BlockFace face : adjacentFaces) {
                if (position.getY() == 0 && face == BlockFace.DOWN) {
                    continue;
                }
                if (position.getY() == (craftWorld.getMaxHeight() - 1) && face == BlockFace.UP) {
                    continue;
                }
                Block possible = block.getRelative(face);
                if (possible.getType() == Material.AIR) {
                    adjacentAir = possible;
                    break;
                }
            }
            if (adjacentAir != null) {
                worldServer.x(new BlockPosition(adjacentAir.getX(), adjacentAir.getY(), adjacentAir.getZ()));
            }
            this.cachedLight = lightLevel;
        }
        if (updateChunk) {
            updateChunk(chunk, worldServer.getPlayerChunkMap());
        }
        this.currentLight = lightLevel;
    }

    private static void updateChunk(Chunk chunk, PlayerChunkMap playerChunkMap) {
        int cX = chunk.locX;
        int cZ = chunk.locZ;
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Object pChunk = getPlayerChunk(playerChunkMap, cX + x, cZ + z);
                if (pChunk == null) {
                    continue;
                }
                BlockLight.setDirtyCount(pChunk);
            }
        }
    }

    private static Object getPlayerChunk(PlayerChunkMap map, int x, int z) {
        try {
            return playerChunkMethod.invoke(map, x, z, false);
        }
        catch (Exception e) {
            dB.echoError(e);
        }
        return null;
    }

    private static void setDirtyCount(Object playerChunk) {
        try {
            int dirtyCount = dirtyCountField.getInt(playerChunk);
            if (dirtyCount > 0 && dirtyCount < 64) {
                dirtyCountField.set(playerChunk, 64);
            }
        }
        catch (Exception e) {
            dB.echoError(e);
        }
    }

    private static IWorldAccess getIWorldAccess(World world) {
        final PlayerChunkMap map = ((CraftWorld) world).getHandle().getPlayerChunkMap();
        return new IWorldAccess() {
            @Override
            public void a(BlockPosition position) {
                map.flagDirty(position);
            }

            @Override
            public void b(BlockPosition position) {
                map.flagDirty(position);
            }

            @Override
            public void b(int arg0, BlockPosition arg1, int arg2) {
            }

            @Override
            public void a(EntityHuman arg0, int arg1, BlockPosition arg2, int arg3) {
            }

            @Override
            public void a(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
            }

            @Override
            public void a(int arg0, BlockPosition arg1, int arg2) {
            }

            @Override
            public void a(String arg0, double arg1, double arg2, double arg3, float arg4, float arg5) {
            }

            @Override
            public void a(EntityHuman arg0, String arg1, double arg2, double arg3, double arg4, float arg5, float arg6) {
            }

            @Override
            public void a(int arg0, boolean arg1, double arg2, double arg3, double arg4, double arg5, double arg6, double arg7, int... arg8) {
            }

            @Override
            public void a(String arg0, BlockPosition arg1) {
            }

            @Override
            public void a(Entity arg0) {
            }

            @Override
            public void b(Entity arg0) {
            }
        };
    }
}
