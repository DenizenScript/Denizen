package net.aufdemrand.denizen.commands.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.ChatColor;

import net.aufdemrand.denizen.commands.AbstractCommand;
import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.runnables.FourItemRunnable;
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

	private enum FlagType { VALUE, INC, DEC, BOOLEAN }

	private Map<String, Integer> taskMap = new ConcurrentHashMap<String, Integer>();

	@Override
	public boolean execute(ScriptEntry theEntry) throws CommandException {

		String theFlag = null;
		FlagType flagType = null;
		Integer duration = null;
		String theValue = null;
		boolean global = false;
		DenizenNPC theDenizen = null;
		boolean isDenizen = false;

		if (theEntry.arguments() == null)
			throw new CommandException("...Usage: FLAG [[NAME]:[VALUE]|[NAME]:++|[NAME]:--]");

		/* Get arguments */
		for (String thisArg : theEntry.arguments()) {
			
			// Fill replaceables
			if (thisArg.contains("<")) thisArg = aH.fillReplaceables(theEntry.getPlayer(), theEntry.getDenizen(), thisArg, false);

			/* If argument is a DURATION: modifier */
			if (aH.matchesDuration(thisArg)) {
				duration = aH.getIntegerModifier(thisArg);
				aH.echoDebug("...flag will be removed after '%s' seconds.", thisArg);
			}

			else if (thisArg.toUpperCase().equals("GLOBAL")) {
				global = true;
				aH.echoDebug("...this flag will be GLOBAL.");
			}

			else if (thisArg.toUpperCase().equals("DENIZEN")) {
				if (theEntry.getDenizen() != null) {
					theDenizen = theEntry.getDenizen();
					isDenizen = true;
					aH.echoDebug("...this flag will be bound to the DENIZEN.");
				} else {
					aH.echoError("No NPC found!");
					return false;
				}
			}

			else if (thisArg.toUpperCase().equals("PLAYER")) {
					aH.echoDebug("...this flag will be bound to the PLAYER.");
			}
			
			else if (aH.matchesNPCID(thisArg)) {
				theDenizen = aH.getNPCIDModifier(thisArg);
				if (theDenizen == null) {
					aH.echoError("No NPC found!");
					return false;
				}
				isDenizen = true;
				aH.echoDebug("...changing referenced NPC to '%s'.", theDenizen.getName());
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
					theValue = thisArg.split(":")[1];
				}
				aH.echoDebug("...setting FLAG '%s'.", thisArg);
			}

			/* Otherwise, argument is a Boolean */
			else {
				theFlag = thisArg.toUpperCase();
				theValue = "TRUE";
				flagType = FlagType.BOOLEAN;
				aH.echoDebug("...setting '%s' as boolean flag.", thisArg.toUpperCase());
			}

		}
		
		
		if (theEntry.getTexts()[0] != null && theValue != null)
			if (theValue.contains("<*>"))
				theValue = theValue.replace("<*>", theEntry.getTexts()[0]);
		
		

		/* If a duration is set... */
		if (duration != null && flagType != null && theFlag != null) {

			aH.echoDebug("Setting delayed task: RESET FLAG '%s'", theFlag);

			String flagKey = theEntry.getPlayer().getName() + " " + theFlag;
			if (global) flagKey = "GLOBAL" + " " + theFlag;
			if (isDenizen) flagKey = theDenizen.getName() + "." + theDenizen.getId() + " " + theFlag;

			if (taskMap.containsKey(flagKey)) {
				try {
					plugin.getServer().getScheduler().cancelTask(taskMap.get(flagKey));
				} catch (Exception e) { }
			}
			
			flagKey = flagKey.split(" ")[0];

			taskMap.put(flagKey, plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new FourItemRunnable<String, String, String, Boolean>(flagKey, theFlag, theValue, isDenizen) {
				@Override
				public void run(String player, String flag, String checkValue, Boolean denizen) { 

					if (player.equals("GLOBAL")) {
						aH.echoDebug(ChatColor.YELLOW + "//DELAYED//" + ChatColor.WHITE + " Running delayed task: RESET GLOBAL FLAG '" + flag + ".");
						if (plugin.getSaves().contains("Global.Flags." + flag))
							if (plugin.getSaves().getString("Global.Flags." + flag).equals(checkValue))
								plugin.getSaves().set("Global.Flags." + flag, null);
					} else if(denizen) { 
						aH.echoDebug(ChatColor.YELLOW + "//DELAYED//" + ChatColor.WHITE + " Running delayed task: RESET FLAG '" + flag + "' for " + player + ".");
						if (plugin.getSaves().contains("Denizens." + player + ".Flags." + flag))
							if (plugin.getSaves().getString("Denizens." + player + ".Flags." + flag).equals(checkValue))
								plugin.getSaves().set("Denizens." + player + ".Flags." + flag, null);
					} else {
						aH.echoDebug(ChatColor.YELLOW + "//DELAYED//" + ChatColor.WHITE + " Running delayed task: RESET FLAG '" + flag + "' for " + player + ".");
						if (plugin.getSaves().contains("Players." + player + ".Flags." + flag))
							if (plugin.getSaves().getString("Players." + player + ".Flags." + flag).equals(checkValue))
								plugin.getSaves().set("Players." + player + ".Flags." + flag, null);
					}
				}
			}, duration * 20));	
		}


		/* Set the flag! */
		if (flagType != null && theFlag != null) {

			switch (flagType) {

			case INC:
				try {
					if (global) {
						double incValue = Double.valueOf(plugin.getSaves().getString("Global.Flags." + theFlag, "0")) + 1;
						plugin.getSaves().set("Global.Flags." + theFlag, incValue);
					} else if (isDenizen) {
						double incValue = Double.valueOf(plugin.getSaves().getString("Denizens." + theDenizen.getName() + "." + theDenizen.getId() + ".Flags." + theFlag, "0")) + 1;
						plugin.getSaves().set("Denizens." + theDenizen.getName() + "." + theDenizen.getId() + ".Flags." + theFlag, incValue);
					} else {
						double incValue = Double.valueOf(plugin.getSaves().getString("Players." + theEntry.getPlayer().getName() + ".Flags." + theFlag, "0")) + 1;
						plugin.getSaves().set("Players." + theEntry.getPlayer().getName() + ".Flags." + theFlag, incValue);
					}
				} catch (NumberFormatException e) {
					aH.echoError("Cannot increment a non-integer flag!");
				}
				break;

			case DEC:
				try {
					if (global) {
						double incValue = Double.valueOf(plugin.getSaves().getString("Global.Flags." + theFlag, "0")) - 1;
						plugin.getSaves().set("Global.Flags." + theFlag, incValue);
					} else if (isDenizen) {
						double incValue = Double.valueOf(plugin.getSaves().getString("Denizens." + theDenizen.getName() + "." + theDenizen.getId() + ".Flags." + theFlag, "0")) - 1;
						plugin.getSaves().set("Denizens." + theDenizen.getName() + "." + theDenizen.getId() + ".Flags." + theFlag, incValue);
					} else {
						double incValue = Double.valueOf(plugin.getSaves().getString("Players." + theEntry.getPlayer().getName() + ".Flags." + theFlag, "0")) - 1;
						plugin.getSaves().set("Players." + theEntry.getPlayer().getName() + ".Flags." + theFlag, incValue);
					}
				} catch (NumberFormatException e) {
					aH.echoError("Cannot decrease a non-integer flag!");
				}
				break;

			case VALUE:
				if (global) {
					plugin.getSaves().set("Global.Flags." + theFlag, theValue);
				} else if(isDenizen) {
					plugin.getSaves().set("Denizens." + theDenizen.getName() + "." + theDenizen.getId() + ".Flags." + theFlag, theValue);
				} else {
					plugin.getSaves().set("Players." + theEntry.getPlayer().getName() + ".Flags." + theFlag, theValue);
				}
				break;

			case BOOLEAN:
				if (global) {
					plugin.getSaves().set("Global.Flags." + theFlag, theValue);	
				} else if (isDenizen) {
					plugin.getSaves().set("Denizens." + theDenizen.getName() + "." + theDenizen.getId() + ".Flags." + theFlag, theValue);
				} else {
					plugin.getSaves().set("Players." + theEntry.getPlayer().getName() + ".Flags." + theFlag, theValue);
				}
				break;

			}

			plugin.saveSaves();
			return true;
		}

		return false;
	}


}