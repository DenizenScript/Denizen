package com.denizenscript.denizen.utilities.blocks;

import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.DenizenAPI;
import com.denizenscript.denizencore.objects.core.DurationTag;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

/**
 * Creates a temporary fake block and shows it to a PlayerTag.
 */
public class FakeBlock {

    public static class FakeBlockMap {

        public Map<LocationTag, FakeBlock> byLocation = new HashMap<>();

        public Map<ChunkCoordinate, List<FakeBlock>> byChunk = new HashMap<>();

        public FakeBlock getOrAdd(PlayerTag player, LocationTag location) {
            FakeBlock block = byLocation.get(location);
            if (block != null) {
                return block;
            }
            block = new FakeBlock(player, location);
            byLocation.put(location, block);
            List<FakeBlock> chunkBlocks = byChunk.get(block.chunkCoord);
            if (chunkBlocks == null) {
                chunkBlocks = new ArrayList<>();
                byChunk.put(block.chunkCoord, chunkBlocks);
            }
            chunkBlocks.add(block);
            return block;
        }

        public void remove(FakeBlock block) {
            if (byLocation.remove(block.location) != null) {
                List<FakeBlock> chunkBlocks = byChunk.get(block.chunkCoord);
                if (chunkBlocks != null) {
                    chunkBlocks.remove(block);
                }
            }
        }
    }

    public final static Map<UUID, FakeBlockMap> blocks = new HashMap<>();

    public static FakeBlock getFakeBlockFor(UUID id, LocationTag location) {
        FakeBlockMap map = blocks.get(id);
        if (map == null) {
            return null;
        }
        return map.byLocation.get(location);
    }

    public static List<FakeBlock> getFakeBlocksFor(UUID id, ChunkCoordinate chunkCoord) {
        FakeBlockMap map = blocks.get(id);
        if (map == null) {
            return null;
        }
        return map.byChunk.get(chunkCoord);
    }

    public final PlayerTag player;
    public final LocationTag location;
    public final ChunkCoordinate chunkCoord;
    public MaterialTag material;
    public BukkitTask currentTask = null;

    private FakeBlock(PlayerTag player, LocationTag location) {
        this.player = player;
        this.location = location;
        this.chunkCoord = new ChunkCoordinate(location);
    }

    public static void showFakeBlockTo(List<PlayerTag> players, LocationTag location, MaterialTag material, DurationTag duration) {
        for (PlayerTag player : players) {
            if (!player.isOnline() || !player.isValid()) {
                continue;
            }
            UUID uuid = player.getPlayerEntity().getUniqueId();
            FakeBlockMap playerBlocks = blocks.get(uuid);
            if (playerBlocks == null) {
                playerBlocks = new FakeBlockMap();
                blocks.put(uuid, playerBlocks);
            }
            FakeBlock block = playerBlocks.getOrAdd(player, location);
            block.updateBlock(material, duration);
        }
        lastChunkRefresh.clear();
    }

    public static void stopShowingTo(List<PlayerTag> players, final LocationTag location) {
        for (PlayerTag player : players) {
            FakeBlockMap playerBlocks = blocks.get(player.getPlayerEntity().getUniqueId());
            if (playerBlocks != null) {
                FakeBlock block = playerBlocks.byLocation.get(location);
                if (block != null) {
                    block.cancelBlock();
                }
            }
        }
    }

    public static HashMap<ChunkCoordinate, Long> lastChunkRefresh = new HashMap<>();

    public void cancelBlock() {
        if (currentTask != null) {
            currentTask.cancel();
            currentTask = null;
        }
        material = null;
        if (player.isOnline()) {
            location.getBlock().getState().update();
            Long l = lastChunkRefresh.get(chunkCoord);
            if (l == null || l < location.getWorld().getFullTime()) {
                lastChunkRefresh.put(chunkCoord, location.getWorld().getFullTime());
                location.getWorld().refreshChunk(chunkCoord.x, chunkCoord.z);
            }
        }
        FakeBlockMap mapping = blocks.get(player.getOfflinePlayer().getUniqueId());
        mapping.remove(this);
    }

    private void updateBlock(MaterialTag material, DurationTag duration) {
        if (currentTask != null) {
            currentTask.cancel();
        }
        this.material = material;
        if (player.hasChunkLoaded(location.getChunk())) {
            material.getModernData().sendFakeChangeTo(player.getPlayerEntity(), location);
            if (material.getMaterial().name().endsWith("_BANNER")) { // Banners are weird
                location.getWorld().refreshChunk(chunkCoord.x, chunkCoord.z);
            }
        }
        if (duration != null && duration.getTicks() > 0) {
            currentTask = new BukkitRunnable() {
                @Override
                public void run() {
                    currentTask = null;
                    cancelBlock();
                }
            }.runTaskLater(DenizenAPI.getCurrentInstance(), duration.getTicks());
        }
    }
}

