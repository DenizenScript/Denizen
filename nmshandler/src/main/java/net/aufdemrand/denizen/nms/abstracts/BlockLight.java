package net.aufdemrand.denizen.nms.abstracts;

import net.aufdemrand.denizen.nms.NMSHandler;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BlockLight {

    protected static final Map<Location, BlockLight> lightsByLocation = new HashMap<>();
    protected static final Map<Chunk, List<BlockLight>> lightsByChunk = new HashMap<>();
    protected static final BlockFace[] adjacentFaces = new BlockFace[] {
            BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN
    };

    protected final Block block;
    public final Chunk chunk;
    protected final int originalLight;
    protected int currentLight;
    public int cachedLight;
    public BukkitTask removeTask;

    protected BlockLight(Location location, long ticks) {
        this.block = location.getBlock();
        this.chunk = location.getChunk();
        this.originalLight = block.getLightLevel();
        this.currentLight = originalLight;
        this.cachedLight = originalLight;
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

    public void reset(boolean updateChunk) {
        this.update(originalLight, updateChunk);
    }

    public abstract void update(int lightLevel, boolean updateChunk);
}
