package net.aufdemrand.denizen.objects;

import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
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
    
    public String getName() {
        return world_name;
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
    public String getObjectType() {
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
        
        /////////////////////
        //   ENTITY LIST ATTRIBUTES
        /////////////////
        
        // <--[tag]
        // @attribute <w@world.entities>
        // @returns dList(dPlayer)
        // @description
        // Returns a list of entities in this world.
        // -->
        if (attribute.startsWith("entities")) {
            List<String> entities = new ArrayList<String>();
            for (Entity entity : getWorld().getEntities())
                entities.add("e@" + entity.getEntityId());

            return new dList(entities)
                    .getAttribute(attribute.fulfill(1));
        }
        
        // <--[tag]
        // @attribute <w@world.living_entities>
        // @returns dList(dPlayer)
        // @description
        // Returns a list of living entities in this world.
        // -->
        if (attribute.startsWith("living_entities")) {
            List<String> entities = new ArrayList<String>();
            for (LivingEntity entity : getWorld().getLivingEntities())
                entities.add("e@" + entity.getEntityId());

            return new dList(entities)
                    .getAttribute(attribute.fulfill(1));
        }
        
        // <--[tag]
        // @attribute <w@world.players>
        // @returns dList(dPlayer)
        // @description
        // Returns a list of online players in this world.
        // -->
        if (attribute.startsWith("players")) {
            List<String> players = new ArrayList<String>();
            for (Player player : getWorld().getPlayers())
                players.add("p@" + player.getName());

            return new dList(players)
                    .getAttribute(attribute.fulfill(1));
        }
        
        
        /////////////////////
        //   DEBUG ATTRIBUTES
        /////////////////
        
        // <--[tag]
        // @attribute <w@world.prefix>
        // @returns Element
        // @description
        // Returns the prefix of the world dObject.
        // -->
        if (attribute.startsWith("prefix"))
            return new Element(prefix)
                    .getAttribute(attribute.fulfill(1));
        
        
        /////////////////////
        //   GEOGRAPHY ATTRIBUTES
        /////////////////
        
        // <--[tag]
        // @attribute <w@world.highest_block>
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

        // <--[tag]
        // @attribute <w@world.sea_level>
        // @returns Element(number)
        // @description
        // returns the level of the sea
        // -->
        if (attribute.startsWith("sea_level"))
            return new Element(getWorld().getSeaLevel())
                    .getAttribute(attribute.fulfill(1));

        
        /////////////////////
        //   IDENTIFICATION ATTRIBUTES
        /////////////////
        
        // <--[tag]
        // @attribute <w@world.name>
        // @returns Element
        // @description
        // returns the name of the world
        // -->
        if (attribute.startsWith("name"))
            return new Element(getWorld().getName())
                    .getAttribute(attribute.fulfill(1));
        
        // <--[tag]
        // @attribute <w@world.seed>
        // @returns Element
        // @description
        // returns the world seed
        // -->
        if (attribute.startsWith("seed"))
            return new Element(getWorld().getSeed())
                    .getAttribute(attribute.fulfill(1));
        
        
        /////////////////////
        //   SETTINGS ATTRIBUTES
        /////////////////
        
        // <--[tag]
        // @attribute <w@world.allows_animals>
        // @returns Element(boolean)
        // @description
        // Returns whether animals can spawn in this world.
        // -->
        if (attribute.startsWith("allows_animals"))
            return new Element(getWorld().getAllowAnimals())
                    .getAttribute(attribute.fulfill(1));
        
        // <--[tag]
        // @attribute <w@world.allows_monsters>
        // @returns Element(boolean)
        // @description
        // Returns whether monsters can spawn in this world.
        // -->
        if (attribute.startsWith("allows_monsters"))
            return new Element(getWorld().getAllowMonsters())
                    .getAttribute(attribute.fulfill(1));
        
        // <--[tag]
        // @attribute <w@world.allows_pvp>
        // @returns Element(boolean)
        // @description
        // Returns whether player versus player combat is allowed in this world.
        // -->
        if (attribute.startsWith("allows_pvp"))
            return new Element(getWorld().getPVP())
                    .getAttribute(attribute.fulfill(1));
        
        // <--[tag]
        // @attribute <w@world.ambient_spawn_limit>
        // @returns Element(integer)
        // @description
        // Returns the number of ambient mobs that can spawn in a chunk in this world
        // -->
        if (attribute.startsWith("ambient_spawn_limit"))
            return new Element(getWorld().getAmbientSpawnLimit())
                    .getAttribute(attribute.fulfill(1));
        
        // <--[tag]
        // @attribute <w@world.animal_spawn_limit>
        // @returns Element(integer)
        // @description
        // Returns the number of animals that can spawn in a chunk in this world.
        // -->
        if (attribute.startsWith("animal_spawn_limit"))
            return new Element(getWorld().getAnimalSpawnLimit())
                    .getAttribute(attribute.fulfill(1));
        
        // <--[tag]
        // @attribute <w@world.can_generate_structures>
        // @returns Element(boolean)
        // @description
        // Returns whether the world will generate structures.
        // -->
        if (attribute.startsWith("can_generate_structures"))
            return new Element(getWorld().canGenerateStructures())
                    .getAttribute(attribute.fulfill(1));
        
        // <--[tag]
        // @attribute <w@world.difficulty>
        // @returns Element
        // @description
        // returns the name of the difficulty level
        // -->
        if (attribute.startsWith("difficulty"))
            return new Element(getWorld().getDifficulty().name())
                    .getAttribute(attribute.fulfill(1));
        
        // <--[tag]
        // @attribute <w@world.max_height>
        // @returns Element(integer)
        // @description
        // Returns the maximum height of this world.
        // -->
        if (attribute.startsWith("max_height"))
            return new Element(getWorld().getMaxHeight())
                    .getAttribute(attribute.fulfill(1));
        
        // <--[tag]
        // @attribute <w@world.monster_spawn_limit>
        // @returns Element(integer)
        // @description
        // Returns the number of monsters that can spawn in a chunk in this world.
        // -->
        if (attribute.startsWith("monster_spawn_limit"))
            return new Element(getWorld().getMonsterSpawnLimit())
                    .getAttribute(attribute.fulfill(1));
        
        // <--[tag]
        // @attribute <w@world.ticks_per_animal_spawn>
        // @returns Element(long)
        // @description
        // Returns the world's ticks per animal spawn value
        // -->
        if (attribute.startsWith("ticks_per_animal_spawn"))
            return new Element(getWorld().getTicksPerAnimalSpawns() )
                    .getAttribute(attribute.fulfill(1));
        
        // <--[tag]
        // @attribute <w@world.ticks_per_monster_spawn>
        // @returns Element(long)
        // @description
        // Returns the world's ticks per monster spawn value
        // -->
        if (attribute.startsWith("ticks_per_monster_spawn"))
            return new Element(getWorld().getTicksPerMonsterSpawns() )
                    .getAttribute(attribute.fulfill(1));
        
        // <--[tag]
        // @attribute <w@world.water_animal_spawn_limit>
        // @returns Element(integer)
        // @description
        // Returns the number of water animals that can spawn in a chunk in this world
        // -->
        if (attribute.startsWith("water_animal_spawn_limit"))
            return new Element(getWorld().getWaterAnimalSpawnLimit())
                    .getAttribute(attribute.fulfill(1));
        
        
        /////////////////////
        //   TIME ATTRIBUTES
        /////////////////
        
        // Return "day", "night", "dawn" or "dusk"
        // <--[tag]
        // @attribute <w@world.time.period>
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
        // @attribute <w@world.time>
        // @returns Element(long)
        // @description
        // returns the current time in ticks
        // -->
        if (attribute.startsWith("time"))
            return new Element(getWorld().getTime())
                    .getAttribute(attribute.fulfill(1));

        
        /////////////////////
        //   WEATHER ATTRIBUTES
        /////////////////
        
        // <--[tag]
        // @attribute <w@world.has_storm>
        // @returns Element(boolean)
        // @description
        // returns whether there is currently a storm in this world
        // -->
        if (attribute.startsWith("has_storm"))
            return new Element(getWorld().hasStorm())
                    .getAttribute(attribute.fulfill(1));
        
        // <--[tag]
        // @attribute <w@world.weather_duration>
        // @returns Element
        // @description
        // Returns the duration of storms
        // -->
        if (attribute.startsWith("weather_duration"))
            return Duration.valueOf(getWorld().getWeatherDuration() + "t")
                    .getAttribute(attribute.fulfill(1));

        return new Element(identify()).getAttribute(attribute.fulfill(0));
    }

}
