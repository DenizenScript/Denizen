package net.aufdemrand.denizen.scripts.commands.core;

import org.bukkit.Bukkit;

import net.aufdemrand.denizen.events.ScriptFailEvent;
import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.Debugger.Messages;

/**
 * Sets a Script as 'FAILED'. Scripts can be failed multiple times and Denizen will keep track
 * of the total amount. This can also be checked against with the SCRIPT requirement.
 * 
 * @author Jeremy Schroeder
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
		failScript(playerName, scriptName);		
	}

	/**
	 * Increases a scripts 'failed' counter for a specified Player. 
	 * 
	 * @param playerName
	 * 		name of the Player
	 * @param scriptName
	 * 		name of the Script
	 */
	public void failScript(String playerName, String scriptName) {
		scriptName = scriptName.toUpperCase();
		int fails = denizen.getSaves().getInt("Players." + playerName.toUpperCase() + "." + scriptName + "." + "Failed", 0);

		// Increase fails by one and set.
		fails++;
		denizen.getSaves().set("Players." + playerName.toUpperCase() + "." + scriptName + "." + "Failed", fails);
		denizen.saveSaves();
		
		// Call ScriptFailEvent
		ScriptFailEvent event = new ScriptFailEvent(playerName, scriptName, fails);
		Bukkit.getServer().getPluginManager().callEvent(event);
	}
	
	/**
	 * Gets the number of times a Player has 'failed' a script.
	 * 
	 * @param playerName
	 * 		name of the Player
	 * @param scriptName
	 * 		name of the Script
	 * @return
	 * 		number of times the Player has failed the specified script
	 */
	public int getScriptFails(String playerName, String scriptName) {
		return denizen.getSaves().getInt("Players." + playerName.toUpperCase() + "." + scriptName.toUpperCase() + "." + "Failed", 0); 
	}

    @Override
    public void onEnable() {
        // TODO Auto-generated method stub
        
    }
}