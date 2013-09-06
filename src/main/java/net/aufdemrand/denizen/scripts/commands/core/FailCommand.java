package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.events.ScriptFailEvent;
import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.dScript;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
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
        // Get some defaults from the ScriptEntry
        dScript script = scriptEntry.getScript();

        // Parse the arguments
        for (String arg : scriptEntry.getArguments()) {

            if (aH.matchesScript(arg)) {
                script = aH.getScriptFrom(arg);

            } else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
        }

        // Check for required args
        if (scriptEntry.getPlayer() == null)
            throw new InvalidArgumentsException(Messages.ERROR_NO_PLAYER);
        if (script == null)
            throw new InvalidArgumentsException(Messages.ERROR_NO_SCRIPT);

        // Stash objects
        scriptEntry.addObject("script", script);
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        // Grab objects from scriptEntry
        dScript script = (dScript) scriptEntry.getObject("script");

        // Report to dB
        dB.report(getName(),
                scriptEntry.getPlayer().debug()
                        + script.debug());

        failScript(scriptEntry.getPlayer().getName(), script.getName());
    }

    public static void resetFails(String playerName, String scriptName) {
        scriptName = scriptName.toUpperCase();
        DenizenAPI._saves().set("Players." + playerName + "." + scriptName + "." + "Failed", null);
    }

    /**
     * Increases a scripts 'failed' counter for a specified Player.
     *
     * @param playerName
     *         name of the Player
     * @param scriptName
     *         name of the Script
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
     * @param playerName
     *         name of the Player
     * @param scriptName
     *         name of the Script
     * @return
     *         number of times the Player has failed the specified script
     */
    public static int getScriptFails(String playerName, String scriptName) {
        return DenizenAPI.getCurrentInstance().getSaves().getInt("Players." + playerName + "." + scriptName.toUpperCase() + "." + "Failed", 0);
    }

}
