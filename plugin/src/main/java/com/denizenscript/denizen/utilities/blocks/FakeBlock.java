package com.denizenscript.denizen.utilities.blocks;

import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.DenizenAPI;
import com.denizenscript.denizencore.objects.core.DurationTag;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

/**
 * Creates a temporary fake block and shows it to a PlayerTag.
 */
public class FakeBlock {

    private final static Map<UUID, Map<Location, FakeBlock>> blocks = new HashMap<>();

    private final PlayerTag player;
    private final Location location;
    private MaterialTag material;
    private long cancelTime = -1;
    private BukkitTask currentTask = null;

    private FakeBlock(PlayerTag player, Location location) {
        this.player = player;
        this.location = location;
    }

    public static void showFakeBlockTo(List<PlayerTag> players, Location location, MaterialTag material, DurationTag duration) {
        for (PlayerTag player : players) {
            if (!player.isOnline() || !player.isValid()) {
                continue;
            }
            UUID uuid = player.getPlayerEntity().getUniqueId();
            if (!blocks.containsKey(uuid)) {
                blocks.put(uuid, new HashMap<>());
            }
            Map<Location, FakeBlock> playerBlocks = blocks.get(uuid);
            if (!playerBlocks.containsKey(location)) {
                playerBlocks.put(location, new FakeBlock(player, location));
            }
            playerBlocks.get(location).updateBlock(material, duration.getTicks());
        }
    }

    public static void stopShowingTo(List<PlayerTag> players, final LocationTag location) {
        final List<UUID> uuids = new ArrayList<>();
        for (PlayerTag player : players) {
            if (!player.isOnline() || !player.isValid()) {
                continue;
            }
            UUID uuid = player.getPlayerEntity().getUniqueId();
            uuids.add(uuid);
            if (blocks.containsKey(uuid)) {
                Map<Location, FakeBlock> playerBlocks = blocks.get(uuid);
                if (playerBlocks.containsKey(location)) {
                    playerBlocks.get(location).cancelBlock();
                }
            }
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                for (UUID uuid : blocks.keySet()) {
                    if (uuids.contains(uuid)) {
                        continue;
                    }
                    Map<Location, FakeBlock> playerBlocks = blocks.get(uuid);
                    if (playerBlocks.containsKey(location)) {
                        playerBlocks.get(location).updateBlock();
                    }
                }
            }
        }.runTaskLater(DenizenAPI.getCurrentInstance(), 2);
    }

    public static Map<UUID, Map<Location, FakeBlock>> getBlocks() {
        return blocks;
    }

    private void cancelBlock() {
        if (currentTask != null) {
            currentTask.cancel();
            currentTask = null;
        }
        cancelTime = -1;
        material = null;
        location.getBlock().getState().update();
        blocks.get(player.getOfflinePlayer().getUniqueId()).remove(location);
    }


    public void updateBlock() {
        if (material != null) {
            updateBlock(material, cancelTime == -1 ? 0 : cancelTime - location.getWorld().getFullTime());
        }
    }

    private void updateBlock(MaterialTag material, long ticks) {
        if (currentTask != null) {
            currentTask.cancel();
        }
        this.material = material;
        if (!player.hasChunkLoaded(location.getChunk())) {
            return;
        }
        if (material.hasModernData()) {
            material.getModernData().sendFakeChangeTo(player.getPlayerEntity(), location);
        }
        else {
            player.getPlayerEntity().sendBlockChange(location, material.getMaterial(),
                    material.getMaterialData().getData());
        }
        if (ticks > 0) {
            cancelTime = location.getWorld().getFullTime() + ticks;
            currentTask = new BukkitRunnable() {
                @Override
                public void run() {
                    currentTask = null;
                    if (player.isValid() && player.isOnline()) {
                        cancelBlock();
                    }
                }
            }.runTaskLater(DenizenAPI.getCurrentInstance(), ticks);
        }
    }

    static {
        final FakeBlockListeners listeners = new FakeBlockListeners();
    }

    public static class FakeBlockListeners implements Listener {
        public FakeBlockListeners() {
            DenizenAPI.getCurrentInstance().getServer().getPluginManager()
                    .registerEvents(this, DenizenAPI.getCurrentInstance());
        }

        @EventHandler
        public void playerQuit(PlayerQuitEvent event) {
            UUID uuid = event.getPlayer().getUniqueId();
            if (blocks.containsKey(uuid)) {
                blocks.remove(uuid);
            }
        }
    }
}

