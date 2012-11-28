package net.aufdemrand.denizen.scripts.commands.core;

import org.bukkit.Bukkit;

import net.aufdemrand.denizen.events.ScriptFinishEvent;
import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.Debugger.Messages;

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

	String scriptName = null;
	String playerName = null;

	@Override
	public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

		// Get some defaults from the ScriptEntry
		scriptName = scriptEntry.getScript();
		if (scriptEntry.getPlayer() != null) playerName = scriptEntry.getPlayer().getName();

		// Parse the arguments
		for (String arg : scriptEntry.getArguments()) {

			if (aH.matchesScript(arg)) {
				scriptName = aH.getStringFrom(arg);
				dB.echoDebug(Messages.DEBUG_SET_SCRIPT, arg);
				continue;
				
			}	else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
		}
		
		if (playerName == null) throw new InvalidArgumentsException(Messages.ERROR_NO_PLAYER);
		if (scriptName == null) throw new InvalidArgumentsException(Messages.ERROR_NO_SCRIPT);
	}

	@Override
	public void execute(String commandName) throws CommandExecutionException {
		finishScript(playerName, scriptName);		
	}

	public boolean finishScript(String playerName, String scriptName) {
		scriptName = scriptName.toUpperCase();
		int finishes = denizen.getSaves().getInt("Players." + playerName.toUpperCase() + "." + scriptName + "." + "Completed", 0);

		// Increase finishes by one and save.
		finishes++;
		denizen.getSaves().set("Players." + playerName.toUpperCase() + "." + scriptName + "." + "Completed", finishes);
		denizen.saveSaves();

		// Call ScriptFinishEvent
		ScriptFinishEvent event = new ScriptFinishEvent(playerName, scriptName, finishes);
		Bukkit.getServer().getPluginManager().callEvent(event);

		return true;
	}

	public int getScriptCompletes(String playerName, String scriptName) {
		return denizen.getSaves().getInt("Players." + playerName.toUpperCase() + "." + scriptName.toUpperCase() + "." + "Failed", 0);	
	}

    @Override
    public void onEnable() {
        // TODO Auto-generated method stub
        
    }

}


