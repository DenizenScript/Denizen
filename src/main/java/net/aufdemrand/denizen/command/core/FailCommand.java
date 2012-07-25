package net.aufdemrand.denizen.command.core;

import org.bukkit.entity.Player;

import net.aufdemrand.denizen.commands.DenizenCommand;
import net.aufdemrand.denizen.scriptEngine.ScriptCommand;
import net.citizensnpcs.command.exception.CommandException;

/**
 * 
 * 
 * 
 * @author Jeremy Schroeder
 *
 */

public class FailCommand extends DenizenCommand {

	/* 

	/* Arguments: [] - Required, () - Optional 
	 *  
	 * 
	 * Modifiers: 
	 * 
	 */

	@Override
	public boolean execute(ScriptCommand theCommand) throws CommandException {

		/* Get arguments */
		if (theCommand.arguments() != null) {
			for (String thisArgument : theCommand.arguments()) {

				/* If number argument... */
				if (thisArgument.matches("((-|\\+)?[0-9]+(\\.[0-9]+)?)+"))
					

				/* If modifier... */
				else if (thisArgument.contains("SCRIPT:")) 


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