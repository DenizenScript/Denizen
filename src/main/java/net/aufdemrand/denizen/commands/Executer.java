package net.aufdemrand.denizen.commands;

import java.util.logging.Level;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.scriptEngine.ScriptCommand;
import net.citizensnpcs.command.exception.CommandException;

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

			try {
				command.execute(theCommand);
			} catch (CommandException e) {
				plugin.getLogger().log(Level.SEVERE, e.getMessage()); 
				e.printStackTrace();
			}
			return true;

		}

		else plugin.getLogger().log(Level.SEVERE, "Invalid scriptCommand!");
		return false;

	}



}