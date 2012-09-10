package net.aufdemrand.denizen.listeners;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.commands.ArgumentHelper;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public abstract class AbstractListener implements Listener {

	Denizen plugin = (Denizen) Bukkit.getServer().getPluginManager().getPlugin("Denizen");
	String scriptName;
	Player thePlayer;
	String[] args;
	ArgumentHelper aH;

	public AbstractListener(Player thePlayer, String[] args, String scriptName) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		this.thePlayer = thePlayer;
		this.args = args;
		this.scriptName = scriptName;
		this.aH = plugin.getCommandRegistry().getArgumentHelper();
	}
	
	public abstract void save();

	public abstract void load(Player thePlayer);

	public abstract void complete();

	public abstract void cancel();

}
