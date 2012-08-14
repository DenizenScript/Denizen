package net.aufdemrand.denizen.commands.core;

import net.aufdemrand.denizen.commands.AbstractCommand;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.citizensnpcs.command.exception.CommandException;

/**
 * Cooldown Command
 * Sets a 'cooldown' period on a script. Can be per-player or globally.
 * 
 * @author Jeremy Schroeder
 */

public class CooldownCommand extends AbstractCommand {

	/* COOLDOWN [# of Seconds] (GLOBAL) ('SCRIPT:[Name of Script]') */

	/* 
	 * Arguments: [] - Required, () - Optional 
	 * [# of Seconds] 
	 * (GLOBAL) will set a 'global cooldown, affecting all players.
	 *   Be diligent when using this, as it will essentially
	 *   'lock down' all steps -- the entire script!
	 *   By default, only the interacting player will be affected
	 *   by the cooldown.
	 *   
	 * Modifiers:
	 * (DURATION:#) Same as [# of Seconds]
	 * ('SCRIPT:[Name of Script]')
	 * 
	 * Example Usage:
	 * COOLDOWN 60
	 * COOLDOWN GLOBAL 100
	 * COOLDOWN 'SCRIPT:A Different Script' 600
	 * COOLDOWN DURATION:15
	 * 
	 */

	@Override

	public boolean execute(ScriptEntry theEntry) throws CommandException {

		/* Initialize variables */ 
		Boolean isGlobal = false;
		Integer duration = null;
		String theScript = null;

		if (theEntry.arguments() == null)
			throw new CommandException("...Usage: COOLDOWN [# of Seconds] (GLOBAL) ('SCRIPT:[Name of Script]')");

		/* Match arguments to expected variables */
		for (String thisArg : theEntry.arguments()) {

			// If argument is a Duration modifier
			if (aH.matchesDuration(thisArg)) {
				duration = aH.getIntegerModifier(thisArg);
				aH.echoDebug("...cooldown duration now '%s'.", thisArg);
			}

			// If argument matches duration (by indicating just an Integer)
			else if (aH.matchesInteger(thisArg)) {
				duration = aH.getIntegerModifier(thisArg);
				aH.echoDebug("...cooldown duration now '%s'.", thisArg);
			}

			// If argument is a GLOBAL modifier
			else if (thisArg.equalsIgnoreCase("GLOBAL")) {
				isGlobal = true;
				aH.echoDebug("...script COOLDOWN will now be GLOBAL.", thisArg);
			}

			// If argument is a Script modifier
			else if (aH.matchesScript(thisArg)) {
				theScript = aH.getStringModifier(thisArg);
				aH.echoDebug("...command will now affect '%s'.", thisArg);
			}

			// Can't match to anything
			else aH.echoError("...unable to match argument!");
		}	


		/* Execute the command, if all required variables are filled. */
		if (duration != null) {

			if (theScript == null) theScript = theEntry.getScript();

			// If global, set global cool-down
			if (isGlobal) {
				plugin.getSaves().set("Global.Scripts." + theScript + ".Cooldown Time", System.currentTimeMillis() + (duration * 1000));
				plugin.saveSaves();
			}

			// If not global, set cool-down for player:
			else if (!isGlobal) {
				plugin.getSaves().set("Players." + theEntry.getPlayer().getName() + ".Scripts." + theScript + ".Cooldown Time", System.currentTimeMillis() + (duration * 1000));
				plugin.saveSaves();
			}

			return true;
		}

		return false;
	}

}