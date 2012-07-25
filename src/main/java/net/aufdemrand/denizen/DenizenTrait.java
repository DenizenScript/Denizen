package net.aufdemrand.denizen;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.aufdemrand.denizen.SpeechEngine.Reason;
import net.aufdemrand.denizen.SpeechEngine.TalkType;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.trait.Toggleable;


public class DenizenTrait extends Trait implements Toggleable {

	private Map<String, Boolean> triggerMap = new HashMap<String, Boolean>();
	private Denizen plugin;

	private boolean isDenizen = false;

	public DenizenTrait() {
		super("denizen");
	}
	
	
	@Override
	public void load(DataKey key) throws NPCLoadException {
		plugin = (Denizen) Bukkit.getServer().getPluginManager().getPlugin("Denizen");

		isDenizen = key.getBoolean("toggle", false);
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
		isDenizen = !isDenizen;
		return isDenizen;
	}
	
	
	public boolean isDenizen() {
		return isDenizen;
	}

	
	public boolean triggerIsEnabled(String theName) {
		if (triggerMap.containsKey(theName))
			return triggerMap.get(theName);
		else return false;
	}


	public void talk(TalkType talkType, Player thePlayer, String theText) {
		((Denizen) Bukkit.getServer().getPluginManager().getPlugin("Denizen"))
		.getSpeechEngine().talk(npc, thePlayer, theText, talkType);
	}


	public void talk(TalkType talkType, Player thePlayer, Reason theReason) {
		// TODO: Finish before 0.7 release.
	}

	
}
