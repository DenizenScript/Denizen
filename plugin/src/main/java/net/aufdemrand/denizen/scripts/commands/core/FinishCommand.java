package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.bukkit.ScriptFinishEvent;
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
 * Sets a Script as 'FINISHED'. Scripts can be finished multiple times and Denizen will keep track
 * of the total amount. This can also be checked with the SCRIPT requirement or an IF command.
 *
 * @author Jeremy Schroeder
 */

public class FinishCommand extends AbstractCommand {

    /* FINISH ('SCRIPT:[Script Name]') (PLAYER:[Player Name])

    /* Arguments: [] - Required, () - Optional
     * ('SCRIPT:[Script Name]') Changes the script from the triggering script to the one specified.
     *    Defaults to the script that the Script Entry was sent from.
     * (PLAYER:[Player Name]) Changes the Player affected.
     *    Defaults to the Player who triggered the script.
     *
     * Example Usage:
     * FINISH PLAYER:aufdemrand
     * FINISH 'SCRIPT:A different script'
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
        dB.report(scriptEntry, getName(),
                player.debug() + script.debug());

        dB.echoError(scriptEntry.getResidingQueue(), "Finish is outdated, use flags instead!");

        finishScript(player.getName(), script.getName());
    }

    public static void resetFinishes(String playerName, String scriptName) {
        scriptName = scriptName.toUpperCase();
        DenizenAPI._saves().set("Players." + playerName + "." + scriptName + "." + "Completed", null);
    }

    // TODO: Why is this a boolean?
    public static boolean finishScript(String playerName, String scriptName) {
        scriptName = scriptName.toUpperCase();
        int finishes = DenizenAPI._saves().getInt("Players." + playerName + "." + scriptName + "." + "Completed", 0);

        // Increase finishes by one and save.
        finishes++;
        DenizenAPI._saves().set("Players." + playerName + "." + scriptName + "." + "Completed", finishes);

        // Call ScriptFinishEvent
        ScriptFinishEvent event = new ScriptFinishEvent(playerName, scriptName, finishes);
        Bukkit.getServer().getPluginManager().callEvent(event);

        return true;
    }

    public static int getScriptCompletes(String playerName, String scriptName) {
        return DenizenAPI.getCurrentInstance().getSaves().getInt("Players." + playerName + "." + scriptName.toUpperCase() + "." + "Completed", 0);
    }
}


