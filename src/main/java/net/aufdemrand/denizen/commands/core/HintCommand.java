package net.aufdemrand.denizen.commands.core;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;

import net.aufdemrand.denizen.commands.AbstractCommand;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.triggers.core.ChatTrigger;
import net.citizensnpcs.command.exception.CommandException;

/**
 * Hints to the Player the available Chat Triggers available. 
 * 
 * @author Jeremy Schroeder
 */

public class HintCommand extends AbstractCommand {

	/* HINT */

	/* 
	 * Arguments: [] - Required, () - Optional 
	 * None.
	 *   
	 * Example Usage:
	 * HINT
	 * 
	 */

	@Override
	public boolean execute(ScriptEntry theEntry) throws CommandException {

		String theScript = theEntry.getScript();
		Integer theStep = theEntry.getStep();

		// Get all chat triggers.

		List<String> chatTriggers = new ArrayList<String>();
		String scriptPath = theScript + ".Steps." + theStep + ".Chat Trigger."; 


		boolean thisTriggerExists = true;
		int x = 1;

		do {
			if (plugin.getScripts().contains(scriptPath + x + ".Trigger")) {
				if (!plugin.getScripts().getString(scriptPath + x + ".Trigger").contains("*")) {
					chatTriggers.add(plugin.getScripts().getString(scriptPath + x + ".Trigger"));
					plugin.getLogger().info("added" + chatTriggers.get(x));
				}
			}
			else thisTriggerExists = false;
			
			x++;
		} while (thisTriggerExists);


		if (chatTriggers.isEmpty()) return false;

		// Format the chatTriggers
		int numberOfTriggers = chatTriggers.size();

		String template = "[HINT] You can say |%s|, %s|or %s.";
		String result = "";

		String firstItem = chatTriggers.get(0);
		plugin.getLogger().info("firstitem " + firstItem);
		String lastItem = chatTriggers.get(numberOfTriggers = 1);
		plugin.getLogger().info("lastItem " + lastItem);
		
		String [] otherItems = null;

		if (numberOfTriggers > 2) {
			chatTriggers.remove(numberOfTriggers - 1);
			chatTriggers.remove(0);
			otherItems = (String[]) chatTriggers.toArray();
		}

		// Build String
		result = template.split("|")[0];
		result = result + template.split("|")[1].replace("%s", firstItem.replace(" /", ChatColor.UNDERLINE + "").replace("/ ", ChatColor.RESET + ""));

		if (otherItems != null) {
			for (String otherItem : otherItems) {
				result = result + template.split("|")[2].replace("%s", otherItem.replace(" /", ChatColor.UNDERLINE + "").replace("/ ", ChatColor.RESET + ""));
				plugin.getLogger().info("building... " + result);
			}
		}
		
		result = result + template.split("|")[3].replace("%s", lastItem.replace(" /", ChatColor.UNDERLINE + "").replace("/ ", ChatColor.RESET + ""));

		theEntry.getPlayer().sendMessage(result);
		return true;
	}


}