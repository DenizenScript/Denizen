package net.aufdemrand.denizen;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import net.aufdemrand.denizen.ScriptEngine;
import net.aufdemrand.denizen.utilities.GetDenizen;
import net.aufdemrand.denizen.utilities.GetPlayer;
import net.aufdemrand.denizen.utilities.GetRequirements;
import net.aufdemrand.denizen.utilities.GetScript;
import net.aufdemrand.denizen.utilities.GetWorld;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.character.CharacterFactory;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class Denizen extends JavaPlugin {

	public static Map<Player, List<String>> playerQue = new ConcurrentHashMap<Player, List<String>>();
	public static Map<NPC, Location>   previousNPCLoc = new ConcurrentHashMap<NPC, Location>(); 
	public static Map<Player, Long>  interactCooldown = new ConcurrentHashMap<Player, Long>();
	public static Map<Player, String>  proximityCheck = new ConcurrentHashMap<Player, String>();
	public static Boolean                   DebugMode = false;

	public static ScriptEngine       scriptEngine = new ScriptEngine();
	public static CommandExecuter commandExecuter = new CommandExecuter();
	public static DenizenCharacter   getCharacter = new DenizenCharacter();
	public static GetScript             getScript = new GetScript();
	public static GetDenizen           getDenizen = new GetDenizen();
	public static GetRequirements getRequirements = new GetRequirements();
	public static GetPlayer             getPlayer = new GetPlayer();
	public static GetWorld               getWorld = new GetWorld();
	public static Settings               settings = new Settings();

	public static Economy             denizenEcon = null;
	public static Permission         denizenPerms = null;



	/*
	 * onCommand
	 * 
	 * Handles incoming bukkit console commands.
	 * 
	 */

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {

		if (args.length < 1) {
			sender.sendMessage(ChatColor.RED + "Use /denizen help for command reference.");
			return true;
		}

		/*
		 * Commands for use with the console
		 */

		if (args[0].equalsIgnoreCase("save") && !(sender instanceof Player)) {
			saveAssignments();
			getServer().broadcastMessage("denizens.yml saved.");
			return true;
		}

		if (args[0].equalsIgnoreCase("reload") && !(sender instanceof Player)) {
			reloadConfig();
			reloadScripts();
			reloadAssignments();
			getServer().broadcastMessage("Denizens config.yml and scripts.yml reloaded.");
			return true;
		}


		/*
		 * Commands used by the Player
		 */

		if (!(sender instanceof Player)) {
			sender.sendMessage("You must be in-game to execute commands.");
			return true;
		}

		Player player = (Player) sender;

		if (args[0].equalsIgnoreCase("combine")) {

			try {
				getScript.ConcatenateScripts();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return true;

		}

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
				player.sendMessage(ChatColor.RED + "NOTE! Help system is fleshed out only. Most of these");
				player.sendMessage(ChatColor.RED + "commands DO NOT YET WORK! ");
				player.sendMessage(ChatColor.GOLD + "For a cheat sheet of commands and arguments,");
				player.sendMessage(ChatColor.GOLD + "visit the wiki: http://wiki.citizensnpcs.net/Denizen");   }

			else if (args[1].equalsIgnoreCase("core")) {

				player.sendMessage(ChatColor.GOLD + "------- Denizen Core Commands -------");
				player.sendMessage(ChatColor.GOLD + "");
				player.sendMessage(ChatColor.GOLD + "/denizen RELOAD");
				player.sendMessage(ChatColor.GOLD + "  Reloads config.yml, scripts.yml and saves.yml");
				player.sendMessage(ChatColor.GOLD + "/denizen SAVE");
				player.sendMessage(ChatColor.GOLD + "  Saves to disk config.yml and saves.yml");
				player.sendMessage(ChatColor.GOLD + "/denizen VERSION");
				player.sendMessage(ChatColor.GOLD + "  Displays version and build of Denizen plugin");
				player.sendMessage(ChatColor.GOLD + "/denizen DEBUG TRUE|FALSE");
				player.sendMessage(ChatColor.GOLD + "  Logs debugging information for reporting problems");
				player.sendMessage(ChatColor.GOLD + "/denizen STATS");
				player.sendMessage(ChatColor.GOLD + "  Shows statistical information from Denizens plugin");   
				player.sendMessage(ChatColor.GOLD + "/denizen SCHEDULE");
				player.sendMessage(ChatColor.GOLD + "  Forces the Denizens to check their schedules");   }

			else if (args[1].equalsIgnoreCase("npc")) {

				player.sendMessage(ChatColor.GOLD + "------- Denizen NPC Commands -------");
				player.sendMessage(ChatColor.GOLD + "");
				player.sendMessage(ChatColor.GOLD + "/denizen INFO");
				player.sendMessage(ChatColor.GOLD + "  Shows the config nodes for the Denizen NPC");
				player.sendMessage(ChatColor.GOLD + "/denizen ASSIGN [PRIORITY] [SCRIPT NAME]");
				player.sendMessage(ChatColor.GOLD + "  Assigns a script and priority for the Denizen NPC");
				player.sendMessage(ChatColor.GOLD + "/denizen UNASSIGN [SCRIPT NAME]");
				player.sendMessage(ChatColor.GOLD + "  Unassigns a script from the Denizen NPC");
				player.sendMessage(ChatColor.GOLD + "/denizen BOOKMARK LOCATION|BLOCK");
				player.sendMessage(ChatColor.GOLD + "  Set bookmarks the Denizens. Use /denizen HELP BOOKMARK");
				player.sendMessage(ChatColor.GOLD + "/denizen OPTION (LIST)|[OPTION] [VALUE]");
				player.sendMessage(ChatColor.GOLD + "  Set various flags for Denizens. Use /denizen HELP OPTION");
				player.sendMessage(ChatColor.GOLD + "/denizen TPHERE");
				player.sendMessage(ChatColor.GOLD + "  Teleports the selected Denizen to where you are standing");
				player.sendMessage(ChatColor.GOLD + "/denizen SPAWN|DESPAWN");
				player.sendMessage(ChatColor.GOLD + "  Spawn or despawns the Denizen");
				player.sendMessage(ChatColor.GOLD + "/denizen MEMORY");
				player.sendMessage(ChatColor.GOLD + "  Shows the stored memory of the Denizen NPC");
				player.sendMessage(ChatColor.GOLD + "/denizen REMEMBER|FORGET [NAME] [VALUE]");
				player.sendMessage(ChatColor.GOLD + "  Sets or removes a memory from the Denizn NPC");  }

			else if (args[1].equalsIgnoreCase("bookmark")) {

				player.sendMessage(ChatColor.GOLD + "------- Denizen Bookmark Commands -------");
				player.sendMessage(ChatColor.GOLD + "");
				player.sendMessage(ChatColor.GOLD + "/denizen BOOKMARK LOCATION [Location Name]");
				player.sendMessage(ChatColor.GOLD + "  Saves the location you are in to the Denizen for reference");
				player.sendMessage(ChatColor.GOLD + "  with Script commands such as MOVETO, SPAWN and REMEMBER");
				player.sendMessage(ChatColor.GOLD + "/denizen BOOKMARK BLOCK [Block Name]");
				player.sendMessage(ChatColor.GOLD + "  Sets a bookmark for the block that is in your crosshairs");
				player.sendMessage(ChatColor.GOLD + "  to be referenced to with Script commands such as ACTIVATE,");
				player.sendMessage(ChatColor.GOLD + "  DEACTIVATE, and CHANGE");   }

			else if (args[1].equalsIgnoreCase("option")) {

				player.sendMessage(ChatColor.GOLD + "------- Denizen Option Commands -------");
				player.sendMessage(ChatColor.GOLD + "");
				player.sendMessage(ChatColor.GOLD + "/denizen OPTION LIST");
				player.sendMessage(ChatColor.GOLD + "  Lists Denizen NPC config nodes and values");
				player.sendMessage(ChatColor.GOLD + "");
				player.sendMessage(ChatColor.GOLD + "/denizen OPTION [OPTION] [VALUE]");
				player.sendMessage(ChatColor.GOLD + "  Sets the value of a Denizen NPC option node. Also saves");
				player.sendMessage(ChatColor.GOLD + "  the saves.yml to disk. ie. /denizen option wander true");
				player.sendMessage(ChatColor.GOLD + "");
				player.sendMessage(ChatColor.GOLD + "Some option nodes may require a restart of your server.");
				player.sendMessage(ChatColor.GOLD + "Unless tested, /restarts are typically not recommended.");   }

			return true;
		} 

		if (args[0].equalsIgnoreCase("debug")) {

			if (!Denizen.DebugMode) { 
				DebugMode = true; 
				player.sendMessage(ChatColor.GREEN + "Denizen DEBUG logging mode ON.");   // Talk to the player.
				return true;
			}

			else if (Denizen.DebugMode) { 
				DebugMode = false; 
				player.sendMessage(ChatColor.GREEN + "Denizen DEBUG logging mode OFF.");   // Talk to the player.
				return true;
			}
		}

		if (args[0].equalsIgnoreCase("save")) {
			saveAssignments();
			player.sendMessage(ChatColor.GREEN + "denizens.yml saved.");
			return true;
		}

		if (args[0].equalsIgnoreCase("reload")) {
			reloadAssignments();
			reloadConfig();
			reloadScripts();
			player.sendMessage(ChatColor.GREEN + "config.yml, denizens.yml and scripts.yml reloaded.");
			return true;
		}

		if (args[0].equalsIgnoreCase("schedule")) {
			scriptEngine.scheduleScripts();
			player.sendMessage("Denizen scheduler invoked.");
			return true;
		}

		if (player.getMetadata("selected").isEmpty()) { 
			player.sendMessage(ChatColor.RED + "You must have a Denizen selected.");
			return true;
		}

		NPC ThisNPC = CitizensAPI.getNPCRegistry().getNPC(player.getMetadata("selected").get(0).asInt());      // Gets NPC Selected

		if (ThisNPC.getCharacter() == null || !ThisNPC.getCharacter().getName().equals("denizen")) {
			player.sendMessage(ChatColor.RED + "That command must be performed on a denizen!");
			return true;
		}

		/*
		 * These craftbukkit commands require a Denizen to be selected.
		 */

		if (args[0].equalsIgnoreCase("bookmark")) {
			if(args.length < 3) {
				player.sendMessage(ChatColor.GOLD + "Invalid use.  Use /denizen help bookmark");
				return true;
			}
			else if (args[1].equalsIgnoreCase("location")) {
				List<String> locationList = getAssignments().getStringList("Denizens." + ThisNPC.getName() + ".Bookmarks.Location");
				locationList.add(args[2] + " " + player.getWorld().getName() + ";" + player.getLocation().getX() + ";" +
						player.getLocation().getY() + ";" + player.getLocation().getZ() + ";" + player.getLocation().getYaw() + ";" + player.getLocation().getPitch());
				getAssignments().set("Denizens." + ThisNPC.getName() + ".Bookmarks.Location", locationList);				
				saveAssignments();
				player.sendMessage(ChatColor.GOLD + "Location bookmark added. Your denizen can now reference this location.");
				return true;
			}

			else if (args[1].equalsIgnoreCase("block")) {
				List<String> blockList = getAssignments().getStringList("Denizens." + ThisNPC.getName() + ".Bookmarks.Block");
				Block targetBlock = player.getTargetBlock(null, 6);
				blockList.add(args[2] + " " + player.getWorld().getName() + ";" + targetBlock.getX() + ";" +
						targetBlock.getY() + ";" + targetBlock.getZ());

				getAssignments().set("Denizens." + ThisNPC.getName() + ".Bookmarks.Block", blockList);				
				saveAssignments();
				player.sendMessage(ChatColor.GOLD + "Block bookmark added. Your denizen can now reference this block.");
				return true;
			}
		}
		return true;
	}



	/*
	 * onEnable
	 * 
	 * Sets up Denizen on start of the craftbukkit server.
	 *	
	 */

	@Override
	public void onEnable() {

		if (!setupEconomy()) {
			getLogger().log(Level.SEVERE, String.format("[%s] - Disabled due to no Vault-compatible Economy Plugin found! Install an economy system!", getDescription().getName()));
			getServer().getPluginManager().disablePlugin(this);
			return; 
		}

		/* Set up Vault for Permissions */
		setupPermissions();

		reloadConfig();
		reloadScripts();
		reloadAssignments();
		// getConfig().options().copyDefaults(true);

		CitizensAPI.getCharacterManager().registerCharacter(new CharacterFactory(DenizenCharacter.class).withName("denizen"));
		getServer().getPluginManager().registerEvents(new DenizenCharacter(), this);

		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			@Override
			public void run() { scriptEngine.commandQue(); }
		}, settings.InteractDelayInTicks(), settings.InteractDelayInTicks());

		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			@Override
			public void run() { scriptEngine.scheduleScripts(); }
		}, 1, 1000);

	}




	/*
	 * onDisable
	 * 
	 * Unloads Denizen on shutdown of the craftbukkit server.
	 *	
	 */

	@Override
	public void onDisable() {
		getLogger().log(Level.INFO, " v" + getDescription().getVersion() + " disabled.");
		Bukkit.getServer().getScheduler().cancelTasks(this);
		saveAssignments();
	}




	/*
	 * setupEconomy/setupPermissions
	 * 
	 * Sets up Economy/Permissions object with Vault.
	 *	
	 */

	private boolean setupEconomy() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) return false;
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) return false;
		denizenEcon = rsp.getProvider();
		return denizenEcon != null;
	}

	private boolean setupPermissions() {
		RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
		denizenPerms = rsp.getProvider();
		return denizenPerms != null;
	}




	/*
	 * reloadScripts/getScripts
	 * 
	 * Reloads and retrieves information from the Denizen/scripts.yml.
	 * 
	 */

	private FileConfiguration scriptConfig = null;
	private File scriptConfigFile = null;

	public void reloadScripts() {

		try {
			getScript.ConcatenateScripts();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (scriptConfigFile == null) {
			scriptConfigFile = new File(getDataFolder(), "read-only-scripts.yml");
		}
		scriptConfig = YamlConfiguration.loadConfiguration(scriptConfigFile);

		// Look for defaults in the jar
		InputStream defConfigStream = getResource("read-only-scripts.yml");
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			scriptConfig.setDefaults(defConfig);
		}
	}


	public FileConfiguration getScripts() {
		if (scriptConfig == null) {
			reloadScripts();
		}
		return scriptConfig;
	}

	
	
	
	/*
	 * reloadAssignments/getAssignments/saveAssignments
	 * 
	 * Reloads, retrieves and saves information from the Denizen/denizens.yml.
	 * 
	 */
	
	private FileConfiguration assignmentConfig = null;
	private File assignmentConfigFile = null;

	public void reloadAssignments() {
	    if (assignmentConfigFile == null) {
	    assignmentConfigFile = new File(getDataFolder(), "assignments.yml");
	    }
	    assignmentConfig = YamlConfiguration.loadConfiguration(assignmentConfigFile);
	 
	    // Look for defaults in the jar
	    InputStream defConfigStream = getResource("assignments.yml");
	    if (defConfigStream != null) {
	        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
	        assignmentConfig.setDefaults(defConfig);
	    }
	}

	public FileConfiguration getAssignments() {
	    if (assignmentConfig == null) {
	        reloadAssignments();
	    }
	    return assignmentConfig;
	}

	public void saveAssignments() {
	    if (assignmentConfig == null || assignmentConfigFile == null) {
	    return;
	    }
	    try {
	        assignmentConfig.save(assignmentConfigFile);
	    } catch (IOException ex) {
	        Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE, "Could not save config to " + assignmentConfigFile, ex);
	    }
	}
	
	

}