package net.aufdemrand.denizen;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.trait.Toggleable;


public class DenizenTrait extends Trait implements Toggleable {

	/* map of triggers and their enable status */
	private Map<String, Boolean> triggerMap = new HashMap<String, Boolean>();

	private Denizen plugin;

	/* is this NPC a Denizen? False by default. */
	public boolean isDenizen = false;

	public DenizenTrait() {
		super("denizen");
	}
	
	
	@Override
	public void load(DataKey key) throws NPCLoadException {
		plugin = (Denizen) Bukkit.getServer().getPluginManager().getPlugin("Denizen");

		/* Read Citizens saves.yml to populate trigger enable statuses and isDenizen status */
		isDenizen = key.getBoolean("toggle", false);
		for (String theTriggerName : plugin.triggerRegistry.listTriggers().keySet())
			if (key.keyExists("enable." + theTriggerName.toLowerCase() + "-trigger")) {
				triggerMap.put(theTriggerName, key.getBoolean("enable." + theTriggerName.toLowerCase() + "-trigger"));
			} else {
				triggerMap.put(theTriggerName, plugin.triggerRegistry.getTrigger(theTriggerName).enabledByDefault);
			}
	}

	
	@Override
	public void save(DataKey key) {

		/* Save trigger enable statuses and isDenizen status to Citizens saves.yml */
		for (Entry<String, Boolean> theEntry : triggerMap.entrySet()) {
			key.setBoolean("enable." + theEntry.getKey() + "-trigger", theEntry.getValue());
		}
	
	}

	
	/* Toggle Denizen */
	@Override
	public boolean toggle() {
		isDenizen = !isDenizen;
		return isDenizen;
	}

	
	/* Get Trigger enable status */
	public boolean triggerIsEnabled(String theName) {
		if (triggerMap.containsKey(theName))
			return triggerMap.get(theName);
		else return false;
	}

	
}
