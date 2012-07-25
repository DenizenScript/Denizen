package net.aufdemrand.denizen.command;

import java.util.logging.Level;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.scriptEngine.ScriptEntry;
import net.citizensnpcs.command.exception.CommandException;

public class Executer {

	private Denizen plugin;

	public Executer(Denizen denizen) {
		plugin = denizen;
	}


	/*
	 * Executes a command defined in theCommand 
	 */

	public boolean execute(ScriptEntry theCommand) {
		if (plugin.getCommandRegistry().getCommand(theCommand.getCommand()) != null) {

			Command command = plugin.getCommandRegistry().getCommand(theCommand.getCommand());

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