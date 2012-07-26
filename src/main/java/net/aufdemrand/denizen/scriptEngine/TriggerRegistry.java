package net.aufdemrand.denizen.scriptEngine;

import java.rmi.activation.ActivationException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.scriptEngine.triggers.ChatTrigger;
import net.aufdemrand.denizen.scriptEngine.triggers.ClickTrigger;
import net.aufdemrand.denizen.scriptEngine.triggers.DamageTrigger;
import net.aufdemrand.denizen.scriptEngine.triggers.LocationTrigger;
import net.aufdemrand.denizen.scriptEngine.triggers.ProximityTrigger;

public class TriggerRegistry {

	private Map<String, AbstractTrigger> triggers = new HashMap<String, AbstractTrigger>();
	private Map<Class<? extends AbstractTrigger>, String> triggersClass = new HashMap<Class<? extends AbstractTrigger>, String>();

	public Denizen plugin;

	public TriggerRegistry(Denizen denizen) {
		plugin = denizen;
	}


	public boolean registerTrigger(String triggerName, AbstractTrigger triggerClass) {
		this.triggers.put(triggerName.toUpperCase(), triggerClass);
		this.triggersClass.put(triggerClass.getClass(), triggerName);
		triggerClass.triggerName = triggerName.substring(0, 1).toUpperCase() + triggerName.substring(1).toLowerCase();
		plugin.getLogger().log(Level.INFO, "Loaded " + triggerClass.triggerName + " Trigger successfully!");
		return true;
	}


	public Map<String, AbstractTrigger> listTriggers() {
		if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "Contents of TriggerList: " + triggers.keySet().toString());
		return triggers;
	}

	
	public AbstractTrigger getTrigger(String triggerName) {
		if (triggers.containsKey(triggerName.toUpperCase()))
			return triggers.get(triggerName);
		else
			return null;
	}

	public <T extends AbstractTrigger> T getTrigger(Class<T> theClass) {
		if (triggersClass.containsKey(theClass))
			return (T) theClass.cast(triggers.get(triggersClass.get(theClass)));
		else
			return null;
	}
	
	
	public void registerCoreTriggers() {

		LocationTrigger locationTrigger = new LocationTrigger();
		ChatTrigger chatTrigger = new ChatTrigger();
		ClickTrigger clickTrigger = new ClickTrigger();
		DamageTrigger damageTrigger = new DamageTrigger();
		ProximityTrigger proximityTrigger = new ProximityTrigger();
		
		/* Activate Denizen Triggers */
		try {
			
			locationTrigger.activateAs("Location");
			chatTrigger.activateAs("Chat");
			clickTrigger.activateAs("Click");
			damageTrigger.activateAs("Damage");
			proximityTrigger.activateAs("Proximity");
		
		} catch (ActivationException e) {
			plugin.getLogger().log(Level.SEVERE, "Oh no! Denizen has run into a problem registering the core triggers!");
			e.printStackTrace();
		}

		/* Register Listener events. */
		plugin.getServer().getPluginManager().registerEvents(locationTrigger, plugin);
		plugin.getServer().getPluginManager().registerEvents(chatTrigger, plugin);
		plugin.getServer().getPluginManager().registerEvents(clickTrigger, plugin);
		plugin.getServer().getPluginManager().registerEvents(damageTrigger, plugin);
		plugin.getServer().getPluginManager().registerEvents(proximityTrigger, plugin);
		
	}


}
