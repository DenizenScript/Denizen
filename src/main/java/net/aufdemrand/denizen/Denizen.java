package net.aufdemrand.denizen;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.aufdemrand.denizen.events.EventManager;
import net.aufdemrand.denizen.events.bukkit.SavesReloadEvent;
import net.aufdemrand.denizen.flags.FlagManager;
import net.aufdemrand.denizen.interfaces.dExternal;
import net.aufdemrand.denizen.listeners.ListenerRegistry;
import net.aufdemrand.denizen.npc.dNPCRegistry;
import net.aufdemrand.denizen.npc.speech.DenizenChat;
import net.aufdemrand.denizen.npc.traits.*;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.objects.notable.NotableManager;
import net.aufdemrand.denizen.objects.properties.PropertyParser;
import net.aufdemrand.denizen.scripts.*;
import net.aufdemrand.denizen.scripts.commands.CommandRegistry;
import net.aufdemrand.denizen.scripts.containers.core.*;
import net.aufdemrand.denizen.scripts.queues.ScriptEngine;
import net.aufdemrand.denizen.scripts.queues.core.InstantQueue;
import net.aufdemrand.denizen.scripts.requirements.RequirementRegistry;
import net.aufdemrand.denizen.scripts.triggers.TriggerRegistry;
import net.aufdemrand.denizen.tags.TagManager;
import net.aufdemrand.denizen.utilities.RuntimeCompiler;
import net.aufdemrand.denizen.utilities.ScoreboardHelper;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.utilities.command.CommandManager;
import net.aufdemrand.denizen.utilities.command.Injector;
import net.aufdemrand.denizen.utilities.command.messaging.Messaging;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.depends.Depends;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.trait.TraitInfo;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;


public class Denizen extends JavaPlugin {
    public final static int configVersion = 7;
    public static String versionTag = null;
    private boolean startedSuccessful = false;


    private CommandManager commandManager;

    public CommandManager getCommandManager() { return commandManager; }


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
            net.minecraft.server.v1_7_R4.Block.getById(0);
        }
        catch (NoClassDefFoundError e) {
            getLogger().warning("-------------------------------------");
            getLogger().warning("This Denizen version is not compatible with this CraftBukkit version! Deactivating Denizen!");
            getLogger().warning("-------------------------------------");
            getServer().getPluginManager().disablePlugin(this);
            startedSuccessful = false;
            return;
        }

        try {
            // Activate dependencies
            depends.initialize();

            if(Depends.citizens == null || !Depends.citizens.isEnabled()) {
                getLogger().warning("Citizens does not seem to be activated! Denizen will have greatly reduced functionality!");
                //getServer().getPluginManager().disablePlugin(this);
                //return;
            }
            startedSuccessful = true;
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

            // Create our CommandManager to handle '/denizen' commands
            commandManager = new CommandManager();
            commandManager.setInjector(new Injector(this));
            commandManager.register(DenizenCommandHandler.class);

            // If Citizens is enabled, let it handle '/npc' commands
            if (Depends.citizens != null) {
                Depends.citizens.registerCommandClass(NPCCommandHandler.class);
            }

            // Track all player names for quick dPlayer matching
            for (OfflinePlayer player: Bukkit.getOfflinePlayers()) {
                dPlayer.notePlayer(player);
            }
        }
        catch (Exception e) {
            dB.echoError(e);
        }

        try {
            getCommandRegistry().registerCoreMembers();
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

            // Create the command script handler for listener
            ws_helper = new WorldScriptHelper();
            ItemScriptHelper is_helper = new ItemScriptHelper();
            InventoryScriptHelper in_helper = new InventoryScriptHelper();
            EntityScriptHelper es_helper = new EntityScriptHelper();
            CommandScriptHelper cs_helper = new CommandScriptHelper();
        }
        catch (Exception e) {
            dB.echoError(e);
        }

        try {
            if (Depends.citizens != null) {
                // Register traits
                // TODO: should this be a separate function?
                CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(TriggerTrait.class).withName("triggers"));
                CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(PushableTrait.class).withName("pushable"));
                CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(AssignmentTrait.class).withName("assignment"));
                CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(NicknameTrait.class).withName("nickname"));
                CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(HealthTrait.class).withName("health"));
                CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(ConstantsTrait.class).withName("constants"));
                CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(HungerTrait.class).withName("hunger"));
                CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(SittingTrait.class).withName("sitting"));
                CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(FishingTrait.class).withName("fishing"));
                CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(SleepingTrait.class).withName("sleeping"));
                CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(ParticlesTrait.class).withName("particles"));
                CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(SneakingTrait.class).withName("sneaking"));
                CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(InvisibleTrait.class).withName("invisible"));
                CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(MobproxTrait.class).withName("mobprox"));

                // Register Speech AI
                CitizensAPI.getSpeechFactory().register(DenizenChat.class, "denizen_chat");
            }

            // If Program AB, used for reading Artificial Intelligence Markup Language
            // 2.0, is included as a dependency at Denizen/lib/Ab.jar, register the
            // ChatbotTrait
            if (Depends.hasProgramAB)
                CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(ChatbotTrait.class).withName("chatbot"));

            // Compile and load Denizen externals
            runtimeCompiler = new RuntimeCompiler(this);
            runtimeCompiler.loader();
        }
        catch (Exception e) {
            dB.echoError(e);
        }

        // Register Core Members in the Denizen Registries
        try {
            getTriggerRegistry().registerCoreMembers();
            getRequirementRegistry().registerCoreMembers();
            getListenerRegistry().registerCoreMembers();
        }
        catch (Exception e) {
            dB.echoError(e);
        }

        try {
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
        }
        catch (Exception e) {
            dB.echoError(e);
        }

        // Run everything else on the first server tick
        getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                try {
                    ScriptHelper.reloadScripts();

                    // Reload notables from notables.yml into memory
                    notableManager.reloadNotables();

                    // Load the saves.yml into memory
                    reloadSaves();

                    dB.log(ChatColor.LIGHT_PURPLE + "+-------------------------+");

                    // Fire the 'on Server Start' world event
                    ws_helper.serverStartEvent();
                }
                catch (Exception e) {
                    dB.echoError(e);
                }
            }
        }, 1);
    }


    /*
     * Unloads Denizen on shutdown of the craftbukkit server.
     */
    @Override
    public void onDisable() {
        if(!startedSuccessful) return;

        // Save notables
        notableManager.saveNotables();

        // Save scoreboards
        ScoreboardHelper._saveScoreboards();

        // Save entities
        EntityScriptHelper.saveEntities();

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
    private FileConfiguration entityConfig = null;
    private File entityConfigFile = null;

    public void reloadSaves() {
        if (savesConfigFile == null) {
            savesConfigFile = new File(getDataFolder(), "saves.yml");
        }
        savesConfig = YamlConfiguration.loadConfiguration(savesConfigFile);

        // Reload dLocations from saves.yml, load them into NotableManager // TODO: probably remove this
        dLocation._recallLocations();

        // Update saves from name to UUID
        updateSaves();

        if (scoreboardsConfigFile == null) {
            scoreboardsConfigFile = new File(getDataFolder(), "scoreboards.yml");
        }
        scoreboardsConfig = YamlConfiguration.loadConfiguration(scoreboardsConfigFile);
        // Reload scoreboards from scoreboards.yml
        ScoreboardHelper._recallScoreboards();

        if (entityConfigFile == null) {
            entityConfigFile = new File(getDataFolder(), "entities.yml");
        }
        entityConfig = YamlConfiguration.loadConfiguration(entityConfigFile);
        // Load entities from entities.yml
        EntityScriptHelper.reloadEntities();

        Bukkit.getServer().getPluginManager().callEvent(new SavesReloadEvent());
    }

    public void updateSaves() {
        int saves_version = 1;
        if (savesConfig.contains("a_saves.version"))
            saves_version = savesConfig.getInt("a_saves.version");

        if (saves_version == 1) {
            dB.log("Updating saves from v1 to v2...");
            ConfigurationSection section = savesConfig.getConfigurationSection("Players");
            if (section != null) {
                ArrayList<String> keyList = new ArrayList<String>(section.getKeys(false));
                // Remove UPPERCASE cooldown saves from the list - handled manually
                for (int i = 0; i < keyList.size(); i++) {
                    String key = keyList.get(i);
                    if (!key.equals(key.toUpperCase()) && keyList.contains(key.toUpperCase())) {
                        keyList.remove(key.toUpperCase());
                    }
                }
                // Handle all actual player saves
                for (int i = 0; i < keyList.size(); i++) {
                    String key = keyList.get(i);
                    try {
                        // Flags
                        ConfigurationSection playerSection = savesConfig.getConfigurationSection("Players." + key);
                        if (playerSection == null) {
                            dB.echoError("Can't update saves for player '" + key + "' - broken YAML section!");
                            continue;
                        }
                        Map<String, Object> keys = playerSection.getValues(true);
                        if (!key.equals(key.toUpperCase()) && savesConfig.contains("Players." + key.toUpperCase())) {
                            // Cooldowns
                            keys.putAll(savesConfig.getConfigurationSection("Players." + key.toUpperCase()).getValues(true));
                            savesConfig.set("Players." + key.toUpperCase(), null);
                        }
                        dPlayer player = dPlayer.valueOf(key);
                        if (player == null) {
                            dB.echoError("Can't update saves for player '" + key + "' - invalid name!");
                            savesConfig.createSection("PlayersBACKUP." + key, keys);
                            // TODO: READ FROM BACKUP AT LOG IN
                        }
                        else {
                            savesConfig.createSection("Players." + player.getSaveName(), keys);
                        }
                        savesConfig.set("Players." + key, null);
                    }
                    catch (Exception ex) {
                        dB.echoError(ex);
                    }
                }
            }
            section = savesConfig.getConfigurationSection("Listeners");
            if (section != null) {
                for (String key: section.getKeys(false)) {
                    try {
                        dPlayer player = dPlayer.valueOf(key);
                        if (player == null)
                            dB.log("Warning: can't update listeners for player '" + key + "' - invalid name!");
                        else // Listeners
                            savesConfig.createSection("Listeners." + player.getSaveName(), savesConfig.getConfigurationSection("Listeners." + key).getValues(true));
                        savesConfig.set("Listeners." + key, null);
                    }
                    catch (Exception ex) {
                        dB.echoError(ex);
                    }
                }
            }
            savesConfig.set("a_saves.version", "2");
            dB.log("Done!");
        }
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

    public FileConfiguration getEntities() {
        if (entityConfig == null) {
            reloadSaves();
        }
        return entityConfig;
    }

    public void saveSaves() {
        if (savesConfig == null || savesConfigFile == null) {
            return;
        }
        // Save notables
        notableManager.saveNotables();
        // Save scoreboards to scoreboards.yml
        ScoreboardHelper._saveScoreboards();
        // Save entities to entities.yml
        EntityScriptHelper.saveEntities();
        try {
            savesConfig.save(savesConfigFile);
        } catch (IOException ex) {
            Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE, "Could not save to " + savesConfigFile, ex);
        }
        try {
            scoreboardsConfig.save(scoreboardsConfigFile);
        } catch (IOException ex) {
            Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE, "Could not save to " + scoreboardsConfigFile, ex);
        }
        try {
            entityConfig.save(entityConfigFile);
        } catch (IOException ex) {
            Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE, "Could not save to " + entityConfigFile, ex);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String cmdName, String[] args) {

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

            if (Settings.ShowExHelp()) {
                if (dB.showDebug)
                    sender.sendMessage(ChatColor.YELLOW + "Executing dCommand... check the console for debug output!");
                else
                    sender.sendMessage(ChatColor.YELLOW + "Executing dCommand... to see debug, use /denizen debug");
            }

            entries.add(entry);
            InstantQueue queue = InstantQueue.getQueue(null);
            dNPC npc = null;
            if (Depends.citizens != null && Depends.citizens.getNPCSelector().getSelected(sender) != null)
                npc = new dNPC(Depends.citizens.getNPCSelector().getSelected(sender));
            List<ScriptEntry> scriptEntries = ScriptBuilder.buildScriptEntries(entries, null,
                    (sender instanceof Player)?dPlayer.mirrorBukkitPlayer((Player) sender):null, npc);

            queue.addEntries(scriptEntries);
            queue.start();
            return true;
        }

        //if (Depends.citizens != null)
        //    return citizens.onCommand(sender, cmd, cmdName, args);
        String modifier = args.length > 0 ? args[0] : "";
        if (!commandManager.hasCommand(cmd, modifier) && !modifier.isEmpty()) {
            return suggestClosestModifier(sender, cmd.getName(), modifier);
        }

        Object[] methodArgs = { sender };
        return commandManager.executeSafe(cmd, args, sender, methodArgs);

    }

    private boolean suggestClosestModifier(CommandSender sender, String command, String modifier) {
        String closest = commandManager.getClosestCommandModifier(command, modifier);
        if (!closest.isEmpty()) {
            Messaging.send(sender, "<7>Unknown command. Did you mean:");
            Messaging.send(sender, " /" + command + " " + closest);
            return true;
        }
        return false;
    }
}


