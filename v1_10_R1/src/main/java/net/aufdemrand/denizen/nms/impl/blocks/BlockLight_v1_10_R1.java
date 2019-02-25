package net.aufdemrand.denizen.nms.impl.blocks;

import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.nms.abstracts.BlockLight;
import net.aufdemrand.denizencore.utilities.debugging.dB;
import net.minecraft.server.v1_10_R1.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_10_R1.CraftWorld;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class BlockLight_v1_10_R1 extends BlockLight {

    private static final Method playerChunkMethod;
    private static final Field dirtyCountField;
    private static final BukkitTask bukkitTask;

    private static final Set<UUID> worlds = new HashSet<UUID>();

    static {
        Method pcm = null;
        Field dcf = null;
        try {
            pcm = PlayerChunkMap.class.getDeclaredMethod("c", int.class, int.class);
            pcm.setAccessible(true);
            dcf = pcm.getReturnType().getDeclaredField("dirtyCount");
            dcf.setAccessible(true);
        }
        catch (Exception e) {
            dB.echoError(e);
        }
        playerChunkMethod = pcm;
        dirtyCountField = dcf;
        bukkitTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Map.Entry<org.bukkit.Chunk, List<BlockLight>> entry : lightsByChunk.entrySet()) {
                    org.bukkit.Chunk chunk = entry.getKey();
                    if (chunk.isLoaded()) {
                        List<BlockLight> blockLights = entry.getValue();
                        if (blockLights.isEmpty()) {
                            continue;
                        }
                        PlayerChunkMap playerChunkMap = ((BlockLight_v1_10_R1) blockLights.get(0)).worldServer.getPlayerChunkMap();
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
        }.runTaskTimer(NMSHandler.getJavaPlugin(), 5, 5);
    }

    private final CraftWorld craftWorld;
    private final WorldServer worldServer;
    private final BlockPosition position;

    private BlockLight_v1_10_R1(Location location, long ticks) {
        super(location, ticks);
        this.craftWorld = (CraftWorld) location.getWorld();
        this.worldServer = craftWorld.getHandle();
        if (!worlds.contains(craftWorld.getUID())) {
            IWorldAccess access = getIWorldAccess(craftWorld);
            worldServer.addIWorldAccess(access);
            worlds.add(craftWorld.getUID());
        }
        this.position = new BlockPosition(block.getX(), block.getY(), block.getZ());
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
            blockLight.reset(true);
            blockLight.removeLater(ticks);
        }
        else {
            blockLight = new BlockLight_v1_10_R1(location, ticks);
            lightsByLocation.put(location, blockLight);
            if (!lightsByChunk.containsKey(blockLight.chunk)) {
                lightsByChunk.put(blockLight.chunk, new ArrayList<BlockLight>());
            }
            lightsByChunk.get(blockLight.chunk).add(blockLight);
        }
        blockLight.update(lightLevel, true);
        return blockLight;
    }

    @Override
    public void update(int lightLevel, boolean updateChunk) {
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
                worldServer.w(new BlockPosition(adjacentAir.getX(), adjacentAir.getY(), adjacentAir.getZ()));
            }
            this.cachedLight = lightLevel;
        }
        if (updateChunk) {
            updateChunk(chunk, worldServer.getPlayerChunkMap());
        }
        this.currentLight = lightLevel;
    }

    private static void updateChunk(org.bukkit.Chunk chunk, PlayerChunkMap playerChunkMap) {
        int cX = chunk.getX();
        int cZ = chunk.getZ();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Object pChunk = getPlayerChunk(playerChunkMap, cX + x, cZ + z);
                if (pChunk == null) {
                    continue;
                }
                setDirtyCount(pChunk);
            }
        }
    }

    private static Object getPlayerChunk(PlayerChunkMap map, int x, int z) {
        try {
            return playerChunkMethod.invoke(map, x, z);
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
            public void a(net.minecraft.server.v1_10_R1.World world, BlockPosition blockPosition, IBlockData iBlockData, IBlockData iBlockData1, int i) {

            }

            @Override
            public void a(BlockPosition position) {
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
            public void a(EntityHuman entityHuman, SoundEffect soundEffect, SoundCategory soundCategory, double v, double v1, double v2, float v3, float v4) {

            }

            @Override
            public void a(SoundEffect soundEffect, BlockPosition blockPosition) {

            }

            @Override
            public void a(int arg0, BlockPosition arg1, int arg2) {
            }

            @Override
            public void a(int arg0, boolean arg1, double arg2, double arg3, double arg4, double arg5, double arg6, double arg7, int... arg8) {
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
