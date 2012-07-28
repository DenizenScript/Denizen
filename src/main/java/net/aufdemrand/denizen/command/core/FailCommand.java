package net.aufdemrand.denizen.command.core;

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

		/* Get arguments */
		if (theCommand.arguments() != null) {
			for (String thisArgument : theCommand.arguments()) {

				/* If number argument... */
				if (thisArgument.matches("((-|\\+)?[0-9]+(\\.[0-9]+)?)+"))
					return true;
					

				/* If modifier... */
		//		else if (thisArgument.contains("SCRIPT:")) 


			}
		}


		
		
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