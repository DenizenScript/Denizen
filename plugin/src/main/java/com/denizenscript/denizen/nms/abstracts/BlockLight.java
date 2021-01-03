package com.denizenscript.denizen.nms.abstracts;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.utilities.blocks.ChunkCoordinate;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BlockLight {

    public static final Map<Location, BlockLight> lightsByLocation = new HashMap<>();
    public static final Map<ChunkCoordinate, List<BlockLight>> lightsByChunk = new HashMap<>();

    public final Block block;
    public final ChunkCoordinate chunkCoord;
    public Chunk chunk;
    public final int originalLight;
    public int currentLight;
    public int cachedLight;
    public int intendedLevel;
    public BukkitTask removeTask;
    public BukkitTask updateTask;

    public Chunk getChunk() {
        chunk = Bukkit.getWorld(chunkCoord.worldName).getChunkAt(chunkCoord.x, chunkCoord.z);
        return chunk;
    }

    protected BlockLight(Location location, long ticks) {
        this.block = location.getBlock();
        this.chunk = location.getChunk();
        this.chunkCoord = new ChunkCoordinate(chunk);
        this.originalLight = block.getLightFromBlocks();
        this.currentLight = originalLight;
        this.cachedLight = originalLight;
        this.intendedLevel = originalLight;
        this.removeLater(ticks);
    }

    public void removeLater(long ticks) {
        if (ticks > 0) {
            this.removeTask = new BukkitRunnable() {
                @Override
                public void run() {
                    removeTask = null;
                    removeLight(block.getLocation());
                }
            }.runTaskLater(NMSHandler.getJavaPlugin(), ticks);
        }
    }

    public static void removeLight(Location location) {
        location = location.getBlock().getLocation();
        BlockLight blockLight = lightsByLocation.get(location);
        if (blockLight != null) {
            if (blockLight.updateTask != null) {
                blockLight.updateTask.cancel();
                blockLight.updateTask = null;
            }
            blockLight.reset(true);
            if (blockLight.removeTask != null) {
                blockLight.removeTask.cancel();
                blockLight.removeTask = null;
            }
            lightsByLocation.remove(location);
            List<BlockLight> lights = lightsByChunk.get(blockLight.chunkCoord);
            lights.remove(blockLight);
            if (lights.isEmpty()) {
                lightsByChunk.remove(blockLight.chunkCoord);
            }
        }
    }

    public void reset(boolean updateChunk) {
        this.update(originalLight, updateChunk);
    }

    public abstract void update(int lightLevel, boolean updateChunk);
}
