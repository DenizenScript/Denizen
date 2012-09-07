package net.aufdemrand.denizen;

import java.util.List;

import net.aufdemrand.denizen.npc.DenizenTrait;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHandler {

	Denizen plugin;

	public CommandHandler(Denizen plugin) {
		this.plugin = plugin;
	}

	public boolean onCommand(CommandSender sender, Command cmd,
			String cmdLabel, String[] args) {
		if (args.length < 1) {
			sender.sendMessage(ChatColor.RED + "Use /denizen help for command reference.");
			return true;
		}

		/*
		 * Commands for use with the console -- SAVE, RELOAD, VERSION, DEBUG, STACKTRACE
		 */

		if (args[0].equalsIgnoreCase("save") && !(sender instanceof Player)) {
			plugin.saveSaves();
			sender.sendMessage("Denizen/saves.yml saved.");
			return true;
		}

		if (args[0].equalsIgnoreCase("reload") && !(sender instanceof Player)) {
			plugin.reloadConfig();
			plugin.reloadScripts();
			plugin.reloadAssignments();
			plugin.reloadSaves();
			plugin.bookmarks.buildLocationTriggerList();
			plugin.getActivityEngine().scheduleScripts(true);
			sender.sendMessage(ChatColor.GREEN + "config.yml, saves.yml, assignments.yml, scripts reloaded, and activities reset.");			sender.sendMessage("Denizens/config.yml, scripts, and assignments.yml reloaded.");
			return true;
		}

		if (args[0].equalsIgnoreCase("version") && !(sender instanceof Player)) {
			sender.sendMessage(ChatColor.GREEN + plugin.utilities.getVersionString());
			return true;
		}

		if (args[0].equalsIgnoreCase("debug") && !(sender instanceof Player)) {

			if (!plugin.debugMode) { 
				plugin.debugMode = true; 
				sender.sendMessage(ChatColor.GREEN + "Denizen DEBUG logging mode ON."); 
				return true;
			}

			else if (plugin.debugMode) { 
				plugin.debugMode = false; 
				sender.sendMessage(ChatColor.GREEN + "Denizen DEBUG logging mode OFF."); 
				return true;
			}
		}

		if (args[0].equalsIgnoreCase("stacktrace") && !(sender instanceof Player)) {

			if (!plugin.showStackTraces) { 
				plugin.showStackTraces = true; 
				sender.sendMessage(ChatColor.GREEN + "Denizen DEBUG logging mode will show stacktraces."); 
				return true;
			}

			else if (plugin.showStackTraces) { 
				plugin.showStackTraces = false; 
				sender.sendMessage(ChatColor.GREEN + "Denizen DEBUG logging mode will NOT show stacktraces."); 
				return true;
			}
		}
		
		if (args[0].equalsIgnoreCase("testbitch")) {
			sender.sendMessage("TEST!");
		}



		/*
		 * Commands used by the Player
		 */

		if (!(sender instanceof Player)) {
			sender.sendMessage("You must be in-game to execute commands.");
			return true;
		}

		Player player = (Player) sender;

		/*
		 *  /denizen getdata|adddata|decdata shows/modifies the block data for block in targets.
		 */

		if (args[0].equalsIgnoreCase("getdata")) {
			player.sendMessage("Current block data: " + player.getTargetBlock(null, 20).getData());
			return true;
		}
		if (args[0].equalsIgnoreCase("adddata")) {
			Block toAddData = player.getTargetBlock(null, 20);
			toAddData.setData((byte) (toAddData.getData() + 1));
			player.sendMessage("Current block data: " + player.getTargetBlock(null, 20).getData());
			return true;	
		}
		if (args[0].equalsIgnoreCase("decdata")) {
			Block toAddData = player.getTargetBlock(null, 20);
			toAddData.setData((byte) (toAddData.getData() - 1));
			player.sendMessage("Current block data: " + player.getTargetBlock(null, 20).getData());
			return true;
		}

		// Help commands

		if (args[0].equalsIgnoreCase("help")) {

			if(args.length == 1) {

				player.sendMessage(ChatColor.GOLD + "------- Denizen Commands -------");
				player.sendMessage(ChatColor.GOLD + "");
				player.sendMessage(ChatColor.GOLD + "Denizen Core Commands:");
				player.sendMessage(ChatColor.GOLD + "use /denizen HELP CORE");
				player.sendMessage(ChatColor.GOLD + "");
				player.sendMessage(ChatColor.GOLD + "Denizen NPC Commands:");
				player.sendMessage(ChatColor.GOLD + "use /denizen HELP NPC ");
				player.sendMessage(ChatColor.GOLD + "");
				player.sendMessage(ChatColor.GOLD + "For a cheat sheet of commands and arguments,");
				player.sendMessage(ChatColor.GOLD + "visit the wiki: http://wiki.citizensnpcs.net/Denizen");   
			}

			else if (args[1].equalsIgnoreCase("core")) {

				player.sendMessage(ChatColor.GOLD + "------- Denizen Core Commands -------");
				player.sendMessage(ChatColor.GOLD + "");
				player.sendMessage(ChatColor.GOLD + "/denizen RELOAD");
				player.sendMessage(ChatColor.GOLD + "  Reloads config.yml, scripts.yml and saves.yml");
				player.sendMessage(ChatColor.GOLD + "/denizen SAVE");
				player.sendMessage(ChatColor.GOLD + "  Saves to disk config.yml and saves.yml");
				player.sendMessage(ChatColor.GOLD + "/denizen VERSION");
				player.sendMessage(ChatColor.GOLD + "  Displays version and build of Denizen plugin");
				player.sendMessage(ChatColor.GOLD + "/denizen DEBUG");
				player.sendMessage(ChatColor.GOLD + "  Logs debugging information for reporting problems");
				player.sendMessage(ChatColor.GOLD + "/denizen SCHEDULE");
				player.sendMessage(ChatColor.GOLD + "  Forces the Denizens to check their schedules");   
			}

			else if (args[1].equalsIgnoreCase("npc")) {

				player.sendMessage(ChatColor.GOLD + "------- Denizen NPC Commands -------");
				player.sendMessage(ChatColor.GOLD + "");
				player.sendMessage(ChatColor.GOLD + "/denizen INFO");
				player.sendMessage(ChatColor.GOLD + "  Shows the config nodes for the Denizen NPC");
				player.sendMessage(ChatColor.GOLD + "/denizen ASSIGN [PRIORITY] [SCRIPT NAME]");
				player.sendMessage(ChatColor.GOLD + "  Assigns a script and priority for the Denizen NPC");
				player.sendMessage(ChatColor.GOLD + "/denizen UNASSIGN [SCRIPT NAME]");
				player.sendMessage(ChatColor.GOLD + "  Unassigns a script from the Denizen NPC");
				player.sendMessage(ChatColor.GOLD + "/denizen TRIGGER TOGGLE|LIST [TRIGGER NAME]");
				player.sendMessage(ChatColor.GOLD + "  Toggles triggers for a Denizen NPC");
				player.sendMessage(ChatColor.GOLD + "/denizen BOOKMARK LOCATION|BLOCK [Name]");
				player.sendMessage(ChatColor.GOLD + "  Set bookmarks the Denizens. Use /denizen HELP BOOKMARK");
				player.sendMessage(ChatColor.GOLD + "/denizen SCHEDULE");
				player.sendMessage(ChatColor.GOLD + "  Clears current Activity and forces a schedule check");   }

			else if (args[1].equalsIgnoreCase("bookmark")) {

				player.sendMessage(ChatColor.GOLD + "------- Denizen Bookmark Commands -------");
				player.sendMessage(ChatColor.GOLD + "");
				player.sendMessage(ChatColor.GOLD + "/denizen BOOKMARK LOCATION [Location Name]");
				player.sendMessage(ChatColor.GOLD + "  Saves the location you are in to the Denizen for reference");
				player.sendMessage(ChatColor.GOLD + "  with Script commands such as MOVETO, SPAWN and REMEMBER");
				player.sendMessage(ChatColor.GOLD + "/denizen BOOKMARK BLOCK [Block Name]");
				player.sendMessage(ChatColor.GOLD + "  Sets a bookmark for the block that is in your crosshairs");
				player.sendMessage(ChatColor.GOLD + "  to be referenced to with Script commands such as SWITCH,");
				player.sendMessage(ChatColor.GOLD + "  and CHANGE");   
			}

			return true;
		} 

		if (args[0].equalsIgnoreCase("debug")) {

			if (!plugin.debugMode) { 
				plugin.debugMode = true; 
				player.sendMessage(ChatColor.GREEN + "Denizen DEBUG logging mode ON.");   // Talk to the player.
				return true;
			}

			else if (plugin.debugMode) { 
				plugin.debugMode = false; 
				player.sendMessage(ChatColor.GREEN + "Denizen DEBUG logging mode OFF.");   // Talk to the player.
				return true;
			}
		}
		
		
		

		if (args[0].equalsIgnoreCase("stacktrace"))  {

			if (!plugin.showStackTraces) { 
				plugin.showStackTraces = true; 
				sender.sendMessage(ChatColor.GREEN + "Denizen DEBUG logging mode will show stacktraces."); 
				return true;
			}

			else if (plugin.showStackTraces) { 
				plugin.showStackTraces = false; 
				sender.sendMessage(ChatColor.GREEN + "Denizen DEBUG logging mode will NOT show stacktraces."); 
				return true;
			}
		}
		
		if (args[0].equalsIgnoreCase("save")) {
			plugin.saveSaves();
			player.sendMessage(ChatColor.GREEN + "denizens.yml saved.");
			return true;
		}

		if (args[0].equalsIgnoreCase("reload")) {
			plugin.reloadSaves();
			plugin.reloadConfig();
			plugin.reloadScripts();
			plugin.reloadAssignments();
			plugin.bookmarks.buildLocationTriggerList();
			plugin.getActivityEngine().scheduleScripts(true);
			player.sendMessage(ChatColor.GREEN + "config.yml, saves.yml, assignments.yml, scripts reloaded, and activities reset.");
			return true;
		}
		
		if (args[0].equalsIgnoreCase("version")) {
			player.sendMessage(ChatColor.GREEN + plugin.utilities.getVersionString());
			return true;
		}

		if (player.getMetadata("selected").isEmpty()) { 
			player.sendMessage(ChatColor.RED + "You must have a Denizen selected.");
			return true;
		}

		/* Gets the C2NPC that is selected */
		NPC theNPC = CitizensAPI.getNPCRegistry().getById(player.getMetadata("selected").get(0).asInt());

		if (!theNPC.hasTrait(DenizenTrait.class)) {
			player.sendMessage(ChatColor.RED + "That command must be performed on a denizen!");
			return true;
		}

		if (args[0].equalsIgnoreCase("info")) {
			plugin.getDenizenNPCRegistry().showInfo(player, plugin.getDenizenNPCRegistry().getDenizen(theNPC));
			return true;
		}
		
		if (args[0].equalsIgnoreCase("test")) {
			plugin.getSaves().set("Denizens." + theNPC.getName() + ".Interact Scripts", "TEST");
			plugin.saveSaves();
			return true;
		}
					
		if (args[0].equalsIgnoreCase("reschedule")) {
			plugin.getSaves().set("Denizen." + theNPC.getName() + ".Active Activity Script", null);
			plugin.getActivityEngine().scheduleScripts(false);
			plugin.getSaves().set("Denizens." + theNPC.getName() + "." + theNPC.getId() + ".Active Activity Script", null);
			player.sendMessage(ChatColor.GREEN + "Reset activities for " + theNPC.getName() + "/" + theNPC.getId() + " and rescheduled.");
			return true;
		}

		if (args[0].equalsIgnoreCase("sethealth")) {
			if(args.length < 2 || args.length > 2) {
				player.sendMessage(ChatColor.GOLD + "Invalid use. Use /denizen sethealth [#]");
				return true;
			} else {
				try { 
					plugin.getDenizenNPCRegistry().getDenizen(theNPC).setHealth(Integer.valueOf(args[1]));
					player.sendMessage(ChatColor.GREEN + "Health set.");
				} catch (NumberFormatException e) {
					player.sendMessage(ChatColor.GOLD + "Argument must be a number. Use /denizen sethealth [#]");
				}
			}
		}

		if (args[0].equalsIgnoreCase("trigger")) {
			if (args[1].equalsIgnoreCase("list")) {
				player.sendMessage(ChatColor.GOLD + "Trigger list and status:");
				player.sendMessage(theNPC.getTrait(DenizenTrait.class).listTriggers());
				return true;
			}
			if (args[1].equalsIgnoreCase("toggle")) {
				if (args.length > 2) {
					player.sendMessage(theNPC.getTrait(DenizenTrait.class).toggleTrigger(args[2]));
					return true;
				}
				else {
					player.sendMessage(ChatColor.GOLD + "Must specify a Trigger to toggle. Use /denizen trigger list to see what triggers are available.");
				}
			}
		}

		if (args[0].equalsIgnoreCase("activity")) {
			if(args.length < 3) {
				player.sendMessage(ChatColor.GOLD + "Invalid use.  Use /denizen help activity");
				return true;
			}
			else if (args[1].equalsIgnoreCase("add")) {
				if (args.length < 4) {
					player.sendMessage(ChatColor.GOLD + "Invalid use.  Use /denizen help activity");
					return true;
				}

				else plugin.getActivityRegistry().addActivity(args[2], plugin.getDenizenNPCRegistry().getDenizen(theNPC), new String[0], Integer.valueOf(args[3]));

			}

			else if (args[1].equalsIgnoreCase("remove")) {
				plugin.getActivityRegistry().removeActivity(args[2], theNPC);
			}
			return true;
		}

		if (args[0].equalsIgnoreCase("bookmark")) {
			if(args.length < 3) {
				player.sendMessage(ChatColor.GOLD + "Invalid use.  Use /denizen help bookmark");
				return true;
			}
			else if (args[1].equalsIgnoreCase("location")) {
				List<String> locationList = plugin.getSaves().getStringList("Denizens." + theNPC.getName() + ".Bookmarks.Location");
				locationList.add(args[2] + " " + player.getWorld().getName() + ";" + player.getLocation().getX() + ";" +
						player.getLocation().getY() + ";" + player.getLocation().getZ() + ";" + player.getLocation().getYaw() + ";" + player.getLocation().getPitch());
				plugin.getSaves().set("Denizens." + theNPC.getName() + ".Bookmarks.Location", locationList);				
				plugin.saveSaves();
				plugin.bookmarks.buildLocationTriggerList();
				player.sendMessage(ChatColor.GOLD + "Location bookmark added. Your denizen can now reference this location.");
				return true;
			}

			else if (args[1].equalsIgnoreCase("block")) {
				List<String> blockList = plugin.getSaves().getStringList("Denizens." + theNPC.getName() + ".Bookmarks.Block");
				Block targetBlock = player.getTargetBlock(null, 15);
				blockList.add(args[2] + " " + player.getWorld().getName() + ";" + targetBlock.getX() + ";" +
						targetBlock.getY() + ";" + targetBlock.getZ());

				plugin.getSaves().set("Denizens." + theNPC.getName() + ".Bookmarks.Block", blockList);				
				plugin.saveSaves();
				player.sendMessage(ChatColor.GOLD + "Block bookmark added. Your denizen can now reference this block.");
				return true;
			}
		}
		
		
		return false;	
	}

}
