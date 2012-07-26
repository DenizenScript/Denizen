package net.aufdemrand.denizen;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.aufdemrand.denizen.bookmarks.Bookmarks;
import net.aufdemrand.denizen.command.CommandRegistry;
import net.aufdemrand.denizen.command.Executer;
import net.aufdemrand.denizen.npc.DenizenNPCRegistry;
import net.aufdemrand.denizen.npc.DenizenTrait;
import net.aufdemrand.denizen.npc.SpeechEngine;
import net.aufdemrand.denizen.scriptEngine.ScriptEngine;
import net.aufdemrand.denizen.scriptEngine.TriggerRegistry;
import net.aufdemrand.denizen.utilities.GetDenizen;
import net.aufdemrand.denizen.utilities.GetPlayer;
import net.aufdemrand.denizen.utilities.GetRequirements;
import net.aufdemrand.denizen.utilities.GetWorld;
import net.aufdemrand.denizen.utilities.Utilities;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.TraitInfo;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class Denizen extends JavaPlugin {

	public Economy   economy = null;
	public Permission  perms = null;
	
	private CommandRegistry commandRegistry = new CommandRegistry(this);
	private TriggerRegistry triggerRegistry = new TriggerRegistry(this);
 // private RequirementRegistry requirementRegistry = new RequirementRegistry(this);
	private DenizenNPCRegistry denizenNPCRegistry = new DenizenNPCRegistry(this);

	private ScriptEngine scriptEngine = new ScriptEngine(this);
	private SpeechEngine speechEngine = new SpeechEngine(this);
	public Executer         executer = new Executer(this);
	
	public Bookmarks             bookmarks = new Bookmarks(this);
	public Utilities			 utilities = new Utilities(this);
	public Settings               settings = new Settings(this);
	
	public GetDenizen           getDenizen = new GetDenizen(this);
	public GetPlayer             getPlayer = new GetPlayer(this);
	public GetRequirements getRequirements = new GetRequirements(this);
	public GetWorld               getWorld = new GetWorld(this);
	
	public Boolean   debugMode = false;
	public Boolean preciseMode = false;
	public Boolean    newbMode = true;
	
	
	public DenizenNPCRegistry getDenizenNPCRegistry() {
		return denizenNPCRegistry;
	}
	
	public CommandRegistry getCommandRegistry() {
		return commandRegistry;
	}

	public TriggerRegistry getTriggerRegistry() {
		return triggerRegistry;
	}
	
	public SpeechEngine getSpeechEngine() {
		return speechEngine;
	}
	
	public ScriptEngine getScriptEngine() {
		return scriptEngine;
	}
	
	
	
	/*
	 * Sets up Denizen on start of the craftbukkit server.	
	 */

	@Override
	public void onEnable() {

		/* Set up Vault */
		if (!setupEconomy() || !setupPermissions())
			getLogger().log(Level.SEVERE, "No permissions an/or economy system found! Some commands may produce errors!");

		/* Load YAML files into memory */
		reloadConfig();
		reloadScripts();
		reloadSaves();
		reloadAssignments();

		/* Register Citizens2 trait, Denizen Modules, and Bukkit tasks */
		CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(DenizenTrait.class).withName("denizen"));
		commandRegistry.registerCoreCommands();
		triggerRegistry.registerCoreTriggers();
		
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			@Override public void run() { scriptEngine.runQueues(); }
		}, settings.InteractDelayInTicks(), settings.InteractDelayInTicks());

		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			@Override public void run() { scriptEngine.scheduleScripts(); }
		}, 1, 1000);

		getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			@Override public void run() { bookmarks.buildLocationTriggerList(); }
		}, 50);

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
		saveSaves();
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
		economy = rsp.getProvider();
		return economy != null;
	}

	private boolean setupPermissions() {
		RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
		perms = rsp.getProvider();
		return perms != null;
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
			scriptEngine.helper.ConcatenateScripts();
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
	 * reloadSaves/getSaves/saveSaves
	 * 
	 * Reloads, retrieves and saves progress information Denizen/saves.yml.
	 * 
	 */

	private FileConfiguration savesConfig = null;
	private File savesConfigFile = null;

	public void reloadSaves() {
		if (savesConfigFile == null) {
			savesConfigFile = new File(getDataFolder(), "saves.yml");
		}
		savesConfig = YamlConfiguration.loadConfiguration(savesConfigFile);

		// Look for defaults in the jar
		InputStream defConfigStream = getResource("saves.yml");
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			savesConfig.setDefaults(defConfig);
		}
	}

	public FileConfiguration getSaves() {
		if (savesConfig == null) {
			reloadSaves();
		}
		return savesConfig;
	}

	public void saveSaves() {
		if (savesConfig == null || savesConfigFile == null) {
			return;
		}
		try {
			savesConfig.save(savesConfigFile);
		} catch (IOException ex) {
			Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE, "Could not save config to " + savesConfigFile, ex);
		}
	}



	/*
	 * reloadAssignments/getAssignments/saveAssignments
	 * 
	 * Reloads, retrieves and saves information from the Denizen/assignments.yml.
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
		 * Commands for use with the console -- SAVE, RELOAD, VERSION
		 */

		if (args[0].equalsIgnoreCase("save") && !(sender instanceof Player)) {
			saveSaves();
			sender.sendMessage("Denizen/saves.yml saved.");
			return true;
		}

		if (args[0].equalsIgnoreCase("reload") && !(sender instanceof Player)) {
			reloadConfig();
			reloadScripts();
			reloadAssignments();
			reloadSaves();
			bookmarks.buildLocationTriggerList();
			sender.sendMessage("Denizens/config.yml, scripts, and assignments.yml reloaded.");
			return true;
		}

		if (args[0].equalsIgnoreCase("version") && !(sender instanceof Player)) {
			sender.sendMessage(ChatColor.GREEN + utilities.getVersionString());
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

			if (!debugMode) { 
				debugMode = true; 
				player.sendMessage(ChatColor.GREEN + "Denizen DEBUG logging mode ON.");   // Talk to the player.
				return true;
			}

			else if (debugMode) { 
				debugMode = false; 
				player.sendMessage(ChatColor.GREEN + "Denizen DEBUG logging mode OFF.");   // Talk to the player.
				return true;
			}
		}

		if (args[0].equalsIgnoreCase("save")) {
			saveSaves();
			player.sendMessage(ChatColor.GREEN + "denizens.yml saved.");
			return true;
		}

		if (args[0].equalsIgnoreCase("reload")) {
			reloadSaves();
			reloadConfig();
			reloadScripts();
			reloadAssignments();
			player.sendMessage(ChatColor.GREEN + "config.yml, assignemnts.yml and scripts reloaded.");
			return true;
		}

		if (args[0].equalsIgnoreCase("version")) {
			player.sendMessage(ChatColor.GREEN + utilities.getVersionString());
			return true;
		}

		if (player.getMetadata("selected").isEmpty()) { 
			player.sendMessage(ChatColor.RED + "You must have a Denizen selected.");
			return true;
		}

		NPC ThisNPC = CitizensAPI.getNPCRegistry().getById(player.getMetadata("selected").get(0).asInt());      // Gets NPC Selected

		if (!ThisNPC.getTrait(DenizenTrait.class).isDenizen) {
			player.sendMessage(ChatColor.RED + "That command must be performed on a denizen!");
			return true;
		}

		if (args[0].equalsIgnoreCase("bookmark")) {
			if(args.length < 3) {
				player.sendMessage(ChatColor.GOLD + "Invalid use.  Use /denizen help bookmark");
				return true;
			}
			else if (args[1].equalsIgnoreCase("location")) {
				List<String> locationList = getSaves().getStringList("Denizens." + ThisNPC.getName() + ".Bookmarks.Location");
				locationList.add(args[2] + " " + player.getWorld().getName() + ";" + player.getLocation().getX() + ";" +
						player.getLocation().getY() + ";" + player.getLocation().getZ() + ";" + player.getLocation().getYaw() + ";" + player.getLocation().getPitch());
				getSaves().set("Denizens." + ThisNPC.getName() + ".Bookmarks.Location", locationList);				
				saveSaves();
				bookmarks.buildLocationTriggerList();
				player.sendMessage(ChatColor.GOLD + "Location bookmark added. Your denizen can now reference this location.");
				return true;
			}

			else if (args[1].equalsIgnoreCase("block")) {
				List<String> blockList = getSaves().getStringList("Denizens." + ThisNPC.getName() + ".Bookmarks.Block");
				Block targetBlock = player.getTargetBlock(null, 6);
				blockList.add(args[2] + " " + player.getWorld().getName() + ";" + targetBlock.getX() + ";" +
						targetBlock.getY() + ";" + targetBlock.getZ());

				getSaves().set("Denizens." + ThisNPC.getName() + ".Bookmarks.Block", blockList);				
				saveSaves();
				player.sendMessage(ChatColor.GOLD + "Block bookmark added. Your denizen can now reference this block.");
				return true;
			}
		}
		return true;
	}







}


