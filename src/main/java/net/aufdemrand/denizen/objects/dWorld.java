package net.aufdemrand.denizen.objects;

import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class dWorld implements dObject {

    static Map<String, dWorld> worlds = new HashMap<String, dWorld>();

    public static dWorld mirrorBukkitWorld(World world) {
        if (worlds.containsKey(world.getName())) return worlds.get(world.getName());
        else return new dWorld(world);
    }

    @ObjectFetcher("w")
    public static dWorld valueOf(String string) {
        if (string == null) return null;

        string = string.replace("w@", "");

        ////////
        // Match world name

        World returnable = null;

        for (World world : Bukkit.getWorlds())
            if (world.getName().equalsIgnoreCase(string))
                returnable = world;

        if (returnable != null) {
            if (worlds.containsKey(returnable.getName()))
                return worlds.get(returnable.getName());
            else return new dWorld(returnable);
        }
        else dB.echoError("Invalid World! '" + string
                + "' could not be found.");

        return null;
    }


    public static boolean matches(String arg) {

        arg = arg.replace("w@", "");

        World returnable = null;

        for (World world : Bukkit.getWorlds())
            if (world.getName().equalsIgnoreCase(arg))
                returnable = world;

        return returnable != null;
    }


    public World getWorld() {
        return Bukkit.getWorld(world_name);
    }
    
    public List<Entity> getEntities() {
        return getWorld().getEntities();
    }

    private String prefix;
    String world_name;

    public dWorld(World world) {
        this(null, world);
    }

    public dWorld(String prefix, World world) {
        if (prefix == null) this.prefix = "World";
        else this.prefix = prefix;
        this.world_name = world.getName();
        if (!worlds.containsKey(world.getName()))
            worlds.put(world.getName(), this);
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public String debug() {
        return (prefix + "='<A>" + identify() + "<G>'  ");
    }

    @Override
    public boolean isUnique() {
        return true;
    }

    @Override
    public String getType() {
        return "World";
    }

    @Override
    public String identify() {
        return "w@" + world_name;

    }

    @Override
    public String toString() {
        return identify();
    }

    @Override
    public dObject setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) return null;

        // <--[tag]
        // @attribute <world.can_generate_structures>
        // @returns Element(boolean)
        // @description
        // returns whether the world will generate structures.
        // -->
        if (attribute.startsWith("can_generate_structures"))
            return new Element(String.valueOf(getWorld().canGenerateStructures()))
                    .getAttribute(attribute.fulfill(1));

//        getWorld().getName())
//        .getAttribute(attribute.fulfill(1));
//
//        getWorld().getAllowAnimals())
//        .getAttribute(attribute.fulfill(1));
//
//        getWorld().getAllowMonsters())
//        .getAttribute(attribute.fulfill(1));
//
//        getWorld().getAmbientSpawnLimit())
//        .getAttribute(attribute.fulfill(1));
//
//        getWorld().getAnimalSpawnLimit())
//        .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <world.highest_block>
        // @returns dLocation
        // @description
        // returns the location of the highest non-air block.
        // -->
        if (attribute.startsWith("highest_block")) {
            // TODO: finish
            int x = 1;
            int z = 1;

            return new dLocation(getWorld().getHighestBlockAt(x, z).getLocation())
                    .getAttribute(attribute.fulfill(1));
        }

//        getWorld().getChunkAt()

        // <--[tag]
        // @attribute <world.difficulty>
        // @returns Element
        // @description
        // returns the name of the difficulty level
        // -->
        if (attribute.startsWith("difficulty"))
            return new Element(getWorld().getDifficulty().name())
                    .getAttribute(attribute.fulfill(1));
        
        // <--[tag]
        // @attribute <world.name>
        // @returns Element
        // @description
        // returns the name of the world
        // -->
        if (attribute.startsWith("name"))
            return new Element(String.valueOf(getWorld().getName()))
                    .getAttribute(attribute.fulfill(1));
        
        // <--[tag]
        // @attribute <world.players>
        // @returns dList(dPlayer)
        // @description
        // returns a list of online players
        // -->
        if (attribute.startsWith("players")) {
            List<String> players = new ArrayList<String>();
            for(Player player : getWorld().getPlayers())
                players.add(player.getName());

            return new dList(players)
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <world.sea_level>
        // @returns Element(number)
        // @description
        // returns the level of the sea
        // -->
        if (attribute.startsWith("sea_level"))
            return new Element(String.valueOf(getWorld().getSeaLevel()))
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <world.seed>
        // @returns Element
        // @description
        // returns the world seed
        // -->
        if (attribute.startsWith("seed"))
            return new Element(String.valueOf(getWorld().getSeed()))
                    .getAttribute(attribute.fulfill(1));

//        getWorld().getEntities())
//        .getAttribute(attribute.fulfill(1));
//
//        getWorld().getEntitiesByClass())
//        .getAttribute(attribute.fulfill(1));
//
//        getWorld().getEntitiesByClasses())
//        .getAttribute(attribute.fulfill(1));

        // Return "day", "night", "dawn" or "dusk"
        // <--[tag]
        // @attribute <world.time.period>
        // @returns Element
        // @description
        // returns the time as day, night, dawn, or dusk
        // -->
        if (attribute.startsWith("time.period")) {
            
            long time = getWorld().getTime();
            String period;
            
            if (time >= 23000) period = "dawn"; 
            else if (time >= 13500) period = "night";
            else if (time >= 12500) period = "dusk";
            else period = "day";
            
            return new Element(period).getAttribute(attribute.fulfill(2));
        }
        
        // <--[tag]
        // @attribute <world.time>
        // @returns Element(number)
        // @description
        // returns the current time in ticks
        // -->
        if (attribute.startsWith("time"))
            return new Element(String.valueOf(getWorld().getTime()))
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <world.weather_duration>
        // @returns Element
        // @description
        // returns the duration of storms in ticks
        // -->
        if (attribute.startsWith("weather_duration"))
            return Duration.valueOf(String.valueOf(getWorld().getWeatherDuration()) + "t")
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <world.has_storm>
        // @returns Element(boolean)
        // @description
        // returns whether there is currently a storm in this world
        // -->
        if (attribute.startsWith("has_storm"))
            return new Element(String.valueOf(getWorld().hasStorm()))
                    .getAttribute(attribute.fulfill(1));



        if (attribute.startsWith("prefix"))
            return new Element(prefix)
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("debug.log")) {
            dB.log(debug());
            return new Element(Boolean.TRUE.toString())
                    .getAttribute(attribute.fulfill(2));
        }

        if (attribute.startsWith("debug.no_color")) {
            return new Element(ChatColor.stripColor(debug()))
                    .getAttribute(attribute.fulfill(2));
        }

        if (attribute.startsWith("debug")) {
            return new Element(debug())
                    .getAttribute(attribute.fulfill(1));
        }

        return new Element(identify()).getAttribute(attribute.fulfill(0));
    }

}
