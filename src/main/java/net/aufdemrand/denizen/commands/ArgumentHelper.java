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

	// -----------------

	final public Pattern replaceableFlagWithFallback = Pattern.compile("(<)(FLAG|flag|Flag)(:)(.*?)(:)(.*?)(>)");
	final public Pattern replaceableFlag = Pattern.compile("(<)(FLAG|flag|Flag)(:)(.*?)(>)");
	public String fillFlags(Player thePlayer, String stringToFill) {

		if (stringToFill == null) return null;
		String filledString = stringToFill;
		Matcher fF = replaceableFlagWithFallback.matcher(stringToFill);
		String searchString = "Global";
		if (thePlayer != null) searchString = "Players." + thePlayer.getName();

		while (fF.find()) {
			if (plugin.getSaves().contains(searchString + ".Flags." + fF.group(4).toUpperCase())) {
				filledString = fF.replaceFirst(plugin.getSaves().getString(searchString + ".Flags." + fF.group(4).toUpperCase()));
				echoDebug(ChatColor.YELLOW + "//REPLACED//" + ChatColor.WHITE + " '%s' with flag value.", fF.group(4));
			}
			else if (plugin.getSaves().contains("Global.Flags." + fF.group(4).toUpperCase())) {
				filledString = fF.replaceFirst(plugin.getSaves().getString("Global.Flags." + fF.group(4).toUpperCase()));
				echoDebug(ChatColor.YELLOW + "//REPLACED//" + ChatColor.WHITE + " '%s' with flag value.", fF.group(4));
			}
			// No flags found, use fallback text
			else {
				filledString = fF.replaceFirst(fF.group(6));
				echoDebug(ChatColor.YELLOW + "//REPLACED//"  +ChatColor.WHITE + " '%s' with fallback value.", fF.group(4));
			}
		}

		stringToFill = filledString;

		// okay, now check for without fallback.
		Matcher f = replaceableFlag.matcher(stringToFill);

		while (f.find()) {
			if (plugin.getSaves().contains(searchString + ".Flags." + f.group(4).toUpperCase())) {
				filledString = f.replaceFirst(plugin.getSaves().getString(searchString + ".Flags." + f.group(4).toUpperCase()));
				echoDebug(ChatColor.YELLOW + "//REPLACED//" + ChatColor.WHITE + " '%s' with flag value.", f.group(4));
			}
			else if (plugin.getSaves().contains("Global.Flags." + f.group(4).toUpperCase())){
				filledString = f.replaceFirst(plugin.getSaves().getString("Global.Flags." + f.group(4).toUpperCase()));
				echoDebug(ChatColor.YELLOW + "//REPLACED//" + ChatColor.WHITE + " '%s' with flag value.", f.group(4));
			}
			// No flags found, use ""
			else  {
				filledString = f.replaceFirst("");
				echoDebug(ChatColor.YELLOW + "//REPLACED//" + ChatColor.WHITE + " '%s' with nothing, no flag value found.", f.group(4));
			}
		}

		return filledString;
	}

	
	
	final public Pattern replaceableBuildFlagWithFallback = Pattern.compile("(<)(\\^)(FLAG|flag|Flag)(:)(.*?)(:)(.*?)(>)");
	final public Pattern replaceableBuildFlag = Pattern.compile("(<)(\\^)(FLAG|flag|Flag)(:)(.*?)(>)");
	public String fillBuildFlags(Player thePlayer, String stringToFill) {

		if (stringToFill == null) return null;
		String filledString = stringToFill;
		Matcher fF = replaceableBuildFlagWithFallback.matcher(stringToFill);
		String searchString = "Global";
		if (thePlayer != null) searchString = "Players." + thePlayer.getName();

		while (fF.find()) {
			if (plugin.getSaves().contains(searchString + ".Flags." + fF.group(5).toUpperCase())) {
				filledString = fF.replaceFirst(plugin.getSaves().getString(searchString + ".Flags." + fF.group(5).toUpperCase()));
				echoDebug(ChatColor.YELLOW + "//REPLACED//" + ChatColor.WHITE + " '%s' with flag value.", fF.group(5));
			}
			else if (plugin.getSaves().contains("Global.Flags." + fF.group(5).toUpperCase())) {
				filledString = fF.replaceFirst(plugin.getSaves().getString("Global.Flags." + fF.group(5).toUpperCase()));
				echoDebug(ChatColor.YELLOW + "//REPLACED//" + ChatColor.WHITE + " '%s' with flag value.", fF.group(5));
			}
			// No flags found, use fallback text
			else {
				filledString = fF.replaceFirst(fF.group(7));
				echoDebug(ChatColor.YELLOW + "//REPLACED//" + ChatColor.WHITE + " '%s' with fallback value.", fF.group(5));
			}
		}

		stringToFill = filledString;

		// okay, now check for without fallback.
		Matcher f = replaceableBuildFlag.matcher(stringToFill);

		while (f.find()) {
			if (plugin.getSaves().contains(searchString + ".Flags." + f.group(5).toUpperCase())) {
				filledString = f.replaceFirst(plugin.getSaves().getString(searchString + ".Flags." + f.group(5).toUpperCase()));
				echoDebug(ChatColor.YELLOW + "//REPLACED//" + ChatColor.WHITE + " '%s' with flag value.", f.group(5));
			}
			else if (plugin.getSaves().contains("Global.Flags." + f.group(5).toUpperCase())){
				filledString = f.replaceFirst(plugin.getSaves().getString("Global.Flags." + f.group(5).toUpperCase()));
				echoDebug(ChatColor.YELLOW + "//REPLACED//" + ChatColor.WHITE + " '%s' with flag value.", f.group(5));
			}
			// No flags found, use ""
			else  {
				filledString = f.replaceFirst("");
				echoDebug(ChatColor.YELLOW + "//REPLACED//" + ChatColor.WHITE + " '%s' with nothing, no flag value found.", f.group(5));
			}
		}

		return filledString;
	}





}
