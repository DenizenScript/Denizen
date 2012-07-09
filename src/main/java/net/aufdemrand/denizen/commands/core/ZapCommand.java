package net.aufdemrand.denizen.commands.core;

import net.aufdemrand.denizen.commands.Command;
import net.aufdemrand.denizen.scriptEngine.ScriptCommand;

/**
 * Sets the current step for Players in a specific script then stores
 * the information in the Denizen 'saves.yml'.
 * 
 * @author Jeremy Schroeder
 *
 */

public class ZapCommand extends Command {

	/* ZAP (Step #)

	/* Arguments: [] - Required, () - Optional 
	 * (Step #) The step to make the current step. If not specified, assumes current step + 1. 
	 * 
	 * Modifiers: 
	 * (SCRIPT:[Script Name]) Changes the script to ZAP from the current script to the one specified.
	 */

	@Override
	public boolean execute(ScriptCommand theCommand) {

		String theScript = theCommand.getScript();
		Integer theStep = theCommand.getStep() + 1;

		/* Get arguments */
		if (theCommand.arguments() != null) {
			for (String thisArgument : theCommand.arguments()) {
				if (thisArgument.matches("((-|\\+)?[0-9]+(\\.[0-9]+)?)+"))
					theStep = Integer.valueOf(thisArgument);

				else if (thisArgument.contains("SCRIPT:")) 
					theScript = thisArgument.split(":", 2)[1];
			}
		}

		/* Set saves.yml */
		if (theCommand.getPlayer() != null && theScript != null && theStep != null) {
			plugin.getSaves().set("Players." + theCommand.getPlayer().getName() + "." + theScript + ".Current Step", theStep); 
			plugin.saveSaves();
			return true;
		}

		theCommand.error("Unknown error. Check syntax.");
		return false;
	}

}