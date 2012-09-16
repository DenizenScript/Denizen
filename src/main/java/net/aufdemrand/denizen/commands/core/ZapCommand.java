package net.aufdemrand.denizen.commands.core;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.ChatColor;

import net.aufdemrand.denizen.commands.AbstractCommand;
import net.aufdemrand.denizen.runnables.FourItemRunnable;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.citizensnpcs.command.exception.CommandException;

/**
 * Sets the current step for Players in a specific script by storing
 * the information in the Denizen 'saves.yml'.
 * 
 * @author Jeremy Schroeder
 */

public class ZapCommand extends AbstractCommand {

	/* ZAP (Step #)

	/* Arguments: [] - Required, () - Optional 
	 * (Step #) The step to make the current step. If not specified, assumes current step + 1. 
	 * 
	 * Modifiers: 
	 * ('SCRIPT:[Script Name]') Changes the script from the triggering script to the one specified.
	 * (DURATION:#) Reverts the ZAP after # amount of seconds.
	 */
	
	private Map<String, Integer> taskMap = new ConcurrentHashMap<String, Integer>();

	@Override
	public boolean execute(ScriptEntry theEntry) throws CommandException {

		String theScript = theEntry.getScript();
		Integer theStep = null;
		Integer duration = null;

		/* Get arguments */
		if (theEntry.arguments() != null) {
			for (String thisArg : theEntry.arguments()) {

				// Fill replaceables
				if (thisArg.contains("<")) thisArg = aH.fillReplaceables(theEntry.getPlayer(), theEntry.getDenizen(), thisArg, false);
				
				/* Set the step to ZAP to */
				if (aH.matchesInteger(thisArg)) {
					theStep = aH.getIntegerModifier(thisArg);
					aH.echoDebug("...zapping to step '" + theStep + "'.");
				}

				/* Change the script to a specified one */
				else if (aH.matchesScript(thisArg)) {
					theScript = aH.getStringModifier(thisArg);
					aH.echoDebug("...zapping script '" + theScript + "'.");	
				}

				/* Pick a random step */
				else if (thisArg.toUpperCase().contains("RANDOM:")) {
					int high = 1, low = 1;
					if (thisArg.split(":")[1].split(" ").length == 1
							&& thisArg.split("-").length == 1) {
						if (aH.matchesInteger(thisArg.split(":")[1])) {
							low = 1;
							high = Integer.valueOf(thisArg.split(":")[1]); 
						} 
					}
					else if (thisArg.split(":")[1].split(" ").length == 2) {
						if (aH.matchesInteger(thisArg.split(":")[1].split(" ")[0])
								&& aH.matchesInteger(thisArg.split(":")[1].split(" ")[1])) {
							low = Integer.valueOf(thisArg.split(":")[1].split(" ")[0]);
							high = Integer.valueOf(thisArg.split(":")[1].split(" ")[1]);
						}
					}
					else if (thisArg.split(":")[1].split("-").length == 2) {
						if (aH.matchesInteger(thisArg.split(":")[1].split("-")[0])
								&& aH.matchesInteger(thisArg.split(":")[1].split("-")[1])) {
							low = Integer.valueOf(thisArg.split(":")[1].split("-")[0]);
							high = Integer.valueOf(thisArg.split(":")[1].split("-")[1]);
						}
					}
					Random randomInt = new Random();
					if (high - low > 0) theStep = randomInt.nextInt(high - low + 1) + low;
					else theStep = high;
					aH.echoDebug("...randomly selected step '" + theStep + "'.");
					
				}

				/* Set a duration */
				else if (aH.matchesDuration(thisArg)) {
					duration = aH.getIntegerModifier(thisArg);
					aH.echoDebug("...duration set to '%s'.", thisArg);
				}

				else aH.echoError("Could not match argument '%s'!", thisArg);
			}
		}

		if (theStep == null) theStep = plugin.getScriptEngine().helper.getCurrentStep(theEntry.getPlayer(), theScript) + 1;

		/* Make delayed task to reset step if duration is set */
		if (duration != null) {

			Integer oldStep = plugin.getScriptEngine().helper.getCurrentStep(theEntry.getPlayer(), theScript);

			if (taskMap.containsKey(theEntry.getPlayer().getName())) {
				try {
					plugin.getServer().getScheduler().cancelTask(taskMap.get(theEntry.getPlayer().getName()));
				} catch (Exception e) { }
			}
			aH.echoDebug("Setting delayed task: RESET ZAP");

			taskMap.put(theEntry.getPlayer().getName(), plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, 
					new FourItemRunnable<String, String, Integer, Integer>(theEntry.getPlayer().getName(), theScript, theStep, oldStep) {

				@Override
				public void run(String player, String script, Integer step, Integer oldStep) { 
					aH.echoDebug(ChatColor.YELLOW + "//DELAYED//" + ChatColor.WHITE + " Running delayed task: RESET ZAP for " + player + ".");

					// Reset step after duration if step remains the same.
					if (plugin.getSaves().getInt("Players." + player + "." + script + ".Current Step") == step) {
						plugin.getSaves().set("Players." + player + "." + script + ".Current Step", oldStep);
					}

				}
			}, duration * 20));
		}

		/* Warn console if step doesn't actually exist. */
		if (!plugin.getScripts().contains(theScript + ".Steps." + theStep))
			aH.echoDebug("...this command is ZAPPING to a step that does not exist! Is this indended?");


		/* Set saves.yml */
		if (theEntry.getPlayer() != null && theScript != null && theStep != null) {
			plugin.getSaves().set("Players." + theEntry.getPlayer().getName() + "." + theScript + ".Current Step", theStep); 
			plugin.saveSaves();
			return true;
		}

		return false;
	}

}