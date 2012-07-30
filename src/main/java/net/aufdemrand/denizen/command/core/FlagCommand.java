package net.aufdemrand.denizen.command.core;

import java.util.logging.Level;

import net.aufdemrand.denizen.command.Command;
import net.aufdemrand.denizen.scriptEngine.ScriptEntry;
import net.citizensnpcs.command.exception.CommandException;

/**
 * Sets a Player 'Flag'. Flags can hold information to check against
 * with the HASFLAG or FLAG requirements.
 *  
 * @author Jeremy Schroeder
 */

public class FlagCommand extends Command {

	/* 

	/* Arguments: [] - Required, () - Optional 
	 * [NAME:VALUE]  or  [NAME:++]  or  [NAME:--]
	 * 
	 * Modifiers: 
	 * (DURATION:#) Reverts to the previous head position after # amount of seconds.
	 * 
	 * Example usages:
	 * FLAG 'MAGICSHOPITEM:FEATHER' 'DURATION:60'
	 * FLAG 'HOSTILECOUNT:++'
	 * FLAG 'ALIGNMENT:--'
	 * FLAG 'CUSTOMFLAG:SET'
	 */

	enum FlagType { VALUE, INC, DEC, BOOLEAN }

	@Override
	public boolean execute(ScriptEntry theCommand) throws CommandException {

		String theFlag = null;
		FlagType flagType = null;
		Integer duration = null;
		String theValue = null;

		/* Get arguments */
		if (theCommand.arguments() != null) {
			for (String thisArgument : theCommand.arguments()) {

				if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "Processing command " + theCommand.getCommand() + " argument: " + thisArgument);

				// If argument is a DURATION modifier...
				if (thisArgument.toUpperCase().contains("DURATION:")) {
					if (thisArgument.split(":")[1].matches("((-|\\+)?[0-9]+(\\.[0-9]+)?)+")) {
						if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...duration set to " + duration + " second(s).");
						duration = Integer.valueOf(thisArgument.split(":")[1]);
					}
				}

				else if (thisArgument.split(":").length == 2) {
					if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...setting flag '" + thisArgument.split(":")[0].toUpperCase() + "'.");

					theFlag = thisArgument.split(":")[0].toUpperCase();
					if (thisArgument.split(":")[1].equals("++")) 
						flagType = FlagType.INC;
					else if (thisArgument.split(":")[1].equals("--"))
						flagType = FlagType.DEC;
					else {
						flagType = FlagType.VALUE;
						theValue = thisArgument.split(":")[1].toUpperCase();
					}
				}
				
				else {
					
					if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...setting flag '" + thisArgument.split(":")[0].toUpperCase() + "'.");
					flagType = FlagType.BOOLEAN;
				}

			}
		}

		if (flagType != null && theFlag != null) {

			switch (flagType) {

			case INC:
				int incValue = plugin.getSaves().getInt("Players." + theCommand.getPlayer().getName() + ".Flags." + theFlag, 0) + 1;
				plugin.getSaves().set("Players." + theCommand.getPlayer().getName()+ ".Flags." + theFlag, incValue);
				plugin.saveSaves();
				break;

			case DEC:
				int decValue = plugin.getSaves().getInt("Players." + theCommand.getPlayer().getName() + ".Flags." + theFlag, 0) - 1;
				plugin.getSaves().set("Players." + theCommand.getPlayer().getName()+ ".Flags." + theFlag, decValue);
				plugin.saveSaves();
				break;

			case VALUE:
				plugin.getSaves().set("Players." + theCommand.getPlayer().getName()+ ".Flags." + theFlag, theValue);
				plugin.saveSaves();
				break;
				
			case BOOLEAN:
				plugin.getSaves().set("Players." + theCommand.getPlayer().getName()+ ".Flags." + theFlag, true);
				plugin.saveSaves();
				break;

			}

			return true;
		}

		if (duration != null) {

			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new FlagCommandRunnable<String, String, String>(theCommand.getPlayer().getName(), theFlag, theValue) {
				@Override
				public void run(String player, String flag, String checkValue) { 
					if (plugin.getSaves().getString("Players." + player + ".Flags." + flag).equals(checkValue))
						plugin.getSaves().set("Players." + player + ".Flags." + flag, null);
				}
			}, duration * 20);
		}

		throw new CommandException("Unknown error, check syntax!");
	}


}