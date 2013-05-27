package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.events.ScriptFinishEvent;
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
        // Initialize required fields
        String player = null;

        // Get some defaults from the ScriptEntry
        dScript script = scriptEntry.getScript();

        if (scriptEntry.getPlayer() != null)
            player = scriptEntry.getPlayer().getName();
        if (player == null && scriptEntry.getOfflinePlayer() != null)
            player = scriptEntry.getOfflinePlayer().getName();

        // Parse the arguments
        for (String arg : scriptEntry.getArguments()) {

            if (aH.matchesScript(arg)) {
                script = aH.getScriptFrom(arg);

            } else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
        }

        // Check for required args
        if (player == null)
            throw new InvalidArgumentsException(Messages.ERROR_NO_PLAYER);
        if (script == null)
            throw new InvalidArgumentsException(Messages.ERROR_NO_SCRIPT);

        // Stash objects
        scriptEntry.addObject("script", script);
        scriptEntry.addObject("player", player);
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        // Grab objects from scriptEntry
        String player = (String) scriptEntry.getObject("player");
        dScript script = (dScript) scriptEntry.getObject("script");

        // Report to dB
        dB.report(getName(),
                aH.debugObj("Player", player)
                        + script.debug());

		finishScript(player, script.getName());
	}

    public static void resetFinishes(String playerName, String scriptName) {
        scriptName = scriptName.toUpperCase();
        DenizenAPI._saves().set("Players." + playerName + "." + scriptName + "." + "Completed", null);
    }

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


