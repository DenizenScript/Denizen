package net.aufdemrand.denizen.npc;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.aufdemrand.denizen.Denizen;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.trait.Toggleable;


public class DenizenTrait extends Trait implements Toggleable {

	private Map<String, Boolean> triggerMap = new HashMap<String, Boolean>();
	private Denizen plugin;

	private boolean isToggled = false;

	public DenizenTrait() {
		super("denizen");
	}
	
	
	@Override
	public void load(DataKey key) throws NPCLoadException {
		plugin = (Denizen) Bukkit.getServer().getPluginManager().getPlugin("Denizen");

		plugin.getDenizenNPCRegistry().registerNPC(npc);
		
		isToggled = key.getBoolean("toggle", false);
		for (String theTriggerName : plugin.getTriggerRegistry().listTriggers().keySet())
			if (key.keyExists("enable." + theTriggerName.toLowerCase() + "-trigger")) {
				triggerMap.put(theTriggerName, key.getBoolean("enable." + theTriggerName.toLowerCase() + "-trigger"));
			} else {
				triggerMap.put(theTriggerName, plugin.getTriggerRegistry().getTrigger(theTriggerName).getEnabledByDefault());
			}
	}

	
	@Override
	public void save(DataKey key) {
		for (Entry<String, Boolean> theEntry : triggerMap.entrySet()) {
			key.setBoolean("enable." + theEntry.getKey() + "-trigger", theEntry.getValue());
		}
	}

	
	@Override
	public boolean toggle() {
		isToggled = !isToggled;
		return isToggled;
	}
	
	
	public boolean isToggled() {
		return isToggled;
	}

	
	public boolean triggerIsEnabled(String theName) {
		if (triggerMap.containsKey(theName))
			return triggerMap.get(theName);
		else return false;
	}
	
	
}
