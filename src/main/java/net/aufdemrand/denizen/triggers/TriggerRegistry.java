package net.aufdemrand.denizen.triggers;

import java.rmi.activation.ActivationException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.triggers.core.ChatTrigger;
import net.aufdemrand.denizen.triggers.core.ClickTrigger;
import net.aufdemrand.denizen.triggers.core.DamageTrigger;
import net.aufdemrand.denizen.triggers.core.LocationTrigger;
import net.aufdemrand.denizen.triggers.core.PlayerdeathTrigger;
import net.aufdemrand.denizen.triggers.core.ProximityTrigger;

public class TriggerRegistry {

	private Map<String, AbstractTrigger> triggers = new HashMap<String, AbstractTrigger>();
	private Map<Class<? extends AbstractTrigger>, String> triggersClass = new HashMap<Class<? extends AbstractTrigger>, String>();

	public Denizen plugin;

	public TriggerRegistry(Denizen denizen) {
		plugin = denizen;
	}


	public boolean registerTrigger(String triggerName, AbstractTrigger triggerClass) {
		this.triggers.put(triggerName.toUpperCase(), triggerClass);
		this.triggersClass.put(triggerClass.getClass(), triggerName.toUpperCase());
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
			return triggers.get(triggerName.toUpperCase());
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
		PlayerdeathTrigger playerdeathTrigger = new PlayerdeathTrigger();
		
		/* Activate Denizen Triggers */
		try {
			
			locationTrigger.activateAs("Location");
			locationTrigger.setEnabledByDefault(false);
			playerdeathTrigger.activateAs("Playerdeath");
			playerdeathTrigger.setEnabledByDefault(false);
			chatTrigger.activateAs("Chat");
			chatTrigger.setEnabledByDefault(true);
			clickTrigger.activateAs("Click");
			clickTrigger.setEnabledByDefault(true);
			damageTrigger.activateAs("Damage");
			proximityTrigger.activateAs("Proximity");
			proximityTrigger.setEnabledByDefault(false);
		
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
		plugin.getServer().getPluginManager().registerEvents(playerdeathTrigger, plugin);
		
	}


}
