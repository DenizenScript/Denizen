package net.aufdemrand.denizen.commands.core;

import net.aufdemrand.denizen.commands.AbstractCommand;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.citizensnpcs.command.exception.CommandException;

public class WeatherCommand extends AbstractCommand {

	/* WEATHER */
	/* 
	 * Arguments: [] - Required, () - Optional 
	 * [SUNNY or STORMING or PRECIPITATING]
	 *   
	 * Example Usage:
	 * WEATHER STORMY
	 */
	@Override
	public boolean execute(ScriptEntry theEntry) throws CommandException {

		if (theEntry.arguments().length == 0 || theEntry.arguments() == null)
			throw new CommandException("...Usage: WEATHER [SUN|STORM|PRECIPITATION]");

		
		String arg = theEntry.arguments()[0].toLowerCase();

		if (arg.contains("sun")) { 
			theEntry.getDenizen().getWorld().setStorm(false); 
			aH.echoDebug("...set Weather to 'Sunny'.");
			return true;
		}
		else if (arg.contains("storm")) {
			theEntry.getDenizen().getWorld().setThundering(true); 
			aH.echoDebug("...set weather to 'Storming'.");
			return true;
		}
		else if (arg.contains("precip")) {
			theEntry.getDenizen().getWorld().setStorm(true); 
			aH.echoDebug("...set weather to 'Precipitating'.");
			return true;
		}

		else aH.echoError("Invalid weather: '%s'", arg);
		
		return false;

	}

}
