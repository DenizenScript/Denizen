package net.aufdemrand.denizen.utilities.blocks;


import net.aufdemrand.denizen.objects.Duration;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dMaterial;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Creates a temporary fake block and shows it to a dPlayer.
 *
 */
public class FakeBlock {

    private final static Map<UUID, Map<dLocation, FakeBlock>> blocks = new HashMap<UUID, Map<dLocation, FakeBlock>>();

    private final dPlayer player;
    private final dLocation location;
    private dMaterial material;
    private BukkitTask currentTask = null;

    private FakeBlock(dPlayer player, dLocation location) {
        this.player = player;
        this.location = location;
    }

    public static void showFakeBlockTo(dPlayer player, dLocation location, dMaterial material, Duration duration) {
        UUID uuid = player.getPlayerEntity().getUniqueId();
        if (!blocks.containsKey(uuid))
            blocks.put(uuid, new HashMap<dLocation, FakeBlock>());
        Map<dLocation, FakeBlock> playerBlocks = blocks.get(uuid);
        if (!playerBlocks.containsKey(location)) {
            playerBlocks.put(location, new FakeBlock(player, location));
        }
        playerBlocks.get(location).updateBlock(material, duration);
    }

    public static void stopShowingTo(dPlayer player, dLocation location) {
        UUID uuid = player.getPlayerEntity().getUniqueId();
        if (blocks.containsKey(uuid)) {
            Map<dLocation, FakeBlock> playerBlocks = blocks.get(uuid);
            if (playerBlocks.containsKey(location))
                playerBlocks.get(location).cancelBlock();
        }
    }

    private void cancelBlock() {
        if (currentTask != null) {
            currentTask.cancel();
            currentTask = null;
        }
        Block block = location.getBlock();
        player.getPlayerEntity().sendBlockChange(location, block.getType(), block.getData());
    }

    private void updateBlock(dMaterial material, Duration duration) {
        if (currentTask != null)
            currentTask.cancel();
        player.getPlayerEntity().sendBlockChange(location, material.getMaterial(),
                material.getMaterialData().getData());
        currentTask = duration.getTicks() > 0 ? new BukkitRunnable() {
            @Override
            public void run() {
                currentTask = null;
                if (player.isValid() && player.isOnline())
                    cancelBlock();
            }
        }.runTaskLater(DenizenAPI.getCurrentInstance(), duration.getTicks()) : null;
    }
}

