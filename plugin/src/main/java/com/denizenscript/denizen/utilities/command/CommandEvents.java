package com.denizenscript.denizen.utilities.command;

import com.denizenscript.denizen.Denizen;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandSendEvent;

public class CommandEvents implements Listener {

    public CommandEvents() {
        Bukkit.getPluginManager().registerEvents(this, Denizen.getInstance());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerCommandSend(PlayerCommandSendEvent event) {
        event.getCommands().remove("denizenclickable");
        event.getCommands().remove("denizen:denizenclickable");
    }
}
