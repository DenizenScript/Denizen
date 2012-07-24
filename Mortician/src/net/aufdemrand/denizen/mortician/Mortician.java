package net.aufdemrand.denizen.mortician;

import java.rmi.activation.ActivationException;

import net.aufdemrand.denizen.Denizen;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Mortician extends JavaPlugin {
	
	DeathTrigger deathTrigger = new DeathTrigger();
	
	
	@Override
	public void onEnable() {
		
		// Register Denizen Trigger
		try { deathTrigger.activateAs("death"); } catch (ActivationException e) { e.printStackTrace(); }

		// Register bukkit Listener
		getServer().getPluginManager().registerEvents(deathTrigger, this);
		
	}
	
	@Override
	public void onDisable() {
		
	}

	

}
