package com.denizenscript.denizen;

import com.denizenscript.denizen.objects.*;
import com.denizenscript.denizen.tags.core.*;
import com.denizenscript.denizen.utilities.depends.Depends;
import com.denizenscript.denizencore.objects.ObjectFetcher;

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
    // The use of this notation is encouraged, but not always required.
    //
    // Let's take the tag system, for example. It uses the ObjectTags system pretty heavily. For instance,
    // every time you use <player.name> or <npc.id>, you're using a ObjectTag, which brings us to a simple
    // clarification: Why <player.name> and not <PlayerTag.name>? That's because Denizen allows Players,
    // NPCs and other 'in-context objects' to be linked to certain scripts. In short, <player> already
    // contains a reference to a specific player, such as the player that died in a world event 'on player dies'.
    // <PlayerTag.name> would incorrectly reference the player named 'player', however this format is often
    // used to help with usage of a tag, simply indicating 'any player object'.
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
    //
    // Here's an overview of each object type that is implemented by the Denizen core:
    //
    // + ----- PlayerTag ----- +
    // | object notation: p@    can reference unique objects: yes    can be notable: no
    // | constructors: ( <>'s represent non-static information and are not literal)
    // |   p@<UUID> - fetches an online or offline player with the specified UUID
    //
    // + ----- NPCTag ---------+
    // | object notation: n@    can reference unique objects: yes    can be notable: no
    // | constructors: ( <>'s represent non-static information and are not literal)
    // |   n@<npc_id> - fetches the NPC with the specified ID
    //
    // + ----- LocationTag ----+
    // | object notation: l@    can reference unique objects: no     can be notable: yes
    // | constructors: ( <>'s represent non-static information and are not literal)
    // |   l@<x>,<y>,<z>,<world_name> - fetches a specific location
    // |   l@<x>,<y>,<z>,<pitch>,<yaw>,<world_name> - fetches a specific location and direction
    // |   l@<notable_location_name> - fetches the location that has been 'noted' with the specified ID
    //
    // + ----- EntityTag ------+
    // | object notation: e@    can reference unique objects: yes    can be notable: no
    // | constructors: ( <>'s represent non-static information and are not literal)
    // |   e@<entity_type> - fetches a new entity with the specified type as implemented by Bukkit's entity type enumeration
    // |   e@<entity_type>,<setting> - fetches a new entity of the specified type with a custom setting unique to the type
    // |   e@<entity_script_name> - fetches a new custom entity as specified by the referenced entity script (soon)
    // |   e@random - fetches a new, random entity
    //
    // + ----- ItemTag --------+
    // | object notation: i@    can reference unique objects: no     can be notable: yes
    // | constructors: ( <>'s represent non-static information and are not literal)
    // |   i@<material_name> - fetches a new item of the specified material
    // |   i@<item_script_name> - fetches a new custom item as specified by the referenced item script
    // |   i@<notable_name> - fetches the item that has been noted with the specified ID
    //
    // + ----- WorldTag -------+
    // | object notation: w@    can reference unique objects: yes     can be notable: no
    // | constructors: ( <>'s represent non-static information and are not literal)
    // |   w@<world_name> - fetches the world with the specified name
    //
    // + ----- ColorTag -------+
    // | object notation: co@    can reference unique objects: no      can be notable: soon
    // | constructors: ( <>'s represent non-static information and are not literal)
    // |   co@<color_name> - fetches a named color, as implemented by Bukkit's color enumeration
    // |   co@<r>,<g>,<b> - fetches a color made of the specified Red,Green,Blue value
    // |   co@random - fetches a random color
    //
    // + ----- CuboidTag ------+
    // | object notation: cu@   can reference unique objects: no      can be notable: yes
    // | constructors: ( <>'s represent non-static information and are not literal)
    // |   cu@<position_1>|<position_2>|... - fetches a new cuboid encompassing a region from position 1 to 2, from 3 to 4, ...
    // |   cu@<notable_name> - fetches the cuboid that has been noted with the specified ID
    //
    // + ----- EllipsoidTag ------+
    // | object notation: ellipsoid@   can reference unique objects: no      can be notable: yes
    // | constructors: ( <>'s represent non-static information and are not literal)
    // |   ellipsoid@<x>,<y>,<z>,<world>,<xrad>,<yrad>,<zrad> - fetches a new ellispoid at the position with the given radius
    // |   ellipsoid@<notable_name> - fetches the ellipsoid that has been noted with the specified ID
    //
    // + ----- ChunkTag ------+
    // | object notation: ch@   can reference unique objects: yes      can be notable: no
    // | constructors: ( <>'s represent non-static information and are not literal)
    // |   ch@<x>,<y>,<world> - fetches a chunk at the given chunk location
    //
    // + ----- InventoryTag ---+
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
    // + ----- MaterialTag ----+
    // | object notation: m@    can reference unique objects: no      can be notable: no
    // | constructors: ( <>'s represent non-static information and are not literal)
    // |   m@<material_name> - fetches the material as specified by Bukkit's material enumeration
    // |   m@random - fetches a random material
    //
    // + ----- TradeTag -----+
    // | object notation: trade@    can reference unique objects: no      can be notable: no
    // |   TradeTag - represents a generic, customizable merchant trade to be used with merchant trade properties (See <@link language Merchant Trades>)
    //
    // + ----- ListTag -------+
    // | object notation: li@  can reference unique objects: yes  can be notable: no
    // | constructors: ( <>'s represent non-static information and are not literal)
    // |   li@<items|...> - fetches a new list with the elements specified, separated by a pipe (|) character
    //
    // + ----- ScriptTag -------+
    // | object notation: s@    can reference unique objects: yes     can be notable: no
    // | constructors: ( <>'s represent non-static information and are not literal)
    // |   s@<script_container_name> - fetches the script container with the specified name
    //
    // + ----- DurationTag ------+
    // | object notation: d@    can reference unique objects: no      can be notable: no
    // | constructors: ( <>'s represent non-static information and are not literal)
    // |   d@<duration> - fetches a duration object with the specified amount of time
    // |   d@<low>-<high> - fetches a duration that is randomly selected between the specified 'low' and 'high'
    //
    // + ----- PluginTag -------+
    // | object notation: pl@    can reference unique objects: yes     can be notable: no
    // | constructors: ( <>'s represent non-static information and are not literal)
    // |   pl@<plugin_name> - fetches the plugin with the specified name
    //
    // + ----- ElementTag ------+
    // | object notation: el@   can reference unique objects: no      can be notable: no
    // | constructors: ( <>'s represent non-static information and are not literal)
    // |   el@<value> - fetches an element with the specified value
    // |   el@val[<value>] - slightly more verbose, but tag friendly way to fetch a new element (allows periods)
    //
    // + ----- QueueTag ------+
    // | object notation: q@   can reference unique objects: yes      can be notable: no
    // | constructors: ( <>'s represent non-static information and are not literal)
    // |   q@<id> - fetches the queue with the given ID
    //
    // + ----- CustomObjectTag ------+
    // | object notation: custom@   can reference unique objects: no      can be notable: no
    // | constructors: ( <>'s represent non-static information and are not literal)
    // |   custom@<custom_script_name> - fetches a custom object of the specified base custom script.
    //
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
        new BiomeTagBase();
        new ChunkTagBase();
        new ColorTagBase();
        new CuboidTagBase();
        new EllipsoidTagBase();
        new EntityTagBase();
        new InventoryTagBase();
        new ItemTagBase();
        new LocationTagBase();
        new MaterialTagBase();
        if (Depends.citizens != null) {
            new NPCTagBase();
        }
        new PlayerTagBase();
        new PluginTagBase();
        new TradeTagBase();
        new WorldTagBase();

        // Other bases
        new ServerTagBase();
        new TextTagBase();
        new ParseTagBase();
    }

    public static void registerMainObjects() {
        ObjectFetcher.registerWithObjectFetcher(BiomeTag.class);     // b@
        BiomeTag.registerTags(); // TODO: Automate this once all classes have tag registries
        ObjectFetcher.registerWithObjectFetcher(ChunkTag.class);     // ch@
        ChunkTag.registerTags(); // TODO: Automate this once all classes have tag registries
        ObjectFetcher.registerWithObjectFetcher(ColorTag.class);     // co@
        ColorTag.registerTags(); // TODO: Automate this once all classes have tag registries
        ObjectFetcher.registerWithObjectFetcher(CuboidTag.class);    // cu@
        CuboidTag.registerTags(); // TODO: Automate this once all classes have tag registries
        ObjectFetcher.registerWithObjectFetcher(EllipsoidTag.class); // ellipsoid@
        EllipsoidTag.registerTags(); // TODO: Automate this once all classes have tag registries
        ObjectFetcher.registerWithObjectFetcher(EntityTag.class);    // e@
        ObjectFetcher.registerWithObjectFetcher(InventoryTag.class); // in@
        ObjectFetcher.registerWithObjectFetcher(ItemTag.class);      // i@
        ItemTag.registerTags(); // TODO: Automate this once all classes have tag registries
        ObjectFetcher.registerWithObjectFetcher(LocationTag.class);  // l@
        ObjectFetcher.registerWithObjectFetcher(MaterialTag.class);  // m@
        MaterialTag.registerTags(); // TODO: Automate this once all classes have tag registries
        if (Depends.citizens != null) {
            ObjectFetcher.registerWithObjectFetcher(NPCTag.class);   // n@
        }
        ObjectFetcher.registerWithObjectFetcher(PlayerTag.class);    // p@
        ObjectFetcher.registerWithObjectFetcher(PluginTag.class);    // pl@
        PluginTag.registerTags(); // TODO: Automate this once all classes have tag registries
        ObjectFetcher.registerWithObjectFetcher(TradeTag.class);     // trade@
        ObjectFetcher.registerWithObjectFetcher(WorldTag.class);     // w@
        WorldTag.registerTags(); // TODO: Automate this once all classes have tag registries
    }
}
