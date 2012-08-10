package net.aufdemrand.denizen.npc;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import net.aufdemrand.denizen.Denizen;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.trait.Toggleable;


public class DenizenTrait extends Trait implements Toggleable {

	private Map<String, Boolean> triggerMap = new HashMap<String, Boolean>();
	private Denizen plugin;

	private boolean isToggled = true;

	public DenizenTrait() {
		super("denizen");
	}

	@Override
	public void onSpawn() {
		plugin = (Denizen) Bukkit.getServer().getPluginManager().getPlugin("Denizen");
		plugin.getDenizenNPCRegistry().registerNPC(npc);

		for (String theTriggerName : plugin.getTriggerRegistry().listTriggers().keySet())
			if (!triggerMap.containsKey(theTriggerName))
				triggerMap.put(theTriggerName, plugin.getTriggerRegistry().getTrigger(theTriggerName).getEnabledByDefault());
	}

	@Override
	public void load(DataKey key) throws NPCLoadException {
		plugin = (Denizen) Bukkit.getServer().getPluginManager().getPlugin("Denizen");

		plugin.getDenizenNPCRegistry().registerNPC(npc);

		isToggled = key.getBoolean("toggled", true);
		for (String theTriggerName : plugin.getTriggerRegistry().listTriggers().keySet())
			if (key.keyExists("enable." + theTriggerName.toLowerCase() + "-trigger")) {
				triggerMap.put(theTriggerName, key.getBoolean("enable." + theTriggerName.toLowerCase() + "-trigger"));
			} else {
				triggerMap.put(theTriggerName, plugin.getTriggerRegistry().getTrigger(theTriggerName).getEnabledByDefault());
			}
	}


	@Override
	public void save(DataKey key) {

		key.setBoolean("toggled", isToggled);

		for (Entry<String, Boolean> theEntry : triggerMap.entrySet()) {
			key.setBoolean("enable." + theEntry.getKey().toLowerCase() + "-trigger", theEntry.getValue());
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
		if (triggerMap.containsKey(theName.toUpperCase()))
			return triggerMap.get(theName.toUpperCase());
		else return false;
	}

	public String listTriggers() {
		String theList = ChatColor.GRAY + "Current trigger status: ";
		for (Entry<String, Boolean> theEntry : triggerMap.entrySet()) {
			theList = theList + theEntry.getKey().toLowerCase() + "-trigger: ";
			if (theEntry.getValue())
				theList = theList + ChatColor.GREEN + "ENABLED" + ChatColor.GRAY + ", ";
			else
				theList = theList + ChatColor.RED + "DISABLED" + ChatColor.GRAY + ", ";
		}
		theList = theList.substring(0, theList.length() - 2);
		return theList;
	}

	public String toggleTrigger(String theTrigger) {
		if (triggerMap.containsKey(theTrigger.toUpperCase())) {
			if (triggerMap.get(theTrigger.toUpperCase())) {
				triggerMap.put(theTrigger.toUpperCase(), false);
				return theTrigger + "-trigger now disabled.";
			} else {
				triggerMap.put(theTrigger.toUpperCase(), true);
				return theTrigger + "-trigger now enabled.";
			}
		} else {
			return "Trigger not found!";
		}
	}

}
