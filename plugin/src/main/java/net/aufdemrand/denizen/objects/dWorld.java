package net.aufdemrand.denizen.objects;

import net.aufdemrand.denizen.Settings;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.objects.*;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.objects.properties.PropertyParser;
import net.aufdemrand.denizencore.tags.Attribute;
import net.aufdemrand.denizencore.tags.TagContext;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.apache.commons.io.FileUtils;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class dWorld implements dObject, Adjustable {


    /////////////////////
    //   STATIC METHODS
    /////////////////

    static Map<String, dWorld> worlds = new HashMap<String, dWorld>();

    public static dWorld mirrorBukkitWorld(World world) {
        if (world == null) {
            return null;
        }
        if (worlds.containsKey(world.getName())) {
            return worlds.get(world.getName());
        }
        else {
            return new dWorld(world);
        }
    }


    /////////////////////
    //   OBJECT FETCHER
    /////////////////

    // <--[language]
    // @name w@
    // @group Object Fetcher System
    // @description
    // w@ refers to the 'object identifier' of a dWorld. The 'w@' is notation for Denizen's Object
    // Fetcher. The only valid constructor for a dWorld is the name of the world it should be
    // associated with. For example, to reference the world named 'world1', use w@world1.
    // World names are case insensitive.
    // -->


    public static dWorld valueOf(String string) {
        return valueOf(string, null);
    }

    @Fetchable("w")
    public static dWorld valueOf(String string, TagContext context) {
        return valueOf(string, context == null || context.debug);
    }

    public static dWorld valueOf(String string, boolean announce) {
        if (string == null) {
            return null;
        }

        string = string.replace("w@", "");

        ////////
        // Match world name

        World returnable = null;

        for (World world : Bukkit.getWorlds()) {
            if (world.getName().equalsIgnoreCase(string)) {
                returnable = world;
            }
        }

        if (returnable != null) {
            if (worlds.containsKey(returnable.getName())) {
                return worlds.get(returnable.getName());
            }
            else {
                return new dWorld(returnable);
            }
        }
        else if (announce) {
            dB.echoError("Invalid World! '" + string
                    + "' could not be found.");
        }

        return null;
    }


    public static boolean matches(String arg) {

        arg = arg.replace("w@", "");

        World returnable = null;

        for (World world : Bukkit.getWorlds()) {
            if (world.getName().equalsIgnoreCase(arg)) {
                returnable = world;
            }
        }

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
        if (prefix == null) {
            this.prefix = "World";
        }
        else {
            this.prefix = prefix;
        }
        this.world_name = world.getName();
        if (!worlds.containsKey(world.getName())) {
            worlds.put(world.getName(), this);
        }
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
    public String identifySimple() {
        return identify();
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

    public static void registerTags() {

        /////////////////////
        //   ENTITY LIST ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <w@world.entities>
        // @returns dList(dEntity)
        // @description
        // Returns a list of entities in this world.
        // -->
        registerTag("entities", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                ArrayList<dEntity> entities = new ArrayList<dEntity>();

                for (Entity entity : ((dWorld) object).getWorld().getEntities()) {
                    entities.add(new dEntity(entity));
                }

                return new dList(entities)
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <w@world.living_entities>
        // @returns dList(dEntity)
        // @description
        // Returns a list of living entities in this world.
        // -->
        registerTag("living_entities", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                ArrayList<dEntity> entities = new ArrayList<dEntity>();

                for (Entity entity : ((dWorld) object).getWorld().getLivingEntities()) {
                    entities.add(new dEntity(entity));
                }

                return new dList(entities)
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <w@world.players>
        // @returns dList(dPlayer)
        // @description
        // Returns a list of online players in this world.
        // -->
        registerTag("players", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                ArrayList<dPlayer> players = new ArrayList<dPlayer>();

                for (Player player : ((dWorld) object).getWorld().getPlayers()) {
                    if (!dEntity.isNPC(player)) {
                        players.add(new dPlayer(player));
                    }
                }

                return new dList(players)
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <w@world.spawned_npcs>
        // @returns dList(dNPC)
        // @description
        // Returns a list of spawned NPCs in this world.
        // -->
        registerTag("spawned_npcs", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                ArrayList<dNPC> npcs = new ArrayList<dNPC>();

                World thisWorld = ((dWorld) object).getWorld();

                for (NPC npc : CitizensAPI.getNPCRegistry()) {
                    if (npc.isSpawned() && npc.getEntity().getLocation().getWorld().equals(thisWorld)) {
                        npcs.add(dNPC.mirrorCitizensNPC(npc));
                    }
                }

                return new dList(npcs)
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <w@world.npcs>
        // @returns dList(dNPC)
        // @description
        // Returns a list of all NPCs in this world.
        // -->
        registerTag("npcs", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                ArrayList<dNPC> npcs = new ArrayList<dNPC>();

                World thisWorld = ((dWorld) object).getWorld();

                for (NPC npc : CitizensAPI.getNPCRegistry()) {
                    Location location = npc.getStoredLocation();
                    if (location != null) {
                        World world = location.getWorld();
                        if (world != null && world.equals(thisWorld)) {
                            npcs.add(dNPC.mirrorCitizensNPC(npc));
                        }
                    }
                }

                return new dList(npcs)
                        .getAttribute(attribute.fulfill(1));
            }
        });


        /////////////////////
        //   GEOGRAPHY ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <w@world.can_generate_structures>
        // @returns Element(Boolean)
        // @description
        // Returns whether the world will generate structures.
        // -->
        registerTag("can_generate_structures", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dWorld) object).getWorld().canGenerateStructures())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <w@world.loaded_chunks>
        // @returns dList(dChunk)
        // @description
        // Returns a list of all the currently loaded chunks.
        // -->
        registerTag("loaded_chunks", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                dList chunks = new dList();
                for (Chunk ent : ((dWorld) object).getWorld().getLoadedChunks()) {
                    chunks.add(new dChunk(ent).identify());
                }

                return chunks.getAttribute(attribute.fulfill(1));
            }
        });
        // <--[tag]
        // @attribute <w@world.random_loaded_chunk>
        // @returns dChunk
        // @description
        // Returns a random loaded chunk.
        // -->
        registerTag("random_loaded_chunk", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                int random = CoreUtilities.getRandom().nextInt(((dWorld) object).getWorld().getLoadedChunks().length);
                return new dChunk(((dWorld) object).getWorld().getLoadedChunks()[random])
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <w@world.sea_level>
        // @returns Element(Number)
        // @description
        // Returns the level of the sea.
        // -->
        registerTag("sea_level", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dWorld) object).getWorld().getSeaLevel())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <w@world.spawn_location>
        // @returns dLocation
        // @description
        // Returns the spawn location of the world.
        // -->
        registerTag("spawn_location", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new dLocation(((dWorld) object).getWorld().getSpawnLocation())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <-- [tag]
        // @attribute <w@world.world_type>
        // @returns Element
        // @description
        // Returns the world type of the world.
        // Can return any enum from: <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/WorldType.html>
        // -->
        registerTag("world_type", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dWorld) object).getWorld().getWorldType().getName())
                        .getAttribute(attribute.fulfill(1));
            }
        });


        /////////////////////
        //   IDENTIFICATION ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <w@world.name>
        // @returns Element
        // @description
        // Returns the name of the world.
        // -->
        registerTag("name", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dWorld) object).getWorld().getName())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <w@world.seed>
        // @returns Element
        // @description
        // Returns the world seed.
        // -->
        registerTag("seed", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dWorld) object).getWorld().getSeed())
                        .getAttribute(attribute.fulfill(1));
            }
        });


        /////////////////////
        //   SETTINGS ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <w@world.allows_animals>
        // @returns Element(Boolean)
        // @description
        // Returns whether animals can spawn in this world.
        // -->
        registerTag("allows_animals", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dWorld) object).getWorld().getAllowAnimals())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <w@world.allows_monsters>
        // @returns Element(Boolean)
        // @description
        // Returns whether monsters can spawn in this world.
        // -->
        registerTag("allows_monsters", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dWorld) object).getWorld().getAllowMonsters())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <w@world.allows_pvp>
        // @returns Element(Boolean)
        // @description
        // Returns whether player versus player combat is allowed in this world.
        // -->
        registerTag("allows_pvp", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dWorld) object).getWorld().getPVP())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <w@world.auto_save>
        // @returns Element(Boolean)
        // @description
        // Returns whether the world automatically saves.
        // -->
        registerTag("auto_save", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dWorld) object).getWorld().isAutoSave())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <w@world.ambient_spawn_limit>
        // @returns Element(Number)
        // @description
        // Returns the number of ambient mobs that can spawn in a chunk in this world.
        // -->
        registerTag("ambient_spawn_limit", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dWorld) object).getWorld().getAmbientSpawnLimit())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <w@world.animal_spawn_limit>
        // @returns Element(Number)
        // @description
        // Returns the number of animals that can spawn in a chunk in this world.
        // -->
        registerTag("animal_spawn_limit", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dWorld) object).getWorld().getAnimalSpawnLimit())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <w@world.monster_spawn_limit>
        // @returns Element(Number)
        // @description
        // Returns the number of monsters that can spawn in a chunk in this world.
        // -->
        registerTag("monster_spawn_limit", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dWorld) object).getWorld().getMonsterSpawnLimit())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <w@world.water_animal_spawn_limit>
        // @returns Element(Number)
        // @description
        // Returns the number of water animals that can spawn in a chunk in this world.
        // -->
        registerTag("water_animal_spawn_limit", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dWorld) object).getWorld().getWaterAnimalSpawnLimit())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <w@world.difficulty>
        // @returns Element
        // @description
        // Returns the name of the difficulty level.
        // -->
        registerTag("difficulty", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dWorld) object).getWorld().getDifficulty().name())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <w@world.keep_spawn>
        // @returns Element(Boolean)
        // @description
        // Returns whether the world's spawn area should be kept loaded into memory.
        // -->
        registerTag("keep_spawn", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dWorld) object).getWorld().getKeepSpawnInMemory())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <w@world.max_height>
        // @returns Element(Number)
        // @description
        // Returns the maximum height of this world.
        // -->
        registerTag("max_height", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dWorld) object).getWorld().getMaxHeight())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <w@world.ticks_per_animal_spawn>
        // @returns Duration
        // @description
        // Returns the world's ticks per animal spawn value.
        // -->
        registerTag("ticks_per_animal_spawn", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Duration(((dWorld) object).getWorld().getTicksPerAnimalSpawns())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <w@world.ticks_per_monster_spawn>
        // @returns Duration
        // @description
        // Returns the world's ticks per monster spawn value.
        // -->
        registerTag("ticks_per_monster_spawn", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Duration(((dWorld) object).getWorld().getTicksPerMonsterSpawns())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        /////////////////////
        //   TIME ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <w@world.time>
        // @returns Element(Number)
        // @description
        // Returns the relative in-game time of this world.
        // -->
        registerTag("time", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                attribute = attribute.fulfill(1);

                // <--[tag]
                // @attribute <w@world.time.duration>
                // @returns Durations
                // @description
                // Returns the relative in-game time of this world as a duration.
                // -->
                if (attribute.startsWith("duration")) {
                    return new Duration(((dWorld) object).getWorld().getTime())
                            .getAttribute(attribute.fulfill(1));
                }

                // <--[tag]
                // @attribute <w@world.time.full>
                // @returns Duration
                // @description
                // Returns the in-game time of this world.
                // -->
                else if (attribute.startsWith("full")) {
                    return new Element(((dWorld) object).getWorld().getFullTime())
                            .getAttribute(attribute.fulfill(1));
                }

                // <--[tag]
                // @attribute <w@world.time.period>
                // @returns Element
                // @description
                // Returns the time as 'day', 'night', 'dawn', or 'dusk'.
                // -->
                else if (attribute.startsWith("period")) {

                    long time = ((dWorld) object).getWorld().getTime();
                    String period;

                    if (time >= 23000) {
                        period = "dawn";
                    }
                    else if (time >= 13500) {
                        period = "night";
                    }
                    else if (time >= 12500) {
                        period = "dusk";
                    }
                    else {
                        period = "day";
                    }

                    return new Element(period).getAttribute(attribute.fulfill(1));
                }
                else {
                    return new Element(((dWorld) object).getWorld().getTime()).getAttribute(attribute);
                }
            }
        });

        // <--[tag]
        // @attribute <w@world.moon_phase>
        // @returns Element(Number)
        // @description
        // Returns the current phase of the moon, as an integer from 1 to 8.
        // -->
        registerTag("moon_phase", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element((int) ((((dWorld) object).getWorld().getFullTime() / 24000) % 8) + 1)
                        .getAttribute(attribute.fulfill(1));
            }
        });
        registerTag("moonphase", registeredTags.get("moon_phase"));

        /////////////////////
        //   WEATHER ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <w@world.has_storm>
        // @returns Element(Boolean)
        // @description
        // Returns whether there is currently a storm in this world.
        // -->
        registerTag("has_storm", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dWorld) object).getWorld().hasStorm())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <w@world.thunder_duration>
        // @returns Duration
        // @description
        // Returns the duration of thunder.
        // -->
        registerTag("thunder_duration", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Duration((long) ((dWorld) object).getWorld().getThunderDuration())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <w@world.thundering>
        // @returns Element(Boolean)
        // @description
        // Returns whether it is currently thundering in this world.
        // -->
        registerTag("thundering", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dWorld) object).getWorld().isThundering())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <w@world.weather_duration>
        // @returns Duration
        // @description
        // Returns the duration of storms.
        // -->
        registerTag("weather_duration", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Duration((long) ((dWorld) object).getWorld().getWeatherDuration())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <w@world.environment>
        // @returns Element
        // @description
        // Returns the environment of the world: NORMAL, NETHER, or THE_END.
        // -->
        registerTag("environment", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dWorld) object).getWorld().getEnvironment().name())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        /////////////////////
        //   DEBUG TAGS
        /////////////////

        // <--[tag]
        // @attribute <w@world.prefix>
        // @returns Element
        // @description
        // Returns the prefix of the world dObject.
        // -->
        registerTag("prefix", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dWorld) object).prefix)
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <w@world.type>
        // @returns Element
        // @description
        // Always returns 'World' for dWorld objects. All objects fetchable by the Object Fetcher will return the
        // type of object that is fulfilling this attribute.
        // -->
        registerTag("type", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element("World").getAttribute(attribute.fulfill(1));
            }
        });

        /////////////////////
        //   WORLD BORDER ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <w@world.border_size>
        // @returns Element
        // @description
        // returns the size of the world border in this world.
        // -->
        registerTag("border_size", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dWorld) object).getWorld().getWorldBorder().getSize())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <w@world.border_center>
        // @returns dLocation
        // @description
        // returns the center of the world border in this world.
        // -->
        registerTag("border_center", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new dLocation(((dWorld) object).getWorld().getWorldBorder().getCenter())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <w@world.border_damage>
        // @returns Element
        // @description
        // returns the size of the world border in this world.
        // -->
        registerTag("border_damage", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dWorld) object).getWorld().getWorldBorder().getDamageAmount())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <w@world.border_damage_buffer>
        // @returns Element
        // @description
        // returns the damage buffer of the world border in this world.
        // -->
        registerTag("border_damage_buffer", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dWorld) object).getWorld().getWorldBorder().getDamageBuffer())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <w@world.border_warning_distance>
        // @returns Element
        // @description
        // returns the warning distance of the world border in this world.
        // -->
        registerTag("border_warning_distance", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dWorld) object).getWorld().getWorldBorder().getWarningDistance())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <w@world.border_warning_time>
        // @returns Duration
        // @description
        // returns warning time of the world border in this world as a duration.
        // -->
        registerTag("border_warning_time", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Duration(((dWorld) object).getWorld().getWorldBorder().getWarningTime())
                        .getAttribute(attribute.fulfill(1));
            }
        });

    }

    public static HashMap<String, TagRunnable> registeredTags = new HashMap<String, TagRunnable>();

    public static void registerTag(String name, TagRunnable runnable) {
        if (runnable.name == null) {
            runnable.name = name;
        }
        registeredTags.put(name, runnable);
    }

    @Override
    public String getAttribute(Attribute attribute) {
        if (attribute == null) {
            return null;
        }

        // TODO: Scrap getAttribute, make this functionality a core system
        String attrLow = CoreUtilities.toLowerCase(attribute.getAttributeWithoutContext(1));
        TagRunnable tr = registeredTags.get(attrLow);
        if (tr != null) {
            if (!tr.name.equals(attrLow)) {
                net.aufdemrand.denizencore.utilities.debugging.dB.echoError(attribute.getScriptEntry() != null ? attribute.getScriptEntry().getResidingQueue() : null,
                        "Using deprecated form of tag '" + tr.name + "': '" + attrLow + "'.");
            }
            return tr.run(attribute, this);
        }

        // Iterate through this object's properties' attributes
        for (Property property : PropertyParser.getProperties(this)) {
            String returned = property.getAttribute(attribute);
            if (returned != null) {
                return returned;
            }
        }

        return new Element(identify()).getAttribute(attribute);
    }


    public void applyProperty(Mechanism mechanism) {
        dB.echoError("Cannot apply properties to a world!");
    }

    @Override
    public void adjust(Mechanism mechanism) {

        Element value = mechanism.getValue();

        // <--[mechanism]
        // @object dWorld
        // @name ambient_spawn_limit
        // @input Element(Integer)
        // @description
        // Sets the limit for number of ambient mobs that can spawn in a chunk in this world.
        // @tags
        // <w@world.ambient_spawn_limit>
        // -->
        if (mechanism.matches("ambient_spawn_limit")
                && mechanism.requireInteger()) {
            getWorld().setAmbientSpawnLimit(value.asInt());
        }

        // <--[mechanism]
        // @object dWorld
        // @name animal_spawn_limit
        // @input Element(Integer)
        // @description
        // Sets the limit for number of animals that can spawn in a chunk in this world.
        // @tags
        // <w@world.animal_spawn_limit>
        // -->
        if (mechanism.matches("animal_spawn_limit")
                && mechanism.requireInteger()) {
            getWorld().setAnimalSpawnLimit(value.asInt());
        }

        // <--[mechanism]
        // @object dWorld
        // @name auto_save
        // @input Element(Boolean)
        // @description
        // Sets whether the world will automatically save edits.
        // @tags
        // <w@world.auto_save>
        // -->
        if (mechanism.matches("auto_save")
                && mechanism.requireBoolean()) {
            getWorld().setAutoSave(value.asBoolean());
        }

        // <--[mechanism]
        // @object dWorld
        // @name difficulty
        // @input Element
        // @description
        // Sets the difficulty level of this world.
        // Possible values: Peaceful, Easy, Normal, Hard.
        // @tags
        // <w@world.difficulty>
        // -->
        if (mechanism.matches("difficulty") && mechanism.requireEnum(true, Difficulty.values())) {
            String upper = value.asString().toUpperCase();
            Difficulty diff;
            if (upper.matches("(PEACEFUL|EASY|NORMAL|HARD)")) {
                diff = Difficulty.valueOf(upper);
            }
            else {
                diff = Difficulty.getByValue(value.asInt());
            }
            if (diff != null) {
                getWorld().setDifficulty(diff);
            }
        }

        // <--[mechanism]
        // @object dWorld
        // @name save
        // @input None
        // @description
        // Saves the world to file.
        // @tags
        // None
        // -->
        if (mechanism.matches("save")) {
            getWorld().save();
        }

        // <--[mechanism]
        // @object dWorld
        // @name destroy
        // @input None
        // @description
        // Unloads the world from the server without saving chunks, then destroys all data that is part of the world.
        // Require config setting 'Commands.Delete.Allow file deletion'.
        // @tags
        // None
        // -->
        if (mechanism.matches("destroy")) {
            if (!Settings.allowDelete()) {
                dB.echoError("Unable to delete due to config.");
                return;
            }
            File folder = new File(getWorld().getName());
            Bukkit.getServer().unloadWorld(getWorld(), false);
            try {
                FileUtils.deleteDirectory(folder);
            }
            catch (Exception ex) {
                dB.echoError(ex);
            }
            return;
        }

        // <--[mechanism]
        // @object dWorld
        // @name force_unload
        // @input None
        // @description
        // Unloads the world from the server without saving chunks.
        // @tags
        // None
        // -->
        if (mechanism.matches("force_unload")) {
            Bukkit.getServer().unloadWorld(getWorld(), false);
            return;
        }

        // <--[mechanism]
        // @object dWorld
        // @name full_time
        // @input Element(Integer)
        // @description
        // Sets the in-game time on the server.
        // @tags
        // <w@world.time.full>
        // -->
        if (mechanism.matches("full_time") && mechanism.requireInteger()) {
            getWorld().setFullTime(value.asInt());
        }

        // <--[mechanism]
        // @object dWorld
        // @name keep_spawn
        // @input Element(Boolean)
        // @description
        // Sets whether the world's spawn area should be kept loaded into memory.
        // @tags
        // <w@world.time.full>
        // -->
        if (mechanism.matches("keep_spawn") && mechanism.requireBoolean()) {
            getWorld().setKeepSpawnInMemory(value.asBoolean());
        }

        // <--[mechanism]
        // @object dWorld
        // @name monster_spawn_limit
        // @input Element(Integer)
        // @description
        // Sets the limit for number of monsters that can spawn in a chunk in this world.
        // @tags
        // <w@world.monster_spawn_limit>
        // -->
        if (mechanism.matches("monster_spawn_limit") && mechanism.requireInteger()) {
            getWorld().setMonsterSpawnLimit(value.asInt());
        }

        // <--[mechanism]
        // @object dWorld
        // @name allow_pvp
        // @input Element(Boolean)
        // @description
        // Sets whether player versus player combat is allowed in this world.
        // @tags
        // <w@world.allows_pvp>
        // -->
        if (mechanism.matches("allow_pvp") && mechanism.requireBoolean()) {
            getWorld().setPVP(value.asBoolean());
        }

        // <--[mechanism]
        // @object dWorld
        // @name spawn_location
        // @input dLocation
        // @description
        // Sets the spawn location of this world. (This ignores the world value of the dLocation.)
        // @tags
        // <w@world.spawn_location>
        // -->
        if (mechanism.matches("spawn_location") && mechanism.requireObject(dLocation.class)) {
            dLocation loc = value.asType(dLocation.class);
            getWorld().setSpawnLocation(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        }

        // <--[mechanism]
        // @object dWorld
        // @name storming
        // @input Element(Boolean)
        // @description
        // Sets whether there is a storm.
        // @tags
        // <w@world.has_storm>
        // -->
        if (mechanism.matches("storming") && mechanism.requireBoolean()) {
            getWorld().setStorm(value.asBoolean());
        }

        // <--[mechanism]
        // @object dWorld
        // @name thunder_duration
        // @input Duration
        // @description
        // Sets the duration of thunder.
        // @tags
        // <w@world.thunder_duration>
        // -->
        if (mechanism.matches("thunder_duration") && mechanism.requireObject(Duration.class)) {
            getWorld().setThunderDuration(value.asType(Duration.class).getTicksAsInt());
        }

        // <--[mechanism]
        // @object dWorld
        // @name thundering
        // @input Element(Boolean)
        // @description
        // Sets whether it is thundering.
        // @tags
        // <w@world.thundering>
        // -->
        if (mechanism.matches("thundering") && mechanism.requireBoolean()) {
            getWorld().setThundering(value.asBoolean());
        }

        // <--[mechanism]
        // @object dWorld
        // @name ticks_per_animal_spawns
        // @input Duration
        // @description
        // Sets the time between animal spawns.
        // @tags
        // <w@world.ticks_per_animal_spawns>
        // -->
        if (mechanism.matches("ticks_per_animal_spawns") && mechanism.requireObject(Duration.class)) {
            getWorld().setTicksPerAnimalSpawns(value.asType(Duration.class).getTicksAsInt());
        }

        // <--[mechanism]
        // @object dWorld
        // @name ticks_per_monster_spawns
        // @input Duration
        // @description
        // Sets the time between monster spawns.
        // @tags
        // <w@world.ticks_per_monster_spawns>
        // -->
        if (mechanism.matches("ticks_per_monster_spawns") && mechanism.requireObject(Duration.class)) {
            getWorld().setTicksPerMonsterSpawns(value.asType(Duration.class).getTicksAsInt());
        }

        // <--[mechanism]
        // @object dWorld
        // @name time
        // @input Element(Integer)
        // @description
        // Sets the relative in-game time on the server.
        // @tags
        // <w@world.time>
        // -->
        if (mechanism.matches("time") && mechanism.requireInteger()) {
            getWorld().setTime(value.asInt());
        }

        // <--[mechanism]
        // @object dWorld
        // @name unload
        // @input None
        // @description
        // Unloads the world from the server and saves chunks.
        // @tags
        // None
        // -->
        if (mechanism.matches("unload")) {
            Bukkit.getServer().unloadWorld(getWorld(), true);
            return;
        }

        // <--[mechanism]
        // @object dWorld
        // @name water_animal_spawn_limit
        // @input Element(Integer)
        // @description
        // Sets the limit for number of water animals that can spawn in a chunk in this world.
        // @tags
        // <w@world.water_animal_spawn_limit>
        // -->
        if (mechanism.matches("water_animal_spawn_limit") && mechanism.requireInteger()) {
            getWorld().setWaterAnimalSpawnLimit(value.asInt());
        }

        // <--[mechanism]
        // @object dWorld
        // @name weather_duration
        // @input Duration
        // @description
        // Set the remaining time in ticks of the current conditions.
        // @tags
        // <w@world.weather_duration>
        // -->
        if (mechanism.matches("weather_duration") && mechanism.requireObject(Duration.class)) {
            getWorld().setWeatherDuration(value.asType(Duration.class).getTicksAsInt());
        }

        // Iterate through this object's properties' mechanisms
        for (Property property : PropertyParser.getProperties(this)) {
            property.adjust(mechanism);
            if (mechanism.fulfilled()) {
                break;
            }
        }

        if (!mechanism.fulfilled()) {
            mechanism.reportInvalid();
        }

    }
}
