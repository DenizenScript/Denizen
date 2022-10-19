package com.denizenscript.denizen.utilities;

import com.denizenscript.denizen.objects.*;
import com.denizenscript.denizen.tags.core.*;
import com.denizenscript.denizencore.objects.ObjectType;
import com.denizenscript.denizencore.utilities.CoreConfiguration;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.depends.Depends;
import com.denizenscript.denizencore.objects.ObjectFetcher;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.notable.NoteManager;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.debugging.DebugInternals;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.plugin.Plugin;

public class CommonRegistries {

    // <--[language]
    // @name ObjectTags
    // @group Object System
    // @description
    // ObjectTags are a system put into place by Denizen that make working with things, or 'objects',
    // in Minecraft and Denizen easier. Many parts of scripts will require some kind of object as an
    // argument, identifier/type, or such as in world events, part of an event name. The ObjectTags notation
    // system helps both you and Denizen know what type of objects are being referenced and worked with.
    //
    // So when should you use ObjectTags? In arguments, event names, replaceable tags, configs, flags, and
    // more! If you're just a beginner, you've probably been using them without even realizing it!
    //
    // ObjectTag is a broader term for a 'type' of object that more specifically represents something,
    // such as a LocationTag or ScriptTag, often times just referred to as a 'location' or 'script'. Denizen
    // employs many object types that you should be familiar with. You'll notice that many times objects
    // are referenced with their 'ObjectTag notation' which is in the format of 'x@', the x being the specific
    // notation of an object type. Example: player objects use the p@ notation, and locations use l@.
    // This notation is automatically generated when directly displaying objects, or saving them into data files.
    // It should never be manually typed into a script.
    //
    // Let's take the tag system, for example. It uses the ObjectTags system pretty heavily. For instance,
    // every time you use <player.name> or <npc.id>, you're using a ObjectTag, which brings us to a simple
    // clarification: Why <player.name> and not <PlayerTag.name>? That's because Denizen allows Players,
    // NPCs and other 'in-context objects' to be linked to certain scripts. In short, <player> already
    // contains a reference to a specific player, such as the player that died in a world event 'on player dies'.
    // <PlayerTag.name> is instead the format for documentation, with "PlayerTag" simply indicating 'any player object here'.
    //
    // ObjectTags can be used to CREATE new instances of objects, too! Though not all types allow 'new'
    // objects to be created, many do, such as ItemTags. With the use of tags, it's easy to reference a specific
    // item, say -- an item in the Player's hand -- items are also able to use a constructor to make a new item,
    // and say, drop it in the world. Take the case of the command/usage '- drop diamond_ore'. The item object
    // used is a brand new diamond_ore, which is then dropped by the command to a location of your choice -- just
    // specify an additional location argument.
    //
    // There's a great deal more to learn about ObjectTags, so be sure to check out each object type for more
    // specific information. While all ObjectTags share some features, many contain goodies on top of that!
    // -->

    // <--[language]
    // @name Tick
    // @group Common Terminology
    // @description
    // A 'tick' is usually referred to as 1/20th of a second, the speed at which Minecraft servers update
    // and process everything on them.
    // -->

    public static void registerMainTagHandlers() {
        // Objects
        if (Depends.citizens != null) {
            new NPCTagBase();
        }
        new PlayerTagBase();
        // Other bases
        new CustomColorTagBase();
        new ServerTagBase();
        new TextTagBase();
        new ParseTagBase();
    }

    public static void registerMainObjects() {
        registerObjectTypes();
        registerNotables();
        registerConversions();
        registerSubtypeSets();
        // Final debug
        if (CoreConfiguration.debugVerbose) {
            StringBuilder debug = new StringBuilder(256);
            for (ObjectType<?> objectType : ObjectFetcher.objectsByPrefix.values()) {
                debug.append(DebugInternals.getClassNameOpti(objectType.clazz)).append(" as ").append(objectType.prefix).append(", ");
            }
            Debug.echoApproval("Loaded core object types: [" + debug.substring(0, debug.length() - 2) + "]");
        }
    }

    public static ObjectType<BiomeTag> TYPE_BIOME;
    public static ObjectType<ChunkTag> TYPE_CHUNK;
    public static ObjectType<ColorTag> TYPE_COLOR;
    public static ObjectType<CuboidTag> TYPE_CUBOID;
    public static ObjectType<EllipsoidTag> TYPE_ELLIPSOID;
    public static ObjectType<EnchantmentTag> TYPE_ENCHANTMENT;
    public static ObjectType<EntityTag> TYPE_ENTITY;
    public static ObjectType<InventoryTag> TYPE_INVENTORY;
    public static ObjectType<ItemTag> TYPE_ITEM;
    public static ObjectType<LocationTag> TYPE_LOCATION;
    public static ObjectType<MaterialTag> TYPE_MATERIAL;
    public static ObjectType<NPCTag> TYPE_NPC;
    public static ObjectType<PlayerTag> TYPE_PLAYER;
    public static ObjectType<PluginTag> TYPE_PLUGIN;
    public static ObjectType<PolygonTag> TYPE_POLYGON;
    public static ObjectType<TradeTag> TYPE_TRADE;
    public static ObjectType<WorldTag> TYPE_WORLD;

    private static void registerObjectTypes() {

        // <--[tag]
        // @attribute <biome[<biome>]>
        // @returns BiomeTag
        // @description
        // Returns a biome object constructed from the input value.
        // Refer to <@link objecttype BiomeTag>.
        // -->
        TYPE_BIOME = ObjectFetcher.registerWithObjectFetcher(BiomeTag.class, BiomeTag.tagProcessor).setAsNOtherCode().setCanConvertStatic().generateBaseTag(); // b@

        // <--[tag]
        // @attribute <chunk[<chunk>]>
        // @returns ChunkTag
        // @description
        // Returns a chunk object constructed from the input value.
        // Refer to <@link objecttype ChunkTag>.
        // -->
        TYPE_CHUNK = ObjectFetcher.registerWithObjectFetcher(ChunkTag.class, ChunkTag.tagProcessor).setAsNOtherCode().setCanConvertStatic().generateBaseTag(); // ch@

        // <--[tag]
        // @attribute <color[<color>]>
        // @returns ColorTag
        // @description
        // Returns a color object constructed from the input value.
        // Refer to <@link objecttype ColorTag>.
        // -->
        TYPE_COLOR = ObjectFetcher.registerWithObjectFetcher(ColorTag.class, ColorTag.tagProcessor).setAsNOtherCode().setCanConvertStatic().generateBaseTag(); // co@

        // <--[tag]
        // @attribute <cuboid[<cuboid>]>
        // @returns CuboidTag
        // @description
        // Returns a cuboid object constructed from the input value.
        // Refer to <@link objecttype CuboidTag>.
        // -->
        TYPE_CUBOID = ObjectFetcher.registerWithObjectFetcher(CuboidTag.class, CuboidTag.tagProcessor).setAsNOtherCode().setCanConvertStatic().generateBaseTag(); // cu@

        // <--[tag]
        // @attribute <ellipsoid[<ellipsoid>]>
        // @returns EllipsoidTag
        // @description
        // Returns an ellipsoid object constructed from the input value.
        // Refer to <@link objecttype EllipsoidTag>.
        // -->
        TYPE_ELLIPSOID = ObjectFetcher.registerWithObjectFetcher(EllipsoidTag.class, EllipsoidTag.tagProcessor).setAsNOtherCode().setCanConvertStatic().generateBaseTag(); // ellipsoid@

        // <--[tag]
        // @attribute <enchantment[<enchantment>]>
        // @returns EnchantmentTag
        // @description
        // Returns an enchantment object constructed from the input value.
        // Refer to <@link objecttype EnchantmentTag>.
        // -->
        TYPE_ENCHANTMENT = ObjectFetcher.registerWithObjectFetcher(EnchantmentTag.class, EnchantmentTag.tagProcessor).setAsNOtherCode().setCanConvertStatic().generateBaseTag(); // enchantment@

        // <--[tag]
        // @attribute <entity[<entity>]>
        // @returns EntityTag
        // @description
        // Returns an entity object constructed from the input value.
        // Refer to <@link objecttype EntityTag>.
        // -->
        TYPE_ENTITY = ObjectFetcher.registerWithObjectFetcher(EntityTag.class, EntityTag.tagProcessor).generateBaseTag(); // e@
        TYPE_ENTITY.typeChecker = (inp) -> { // This is adapted 'no other type code' but for e@, p@, and n@
            if (inp == null) {
                return false;
            }
            if (inp instanceof PlayerTag || inp instanceof EntityTag || inp instanceof NPCTag) {
                return true;
            }
            if (inp instanceof ElementTag) {
                String simple = inp.identifySimple();
                int atIndex = simple.indexOf('@');
                if (atIndex != -1) {
                    String code = simple.substring(0, atIndex);
                    if (!code.equals("e") && !code.equals("p") && !code.equals("n") && !code.equals("el")) {
                        if (ObjectFetcher.objectsByPrefix.containsKey(code)) {
                            return false;
                        }
                    }
                }
                return true;
            }
            return false;
        };
        TYPE_ENTITY.typeConverter = (obj, context) -> {
            if (obj instanceof PlayerTag) {
                if (!((PlayerTag) obj).isOnline()) {
                    if (context.showErrors()) {
                        Debug.echoError("Player '" + obj.debuggable() + "' is offline, cannot convert to EntityTag.");
                    }
                    return null;
                }
                return new EntityTag(((PlayerTag) obj).getPlayerEntity());
            }
            else if (obj instanceof NPCTag) {
                if (!((NPCTag) obj).isSpawned() && !EntityTag.allowDespawnedNpcs) {
                    if (context.showErrors()) {
                        Debug.echoError("NPC '" + obj.debuggable() + "' is unspawned, cannot convert to EntityTag.");
                    }
                    return null;
                }
                return new EntityTag((NPCTag) obj);
            }
            return EntityTag.valueOf(obj.toString(), context);
        };
        TYPE_ENTITY.typeShouldBeChecker = (obj) -> {
            if (obj instanceof EntityFormObject) {
                return true;
            }
            String raw = obj.toString();
            if (raw.startsWith("p@") || raw.startsWith("e@") || raw.startsWith("n@")) {
                return true;
            }
            return false;
        };

        // <--[tag]
        // @attribute <inventory[<inventory>]>
        // @returns InventoryTag
        // @description
        // Returns an inventory object constructed from the input value.
        // Refer to <@link objecttype InventoryTag>.
        // -->
        // non-static due to notes and inventory scripts
        TYPE_INVENTORY = ObjectFetcher.registerWithObjectFetcher(InventoryTag.class, InventoryTag.tagProcessor).setAsNOtherCode().generateBaseTag(); // in@

        // <--[tag]
        // @attribute <item[<item>]>
        // @returns ItemTag
        // @description
        // Returns an item object constructed from the input value.
        // Refer to <@link objecttype ItemTag>.
        // -->
        // non-static as item scripts can contain tags
        TYPE_ITEM = ObjectFetcher.registerWithObjectFetcher(ItemTag.class, ItemTag.tagProcessor).setAsNOtherCode().generateBaseTag(); // i@

        // <--[tag]
        // @attribute <location[<location>]>
        // @returns LocationTag
        // @description
        // Returns a location object constructed from the input value.
        // Refer to <@link objecttype LocationTag>.
        // -->
        TYPE_LOCATION = ObjectFetcher.registerWithObjectFetcher(LocationTag.class, LocationTag.tagProcessor).setAsNOtherCode().setCanConvertStatic().generateBaseTag(); // l@

        // <--[tag]
        // @attribute <material[<material>]>
        // @returns MaterialTag
        // @description
        // Returns a material object constructed from the input value.
        // Refer to <@link objecttype MaterialTag>.
        // -->
        TYPE_MATERIAL = ObjectFetcher.registerWithObjectFetcher(MaterialTag.class, MaterialTag.tagProcessor).setAsNOtherCode().setCanConvertStatic().generateBaseTag(); // m@

        if (Depends.citizens != null) {
            // Tag generated externally as input is optional
            TYPE_NPC = ObjectFetcher.registerWithObjectFetcher(NPCTag.class, NPCTag.tagProcessor); // n@
            TYPE_NPC.typeChecker = (inp) -> { // This is adapted 'no other type code' but allows instanceof EntityTag
                if (inp == null) {
                    return false;
                }
                if (inp instanceof NPCTag || inp instanceof EntityTag) {
                    return true;
                }
                if (inp instanceof ElementTag) {
                    String simple = inp.identifySimple();
                    int atIndex = simple.indexOf('@');
                    if (atIndex != -1) {
                        String code = simple.substring(0, atIndex);
                        if (!code.equals("n") && !code.equals("el")) {
                            if (ObjectFetcher.objectsByPrefix.containsKey(code)) {
                                return false;
                            }
                        }
                    }
                    return true;
                }
                return false;
            };
            TYPE_NPC.typeConverter = (obj, context) -> {
                if (obj instanceof EntityTag && ((EntityTag) obj).isCitizensNPC()) {
                    return ((EntityTag) obj).getDenizenNPC();
                }
                return NPCTag.valueOf(obj.toString(), context);
            };
        }

        // Tag generated externally as input is optional
        TYPE_PLAYER = ObjectFetcher.registerWithObjectFetcher(PlayerTag.class, PlayerTag.tagProcessor); // p@
        TYPE_PLAYER.typeChecker = (inp) -> { // This is adapted 'no other type code' but allows instanceof EntityTag
            if (inp == null) {
                return false;
            }
            if (inp instanceof PlayerTag || inp instanceof EntityTag) {
                return true;
            }
            if (inp instanceof ElementTag) {
                String simple = inp.identifySimple();
                int atIndex = simple.indexOf('@');
                if (atIndex != -1) {
                    String code = simple.substring(0, atIndex);
                    if (!code.equals("p") && !code.equals("el")) {
                        if (ObjectFetcher.objectsByPrefix.containsKey(code)) {
                            return false;
                        }
                    }
                }
                return true;
            }
            return false;
        };
        TYPE_PLAYER.typeConverter = (obj, context) -> {
            if (obj instanceof EntityTag && ((EntityTag) obj).isPlayer()) {
                return ((EntityTag) obj).getDenizenPlayer();
            }
            return PlayerTag.valueOf(obj.toString(), context);
        };

        // <--[tag]
        // @attribute <plugin[<plugin>]>
        // @returns PluginTag
        // @description
        // Returns a plugin object constructed from the input value.
        // Refer to <@link objecttype PluginTag>.
        // -->
        TYPE_PLUGIN = ObjectFetcher.registerWithObjectFetcher(PluginTag.class, PluginTag.tagProcessor).setAsNOtherCode().setCanConvertStatic().generateBaseTag(); // pl@

        // <--[tag]
        // @attribute <polygon[<polygon>]>
        // @returns PolygonTag
        // @description
        // Returns a polygon object constructed from the input value.
        // Refer to <@link objecttype PolygonTag>.
        // -->
        TYPE_POLYGON = ObjectFetcher.registerWithObjectFetcher(PolygonTag.class, PolygonTag.tagProcessor).setAsNOtherCode().setCanConvertStatic().generateBaseTag(); // polygon@

        // <--[tag]
        // @attribute <trade[<trade>]>
        // @returns TradeTag
        // @description
        // Returns a trade object constructed from the input value.
        // Refer to <@link objecttype TradeTag>.
        // -->
        // Non-static due to potential for dynamic items.
        TYPE_TRADE = ObjectFetcher.registerWithObjectFetcher(TradeTag.class, TradeTag.tagProcessor).setAsNOtherCode().generateBaseTag(); // trade@

        // <--[tag]
        // @attribute <world[<world>]>
        // @returns WorldTag
        // @description
        // Returns a world object constructed from the input value.
        // Refer to <@link objecttype WorldTag>.
        // -->
        // non-static as worlds can be dynamically loaded
        TYPE_WORLD = ObjectFetcher.registerWithObjectFetcher(WorldTag.class, WorldTag.tagProcessor).setAsNOtherCode().generateBaseTag(); // w@
    }

    private static void registerNotables() {
        NoteManager.registerObjectTypeAsNotable(CuboidTag.class);
        NoteManager.registerObjectTypeAsNotable(EllipsoidTag.class);
        NoteManager.registerObjectTypeAsNotable(InventoryTag.class);
        NoteManager.registerObjectTypeAsNotable(ItemTag.class);
        NoteManager.registerObjectTypeAsNotable(LocationTag.class);
        NoteManager.registerObjectTypeAsNotable(PolygonTag.class);
    }

    private static void registerConversions() {
        CoreUtilities.objectConversions.add((obj) -> {
            if (obj instanceof Biome) {
                return new BiomeTag((Biome) obj);
            }
            if (obj instanceof Chunk) {
                return new ChunkTag((Chunk) obj);
            }
            if (obj instanceof Color) {
                return new ColorTag((Color) obj);
            }
            if (obj instanceof Enchantment) {
                return new EnchantmentTag((Enchantment) obj);
            }
            if (obj instanceof Entity) {
                return new EntityTag((Entity) obj).getDenizenObject();
            }
            if (obj instanceof Inventory) {
                return InventoryTag.mirrorBukkitInventory((Inventory) obj);
            }
            if (obj instanceof ItemStack) {
                return new ItemTag((ItemStack) obj);
            }
            if (obj instanceof Location) {
                return new LocationTag((Location) obj);
            }
            if (obj instanceof Material) {
                return new MaterialTag((Material) obj);
            }
            if (obj instanceof BlockData) {
                return new MaterialTag((BlockData) obj);
            }
            if (obj instanceof Block) {
                return new LocationTag(((Block) obj).getLocation());
            }
            if (Depends.citizens != null && obj instanceof NPC) {
                return new NPCTag((NPC) obj);
            }
            if (obj instanceof OfflinePlayer) {
                return new PlayerTag((OfflinePlayer) obj);
            }
            if (obj instanceof Plugin) {
                return new PluginTag((Plugin) obj);
            }
            if (obj instanceof MerchantRecipe) {
                return new TradeTag((MerchantRecipe) obj);
            }
            if (obj instanceof World) {
                return new WorldTag((World) obj);
            }
            return null;
        });
    }

    private static void registerSubtypeSets() {
        ObjectFetcher.registerCrossType(EntityTag.class, EntityFormObject.class);
        ObjectFetcher.registerCrossType(PlayerTag.class, EntityTag.class);
        ObjectFetcher.registerCrossType(PlayerTag.class, EntityFormObject.class);
        if (Depends.citizens != null) {
            ObjectFetcher.registerCrossType(NPCTag.class, EntityTag.class);
            ObjectFetcher.registerCrossType(NPCTag.class, EntityFormObject.class);
        }
    }
}
