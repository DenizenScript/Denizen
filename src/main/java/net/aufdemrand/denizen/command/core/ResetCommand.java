package net.aufdemrand.denizen.command.core;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.aufdemrand.denizen.command.Command;
import net.aufdemrand.denizen.scriptEngine.ScriptEntry;
import net.citizensnpcs.command.exception.CommandException;

/**
 * 
 *  
 * @author Jeremy Schroeder
 */

public class ResetCommand extends Command {

	/* RESET [FINISHED|FAILED] ['Name of Script']  or  RESET [FLAG:[NAME]]

	/* Arguments: [] - Required, () - Optional 
	 * [FINISHED|FAILED|FLAG:[NAME]] 
	 * (['Name of Script']) - Required when using FINISH/FAIL
	 * 
	 * Modifiers: 
	 * None.
	 * 
	 * Example usages:
	 */

	//case RESET: // RESET FINISH(ED) [Name of Script]  or  RESET FAIL(ED) [NAME OF SCRIPT]
	//	String executeScript;
	//	if (commandArgs[2] == null) executeScript=theScript; else executeScript=executeArgs[4].split(" ", 3)[2];
	//	if (commandArgs[1].equalsIgnoreCase("FINISH") || commandArgs[1].equalsIgnoreCase("FINISHED")) {
	//		plugin.getSaves().set("Players." + thePlayer.getName() + "." + executeScript + "." + "Completed", 0);
	//		plugin.saveSaves();
	//	}
	//
	//	if (commandArgs[1].equalsIgnoreCase("FAIL") || commandArgs[1].equalsIgnoreCase("FAILED")) {
	//		plugin.getSaves().set("Players." + thePlayer.getName() + "." + executeScript + "." + "Failed", false);
	//		plugin.saveSaves();
	//	}
	//
	//	break;
	
	
	@Override
	public boolean execute(ScriptEntry theCommand) throws CommandException {

		String theScript = theCommand.getScript();
		
		throw new CommandException("Unknown error, check syntax!");
	}


}