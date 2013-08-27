package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;

/**
 * <p>Sets a 'cooldown' period on a script. Can be per-player or globally.</p>
 *
 * @author Jeremy Schroeder
 *
 */
public class CooldownCommand extends AbstractCommand {

    private enum Type { GLOBAL, PLAYER }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Defaults are Type.PLAYER and the attached Script
        scriptEntry.addObject("type", Type.PLAYER);
        scriptEntry.addObject("script", scriptEntry.getScript());

        // Parse arguments.. we need a type, duration, and script.

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            // Type may be PLAYER or GLOBAL.. must not have a prefix.
            if (!arg.hasPrefix() && arg.matchesEnum(Type.values())) {
                scriptEntry.addObject("type", Type.valueOf(arg.getValue().toUpperCase()));
            }

            // Duration does not need a prefix, but is required.
            else if (!scriptEntry.hasObject("duration")
                    && arg.matchesArgumentType(Duration.class)) {
                scriptEntry.addObject("duration", arg.asType(Duration.class));
            }

            // Require a prefix on the script, since it's optional.
            else if (arg.matchesPrefix("script, s")) {
                // Check matchesArgumentType afterwards so we don't default
                // to the attached script unintentionally.
                if (arg.matchesArgumentType(dScript.class))
                    scriptEntry.addObject("script", arg.asType(dScript.class));
                else
                    throw new InvalidArgumentsException("Specified an invalid script!");
            }

            else dB.echoDebug("Unhandled argument: " + arg.raw_value);
        }

        if (!scriptEntry.hasObject("duration"))
            throw new InvalidArgumentsException("Requires a valid duration!");
    }


    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        // Fetch objects
        dScript script = (dScript) scriptEntry.getObject("script");
        Duration duration = (Duration) scriptEntry.getObject("duration");
        Type type = (scriptEntry.hasObject("type") ?
                (Type) scriptEntry.getObject("type") : Type.PLAYER);

        // Report to dB
        dB.report(getName(), aH.debugObj("Type", type.name())
                + script.debug()
                + (type.name().equalsIgnoreCase("player") ? scriptEntry.getPlayer().debug() : "")
                + duration.debug());

        // Perform cooldown
        switch (type) {
            case PLAYER:
                setCooldown(scriptEntry.getPlayer().getName(),
                        duration,
                        script.getName(),
                        false);

            case GLOBAL:
                setCooldown(null,
                        duration,
                        script.getName(),
                        true);
        }
    }


    /**
     * Gets the duration of a script cool-down.
     *
     * @param playerName
     *         the Player to check, null if only checking Global.
     * @param scriptName
     *         the name of the script to check
     * @return a Duration of the time remaining
     */
    public static Duration getCooldownDuration(String playerName, String scriptName) {

        // Change to UPPERCASE so there's no case-sensitivity.
        scriptName = scriptName.toUpperCase();

        Duration duration = Duration.ZERO;

        // Check current entry GLOBALLY, reset it if necessary
        if (DenizenAPI._saves().contains("Global.Scripts." + scriptName + ".Cooldown Time")) {
            if (System.currentTimeMillis()
                    < DenizenAPI._saves().getLong("Global.Scripts." + scriptName + ".Cooldown Time"))
                duration = new Duration((double) (DenizenAPI._saves().getLong("Global.Scripts." + scriptName
                        + ".Cooldown Time") - System.currentTimeMillis()) / 1000);
        }

        // No player specified? No need to check any further...
        if (playerName == null)
            return duration;

        // Now check for player-specific cooldowns
        playerName = playerName.toUpperCase();

        // If no entry for the script, return true
        if (!DenizenAPI._saves().contains("Players." + playerName + ".Scripts." + scriptName + ".Cooldown Time"))
            return duration;

        // If there is an entry, check against the time
        if (System.currentTimeMillis()
                >= DenizenAPI._saves().getLong("Players." + playerName + ".Scripts." + scriptName + ".Cooldown Time")) {
            Duration player_dur = new Duration((double) (DenizenAPI._saves().getLong("Players." + playerName + ".Scripts."
                    + scriptName + ".Cooldown Time") - System.currentTimeMillis()) / 1000);
            if (player_dur.getSeconds() > duration.getSeconds())
                return player_dur;
        }

        return duration;
    }


    /**
     * Checks if a script is cooled-down. If a cool-down is currently in progress,
     * its requirements will fail and it will not trigger. If the script is being cooled down
     * globally, this will also return false.
     *
     * @param playerName
     *         the Player to check, null if only checking Global.
     * @param scriptName
     *         the name of the script to check
     * @return true if the script is cool
     */
    public static boolean checkCooldown(String playerName, String scriptName) {

        // Change to UPPERCASE so there's no case-sensitivity.
        scriptName = scriptName.toUpperCase();

        // Check current entry GLOBALLY, reset it if necessary
        if (DenizenAPI._saves().contains("Global.Scripts." + scriptName + ".Cooldown Time")) {
            if (System.currentTimeMillis()
                    < DenizenAPI._saves().getLong("Global.Scripts." + scriptName + ".Cooldown Time"))
                return false;
            else
                DenizenAPI._saves().set("Global.Scripts." + scriptName + ".Cooldown Time", null);
        }

        // No player specified? No need to check any further...
        if (playerName == null)
            return true;

        // Now check for player-specific cooldowns
        playerName = playerName.toUpperCase();

        // If no entry for the script, return true
        if (!DenizenAPI._saves().contains("Players." + playerName + ".Scripts." + scriptName + ".Cooldown Time"))
            return true;

        // If there is an entry, check against the time
        if (System.currentTimeMillis()
                >= DenizenAPI._saves().getLong("Players." + playerName + ".Scripts." + scriptName + ".Cooldown Time"))    {
            DenizenAPI._saves().set("Players." + playerName + ".Scripts." + scriptName + ".Cooldown Time", null);
            return true;
        }

        return false;
    }


    /**
     * Sets a cooldown for a Denizen Script. Can be for a specific Player, or GLOBAL.
     *
     * @param playerName
     *         if not a global cooldown, the Player to set the cooldown for
     * @param duration
     *         the duration of the cooldown period, in seconds
     * @param scriptName
     *         the name of the script to cooldown
     * @param global
     *         whether the script should be cooled down globally
     */
    public static void setCooldown(String playerName, Duration duration, String scriptName, boolean global) {
        // I hate case-sensitivity. The positive here outweighs the negative.
        if (playerName != null) playerName = playerName.toUpperCase();
        scriptName = scriptName.toUpperCase();

        // Set global cooldown
        if (global) {
            DenizenAPI._saves().set("Global.Scripts." + scriptName + ".Cooldown Time",
                    System.currentTimeMillis()
                            + (duration.getSecondsAsInt() * 1000));

            // or set Player cooldown
        }   else {
            DenizenAPI._saves().set("Players." + playerName + ".Scripts." + scriptName + ".Cooldown Time",
                    System.currentTimeMillis()
                            + (duration.getSecondsAsInt() * 1000));
        }
    }

}