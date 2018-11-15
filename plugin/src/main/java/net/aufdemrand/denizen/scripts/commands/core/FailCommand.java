package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.bukkit.ScriptFailEvent;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dScript;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.Bukkit;

/**
 * Sets a Script as 'FAILED'. Scripts can be failed multiple times and Denizen will keep track
 * of the total amount. This can also be checked against with the SCRIPT requirement.
 *
 * @author aufdemrand
 */

public class FailCommand extends AbstractCommand {

    /* FAIL ('SCRIPT:[Script Name]') (PLAYER:[Player Name])

    /* Arguments: [] - Required, () - Optional
     * ('SCRIPT:[Script Name]') Changes the script from the triggering script to the one specified.
     *    Defaults to the script that the Script Entry was sent from.
     * (PLAYER:[Player Name]) Changes the Player affected.
     *    Defaults to the Player who triggered the script.
     *
     * Example Usage:
     * FAIL PLAYER:aufdemrand
     * FAIL 'SCRIPT:A different script'
     */

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Parse the arguments
        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (arg.matchesArgumentType(dScript.class)) {
                scriptEntry.addObject("script", arg.asType(dScript.class));
            }
            else if (arg.matchesArgumentType(dPlayer.class)) {
                scriptEntry.addObject("player", arg.asType(dPlayer.class));
            }
            else {
                arg.reportUnhandled();
            }
        }

        // Check for required args
        scriptEntry.defaultObject("player", ((BukkitScriptEntryData) scriptEntry.entryData).getPlayer());
        scriptEntry.defaultObject("script", scriptEntry.getScript());
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        // Grab objects from scriptEntry
        dScript script = (dScript) scriptEntry.getObject("script");
        dPlayer player = (dPlayer) scriptEntry.getObject("player");

        // Report to dB
        if (scriptEntry.dbCallShouldDebug()) {
            dB.report(scriptEntry, getName(),
                    player.debug() + script.debug());
        }

        dB.echoError(scriptEntry.getResidingQueue(), "The Fail command is outdated, use flags intead!");

        failScript(player.getName(), script.getName());
    }

    public static void resetFails(String playerName, String scriptName) {
        scriptName = scriptName.toUpperCase();
        DenizenAPI._saves().set("Players." + playerName + "." + scriptName + "." + "Failed", null);
    }

    /**
     * Increases a scripts 'failed' counter for a specified Player.
     *
     * @param playerName name of the Player
     * @param scriptName name of the Script
     */
    public static void failScript(String playerName, String scriptName) {
        scriptName = scriptName.toUpperCase();
        int fails = DenizenAPI._saves().getInt("Players." + playerName + "." + scriptName + "." + "Failed", 0);

        // Increase fails by one and set.
        fails++;
        DenizenAPI._saves().set("Players." + playerName + "." + scriptName + "." + "Failed", fails);

        // Call ScriptFailEvent
        ScriptFailEvent event = new ScriptFailEvent(playerName, scriptName, fails);
        Bukkit.getServer().getPluginManager().callEvent(event);
    }

    /**
     * Gets the number of times a Player has 'failed' a script.
     *
     * @param playerName name of the Player
     * @param scriptName name of the Script
     * @return number of times the Player has failed the specified script
     */
    public static int getScriptFails(String playerName, String scriptName) {
        return DenizenAPI.getCurrentInstance().getSaves()
                .getInt("Players." + playerName + "." + scriptName.toUpperCase() + "." + "Failed", 0);
    }
}
