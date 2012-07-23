package net.aufdemrand.denizen.scriptEngine;

import java.rmi.activation.ActivationException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.scriptEngine.triggers.ChatTrigger;
import net.aufdemrand.denizen.scriptEngine.triggers.ClickTrigger;
import net.aufdemrand.denizen.scriptEngine.triggers.DamageTrigger;
import net.aufdemrand.denizen.scriptEngine.triggers.DeathTrigger;
import net.aufdemrand.denizen.scriptEngine.triggers.LocationTrigger;
import net.aufdemrand.denizen.scriptEngine.triggers.ProximityTrigger;

public class TriggerRegistry {

	private Map<String, Trigger> triggers = new HashMap<String, Trigger>();

	public Denizen plugin;

	public TriggerRegistry(Denizen denizen) {
		plugin = denizen;
	}


	public boolean registerTrigger(String triggerName, Trigger triggerClass) {
		this.triggers.put(triggerName.toUpperCase(), triggerClass);
		plugin.getLogger().log(Level.INFO, "Loaded " + triggerName + " Trigger successfully!");
		return true;
	}


	public Map<String, Trigger> listTriggers() {
		return triggers;
	}

	
	public Trigger getTrigger(String triggerName) {
		if (triggers.containsKey(triggerName.toUpperCase()))
			return triggers.get(triggerName);
		else
			return null;
	}

	
	public void registerCoreTriggers() {

		LocationTrigger locationTrigger = new LocationTrigger();
		ChatTrigger chatTrigger = new ChatTrigger();
		ClickTrigger clickTrigger = new ClickTrigger();
		DamageTrigger damageTrigger = new DamageTrigger();
		ProximityTrigger proximityTrigger = new ProximityTrigger();
		DeathTrigger deathTrigger = new DeathTrigger();
		
		/* Activate Denizen Triggers */
		try {
			
			locationTrigger.activateAs("Location");
			chatTrigger.activateAs("Chat");
			clickTrigger.activateAs("Click");
			damageTrigger.activateAs("Damage");
			proximityTrigger.activateAs("Proximity");
			deathTrigger.activateAs("Death");
		
		} catch (ActivationException e) {
			plugin.getLogger().log(Level.SEVERE, "Oh no! Denizen has run into a problem registering the core triggers!");
			e.printStackTrace();
		}

		/* Register Listener events. */
		plugin.getServer().getPluginManager().registerEvents(locationTrigger, plugin);
		plugin.getServer().getPluginManager().registerEvents(chatTrigger, plugin);
		plugin.getServer().getPluginManager().registerEvents(clickTrigger, plugin);
		plugin.getServer().getPluginManager().registerEvents(damageTrigger, plugin);
		plugin.getServer().getPluginManager().registerEvents(deathTrigger, plugin);
		plugin.getServer().getPluginManager().registerEvents(proximityTrigger, plugin);
		
	}


}
