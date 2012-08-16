package net.aufdemrand.denizen.commands.core;

import net.aufdemrand.denizen.commands.AbstractCommand;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.citizensnpcs.command.exception.CommandException;

/**
 * Sets a Player 'Flag'. Flags can hold information to check against
 * with the HASFLAG or FLAG requirements.
 *  
 * @author Jeremy Schroeder
 */

public class FlagCommand extends AbstractCommand {

	/* FLAG [[NAME]:[VALUE]|[NAME]:++|[NAME]:--]

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
	public boolean execute(ScriptEntry theEntry) throws CommandException {

		String theFlag = null;
		FlagType flagType = null;
		Integer duration = null;
		String theValue = null;

		if (theEntry.arguments() == null)
			throw new CommandException("...Usage: FLAG [[NAME]:[VALUE]|[NAME]:++|[NAME]:--]");

		/* Get arguments */
		for (String thisArg : theEntry.arguments()) {

			/* If argument is a DURATION: modifier */
			if (aH.matchesDuration(thisArg)) {
				duration = aH.getIntegerModifier(thisArg);
				aH.echoDebug("...flag will be removed after '&s' seconds.", thisArg);
			}

			/* If argument is a flag with value */
			else if (thisArg.split(":").length == 2) {
				theFlag = thisArg.split(":")[0].toUpperCase();

				if (thisArg.split(":")[1].equals("++")) 
					flagType = FlagType.INC;
				else if (thisArg.split(":")[1].equals("--"))
					flagType = FlagType.DEC;
				else {
					flagType = FlagType.VALUE;
					theValue = thisArg.split(":")[1].toUpperCase();
				}
				aH.echoDebug("...setting FLAG '%s'.", thisArg);
			}

			/* Otherwise, argument is a Boolean */
			else {
				theFlag = thisArg.toUpperCase();
				flagType = FlagType.BOOLEAN;
				aH.echoDebug("...setting '%s' as boolean flag.", thisArg.toUpperCase());
			}

		}


		/* If a duration is set... */
		if (duration != null && flagType != null && theFlag != null) {

			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new FlagCommandRunnable<String, String, String>(theEntry.getPlayer().getName(), theFlag, theValue) {
				@Override
				public void run(String player, String flag, String checkValue) { 
					if (plugin.getSaves().getString("Players." + player + ".Flags." + flag).equals(checkValue))
						plugin.getSaves().set("Players." + player + ".Flags." + flag, null);
				}
			}, duration * 20);
		}

		
		/* Set the flag! */
		if (flagType != null && theFlag != null) {

			switch (flagType) {

			case INC:
				int incValue = plugin.getSaves().getInt("Players." + theEntry.getPlayer().getName() + ".Flags." + theFlag, 0) + 1;
				plugin.getSaves().set("Players." + theEntry.getPlayer().getName() + ".Flags." + theFlag, incValue);
				break;

			case DEC:
				int decValue = plugin.getSaves().getInt("Players." + theEntry.getPlayer().getName() + ".Flags." + theFlag, 0) - 1;
				plugin.getSaves().set("Players." + theEntry.getPlayer().getName() + ".Flags." + theFlag, decValue);
				break;

			case VALUE:
				plugin.getSaves().set("Players." + theEntry.getPlayer().getName() + ".Flags." + theFlag, theValue);
				break;

			case BOOLEAN:
				plugin.getSaves().set("Players." + theEntry.getPlayer().getName() + ".Flags." + theFlag, true);
				break;

			}
			
			plugin.saveSaves();
			return true;
		}

		return false;
	}


}