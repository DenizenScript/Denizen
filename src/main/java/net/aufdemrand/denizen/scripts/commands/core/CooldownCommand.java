package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.dScript;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.objects.Duration;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import org.bukkit.OfflinePlayer;

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

    private enum Type {GLOBAL, PLAYER}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Define necessary fields
        dScript script = scriptEntry.getScript();
        Duration duration = null;
        OfflinePlayer player = null;
        Type type = Type.PLAYER;

        // Set player in scriptEntry to the target
        if (scriptEntry.getPlayer() != null)
            player = scriptEntry.getPlayer();
        else if (scriptEntry.getOfflinePlayer() != null)
            player = scriptEntry.getOfflinePlayer();

        // Parse Arguments
        for (String arg : scriptEntry.getArguments()) {

            if (aH.matchesDuration(arg) || aH.matchesInteger(arg))
                duration = aH.getDurationFrom(arg);

                // Default is PLAYER, but having both to choose from seems to be most straightforward
            else if (aH.matchesArg("GLOBAL, PLAYER", arg))
                type = Type.valueOf(arg.toUpperCase());

                // Must be an actual script! If the script doesn't exist, matchesScript(...)
                // will echo an error.
            else if (aH.matchesScript(arg))
                script = aH.getScriptFrom(arg);

            else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
        }

        // Check to make sure required arguments have been filled
        if (type == Type.PLAYER && player == null)
            throw new InvalidArgumentsException(Messages.ERROR_NO_PLAYER);
        if (script == null)
            throw new InvalidArgumentsException(Messages.ERROR_NO_SCRIPT);

        // Store necessary items in the scriptEntry
        scriptEntry.addObject("script", script);
        scriptEntry.addObject("duration", duration);
        scriptEntry.addObject("type", type);
        // Store the player only when necessary
        if (type == Type.PLAYER)
            scriptEntry.addObject("player", player);
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        // Fetch objects
        dScript script = (dScript) scriptEntry.getObject("script");
        Duration duration = (Duration) scriptEntry.getObject("duration");
        Type type = (Type) scriptEntry.getObject("type");

        // Report to dB
        dB.report(getName(),
                aH.debugObj("Type", type.toString())
                        + script.debug()
                        + (type == Type.PLAYER ? aH.debugObj("Player", scriptEntry.getPlayer().getName()) : "")
                        + duration.debug());

        // Perform cooldown
        if (type == Type.PLAYER)
            setCooldown(((OfflinePlayer) scriptEntry.getObject("player")).getName(),
                    duration.getSecondsAsInt(),
                    script.getName(),
                    false);

        else if (type == Type.GLOBAL)
            setCooldown(null,
                    duration.getSecondsAsInt(),
                    script.getName(),
                    true);

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
     *
     */
    public static boolean checkCooldown(String playerName, String scriptName) {
        // I hate case-sensitivity. The positive here outweighs the negative.
        playerName = playerName.toUpperCase();
        scriptName = scriptName.toUpperCase();

        // Check current entry GLOBALLY, reset it if necessary
        if (DenizenAPI._saves().contains("Global.Scripts." + scriptName + ".Cooldown Time")) {
            if (System.currentTimeMillis() < DenizenAPI._saves().getLong("Global.Scripts." + scriptName + ".Cooldown Time"))
                return false;
            else {
                DenizenAPI._saves().set("Global.Scripts." + scriptName + ".Cooldown Time", null);
            }
        }

        // Now check for player-specific cooldowns

        // If no entry for the script, return true
        if (!DenizenAPI._saves().contains("Players." + playerName + ".Scripts." + scriptName + ".Cooldown Time"))
            return true;

        // If there is an entry, check against the time
        if (System.currentTimeMillis() >= DenizenAPI._saves().getLong("Players." + playerName + ".Scripts." + scriptName + ".Cooldown Time"))	{
            DenizenAPI._saves().set("Players." + playerName + ".Scripts." + scriptName + ".Cooldown Time", null);
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
    public static void setCooldown(String playerName, int duration, String scriptName, boolean global) {
        // I hate case-sensitivity. The positive here outweighs the negative.
        if (playerName != null) playerName = playerName.toUpperCase();
        scriptName = scriptName.toUpperCase();

        // Set global cooldown
        if (global) {
            DenizenAPI._saves().set("Global.Scripts." + scriptName + ".Cooldown Time", System.currentTimeMillis() + (duration * 1000));

            // or set Player cooldown
        }   else {
            DenizenAPI._saves().set("Players." + playerName + ".Scripts." + scriptName + ".Cooldown Time", System.currentTimeMillis() + (duration * 1000));
        }
    }

}