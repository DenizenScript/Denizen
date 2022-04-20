package com.denizenscript.denizen.utilities.packets;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.nms.NMSHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

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
            NMSHandler.packetHelper.setNetworkManagerFor(player);
        }
        NMSHandler.packetHelper.enableNetworkManager();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        NMSHandler.packetHelper.setNetworkManagerFor(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!event.getPlayer().isOnline()) { // Workaround: Paper misfires this event extra times after the player is already gone.
            event.setQuitMessage(null); // Block the message too since it's obviously not valid for the message to show a second time.
            // Also note that Paper literally has a commit that just removes a warning that would have helped catch issues like this because I guess they just like having errors https://i.alexgoodwin.media/i/misc/a8f5c3.png
        }
    }
}
