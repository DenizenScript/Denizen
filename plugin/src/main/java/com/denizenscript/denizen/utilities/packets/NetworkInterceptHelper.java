package com.denizenscript.denizen.utilities.packets;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.nms.NMSHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class NetworkInterceptHelper implements Listener {

    public static boolean isEnabled = false;

    public static void enable() {
        if (isEnabled) {
            return;
        }
        isEnabled = true;
        DenizenPacketHandler.instance = new DenizenPacketHandler();
        Bukkit.getPluginManager().registerEvents(new NetworkInterceptHelper(), Denizen.getInstance());
        for (Player player : Bukkit.getOnlinePlayers()) {
            NMSHandler.getPacketHelper().setNetworkManagerFor(player);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        NMSHandler.getPacketHelper().setNetworkManagerFor(event.getPlayer());
    }
}
