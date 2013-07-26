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


    public static String getHelp() {
        return  "Cools down an interact script. While cool, players cannot " +
                "run the script. When on cooldown, the script will not pass " +
                "requirements allowing the next lowest priority script to " +
                "trigger. You can use <s@script_name.cooled_down[player]> to " +
                "return whether the script is cooled down, and <s@script_name.cooldown> " +
                "to get the duration of the cooldown in progress. Cooldown requires" +
                "a type (player or default, a script, and a duration. It also requires" +
                "a valid link to a dPlayer.\n" +
                " \n" +
                "Use to keep a player from activating a script for a specified duration. \n" +
                "- cooldown bonus_script 11h \n" +
                "- cooldown hit_indicator 5s \n" +
                "Use the 'global' argument to indicate the script to be on cooldown for all players. \n" +
                "- cooldown global daily_treasure_offering 24h  \n";
    }


    public static String getUsage() {
        return "- cooldown ({player}|global) (script_name) [duration]";
    }


    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("type")
                    && arg.matchesEnum(Type.values()))
                // add Type
                scriptEntry.addObject("type", arg.asElement());


            else if (!scriptEntry.hasObject("duration")
                    && arg.matchesArgumentType(Duration.class))
                // add range (for WALKNEAR)
                scriptEntry.addObject("duration", arg.asType(Duration.class));


            else if (!scriptEntry.hasObject("script")
                    && arg.matchesArgumentType(dScript.class))
                // add anchor ID
                scriptEntry.addObject("script", arg.asType(dScript.class));
        }

        // Check to make sure required arguments have been filled

        if (scriptEntry.hasObject("type")
                && ((Element) scriptEntry.getObject("type")).identify().equalsIgnoreCase("player")
                && scriptEntry.getPlayer() == null)
            throw new InvalidArgumentsException("Requires a type, either ");

        if ((!scriptEntry.hasObject("script"))
                || (scriptEntry.hasObject("script")
                && !((dScript) scriptEntry.getObject("script")).isValid()))
            throw new InvalidArgumentsException(Messages.ERROR_NO_SCRIPT);

        if ((!scriptEntry.hasObject("duration"))
                || (scriptEntry.hasObject("script")
                && !((dScript) scriptEntry.getObject("script")).isValid()))
            throw new InvalidArgumentsException(Messages.ERROR_NO_SCRIPT);
    }


    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        // Fetch objects
        dScript script = (dScript) scriptEntry.getObject("script");
        Duration duration = (Duration) scriptEntry.getObject("duration");
        Element type = (scriptEntry.hasObject("type") ?
                (Element) scriptEntry.getObject("type") : new Element("player"));

        // Report to dB
        dB.report(getName(), type.debug()
                + script.debug()
                + (type.toString().equalsIgnoreCase("player") ? scriptEntry.getPlayer().debug() : "")
                + duration.debug());

        // Perform cooldown
        Type type_ = Type.valueOf(type.asString().toUpperCase());

        switch (type_) {
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
            if (System.currentTimeMillis()
                    < DenizenAPI._saves().getLong("Global.Scripts." + scriptName + ".Cooldown Time"))
                return false;
            else
                DenizenAPI._saves().set("Global.Scripts." + scriptName + ".Cooldown Time", null);
        }

        // Now check for player-specific cooldowns

        // If no entry for the script, return true
        if (!DenizenAPI._saves().contains("Players." + playerName + ".Scripts." + scriptName + ".Cooldown Time"))
            return true;

        // If there is an entry, check against the time
        if (System.currentTimeMillis()
                >= DenizenAPI._saves().getLong("Players." + playerName + ".Scripts." + scriptName + ".Cooldown Time"))	{
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