package net.aufdemrand.denizen.commands.core;

import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.citizensnpcs.command.exception.CommandException;

public class WeatherCommand extends net.aufdemrand.denizen.commands.AbstractCommand {
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

		if (theEntry.arguments().length == 0|| theEntry.arguments()[0] == null) return false;

		String arg = theEntry.arguments()[0].toLowerCase();

		if (arg.contains("sun")) { 
			theEntry.getDenizen().getWorld().setStorm(false); 
			if (plugin.debugMode) plugin.getLogger().info("Set Weather to Sunny");
			return true;
		}
		else if (arg.contains("storm")) {
			theEntry.getDenizen().getWorld().setThundering(true); 
			if (plugin.debugMode) plugin.getLogger().info("Set Weather to Storming");
			return true;
		}
		else if (arg.contains("precip")) {
			theEntry.getDenizen().getWorld().setStorm(true); 
			if (plugin.debugMode) plugin.getLogger().info("Set Weather to Precitating");
			return true;
		}


		if (plugin.debugMode) plugin.getLogger().info("Invalid weather: " + arg);
		
		return false;

	}

}

