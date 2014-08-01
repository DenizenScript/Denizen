package net.aufdemrand.denizen.utilities.blocks;


import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import org.bukkit.Bukkit;

/**
 * Creates a temporary fake block and shows it to a dPlayer.
 *
 */
public class FakeBlock {

    public FakeBlock(final dPlayer player, final dLocation location,
                     final dMaterial material, final Duration duration) {

        // Send the block to the player
        player.getPlayerEntity().sendBlockChange(
                location,
                material.getMaterial(),
                material.getMaterialData().getData());

        if (duration.getTicks() > 0)
        {
            // Schedule removal of the block (and show the original block again)
            Bukkit.getScheduler().scheduleSyncDelayedTask(DenizenAPI.getCurrentInstance(),
                new Runnable() {
                    @Override
                    public void run() {
                        player.getPlayerEntity().sendBlockChange(
                                location,
                                location.getBlock().getType(),
                                location.getBlock().getData());
                    }
                }, duration.getTicks());
        }
    }
}

