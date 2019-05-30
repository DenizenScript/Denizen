package net.aufdemrand.denizen;

import net.aufdemrand.denizen.events.block.*;
import net.aufdemrand.denizen.events.bukkit.SavesReloadEvent;
import net.aufdemrand.denizen.events.bukkit.ScriptReloadEvent;
import net.aufdemrand.denizen.events.core.CommandSmartEvent;
import net.aufdemrand.denizen.events.core.CuboidEnterExitSmartEvent;
import net.aufdemrand.denizen.events.core.FlagSmartEvent;
import net.aufdemrand.denizen.events.core.NPCNavigationSmartEvent;
import net.aufdemrand.denizen.events.entity.*;
import net.aufdemrand.denizen.events.player.*;
import net.aufdemrand.denizen.events.world.*;
import net.aufdemrand.denizen.flags.FlagManager;
import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.nms.NMSVersion;
import net.aufdemrand.denizen.nms.interfaces.FakeArrow;
import net.aufdemrand.denizen.nms.interfaces.FakePlayer;
import net.aufdemrand.denizen.nms.interfaces.ItemProjectile;
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
import net.aufdemrand.denizen.objects.properties.material.MaterialAge;
import net.aufdemrand.denizen.objects.properties.material.MaterialLevel;
import net.aufdemrand.denizen.objects.properties.trade.*;
import net.aufdemrand.denizen.scripts.commands.BukkitCommandRegistry;
import net.aufdemrand.denizen.scripts.containers.core.*;
import net.aufdemrand.denizen.scripts.triggers.TriggerRegistry;
import net.aufdemrand.denizen.tags.BukkitTagContext;
import net.aufdemrand.denizen.tags.core.*;
import net.aufdemrand.denizen.utilities.*;
import net.aufdemrand.denizen.utilities.blocks.OldMaterialsHelper;
import net.aufdemrand.denizen.utilities.command.CommandManager;
import net.aufdemrand.denizen.utilities.command.Injector;
import net.aufdemrand.denizen.utilities.command.messaging.Messaging;
import net.aufdemrand.denizen.utilities.debugging.StatsRecord;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.depends.Depends;
import net.aufdemrand.denizen.utilities.entity.DenizenEntityType;
import net.aufdemrand.denizen.utilities.maps.DenizenMapManager;
import net.aufdemrand.denizen.utilities.packets.DenizenPacketHandler;
import net.aufdemrand.denizencore.DenizenCore;
import net.aufdemrand.denizencore.DenizenImplementation;
import net.aufdemrand.denizencore.events.OldEventManager;
import net.aufdemrand.denizencore.events.ScriptEvent;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.ObjectFetcher;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.dScript;
import net.aufdemrand.denizencore.objects.properties.PropertyParser;
import net.aufdemrand.denizencore.scripts.ScriptBuilder;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.ScriptHelper;
import net.aufdemrand.denizencore.scripts.ScriptRegistry;
import net.aufdemrand.denizencore.scripts.commands.core.AdjustCommand;
import net.aufdemrand.denizencore.scripts.queues.ScriptQueue;
import net.aufdemrand.denizencore.scripts.queues.core.InstantQueue;
import net.aufdemrand.denizencore.tags.TagContext;
import net.aufdemrand.denizencore.tags.TagManager;
import net.aufdemrand.denizencore.utilities.debugging.Debuggable;
import net.aufdemrand.denizencore.utilities.debugging.SlowWarning;
import net.aufdemrand.denizencore.utilities.debugging.dB.DebugElement;
import net.aufdemrand.denizencore.utilities.text.ConfigUpdater;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.trait.TraitInfo;
import org.apache.commons.lang.StringUtils;
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
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
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
// |   i@<material_name>,<data> - fetches a new item with the specified data (deprecated)
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
// |   cu@<position_1>|<position_2>|... - fetches a new cuboid encompassing a region from position 1 to 2, from 3 to 4, ...
// |   cu@<notable_name> - fetches the cuboid that has been noted with the specified ID
//
// + ----- dEllipsoid ------+
// | object notation: ellipsoid@   can reference unique objects: no      can be notable: yes
// | constructors: ( <>'s represent non-static information and are not literal)
// |   ellipsoid@<x>,<y>,<z>,<world>,<xrad>,<yrad>,<zrad> - fetches a new ellispoid at the position with the given radius
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
// |   m@<material_name>,<data> - fetches the material as specified by Bukkit's material enumeration with specified data (deprecated)
// |   m@<data_variety_material> - fetches the material specified by Denizen's 'data variety' dMaterials (deprecated)
// |   m@random - fetches a random material
//
// + ----- dTrade -----+
// | object notation: trade@    can reference unique objects: no      can be notable: no
// |   trade@trade - represents a generic, customizable merchant trade to be used with merchant trade properties (See <@link language Merchant Trades>)
//
// + ----- dList -------+
// | object notation: li@  can reference unique objects: yes  can be notable: no
// | constructors: ( <>'s represent non-static information and are not literal)
// |   li@<items|...> - fetches a new list with the elements specified, separated by a pipe (|) character
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
// |   d@<low>-<high> - fetches a duration that is randomly selected between the specified 'low' and 'high'
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
// + ----- Custom Object ------+
// | object notation: custom@   can reference unique objects: no      can be notable: no
// | constructors: ( <>'s represent non-static information and are not literal)
// |   custom@<custom_script_name> - fetches a custom object of the specified base custom script.
//
// -->


public class Denizen extends JavaPlugin implements DenizenImplementation {

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
    private dNPCRegistry dNPCRegistry;


    public BukkitCommandRegistry getCommandRegistry() {
        return commandRegistry;
    }

    public dNPCRegistry getNPCRegistry() {
        return dNPCRegistry;
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
            DenizenCore.init(this);

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
            dB.echoError(e);
        }

        try {
            // Populate config.yml if it doesn't yet exist.
            saveDefaultConfig();
            reloadConfig();

            // Startup procedure
            dB.log(ChatColor.LIGHT_PURPLE + "+-------------------------+");
            dB.log(ChatColor.YELLOW + " _/_ _  ._  _ _  ");
            dB.log(ChatColor.YELLOW + "(/(-/ )/ /_(-/ ) " + ChatColor.GRAY + " scriptable minecraft");
            dB.log("");
            dB.log(ChatColor.GRAY + "by: " + ChatColor.WHITE + "mcmonkey, Morphan1, aufdemrand and others");
            dB.log(ChatColor.GRAY + "Chat with us at: " + ChatColor.WHITE + " https://discord.gg/Q6pZGSR");
            dB.log(ChatColor.GRAY + "Or learn more at: " + ChatColor.WHITE + " https://denizenscript.com");
            dB.log(ChatColor.GRAY + "version: " + ChatColor.WHITE + versionTag);
            dB.log(ChatColor.LIGHT_PURPLE + "+-------------------------+");
        }
        catch (Exception e) {
            dB.echoError(e);
        }

        // mcstats.org
        try {
            MetricsLite metrics = new MetricsLite(this);
            metrics.start();
        }
        catch (Exception e) {
            dB.echoError(e);
        }
        // bstats.org
        try {
            BStatsMetricsLite metrics = new BStatsMetricsLite(this);
        }
        catch (Throwable e) {
            dB.echoError(e);
        }

        try {
            // If Citizens is enabled, Create the dNPC Registry
            if (Depends.citizens != null) {
                dNPCRegistry = new dNPCRegistry(this);
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
            if (Depends.vault != null) {
                ScriptRegistry._registerType("economy", EconomyScriptContainer.class);
            }
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
            // Automatic config file update
            InputStream properConfig = Denizen.class.getResourceAsStream("/config.yml");
            String properConfigString = ScriptHelper.convertStreamToString(properConfig);
            properConfig.close();
            FileInputStream currentConfig = new FileInputStream(getDataFolder() + "/config.yml");
            String currentConfigString = ScriptHelper.convertStreamToString(currentConfig);
            currentConfig.close();
            String updated = ConfigUpdater.updateConfig(currentConfigString, properConfigString);
            if (updated != null) {
                dB.log("Your config file is outdated. Automatically updating it...");
                FileOutputStream configOutput = new FileOutputStream(getDataFolder() + "/config.yml");
                OutputStreamWriter writer = new OutputStreamWriter(configOutput);
                writer.write(updated);
                writer.close();
                configOutput.close();
            }
        }
        catch (Exception e) {
            dB.echoError(e);
        }

        try {
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
                CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(SneakingTrait.class).withName("sneaking"));
                CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(InvisibleTrait.class).withName("invisible"));
                CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(MobproxTrait.class).withName("mobprox"));
                CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(MirrorTrait.class).withName("mirror"));

                // Register Speech AI
                CitizensAPI.getSpeechFactory().register(DenizenChat.class, "denizen_chat");
            }
        }
        catch (Exception e) {
            dB.echoError(e);
        }

        // Register Core Members in the Denizen Registries
        try {
            if (Depends.citizens != null) {
                getTriggerRegistry().registerCoreMembers();
            }
        }
        catch (Exception e) {
            dB.echoError(e);
        }

        try {
            AdjustCommand.specialAdjustables.put("server", ServerTags::adjustServer);

            tagManager().registerCoreTags();

            // Objects
            new BiomeTags(this);
            new ChunkTags(this);
            new ColorTags(this);
            new CuboidTags(this);
            new EllipsoidTags(this);
            new EntityTags(this);
            new InventoryTags(this);
            new ItemTags(this);
            new LocationTags(this);
            new MaterialTags(this);
            if (Depends.citizens != null) {
                new NPCTags(this);
            }
            new PlayerTags(this);
            new PluginTags(this);
            new TradeTags(this);
            new WorldTags(this);

            // Other bases
            new ServerTags(this);
            new TextTags(this);
            new ParseTags(this);
            if (Depends.citizens != null) {
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
            eventManager().registerCoreMembers();

            ScriptEvent.registerScriptEvent(new BiomeEnterExitScriptEvent());
            ScriptEvent.registerScriptEvent(new BlockBuiltScriptEvent());
            ScriptEvent.registerScriptEvent(new BlockBurnsScriptEvent());
            ScriptEvent.registerScriptEvent(new BlockDispensesScriptEvent());
            ScriptEvent.registerScriptEvent(new BlockFadesScriptEvent());
            ScriptEvent.registerScriptEvent(new BlockFallsScriptEvent());
            ScriptEvent.registerScriptEvent(new BlockFormsScriptEvent());
            ScriptEvent.registerScriptEvent(new BlockGrowsScriptEvent());
            ScriptEvent.registerScriptEvent(new BlockIgnitesScriptEvent());
            ScriptEvent.registerScriptEvent(new BlockPhysicsScriptEvent());
            ScriptEvent.registerScriptEvent(new BlockSpreadsScriptEvent());
            ScriptEvent.registerScriptEvent(new BrewsScriptEvent());
            ScriptEvent.registerScriptEvent(new ChatScriptEvent());
            ScriptEvent.registerScriptEvent(new ChunkLoadScriptEvent());
            ScriptEvent.registerScriptEvent(new ChunkUnloadScriptEvent());
            ScriptEvent.registerScriptEvent(new CreeperPoweredScriptEvent());
            ScriptEvent.registerScriptEvent(new EntityBreaksHangingScriptEvent());
            ScriptEvent.registerScriptEvent(new EntityBreedScriptEvent());
            ScriptEvent.registerScriptEvent(new EntityChangesBlockScriptEvent());
            ScriptEvent.registerScriptEvent(new EntityCombustsScriptEvent());
            ScriptEvent.registerScriptEvent(new EntityCreatePortalScriptEvent());
            ScriptEvent.registerScriptEvent(new EntityDamagedScriptEvent());
            ScriptEvent.registerScriptEvent(new EntityDeathScriptEvent());
            ScriptEvent.registerScriptEvent(new EntityDespawnScriptEvent());
            ScriptEvent.registerScriptEvent(new EntityEntersPortalScriptEvent());
            ScriptEvent.registerScriptEvent(new EntityEntersVehicleScriptEvent());
            ScriptEvent.registerScriptEvent(new EntityExitsPortalScriptEvent());
            ScriptEvent.registerScriptEvent(new EntityExitsVehicleScriptEvent());
            ScriptEvent.registerScriptEvent(new EntityExplodesScriptEvent());
            ScriptEvent.registerScriptEvent(new EntityExplosionPrimesScriptEvent());
            ScriptEvent.registerScriptEvent(new EntityFoodLevelChangeScriptEvent());
            ScriptEvent.registerScriptEvent(new EntityFormsBlockScriptEvent());
            ScriptEvent.registerScriptEvent(new EntityGlideScriptEvent());
            ScriptEvent.registerScriptEvent(new EntityHealsScriptEvent());
            ScriptEvent.registerScriptEvent(new EntityInteractScriptEvent());
            ScriptEvent.registerScriptEvent(new EntityKilledScriptEvent());
            ScriptEvent.registerScriptEvent(new EntityResurrectScriptEvent());
            ScriptEvent.registerScriptEvent(new EntityShootsBowEvent());
            ScriptEvent.registerScriptEvent(new EntitySpawnerSpawnScriptEvent());
            ScriptEvent.registerScriptEvent(new EntitySpawnScriptEvent());
            if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13_R2)) {
                ScriptEvent.registerScriptEvent(new EntitySwimScriptEvent());
            }
            ScriptEvent.registerScriptEvent(new EntityTamesScriptEvent());
            ScriptEvent.registerScriptEvent(new EntityTargetsScriptEvent());
            ScriptEvent.registerScriptEvent(new EntityTeleportScriptEvent());
            ScriptEvent.registerScriptEvent(new EntityUnleashedScriptEvent());
            ScriptEvent.registerScriptEvent(new FireworkBurstsScriptEvent());
            ScriptEvent.registerScriptEvent(new FurnaceBurnsItemScriptEvent());
            ScriptEvent.registerScriptEvent(new FurnaceSmeltsItemScriptEvent());
            ScriptEvent.registerScriptEvent(new HangingBreaksScriptEvent());
            ScriptEvent.registerScriptEvent(new HorseJumpsScriptEvent());
            ScriptEvent.registerScriptEvent(new InventoryPicksUpItemScriptEvent());
            ScriptEvent.registerScriptEvent(new ItemDespawnsScriptEvent());
            ScriptEvent.registerScriptEvent(new ItemEnchantedScriptEvent());
            ScriptEvent.registerScriptEvent(new ItemMergesScriptEvent());
            ScriptEvent.registerScriptEvent(new ItemMoveScriptEvent());
            ScriptEvent.registerScriptEvent(new ItemRecipeFormedScriptEvent());
            ScriptEvent.registerScriptEvent(new ItemScrollScriptEvent());
            ScriptEvent.registerScriptEvent(new ItemSpawnsScriptEvent());
            ScriptEvent.registerScriptEvent(new LeafDecaysScriptEvent());
            ScriptEvent.registerScriptEvent(new LightningStrikesScriptEvent());
            ScriptEvent.registerScriptEvent(new LingeringPotionSplashScriptEvent());
            ScriptEvent.registerScriptEvent(new LiquidSpreadScriptEvent());
            ScriptEvent.registerScriptEvent(new ListPingScriptEvent());
            ScriptEvent.registerScriptEvent(new PigZappedScriptEvent());
            ScriptEvent.registerScriptEvent(new PistonExtendsScriptEvent());
            ScriptEvent.registerScriptEvent(new PistonRetractsScriptEvent());
            if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13_R2)) {
                ScriptEvent.registerScriptEvent(new PlayerRiptideScriptEvent());
            }
            ScriptEvent.registerScriptEvent(new PlayerAnimatesScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerBreaksBlockScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerBreaksItemScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerChangesGamemodeScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerChangesSignScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerChangesWorldScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerChangesXPScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerClicksBlockScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerClosesInvScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerCompletesAdvancementScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerConsumesScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerCraftsItemScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerDamagesBlockScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerDragsInInvScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerDropsItemScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerEditsBookScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerEmptiesBucketScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerEntersBedScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerEquipsArmorScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerFillsBucketScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerFishesScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerFlyingScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerItemTakesDamageScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerJoinsScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerJumpScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerKickedScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerLeashesEntityScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerLeavesBedScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerLevelsUpScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerLoginScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerMendsItemScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerOpensInvScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerPicksUpScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerPlacesBlockScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerPlacesHangingScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerPreparesAnvilCraftScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerQuitsScriptEvent());
            if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13_R2)) {
                ScriptEvent.registerScriptEvent(new PlayerReceivesCommandsScriptEvent());
            }
            ScriptEvent.registerScriptEvent(new PlayerReceivesMessageScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerRespawnsScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerRightClicksAtEntityScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerSwapsItemsScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerRightClicksEntityScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerShearsScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerSneakScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerSprintScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerStandsOnScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerStatisticIncrementsScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerSteersEntityScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerStepsOnScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerTabCompleteScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerTakesFromFurnaceScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerThrowsEggScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerUsesPortalScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerWalkScriptEvent());
            ScriptEvent.registerScriptEvent(new PlayerWalksOverScriptEvent());
            ScriptEvent.registerScriptEvent(new PortalCreateScriptEvent());
            ScriptEvent.registerScriptEvent(new PotionSplashScriptEvent());
            ScriptEvent.registerScriptEvent(new ProjectileHitsScriptEvent());
            ScriptEvent.registerScriptEvent(new ProjectileLaunchedScriptEvent());
            ScriptEvent.registerScriptEvent(new RedstoneScriptEvent());
            ScriptEvent.registerScriptEvent(new ResourcePackStatusScriptEvent());
            ScriptEvent.registerScriptEvent(new SheepDyedScriptEvent());
            ScriptEvent.registerScriptEvent(new SheepRegrowsScriptEvent());
            ScriptEvent.registerScriptEvent(new SlimeSplitsScriptEvent());
            ScriptEvent.registerScriptEvent(new SpawnChangeScriptEvent());
            ScriptEvent.registerScriptEvent(new StructureGrowsScriptEvent());
            ScriptEvent.registerScriptEvent(new VehicleCollidesBlockScriptEvent());
            ScriptEvent.registerScriptEvent(new VehicleCollidesEntityScriptEvent());
            ScriptEvent.registerScriptEvent(new VehicleCreatedScriptEvent());
            ScriptEvent.registerScriptEvent(new VehicleDamagedScriptEvent());
            ScriptEvent.registerScriptEvent(new VehicleDestroyedScriptEvent());
            ScriptEvent.registerScriptEvent(new VehicleMoveScriptEvent());
            ScriptEvent.registerScriptEvent(new WeatherChangesScriptEvent());
            ScriptEvent.registerScriptEvent(new WorldInitsScriptEvent());
            ScriptEvent.registerScriptEvent(new WorldLoadsScriptEvent());
            ScriptEvent.registerScriptEvent(new WorldSavesScriptEvent());
            ScriptEvent.registerScriptEvent(new WorldUnloadsScriptEvent());


            ObjectFetcher.registerWithObjectFetcher(dBiome.class);     // b@
            dBiome.registerTags(); // TODO: Automate this once all classes have tag registries
            ObjectFetcher.registerWithObjectFetcher(dChunk.class);     // ch@
            dChunk.registerTags(); // TODO: Automate this once all classes have tag registries
            ObjectFetcher.registerWithObjectFetcher(dColor.class);     // co@
            dColor.registerTags(); // TODO: Automate this once all classes have tag registries
            ObjectFetcher.registerWithObjectFetcher(dCuboid.class);    // cu@
            dCuboid.registerTags(); // TODO: Automate this once all classes have tag registries
            ObjectFetcher.registerWithObjectFetcher(dEllipsoid.class); // ellipsoid@
            dEllipsoid.registerTags(); // TODO: Automate this once all classes have tag registries
            ObjectFetcher.registerWithObjectFetcher(dEntity.class);    // e@
            ObjectFetcher.registerWithObjectFetcher(dInventory.class); // in@
            ObjectFetcher.registerWithObjectFetcher(dItem.class);      // i@
            dItem.registerTags(); // TODO: Automate this once all classes have tag registries
            ObjectFetcher.registerWithObjectFetcher(dLocation.class);  // l@
            ObjectFetcher.registerWithObjectFetcher(dMaterial.class);  // m@
            dMaterial.registerTags(); // TODO: Automate this once all classes have tag registries
            if (Depends.citizens != null) {
                ObjectFetcher.registerWithObjectFetcher(dNPC.class);   // n@
            }
            ObjectFetcher.registerWithObjectFetcher(dPlayer.class);    // p@
            ObjectFetcher.registerWithObjectFetcher(dPlugin.class);    // pl@
            dPlugin.registerTags(); // TODO: Automate this once all classes have tag registries
            ObjectFetcher.registerWithObjectFetcher(dTrade.class);     // trade@
            ObjectFetcher.registerWithObjectFetcher(dWorld.class);     // w@
            dWorld.registerTags(); // TODO: Automate this once all classes have tag registries


            // Register Core dObjects with the ObjectFetcher
            ObjectFetcher._registerCoreObjects();
        }
        catch (Exception e) {
            dB.echoError(e);
        }

        try {
            // Initialize old Materials helper
            OldMaterialsHelper._initialize();

            // register properties that add Bukkit code to core objects
            PropertyParser.registerProperty(BukkitScriptProperties.class, dScript.class);
            PropertyParser.registerProperty(BukkitQueueProperties.class, ScriptQueue.class);
            PropertyParser.registerProperty(BukkitElementProperties.class, Element.class);
            PropertyParser.registerProperty(BukkitListProperties.class, dList.class);

            // register core dEntity properties
            PropertyParser.registerProperty(EntityAge.class, dEntity.class);
            PropertyParser.registerProperty(EntityAI.class, dEntity.class);
            PropertyParser.registerProperty(EntityAnger.class, dEntity.class);
            PropertyParser.registerProperty(EntityAngry.class, dEntity.class);
            PropertyParser.registerProperty(EntityAreaEffectCloud.class, dEntity.class);
            PropertyParser.registerProperty(EntityArmorBonus.class, dEntity.class);
            PropertyParser.registerProperty(EntityArrowDamage.class, dEntity.class);
            PropertyParser.registerProperty(EntityInvulnerable.class, dEntity.class);
            PropertyParser.registerProperty(EntityBoatType.class, dEntity.class);
            PropertyParser.registerProperty(EntityArmorPose.class, dEntity.class);
            PropertyParser.registerProperty(EntityArms.class, dEntity.class);
            PropertyParser.registerProperty(EntityBasePlate.class, dEntity.class);
            PropertyParser.registerProperty(EntityBeamTarget.class, dEntity.class);
            PropertyParser.registerProperty(EntityBodyArrows.class, dEntity.class);
            PropertyParser.registerProperty(EntityBoundingBox.class, dEntity.class);
            PropertyParser.registerProperty(EntityChestCarrier.class, dEntity.class);
            PropertyParser.registerProperty(EntityColor.class, dEntity.class);
            PropertyParser.registerProperty(EntityCritical.class, dEntity.class);
            PropertyParser.registerProperty(EntityCustomName.class, dEntity.class);
            PropertyParser.registerProperty(EntityDisabledSlots.class, dEntity.class);
            PropertyParser.registerProperty(EntityPotionEffects.class, dEntity.class);
            PropertyParser.registerProperty(EntityElder.class, dEntity.class);
            PropertyParser.registerProperty(EntityEquipment.class, dEntity.class);
            PropertyParser.registerProperty(EntityExplosionFire.class, dEntity.class);
            PropertyParser.registerProperty(EntityExplosionRadius.class, dEntity.class);
            PropertyParser.registerProperty(EntityFirework.class, dEntity.class);
            PropertyParser.registerProperty(EntityFramed.class, dEntity.class);
            PropertyParser.registerProperty(EntityGravity.class, dEntity.class);
            PropertyParser.registerProperty(EntityHealth.class, dEntity.class);
            PropertyParser.registerProperty(EntityInfected.class, dEntity.class);
            PropertyParser.registerProperty(EntityInventory.class, dEntity.class);
            PropertyParser.registerProperty(EntityIsShowingBottom.class, dEntity.class);
            PropertyParser.registerProperty(EntityItem.class, dEntity.class);
            PropertyParser.registerProperty(EntityJumpStrength.class, dEntity.class);
            PropertyParser.registerProperty(EntityKnockback.class, dEntity.class);
            PropertyParser.registerProperty(EntityMarker.class, dEntity.class);
            PropertyParser.registerProperty(EntityMaxFuseTicks.class, dEntity.class);
            PropertyParser.registerProperty(EntityPainting.class, dEntity.class);
            PropertyParser.registerProperty(EntityPickupStatus.class, dEntity.class);
            PropertyParser.registerProperty(EntityPotion.class, dEntity.class);
            PropertyParser.registerProperty(EntityPowered.class, dEntity.class);
            PropertyParser.registerProperty(EntityProfession.class, dEntity.class);
            if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13_R2)) {
                PropertyParser.registerProperty(EntityRiptide.class, dEntity.class);
            }
            PropertyParser.registerProperty(EntityRotation.class, dEntity.class);
            PropertyParser.registerProperty(EntitySmall.class, dEntity.class);
            PropertyParser.registerProperty(EntitySilent.class, dEntity.class);
            PropertyParser.registerProperty(EntitySitting.class, dEntity.class);
            PropertyParser.registerProperty(EntitySize.class, dEntity.class);
            PropertyParser.registerProperty(EntitySkeleton.class, dEntity.class);
            PropertyParser.registerProperty(EntitySpeed.class, dEntity.class);
            PropertyParser.registerProperty(EntitySpell.class, dEntity.class);
            PropertyParser.registerProperty(EntityTame.class, dEntity.class);
            PropertyParser.registerProperty(EntityTrades.class, dEntity.class);
            PropertyParser.registerProperty(EntityVisible.class, dEntity.class);

            // register core dInventory properties
            PropertyParser.registerProperty(InventoryHolder.class, dInventory.class); // Holder must be loaded first to initiate correctly
            PropertyParser.registerProperty(InventorySize.class, dInventory.class); // Same with size... (too small for contents)
            PropertyParser.registerProperty(InventoryContents.class, dInventory.class);
            PropertyParser.registerProperty(InventoryTitle.class, dInventory.class);

            // register core dItem properties
            PropertyParser.registerProperty(ItemApple.class, dItem.class);
            PropertyParser.registerProperty(ItemBaseColor.class, dItem.class);
            PropertyParser.registerProperty(ItemBook.class, dItem.class);
            PropertyParser.registerProperty(ItemDisplayname.class, dItem.class);
            PropertyParser.registerProperty(ItemDurability.class, dItem.class);
            PropertyParser.registerProperty(ItemCanDestroy.class, dItem.class);
            PropertyParser.registerProperty(ItemCanPlaceOn.class, dItem.class);
            PropertyParser.registerProperty(ItemColor.class, dItem.class);
            if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_14_R1)) {
                PropertyParser.registerProperty(ItemChargedProjectile.class, dItem.class);
            }
            PropertyParser.registerProperty(ItemEnchantments.class, dItem.class);
            PropertyParser.registerProperty(ItemFirework.class, dItem.class);
            PropertyParser.registerProperty(ItemFlags.class, dItem.class);
            PropertyParser.registerProperty(ItemInventory.class, dItem.class);
            PropertyParser.registerProperty(ItemLock.class, dItem.class);
            PropertyParser.registerProperty(ItemLore.class, dItem.class);
            PropertyParser.registerProperty(ItemMap.class, dItem.class);
            PropertyParser.registerProperty(ItemNBT.class, dItem.class);
            PropertyParser.registerProperty(ItemAttributeNBT.class, dItem.class);
            PropertyParser.registerProperty(ItemPatterns.class, dItem.class);
            PropertyParser.registerProperty(ItemPlantgrowth.class, dItem.class);
            PropertyParser.registerProperty(ItemPotion.class, dItem.class);
            PropertyParser.registerProperty(ItemQuantity.class, dItem.class);
            PropertyParser.registerProperty(ItemScript.class, dItem.class);
            PropertyParser.registerProperty(ItemSignContents.class, dItem.class);
            PropertyParser.registerProperty(ItemSkullskin.class, dItem.class);
            PropertyParser.registerProperty(ItemSpawnEgg.class, dItem.class);
            PropertyParser.registerProperty(ItemUnbreakable.class, dItem.class);

            if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13_R2)) {
                // register core dMaterial properties
                PropertyParser.registerProperty(MaterialAge.class, dMaterial.class);
                PropertyParser.registerProperty(MaterialLevel.class, dMaterial.class);
            }

            // register core dTrade properties
            PropertyParser.registerProperty(TradeHasXp.class, dTrade.class);
            PropertyParser.registerProperty(TradeInputs.class, dTrade.class);
            PropertyParser.registerProperty(TradeMaxUses.class, dTrade.class);
            PropertyParser.registerProperty(TradeResult.class, dTrade.class);
            PropertyParser.registerProperty(TradeUses.class, dTrade.class);
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

        if (Settings.packetInterception()) {
            NMSHandler.getInstance().enablePacketInterception(new DenizenPacketHandler());
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

                    dB.log(ChatColor.LIGHT_PURPLE + "+-------------------------+");

                    // Fire the 'on Server Start' world event
                    ws_helper.serverStartEvent();

                    if (Settings.allowStupidx()) {
                        dB.echoError("Don't screw with bad config values.");
                        Bukkit.shutdown();
                    }
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
        HashMap<String, dObject> context = new HashMap<>();
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
        if (savesConfig.contains("a_saves.version")) {
            saves_version = savesConfig.getInt("a_saves.version");
        }

        if (saves_version == 1) {
            dB.log("Updating saves from v1 to v2...");
            ConfigurationSection section = savesConfig.getConfigurationSection("Players");
            if (section != null) {
                ArrayList<String> keyList = new ArrayList<>(section.getKeys(false));
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
                        if (player == null) {
                            dB.log("Warning: can't update listeners for player '" + key + "' - invalid name!");
                        }
                        else // Listeners
                        {
                            savesConfig.createSection("Listeners." + player.getSaveName(), savesConfig.getConfigurationSection("Listeners." + key).getValues(true));
                        }
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
                sender.sendMessage("/ex (-q) <dCommand> (arguments)");
                return true;
            }

            if (Settings.showExHelp()) {
                if (dB.showDebug) {
                    sender.sendMessage(ChatColor.YELLOW + "Executing dCommand... check the console for full debug output!");
                }
                else {
                    sender.sendMessage(ChatColor.YELLOW + "Executing dCommand... to see debug, use /denizen debug");
                }
            }

            entries.add(entry);
            InstantQueue queue = InstantQueue.getQueue(ScriptQueue.getNextId("EXCOMMAND"));
            dNPC npc = null;
            if (Depends.citizens != null && Depends.citizens.getNPCSelector().getSelected(sender) != null) {
                npc = new dNPC(Depends.citizens.getNPCSelector().getSelected(sender));
            }
            List<ScriptEntry> scriptEntries = ScriptBuilder.buildScriptEntries(entries, null,
                    new BukkitScriptEntryData(sender instanceof Player ? new dPlayer((Player) sender) : null, npc));

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

    // --------------------------------------------------------------------------------------
    // ------------------------- Begin Denizen Core Implementations -------------------------
    // --------------------------------------------------------------------------------------

    // <--[language]
    // @name Tick
    // @group Common Terminology
    // @description
    // A 'tick' is usually referred to as 1/20th of a second, the speed at which Minecraft servers update
    // and process everything on them.
    // -->

    @Override
    public File getScriptFolder() {
        File file = null;
        // Get the script directory
        if (Settings.useDefaultScriptPath()) {
            file = new File(DenizenAPI.getCurrentInstance()
                    .getDataFolder() + File.separator + "scripts");
        }
        else {
            file = new File(Settings.getAlternateScriptPath().replace("/", File.separator));
        }
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
        // Remove all registered economy scripts if needed
        if (Depends.vault != null) {
            EconomyScriptContainer.cleanup();
        }
    }

    @Override
    public void onScriptReload() {
        Depends.setupEconomy();
        Bukkit.getServer().getPluginManager().callEvent(new ScriptReloadEvent());
    }

    @Override
    public void buildCoreContainers(net.aufdemrand.denizencore.utilities.YamlConfiguration config) {
        ScriptRegistry._buildCoreYamlScriptContainers(config);
    }

    @Override
    public List<net.aufdemrand.denizencore.utilities.YamlConfiguration> getOutsideScripts() {
        List<net.aufdemrand.denizencore.utilities.YamlConfiguration> files = new ArrayList<>();
        try {
            files.add(ScriptHelper.loadConfig("Denizen.jar/util.dsc", getResource("util.dsc")));
        }
        catch (IOException e) {
            dB.echoError(e);
        }
        return files;
    }

    @Override
    public boolean shouldDebug(Debuggable debug) {
        return dB.shouldDebug(debug);
    }

    @Override
    public void debugQueueExecute(ScriptEntry entry, String queue, String execute) {
        Consumer<String> altDebug = entry.getResidingQueue().debugOutput;
        entry.getResidingQueue().debugOutput = null;
        dB.echoDebug(entry, ChatColor.DARK_GRAY + "Queue '" + queue + "' Executing: " + execute);
        entry.getResidingQueue().debugOutput = altDebug;
    }

    @Override
    public void debugTagFill(Debuggable entry, String tag, String result) {
        dB.echoDebug(entry, ChatColor.DARK_GRAY + "Filled tag <" + ChatColor.WHITE + tag
                + ChatColor.DARK_GRAY + "> with '" + ChatColor.WHITE + result + ChatColor.DARK_GRAY + "'.");
    }

    @Override
    public void debugCommandHeader(ScriptEntry scriptEntry) {
        if (!dB.shouldDebug(scriptEntry)) {
            return;
        }
        if (scriptEntry.getOriginalArguments() == null ||
                scriptEntry.getOriginalArguments().size() == 0 ||
                !scriptEntry.getOriginalArguments().get(0).equals("\0CALLBACK")) {
            if (((BukkitScriptEntryData) scriptEntry.entryData).hasPlayer()) {
                dB.echoDebug(scriptEntry, DebugElement.Header,
                        "Executing dCommand: " + scriptEntry.getCommandName() + "/p@" +
                                ((BukkitScriptEntryData) scriptEntry.entryData).getPlayer().getName());
            }
            else {
                dB.echoDebug(scriptEntry, DebugElement.Header, "Executing dCommand: " +
                        scriptEntry.getCommandName() + (((BukkitScriptEntryData) scriptEntry.entryData).hasNPC() ?
                        "/n@" + ((BukkitScriptEntryData) scriptEntry.entryData).getNPC().getName() : ""));
            }
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
    public boolean needsHandleArgPrefix(String prefix) {
        return prefix.equals("player") || prefix.equals("npc") || prefix.equals("npcid");
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
                        if (FlagManager.playerHasFlag(player, flag)) {
                            return flag_manager.getPlayerFlag(player, flag);
                        }
                        else {
                            dB.echoError("Player '" + owner + "' flag '" + flag + "' not found.");
                        }
                    }
                    else if (Depends.citizens != null && dNPC.matches(owner)) {
                        dNPC npc = dNPC.valueOf(owner);
                        if (FlagManager.npcHasFlag(npc, flag)) {
                            return flag_manager.getNPCFlag(npc.getId(), flag);
                        }
                        else {
                            dB.echoError("NPC '" + owner + "' flag '" + flag + "' not found.");
                        }
                    }
                    else if (dEntity.matches(owner)) {
                        dEntity entity = dEntity.valueOf(owner);
                        if (FlagManager.entityHasFlag(entity, flag)) {
                            return flag_manager.getEntityFlag(entity, flag);
                        }
                        else {
                            dB.echoError("Entity '" + owner + "' flag '" + flag + "' not found.");
                        }
                    }
                }
                else {
                    dB.echoError("Invalid dFlag format: " + string);
                }
            }
            else if (string.indexOf('@') == 2) {
                String flag = string.substring(3);
                if (FlagManager.serverHasFlag(flag)) {
                    return flag_manager.getGlobalFlag(flag);
                }
                else {
                    dB.echoError("Global flag '" + flag + "' not found.");
                }
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

    @Override
    public int getTagTimeout() {
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_14_R1)) {
            return 0;
        }
        return Settings.tagTimeout();
    }

    @Override
    public boolean allowConsoleRedirection() {
        return Settings.allowConsoleRedirection();
    }

    @Override
    public String cleanseLogString(String input) {
        return cleanseLog(input);
    }

    public static String cleanseLog(String input) {
        String esc = String.valueOf((char) 0x1b);
        String repc = String.valueOf(ChatColor.COLOR_CHAR);
        if (input.contains(esc)) {
            input = StringUtils.replace(input, esc + "[0;30;22m", repc + "0");
            input = StringUtils.replace(input, esc + "[0;34;22m", repc + "1");
            input = StringUtils.replace(input, esc + "[0;32;22m", repc + "2");
            input = StringUtils.replace(input, esc + "[0;36;22m", repc + "3");
            input = StringUtils.replace(input, esc + "[0;31;22m", repc + "4");
            input = StringUtils.replace(input, esc + "[0;35;22m", repc + "5");
            input = StringUtils.replace(input, esc + "[0;33;22m", repc + "6");
            input = StringUtils.replace(input, esc + "[0;37;22m", repc + "7");
            input = StringUtils.replace(input, esc + "[0;30;1m", repc + "8");
            input = StringUtils.replace(input, esc + "[0;34;1m", repc + "9");
            input = StringUtils.replace(input, esc + "[0;32;1m", repc + "a");
            input = StringUtils.replace(input, esc + "[0;36;1m", repc + "b");
            input = StringUtils.replace(input, esc + "[0;31;1m", repc + "c");
            input = StringUtils.replace(input, esc + "[0;35;1m", repc + "d");
            input = StringUtils.replace(input, esc + "[0;33;1m", repc + "e");
            input = StringUtils.replace(input, esc + "[0;37;1m", repc + "f");
            input = StringUtils.replace(input, esc + "[5m", repc + "k");
            input = StringUtils.replace(input, esc + "[21m", repc + "l");
            input = StringUtils.replace(input, esc + "[9m", repc + "m");
            input = StringUtils.replace(input, esc + "[4m", repc + "n");
            input = StringUtils.replace(input, esc + "[3m", repc + "o");
            input = StringUtils.replace(input, esc + "[m", repc + "r");
        }
        return input;
    }

    @Override
    public boolean matchesType(String comparable, String comparedto) {

        boolean outcome = false;

        if (comparedto.equalsIgnoreCase("location")) {
            outcome = dLocation.matches(comparable);
        }
        else if (comparedto.equalsIgnoreCase("material")) {
            outcome = dMaterial.matches(comparable);
        }
        else if (comparedto.equalsIgnoreCase("materiallist")) {
            outcome = dList.valueOf(comparable).containsObjectsFrom(dMaterial.class);
        }
        else if (comparedto.equalsIgnoreCase("entity")) {
            outcome = dEntity.matches(comparable);
        }
        else if (comparedto.equalsIgnoreCase("spawnedentity")) {
            outcome = (dEntity.matches(comparable) && dEntity.valueOf(comparable).isSpawned());
        }
        else if (comparedto.equalsIgnoreCase("npc")) {
            outcome = dNPC.matches(comparable);
        }
        else if (comparedto.equalsIgnoreCase("player")) {
            outcome = dPlayer.matches(comparable);
        }
        else if (comparedto.equalsIgnoreCase("offlineplayer")) {
            outcome = (dPlayer.valueOf(comparable) != null && !dPlayer.valueOf(comparable).isOnline());
        }
        else if (comparedto.equalsIgnoreCase("onlineplayer")) {
            outcome = (dPlayer.valueOf(comparable) != null && dPlayer.valueOf(comparable).isOnline());
        }
        else if (comparedto.equalsIgnoreCase("item")) {
            outcome = dItem.matches(comparable);
        }
        else if (comparedto.equalsIgnoreCase("cuboid")) {
            outcome = dCuboid.matches(comparable);
        }
        else if (comparedto.equalsIgnoreCase("trade")) {
            outcome = dTrade.matches(comparable);
        }
        else {
            dB.echoError("Invalid 'matches' type '" + comparedto + "'!");
        }

        return outcome;
    }

    @Override
    public Thread getMainThread() {
        return NMSHandler.getInstance().getMainThread();
    }

    @Override
    public boolean allowedToWebget() {
        return Settings.allowWebget();
    }

    @Override
    public void preTagExecute() {
        try {
            NMSHandler.getInstance().disableAsyncCatcher();
        }
        catch (Throwable e) {
            dB.echoError("Running not-Spigot?!");
        }
    }

    @Override
    public void postTagExecute() {
        try {
            NMSHandler.getInstance().undisableAsyncCatcher();
        }
        catch (Throwable e) {
            dB.echoError("Running not-Spigot?!");
        }
    }

    Boolean tTimeoutSil = null;

    @Override
    public boolean tagTimeoutWhenSilent() {
        if (tTimeoutSil == null) {
            tTimeoutSil = Settings.tagTimeoutSilent();
        }
        return tTimeoutSil;
    }

    @Override
    public boolean getDefaultDebugMode() {
        return Settings.defaultDebugMode();
    }

    @Override
    public boolean canWriteToFile(File f) {
        return Utilities.isSafeFile(f);
    }
}


