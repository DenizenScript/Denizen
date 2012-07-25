package net.aufdemrand.denizen.command.core;

import java.util.Random;

import net.aufdemrand.denizen.command.Command;
import net.aufdemrand.denizen.scriptEngine.ScriptEntry;
import net.citizensnpcs.command.exception.CommandException;

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
	public boolean execute(ScriptEntry theCommand) throws CommandException {

		String theScript = theCommand.getScript();
		Integer theStep = null;
		Integer duration = null;

		/* Get arguments */
		if (theCommand.arguments() != null) {
			for (String thisArgument : theCommand.arguments()) {

				/* Set the step to ZAP to */
				if (thisArgument.matches("((-|\\+)?[0-9]+(\\.[0-9]+)?)+"))
					theStep = Integer.valueOf(thisArgument);

				/* Change the script to a specified one */
				else if (thisArgument.contains("SCRIPT:")) 
					theScript = thisArgument.split(":", 2)[1];

				/* Pick a random step */
				else if (thisArgument.contains("RANDOM:")) {
					int high = 1, low = 1;
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
					if (high - low > 0) theStep = randomInt.nextInt(high - low + 1) + low;
					else theStep = high;
				}
				
				/* Set a duration */
				else if (thisArgument.toUpperCase().contains("DURATION:"))
					if (thisArgument.split(":")[1].matches("((-|\\+)?[0-9]+(\\.[0-9]+)?)+"))
						duration = Integer.valueOf(thisArgument.split(":")[1]);

			}
		}

		if (theStep == null) theStep = plugin.scriptEngine.getCurrentStep(theCommand.getPlayer(), theScript);

		
		/* Make delayed task to reset step if duration is set */
		if (duration != null) {

			final String player = theCommand.getPlayer().getName();
			final String script = theScript;
			final Integer step = theStep;
			final Integer oldStep = plugin.scriptEngine.getCurrentStep(theCommand.getPlayer(), theScript);
			
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				@Override
				public void run() { 

					// Reset step after duration if step remains the same.
					if (plugin.getSaves().getInt("Players." + player + "." + script + ".Current Step") == step) {
						plugin.getSaves().set("Players." + player + "." + script + ".Current Step", oldStep);
					}
					
				}
			}, duration * 20);
		}

		/* Set saves.yml */
		if (theCommand.getPlayer() != null && theScript != null && theStep != null) {
			plugin.getSaves().set("Players." + theCommand.getPlayer().getName() + "." + theScript + ".Current Step", theStep); 
			plugin.saveSaves();
			return true;
		}

		throw new CommandException("Unknown error, check syntax!");
	}

}