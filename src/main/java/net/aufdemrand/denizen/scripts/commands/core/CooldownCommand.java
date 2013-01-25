package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * <p>Sets a 'cooldown' period on a script. Can be per-player or globally.</p>
 * 
 * <b>dScript Usage:</b><br>
 * <pre>COOLDOWN ({PLAYER}|GLOBAL) (#{60}|DURATION:#) (SCRIPT:script_name)</pre>
 * 
 * <ol><tt>Arguments: [] - Required, () - Optional, {} - Default</ol></tt>
 * 
 * <ol><tt>({PLAYER}|GLOBAL)</tt><br> 
 *         The scope of the cooldown. If not specified, it's assumed that the script
 *         should be cooled down for the PLAYER. If GLOBAL, all players are affected.</ol>
 * 
 * <ol><tt>(#{60})</tt><br> 
 *         The duration of the cooldown period. Worth noting that if not specified, the
 *         default value is 60 seconds.</ol>
 *
 * <ol><tt>(DURATION:#)</tt><br>
 *         Same as using an integer value for the cooldown period, but accepts the dScript
 *         time format for minutes, hours, days etc. For example: '60m' = 60 minutes. '1d' = 1 day.
 *         Worth noting: Durations are in real-time, not minecraft time.</ol>
 *
 * <ol><tt>[({PLAYER}|GLOBAL)]</tt><br> 
 *         The scope of the cooldown. Specifying PLAYER only affects the player, GLOBAL in turn
 *         will affect ALL players.</ol>
 * 
 * <br><b>Example Usage:</b><br>
 * <ol><tt>
 *  - COOLDOWN 100 <br>
 *  - COOLDOWN DURATION:18h GLOBAL <br>
 *  - COOLDOWN 'SCRIPT:A Different Script' 10m
 * </ol></tt>
 * 
 *
 * @author Jeremy Schroeder
 *
 */
public class CooldownCommand extends AbstractCommand {

	private enum CooldownType { GLOBAL, PLAYER }

	@Override
	public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

		// Define necessary fields
		String script = scriptEntry.getScript();
		double duration = -1;
		OfflinePlayer player = null;
		CooldownType type = CooldownType.PLAYER;

		// Set player in scriptEntry to the target
		if (scriptEntry.getPlayer() != null) 
			player = scriptEntry.getPlayer();
		else if (scriptEntry.getOfflinePlayer() != null) 
			player = scriptEntry.getOfflinePlayer();

		// Parse Arguments
		for (String arg : scriptEntry.getArguments()) {

			if (aH.matchesDuration(arg) || aH.matchesInteger(arg))
				// Usually a duration of negative value will return an error, but
				// in this case, allowing a negative integer will be of no consequence.
				duration = aH.getSecondsFrom(arg);

				// Default is PLAYER, but having both to choose from seems to be best
			else if (aH.matchesArg("GLOBAL, PLAYER", arg))
				type = CooldownType.valueOf(arg.toUpperCase());

				// Must be an actual script! If the script doesn't exist, matchesScript(...)
				// will echo an error.
			else if (aH.matchesScript(arg))
				script = aH.getStringFrom(arg);

			else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
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
		int duration = ((Double) scriptEntry.getObject("duration")).intValue();
		CooldownType type = (CooldownType) scriptEntry.getObject("type");



		if (type == CooldownType.PLAYER)
		setCooldown(((OfflinePlayer) scriptEntry.getObject("player")).getName(), duration, script, false);
		// else, a GLOBAL cooldown
		else setCooldown(null, duration, script, true);

	}

    private FileConfiguration saves = null;

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
     *
	 */
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