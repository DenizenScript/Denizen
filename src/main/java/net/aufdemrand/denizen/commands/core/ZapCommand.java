package net.aufdemrand.denizen.commands.core;

import java.util.Random;

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
		Integer theStep = null;

		/* Get arguments */
		if (theCommand.arguments() != null) {
			for (String thisArgument : theCommand.arguments()) {
				if (thisArgument.matches("((-|\\+)?[0-9]+(\\.[0-9]+)?)+"))
					theStep = Integer.valueOf(thisArgument);

				else if (thisArgument.contains("SCRIPT:")) 
					theScript = thisArgument.split(":", 2)[1];

				else if (thisArgument.contains("RANDOM:")) {

					int high = 0, low = 0;

					if (thisArgument.split(":")[1].split(" ").length == 1) {
						if (thisArgument.split(":")[1].matches("((-|\\+)?[0-9]+(\\.[0-9]+)?)+")) {
							low = 1;
							high = Integer.valueOf(thisArgument.split(":")[1]); 
						} 
					}
					else if (thisArgument.split(":")[1].split(" ").length == 2) {
						if (thisArgument.split(":")[1].split(" ")[0].matches("((-|\\+)?[0-9]+(\\.[0-9]+)?)+")
								&& thisArgument.split(":")[1].split(" ")[1].matches("((-|\\+)?[0-9]+(\\.[0-9]+)?)+") ) {
							low = Integer.valueOf(thisArgument.split(":")[1].split(" ")[0]);
							high = Integer.valueOf(thisArgument.split(":")[1].split(" ")[1]);
						}
					}
					
					Random randomInt = new Random();
					
					if (high - low > 0) {
						theStep = randomInt.nextInt(high - low + 1) + low;
					}
					
					else theStep = high;
					
				}
			}
		}

		if (theStep == null) theStep = plugin.scriptEngine.getCurrentStep(theCommand.getPlayer(), theScript);

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