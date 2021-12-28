package com.denizenscript.denizen.utilities.blocks;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.packets.NetworkInterceptHelper;
import com.denizenscript.denizencore.objects.core.DurationTag;
import org.bukkit.Bukkit;
import org.bukkit.World;
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
            location = new LocationTag(location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getWorldName());
            FakeBlock block = byLocation.get(location);
            if (block != null) {
                return block;
            }
            block = new FakeBlock(player, location);
            byLocation.put(location, block);
            List<FakeBlock> chunkBlocks = byChunk.computeIfAbsent(block.chunkCoord, k -> new ArrayList<>());
            chunkBlocks.add(block);
            return block;
        }

        public void remove(FakeBlock block) {
            if (byLocation.remove(block.location) != null) {
                List<FakeBlock> chunkBlocks = byChunk.get(block.chunkCoord);
                if (chunkBlocks != null) {
                    chunkBlocks.remove(block);
                    if (chunkBlocks.isEmpty()) {
                        byChunk.remove(block.chunkCoord);
                    }
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

    public static void showFakeBlockTo(List<PlayerTag> players, LocationTag location, MaterialTag material, DurationTag duration, boolean sendNow) {
        NetworkInterceptHelper.enable();
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
            block.updateBlock(material, duration, sendNow);
        }
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

    public static HashMap<ChunkCoordinate, BukkitTask> scheduled = new HashMap<>();

    public static void scheduleChunkRefresh(World world, ChunkCoordinate coord) {
        BukkitTask task = scheduled.get(coord);
        if (task != null && !task.isCancelled()) {
            return;
        }
        scheduled.put(coord, Bukkit.getScheduler().runTaskLater(Denizen.getInstance(), () -> {
            world.refreshChunk(coord.x, coord.z);
            scheduled.remove(coord);
        }, 1));
    }

    public void cancelBlock() {
        if (currentTask != null) {
            currentTask.cancel();
            currentTask = null;
        }
        material = null;
        FakeBlockMap mapping = blocks.get(player.getUUID());
        mapping.remove(this);
        if (mapping.byChunk.isEmpty()) {
            blocks.remove(player.getUUID());
        }
        if (player.isOnline()) {
            scheduleChunkRefresh(location.getWorld(), chunkCoord);
            if (!NMSHandler.getVersion().isAtLeast(NMSVersion.v1_18)) {
                player.getPlayerEntity().sendBlockChange(location, location.getBlock().getBlockData());
            }
        }
    }

    private void updateBlock(MaterialTag material, DurationTag duration, boolean sendNow) {
        if (currentTask != null) {
            currentTask.cancel();
        }
        this.material = material;
        if (player.hasChunkLoaded(location.getChunk())) {
            if (sendNow || !NMSHandler.getVersion().isAtLeast(NMSVersion.v1_18)) {
                player.getPlayerEntity().sendBlockChange(location, material.getModernData());
            }
            scheduleChunkRefresh(location.getWorld(), chunkCoord);
        }
        if (duration != null && duration.getTicks() > 0) {
            currentTask = new BukkitRunnable() {
                @Override
                public void run() {
                    currentTask = null;
                    cancelBlock();
                }
            }.runTaskLater(Denizen.getInstance(), duration.getTicks());
        }
    }
}
