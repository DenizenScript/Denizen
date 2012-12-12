package net.aufdemrand.denizen;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.aufdemrand.denizen.flags.FlagManager;
import net.aufdemrand.denizen.interfaces.NPCSpeechEngine;
import net.aufdemrand.denizen.listeners.ListenerRegistry;
import net.aufdemrand.denizen.notables.NotableManager;
import net.aufdemrand.denizen.npc.DenizenNPCRegistry;
import net.aufdemrand.denizen.npc.activities.ActivityEngine;
import net.aufdemrand.denizen.npc.activities.ActivityRegistry;
import net.aufdemrand.denizen.npc.traits.AssignmentTrait;
import net.aufdemrand.denizen.npc.traits.HealthTrait;
import net.aufdemrand.denizen.npc.traits.NicknameTrait;
import net.aufdemrand.denizen.npc.traits.TalkTrait;
import net.aufdemrand.denizen.npc.traits.TriggerTrait;
import net.aufdemrand.denizen.npc.traits.PushableTrait;
import net.aufdemrand.denizen.scripts.ScriptEngine;
import net.aufdemrand.denizen.scripts.commands.CommandRegistry;
import net.aufdemrand.denizen.scripts.requirements.RequirementRegistry;
import net.aufdemrand.denizen.scripts.triggers.TriggerRegistry;
import net.aufdemrand.denizen.tags.TagManager;
import net.aufdemrand.denizen.utilities.RuntimeCompiler;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.utilities.debugging.Debugger;
import net.aufdemrand.denizen.utilities.debugging.Debugger.DebugElement;

import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.trait.TraitInfo;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;


public class Denizen extends JavaPlugin {

    public static String versionTag = "0.8.2 pre-release";
    
    private Debugger debugger = new Debugger(this);

    public Debugger getDebugger() {
        return debugger;
    }
    
    private CommandHandler commandHandler;
    
    public CommandHandler getCommandHandler() {
        return commandHandler;
    }
    
    
    /*
     * Denizen Engines
     */

    private ScriptEngine scriptEngine = new ScriptEngine(this);
    private ActivityEngine activityEngine = new ActivityEngine(this);
    private NPCSpeechEngine speechEngine;

    public ActivityEngine getActivityEngine() {
        return activityEngine;
    }
    
    public ScriptEngine getScriptEngine() {
        return scriptEngine;
    }

    public NPCSpeechEngine getSpeechEngine() {
        return speechEngine;
    }

    public void registerSpeechEngine(NPCSpeechEngine speechEngine) {
        this.speechEngine = speechEngine;
    }

    
    /*
     * Denizen Registries
     */

    private CommandRegistry commandRegistry = new CommandRegistry(this);
    private TriggerRegistry triggerRegistry = new TriggerRegistry(this);
    private RequirementRegistry requirementRegistry = new RequirementRegistry(this);
    private ActivityRegistry activityRegistry = new ActivityRegistry(this);
    private ListenerRegistry listenerRegistry = new ListenerRegistry(this);
    private DenizenNPCRegistry denizenNPCRegistry;
    
    
    public ActivityRegistry getActivityRegistry() {
        return activityRegistry;
    }

    public CommandRegistry getCommandRegistry() {
        return commandRegistry;
    }

    public DenizenNPCRegistry getNPCRegistry() {
        return denizenNPCRegistry;
    }

    public ListenerRegistry getListenerRegistry() {
        return listenerRegistry;
    }
    
    public RequirementRegistry getRequirementRegistry() {
        return requirementRegistry;
    }

    public TriggerRegistry getTriggerRegistry() {
        return triggerRegistry;
    }

    
    /*
     * Denizen Managers
     */
    
    private NotableManager notableManager = new NotableManager(this);
    private FlagManager flagManager = new FlagManager(this);
    private TagManager tagManager = new TagManager(this);

    public FlagManager flagManager() {
        return flagManager;
    }

    public TagManager tagManager() {
        return tagManager;
    }

    public NotableManager notableManager() {
        return notableManager;
    }
    
    
    /*
     * Utilities
     */

    public static Settings settings;
    public static Utilities utilities = new Utilities();


    /*
     * Sets up Denizen on start of the craftbukkit server.	
     */

    @Override
    public void onEnable() {
        // Startup procedure
        debugger.echoDebug(DebugElement.Footer);
        debugger.echoDebug(ChatColor.YELLOW + " _/_ _  ._  _ _  ");
        debugger.echoDebug(ChatColor.YELLOW + "(/(-/ )/ /_(-/ ) " + ChatColor.GRAY + " scriptable NPCs"); 
        debugger.echoDebug(ChatColor.GRAY + "by: " + ChatColor.WHITE + "aufdemrand");
        debugger.echoDebug(ChatColor.GRAY + "version: "+ ChatColor.WHITE + versionTag);
        debugger.echoDebug(DebugElement.Footer);
        
        // Register commandHandler with Citizens2
        Citizens citizens = (Citizens) getServer().getPluginManager().getPlugin("Citizens");
        commandHandler = new CommandHandler(citizens);
        
        
        denizenNPCRegistry = new DenizenNPCRegistry(this);
        settings =  new Settings();

        // Populate config.yml if it doesn't yet exist.
        saveDefaultConfig(); 
        reloadConfig();
        reloadScripts();
        reloadSaves();

        // Register traits
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(TriggerTrait.class).withName("triggers"));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(PushableTrait.class).withName("pushable"));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(AssignmentTrait.class).withName("assignment"));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(TalkTrait.class).withName("talk"));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(NicknameTrait.class).withName("nickname"));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(HealthTrait.class).withName("health"));

        // Compile and load Denizen externals
        RuntimeCompiler compiler = new RuntimeCompiler(this);
        compiler.loader();
        
        // Register Core Members in the Denizen Registries
        getCommandRegistry().registerCoreMembers();
        getTriggerRegistry().registerCoreMembers();
        getActivityRegistry().registerCoreMembers();
        getRequirementRegistry().registerCoreMembers();
        
        // Load Notables into memory (for the Location Triggers to reference)
        notableManager().loadNotables();
        tagManager().registerCoreTags();
        citizens.registerCommandClass(CommandHandler.class);

        // Start the scriptEngine.. VROOM VROOM!
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override public void run() { getScriptEngine().run(); }
        }, settings.InteractDelayInTicks(), settings.InteractDelayInTicks());

        // Start the activityEngine
        getServer().getPluginManager().registerEvents(getActivityEngine(), this);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override public void run() { getActivityEngine().scheduler(false); }
        }, 1, 600);

        debugger.echoDebug(DebugElement.Footer);
    }


    /*
     * Unloads Denizen on shutdown of the craftbukkit server.
     */

    @Override
    public void onDisable() {
        getLogger().log(Level.INFO, " v" + getDescription().getVersion() + " disabled.");
        Bukkit.getServer().getScheduler().cancelTasks(this);
        HandlerList.unregisterAll(this);
        saveSaves();
    }


    /*
     * Reloads and retrieves information from the Denizen/scripts.yml.
     */

    private YamlConfiguration scriptConfig = null;

    public void reloadScripts() {
        String concatenated = scriptEngine.getScriptHelper().concatenateScripts();
        if (scriptConfig == null) scriptConfig = new YamlConfiguration();

        try { scriptConfig.loadFromString(concatenated);
        } catch (InvalidConfigurationException e) {
            getLogger().log(Level.SEVERE, "Error loading scripts to memory!");
            e.printStackTrace();
        }
    }

    public FileConfiguration getScripts() {
        if (scriptConfig == null) {
            reloadScripts();
        }
        return scriptConfig;
    }

    /*
     * Reloads, retrieves and saves progress information Denizen/saves.yml.
     */

    private FileConfiguration savesConfig = null;
    private File savesConfigFile = null;

    public void reloadSaves() {
        if (savesConfigFile == null) {
            savesConfigFile = new File(getDataFolder(), "saves.yml");
        }
        savesConfig = YamlConfiguration.loadConfiguration(savesConfigFile);
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
     * Reloads, retrieves and saves information from the Denizen/assignments.yml.
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


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String cmdName, String[] args) {
        Citizens citizens = (Citizens) getServer().getPluginManager().getPlugin("Citizens");
        return citizens.onCommand(sender, cmd, cmdName, args);
    }
    
}


