package net.aufdemrand.denizen.scripts.commands.core;

import org.bukkit.configuration.file.FileConfiguration;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;

/**
 * Sets a 'cooldown' period on a script. Can be per-player or globally.
 * 
 * @author Jeremy Schroeder
 */

public class CooldownCommand extends AbstractCommand {

	/* COOLDOWN [DURATION:#] (GLOBAL) ('SCRIPT:[Name of Script]')  */

	/* 
	 * Arguments: [] - Required, () - Optional 
	 * [DURATION:#] 
	 * (SCRIPT:Name of Script)
	 * ')
	 * 
	 * Example Usage:
	 * COOLDOWN 60
	 * COOLDOWN GLOBAL 100
	 * COOLDOWN 'SCRIPT:A Different Script' 600
	 * COOLDOWN DURATION:15
	 * 
	 */

	String scriptName;
	int duration;
	String playerName;
	boolean global;

	@Override
	public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

		// Set some defaults based on the scriptEntry
		scriptName = scriptEntry.getScript();
	    duration = 0;
	    playerName = null;
	    global = false;
		if (scriptEntry.getPlayer() != null) playerName = scriptEntry.getPlayer().getName();
		else if (scriptEntry.getOfflinePlayer() != null) playerName = scriptEntry.getOfflinePlayer().getName();
		
		// Parse Arguments
		for (String arg : scriptEntry.getArguments()) {

			if (aH.matchesDuration(arg) || aH.matchesInteger(arg)) {
				duration = aH.getIntegerFrom(arg);
				dB.echoDebug(Messages.DEBUG_SET_DURATION, arg);
				continue;

			}	else if (aH.matchesArg("GLOBAL", arg)) {
				global = true;
				dB.echoDebug(Messages.DEBUG_SET_GLOBAL, arg);
				continue;

			}	else if (aH.matchesScript(arg)) {
				scriptName = aH.getStringFrom(arg);
				dB.echoDebug(Messages.DEBUG_SET_SCRIPT, scriptName);
				continue;

			}	else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
		}
	}

	@Override
	public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

		if (playerName == null && !global) throw new CommandExecutionException(Messages.ERROR_NO_PLAYER);
		
		setCooldown(playerName, duration, scriptName, global);
		
	}
	

	/**
	 * Checks if a script is cooled-down for a Player. If a cool-down is currently in progress,
	 * its requirements will fail and it will not trigger. If the script is being cooled down
	 * globally, this will also return false.
	 * 
	 * @param playerName
	 * 		the Player to check
	 * @param scriptName
	 * 		the name of the script to check
	 * @return
	 * 		if the script is cool for the Player
	 */
	
	private FileConfiguration sA = null;
	
	public boolean checkCooldown(String playerName, String scriptName) {
		
		playerName = playerName.toUpperCase();
		scriptName = scriptName.toUpperCase();
		
		if (sA == null) sA = denizen.getScripts();
		
		// Check current entry, reset it if necessary
		if (sA.contains("Global.Scripts." + scriptName + ".Cooldown Time")) {
			if (System.currentTimeMillis() < sA.getLong("Global.Scripts." + scriptName + ".Cooldown Time"))
				return false;
			else sA.set("Global.Scripts." + scriptName + ".Cooldown Time", null);
		}

		// If no entry for the script, return true
		if (!sA.contains("Players." + playerName + ".Scripts." + scriptName + ".Cooldown Time")) 
			return true;

		// If there is an entry, check against the time 
		if (System.currentTimeMillis() >= sA.getLong("Players." + playerName + ".Scripts." + scriptName + ".Cooldown Time"))	{
			sA.set("Players." + playerName + ".Scripts." + scriptName + ".Cooldown Time", null);
			return true;
		}
		
		return false;
	}
	
	/**
	 * Sets a cooldown for a Denizen Script. Can be for a specific Player, or global.
	 * 
	 * @param playerName
	 * 		if not a global cooldown, the Player to set the cooldown for
	 * @param duration
	 * 		the duration of the cooldown period, in seconds
	 * @param scriptName
	 * 		the name of the script to cooldown
	 * @param global
	 * 		whether the script should be cooled down globally
	 */
	public void setCooldown(String playerName, int duration, String scriptName, boolean global) {
		playerName = playerName.toUpperCase();
		scriptName = scriptName.toUpperCase();
		
		if (global) {
			denizen.getSaves().set("Global.Scripts." + scriptName + ".Cooldown Time", System.currentTimeMillis() + (duration * 1000));
			denizen.saveSaves();

		}   else {
			denizen.getSaves().set("Players." + playerName + ".Scripts." + scriptName + ".Cooldown Time", System.currentTimeMillis() + (duration * 1000));
			denizen.saveSaves();
		}
	}

    @Override
    public void onEnable() {
        // TODO Auto-generated method stub
        
    }
}