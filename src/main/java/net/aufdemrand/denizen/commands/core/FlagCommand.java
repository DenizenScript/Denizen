package net.aufdemrand.denizen.commands.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.ChatColor;

import net.aufdemrand.denizen.commands.AbstractCommand;
import net.aufdemrand.denizen.runnables.ThreeItemRunnable;
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

		if (theEntry.arguments() == null)
			throw new CommandException("...Usage: FLAG [[NAME]:[VALUE]|[NAME]:++|[NAME]:--]");

		/* Get arguments */
		for (String thisArg : theEntry.arguments()) {

			/* If argument is a DURATION: modifier */
			if (aH.matchesDuration(thisArg)) {
				duration = aH.getIntegerModifier(thisArg);
				aH.echoDebug("...flag will be removed after '%s' seconds.", thisArg);
			}

			else if (thisArg.toUpperCase().contains("GLOBAL")) {
				global = true;
				aH.echoDebug("...this flag will be GLOBAL.");
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

		if (theEntry.getTexts()[0] != null && theValue != null) {
			theValue = theValue.replace("<*>", theEntry.getTexts()[0]);
			theFlag = theFlag.replace("<*>", theEntry.getTexts()[0]);
		}

		if (theValue != null && theEntry.getPlayer() != null) {

			String itemInHandMaterial = "FALSE"; 
			if (theEntry.getPlayer().getItemInHand() != null) 
				itemInHandMaterial = theEntry.getPlayer().getItemInHand().getType().name();

			String itemInHandQty = "FALSE"; 
			if (theEntry.getPlayer().getItemInHand() != null) 
				itemInHandQty = String.valueOf(theEntry.getPlayer().getItemInHand().getAmount());

			String itemInHandId = "FALSE"; 
			if (theEntry.getPlayer().getItemInHand() != null) 
				itemInHandId = String.valueOf(theEntry.getPlayer().getItemInHand().getTypeId());

			String playerKiller = "FALSE"; 
			if (theEntry.getPlayer().getKiller() != null) 
				playerKiller = theEntry.getPlayer().getKiller().getName();

			String playerHelm = "FALSE"; 
			if (theEntry.getPlayer().getInventory().getHelmet() != null) 
				playerHelm = theEntry.getPlayer().getInventory().getHelmet().getType().name();

			String playerBoots = "FALSE"; 
			if (theEntry.getPlayer().getInventory().getBoots() != null) 
				playerBoots = theEntry.getPlayer().getInventory().getBoots().getType().name();

			String playerChestplate = "FALSE"; 
			if (theEntry.getPlayer().getInventory().getChestplate() != null) 
				playerChestplate = theEntry.getPlayer().getInventory().getChestplate().getType().name();

			String playerLeggings = "FALSE"; 
			if (theEntry.getPlayer().getInventory().getLeggings() != null) 
				playerLeggings = theEntry.getPlayer().getInventory().getLeggings().getType().name();

			String playerMoney = "0";
			if (plugin.economy != null) 
				playerMoney = String.valueOf(plugin.economy.getBalance(theEntry.getPlayer().getName()));



			theValue = theValue.replace("<ITEM_IN_HAND.MATERIAL>", itemInHandMaterial)
					.replace("<ITEM_IN_HAND.QTY>", itemInHandQty)
					.replace("<ITEM_IN_HAND.ID>", itemInHandId)
					.replace("<PLAYER.NAME>", theEntry.getPlayer().getName())
					.replace("<PLAYER.KILLER>", playerKiller)
					.replace("<PLAYER.HEALTH>", String.valueOf(theEntry.getPlayer().getHealth()))
					.replace("<PLAYER.HELM>", playerHelm)
					.replace("<PLAYER.LEGGINGS>", playerLeggings)
					.replace("<PLAYER.BOOTS>", playerBoots)
					.replace("<PLAYER.CHESTPLATE>", playerChestplate)
					.replace("<PLAYER.WORLD>", theEntry.getPlayer().getWorld().getName())
					.replace("<PLAYER.MONEY>", playerMoney);
			theFlag = theFlag.replace("<ITEM_IN_HAND.MATERIAL>", itemInHandMaterial)
					.replace("<ITEM_IN_HAND.QTY>", itemInHandQty)
					.replace("<ITEM_IN_HAND.ID>", itemInHandId)
					.replace("<PLAYER.NAME>", theEntry.getPlayer().getName())
					.replace("<PLAYER.KILLER>", playerKiller)
					.replace("<PLAYER.HEALTH>", String.valueOf(theEntry.getPlayer().getHealth()))
					.replace("<PLAYER.HELM>", playerHelm)
					.replace("<PLAYER.LEGGINGS>", playerLeggings)
					.replace("<PLAYER.BOOTS>", playerBoots)
					.replace("<PLAYER.CHESTPLATE>", playerChestplate)
					.replace("<PLAYER.WORLD>", theEntry.getPlayer().getWorld().getName())
					.replace("<PLAYER.MONEY>", playerMoney);

		}

		String denizenNPCId = "";
		if (theValue != null && theEntry.getDenizen() != null) {
			denizenNPCId = String.valueOf(theEntry.getDenizen().getName());
			theValue = theValue.replace("<DENIZEN.NPCID>", denizenNPCId);
			theFlag = theFlag.replace("<DENIZEN.NPCID>", denizenNPCId);
		}


		/* If a duration is set... */
		if (duration != null && flagType != null && theFlag != null) {

			aH.echoDebug("Setting delayed task: RESET FLAG '%s'", theFlag);

			String playerName = theEntry.getPlayer().getName();
			if (global) playerName = "GLOBAL";

			if (taskMap.containsKey(playerName)) {
				try {
					plugin.getServer().getScheduler().cancelTask(taskMap.get(playerName));
				} catch (Exception e) { }
			}

			taskMap.put(playerName, plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new ThreeItemRunnable<String, String, String>(playerName, theFlag, theValue) {
				@Override
				public void run(String player, String flag, String checkValue) { 

					if (player.equals("GLOBAL")) {
						aH.echoDebug(ChatColor.YELLOW + "//DELAYED//" + ChatColor.WHITE + " Running delayed task: RESET GLOBAL FLAG '" + flag + ".");
						if (plugin.getSaves().contains("Global.Flags." + flag))
							if (plugin.getSaves().getString("Global.Flags." + flag).equals(checkValue))
								plugin.getSaves().set("Global.Flags." + flag, null);
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
				if (global) {
					int incValue = plugin.getSaves().getInt("Server.Flags." + theFlag, 0) + 1;
					plugin.getSaves().set("Server.Flags." + theFlag, incValue);
				} else {
					int incValue = plugin.getSaves().getInt("Players." + theEntry.getPlayer().getName() + ".Flags." + theFlag, 0) + 1;
					plugin.getSaves().set("Players." + theEntry.getPlayer().getName() + ".Flags." + theFlag, incValue);
				}
				break;

			case DEC:
				if (global) {
					int decValue = plugin.getSaves().getInt("Global.Flags." + theFlag, 0) - 1;
					plugin.getSaves().set("Global.Flags." + theFlag, decValue);
				} else {
					int decValue = plugin.getSaves().getInt("Players." + theEntry.getPlayer().getName() + ".Flags." + theFlag, 0) - 1;
					plugin.getSaves().set("Players." + theEntry.getPlayer().getName() + ".Flags." + theFlag, decValue);
				}
				break;

			case VALUE:
				if (global) {
					plugin.getSaves().set("Global.Flags." + theFlag, theValue);
				} else {
					plugin.getSaves().set("Players." + theEntry.getPlayer().getName() + ".Flags." + theFlag, theValue);
				}
				break;

			case BOOLEAN:
				if (global) {
					plugin.getSaves().set("Global.Flags." + theFlag, theValue);	
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