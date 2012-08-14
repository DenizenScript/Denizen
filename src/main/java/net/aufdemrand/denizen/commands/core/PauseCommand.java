package net.aufdemrand.denizen.commands.core;

import org.bukkit.Location;

import net.aufdemrand.denizen.commands.AbstractCommand;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.command.exception.CommandException;
import net.citizensnpcs.trait.waypoint.Waypoints;

/**
 * Your command! 
 * This class is a template for a Command in Denizen.
 * 
 * @author You!
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

	@Override
	public boolean execute(ScriptEntry theEntry) throws CommandException {

		/* Initialize variables */ 

		Integer duration = null;

		/* Match arguments to expected variables */
		if (theEntry.arguments() != null) {
			for (String thisArg : theEntry.arguments()) {

				if (aH.matchesDuration(thisArg)) {
					duration = getIntegerModifier(thisArg);
					echoDebug("...duration set to '%s'.", thisArg);
				}

				else {
					echoDebug("...'%s' could not be matched!", thisArg);
				}

			}	
		}


		if (theEntry.getCommand().equalsIgnoreCase("RESUME")) {
			theEntry.getDenizen().getCitizensEntity().getTrait(Waypoints.class).getCurrentProvider().setPaused(false);
			return true;
		}

		// Pause waypoints!
		theEntry.getDenizen().getCitizensEntity().getTrait(Waypoints.class).getCurrentProvider().setPaused(true);

		if (duration != null) 
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new PauseCommandRunnable<NPC>(theEntry.getDenizen().getCitizensEntity()) {
				@Override
				public void run(NPC theNPC) { 
					theNPC.getTrait(Waypoints.class).getCurrentProvider().setPaused(false);
				}
			}, duration * 20);


		return true;
	}


}