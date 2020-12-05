package com.denizenscript.denizen;

import com.denizenscript.denizen.events.ScriptEventRegistry;
import com.denizenscript.denizen.events.bukkit.SavesReloadEvent;
import com.denizenscript.denizen.events.server.ServerPrestartScriptEvent;
import com.denizenscript.denizen.events.server.ServerStartScriptEvent;
import com.denizenscript.denizen.events.server.ServerStopScriptEvent;
import com.denizenscript.denizen.objects.InventoryTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.objects.notable.NotableManager;
import com.denizenscript.denizen.objects.properties.PropertyRegistry;
import com.denizenscript.denizen.scripts.commands.BukkitCommandRegistry;
import com.denizenscript.denizen.scripts.commands.player.ClickableCommand;
import com.denizenscript.denizen.scripts.containers.ContainerRegistry;
import com.denizenscript.denizen.scripts.containers.core.*;
import com.denizenscript.denizen.scripts.triggers.TriggerRegistry;
import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizen.tags.core.ServerTagBase;
import com.denizenscript.denizen.utilities.*;
import com.denizenscript.denizen.utilities.command.DenizenCommandHandler;
import com.denizenscript.denizen.utilities.command.ExCommandHandler;
import com.denizenscript.denizen.utilities.command.ExSustainedCommandHandler;
import com.denizenscript.denizen.utilities.command.NPCCommandHandler;
import com.denizenscript.denizen.utilities.command.manager.CommandManager;
import com.denizenscript.denizen.utilities.command.manager.Injector;
import com.denizenscript.denizen.utilities.command.manager.messaging.Messaging;
import com.denizenscript.denizen.utilities.debugging.BStatsMetricsLite;
import com.denizenscript.denizen.utilities.debugging.StatsRecord;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.depends.Depends;
import com.denizenscript.denizen.utilities.entity.DenizenEntityType;
import com.denizenscript.denizen.utilities.implementation.DenizenCoreImplementation;
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
import com.denizenscript.denizencore.flags.MapTagFlagTracker;
import com.denizenscript.denizencore.objects.ObjectFetcher;
import com.denizenscript.denizencore.scripts.ScriptHelper;
import com.denizenscript.denizencore.scripts.ScriptRegistry;
import com.denizenscript.denizencore.scripts.commands.core.AdjustCommand;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.debugging.SlowWarning;
import com.denizenscript.denizencore.utilities.debugging.StrongWarning;
import com.denizenscript.denizencore.utilities.text.ConfigUpdater;
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
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Denizen extends JavaPlugin {

    private static Denizen instance;

    public static Denizen getInstance() {
        return instance;
    }

    public static String versionTag = null;
    private boolean startedSuccessful = false;

    public static boolean supportsPaper = false;

    public CommandManager commandManager;

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

    public TagManager tagManager = new TagManager();
    public NotableManager notableManager = new NotableManager();
    public OldEventManager eventManager;

    public TagManager tagManager() {
        return tagManager;
    }

    public NotableManager notableManager() {
        return notableManager;
    }

    public BukkitWorldScriptHelper worldScriptHelper;

    public ItemScriptHelper itemScriptHelper;

    public ExCommandHandler exCommand;

    public final static long startTime = System.currentTimeMillis();

    public DenizenCoreImplementation coreImplementation = new DenizenCoreImplementation();

    public MapTagFlagTracker serverFlagMap;

    /*
     * Sets up Denizen on start of the CraftBukkit server.
     */
    @Override
    public void onEnable() {
        instance = this;
        try {
            versionTag = this.getDescription().getVersion();

            CoreUtilities.noDebugContext = new BukkitTagContext(null, null, null, false, null);
            CoreUtilities.basicContext = new BukkitTagContext(null, null, null, true, null);
            CoreUtilities.errorButNoDebugContext = new BukkitTagContext(null, null, null, false, null);
            CoreUtilities.errorButNoDebugContext.showErrors = true;
            // Load Denizen's core
            DenizenCore.init(coreImplementation);
        }
        catch (Exception e) {
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            startedSuccessful = false;
            return;
        }
        String javaVersion = System.getProperty("java.version");
        if (!javaVersion.startsWith("8") && !javaVersion.startsWith("1.8")) {
            if (javaVersion.startsWith("9") || javaVersion.startsWith("1.9") || javaVersion.startsWith("10") || javaVersion.startsWith("1.10") || javaVersion.startsWith("11")) {
                getLogger().warning("Running unreliable Java version. Minecraft is built for Java 8. Newer Java versions are not guaranteed to function properly (due to changes Oracle made to how reflection works).");
                getLogger().warning("Found java version: " + javaVersion);
            }
            else {
                getLogger().warning("-------------------------------------");
                getLogger().warning("Running incompatible Java version! Minecraft is built for Java 8. Older versions will not work, and newer versions will cause errors (due to Oracle removing reflection support)!");
                getLogger().warning("Found java version: " + javaVersion);
                getLogger().warning("-------------------------------------");
            }
        }
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
            // Activate dependencies
            Depends.initialize();
            if (Depends.citizens == null || !Depends.citizens.isEnabled()) {
                getLogger().warning("Citizens does not seem to be activated! Denizen will have greatly reduced functionality!");
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
        try {
            if (Class.forName("com.destroystokyo.paper.PaperConfig") != null) {
                supportsPaper = true;
            }
        }
        catch (ClassNotFoundException ex) {
            // Ignore.
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
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
            worldScriptHelper = new BukkitWorldScriptHelper();
            itemScriptHelper = new ItemScriptHelper();
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
            // Register all the modern script events
            ScriptEventRegistry.registerMainEvents();
            // Register Core ObjectTags with the ObjectFetcher
            ObjectFetcher.registerCoreObjects();
            CommonRegistries.registerMainObjects();
        }
        catch (Exception e) {
            Debug.echoError(e);
        }
        try {
            // Initialize all properties
            PropertyRegistry.registermainProperties();
        }
        catch (Exception e) {
            Debug.echoError(e);
        }
        if (Settings.packetInterception()) {
            NMSHandler.getInstance().enablePacketInterception(new DenizenPacketHandler());
        }
        try {
            if (supportsPaper) {
                final Class<?> clazz = Class.forName("com.denizenscript.denizen.paper.PaperModule");
                clazz.getMethod("init").invoke(null);
            }
        }
        catch (ClassNotFoundException ex) {
            supportsPaper = false;
        }
        catch (Throwable ex) {
            supportsPaper = false;
            Debug.echoError(ex);
        }
        exCommand = new ExCommandHandler();
        exCommand.enableFor(getCommand("ex"));
        ExSustainedCommandHandler exsCommand = new ExSustainedCommandHandler();
        exsCommand.enableFor(getCommand("exs"));
        // Load script files without processing.
        DenizenCore.preloadScripts();
        // Load the saves.yml into memory
        reloadSaves();
        try {
            // Fire the 'on Server PreStart' world event
            ServerPrestartScriptEvent.instance.specialHackRunEvent();
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
        // Run everything else on the first server tick
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                try {
                    exCommand.processTagList();
                    // Process script files (events, etc).
                    DenizenCore.postLoadScripts();
                    // Synchronize any script commands added while loading scripts.
                    CommandScriptHelper.syncDenizenCommands();
                    // Reload notables from notables.yml into memory
                    notableManager.reloadNotables();
                    Debug.log(ChatColor.LIGHT_PURPLE + "+-------------------------+");
                    // Fire the 'on Server Start' world event
                    ServerStartScriptEvent.instance.fire();
                    worldScriptHelper.serverStartEvent();
                    if (Settings.allowStupidx()) {
                        Debug.echoError("Don't screw with bad config values.");
                        Bukkit.shutdown();
                    }
                    Bukkit.getScheduler().scheduleSyncRepeatingTask(Denizen.this, new Runnable() {
                        @Override
                        public void run() {
                            Debug.outputThisTick = 0;
                            Debug.errorDuplicatePrevention = false;
                            DenizenCore.tick(50); // Sadly, minecraft has no delta timing, so a tick is always 50ms.
                        }
                    }, 1, 1);
                    InventoryTag.setupInventoryTracker();
                }
                catch (Exception e) {
                    Debug.echoError(e);
                }
            }
        }, 1);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (Settings.canRecordStats()) {
                    new StatsRecord().start();
                }
            }
        }.runTaskTimer(this, 100, 20 * 60 * 60);
        new BukkitRunnable() {
            @Override
            public void run() {
                PlayerTag.cleanCache();
            }
        }.runTaskTimer(this, 100, 20 * 60 * 2);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!StrongWarning.recentWarnings.isEmpty()) {
                    StringBuilder warnText = new StringBuilder();
                    warnText.append(ChatColor.YELLOW).append("[Denizen] ").append(ChatColor.RED).append("Recent strong system warnings, scripters need to address ASAP (check earlier console logs for details):");
                    for (StrongWarning warning : StrongWarning.recentWarnings) {
                        warnText.append("\n- ").append(warning.message);
                    }
                    StrongWarning.recentWarnings.clear();
                    Bukkit.getConsoleSender().sendMessage(warnText.toString());
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (player.isOp()) {
                            player.sendMessage(warnText.toString());
                        }
                    }
                }
            }
        }.runTaskTimer(this, 100, 20 * 60 * 5);
    }

    public boolean hasDisabled = false;

    /*
     * Unloads Denizen on shutdown of the server.
     */
    @Override
    public void onDisable() {
        if (!startedSuccessful) {
            return;
        }

        if (hasDisabled) {
            return;
        }
        hasDisabled = true;

        ServerStopScriptEvent.instance.fire();

        // Disable the log interceptor... otherwise bad things on /reload
        /*if (logInterceptor != null) {
            logInterceptor.standardOutput();
        }*/

        // Save notables
        notableManager.saveNotables();

        // Save scoreboards
        ScoreboardHelper._saveScoreboards();

        // Save offline player inventories
        InventoryScriptHelper._savePlayerInventories();

        //Disable core members
        getCommandRegistry().disableCoreMembers();

        getLogger().log(Level.INFO, " v" + getDescription().getVersion() + " disabled.");
        Bukkit.getServer().getScheduler().cancelTasks(this);
        HandlerList.unregisterAll(this);

        saveSaves();
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        Settings.refillCache();
        if (!Settings.showDebug()) {
            getLogger().warning("Debug is disabled in the Denizen config. This is almost always a mistake, and should not be done in the majority of cases.");
        }
        SlowWarning.WARNING_RATE = Settings.warningRate();
    }

    private FileConfiguration scoreboardsConfig = null;
    private File scoreboardsConfigFile = null;

    public void reloadSaves() {
        if (scoreboardsConfigFile == null) {
            scoreboardsConfigFile = new File(getDataFolder(), "scoreboards.yml");
        }
        scoreboardsConfig = YamlConfiguration.loadConfiguration(scoreboardsConfigFile);
        // Reload scoreboards from scoreboards.yml
        ScoreboardHelper._recallScoreboards();
        // Load maps from maps.yml
        DenizenMapManager.reloadMaps();
        // Reload server flags
        File serverFlagsFile = new File(getDataFolder(), "server_flags.dat");
        if (serverFlagsFile.exists()) {
            try {
                FileInputStream fis = new FileInputStream(serverFlagsFile);
                String str = ScriptHelper.convertStreamToString(fis);
                fis.close();
                serverFlagMap = new MapTagFlagTracker(str, CoreUtilities.noDebugContext);
            }
            catch (Throwable ex) {
                Debug.echoError(ex);
                serverFlagMap = new MapTagFlagTracker();
            }
        }
        else {
            serverFlagMap = new MapTagFlagTracker();
        }
        if (new File(getDataFolder(), "saves.yml").exists()) {
            LegacySavesUpdater.updateLegacySaves();
        }
        Bukkit.getServer().getPluginManager().callEvent(new SavesReloadEvent());
    }

    public FileConfiguration getScoreboards() {
        if (scoreboardsConfig == null) {
            reloadSaves();
        }
        return scoreboardsConfig;
    }

    public void saveSaves() {
        // Save notables
        notableManager.saveNotables();
        // Save scoreboards to scoreboards.yml
        ScoreboardHelper._saveScoreboards();
        // Save maps to maps.yml
        DenizenMapManager.saveMaps();
        // Save server flags
        File serverFlagsFile = new File(getDataFolder(), "server_flags.dat");
        try {
            String flagData = serverFlagMap.toString();
            Charset charset = ScriptHelper.encoding == null ? null : ScriptHelper.encoding.charset();
            FileOutputStream fiout = new FileOutputStream(serverFlagsFile);
            OutputStreamWriter writer;
            if (charset == null) {
                writer = new OutputStreamWriter(fiout);
            }
            else {
                writer = new OutputStreamWriter(fiout, charset);
            }
            writer.write(flagData);
            writer.close();
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
        try {
            scoreboardsConfig.save(scoreboardsConfigFile);
        }
        catch (IOException ex) {
            Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE, "Could not save to " + scoreboardsConfigFile, ex);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String cmdName, String[] args) {
        if (cmd.getName().equals("denizenclickable")) {
            if (args.length != 1 || !(sender instanceof Player)) {
                return false;
            }
            UUID id;
            try {
                id = UUID.fromString(args[0]);
            }
            catch (IllegalArgumentException ex) {
                return false;
            }
            ClickableCommand.runClickable(id, (Player) sender);
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
}
