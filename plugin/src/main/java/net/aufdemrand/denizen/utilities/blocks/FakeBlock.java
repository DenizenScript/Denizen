package net.aufdemrand.denizen.utilities.blocks;

import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dMaterial;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.Duration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

/**
 * Creates a temporary fake block and shows it to a dPlayer.
 */
public class FakeBlock {

    private final static Map<UUID, Map<dLocation, FakeBlock>> blocks = new HashMap<>();
    private final static Map<dLocation, FakeBlock> blocksByLocation = new HashMap<>();

    private final dPlayer player;
    private final dLocation location;
    private dMaterial material;
    private long cancelTime = -1;
    private BukkitTask currentTask = null;

    private FakeBlock(dPlayer player, dLocation location) {
        this.player = player;
        this.location = location;
    }

    public static void showFakeBlockTo(List<dPlayer> players, dLocation location, dMaterial material, Duration duration) {
        for (dPlayer player : players) {
            if (!player.isOnline() || !player.isValid()) {
                continue;
            }
            UUID uuid = player.getPlayerEntity().getUniqueId();
            if (!blocks.containsKey(uuid)) {
                blocks.put(uuid, new HashMap<>());
            }
            Map<dLocation, FakeBlock> playerBlocks = blocks.get(uuid);
            if (!playerBlocks.containsKey(location)) {
                playerBlocks.put(location, new FakeBlock(player, location));
            }
            playerBlocks.get(location).updateBlock(material, duration.getTicks());
        }
    }

    public static void stopShowingTo(List<dPlayer> players, final dLocation location) {
        final List<UUID> uuids = new ArrayList<>();
        for (dPlayer player : players) {
            if (!player.isOnline() || !player.isValid()) {
                continue;
            }
            UUID uuid = player.getPlayerEntity().getUniqueId();
            uuids.add(uuid);
            if (blocks.containsKey(uuid)) {
                Map<dLocation, FakeBlock> playerBlocks = blocks.get(uuid);
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
                    Map<dLocation, FakeBlock> playerBlocks = blocks.get(uuid);
                    if (playerBlocks.containsKey(location)) {
                        playerBlocks.get(location).updateBlock();
                    }
                }
            }
        }.runTaskLater(DenizenAPI.getCurrentInstance(), 2);
    }

    public static Map<UUID, Map<dLocation, FakeBlock>> getBlocks() {
        return blocks;
    }

    private void cancelBlock() {
        if (currentTask != null) {
            currentTask.cancel();
            currentTask = null;
        }
        cancelTime = -1;
        material = null;
        location.getBlockState().update();
        blocks.get(player.getOfflinePlayer().getUniqueId()).remove(location);
    }


    public void updateBlock() {
        if (material != null) {
            updateBlock(material, cancelTime == -1 ? 0 : cancelTime - location.getWorld().getFullTime());
        }
    }

    private void updateBlock(dMaterial material, long ticks) {
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

