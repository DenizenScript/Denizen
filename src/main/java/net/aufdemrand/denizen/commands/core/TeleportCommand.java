package net.aufdemrand.denizen.commands.core;

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

public class TeleportCommand extends DenizenCommand {

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

}