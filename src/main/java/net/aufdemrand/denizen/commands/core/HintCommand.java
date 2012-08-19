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
	 * (SHORT)
	 *   
	 * Example Usage:
	 * HINT
	 * HINT SHORT
	 */

	@Override
	public boolean execute(ScriptEntry theEntry) throws CommandException {

		boolean shortformat =false;


		if (theEntry.arguments() != null) {
			for (String thisArgument : theEntry.arguments()) {
				if (thisArgument.toLowerCase().contains("short")) shortformat = true;
			}
		}

		String theScript = theEntry.getScript();
		Integer theStep = theEntry.getStep();

		// Get all chat triggers.

		List<String> chatTriggers = new ArrayList<String>();
		String scriptPath = theScript + ".Steps." + theStep + ".Chat Trigger."; 

		boolean thisTriggerExists = true;
		int x = 1;

		do {

			if (plugin.getScripts().contains(scriptPath + x + ".Trigger")) {
				chatTriggers.add(plugin.getScripts().getString(scriptPath + x + ".Trigger"));
				if (plugin.debugMode) plugin.getLogger().info("found " + chatTriggers.get(x));
			}
			else thisTriggerExists = false;

			x++;
		} while (thisTriggerExists);


		if (chatTriggers.isEmpty()) return false;

		// Format the chatTriggers

		StringBuilder sb = new StringBuilder();

		sb.append(plugin.settings.NpcHintPrefix());

		for(int i=0;i<chatTriggers.size();i++) {
			String item = chatTriggers.get(i);
			if(item.contains("/*/")) continue;
			String fitem = getFormattedTrigger(item, shortformat);
			if (plugin.debugMode)	plugin.getLogger().info("formatted "  + fitem);
			sb.append(fitem);	
			if (i != chatTriggers.size() -1) sb.append(", ");
		}

		theEntry.getPlayer().sendMessage(sb.toString());
		return true;
	}


	private String getFormattedTrigger(String str, boolean shortformat){
		if (str ==null) return "";

		if (shortformat) {
			try {
				int start = str.indexOf("/");
				int end = str.indexOf("/",start+1);
				if(start !=-1 && end !=-1 && start != end){
					str = str.substring(start,  end+1);
				}	
			} catch (Exception e) {
				// all kinds of possible index exceptions.
			}
		}

		return str.replace("/", ChatColor.UNDERLINE + "").replace("/", ChatColor.RESET + ""); 

	}

}