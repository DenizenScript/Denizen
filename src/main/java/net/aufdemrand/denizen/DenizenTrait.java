package net.aufdemrand.denizen;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.trait.Toggleable;

public class DenizenTrait extends Trait implements Toggleable {

	private Map<String, Boolean> triggerMap = new HashMap<String, Boolean>();
	
	private Denizen plugin;
	
	public boolean isDenizen = false;
	public boolean enableChatTriggers = true;
	public boolean enableLocationTriggers = true;
	public boolean enableProximityTriggers = true;
	public boolean enableClickTriggers = true;
	public boolean enableDamageTriggers = false;
	public boolean enableDeathTriggers = false;

	public DenizenTrait() {
		super("denizen");
	}

	@Override
	public void load(DataKey key) throws NPCLoadException {
		
		plugin = (Denizen) Bukkit.getServer().getPluginManager().getPlugin("Denizen");
		isDenizen = key.getBoolean("toggle", false);
		
		for (String theTriggerName : plugin.triggerRegistry.listTriggers().keySet())
			if (key.keyExists("enable." + theTriggerName.toLowerCase() + "_triggers"))
				triggerMap.
			
			
		enableClickTriggers = key.getBoolean("enable.click_triggers", true);
		enableDamageTriggers = key.getBoolean("enable.damage_triggers", false);
		enableChatTriggers = key.getBoolean("enable.chat_triggers", true);
		enableProximityTriggers = key.getBoolean("enable.proximity_triggers", true);
		enableLocationTriggers = key.getBoolean("enable.location_triggers", true);
		enableDeathTriggers = key.getBoolean("enable.death_triggers", true);
	}

	@Override
	public void save(DataKey key) {
		key.setBoolean("toggle", isDenizen);
		key.setBoolean("enabled.click_triggers", enableClickTriggers);
		key.setBoolean("enabled.damage_triggers", enableDamageTriggers);
		key.setBoolean("enabled.chat_triggers", enableChatTriggers);
		key.setBoolean("enabled.proximity_triggers", enableProximityTriggers);
		key.setBoolean("enabled.location_triggers", enableLocationTriggers);
		key.setBoolean("enabled.death_triggers", enableDeathTriggers);
	}

	@Override
	public boolean toggle() {
		isDenizen = !isDenizen;
		return isDenizen;
	}


}
