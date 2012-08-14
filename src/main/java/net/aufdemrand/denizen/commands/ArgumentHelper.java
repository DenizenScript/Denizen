package net.aufdemrand.denizen.commands;

import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.bookmarks.BookmarkHelper.BookmarkType;
import net.aufdemrand.denizen.npc.DenizenNPC;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.command.exception.CommandException;

public class ArgumentHelper {

	Denizen plugin;

	ArgumentHelper(Denizen plugin) {
		this.plugin = plugin;
	}


	public void echoDebug(String message, String argument) {
		if (plugin.debugMode)
			plugin.getLogger().info(String.format(message, argument));
	}

	public void echoError(String message) {
		plugin.getLogger().log(Level.WARNING, message);
	}

	

	/* ----------------------------- */

	public String getStringModifier(String argument) {
		if (argument.split(":").length >= 2)
			return argument.split(":")[1];
		else return argument;
	}

	public Integer getIntegerModifier(String argument) {
		if (argument.split(":").length >= 2)
			return Integer.valueOf(argument.split(":")[1]);
		else return Integer.valueOf(argument);
	}

	public Location getBookmarkModifier(String thisArg, DenizenNPC denizenNPC) {
		Matcher m = bookmarkArgument.matcher(thisArg);
		Matcher m2 = bookmarkArgument2.matcher(thisArg);

		if (m.matches()	&& plugin.bookmarks.exists(thisArg.split(":")[1], thisArg.split(":")[2])) 
			return plugin.bookmarks.get(thisArg.split(":")[1], thisArg.split(":")[2], BookmarkType.LOCATION);

		else if (m2.matches() &&	plugin.bookmarks.exists(denizenNPC, thisArg.split(":")[1]))
			return plugin.bookmarks.get(denizenNPC, thisArg, BookmarkType.LOCATION);

		if (plugin.debugMode) echoError("...bookmark not found!");
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
				theItem.setData(new MaterialData(Integer.valueOf(thisArg.split(":")[1])));
			}

			if (m3.matches()) {
				theItem = new ItemStack(Material.valueOf(thisArg.toUpperCase()));
			}

			if (m4.matches()) {
				theItem = new ItemStack(Material.valueOf(thisArg.split(":")[0].toUpperCase()));
				theItem.setData(new MaterialData(Integer.valueOf(thisArg.split(":")[1])));
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

	final public Pattern integerArgument = Pattern.compile("\\d+");
	public boolean matchesInteger(String regex) {
		Matcher m = integerArgument.matcher(regex);
		return m.matches();
	}

	final public Pattern scriptArgument = Pattern.compile("(?:SCRIPT|script|Script)(:)(\\w+)");
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









}
