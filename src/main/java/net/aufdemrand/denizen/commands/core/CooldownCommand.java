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
			
		/* Match arguments to expected variables */
		if (theEntry.arguments() != null) {
			for (String thisArg : theEntry.arguments()) {
				
				// If argument is a Duration modifier
				if (aRegex.matchesDuration(thisArg)) {
					duration = getIntegerModifier(thisArg);
					echoDebug("...cooldown duration now '%s'.", thisArg);
				}
				
				// If argument matches duration (by indicating just an Integer)
				else if (aRegex.matchesInteger(thisArg)) {
					duration = getIntegerModifier(thisArg);
					echoDebug("...cooldown duration now '%s'.", thisArg);
				}
				
				// If argument is a GLOBAL modifier
				else if (thisArg.equalsIgnoreCase("GLOBAL")) {
					isGlobal = true;
					echoDebug("...script COOLDOWN will now be GLOBAL.", thisArg);
				}
				
				// If argument is a Script modifier
				else if (aRegex.matchesScript(thisArg)) {
					theScript = getModifier(thisArg);
					echoDebug("...command will now affect '%s'.", thisArg);
				}
				
				// Couldn't find a match!
				else {
					echoDebug("...could not match '%s'!", thisArg);
				}
			}	
		}

		/* Execute the command, if all required variables are filled. */
		if (duration != null) {

			if (theScript == null) theScript = theEntry.getScript();

			// If global, cooldown:
			if (isGlobal) {
				plugin.getSaves().set("Global.Scripts." + theScript + ".Cooldown Time", System.currentTimeMillis() + (duration * 1000));
				plugin.saveSaves();
			}
			// If not global, cooldown for player:
			else if (!isGlobal) {
				plugin.getSaves().set("Players." + theEntry.getPlayer().getName() + ".Scripts." + theScript + ".Cooldown Time", System.currentTimeMillis() + (duration * 1000));
				plugin.saveSaves();
			}
			return true;
		}
			
		
		echoError("...not enough arguments! Usage: SAMPLECOMMAND [TYPICAL] (ARGUMENTS)");
			
		return false;
	}
	
}