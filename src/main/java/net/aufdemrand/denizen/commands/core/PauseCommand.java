package net.aufdemrand.denizen.commands.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.ChatColor;

import net.aufdemrand.denizen.commands.AbstractCommand;
import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.runnables.TwoItemRunnable;
import net.aufdemrand.denizen.scripts.ScriptEntry;
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
		boolean waypoints = false;

		/* Match arguments to expected variables */
		if (theEntry.arguments() != null)
			for (String thisArg : theEntry.arguments()) {

				if (aH.matchesDuration(thisArg)) {
					duration = aH.getIntegerModifier(thisArg);
					aH.echoDebug("...duration set to '%s'.", thisArg);
				}

				else if (thisArg.toUpperCase().contains("WAYPOINTS")) {
					waypoints = true;
					aH.echoDebug("...affecting WAYPOINTS.", thisArg);
				}

				else 
					aH.echoError("...could not match '%s'!", thisArg);


			}


		if (theEntry.getCommand().equalsIgnoreCase("RESUME")) {
			if (waypoints) {
				if (theEntry.getDenizen().getCitizensEntity().hasTrait(Waypoints.class))
					theEntry.getDenizen().getCitizensEntity().getTrait(Waypoints.class).getCurrentProvider().setPaused(false);
			} else
				theEntry.getDenizen().getCitizensEntity().getDefaultGoalController().setPaused(false);
			return true;
		}

		// Pause GoalController or waypoints!
		if (waypoints) {
			if (theEntry.getDenizen().getCitizensEntity().hasTrait(Waypoints.class)) {
				theEntry.getDenizen().getCitizensEntity().getTrait(Waypoints.class).getCurrentProvider().setPaused(true);
				theEntry.getDenizen().getNavigator().cancelNavigation();
			}
		} else {
			theEntry.getDenizen().getCitizensEntity().getDefaultGoalController().setPaused(true);
			theEntry.getDenizen().getNavigator().cancelNavigation();
		}

		if (duration != null) 

			if (taskMap.containsKey(theEntry.getDenizen().getName())) {
				try {
					plugin.getServer().getScheduler().cancelTask(taskMap.get(theEntry.getDenizen().getName()));
				} catch (Exception e) { }
			}
		aH.echoDebug("Setting delayed task: UNPAUSE GOAL SELECTOR.");

		taskMap.put(theEntry.getDenizen().getName(), plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new TwoItemRunnable<DenizenNPC, Boolean>(theEntry.getDenizen(), waypoints) {
			@Override
			public void run(DenizenNPC theNPC, Boolean waypoints) { 
				aH.echoDebug(ChatColor.YELLOW + "//DELAYED//" + ChatColor.WHITE + " Running delayed task: UNPAUSE GOAL SELECTOR for '%s'.", theNPC.getName());
				if (waypoints) {
					if (theNPC.getCitizensEntity().hasTrait(Waypoints.class)) {
						theNPC.getCitizensEntity().getTrait(Waypoints.class).getCurrentProvider().setPaused(false);
						theNPC.getNavigator().cancelNavigation();
					}
				} else {
					theNPC.getCitizensEntity().getDefaultGoalController().setPaused(false);
					theNPC.getNavigator().cancelNavigation();
				}
			}
		}, duration * 20));


		return true;
	}


}