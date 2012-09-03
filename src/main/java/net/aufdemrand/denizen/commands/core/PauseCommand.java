package net.aufdemrand.denizen.commands.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.ChatColor;

import net.aufdemrand.denizen.commands.AbstractCommand;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.command.exception.CommandException;
import net.citizensnpcs.trait.waypoint.Waypoints;

/**
 * Pauses a Denizen's waypoints.
 * 
 * @author Jeremy Schroeder
 */

public class PauseCommand extends AbstractCommand {

	/* PAUSE (DURATION:#) (NPCID:#) */

	/* 
	 * Arguments: [] - Required, () - Optional 
	 * (DURATION:#)
	 * (ARGUMENTS) should be clear and concise.
	 *   
	 * Modifiers:
	 * (MODIFIER:VALUE) These are typically advanced usage arguments.
	 * (DURATION:#) They should always be optional. Use standard modifiers
	 *   already established if at all possible.
	 *   
	 * Example Usage:
	 * COMMAND_NAME VALUE
	 * COMMAND_NAME DIFFERENTVALUE OPTIONALVALUE
	 * COMMAND_NAME ANOTHERVALUE 'MODIFIER:Show one-line examples.'
	 * 
	 */
	
	private Map<String, Integer> taskMap = new ConcurrentHashMap<String, Integer>();

	@Override
	public boolean execute(ScriptEntry theEntry) throws CommandException {

		/* Initialize variables */ 

		Integer duration = null;

		/* Match arguments to expected variables */
		if (theEntry.arguments() != null)
			for (String thisArg : theEntry.arguments()) {

				if (aH.matchesDuration(thisArg)) {
					duration = aH.getIntegerModifier(thisArg);
					aH.echoDebug("...duration set to '%s'.", thisArg);
				}

				else 
					aH.echoError("...could not match '%s'!", thisArg);
			}


		if (theEntry.getCommand().equalsIgnoreCase("RESUME")) {
			theEntry.getDenizen().getCitizensEntity().getDefaultGoalController().setPaused(false);
			return true;
		}

		// Pause GoalController!
		theEntry.getDenizen().getCitizensEntity().getDefaultGoalController().setPaused(true);

		
		
		if (duration != null) 

			if (taskMap.containsKey(theEntry.getDenizen().getName())) {
				try {
					plugin.getServer().getScheduler().cancelTask(taskMap.get(theEntry.getDenizen().getName()));
				} catch (Exception e) { }
			}
			aH.echoDebug("Setting delayed task: UNPAUSE GOAL SELECTOR.");

			taskMap.put(theEntry.getDenizen().getName(), plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new PauseCommandRunnable<NPC>(theEntry.getDenizen().getCitizensEntity()) {
				@Override
				public void run(NPC theNPC) { 
					aH.echoDebug(ChatColor.YELLOW + "//DELAYED//" + ChatColor.WHITE + " Running delayed task: UNPAUSE GOAL SELECTOR.");
					theNPC.getDefaultGoalController().setPaused(false);
				}
			}, duration * 20));


		return true;
	}


}