package net.aufdemrand.denizen.commands;

import java.util.logging.Level;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.scripts.ScriptEntry;
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

			AbstractCommand command = plugin.getCommandRegistry().getCommand(theCommand.getCommand());

			if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "Executing command " + theCommand.getCommand());

			try {
				if (command.execute(theCommand)) {
					if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...success!"); }
				else {
					if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...check syntax, command was not successfully run!"); }
			} catch (CommandException e) {
				if (plugin.debugMode) plugin.getLogger().log(Level.INFO, e.getMessage()); 
				if (plugin.showStackTraces) e.printStackTrace();
			}
			return true;

		}

		else {
			if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "Executing command " + theCommand.getCommand());
			if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...invalid Command!");
		}
		return false;

	}



}