package net.aufdemrand.denizen;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.aufdemrand.denizen.events.EventManager;
import net.aufdemrand.denizen.events.bukkit.SavesReloadEvent;
import net.aufdemrand.denizen.flags.FlagManager;
import net.aufdemrand.denizen.interfaces.dExternal;
import net.aufdemrand.denizen.listeners.ListenerRegistry;
import net.aufdemrand.denizen.npc.dNPCRegistry;
import net.aufdemrand.denizen.npc.traits.*;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.objects.notable.NotableManager;
import net.aufdemrand.denizen.objects.properties.PropertyParser;
import net.aufdemrand.denizen.scripts.*;
import net.aufdemrand.denizen.scripts.commands.CommandRegistry;
import net.aufdemrand.denizen.scripts.containers.core.InventoryScriptHelper;
import net.aufdemrand.denizen.scripts.containers.core.ItemScriptHelper;
import net.aufdemrand.denizen.scripts.containers.core.WorldScriptHelper;
import net.aufdemrand.denizen.scripts.queues.ScriptEngine;
import net.aufdemrand.denizen.scripts.queues.core.InstantQueue;
import net.aufdemrand.denizen.scripts.requirements.RequirementRegistry;
import net.aufdemrand.denizen.scripts.triggers.TriggerRegistry;
import net.aufdemrand.denizen.tags.TagManager;
import net.aufdemrand.denizen.utilities.RuntimeCompiler;
import net.aufdemrand.denizen.utilities.ScoreboardHelper;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.depends.Depends;
import net.aufdemrand.denizen.utilities.packets.PacketHelper;
import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.TraitInfo;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;


public class Denizen extends JavaPlugin {
    public final static int configVersion = 4;
    public static String versionTag = null;
    private boolean startedSuccessful = false;


    private CommandHandler commandHandler;

    public CommandHandler getCommandHandler() {
        return commandHandler;
    }


    /*
     * Denizen Engines
     */
    private ScriptEngine scriptEngine = new ScriptEngine(this);

    public ScriptEngine getScriptEngine() {
        return scriptEngine;
    }


    /*
     * Denizen Registries
     */
    private CommandRegistry commandRegistry = new CommandRegistry(this);
    private TriggerRegistry triggerRegistry = new TriggerRegistry();
    private RequirementRegistry requirementRegistry = new RequirementRegistry(this);
    private ListenerRegistry listenerRegistry = new ListenerRegistry();
    private dNPCRegistry dNPCRegistry;


    public CommandRegistry getCommandRegistry() {
        return commandRegistry;
    }

    public dNPCRegistry getNPCRegistry() {
        return dNPCRegistry;
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
     * Denizen Property Parser
     */
    private PropertyParser propertyParser;

    public PropertyParser getPropertyParser() {
        return propertyParser;
    }



    /*
     * Denizen Managers
     */
    private FlagManager flagManager = new FlagManager(this);
    private TagManager tagManager = new TagManager(this);
    private NotableManager notableManager = new NotableManager();
    private EventManager eventManager;

    public EventManager eventManager() {
        return eventManager;
    }

    public FlagManager flagManager() {
        return flagManager;
    }

    public TagManager tagManager() {
        return tagManager;
    }

    public NotableManager notableManager() {
        return notableManager;
    }

    public Depends depends = new Depends();

    public RuntimeCompiler runtimeCompiler;

    private WorldScriptHelper ws_helper;


    /*
     * Sets up Denizen on start of the CraftBukkit server.
     */
    @Override
    public void onEnable() {
        try {
        // Activate dependencies
        depends.initialize();

        if(Depends.citizens == null || !Depends.citizens.isEnabled()) {
            dB.echoError("Citizens does not seem to be activated! Deactivating Denizen!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        } else startedSuccessful = true;

        versionTag = this.getDescription().getVersion();

        // Startup procedure
        dB.log(ChatColor.LIGHT_PURPLE + "+-------------------------+");
        dB.log(ChatColor.YELLOW + " _/_ _  ._  _ _  ");
        dB.log(ChatColor.YELLOW + "(/(-/ )/ /_(-/ ) " + ChatColor.GRAY + " scriptable minecraft");
        dB.log("");
        dB.log(ChatColor.GRAY + "by: " + ChatColor.WHITE + "aufdemrand");
        dB.log(ChatColor.GRAY + "version: "+ ChatColor.WHITE + versionTag);
        dB.log(ChatColor.LIGHT_PURPLE + "+-------------------------+");
        }
        catch (Exception e) {
            dB.echoError(e);
        }

        try {
        // Create the dNPC Registry
        dNPCRegistry = new dNPCRegistry(this);

        // Maintain a list of Offline Players
        dPlayer.offlinePlayers.clear();
        for (OfflinePlayer player: Bukkit.getOfflinePlayers())
            dPlayer.offlinePlayers.add(player);

        // Register commandHandler with Citizens2
        commandHandler = new CommandHandler(Depends.citizens);

        // Register CommandHandler with Citizens
        Depends.citizens.registerCommandClass(CommandHandler.class);
        }
        catch (Exception e) {
            dB.echoError(e);
        }

        try {
        // Register script-container types
        ScriptRegistry._registerCoreTypes();

        // Populate config.yml if it doesn't yet exist.
        saveDefaultConfig();
        reloadConfig();
        }
        catch (Exception e) {
            dB.echoError(e);
        }

        try {
        // Ensure the Scripts and Midi folder exist
        new File(getDataFolder() + "/scripts").mkdirs();
        new File(getDataFolder() + "/midi").mkdirs();
        new File(getDataFolder() + "/schematics").mkdirs();

        // Ensure the example Denizen.mid sound file is available
        if (!new File(getDataFolder() + "/midi/Denizen.mid").exists()) {
            String sourceFile = URLDecoder.decode(Denizen.class.getProtectionDomain().getCodeSource().getLocation().getFile());
            dB.log("Denizen.mid not found, extracting from " + sourceFile);
            Utilities.extractFile(new File(sourceFile), "Denizen.mid", getDataFolder() + "/midi/");
        }
        }
        catch (Exception e) {
            dB.echoError(e);
        }

        try {
        // Warn if configuration is outdated / too new
        if (!getConfig().isSet("Config.Version") ||
                getConfig().getInt("Config.Version", 0) != configVersion) {

            dB.echoError("Your Denizen config file is from a different version. " +
                    "Some settings will not be available unless you generate a new one. " +
                    "This is easily done by stopping the server, deleting the current config.yml file in the Denizen folder " +
                    "and restarting the server.");
        }

        // Load the saves.yml into memory
        reloadSaves();

        // Create the command script handler for listener
        ws_helper = new WorldScriptHelper();
        ItemScriptHelper is_helper = new ItemScriptHelper();
        InventoryScriptHelper in_helper = new InventoryScriptHelper();
        }
        catch (Exception e) {
            dB.echoError(e);
        }

        try {
        // Register traits
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(TriggerTrait.class).withName("triggers"));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(PushableTrait.class).withName("pushable"));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(AssignmentTrait.class).withName("assignment"));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(NicknameTrait.class).withName("nickname"));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(HealthTrait.class).withName("health"));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(ConstantsTrait.class).withName("constants"));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(NameplateTrait.class).withName("nameplate"));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(HungerTrait.class).withName("hunger"));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(SittingTrait.class).withName("sitting"));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(FishingTrait.class).withName("fishing"));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(SleepingTrait.class).withName("sleeping"));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(ParticlesTrait.class).withName("particles"));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(SneakingTrait.class).withName("sneaking"));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(InvisibleTrait.class).withName("invisible"));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(MobproxTrait.class).withName("mobprox"));

        // If Program AB, used for reading Artificial Intelligence Markup Language
        // 2.0, is included as a dependency at Denizen/lib/Ab.jar, register the
        // ChatbotTrait
        if (Depends.hasProgramAB)
            CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(ChatbotTrait.class).withName("chatbot"));

        // Create instance of PacketHelper if ProtocolLib has been hooked
        if(Depends.protocolManager != null) {
            new PacketHelper(this);
            dB.echoApproval("ProtocolLib hooked, traits and commands with custom packages can be used!");
        }

        // Compile and load Denizen externals
        runtimeCompiler = new RuntimeCompiler(this);
        runtimeCompiler.loader();
        }
        catch (Exception e) {
            dB.echoError(e);
        }

        // Register Core Members in the Denizen Registries
        try {
        getCommandRegistry().registerCoreMembers();
        getTriggerRegistry().registerCoreMembers();
        getRequirementRegistry().registerCoreMembers();
        getListenerRegistry().registerCoreMembers();
        tagManager().registerCoreTags();
        eventManager = new EventManager();
        eventManager().registerCoreMembers();

        // Register Core dObjects with the ObjectFetcher
            ObjectFetcher._registerCoreObjects();
        }
        catch (Exception e) {
            dB.echoError(e);
        }

        try {
        // Initialize non-standard dMaterials
        dMaterial._initialize();

        // Initialize Property Parser
        propertyParser = new PropertyParser();

        ScriptHelper.reloadScripts();

        dB.log(ChatColor.LIGHT_PURPLE + "+-------------------------+");

        // Fire the 'on Server Start' world event
        ws_helper.serverStartEvent();
        }
        catch (Exception e) {
            dB.echoError(e);
        }
    }


    /*
     * Unloads Denizen on shutdown of the craftbukkit server.
     */
    @Override
    public void onDisable() {
        if(!startedSuccessful) return;

        // Save locations
        dLocation._saveLocations();

        // Save scoreboards
        ScoreboardHelper._saveScoreboards();

        // Save offline player inventories
        InventoryScriptHelper._savePlayerInventories();

        // Deconstruct listeners (server shutdown seems not to be triggering a PlayerQuitEvent)
        for (Player player : this.getServer().getOnlinePlayers())
            getListenerRegistry().deconstructPlayer(dPlayer.mirrorBukkitPlayer(player));

        for (OfflinePlayer player : this.getServer().getOfflinePlayers()) {
            try {
                getListenerRegistry().deconstructPlayer(dPlayer.mirrorBukkitPlayer(player)); } catch (Exception e) {
                if (player == null) dB.echoError("Tell aufdemrand ASAP about this error! ERR: OPN");
                else dB.echoError("'" + player.getName() + "' is having trouble deconstructing! " +
                        "You might have a corrupt player file!");
            }
        }

        // Unload loaded dExternals
        for (dExternal external : RuntimeCompiler.loadedExternals)
            external.unload();
        RuntimeCompiler.loadedExternals.clear();

        //Disable core members
        getCommandRegistry().disableCoreMembers();

        getLogger().log(Level.INFO, " v" + getDescription().getVersion() + " disabled.");
        Bukkit.getServer().getScheduler().cancelTasks(this);
        HandlerList.unregisterAll(this);
        saveSaves();
    }


    /*
     * Reloads, retrieves and saves progress information in
     * Denizen/saves.yml and Denizen/scoreboards.yml
     */
    private FileConfiguration savesConfig = null;
    private File savesConfigFile = null;
    private FileConfiguration scoreboardsConfig = null;
    private File scoreboardsConfigFile = null;

    public void reloadSaves() {
        if (savesConfigFile == null) {
            savesConfigFile = new File(getDataFolder(), "saves.yml");
        }
        savesConfig = YamlConfiguration.loadConfiguration(savesConfigFile);
        // Reload dLocations from saves.yml
        dLocation._recallLocations();

        if (scoreboardsConfigFile == null) {
            scoreboardsConfigFile = new File(getDataFolder(), "scoreboards.yml");
        }
        scoreboardsConfig = YamlConfiguration.loadConfiguration(scoreboardsConfigFile);
        // Reload scoreboards from scoreboards.yml
        ScoreboardHelper._recallScoreboards();

        Bukkit.getServer().getPluginManager().callEvent(new SavesReloadEvent());
    }

    public FileConfiguration getSaves() {
        if (savesConfig == null) {
            reloadSaves();
        }
        return savesConfig;
    }

    public FileConfiguration getScoreboards() {
        if (scoreboardsConfig == null) {
            reloadSaves();
        }
        return scoreboardsConfig;
    }

    public void saveSaves() {
        if (savesConfig == null || savesConfigFile == null) {
            return;
        }
        try {
            // Save dLocations to saves.yml
            dLocation._saveLocations();
            // Save scoreboards to scoreboards.yml
            ScoreboardHelper._saveScoreboards();
            savesConfig.save(savesConfigFile);
        } catch (IOException ex) {
            Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE, "Could not save to " + savesConfigFile, ex);
        }
        try {
            scoreboardsConfig.save(scoreboardsConfigFile);
        } catch (IOException ex) {
            Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE, "Could not save to " + scoreboardsConfigFile, ex);
        }
    }


    /*
     * Use Citizens' Command API to handle commands
     */
    Citizens citizens;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String cmdName, String[] args) {
        if (citizens == null)
            citizens = (Citizens) getServer().getPluginManager().getPlugin("Citizens");

        // <--[language]
        // @name /ex command
        // @group Console Commands
        // @description
        // The '/ex' command is an easy way to run a single denizen script command in-game. Its syntax,
        // aside from '/ex' is exactly the same as any other command. When running a command, some context
        // is also supplied, such as '<player>' if being run by a player (versus the console), as well as
        // '<npc>' if a NPC is selected by using the '/npc sel' command.
        //
        // Examples:
        // /ex flag <player> test_flag:!
        // /ex run 's@npc walk script' as:<npc>
        //
        // Need to '/ex' a command as a different player or NPC? No problem. Just use the 'npc' and 'player'
        // value arguments, or utilize the object fetcher.
        //
        // Examples:
        // /ex narrate player:p@NLBlackEagle 'Your health is <player.health.formatted>.'
        // /ex walk npc:n@fred <player.location.cursor_on>

        // -->

        // ...except this one :) /ex command
        if (cmdName.equalsIgnoreCase("ex")) {
            List<String> entries = new ArrayList<String>();
            String entry = "";
            for (String arg : args)
                entry = entry + arg + " ";

            if (entry.length() < 2) {
                sender.sendMessage("/ex <dCommand> (arguments)");
                return true;
            }

            entries.add(entry);
            InstantQueue queue = InstantQueue.getQueue(null);
            NPC npc = citizens.getNPCSelector().getSelected(sender);
            List<ScriptEntry> scriptEntries = ScriptBuilder.buildScriptEntries(entries, null,
                    (sender instanceof Player)?dPlayer.mirrorBukkitPlayer((Player) sender):null,
                    npc != null ? new dNPC(npc) : null);

            queue.addEntries(scriptEntries);
            queue.start();
            return true;
        }

        return citizens.onCommand(sender, cmd, cmdName, args);
    }

}


