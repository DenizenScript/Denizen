package net.aufdemrand.denizen.utilities.scoreboard;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

public class Helper {
	
	//Helper: here are simple (little!) operations (we need no other .class file)
	
    private final ScoreboardAPI plugin;
    
	//constructor - init plugin
    public Helper(final ScoreboardAPI plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null");
        }

        this.plugin = plugin;
    }
    

}
