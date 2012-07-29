package net.aufdemrand.denizen.command.core;

import java.util.logging.Level;

import org.bukkit.entity.Player;

import net.aufdemrand.denizen.command.Command;
import net.aufdemrand.denizen.scriptEngine.ScriptEntry;
import net.citizensnpcs.command.exception.CommandException;

/**
 * 
 * 
 * 
 * @author Jeremy Schroeder
 *
 */

public class FailCommand extends Command {

	/* 

	/* Arguments: [] - Required, () - Optional 
	 *  
	 * 
	 * Modifiers: 
	 * 
	 */

	@Override
	public boolean execute(ScriptEntry theCommand) throws CommandException {

		String theScript = theCommand.getScript();
		
		/* Get arguments */
		if (theCommand.arguments() != null) {
			for (String thisArgument : theCommand.arguments()) {

				if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "Processing command " + theCommand.getCommand() + " argument: " + thisArgument);

				/* Change the script to a specified one */
				if (thisArgument.contains("SCRIPT:")) 
					theScript = thisArgument.split(":", 2)[1];

				else {
					if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "Unable to match argument!");
				}
			
			}
		}
		
		int fails = plugin.getAssignments().getInt("Players." + theCommand.getPlayer().getName() + "." + theScript + "." + "Failed", 0);

		fails++;	
		
		plugin.getSaves().set("Players." + theCommand.getPlayer().getName() + "." + theScript + "." + "Failed", fails);
		plugin.saveSaves();

		throw new CommandException("Unknown error, check syntax!");
	}

	
	public boolean getScriptFail(Player thePlayer, String theScript, boolean negativeRequirement) {

		boolean outcome = false;

		if (plugin.getSaves().getString("Players." + thePlayer.getName() + "." + theScript + "." + "Failed") != null) { 
			if (plugin.getSaves().getBoolean("Players." + thePlayer.getName() + "." + theScript + "." + "Failed")) outcome = true;
		}

		if (negativeRequirement != outcome) return true;

		return false;

	}
	
}