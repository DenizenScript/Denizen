package net.aufdemrand.denizen;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.aufdemrand.denizen.events.SavesReloadEvent;
import net.aufdemrand.denizen.flags.FlagManager;
import net.aufdemrand.denizen.listeners.ListenerRegistry;
import net.aufdemrand.denizen.npc.dNPCRegistry;
import net.aufdemrand.denizen.npc.traits.*;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.objects.notable.NotableManager;
import net.aufdemrand.denizen.scripts.*;
import net.aufdemrand.denizen.scripts.commands.CommandRegistry;
import net.aufdemrand.denizen.scripts.containers.core.ItemScriptHelper;
import net.aufdemrand.denizen.scripts.containers.core.WorldScriptHelper;
import net.aufdemrand.denizen.scripts.queues.ScriptEngine;
import net.aufdemrand.denizen.scripts.queues.core.InstantQueue;
import net.aufdemrand.denizen.scripts.requirements.RequirementRegistry;
import net.aufdemrand.denizen.scripts.triggers.TriggerRegistry;
import net.aufdemrand.denizen.tags.ObjectFetcher;
import net.aufdemrand.denizen.tags.TagManager;
import net.aufdemrand.denizen.utilities.RuntimeCompiler;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.DebugElement;
import net.aufdemrand.denizen.utilities.depends.Depends;
import net.aufdemrand.denizen.utilities.packets.PacketHelper;
import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.CitizensAPI;
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
    public final static int configVersion = 2;
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
     * Denizen Managers
     */

    private FlagManager flagManager = new FlagManager(this);
    private TagManager tagManager = new TagManager(this);
    private NotableManager notableManager = new NotableManager();

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

    /*
     * Sets up Denizen on start of the craftbukkit server.
     */

    @Override
    public void onEnable() {
        // Activate dependencies
        depends.initialize();

        if(Depends.citizens == null || !Depends.citizens.isEnabled()) {
            dB.echoError("Citizens does not seem to be activated! Deactivating Denizen!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        } else startedSuccessful = true;

        versionTag = getServer().getPluginManager().getPlugin("Denizen").getDescription().getVersion();
        
        // Startup procedure
        dB.echoDebug(DebugElement.Footer);
        dB.echoDebug(ChatColor.YELLOW + " _/_ _  ._  _ _  ");
        dB.echoDebug(ChatColor.YELLOW + "(/(-/ )/ /_(-/ ) " + ChatColor.GRAY + " scriptable minecraft");
        dB.echoDebug(DebugElement.Spacer);
        dB.echoDebug(ChatColor.GRAY + "by: " + ChatColor.WHITE + "aufdemrand");
        dB.echoDebug(ChatColor.GRAY + "version: "+ ChatColor.WHITE + versionTag);
        dB.echoDebug(DebugElement.Footer);

        // Create the dNPC Registry
        dNPCRegistry = new dNPCRegistry(this);

        // Register commandHandler with Citizens2
        commandHandler = new CommandHandler(Depends.citizens);

        // Register script-container types
        ScriptRegistry._registerCoreTypes();

        // Populate config.yml if it doesn't yet exist.
        saveDefaultConfig();
        reloadConfig();

        // Warn if configuration is outdated / too new
        if (!getConfig().isSet("Config.Version") ||
                getConfig().getInt("Config.Version", 0) != configVersion) {

            dB.echoError("Your Denizen config file is from a different version. " +
                    "Some settings will not be available unless you generate a new one. " +
                    "This is easily done by deleting the current config.yml file in the Denizen folder.");
        }

        ScriptHelper.reloadScripts();
        reloadSaves();

        // Create the command script handler for listener
        WorldScriptHelper ws_helper = new WorldScriptHelper();
        
        ItemScriptHelper is_helper = new ItemScriptHelper();

        // Register traits
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(TriggerTrait.class).withName("triggers"));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(PushableTrait.class).withName("pushable"));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(AssignmentTrait.class).withName("assignment"));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(NicknameTrait.class).withName("nickname"));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(HealthTrait.class).withName("health"));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(ConstantsTrait.class).withName("constants"));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(NameplateTrait.class).withName("nameplate"));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(SittingTrait.class).withName("sitting"));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(FishingTrait.class).withName("fishing"));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(SleepingTrait.class).withName("sleeping"));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(ParticlesTrait.class).withName("particles"));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(SneakingTrait.class).withName("sneaking"));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(InvisibleTrait.class).withName("invisible"));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(MobproxTrait.class).withName("mobprox"));

        // Create instance of PacketHelper if ProtocolLib has been hooked
        if(Depends.protocolManager != null) {
            new PacketHelper(this);
            dB.echoApproval("ProtocolLib hooked, traits and commands with custom packages can be used!");
        }

        // Compile and load Denizen externals
        RuntimeCompiler compiler = new RuntimeCompiler(this);
        compiler.loader();

        // Register Core Members in the Denizen Registries
        getCommandRegistry().registerCoreMembers();
        getTriggerRegistry().registerCoreMembers();
        getRequirementRegistry().registerCoreMembers();
        getListenerRegistry().registerCoreMembers();
        tagManager().registerCoreTags();

        // Register CommandHandler with Citizens
        Depends.citizens.registerCommandClass(CommandHandler.class);

        dB.echoDebug(DebugElement.Footer);



        try {
            // Initialize the ObjectFetcher
            ObjectFetcher.registerWithObjectFetcher(dItem.class);      // i@
            ObjectFetcher.registerWithObjectFetcher(dCuboid.class);    // cu@
            ObjectFetcher.registerWithObjectFetcher(dEntity.class);    // e@
            ObjectFetcher.registerWithObjectFetcher(dInventory.class); // in@
            ObjectFetcher.registerWithObjectFetcher(dColor.class);     // co@
            ObjectFetcher.registerWithObjectFetcher(dList.class);      // li@/fl@
            ObjectFetcher.registerWithObjectFetcher(dLocation.class);  // l@
            ObjectFetcher.registerWithObjectFetcher(dMaterial.class);  // m@
            ObjectFetcher.registerWithObjectFetcher(dNPC.class);       // n@
            ObjectFetcher.registerWithObjectFetcher(dPlayer.class);    // p@
            ObjectFetcher.registerWithObjectFetcher(dScript.class);    // s@
            ObjectFetcher.registerWithObjectFetcher(dWorld.class);     // w@
            ObjectFetcher.registerWithObjectFetcher(Element.class);    // el@
            ObjectFetcher.registerWithObjectFetcher(Duration.class);   // d@
            ObjectFetcher._initialize();

            // Initialize the NotableManager
            NotableManager.registerWithObjectFetcher(dCuboid.class);   // cuboid
            NotableManager._initialize();

        } catch (IOException e) {
            //
        } catch (ClassNotFoundException e) {
            //
        }

        ws_helper.serverStartEvent();
    }

    /*
     * Unloads Denizen on shutdown of the craftbukkit server.
     */

    @Override
    public void onDisable() {
        if(!startedSuccessful) return;

        // Save locations
        dLocation._saveLocations();

        // Deconstruct listeners (server shutdown seems not to be triggering a PlayerQuitEvent)
        for (Player player : this.getServer().getOnlinePlayers())
            getListenerRegistry().deconstructPlayer(dPlayer.mirrorBukkitPlayer(player));

        for (OfflinePlayer player : this.getServer().getOfflinePlayers())
            try {
                getListenerRegistry().deconstructPlayer(dPlayer.mirrorBukkitPlayer(player)); } catch (Exception e) {
                if (player == null) dB.echoDebug("Tell aufdemrand ASAP about this error! ERR: OPN");
                else dB.echoError("'" + player.getName() + "' is having trouble deconstructing! " +
                        "You might have a corrupt player file!");
            }

        //Disable core members
        getCommandRegistry().disableCoreMembers();

        getLogger().log(Level.INFO, " v" + getDescription().getVersion() + " disabled.");
        Bukkit.getServer().getScheduler().cancelTasks(this);
        HandlerList.unregisterAll(this);
        saveSaves();
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
        // Reload dLocations from saves.yml
        dLocation._recallLocations();

        Bukkit.getServer().getPluginManager().callEvent(new SavesReloadEvent());
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
            // Save dLocations to saves.yml
            dLocation._saveLocations();
            savesConfig.save(savesConfigFile);
        } catch (IOException ex) {
            Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE, "Could not save to " + savesConfigFile, ex);
        }
    }

    Citizens citizens;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String cmdName, String[] args) {
        if (citizens == null)
            citizens = (Citizens) getServer().getPluginManager().getPlugin("Citizens");

        // /EX command for console
        if (!(sender instanceof Player) &&
                cmdName.equalsIgnoreCase("ex")) {
            List<String> entries = new ArrayList<String>();
            String entry = "";
            for (String arg : args)
                entry = entry + arg + " ";

            entries.add(entry);
            InstantQueue queue = InstantQueue.getQueue(null);
            List<ScriptEntry> scriptEntries = ScriptBuilder.buildScriptEntries(entries, null,
                    null, null);

            queue.addEntries(scriptEntries);
            queue.start();
            return true;

        } else if ((sender instanceof Player) &&
                cmdName.equalsIgnoreCase("ex")) {
            List<String> entries = new ArrayList<String>();
            String entry = "";
            for (String arg : args)
                entry = entry + arg + " ";

            entries.add(entry);
            InstantQueue queue = InstantQueue.getQueue(null);
            List<ScriptEntry> scriptEntries = ScriptBuilder.buildScriptEntries(entries, null,
                    dPlayer.mirrorBukkitPlayer((Player) sender),
                    dPlayer.mirrorBukkitPlayer((Player) sender).getSelectedNPC());

            queue.addEntries(scriptEntries);
            queue.start();
            return true;
        }

        return citizens.onCommand(sender, cmd, cmdName, args);
    }

}


