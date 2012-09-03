package net.aufdemrand.denizen;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.aufdemrand.denizen.activities.ActivityEngine;
import net.aufdemrand.denizen.activities.ActivityRegistry;
import net.aufdemrand.denizen.bookmarks.BookmarkHelper;
import net.aufdemrand.denizen.commands.CommandRegistry;
import net.aufdemrand.denizen.commands.Executer;
import net.aufdemrand.denizen.npc.DenizenNPCRegistry;
import net.aufdemrand.denizen.npc.DenizenTrait;
import net.aufdemrand.denizen.npc.SpeechEngine;
import net.aufdemrand.denizen.requirements.GetRequirements;
import net.aufdemrand.denizen.requirements.RequirementRegistry;
import net.aufdemrand.denizen.scripts.ScriptEngine;
import net.aufdemrand.denizen.triggers.TriggerRegistry;
import net.aufdemrand.denizen.utilities.GetPlayer;
import net.aufdemrand.denizen.utilities.GetWorld;
import net.aufdemrand.denizen.utilities.Utilities;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.trait.TraitInfo;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.herocraftonline.heroes.Heroes;

public class Denizen extends JavaPlugin {

	public Economy   economy = null;
	public Permission  perms = null;
	public Heroes heroes = null;

	private CommandRegistry commandRegistry = new CommandRegistry(this);
	private TriggerRegistry triggerRegistry = new TriggerRegistry(this);
	private RequirementRegistry requirementRegistry = new RequirementRegistry(this);
	private DenizenNPCRegistry denizenNPCRegistry = new DenizenNPCRegistry(this);
	private ActivityRegistry activityRegistry = new ActivityRegistry(this);

	private ScriptEngine scriptEngine = new ScriptEngine(this);
	private SpeechEngine speechEngine = new SpeechEngine(this);
	private ActivityEngine activityEngine = new ActivityEngine(this);

	public Executer         executer = new Executer(this);
	public BookmarkHelper  bookmarks = new BookmarkHelper(this);
	public Utilities	   utilities = new Utilities(this);
	public Settings         settings = new Settings(this);

	public GetPlayer             getPlayer = new GetPlayer(this);
	public GetRequirements getRequirements = new GetRequirements(this);
	public GetWorld               getWorld = new GetWorld(this);

	public Boolean   debugMode = false;
	public Boolean preciseMode = false;
	public Boolean    newbMode = true;


	public DenizenNPCRegistry getDenizenNPCRegistry() {
		return denizenNPCRegistry;
	}

	public ActivityRegistry getActivityRegistry() {
		return activityRegistry;
	}

	public ActivityEngine getActivityEngine() {
		return activityEngine;
	}

	public RequirementRegistry getRequirementRegistry() {
		return requirementRegistry;
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

		if (getServer().getPluginManager().getPlugin("Heroes") != null) {
			getLogger().log(Level.INFO, "Found HEROES, you can use Heroes-specific commands!");
			this.heroes = (Heroes) getServer().getPluginManager().getPlugin("Heroes");
		}
		
		/* Load YAML files into memory */
		reloadConfig();
		reloadScripts();
		reloadSaves();
		reloadAssignments();

		/* Register Citizens2 trait, Denizen Modules, and Bukkit tasks */
		CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(DenizenTrait.class).withName("denizen"));
		commandRegistry.registerCoreCommands();
		triggerRegistry.registerCoreTriggers();
		activityRegistry.registerCoreActivities();
		requirementRegistry.registerCoreRequirements();

		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			@Override public void run() { scriptEngine.runQueues(); }
		}, settings.InteractDelayInTicks(), settings.InteractDelayInTicks());

		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			@Override public void run() { activityEngine.scheduleScripts(false); }
		}, 1, 600);

		getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			@Override public void run() { bookmarks.buildLocationTriggerList(); }
		}, 50);

		getServer().getPluginManager().registerEvents(activityEngine, this);

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
	public boolean showStackTraces = false;

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

	private CommandHandler commandHandler = new CommandHandler(this);
	
	public CommandHandler getCommandHandler() {
		return commandHandler;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {

		return getCommandHandler().onCommand(sender, cmd, cmdLabel, args);
		
	}







}


