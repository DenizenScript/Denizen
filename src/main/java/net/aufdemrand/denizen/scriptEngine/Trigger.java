package net.aufdemrand.denizen.scriptEngine;

import java.rmi.activation.ActivationException;

import net.aufdemrand.denizen.Denizen;

import org.bukkit.Bukkit;

public class Trigger {

	public Denizen plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");
	
	/* Activates the command class as a Denizen Command. Should be called on startup. */
	
	public void activateAs(String triggerName) throws ActivationException {
	
		/* Use Bukkit to reference Denizen Plugin */
		Denizen plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");
		
		/* Register command with Registry */
		if (plugin.triggerRegistry.registerTrigger(triggerName, this)) return;
		else 
			throw new ActivationException("Error activating Trigger with Trigger Registry.");
	}
	
}
