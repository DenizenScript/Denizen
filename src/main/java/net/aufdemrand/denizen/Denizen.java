package net.aufdemrand.denizen;

import net.aufdemrand.denizen.events.block.*;
import net.aufdemrand.denizen.events.bukkit.SavesReloadEvent;
import net.aufdemrand.denizen.events.bukkit.ScriptReloadEvent;
import net.aufdemrand.denizen.events.core.*;
import net.aufdemrand.denizen.events.entity.*;
import net.aufdemrand.denizen.events.player.*;
import net.aufdemrand.denizen.events.world.*;
import net.aufdemrand.denizen.flags.FlagManager;
import net.aufdemrand.denizen.listeners.ListenerRegistry;
import net.aufdemrand.denizen.npc.dNPCRegistry;
import net.aufdemrand.denizen.npc.speech.DenizenChat;
import net.aufdemrand.denizen.npc.traits.*;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.objects.notable.NotableManager;
import net.aufdemrand.denizen.objects.properties.bukkit.BukkitElementProperties;
import net.aufdemrand.denizen.objects.properties.bukkit.BukkitListProperties;
import net.aufdemrand.denizen.objects.properties.bukkit.BukkitQueueProperties;
import net.aufdemrand.denizen.objects.properties.bukkit.BukkitScriptProperties;
import net.aufdemrand.denizen.objects.properties.entity.*;
import net.aufdemrand.denizen.objects.properties.inventory.InventoryContents;
import net.aufdemrand.denizen.objects.properties.inventory.InventoryHolder;
import net.aufdemrand.denizen.objects.properties.inventory.InventorySize;
import net.aufdemrand.denizen.objects.properties.inventory.InventoryTitle;
import net.aufdemrand.denizen.objects.properties.item.*;
import net.aufdemrand.denizen.scripts.commands.BukkitCommandRegistry;
import net.aufdemrand.denizen.scripts.containers.core.*;
import net.aufdemrand.denizen.scripts.requirements.RequirementChecker;
import net.aufdemrand.denizen.scripts.requirements.RequirementRegistry;
import net.aufdemrand.denizen.scripts.triggers.TriggerRegistry;
import net.aufdemrand.denizen.tags.BukkitTagContext;
import net.aufdemrand.denizen.tags.core.*;
import net.aufdemrand.denizen.utilities.*;
import net.aufdemrand.denizen.utilities.command.CommandManager;
import net.aufdemrand.denizen.utilities.command.Injector;
import net.aufdemrand.denizen.utilities.command.messaging.Messaging;
import net.aufdemrand.denizen.utilities.debugging.LogInterceptor;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.depends.Depends;
import net.aufdemrand.denizen.utilities.entity.CraftFakeArrow;
import net.aufdemrand.denizen.utilities.entity.CraftFakePlayer;
import net.aufdemrand.denizen.utilities.entity.CraftItemProjectile;
import net.aufdemrand.denizen.utilities.entity.DenizenEntityType;
import net.aufdemrand.denizen.utilities.maps.DenizenMapManager;
import net.aufdemrand.denizen.utilities.packets.intercept.DenizenPacketListener;
import net.aufdemrand.denizencore.DenizenCore;
import net.aufdemrand.denizencore.DenizenImplementation;
import net.aufdemrand.denizencore.events.OldEventManager;
import net.aufdemrand.denizencore.events.ScriptEvent;
import net.aufdemrand.denizencore.interfaces.dExternal;
import net.aufdemrand.denizencore.objects.*;
import net.aufdemrand.denizencore.objects.properties.PropertyParser;
import net.aufdemrand.denizencore.scripts.*;
import net.aufdemrand.denizencore.scripts.queues.ScriptQueue;
import net.aufdemrand.denizencore.scripts.queues.core.InstantQueue;
import net.aufdemrand.denizencore.tags.TagContext;
import net.aufdemrand.denizencore.tags.TagManager;
import net.aufdemrand.denizencore.utilities.debugging.Debuggable;
import net.aufdemrand.denizencore.utilities.debugging.dB.DebugElement;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.trait.TraitInfo;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

// <--[language]
// @name dObjects
// @group Object System
// @description
// dObjects are a system put into place by Denizen that make working with things, or 'objects',
// in Minecraft and Denizen easier. Many parts of scripts will require some kind of object as an
// argument, identifier/type, or such as in world events, part of an event name. The dObjects notation
// system helps both you and Denizen know what type of objects are being referenced and worked with.
//
// So when should you use dObjects? In arguments, event names, replaceable tags, configs, flags, and
// more! If you're just a beginner, you've probably been using them without even realizing it!
//
// dObject is a broader term for a 'type' of object that more specifically represents something,
// such as a dLocation or dScript, often times just referred to as a 'location' or 'script'. Denizen
// employs many object types that you should be familiar with. You'll notice that many times objects
// are reference with their 'dObject notation' which is in the format of 'x@', the x being the specific
// notation of an object type. Example: player objects use the p@ notation, and locations use l@.
// The use of this notation is encouraged, but not always required.
//
// Let's take the tag system, for example. It uses the dObjects system pretty heavily. For instance,
// every time you use <player.name> or <npc.id>, you're using a dObject, which brings us to a simple
// clarification: Why <player.name> and not <p@player.name>? That's because Denizen allows Players,
// NPCs and other 'in-context objects' to be linked to certain scripts. In short, <player> already
// contains a reference to a specific player, such as the player that died in a world event 'on player dies'.
// <p@player.name> would incorrectly reference the player named 'player', however this format is often
// used to help with usage of a tag, simply indicating 'any player object'.
//
// dObjects can be used to CREATE new instances of objects, too! Though not all types allow 'new'
// objects to be created, many do, such as dItems. With the use of tags, it's easy to reference a specific
// item, say -- an item in the Player's hand -- items are also able to use a constructor to make a new item,
// and say, drop it in the world. Take the case of the command/usage '- drop i@diamond_ore'. The item object
// used is a brand new diamond_ore, which is then dropped by the command to a location of your choice -- just
// specify an additional location argument.
//
// There's a great deal more to learn about dObjects, so be sure to check out each object type for more
// specific information. While all dObjects share some features, many contain goodies on top of that!
//
// Here's an overview of each object type that is implemented by the Denizen core:
//
// + ----- dPlayer ----- +
// | object notation: p@    can reference unique objects: yes    can be notable: no
// | constructors: ( <>'s represent non-static information and are not literal)
// |   p@<UUID> - fetches an online or offline player with the specified UUID
// |   p@<player_name> - Outdated constructor for back-support, fetches by name instead of UUID
//
// + ----- dNPC ---------+
// | object notation: n@    can reference unique objects: yes    can be notable: no
// | constructors: ( <>'s represent non-static information and are not literal)
// |   n@<npc_id> - fetches the NPC with the specified ID
// |   n@<npc_name> - fetches the first NPC found with the specified name
//
// + ----- dLocation ----+
// | object notation: l@    can reference unique objects: no     can be notable: yes
// | constructors: ( <>'s represent non-static information and are not literal)
// |   l@<x>,<y>,<z>,<world_name> - fetches a specific location
// |   l@<x>,<y>,<z>,<pitch>,<yaw>,<world_name> - fetches a specific location and direction
// |   l@<notable_location_name> - fetches the location that has been 'noted' with the specified ID
//
// + ----- dEntity ------+
// | object notation: e@    can reference unique objects: yes    can be notable: no
// | constructors: ( <>'s represent non-static information and are not literal)
// |   e@<entity_type> - fetches a new entity with the specified type as implemented by Bukkit's entity type enumeration
// |   e@<entity_type>,<setting> - fetches a new entity of the specified type with a custom setting unique to the type
// |   e@<entity_script_name> - fetches a new custom entity as specified by the referenced entity script (soon)
// |   e@<entity_id> - fetches the entity that has the (temporary) entity ID set by Bukkit
// |   e@random - fetches a new, random entity
//
// + ----- dItem --------+
// | object notation: i@    can reference unique objects: no     can be notable: yes
// | constructors: ( <>'s represent non-static information and are not literal)
// |   i@<material_name> - fetches a new item of the specified material
// |   i@<material_name>,<data> - fetches a new item with the specified data
// |   i@<item_script_name> - fetches a new custom item as specified by the referenced item script
// |   i@<notable_name> - fetches the item that has been noted with the specified ID
//
// + ----- dWorld -------+
// | object notation: w@    can reference unique objects: yes     can be notable: no
// | constructors: ( <>'s represent non-static information and are not literal)
// |   w@<world_name> - fetches the world with the specified name
//
// + ----- dColor -------+
// | object notation: co@    can reference unique objects: no      can be notable: soon
// | constructors: ( <>'s represent non-static information and are not literal)
// |   co@<color_name> - fetches a named color, as implemented by Bukkit's color enumeration
// |   co@<r>,<g>,<b> - fetches a color made of the specified Red,Green,Blue value
// |   co@random - fetches a random color
//
// + ----- dCuboid ------+
// | object notation: cu@   can reference unique objects: no      can be notable: yes
// | constructors: ( <>'s represent non-static information and are not literal)
// |   cu@<position_1>|<position_2>|... - fetches a new cuboid encompassing a region from position 1 to 2, from 2 to 3, ...
// |   cu@<notable_name> - fetches the cuboid that has been noted with the specified ID
//
// + ----- dEllipsoid ------+
// | object notation: ellipsoid@   can reference unique objects: no      can be notable: yes
// | constructors: ( <>'s represent non-static information and are not literal)
// |   ellipsoid@<x>,<y>,<z>,<world>,<xrad>,<yrad>,<zrad>... - fetches a new ellispoid at the position with the given radius
// |   ellipsoid@<notable_name> - fetches the ellipsoid that has been noted with the specified ID
//
// + ----- dChunk ------+
// | object notation: ch@   can reference unique objects: yes      can be notable: no
// | constructors: ( <>'s represent non-static information and are not literal)
// |   ch@<x>,<y>,<world> - fetches a chunk at the given chunk location
//
// + ----- dInventory ---+
// | object notation: in@   can reference unique objects: yes     can be notable: yes
// | constructors: ( <>'s represent non-static information and are not literal)
// |   in@player[holder=<player>] - fetches the specified Player's inventory (Works for offline players)
// |   in@enderchest[holder=<player>] - fetches the specified Player's enderchest inventory (Works for offline players)
// |   in@npc[holder=<npc>] - fetches the specified NPC's inventory
// |   in@entity[holder=<entity>] - fetches the specified object's inventory, such as a Player, NPC, or Mule
// |   in@location[holder=<location>] - fetches the contents of a chest or other 'inventory' block
// |   in@<notable_inventory_name> - fetches the inventory that has been 'noted' with the specified ID
// |   in@<inventory_script_name> - fetches a new custom inventory as specified by the referenced inventory script
// |   in@generic - represents a generic, customizable virtual inventory to be used with inventory properties (See <@link language Virtual Inventories>)
//
// + ----- dMaterial ----+
// | object notation: m@    can reference unique objects: no      can be notable: no
// | constructors: ( <>'s represent non-static information and are not literal)
// |   m@<material_name> - fetches the material as specified by Bukkit's material enumeration
// |   m@<material_name>,<data> - fetches the material as specified by Bukkit's material enumeration with specified data
// |   m@<data_variety_material> - fetches the material specified by Denizen's 'data variety' dMaterials
// |   m@random - fetches a random material
//
// + ----- dList -------+
// | object notation: li@,fl@  can reference unique objects: yes  can be notable: no
// | constructors: ( <>'s represent non-static information and are not literal)
// |   li@<items|...> - fetches a new list with the elements specified, separated by a pipe (|) character
// |   li@val[<items|...>] - slightly more verbose, but tag friendly way to fetch a new list (allows periods)
// |   fl@<server_flag_name> - fetches the flag list value of the specified server flag, as a dList
// |   fl[<player_object/npc_object]@<flag_name> - fetches the flag list value of the specified player/NPC's flag, as a dList
//
// + ----- dScript -------+
// | object notation: s@    can reference unique objects: yes     can be notable: no
// | constructors: ( <>'s represent non-static information and are not literal)
// |   s@<script_container_name> - fetches the script container with the specified name
//
// + ----- Duration ------+
// | object notation: d@    can reference unique objects: no      can be notable: no
// | constructors: ( <>'s represent non-static information and are not literal)
// |   d@<duration> - fetches a duration object with the specified amount of time
// |   d@<low>|<high> - fetches a duration that is randomly selected between the specified 'low' and 'high'
//
// + ----- dPlugin -------+
// | object notation: pl@    can reference unique objects: yes     can be notable: no
// | constructors: ( <>'s represent non-static information and are not literal)
// |   pl@<plugin_name> - fetches the plugin with the specified name
//
// + ----- Element ------+
// | object notation: el@   can reference unique objects: no      can be notable: no
// | constructors: ( <>'s represent non-static information and are not literal)
// |   el@<value> - fetches an element with the specified value
// |   el@val[<value>] - slightly more verbose, but tag friendly way to fetch a new element (allows periods)
//
// + ----- Queue ------+
// | object notation: q@   can reference unique objects: yes      can be notable: no
// | constructors: ( <>'s represent non-static information and are not literal)
// |   q@<id> - fetches the queue with the given ID
//
// -->


public class Denizen extends JavaPlugin implements DenizenImplementation {
    public final static int configVersion = 11;
    public static String versionTag = null;
    private boolean startedSuccessful = false;

    public static final LogInterceptor logInterceptor = new LogInterceptor();


    private CommandManager commandManager;

    public CommandManager getCommandManager() {
        return commandManager;
    }


    /*
     * Denizen Registries
     */
    private BukkitCommandRegistry commandRegistry = new BukkitCommandRegistry();
    private TriggerRegistry triggerRegistry = new TriggerRegistry();
    private RequirementRegistry requirementRegistry = new RequirementRegistry(this);
    private ListenerRegistry listenerRegistry = new ListenerRegistry();
    private dNPCRegistry dNPCRegistry;


    public BukkitCommandRegistry getCommandRegistry() {
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

    public Depends depends = new Depends();

    public RuntimeCompiler runtimeCompiler;

    private BukkitWorldScriptHelper ws_helper;

    public final static long startTime = System.currentTimeMillis();

    private RequirementChecker requirementChecker;

    /**
     * Gets the currently loaded instance of the RequirementChecker
     *
     * @return ScriptHelper
     */
    public RequirementChecker getRequirementChecker() {
        return requirementChecker;
    }

    /*
     * Sets up Denizen on start of the CraftBukkit server.
     */
    @Override
    public void onEnable() {
        try {
            net.minecraft.server.v1_8_R3.Block.getById(0);
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
            org.spigotmc.AsyncCatcher.enabled = false;
        }
        catch (Exception e) {
            dB.echoError("Running not-Spigot?!");
        }

        try {
            versionTag = this.getDescription().getVersion();

            // Load Denizen's core
            DenizenCore.init(this);

            // Activate dependencies
            depends.initialize();

            if (Depends.citizens == null || !Depends.citizens.isEnabled()) {
                getLogger().warning("Citizens does not seem to be activated! Denizen will have greatly reduced functionality!");
                //getServer().getPluginManager().disablePlugin(this);
                //return;
            }
            startedSuccessful = true;

            requirementChecker = new RequirementChecker();

            // Startup procedure
            dB.log(ChatColor.LIGHT_PURPLE + "+-------------------------+");
            dB.log(ChatColor.YELLOW + " _/_ _  ._  _ _  ");
            dB.log(ChatColor.YELLOW + "(/(-/ )/ /_(-/ ) " + ChatColor.GRAY + " scriptable minecraft");
            dB.log("");
            dB.log(ChatColor.GRAY + "by: " + ChatColor.WHITE + "aufdemrand");
            dB.log(ChatColor.GRAY + "version: " + ChatColor.WHITE + versionTag);
            dB.log(ChatColor.LIGHT_PURPLE + "+-------------------------+");
        }
        catch (Exception e) {
            dB.echoError(e);
        }

        try {
            MetricsLite metrics = new MetricsLite(this);
            metrics.start();
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

            // Register DenizenEntityTypes
            DenizenEntityType.registerEntityType("ITEM_PROJECTILE", CraftItemProjectile.class);
            DenizenEntityType.registerEntityType("FAKE_ARROW", CraftFakeArrow.class);
            DenizenEntityType.registerEntityType("FAKE_PLAYER", CraftFakePlayer.class);

            // Track all player names for quick dPlayer matching
            for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
                dPlayer.notePlayer(player);
            }
        }
        catch (Exception e) {
            dB.echoError(e);
        }

        try {
            DenizenCore.setCommandRegistry(getCommandRegistry());
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
            ScriptRegistry._registerType("interact", InteractScriptContainer.class);
            ScriptRegistry._registerType("book", BookScriptContainer.class);
            ScriptRegistry._registerType("item", ItemScriptContainer.class);
            ScriptRegistry._registerType("entity", EntityScriptContainer.class);
            ScriptRegistry._registerType("assignment", AssignmentScriptContainer.class);
            ScriptRegistry._registerType("format", FormatScriptContainer.class);
            ScriptRegistry._registerType("inventory", InventoryScriptContainer.class);
            ScriptRegistry._registerType("command", CommandScriptContainer.class);
            ScriptRegistry._registerType("map", MapScriptContainer.class);
            ScriptRegistry._registerType("version", VersionScriptContainer.class);
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
                    getConfig().getInt("Config.Version", 0) < configVersion) {

                dB.echoError("Your Denizen config file is from an older version. " +
                        "Some settings will not be available unless you generate a new one. " +
                        "This is easily done by stopping the server, deleting the current config.yml file in the Denizen folder " +
                        "and restarting the server.");
            }

            // Create the command script handler for listener
            ws_helper = new BukkitWorldScriptHelper();
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
            if (Depends.citizens != null)
                getTriggerRegistry().registerCoreMembers();
            getRequirementRegistry().registerCoreMembers();
            getListenerRegistry().registerCoreMembers();
        }
        catch (Exception e) {
            dB.echoError(e);
        }

        try {
            tagManager().registerCoreTags();

            new CuboidTags(this);
            new EntityTags(this);
            new LocationTags(this);
            new PlayerTags(this);
            new ServerTags(this);
            new TextTags(this);
            new ParseTags(this);
            if (Depends.citizens != null) {
                new NPCTags(this);
                new AnchorTags(this);
                new ConstantTags(this);
            }
            new FlagTags(this);
            new NotableLocationTags(this);

            eventManager = new OldEventManager();
            // Register all the 'Core' SmartEvents.
            OldEventManager.registerSmartEvent(new CommandSmartEvent());
            OldEventManager.registerSmartEvent(new CuboidEnterExitSmartEvent());
            OldEventManager.registerSmartEvent(new FlagSmartEvent());
            OldEventManager.registerSmartEvent(new NPCNavigationSmartEvent());
            OldEventManager.registerSmartEvent(new PlayerEquipsArmorSmartEvent());
            eventManager().registerCoreMembers();

            ScriptEvent.registerScriptEvent(new BiomeEnterExitScriptEvent());
            ScriptEvent.registerScriptEvent(new BlockBuiltScriptEvent());
            ScriptEvent.registerScriptEvent(new BlockBurnsScriptEvent());
            ScriptEvent.registerScriptEvent(new BlockDispensesScriptEvent());
            ScriptEvent.registerScriptEvent(new BlockFadesScriptEvent());
            ScriptEvent.registerScriptEvent(new BlockFallsScriptEvent());
            ScriptEvent.registerScriptEvent(new BlockFormsScriptEvent());
            ScriptEvent.registerScriptEvent(new BlockGrowsScriptEvent());
            ScriptEvent.registerScriptEvent(new BlockSpreadsScriptEvent());
            ScriptEvent.registerScriptEvent(new BlockIgnitesScriptEvent());
            ScriptEvent.registerScriptEvent(new BlockPhysicsScriptEvent());
            ScriptEvent.registerScriptEvent(new BrewsScriptEvent());
            ScriptEvent.registerScriptEvent(new BucketEmptyScriptEvent());
            ScriptEvent.registerScriptEvent(new BucketFillScriptEvent());
            ScriptEvent.registerScriptEvent(new ChatScriptEvent());
            ScriptEvent.registerScriptEvent(new ChunkLoadScriptEvent());
            ScriptEvent.registerScriptEvent(new ChunkUnloadScriptEvent());
            ScriptEvent.registerScriptEvent(new CreeperPoweredScriptEvent());
            ScriptEvent.registerScriptEvent(new EntityBreaksHangingScriptEvent());
            ScriptEvent.registerScriptEvent(new EntityChangesBlockScriptEvent());
            ScriptEvent.registerScriptEvent(new EntityCreatePortalScriptEvent());
            ScriptEvent.registerScriptEvent(new EntityCombustsScriptEvent());
            ScriptEvent.registerScriptEvent(new EntityDamagedScriptEvent());
            ScriptEvent.registerScriptEvent(new EntityDeathScriptEvent());
            ScriptEvent.registerScriptEvent(new EntityDespawnScriptEvent());
            ScriptEvent.registerScriptEvent(new EntityEntersPortalScriptEvent());
            ScriptEvent.registerScriptEvent(new EntityExitsPortalScriptEvent());
            ScriptEvent.registerScriptEvent(new EntityExplodesScriptEvent());
            ScriptEvent.registerScriptEvent(new EntityExplosionPrimesScriptEvent());
            ScriptEvent.registerScriptEvent(new EntityFoodLevelChangeScriptEvent());
            ScriptEvent.registerScriptEvent(new EntityFormsBlockScriptEvent());
            ScriptEvent.registerScriptEvent(new EntityHealsScriptEvent());
            ScriptEvent.registerScriptEvent(new EntityInteractScriptEvent());
            ScriptEvent.registerScriptEvent(new EntityKilledScriptEvent());
            ScriptEvent.registerScriptEvent(new EntityShootsBowEvent());
            ScriptEvent.registerScriptEvent(new EntitySpawnScriptEvent());
            ScriptEvent.registerScriptEvent(new EntityTamesScriptEvent());
            ScriptEvent.registerScriptEvent(new EntityTargetsScriptEvent());
            ScriptEvent.registerScriptEvent(new EntityTeleportScriptEvent());
            ScriptEvent.registerScriptEvent(new EntityUnleashedScriptEvent());
            ScriptEvent.registerScriptEvent(new FurnaceBurnsItemScriptEvent());
            ScriptEvent.registerScriptEvent(new FurnaceSmeltsItemScriptEvent());
            ScriptEvent.registerScriptEvent(new HangingBreaksScriptEvent());
            ScriptEvent.registerScriptEvent(new HorseJumpsScriptEvent());
            ScriptEvent.registerScriptEvent(new InvPicksUpItemScriptEvent());
            ScriptEvent.registerScriptEvent(new ItemDespawnsScriptEvent());
            ScriptEvent.registerScriptEvent(new ItemEnchantedScriptEvent());
            ScriptEvent.registerScriptEvent(new ItemMoveScriptEvent());
            ScriptEvent.registerScriptEvent(new ItemScrollScriptEvent());
            ScriptEvent.registerScriptEvent(new ItemSpawnsScriptEvent());
            ScriptEvent.registerScriptEvent(new LeafDecaysScriptEvent());
            ScriptEvent.registerScriptEvent(new LightningStrikesScriptEvent());
            ScriptEvent.registerScriptEvent(new LiquidSpreadScriptEvent());
            ScriptEvent.registerScriptEvent(new ListPingScriptEvent());
            ScriptEvent.registerScriptEvent(new PigZappedScriptEvent());
            ScriptEvent.registerScriptEvent(new PistonExtendsScriptEvent());
            ScriptEvent.registerScriptEvent(new PistonRetractsScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerAnimatesScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerBreaksBlockScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerChangesGamemodeScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerChangesSignScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerChangesWorldScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerChangesXPScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerClosesInvScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerConsumesScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerDamagesBlockScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerDropsItemScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerEntersBedScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerFishesScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerJoinsScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerJumpScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerKickedScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerLeashesScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerLeavesBedScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerLevelsScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerOpensInvScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerPicksUpScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerPlacesBlockScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerPlacesHangingScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerStepsOnScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerTakesFromFurnaceScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerThrowsEggScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerUsesPortalScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerWalkScriptEvent());
            ScriptEvent.registerScriptEvent(new PortalCreateScriptEvent());
            ScriptEvent.registerScriptEvent(new ProjectileLaunchedScriptEvent());
            ScriptEvent.registerScriptEvent(new PotionSplashScriptEvent());
            ScriptEvent.registerScriptEvent(new RedstoneScriptEvent());
            ScriptEvent.registerScriptEvent(new ResourcePackStatusScriptEvent());
            ScriptEvent.registerScriptEvent(new SheepDyedScriptEvent());
            ScriptEvent.registerScriptEvent(new SheepRegrowsScriptEvent());
            ScriptEvent.registerScriptEvent(new SpawnChangeScriptEvent());
            ScriptEvent.registerScriptEvent(new StructureGrowsScriptEvent());
            ScriptEvent.registerScriptEvent(new SlimeSplitsScriptEvent());
            ScriptEvent.registerScriptEvent(new VehicleCollidesBlockScriptEvent());
            ScriptEvent.registerScriptEvent(new VehicleCollidesEntityScriptEvent());
            ScriptEvent.registerScriptEvent(new VehicleMoveScriptEvent());
            ScriptEvent.registerScriptEvent(new WeatherChangesScriptEvent());
            ScriptEvent.registerScriptEvent(new WorldInitsScriptEvent());
            ScriptEvent.registerScriptEvent(new WorldLoadsScriptEvent());
            ScriptEvent.registerScriptEvent(new WorldSavesScriptEvent());
            ScriptEvent.registerScriptEvent(new WorldUnloadsScriptEvent());


            ObjectFetcher.registerWithObjectFetcher(dItem.class);      // i@
            ObjectFetcher.registerWithObjectFetcher(dCuboid.class);    // cu@
            ObjectFetcher.registerWithObjectFetcher(dEntity.class);    // e@
            ObjectFetcher.registerWithObjectFetcher(dInventory.class); // in@
            ObjectFetcher.registerWithObjectFetcher(dColor.class);     // co@
            ObjectFetcher.registerWithObjectFetcher(dLocation.class);  // l@
            ObjectFetcher.registerWithObjectFetcher(dMaterial.class);  // m@
            if (Depends.citizens != null)
                ObjectFetcher.registerWithObjectFetcher(dNPC.class);   // n@
            ObjectFetcher.registerWithObjectFetcher(dPlayer.class);    // p@
            ObjectFetcher.registerWithObjectFetcher(dWorld.class);     // w@
            ObjectFetcher.registerWithObjectFetcher(dChunk.class);     // ch@
            ObjectFetcher.registerWithObjectFetcher(dPlugin.class);    // pl@
            ObjectFetcher.registerWithObjectFetcher(dEllipsoid.class); // ellipsoid@
            ObjectFetcher.registerWithObjectFetcher(dBiome.class);     // b@

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
            // register properties that add Bukkit code to core objects
            propertyParser.registerProperty(BukkitScriptProperties.class, dScript.class);
            propertyParser.registerProperty(BukkitQueueProperties.class, ScriptQueue.class);
            propertyParser.registerProperty(BukkitElementProperties.class, Element.class);
            propertyParser.registerProperty(BukkitListProperties.class, dList.class);

            // register core dEntity properties
            propertyParser.registerProperty(EntityAge.class, dEntity.class);
            propertyParser.registerProperty(EntityAI.class, dEntity.class);
            propertyParser.registerProperty(EntityAngry.class, dEntity.class);
            propertyParser.registerProperty(EntityChestCarrier.class, dEntity.class);
            propertyParser.registerProperty(EntityColor.class, dEntity.class);
            propertyParser.registerProperty(EntityCritical.class, dEntity.class);
            propertyParser.registerProperty(EntityElder.class, dEntity.class);
            propertyParser.registerProperty(EntityEquipment.class, dEntity.class);
            propertyParser.registerProperty(EntityFirework.class, dEntity.class);
            propertyParser.registerProperty(EntityFramed.class, dEntity.class);
            propertyParser.registerProperty(EntityHealth.class, dEntity.class);
            propertyParser.registerProperty(EntityInfected.class, dEntity.class);
            propertyParser.registerProperty(EntityInventory.class, dEntity.class);
            propertyParser.registerProperty(EntityItem.class, dEntity.class);
            propertyParser.registerProperty(EntityJumpStrength.class, dEntity.class);
            propertyParser.registerProperty(EntityKnockback.class, dEntity.class);
            propertyParser.registerProperty(EntityPainting.class, dEntity.class);
            propertyParser.registerProperty(EntityPotion.class, dEntity.class);
            propertyParser.registerProperty(EntityPowered.class, dEntity.class);
            propertyParser.registerProperty(EntityProfession.class, dEntity.class);
            propertyParser.registerProperty(EntityRotation.class, dEntity.class);
            propertyParser.registerProperty(EntitySitting.class, dEntity.class);
            propertyParser.registerProperty(EntitySize.class, dEntity.class);
            propertyParser.registerProperty(EntitySkeleton.class, dEntity.class);
            propertyParser.registerProperty(EntitySpeed.class, dEntity.class);
            propertyParser.registerProperty(EntityTame.class, dEntity.class);

            // register core dInventory properties
            propertyParser.registerProperty(InventoryHolder.class, dInventory.class); // Holder must be loaded first to initiate correctly
            propertyParser.registerProperty(InventorySize.class, dInventory.class); // Same with size...(Too small for contents)
            propertyParser.registerProperty(InventoryContents.class, dInventory.class);
            propertyParser.registerProperty(InventoryTitle.class, dInventory.class);

            // register core dItem properties
            propertyParser.registerProperty(ItemApple.class, dItem.class);
            propertyParser.registerProperty(ItemBaseColor.class, dItem.class);
            propertyParser.registerProperty(ItemBook.class, dItem.class);
            propertyParser.registerProperty(ItemDisplayname.class, dItem.class);
            propertyParser.registerProperty(ItemDurability.class, dItem.class);
            propertyParser.registerProperty(ItemDye.class, dItem.class);
            propertyParser.registerProperty(ItemEnchantments.class, dItem.class);
            propertyParser.registerProperty(ItemFirework.class, dItem.class);
            propertyParser.registerProperty(ItemFlags.class, dItem.class);
            propertyParser.registerProperty(ItemLore.class, dItem.class);
            propertyParser.registerProperty(ItemMap.class, dItem.class);
            propertyParser.registerProperty(ItemPatterns.class, dItem.class);
            propertyParser.registerProperty(ItemPlantgrowth.class, dItem.class);
            propertyParser.registerProperty(ItemPotion.class, dItem.class);
            propertyParser.registerProperty(ItemQuantity.class, dItem.class);
            propertyParser.registerProperty(ItemSkullskin.class, dItem.class);
            propertyParser.registerProperty(ItemSpawnEgg.class, dItem.class);
        }
        catch (Exception e) {
            dB.echoError(e);
        }

        try {
            for (World world : getServer().getWorlds()) {
                EntityScriptHelper.linkWorld(world);
            }
        }
        catch (Exception e) {
            dB.echoError(e);
        }

        // Enable custom inbound packet listener
        DenizenPacketListener.enable();

        // Run everything else on the first server tick
        getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                try {
                    DenizenCore.loadScripts();

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

        getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                DenizenCore.tick(50); // Sadly, minecraft has no delta timing, so a tick is always 50ms.
            }
        }, 1, 1);
    }


    /*
     * Unloads Denizen on shutdown of the craftbukkit server.
     */
    @Override
    public void onDisable() {
        if (!startedSuccessful) return;

        // <--[event]
        // @Events
        // shutdown
        //
        // @Warning not all plugins will be loaded and delayed scripts will be dropped.
        //
        // @Triggers when the server is shutting down.
        //
        // @Context
        // None.
        //
        // -->
        HashMap<String, dObject> context = new HashMap<String, dObject>();
        OldEventManager.doEvents(Arrays.asList("shutdown"), new BukkitScriptEntryData(null, null), context);

        // Disable the log interceptor... otherwise bad things on /reload
        logInterceptor.standardOutput();

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
                getListenerRegistry().deconstructPlayer(dPlayer.mirrorBukkitPlayer(player));
            }
            catch (Exception e) {
                if (player == null)
                    dB.echoError("Tell the Denizen team ASAP about this error! ERR: OPN: " + e.toString());
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

        for (World world : getServer().getWorlds()) {
            EntityScriptHelper.unlinkWorld(world);
        }

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

        // Load maps from maps.yml
        DenizenMapManager.reloadMaps();

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
                for (String key : section.getKeys(false)) {
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
            List<Object> entries = new ArrayList<Object>();
            String entry = "";
            for (String arg : args)
                entry = entry + arg + " ";

            if (entry.length() < 2) {
                sender.sendMessage("/ex <dCommand> (arguments)");
                return true;
            }

            if (Settings.showExHelp()) {
                if (dB.showDebug)
                    sender.sendMessage(ChatColor.YELLOW + "Executing dCommand... check the console for debug output!");
                else
                    sender.sendMessage(ChatColor.YELLOW + "Executing dCommand... to see debug, use /denizen debug");
            }

            entries.add(entry);
            InstantQueue queue = InstantQueue.getQueue(ScriptQueue.getNextId("EXCOMMAND"));
            dNPC npc = null;
            if (Depends.citizens != null && Depends.citizens.getNPCSelector().getSelected(sender) != null)
                npc = new dNPC(Depends.citizens.getNPCSelector().getSelected(sender));
            List<ScriptEntry> scriptEntries = ScriptBuilder.buildScriptEntries(entries, null,
                    new BukkitScriptEntryData(sender instanceof Player ? new dPlayer((Player) sender) : null, npc));

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

    @Override
    public File getScriptFolder() {
        File file = null;
        // Get the script directory
        if (Settings.useDefaultScriptPath())
            file = new File(DenizenAPI.getCurrentInstance()
                    .getDataFolder() + File.separator + "scripts");
        else
            file = new File(Settings.getAlternateScriptPath().replace("/", File.separator));
        return file;
    }

    @Override
    public String getImplementationVersion() {
        return versionTag;
    }

    @Override
    public void debugMessage(String message) {
        dB.log(message);
    }

    @Override
    public void debugException(Throwable ex) {
        dB.echoError(ex);
    }

    @Override
    public void debugError(String error) {
        dB.echoError(error);
    }

    @Override
    public void debugError(ScriptQueue scriptQueue, String s) {
        dB.echoError(scriptQueue, s);
    }

    @Override
    public void debugError(ScriptQueue scriptQueue, Throwable throwable) {
        dB.echoError(scriptQueue, throwable);
    }

    @Override
    public void debugReport(Debuggable debuggable, String s, String s1) {
        dB.report(debuggable, s, s1);
    }

    @Override
    public void debugApproval(String message) {
        dB.echoApproval(message);
    }

    @Override
    public void debugEntry(Debuggable debuggable, String s) {
        dB.echoDebug(debuggable, s);
    }

    @Override
    public void debugEntry(Debuggable debuggable, DebugElement debugElement, String s) {
        dB.echoDebug(debuggable, debugElement, s);
    }

    @Override
    public void debugEntry(Debuggable debuggable, DebugElement debugElement) {
        dB.echoDebug(debuggable, debugElement);
    }

    @Override
    public String getImplementationName() {
        return "Bukkit";
    }

    @Override
    public void preScriptReload() {
        // Remove all recipes added by Denizen item scripts
        ItemScriptHelper.removeDenizenRecipes();
        // Remove all registered commands added by Denizen command scripts
        CommandScriptHelper.removeDenizenCommands();
    }

    @Override
    public void onScriptReload() {
        Bukkit.getServer().getPluginManager().callEvent(new ScriptReloadEvent());
    }

    @Override
    public void buildCoreContainers(net.aufdemrand.denizencore.utilities.YamlConfiguration config) {
        ScriptRegistry._buildCoreYamlScriptContainers(config);
    }

    @Override
    public List<net.aufdemrand.denizencore.utilities.YamlConfiguration> getOutsideScripts() {
        List<net.aufdemrand.denizencore.utilities.YamlConfiguration> files = new ArrayList<net.aufdemrand.denizencore.utilities.YamlConfiguration>();
        try {
            files.add(ScriptHelper.loadConfig("Denizen.jar/util.dscript", getResource("util.dscript")));
        }
        catch (IOException e) {
            dB.echoError(e);
        }
        return files;
    }

    @Override
    public void handleCommandSpecialCases(ScriptEntry scriptEntry) {
        if (((BukkitScriptEntryData) scriptEntry.entryData).hasNPC()
                && ((BukkitScriptEntryData) scriptEntry.entryData).getNPC().getCitizen() == null)
            ((BukkitScriptEntryData) scriptEntry.entryData).setNPC(null);
    }

    @Override
    public void debugCommandHeader(ScriptEntry scriptEntry) {
        if (scriptEntry.getOriginalArguments() == null ||
                scriptEntry.getOriginalArguments().size() == 0 ||
                !scriptEntry.getOriginalArguments().get(0).equals("\0CALLBACK")) {
            if (((BukkitScriptEntryData) scriptEntry.entryData).getPlayer() != null)
                dB.echoDebug(scriptEntry, DebugElement.Header,
                        "Executing dCommand: " + scriptEntry.getCommandName() + "/p@" +
                                ((BukkitScriptEntryData) scriptEntry.entryData).getPlayer().getName());
            else
                dB.echoDebug(scriptEntry, DebugElement.Header, "Executing dCommand: " +
                        scriptEntry.getCommandName() + (((BukkitScriptEntryData) scriptEntry.entryData).getNPC() != null ?
                        "/n@" + ((BukkitScriptEntryData) scriptEntry.entryData).getNPC().getName() : ""));
        }
    }

    @Override
    public TagContext getTagContextFor(ScriptEntry scriptEntry, boolean b) {
        dPlayer player = scriptEntry != null ? ((BukkitScriptEntryData) scriptEntry.entryData).getPlayer() : null;
        dNPC npc = scriptEntry != null ? ((BukkitScriptEntryData) scriptEntry.entryData).getNPC() : null;
        return new BukkitTagContext(player, npc, b, scriptEntry,
                scriptEntry != null ? scriptEntry.shouldDebug() : true,
                scriptEntry != null ? scriptEntry.getScript() : null);
    }

    @Override
    public boolean handleCustomArgs(ScriptEntry scriptEntry, aH.Argument arg, boolean if_ignore) {
        // Fill player/off-line player
        if (arg.matchesPrefix("player") && !if_ignore) {
            dB.echoDebug(scriptEntry, "...replacing the linked player with " + arg.getValue());
            String value = TagManager.tag(arg.getValue(), new BukkitTagContext(scriptEntry, false));
            dPlayer player = dPlayer.valueOf(value);
            if (player == null || !player.isValid()) {
                dB.echoError(scriptEntry.getResidingQueue(), value + " is an invalid player!");
            }
            ((BukkitScriptEntryData) scriptEntry.entryData).setPlayer(player);
            return true;
        }

        // Fill NPCID/NPC argument
        else if (arg.matchesPrefix("npc, npcid") && !if_ignore) {
            dB.echoDebug(scriptEntry, "...replacing the linked NPC with " + arg.getValue());
            String value = TagManager.tag(arg.getValue(), new BukkitTagContext(scriptEntry, false));
            dNPC npc = dNPC.valueOf(value);
            if (npc == null || !npc.isValid()) {
                dB.echoError(scriptEntry.getResidingQueue(), value + " is an invalid NPC!");
                return false;
            }
            ((BukkitScriptEntryData) scriptEntry.entryData).setNPC(npc);
            return true;
        }
        return false;
    }

    @Override
    public void refreshScriptContainers() {
        VersionScriptContainer.scripts.clear();
        ItemScriptHelper.item_scripts.clear();
        ItemScriptHelper.item_scripts_by_hash_id.clear();
        InventoryScriptHelper.inventory_scripts.clear();
        tT = Integer.MAX_VALUE;
    }

    @Override
    public String scriptQueueSpeed() {
        return Settings.scriptQueueSpeed();
    }

    @Override
    public dList valueOfFlagdList(String string) {
        FlagManager.Flag flag = getFlag(string);
        if (flag == null) {
            return null;
        }
        return new dList(flag.toString(), true, flag.values());
    }

    public FlagManager.Flag getFlag(String string) {
        if (string.startsWith("fl")) {
            FlagManager flag_manager = DenizenAPI.getCurrentInstance().flagManager();
            if (string.indexOf('[') == 2) {
                int cb = string.indexOf(']');
                if (cb > 4) {
                    String owner = string.substring(3, cb);
                    String flag = string.substring(cb + 2);
                    if (dPlayer.matches(owner)) {
                        dPlayer player = dPlayer.valueOf(owner);
                        if (FlagManager.playerHasFlag(player, flag))
                            return flag_manager.getPlayerFlag(player, flag);
                        else
                            dB.echoError("Player '" + owner + "' flag '" + flag + "' not found.");
                    }
                    else if (Depends.citizens != null && dNPC.matches(owner)) {
                        dNPC npc = dNPC.valueOf(owner);
                        if (FlagManager.npcHasFlag(npc, flag))
                            return flag_manager.getNPCFlag(npc.getId(), flag);
                        else
                            dB.echoError("NPC '" + owner + "' flag '" + flag + "' not found.");
                    }
                    else if (dEntity.matches(owner)) {
                        dEntity entity = dEntity.valueOf(owner);
                        if (FlagManager.entityHasFlag(entity, flag))
                            return flag_manager.getEntityFlag(entity, flag);
                        else
                            dB.echoError("Entity '" + owner + "' flag '" + flag + "' not found.");
                    }
                }
                else {
                    dB.echoError("Invalid dFlag format: " + string);
                }
            }
            else if (string.indexOf('@') == 2) {
                String flag = string.substring(3);
                if (FlagManager.serverHasFlag(flag))
                    return flag_manager.getGlobalFlag(flag);
                else
                    dB.echoError("Global flag '" + flag + "' not found.");
            }
        }
        return null;
    }

    @Override
    public boolean matchesFlagdList(String arg) {
        boolean flag = false;
        if (arg.startsWith("fl")) {
            if (arg.indexOf('[') == 2) {
                int cb = arg.indexOf(']');
                if (cb > 4 && arg.indexOf('@') == (cb + 1)) {
                    String owner = arg.substring(3, cb);
                    flag = arg.substring(cb + 2).length() > 0 && (dPlayer.matches(owner)
                            || (Depends.citizens != null && dNPC.matches(owner)));
                }
            }
            else if (arg.indexOf('@') == 2) {
                flag = arg.substring(3).length() > 0;
            }
        }
        return flag;
    }

    @Override
    public String getLastEntryFromFlag(String flag) {
        FlagManager.Flag theflag = getFlag(flag);
        if (theflag == null || theflag.getLast() == null) {
            return null;
        }
        return theflag.getLast().asString();
    }

    @Override
    public TagContext getTagContext(ScriptEntry scriptEntry) {
        return new BukkitTagContext(scriptEntry, false);
    }

    @Override
    public ScriptEntryData getEmptyScriptEntryData() {
        return new BukkitScriptEntryData(null, null);
    }

    int tT = Integer.MAX_VALUE;

    @Override
    public int getTagTimeout() {
        if (tT == Integer.MAX_VALUE) {
            tT = Settings.tagTimeout();
        }
        return tT;
    }
}


