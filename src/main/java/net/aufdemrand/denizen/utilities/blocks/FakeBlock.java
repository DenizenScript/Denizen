package net.aufdemrand.denizen.utilities.blocks;


import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import org.bukkit.Bukkit;

public class FakeBlock {

    public FakeBlock(final dPlayer player, final dLocation location, final dMaterial material, final Duration duration) {

        player.getPlayerEntity()
                .sendBlockChange(location, material.getMaterial(), material.getMaterialData().getData());

        Bukkit.getScheduler().scheduleSyncDelayedTask(DenizenAPI.getCurrentInstance(),
                new Runnable() {
                    @Override
                    public void run() {
                        player.getPlayerEntity()
                                .sendBlockChange(location, location.getBlock().getType(),
                                        location.getBlock().getData());
                    }
                }, duration.getTicks());
    }

}

