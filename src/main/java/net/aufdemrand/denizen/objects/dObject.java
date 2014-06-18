package net.aufdemrand.denizen.objects;

import net.aufdemrand.denizen.tags.Attribute;

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
// | object notation: i@    can reference unique objects: no     can be notable: no
// | constructors: ( <>'s represent non-static information and are not literal)
// |   i@<material_name> - fetches a new item of the specified material
// |   i@<material_name>,<data> - fetches a new item with the specified data
// |   i@<item_entity_id> - fetches an item that is laying on the ground in a world by its Bukkit EntityID
// |   i@<item_script_name> - fetches a new custom item as specified by the referenced item script
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
// |   cu@<notable_cuboid_name> - fetches the cuboid that has been 'noted' with the specified ID
//
// + ----- dInventory ---+
// | object notation: in@   can reference unique objects: yes     can be notable: soon
// | constructors: ( <>'s represent non-static information and are not literal)
// |   in@player[<player_name/object>] - fetches the specified Player's inventory
// |   in@npc[<npc_id/object>] - fetches the specified NPC's inventory
// |   in@entity[<entity_object>] - fetches the specified object's inventory, such as a Player, NPC, or Mule
// |   in@location[<location_object>] - fetches the contents of a chest or other 'inventory' block
// |   in@<notable_inventory_name> - fetches the inventory that has been 'noted' with the specified ID
// |   in@<inventory_script_name> - fetches a new custom inventory as specified by the referenced inventory script
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
// -->

public interface dObject {

    /**
     * dObjects should contain these two static methods, of which valueOf contains a valid
     * annotation for ObjectFetcher
     *
     * public static dObject valueOf(String string);
     *
     * valueOf() should take a string representation of the object, preferably with a valid object
     * notation (x@), and turn it into a new instance of the dObject. Care has to be taken to
     * ensure that it is compatible with the tag system (ie. no periods (.) outside of square brackets),
     * and other parts of Denizen.
     *
     * Since your object may be using the dObject Attributes System, valueOf should take that into
     * account as well.
     *
     *
     * public static boolean matches()
     *
     * matches() should use some logic to determine if a string is in the proper format to possibly
     * return a non-null valueOf() call.
     *
     */


    /**
     * Retrieves the dScript argument prefix. dObjects should provide a default
     * prefix if nothing else has been specified.
     *
     * @return the prefix
     */
    public String getPrefix();


    /**
     * <p>Gets a standard dB representation of this argument. All dObjects should follow
     * suit.</p>
     *
     * Example: <br/>
     * <tt>
     * Location='x,y,z,world'
     * Location='unique_location(x,y,z,world)'
     * </tt>
     *
     *
     * @return the debug information
     */
    public String debug();


    /**
     * Determines if this argument object is unique. This typically stipulates
     * that this object has been named, or has some unique identifier that
     * Denizen can use to recall it.
     *
     * @return  true if this object is unique, false if it is a 'singleton generic argument/object'
     */
    public boolean isUnique();


    /**
     * Returns the string type of the object. This is fairly verbose and crude, but used with
     * a basic dScriptArg attribute.
     *
     * @return  a straight-up string description of the type of dScriptArg. ie. dList, dLocation
     */
    public String getObjectType();


    /**
     * Gets an ugly, but exact, string representation of this dObject.
     * While not specified in the dObject Interface, this value should be
     * able to be used with a static valueOf(String) method to reconstruct the object.
     *
     * @return  a single-line string representation of this argument
     */
    public String identify();


    /**
     * Gets an overall string representation of this dObject.
     * This should give the basic jist of the object being identified, but
     * won't include the exactness that identify() uses.
     *
     * <code>
     * Example: i@gold_sword     vs.    i@gold_sword[display_name=Shiny Sword]
     *          ^                       ^
     *          +--- identifySimple()   +--- identify()
     * </code>
     *
     * This may produce the same results as identify(), depending on the complexity
     * of the object being identified.
     *
     * @return  a single-line, 'simple' string representation of this argument
     */
    public String identifySimple();


    /**
     * Sets the prefix for this argument, otherwise uses the default.
     *
     * @return  the dObject
     */
    public dObject setPrefix(String prefix);


    /**
     * Gets a specific attribute using this object to fetch the necessary data.
     *
     * @param attribute  the name of the attribute
     * @return  a string result of the fetched attribute
     */
    public String getAttribute(Attribute attribute);

}
