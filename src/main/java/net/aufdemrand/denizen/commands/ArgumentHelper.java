package net.aufdemrand.denizen.commands;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.bookmarks.BookmarkHelper.BookmarkType;
import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.scripts.ScriptEngine.QueueType;
import net.citizensnpcs.api.CitizensAPI;

public class ArgumentHelper {

	Denizen plugin;

	ArgumentHelper(Denizen plugin) {
		this.plugin = plugin;
	}


	public void echoDebug(String message, String argument) {
		CommandSender cs = plugin.getServer().getConsoleSender();
		if (plugin.debugMode)
			cs.sendMessage(ChatColor.LIGHT_PURPLE + "| " + ChatColor.WHITE + String.format(message, argument));
	}

	public void echoDebug(String message) {
		CommandSender cs = plugin.getServer().getConsoleSender();
		if (plugin.debugMode)
			cs.sendMessage(ChatColor.LIGHT_PURPLE + "| " + ChatColor.WHITE + message);
	}

	public void echoError(String message) {
		CommandSender cs = plugin.getServer().getConsoleSender();
		if (plugin.debugMode)
			cs.sendMessage(ChatColor.LIGHT_PURPLE + "| " + ChatColor.RED + "ERROR! " + ChatColor.WHITE + message);
	}

	public void echoError(String message, String argument) {
		CommandSender cs = plugin.getServer().getConsoleSender();
		if (plugin.debugMode)
			cs.sendMessage(ChatColor.LIGHT_PURPLE + "| " + ChatColor.RED + "ERROR! " + ChatColor.WHITE + String.format(message, argument));
	}


	/* ----------------------------- */

	public String getStringModifier(String argument) {
		if (argument.split(":").length >= 2)
			return argument.split(":")[1];
		else return argument;
	}

	public QueueType getQueueModifier(String argument) {
		try {
			if (argument.split(":").length >= 2)
				return QueueType.valueOf(argument.split(":")[1].toUpperCase());
			else return QueueType.valueOf(argument.toUpperCase());
		} catch (Exception e) { echoError("Invalid Queuetype!"); return null; }
	}

	public Integer getIntegerModifier(String argument) {
		if (argument.split(":").length >= 2)
			return Integer.valueOf(argument.split(":")[1]);
		else return Integer.valueOf(argument);
	}

	public Location getBookmarkModifier(String thisArg, DenizenNPC denizenNPC) {
		Matcher m = bookmarkArgument.matcher(thisArg);
		Matcher m2 = bookmarkArgument2.matcher(thisArg);

		if (m.matches()) {
			if (plugin.bookmarks.exists(thisArg.split(":")[1], thisArg.split(":")[2])) 
				return plugin.bookmarks.get(thisArg.split(":")[1], thisArg.split(":")[2], BookmarkType.LOCATION);
		}

		else if (m2.matches()) {
			if (plugin.bookmarks.exists(denizenNPC, thisArg.split(":")[1]))
				return plugin.bookmarks.get(denizenNPC, thisArg.split(":")[1], BookmarkType.LOCATION);
		}

		if (plugin.debugMode) echoError("...bookmark not found!");
		return null;
	}

	public Location getBlockBookmarkModifier(String thisArg, DenizenNPC denizenNPC) {
		Matcher m = bookmarkArgument.matcher(thisArg);
		Matcher m2 = bookmarkArgument2.matcher(thisArg);

		if (m.matches()) {
			if (plugin.bookmarks.exists(thisArg.split(":")[1], thisArg.split(":")[2])) 
				return plugin.bookmarks.get(thisArg.split(":")[1], thisArg.split(":")[2], BookmarkType.BLOCK);
		}

		else if (m2.matches()) {
			if (plugin.bookmarks.exists(denizenNPC, thisArg.split(":")[1]))
				return plugin.bookmarks.get(denizenNPC, thisArg.split(":")[1], BookmarkType.BLOCK);
		}

		if (plugin.debugMode) echoError("...block bookmark not found!");
		return null;
	}

	public ItemStack getItemModifier(String thisArg) {
		Matcher m = materialArgument.matcher(thisArg);
		Matcher m2 = materialArgument2.matcher(thisArg);
		Matcher m3 = materialArgument3.matcher(thisArg);
		Matcher m4 = materialArgument4.matcher(thisArg);

		ItemStack theItem = null;

		try {

			if (m.matches())
				theItem = new ItemStack(Integer.valueOf(thisArg));

			if (m2.matches()) {
				theItem = new ItemStack(Integer.valueOf(thisArg.split(":")[0]));
				// theItem.setData(new MaterialData(Integer.valueOf(thisArg.split(":")[1])));
				theItem.setDurability(Short.valueOf(thisArg.split(":")[1]));
			}

			if (m3.matches()) {
				theItem = new ItemStack(Material.valueOf(thisArg.toUpperCase()));
			}

			if (m4.matches()) {
				theItem = new ItemStack(Material.valueOf(thisArg.split(":")[0].toUpperCase()));
				// theItem.setData(new MaterialData());
				theItem.setDurability(Short.valueOf(thisArg.split(":")[1]));
			}

		} catch (Exception e) {
			if (plugin.debugMode) echoError("...invalid item!");
			if (plugin.showStackTraces) e.printStackTrace();
		}

		return theItem;
	}

	public DenizenNPC getNPCIDModifier(String thisArg) {
		Matcher m = npcIDArgument.matcher(thisArg);
		DenizenNPC denizen = null;

		if (m.matches()) 
			try {
				if (CitizensAPI.getNPCRegistry().getById(Integer.valueOf(thisArg.split(":")[1])) != null)
					denizen = plugin.getDenizenNPCRegistry().getDenizen(CitizensAPI.getNPCRegistry().getById(Integer.valueOf(thisArg.split(":")[1])));	
			} catch (Exception e) {
				if (plugin.debugMode) echoError("...NPCID specified could not be matched to a Denizen!");
			}

		return denizen;
	}



	/* ------------------------------ */


	final public Pattern npcIDArgument = Pattern.compile("(?:NPCID|npcid)(:)(\\d+)");
	public boolean matchesNPCID(String regex) {
		Matcher m = npcIDArgument.matcher(regex);
		return m.matches();
	}

	final public Pattern materialArgument = Pattern.compile("\\d+");
	final public Pattern materialArgument2 = Pattern.compile("(\\d+)(:)(\\d+)");
	final public Pattern materialArgument3 = Pattern.compile("([a-zA-Z\\x5F]+)");
	final public Pattern materialArgument4 = Pattern.compile("([a-zA-Z]+?)(:)(\\d+)");
	public boolean matchesItem(String regex) {
		Matcher m = materialArgument.matcher(regex);
		Matcher m2 = materialArgument2.matcher(regex);
		Matcher m3 = materialArgument3.matcher(regex);
		Matcher m4 = materialArgument4.matcher(regex);
		if (m.matches() || m2.matches() || m3.matches() || m4.matches())
			return true;

		else return false;
	}

	final public Pattern durationArgument = Pattern.compile("(?:DURATION|duration|Duration)(:)(\\d+)");
	public boolean matchesDuration(String regex) {
		Matcher m = durationArgument.matcher(regex);
		return m.matches();
	}

	final public Pattern queuetypeArgument = Pattern.compile("(?:QUEUE|queue|Queue)(:)(?:TASK|Task|Trigger|TRIGGER)");
	public boolean matchesQueueType(String regex) {
		Matcher m = queuetypeArgument.matcher(regex);
		return m.matches();
	}

	final public Pattern worldArgument = Pattern.compile("(?:WORLD|world|World)(:)([a-zA-Z0-9]+?)");
	public boolean matchesWorld(String regex) {
		Matcher m = worldArgument.matcher(regex);
		return m.matches();
	}

	final public Pattern groupArgument = Pattern.compile("(?:GROUP|group|Group)(:)([a-zA-Z0-9]+?)");
	public boolean matchesGroup(String regex) {
		Matcher m = groupArgument.matcher(regex);
		return m.matches();
	}

	final public Pattern integerArgument = Pattern.compile("\\d+");
	public boolean matchesInteger(String regex) {
		Matcher m = integerArgument.matcher(regex);
		return m.matches();
	}

	final public Pattern scriptArgument = Pattern.compile("(?:SCRIPT|script|Script)(:)(.+)");
	public boolean matchesScript(String regex) {
		Matcher m = scriptArgument.matcher(regex);
		return m.matches();
	}

	final public Pattern quantityArgument = Pattern.compile("(?:QTY|qty|Qty)(:)(\\d+)");
	public boolean matchesQuantity(String regex) {
		Matcher m = quantityArgument.matcher(regex);
		return m.matches();
	}

	final public Pattern bookmarkArgument = Pattern.compile("(?:bookmark|BOOKMARK|Bookmark)(:)(\\w+)(:)(\\w+)");
	final public Pattern bookmarkArgument2 = Pattern.compile("(?:bookmark|BOOKMARK|Bookmark)(:)(\\w+)");
	public boolean matchesBookmark(String regex) {
		Matcher m = bookmarkArgument.matcher(regex);
		Matcher n = bookmarkArgument2.matcher(regex);
		if (m.matches() || n.matches()) return true;
		else return false;
	}



	/**
	 * Fills in replaceable data for flags and more.
	 * 
	 * Handles 2 types of replacement, queue-time and run-time. 
	 * 
	 * Use quickReplaceable = true 
	 * for queue-time. This type of replacement is made when the script is being put into the player
	 * queue.
	 * 
	 * Use quickReplaceable = false
	 * for run-time. This type of replacement is made right at the time of code-execution.
	 *
	 */

	final public Pattern replaceableFlag = Pattern.compile("(<)(\\^FLAG|FLAG)(.G|.GLOBAL|.P|.PLAYER|.D|.DENIZEN)(:)(.*?)(>)");
	public String fillReplaceables(Player thePlayer, DenizenNPC theDenizen, String stringToFill, boolean quickReplaceable) {

		if (stringToFill == null) return null;

		// First, check for flags.
		Matcher f = replaceableFlag.matcher(stringToFill);
		StringBuffer sb = new StringBuffer(stringToFill.length());

		// Filled string to send back. Obviously, not filled yet...
		String filledString = stringToFill;

		// For each find in the String
		while (f.find()) {

			// Check which type of replaceable
			if (f.group(2).contains("^") && !quickReplaceable)
				continue;

			if (!f.group(2).contains("^") && quickReplaceable)
				continue;

			String searchPath = "";

			// Check flag replacement type
			if (f.group(3).startsWith(".G"))
				searchPath = "Global.Flags.";
			else if (f.group(3).startsWith(".D") && theDenizen != null)
				searchPath = "Denizens." + theDenizen.getName() + "." + theDenizen.getId() + ".Flags.";
			else if (f.group(3).startsWith(".P") && thePlayer != null)
				searchPath = "Players." + thePlayer.getName() + ".Flags.";

			// Replaceable data detected
			if (f.group(5).split(":").length > 1) {
				if (plugin.getSaves().contains(searchPath + f.group(5).split(":")[0].toUpperCase() )) {
					f.appendReplacement(sb, plugin.getSaves().getString(searchPath + f.group(5).split(":")[0].toUpperCase() ));
					echoDebug(ChatColor.YELLOW + "//REPLACED//" + ChatColor.WHITE + " '%s' with flag value.", f.group(5));
				} else {
					f.appendReplacement(sb, f.group(5).split(":")[1]);
					echoDebug(ChatColor.YELLOW + "//REPLACED//" + ChatColor.WHITE + " '%s' flag not found, using fallback!", f.group(5));
				}
			}

			// No replaceable data
			else {
				if (plugin.getSaves().contains(searchPath + f.group(5).toUpperCase() )) {
					f.appendReplacement(sb, plugin.getSaves().getString(searchPath + f.group(5).split(":")[0].toUpperCase() ));
					echoDebug(ChatColor.YELLOW + "//REPLACED//" + ChatColor.WHITE + " '%s' with flag value.", f.group(5));
				} else {
					f.appendReplacement(sb, "");
					echoDebug(ChatColor.YELLOW + "//REPLACED//" + ChatColor.WHITE + " '%s' flag not found!", f.group(5));
				}
			}
		}

		f.appendTail(sb);
		
		filledString = sb.toString();

		// Player object flag replacement
		if (thePlayer != null && filledString.contains("<")) {

			String itemInHandMaterial = "AIR"; 
			if (thePlayer.getItemInHand() != null) 
				itemInHandMaterial = thePlayer.getItemInHand().getType().name();
			
			String itemInHandName = "nothing"; 
			if (thePlayer.getItemInHand() != null) 
				itemInHandName = thePlayer.getItemInHand().getType().name().toLowerCase().replace("_", " ");
			
			String itemInHandQty = "0"; 
			if (thePlayer.getItemInHand() != null) 
				itemInHandQty = String.valueOf(thePlayer.getItemInHand().getAmount());

			String itemInHandId = "0"; 
			if (thePlayer.getItemInHand() != null) 
				itemInHandId = String.valueOf(thePlayer.getItemInHand().getTypeId());

			String playerKiller = "nobody"; 
			if (thePlayer.getKiller() != null) 
				playerKiller = thePlayer.getKiller().getName();

			String playerHelm = "FALSE"; 
			if (thePlayer.getInventory().getHelmet() != null) 
				playerHelm = thePlayer.getInventory().getHelmet().getType().name();

			String playerBoots = "FALSE"; 
			if (thePlayer.getInventory().getBoots() != null) 
				playerBoots = thePlayer.getInventory().getBoots().getType().name();

			String playerChestplate = "FALSE"; 
			if (thePlayer.getInventory().getChestplate() != null) 
				playerChestplate = thePlayer.getInventory().getChestplate().getType().name();

			String playerLeggings = "FALSE"; 
			if (thePlayer.getInventory().getLeggings() != null) 
				playerLeggings = thePlayer.getInventory().getLeggings().getType().name();

			String playerMoney = "0";
			if (plugin.economy != null) 
				playerMoney = String.valueOf(plugin.economy.getBalance(thePlayer.getName()));

			if (quickReplaceable) 
				filledString = filledString
				.replace("<^PLAYER.ITEM_IN_HAND.MATERIAL>", itemInHandMaterial)
				.replace("<^PLAYER.ITEM_IN_HAND.NAME>", itemInHandName)
				.replace("<^PLAYER.ITEM_IN_HAND.QTY>", itemInHandQty)
				.replace("<^PLAYER.ITEM_IN_HAND.ID>", itemInHandId)
				.replace("<^PLAYER.NAME>", thePlayer.getName())
				.replace("<^PLAYER>", thePlayer.getName())
				.replace("<^PLAYER.KILLER>", playerKiller)
				.replace("<^PLAYER.HEALTH>", String.valueOf(thePlayer.getHealth()))
				.replace("<^PLAYER.HELM>", playerHelm)
				.replace("<^PLAYER.LEGGINGS>", playerLeggings)
				.replace("<^PLAYER.BOOTS>", playerBoots)
				.replace("<^PLAYER.CHESTPLATE>", playerChestplate)
				.replace("<^PLAYER.WORLD>", thePlayer.getWorld().getName())
				.replace("<^PLAYER.MONEY>", playerMoney)
				.replace("<^PLAYER.EXP_TO_NEXT_LEVEL>", String.valueOf(thePlayer.getExpToLevel()))
				.replace("<^PLAYER.EXP>", String.valueOf(thePlayer.getTotalExperience()))
				.replace("<^PLAYER.FOOD_LEVEL>", String.valueOf(thePlayer.getFoodLevel()));

			else 
				filledString = filledString
				.replace("<PLAYER.ITEM_IN_HAND.MATERIAL>", itemInHandMaterial)
				.replace("<PLAYER.ITEM_IN_HAND.QTY>", itemInHandQty)
				.replace("<PLAYER.ITEM_IN_HAND.ID>", itemInHandId)
				.replace("<PLAYER.NAME>", thePlayer.getName())
				.replace("<PLAYER>", thePlayer.getName())
				.replace("<PLAYER.KILLER>", playerKiller)
				.replace("<PLAYER.HEALTH>", String.valueOf(thePlayer.getHealth()))
				.replace("<PLAYER.HELM>", playerHelm)
				.replace("<PLAYER.LEGGINGS>", playerLeggings)
				.replace("<PLAYER.BOOTS>", playerBoots)
				.replace("<PLAYER.CHESTPLATE>", playerChestplate)
				.replace("<PLAYER.WORLD>", thePlayer.getWorld().getName())
				.replace("<PLAYER.MONEY>", playerMoney)
				.replace("<PLAYER.EXP_TO_NEXT_LEVEL>", String.valueOf(thePlayer.getExpToLevel()))
				.replace("<PLAYER.EXP>", String.valueOf(thePlayer.getTotalExperience()))
				.replace("<PLAYER.FOOD_LEVEL>", String.valueOf(thePlayer.getFoodLevel()));

		}

		// Replaceables for Denizen
		if (theDenizen != null && filledString.contains("<")) {
			if (quickReplaceable) 
				filledString = filledString
				.replace("<^DENIZEN.NPCID>", String.valueOf(theDenizen.getId()))
				.replace("<^NPCID>", String.valueOf(theDenizen.getId()))
				.replace("<^NPC>", theDenizen.getName())
				.replace("<^DENIZEN.NAME>", theDenizen.getName());

			else
				filledString = filledString
				.replace("<DENIZEN.NPCID>", String.valueOf(theDenizen.getId()))
				.replace("<NPCID>", String.valueOf(theDenizen.getId()))
				.replace("<NPC>", theDenizen.getName())
				.replace("<DENIZEN.NAME>", theDenizen.getName());
		}

		// Done!
		return filledString;
	}


}
