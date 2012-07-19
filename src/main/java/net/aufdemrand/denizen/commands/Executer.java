package net.aufdemrand.denizen.commands;

import java.util.logging.Level;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.scriptEngine.ScriptCommand;

public class Executer {
	
	private Denizen plugin;
	
	public Executer(Denizen denizen) {
		plugin = denizen;
	}
	
	
	/*
	 * Executes a command defined in theCommand 
	 */

	public boolean execute(ScriptCommand theCommand) {
		if (plugin.commandRegistry.getCommand(theCommand.getCommand()) != null) {
			
			Command command = plugin.commandRegistry.getCommand(theCommand.getCommand());

			if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "Executing command " + theCommand.getCommand());

			if (!command.execute(theCommand)) {
				plugin.getLogger().log(Level.SEVERE, "A script Command has failed to execute.");
				plugin.getLogger().log(Level.SEVERE, "Command " + theCommand.getCommand() + " has failed in script " + theCommand.getScript() + ".");
				plugin.getLogger().log(Level.SEVERE, "--- Information on the failure below:");
				plugin.getLogger().log(Level.INFO, theCommand.getError());
			}
			return true;

		}
		
		else plugin.getLogger().log(Level.SEVERE, "Invalid scriptCommand!");
		return false;

	}
	
	
	
}