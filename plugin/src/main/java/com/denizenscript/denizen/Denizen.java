package com.denizenscript.denizen;

import com.denizenscript.denizen.events.ScriptEventRegistry;
import com.denizenscript.denizen.events.bukkit.SavesReloadEvent;
import com.denizenscript.denizen.events.core.CommandSmartEvent;
import com.denizenscript.denizen.events.core.CuboidEnterExitSmartEvent;
import com.denizenscript.denizen.events.core.FlagSmartEvent;
import com.denizenscript.denizen.events.core.NPCNavigationSmartEvent;
import com.denizenscript.denizen.flags.FlagManager;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.objects.notable.NotableManager;
import com.denizenscript.denizen.objects.properties.PropertyRegistry;
import com.denizenscript.denizen.scripts.commands.BukkitCommandRegistry;
import com.denizenscript.denizen.scripts.containers.ContainerRegistry;
import com.denizenscript.denizen.scripts.containers.core.*;
import com.denizenscript.denizen.scripts.triggers.TriggerRegistry;
import com.denizenscript.denizen.tags.core.ServerTagBase;
import com.denizenscript.denizen.utilities.*;
import com.denizenscript.denizen.utilities.blocks.OldMaterialsHelper;
import com.denizenscript.denizen.utilities.command.CommandManager;
import com.denizenscript.denizen.utilities.command.Injector;
import com.denizenscript.denizen.utilities.command.messaging.Messaging;
import com.denizenscript.denizen.utilities.debugging.StatsRecord;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.depends.Depends;
import com.denizenscript.denizen.utilities.entity.DenizenEntityType;
import com.denizenscript.denizen.utilities.maps.DenizenMapManager;
import com.denizenscript.denizen.utilities.packets.DenizenPacketHandler;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.interfaces.FakeArrow;
import com.denizenscript.denizen.nms.interfaces.FakePlayer;
import com.denizenscript.denizen.nms.interfaces.ItemProjectile;
import com.denizenscript.denizen.npc.TraitRegistry;
import com.denizenscript.denizen.npc.DenizenNPCHelper;
import com.denizenscript.denizencore.DenizenCore;
import com.denizenscript.denizencore.events.OldEventManager;
import com.denizenscript.denizencore.objects.ObjectFetcher;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptBuilder;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.ScriptHelper;
import com.denizenscript.denizencore.scripts.ScriptRegistry;
import com.denizenscript.denizencore.scripts.commands.core.AdjustCommand;
import com.denizenscript.denizencore.scripts.queues.core.InstantQueue;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.debugging.SlowWarning;
import com.denizenscript.denizencore.utilities.text.ConfigUpdater;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Denizen extends JavaPlugin {

    public static String versionTag = null;
    private boolean startedSuccessful = false;

    private CommandManager commandManager;

    public CommandManager getCommandManager() {
        return commandManager;
    }

    /*
     * Denizen Registries
     */
    private BukkitCommandRegistry commandRegistry = new BukkitCommandRegistry();
    private TriggerRegistry triggerRegistry = new TriggerRegistry();
    private DenizenNPCHelper npcHelper;


    public BukkitCommandRegistry getCommandRegistry() {
        return commandRegistry;
    }

    public DenizenNPCHelper getNPCHelper() {
        return npcHelper;
    }

    public TriggerRegistry getTriggerRegistry() {
        return triggerRegistry;
    }

    /*
     * Denizen Managers
     */
    private FlagManager flagManager = new FlagManager(this);
    private TagManager tagManager = new TagManager();
    private NotableManager notableManager = new NotableManager();
    private OldEventManager eventManager;

    public OldEventManager eventManager() {
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

    private BukkitWorldScriptHelper ws_helper;

    public final static long startTime = System.currentTimeMillis();

    public DenizenCoreImplementation coreImplementation = new DenizenCoreImplementation();

    /*
     * Sets up Denizen on start of the CraftBukkit server.
     */
    @Override
    public void onEnable() {
        if (!NMSHandler.initialize(this)) {
            getLogger().warning("-------------------------------------");
            getLogger().warning("This build of Denizen is not compatible with this Spigot version! Deactivating Denizen!");
            getLogger().warning("-------------------------------------");
            getServer().getPluginManager().disablePlugin(this);
            startedSuccessful = false;
            return;
        }

        if (!NMSHandler.getInstance().isCorrectMappingsCode()) {
            getLogger().warning("-------------------------------------");
            getLogger().warning("This build of Denizen was built for a different Spigot revision! This may potentially cause issues."
                    + " If you are experiencing trouble, update Denizen and Spigot both to latest builds!"
                    + " If this message appears with both Denizen and Spigot fully up-to-date, contact the Denizen team (via GitHub, Spigot, or Discord) to request an update be built.");
            getLogger().warning("-------------------------------------");
        }

        try {
            versionTag = this.getDescription().getVersion();

            // Load Denizen's core
            DenizenCore.init(coreImplementation);

            // Activate dependencies
            Depends.initialize();

            if (Depends.citizens == null || !Depends.citizens.isEnabled()) {
                getLogger().warning("Citizens does not seem to be activated! Denizen will have greatly reduced functionality!");
                //getServer().getPluginManager().disablePlugin(this);
                //return;
            }
            startedSuccessful = true;
        }
        catch (Exception e) {
            Debug.echoError(e);
        }

        try {
            // Populate config.yml if it doesn't yet exist.
            saveDefaultConfig();
            reloadConfig();

            // Startup procedure
            Debug.log(ChatColor.LIGHT_PURPLE + "+-------------------------+");
            Debug.log(ChatColor.YELLOW + " _/_ _  ._  _ _  ");
            Debug.log(ChatColor.YELLOW + "(/(-/ )/ /_(-/ ) " + ChatColor.GRAY + " scriptable minecraft");
            Debug.log("");
            Debug.log(ChatColor.GRAY + "by: " + ChatColor.WHITE + "The DenizenScript team");
            Debug.log(ChatColor.GRAY + "Chat with us at: " + ChatColor.WHITE + " https://discord.gg/Q6pZGSR");
            Debug.log(ChatColor.GRAY + "Or learn more at: " + ChatColor.WHITE + " https://denizenscript.com");
            Debug.log(ChatColor.GRAY + "version: " + ChatColor.WHITE + versionTag);
            Debug.log(ChatColor.LIGHT_PURPLE + "+-------------------------+");
        }
        catch (Exception e) {
            Debug.echoError(e);
        }

        // bstats.org
        try {
            BStatsMetricsLite metrics = new BStatsMetricsLite(this);
        }
        catch (Throwable e) {
            Debug.echoError(e);
        }

        try {
            // If Citizens is enabled, Create the NPC Helper
            if (Depends.citizens != null) {
                npcHelper = new DenizenNPCHelper(this);
            }

            // Create our CommandManager to handle '/denizen' commands
            commandManager = new CommandManager();
            commandManager.setInjector(new Injector(this));
            commandManager.register(DenizenCommandHandler.class);

            // If Citizens is enabled, let it handle '/npc' commands
            if (Depends.citizens != null) {
                Depends.citizens.registerCommandClass(NPCCommandHandler.class);
            }

            DenizenEntityType.registerEntityType("ITEM_PROJECTILE", ItemProjectile.class);
            DenizenEntityType.registerEntityType("FAKE_ARROW", FakeArrow.class);
            DenizenEntityType.registerEntityType("FAKE_PLAYER", FakePlayer.class);

            // Track all player names for quick PlayerTag matching
            for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
                PlayerTag.notePlayer(player);
            }
        }
        catch (Exception e) {
            Debug.echoError(e);
        }

        try {
            DenizenCore.setCommandRegistry(getCommandRegistry());
            getCommandRegistry().registerCommands();
        }
        catch (Exception e) {
            Debug.echoError(e);
        }

        try {
            // Register script-container types
            ScriptRegistry._registerCoreTypes();
        }
        catch (Exception e) {
            Debug.echoError(e);
        }

        try {
            ContainerRegistry.registerMainContainers();
        }
        catch (Exception e) {
            Debug.echoError(e);
        }

        try {
            // Ensure the Scripts and Midi folder exist
            new File(getDataFolder() + "/scripts").mkdirs();
            new File(getDataFolder() + "/midi").mkdirs();
            new File(getDataFolder() + "/schematics").mkdirs();

            // Ensure the example Denizen.mid sound file is available
            if (!new File(getDataFolder() + "/midi/Denizen.mid").exists()) {
                String sourceFile = URLDecoder.decode(Denizen.class.getProtectionDomain().getCodeSource().getLocation().getFile());
                Debug.log("Denizen.mid not found, extracting from " + sourceFile);
                Utilities.extractFile(new File(sourceFile), "Denizen.mid", getDataFolder() + "/midi/");
            }
        }
        catch (Exception e) {
            Debug.echoError(e);
        }

        try {
            // Automatic config file update
            InputStream properConfig = Denizen.class.getResourceAsStream("/config.yml");
            String properConfigString = ScriptHelper.convertStreamToString(properConfig);
            properConfig.close();
            FileInputStream currentConfig = new FileInputStream(getDataFolder() + "/config.yml");
            String currentConfigString = ScriptHelper.convertStreamToString(currentConfig);
            currentConfig.close();
            String updated = ConfigUpdater.updateConfig(currentConfigString, properConfigString);
            if (updated != null) {
                Debug.log("Your config file is outdated. Automatically updating it...");
                FileOutputStream configOutput = new FileOutputStream(getDataFolder() + "/config.yml");
                OutputStreamWriter writer = new OutputStreamWriter(configOutput);
                writer.write(updated);
                writer.close();
                configOutput.close();
            }
        }
        catch (Exception e) {
            Debug.echoError(e);
        }

        try {
            ws_helper = new BukkitWorldScriptHelper();
            ItemScriptHelper is_helper = new ItemScriptHelper();
            InventoryScriptHelper in_helper = new InventoryScriptHelper();
            EntityScriptHelper es_helper = new EntityScriptHelper();
            CommandScriptHelper cs_helper = new CommandScriptHelper();
        }
        catch (Exception e) {
            Debug.echoError(e);
        }

        try {
            if (Depends.citizens != null) {
                // Register traits
                TraitRegistry.registerMainTraits();
            }
        }
        catch (Exception e) {
            Debug.echoError(e);
        }

        // Register Core Members in the Denizen Registries
        try {
            if (Depends.citizens != null) {
                getTriggerRegistry().registerCoreMembers();
            }
        }
        catch (Exception e) {
            Debug.echoError(e);
        }

        try {
            AdjustCommand.specialAdjustables.put("server", ServerTagBase::adjustServer);

            tagManager().registerCoreTags();

            CommonRegistries.registerMainTagHandlers();

            eventManager = new OldEventManager();
            // Register all the 'Core' SmartEvents.
            OldEventManager.registerSmartEvent(new CommandSmartEvent());
            OldEventManager.registerSmartEvent(new CuboidEnterExitSmartEvent());
            OldEventManager.registerSmartEvent(new FlagSmartEvent());
            OldEventManager.registerSmartEvent(new NPCNavigationSmartEvent());
            eventManager().registerCoreMembers();

            // Register all the modern script events
            ScriptEventRegistry.registermainEvents();

            CommonRegistries.registerMainObjects();

            // Register Core ObjectTags with the ObjectFetcher
            ObjectFetcher._registerCoreObjects();
        }
        catch (Exception e) {
            Debug.echoError(e);
        }

        try {
            // Initialize old Materials helper
            OldMaterialsHelper._initialize();
            // Initialize all properties
            PropertyRegistry.registermainProperties();
        }
        catch (Exception e) {
            Debug.echoError(e);
        }

        try {
            for (World world : getServer().getWorlds()) {
                EntityScriptHelper.linkWorld(world);
            }
        }
        catch (Exception e) {
            Debug.echoError(e);
        }

        if (Settings.packetInterception()) {
            NMSHandler.getInstance().enablePacketInterception(new DenizenPacketHandler());
        }
        try {
            if (Class.forName("com.destroystokyo.paper.PaperConfig") != null) {
                final Class<?> clazz = Class.forName("com.denizenscript.denizen.paper.PaperModule");
                clazz.getMethod("init").invoke(null);
            }
        }
        catch (ClassNotFoundException ex) {
            // Ignore.
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }

        // Run everything else on the first server tick
        getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                try {
                    DenizenCore.loadScripts();

                    // Synchronize any script commands added while loading scripts.
                    CommandScriptHelper.syncDenizenCommands();

                    // Load the saves.yml into memory
                    reloadSaves();

                    // Fire the 'on Pre Server Start' world event
                    ws_helper.serverPreStartEvent();

                    // Reload notables from notables.yml into memory
                    notableManager.reloadNotables();

                    Debug.log(ChatColor.LIGHT_PURPLE + "+-------------------------+");

                    // Fire the 'on Server Start' world event
                    ws_helper.serverStartEvent();

                    if (Settings.allowStupidx()) {
                        Debug.echoError("Don't screw with bad config values.");
                        Bukkit.shutdown();
                    }
                }
                catch (Exception e) {
                    Debug.echoError(e);
                }
            }
        }, 1);

        getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                DenizenCore.tick(50); // Sadly, minecraft has no delta timing, so a tick is always 50ms.
            }
        }, 1, 1);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (Settings.canRecordStats()) {
                    new StatsRecord().start();
                }
            }
        }.runTaskTimer(this, 100, 20 * 60 * 60);
    }


    /*
     * Unloads Denizen on shutdown of the craftbukkit server.
     */
    @Override
    public void onDisable() {
        if (!startedSuccessful) {
            return;
        }

        // <--[event]
        // @Events
        // shutdown
        //
        // @Regex ^on shutdown$
        //
        // @Warning not all plugins will be loaded and delayed scripts will be dropped.
        //
        // @Triggers when the server is shutting down.
        //
        // @Context
        // None.
        //
        // -->
        HashMap<String, ObjectTag> context = new HashMap<>();
        OldEventManager.doEvents(Arrays.asList("shutdown"), new BukkitScriptEntryData(null, null), context);

        // Disable the log interceptor... otherwise bad things on /reload
        /*if (logInterceptor != null) {
            logInterceptor.standardOutput();
        }*/

        // Save notables
        notableManager.saveNotables();

        // Save scoreboards
        ScoreboardHelper._saveScoreboards();

        // Save entities
        EntityScriptHelper.saveEntities();

        // Save offline player inventories
        InventoryScriptHelper._savePlayerInventories();

        //Disable core members
        getCommandRegistry().disableCoreMembers();

        getLogger().log(Level.INFO, " v" + getDescription().getVersion() + " disabled.");
        Bukkit.getServer().getScheduler().cancelTasks(this);
        HandlerList.unregisterAll(this);

        for (World world : getServer().getWorlds()) {
            EntityScriptHelper.unlinkWorld(world);
        }

        saveSaves();
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        Settings.refillCache();
        SlowWarning.WARNING_RATE = Settings.warningRate();
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

        // Load maps from maps.yml
        DenizenMapManager.reloadMaps();

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
        // Save maps to maps.yml
        DenizenMapManager.saveMaps();
        try {
            savesConfig.save(savesConfigFile);
        }
        catch (IOException ex) {
            Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE, "Could not save to " + savesConfigFile, ex);
        }
        try {
            scoreboardsConfig.save(scoreboardsConfigFile);
        }
        catch (IOException ex) {
            Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE, "Could not save to " + scoreboardsConfigFile, ex);
        }
        try {
            entityConfig.save(entityConfigFile);
        }
        catch (IOException ex) {
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
        // By default, ex command debug output is sent to the player that ran the ex command (if the command was ran by a player).
        // To avoid this, use '-q' at the start of the ex command.
        // Like: /ex -q narrate "wow no output"
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

        if (cmdName.equalsIgnoreCase("ex")) {
            List<Object> entries = new ArrayList<>();
            String entry = String.join(" ", args);
            boolean quiet = false;
            if (entry.length() > 3 && entry.startsWith("-q ")) {
                quiet = true;
                entry = entry.substring("-q ".length());
            }
            if (!Settings.showExDebug()) {
                quiet = !quiet;
            }

            if (entry.length() < 2) {
                sender.sendMessage("/ex (-q) <denizen script command> (arguments)");
                return true;
            }

            if (Settings.showExHelp()) {
                if (Debug.showDebug) {
                    sender.sendMessage(ChatColor.YELLOW + "Executing Denizen script command... check the console for full debug output!");
                }
                else {
                    sender.sendMessage(ChatColor.YELLOW + "Executing Denizen script command... to see debug, use /denizen debug");
                }
            }

            entries.add(entry);
            InstantQueue queue = new InstantQueue("EXCOMMAND");
            NPCTag npc = null;
            if (Depends.citizens != null && Depends.citizens.getNPCSelector().getSelected(sender) != null) {
                npc = new NPCTag(Depends.citizens.getNPCSelector().getSelected(sender));
            }
            List<ScriptEntry> scriptEntries = ScriptBuilder.buildScriptEntries(entries, null,
                    new BukkitScriptEntryData(sender instanceof Player ? new PlayerTag((Player) sender) : null, npc));

            queue.addEntries(scriptEntries);
            if (!quiet && sender instanceof Player) {
                queue.debugOutput = sender::sendMessage;
            }
            queue.start();
            return true;
        }

        String modifier = args.length > 0 ? args[0] : "";
        if (!commandManager.hasCommand(cmd, modifier) && !modifier.isEmpty()) {
            return suggestClosestModifier(sender, cmd.getName(), modifier);
        }

        Object[] methodArgs = {sender};
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

    public FlagManager.Flag getFlag(String string) {
        if (string.startsWith("fl")) {
            FlagManager flag_manager = DenizenAPI.getCurrentInstance().flagManager();
            if (string.indexOf('[') == 2) {
                int cb = string.indexOf(']');
                if (cb > 4) {
                    String owner = string.substring(3, cb);
                    String flag = string.substring(cb + 2);
                    if (PlayerTag.matches(owner)) {
                        PlayerTag player = PlayerTag.valueOf(owner);
                        if (FlagManager.playerHasFlag(player, flag)) {
                            return flag_manager.getPlayerFlag(player, flag);
                        }
                        else {
                            Debug.echoError("Player '" + owner + "' flag '" + flag + "' not found.");
                        }
                    }
                    else if (Depends.citizens != null && NPCTag.matches(owner)) {
                        NPCTag npc = NPCTag.valueOf(owner);
                        if (FlagManager.npcHasFlag(npc, flag)) {
                            return flag_manager.getNPCFlag(npc.getId(), flag);
                        }
                        else {
                            Debug.echoError("NPC '" + owner + "' flag '" + flag + "' not found.");
                        }
                    }
                    else if (EntityTag.matches(owner)) {
                        EntityTag entity = EntityTag.valueOf(owner);
                        if (FlagManager.entityHasFlag(entity, flag)) {
                            return flag_manager.getEntityFlag(entity, flag);
                        }
                        else {
                            Debug.echoError("Entity '" + owner + "' flag '" + flag + "' not found.");
                        }
                    }
                }
                else {
                    Debug.echoError("Invalid dFlag format: " + string);
                }
            }
            else if (string.indexOf('@') == 2) {
                String flag = string.substring(3);
                if (FlagManager.serverHasFlag(flag)) {
                    return flag_manager.getGlobalFlag(flag);
                }
                else {
                    Debug.echoError("Global flag '" + flag + "' not found.");
                }
            }
        }
        return null;
    }
}
