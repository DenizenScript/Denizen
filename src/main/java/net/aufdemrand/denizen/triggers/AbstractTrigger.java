package net.aufdemrand.denizen.triggers;

import java.rmi.activation.ActivationException;

import net.aufdemrand.denizen.Denizen;

import org.bukkit.Bukkit;

public abstract class AbstractTrigger {

	protected Denizen plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");
	public String triggerName;
	private boolean enabledByDefault = false;
	
	public void echoDebug(String message, String argument) {
		if (plugin.debugMode)
			plugin.getLogger().info("[" + triggerName + " Trigger]: "+ message);
	}
	
	/* Activates the command class as a Denizen Command. Should be called on startup. */
	
	public void activateAs(String triggerName) throws ActivationException {
	
		/* If more than one word, error. */
		if (triggerName.split(" ").length > 1) throw new ActivationException("Trigger names can only be one word.");
		
		/* Use Bukkit to reference Denizen Plugin */
		Denizen plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");
		
		/* Register command with Registry */
		if (plugin.getTriggerRegistry().registerTrigger(triggerName, this)) return;
		else 
			throw new ActivationException("Error activating Trigger with Trigger Registry.");
	}
	
	public void setEnabledByDefault(boolean enabledByDefault) {
		this.enabledByDefault = enabledByDefault;
	}
	
	public boolean getEnabledByDefault() {
		return this.enabledByDefault;
	}
	
	
	
}
