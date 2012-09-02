package net.aufdemrand.denizen.commands.core;

import java.util.List;
import java.util.Random;

import org.bukkit.Location;

import net.aufdemrand.denizen.commands.AbstractCommand;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.citizensnpcs.command.exception.CommandException;

/**
 * Randomly selects a random script entry from the proceeding
 * entries, discards the rest.
 * 
 * @author Jeremy Schroeder
 */

public class RandomCommand extends AbstractCommand {

	/* RANDOM [#] */

	/* 
	 * Arguments: [] - Required, () - Optional 
	 * [#] of entries to randomly select from. Will select 1 of # to execute
	 *   and discard the rest.
	 *   
	 * Example Usage:
	 * RANDOM 3
	 * CHAT Random Message 1
	 * CHAT Random Message 2
	 * CHAT Random Message 3
	 * 
	 */

	@Override
	public boolean execute(ScriptEntry theEntry) throws CommandException {

		/* Initialize variables */ 
		Integer numberOfEntries = null;

		if (theEntry.arguments() == null) {
			aH.echoError("No arguments! Usage: RANDOM [#]");
			return false;
		}

		/* Match arguments to expected variables */
		for (String thisArg : theEntry.arguments()) {
			// If argument is an Integer
			if (aH.matchesInteger(thisArg)) {
				numberOfEntries = aH.getIntegerModifier(thisArg);
				aH.echoDebug("...will randomly select from the next %s entries.", thisArg);
			}
			// Can't match to anything
			else aH.echoError("...unable to match argument '%'!", thisArg);
		}	

		/* Execute the command, if all required variables are filled. */
		if (numberOfEntries == null) {
			aH.echoError("Required integer argument [#] not found. Check syntax.");
			return false;
		}

		List<ScriptEntry> currentQueue = plugin.getScriptEngine().getPlayerQueue(theEntry.getPlayer(), theEntry.sendingQueue());

		if (currentQueue.size() < numberOfEntries) {
			aH.echoError("Invalid Size! RANDOM [#] must not be larger than the script!");
			return false;
		}

		Random random = new Random(numberOfEntries);
		int selected = random.nextInt();
		ScriptEntry sEtoKeep = null;

		aH.echoDebug("...random number generator selected '%s'", String.valueOf(selected));
		
		for (int x = 0; x < numberOfEntries; x++) {
			if (x != random.nextInt()) {
				aH.echoDebug("...removing '%s'", currentQueue.get(0).getCommand());
				currentQueue.remove(0);
			} else {
				aH.echoDebug("...selected '%s'", currentQueue.get(0).getCommand() + ": " + currentQueue.get(0).arguments());
				sEtoKeep = currentQueue.get(0);
				currentQueue.remove(0);
			}
		}
		
		currentQueue.add(0, sEtoKeep);
		plugin.getScriptEngine().replacePlayerQue(theEntry.getPlayer(), currentQueue, theEntry.sendingQueue());
		return true;
	}


}