package net.aufdemrand.denizen.scripts.commands.core;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;

/**
 * <p>Sets a 'cooldown' period on a script. Can be per-player or globally.</p>
 * 
 * <b>dScript Usage:</b><br>
 * <pre>COOLDOWN ({PLAYER}|GLOBAL) (#{60}) (SCRIPT:script_name)</pre>
 * 
 * <ol><tt>Arguments: [] - Required, () - Optional, {} - Default</ol></tt>
 * 
 * <ol><tt>({PLAYER}|GLOBAL)</tt><br> 
 *         The scope of the cooldown. If not specified, it's assumed that the script
 *         should be cooled down for the PLAYER. If GLOBAL, all players are affected.</ol>
 * 
 * <ol><tt>(#{60})</tt><br> 
 *         The duration of the cooldown period. Worth noting that if not specified, the
 *         default value is 60 seconds. Use </ol>
 * 
 * <ol><tt>[({PLAYER}|GLOBAL)]</tt><br> 
 *         The message to send to the server. This will be seen by all Players.</ol>
 * 
 * <br><b>Example Usage:</b><br>
 * <ol><tt>
 *  - ANNOUNCE 'Today is Christmas!' <br>
 *  - ANNOUNCE "&#60;PLAYER.NAME> has completed '&#60;FLAG.P:currentQuest>'!" <br>
 *  - ANNOUNCE "&#60;GOLD>$$$ &#60;WHITE>- Make some quick cash at our &#60;RED>MINEA-SINO&#60;WHITE>!" 
 * </ol></tt>

 * 
 * <code>Usage:	COOLDOWN # (GLOBAL|PLAYER) (SCRIPT:script_name)
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

	private enum CooldownType { GLOBAL, PLAYER }

	@Override
	public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

		// Define necessary fields
		String script = scriptEntry.getScript();
		int duration = -1;
		OfflinePlayer player = null;
		CooldownType type = CooldownType.PLAYER;

		// Set player in scriptEntry to the target
		if (scriptEntry.getPlayer() != null) 
			player = scriptEntry.getPlayer();
		else if (scriptEntry.getOfflinePlayer() != null) 
			player = scriptEntry.getOfflinePlayer();

		// Parse Arguments
		for (String arg : scriptEntry.getArguments()) {

			if (aH.matchesDuration(arg) || aH.matchesInteger(arg)) {
				// Usually a duration of negative value will return an error, but
				// in this case, allowing a negative integer will be of no consequence.
				duration = aH.getIntegerFrom(arg);
				dB.echoDebug(Messages.DEBUG_SET_DURATION, aH.getStringFrom(arg));

				// Default is PLAYER, but having both to choose from seems to be best
			}	else if (aH.matchesArg("GLOBAL, PLAYER", arg)) {
				type = CooldownType.valueOf(arg.toUpperCase());
				dB.echoDebug(Messages.DEBUG_SET_TYPE, arg);

				// Must be an actual script! If the script doesn't exist, matchesScript(...)
				// will echo an error.
			}	else if (aH.matchesScript(arg)) {
				script = aH.getStringFrom(arg);
				dB.echoDebug(Messages.DEBUG_SET_SCRIPT, script);

			}	else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
		}

		// Check to make sure required arguments have been filled
		if (type == CooldownType.PLAYER && player == null)
			throw new InvalidArgumentsException(Messages.ERROR_NO_PLAYER);
		if (script == null)
			throw new InvalidArgumentsException(Messages.ERROR_NO_SCRIPT);

		// Store necessary items in the scriptEntry
		scriptEntry.addObject("script", script);
		scriptEntry.addObject("duration", duration);
		scriptEntry.addObject("type", type);
		// Store the player only when necessary
		if (type == CooldownType.PLAYER)
			scriptEntry.addObject("player", player);		
	}

	@Override
	public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

		String script = (String) scriptEntry.getObject("script");
		int duration = (Integer) scriptEntry.getObject("duration");
		CooldownType type = (CooldownType) scriptEntry.getObject("type");
		
		if (type == CooldownType.PLAYER)
		setCooldown(((OfflinePlayer) scriptEntry.getObject("player")).getName(), duration, script, false);
		// else, a GLOBAL cooldown
		else setCooldown(null, duration, script, true);

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

	private FileConfiguration saves = null;

	public boolean checkCooldown(String playerName, String scriptName) {
		// I hate case-sensitivity. The positive here outweighs the negative.
		playerName = playerName.toUpperCase();
		scriptName = scriptName.toUpperCase();

		if (saves == null) saves = denizen.getSaves();

		// Check current entry GLOBALLY, reset it if necessary
		if (saves.contains("Global.Scripts." + scriptName + ".Cooldown Time")) {
			if (System.currentTimeMillis() < saves.getLong("Global.Scripts." + scriptName + ".Cooldown Time"))
				return false;
			else {
				saves.set("Global.Scripts." + scriptName + ".Cooldown Time", null);
				denizen.saveSaves();
			}
		}

		// Now check for player-specific cooldowns
		
		// If no entry for the script, return true
		if (!saves.contains("Players." + playerName + ".Scripts." + scriptName + ".Cooldown Time")) 
			return true;

		// If there is an entry, check against the time 
		if (System.currentTimeMillis() >= saves.getLong("Players." + playerName + ".Scripts." + scriptName + ".Cooldown Time"))	{
			saves.set("Players." + playerName + ".Scripts." + scriptName + ".Cooldown Time", null);
			denizen.saveSaves();
			return true;
		}

		return false;
	}

	/**
	 * Sets a cooldown for a Denizen Script. Can be for a specific Player, or GLOBAL.
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
		// I hate case-sensitivity. The positive here outweighs the negative.
		if (playerName != null) playerName = playerName.toUpperCase();
		scriptName = scriptName.toUpperCase();

		if (saves == null) saves = denizen.getSaves();

		// Set global cooldown
		if (global) {
			saves.set("Global.Scripts." + scriptName + ".Cooldown Time", System.currentTimeMillis() + (duration * 1000));
			denizen.saveSaves();

			// or set Player cooldown
		}   else {
			saves.set("Players." + playerName + ".Scripts." + scriptName + ".Cooldown Time", System.currentTimeMillis() + (duration * 1000));
			denizen.saveSaves();
		}
	}

}