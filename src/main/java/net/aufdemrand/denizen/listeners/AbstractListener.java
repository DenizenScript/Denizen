package net.aufdemrand.denizen.listeners;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.commands.ArgumentHelper;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public abstract class AbstractListener implements Listener {

	Denizen plugin = (Denizen) Bukkit.getServer().getPluginManager().getPlugin("Denizen");
	String scriptName;
	Player thePlayer;
	String[] args;
	ArgumentHelper aH;

	public AbstractListener() {
		this.aH = plugin.getCommandRegistry().getArgumentHelper();
	}
	
	public abstract void build(String listenerId, Player thePlayer, String[] args, String scriptName);
	
	public abstract void save();

	public abstract void load(Player thePlayer, String listenerId);

	public abstract void complete(boolean forceable);

	public abstract void cancel();
	
	public abstract void report();

}
