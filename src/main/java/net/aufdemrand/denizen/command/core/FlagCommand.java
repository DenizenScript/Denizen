package net.aufdemrand.denizen.command.core;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.aufdemrand.denizen.command.Command;
import net.aufdemrand.denizen.scriptEngine.ScriptEntry;
import net.citizensnpcs.command.exception.CommandException;

/**
 * Sets a Script as 'FINISHED'. Scripts can be finished multiple times.
 * This can also be checked against with the FINISHED requirement.
 *  
 * @author Jeremy Schroeder
 */

public class FlagCommand extends Command {

	/* 

	/* Arguments: [] - Required, () - Optional 
	 * 
	 * 
	 * Modifiers: 
	 * (DURATION:#) Reverts to the previous head position after # amount of seconds.
	 * 
	 * Example usages:
	 */

	@Override
	public boolean execute(ScriptEntry theCommand) throws CommandException {

		String theFlag = null;
		Integer duration = null;
		
		/* Get arguments */
		if (theCommand.arguments() != null) {
			for (String thisArgument : theCommand.arguments()) {

				if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "Processing command " + theCommand.getCommand() + " argument: " + thisArgument);

				// If argument is a DURATION modifier...
				if (thisArgument.toUpperCase().contains("DURATION:")) {
					if (thisArgument.split(":")[1].matches("((-|\\+)?[0-9]+(\\.[0-9]+)?)+")) {
						duration = Integer.valueOf(thisArgument.split(":")[1]);
						if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...duration set to " + duration + " second(s).");
					}
				}
				
				

				else {
					if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "Unable to match argument!");
				}
			
			}
		}
		
	

		throw new CommandException("Unknown error, check syntax!");
	}

	
}