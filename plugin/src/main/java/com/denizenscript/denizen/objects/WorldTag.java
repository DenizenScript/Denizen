package com.denizenscript.denizen.objects;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizen.Settings;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorldTag implements ObjectTag, Adjustable {


    /////////////////////
    //   STATIC METHODS
    /////////////////

    static Map<String, WorldTag> worlds = new HashMap<>();

    public static WorldTag mirrorBukkitWorld(World world) {
        if (world == null) {
            return null;
        }
        if (worlds.containsKey(world.getName())) {
            return worlds.get(world.getName());
        }
        else {
            return new WorldTag(world);
        }
    }


    /////////////////////
    //   OBJECT FETCHER
    /////////////////

    // <--[language]
    // @name WorldTag
    // @group Object System
    // @description
    // A WorldTag represents a world on the server.
    //
    // For format info, see <@link language w@>
    //
    // -->

    // <--[language]
    // @name w@
    // @group Object Fetcher System
    // @description
    // w@ refers to the 'object identifier' of a WorldTag. The 'w@' is notation for Denizen's Object
    // Fetcher. The only valid constructor for a WorldTag is the name of the world it should be
    // associated with. For example, to reference the world named 'world1', use WorldTag1.
    // World names are case insensitive.
    //
    // For general info, see <@link language WorldTag>
    // -->


    public static WorldTag valueOf(String string) {
        return valueOf(string, null);
    }

    @Fetchable("w")
    public static WorldTag valueOf(String string, TagContext context) {
        return valueOf(string, context == null || context.debug);
    }

    public static WorldTag valueOf(String string, boolean announce) {
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
                return new WorldTag(returnable);
            }
        }
        else if (announce) {
            Debug.echoError("Invalid World! '" + string
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

    public List<Entity> getEntitiesForTag() {
        NMSHandler.getChunkHelper().changeChunkServerThread(getWorld());
        try {
            return getWorld().getEntities();
        }
        finally {
            NMSHandler.getChunkHelper().restoreServerThread(getWorld());
        }
    }

    private String prefix;
    String world_name;

    public WorldTag(World world) {
        this(null, world);
    }

    public WorldTag(String prefix, World world) {
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
    public ObjectTag setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public static void registerTags() {

        /////////////////////
        //   ENTITY LIST ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <WorldTag.entities>
        // @returns ListTag(EntityTag)
        // @description
        // Returns a list of entities in this world.
        // -->
        registerTag("entities", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                ArrayList<EntityTag> entities = new ArrayList<>();

                for (Entity entity : ((WorldTag) object).getEntitiesForTag()) {
                    entities.add(new EntityTag(entity));
                }

                return new ListTag(entities)
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <WorldTag.living_entities>
        // @returns ListTag(EntityTag)
        // @description
        // Returns a list of living entities in this world.
        // -->
        registerTag("living_entities", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                ArrayList<EntityTag> entities = new ArrayList<>();

                for (Entity entity : ((WorldTag) object).getWorld().getLivingEntities()) {
                    entities.add(new EntityTag(entity));
                }

                return new ListTag(entities)
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <WorldTag.players>
        // @returns ListTag(PlayerTag)
        // @description
        // Returns a list of online players in this world.
        // -->
        registerTag("players", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                ArrayList<PlayerTag> players = new ArrayList<>();

                for (Player player : ((WorldTag) object).getWorld().getPlayers()) {
                    if (!EntityTag.isNPC(player)) {
                        players.add(new PlayerTag(player));
                    }
                }

                return new ListTag(players)
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <WorldTag.spawned_npcs>
        // @returns ListTag(NPCTag)
        // @description
        // Returns a list of spawned NPCs in this world.
        // -->
        registerTag("spawned_npcs", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                ArrayList<NPCTag> npcs = new ArrayList<>();

                World thisWorld = ((WorldTag) object).getWorld();

                for (NPC npc : CitizensAPI.getNPCRegistry()) {
                    if (npc.isSpawned() && npc.getEntity().getLocation().getWorld().equals(thisWorld)) {
                        npcs.add(NPCTag.mirrorCitizensNPC(npc));
                    }
                }

                return new ListTag(npcs)
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <WorldTag.npcs>
        // @returns ListTag(NPCTag)
        // @description
        // Returns a list of all NPCs in this world.
        // -->
        registerTag("npcs", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                ArrayList<NPCTag> npcs = new ArrayList<>();

                World thisWorld = ((WorldTag) object).getWorld();

                for (NPC npc : CitizensAPI.getNPCRegistry()) {
                    Location location = npc.getStoredLocation();
                    if (location != null) {
                        World world = location.getWorld();
                        if (world != null && world.equals(thisWorld)) {
                            npcs.add(NPCTag.mirrorCitizensNPC(npc));
                        }
                    }
                }

                return new ListTag(npcs)
                        .getAttribute(attribute.fulfill(1));
            }
        });


        /////////////////////
        //   GEOGRAPHY ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <WorldTag.can_generate_structures>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the world will generate structures.
        // -->
        registerTag("can_generate_structures", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((WorldTag) object).getWorld().canGenerateStructures())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <WorldTag.loaded_chunks>
        // @returns ListTag(ChunkTag)
        // @description
        // Returns a list of all the currently loaded chunks.
        // -->
        registerTag("loaded_chunks", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                ListTag chunks = new ListTag();
                for (Chunk ent : ((WorldTag) object).getWorld().getLoadedChunks()) {
                    chunks.add(new ChunkTag(ent).identify());
                }

                return chunks.getAttribute(attribute.fulfill(1));
            }
        });
        // <--[tag]
        // @attribute <WorldTag.random_loaded_chunk>
        // @returns ChunkTag
        // @description
        // Returns a random loaded chunk.
        // -->
        registerTag("random_loaded_chunk", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                int random = CoreUtilities.getRandom().nextInt(((WorldTag) object).getWorld().getLoadedChunks().length);
                return new ChunkTag(((WorldTag) object).getWorld().getLoadedChunks()[random])
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <WorldTag.sea_level>
        // @returns ElementTag(Number)
        // @description
        // Returns the level of the sea.
        // -->
        registerTag("sea_level", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((WorldTag) object).getWorld().getSeaLevel())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <WorldTag.spawn_location>
        // @returns LocationTag
        // @description
        // Returns the spawn location of the world.
        // -->
        registerTag("spawn_location", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                return new LocationTag(((WorldTag) object).getWorld().getSpawnLocation())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <-- [tag]
        // @attribute <WorldTag.world_type>
        // @returns ElementTag
        // @description
        // Returns the world type of the world.
        // Can return any enum from: <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/WorldType.html>
        // -->
        registerTag("world_type", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((WorldTag) object).getWorld().getWorldType().getName())
                        .getAttribute(attribute.fulfill(1));
            }
        });


        /////////////////////
        //   IDENTIFICATION ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <WorldTag.name>
        // @returns ElementTag
        // @description
        // Returns the name of the world.
        // -->
        registerTag("name", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((WorldTag) object).getWorld().getName())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <WorldTag.seed>
        // @returns ElementTag
        // @description
        // Returns the world seed.
        // -->
        registerTag("seed", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((WorldTag) object).getWorld().getSeed())
                        .getAttribute(attribute.fulfill(1));
            }
        });


        /////////////////////
        //   SETTINGS ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <WorldTag.allows_animals>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether animals can spawn in this world.
        // -->
        registerTag("allows_animals", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((WorldTag) object).getWorld().getAllowAnimals())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <WorldTag.allows_monsters>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether monsters can spawn in this world.
        // -->
        registerTag("allows_monsters", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((WorldTag) object).getWorld().getAllowMonsters())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <WorldTag.allows_pvp>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether player versus player combat is allowed in this world.
        // -->
        registerTag("allows_pvp", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((WorldTag) object).getWorld().getPVP())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <WorldTag.auto_save>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the world automatically saves.
        // -->
        registerTag("auto_save", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((WorldTag) object).getWorld().isAutoSave())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <WorldTag.ambient_spawn_limit>
        // @returns ElementTag(Number)
        // @description
        // Returns the number of ambient mobs that can spawn in a chunk in this world.
        // -->
        registerTag("ambient_spawn_limit", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((WorldTag) object).getWorld().getAmbientSpawnLimit())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <WorldTag.animal_spawn_limit>
        // @returns ElementTag(Number)
        // @description
        // Returns the number of animals that can spawn in a chunk in this world.
        // -->
        registerTag("animal_spawn_limit", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((WorldTag) object).getWorld().getAnimalSpawnLimit())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <WorldTag.monster_spawn_limit>
        // @returns ElementTag(Number)
        // @description
        // Returns the number of monsters that can spawn in a chunk in this world.
        // -->
        registerTag("monster_spawn_limit", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((WorldTag) object).getWorld().getMonsterSpawnLimit())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <WorldTag.water_animal_spawn_limit>
        // @returns ElementTag(Number)
        // @description
        // Returns the number of water animals that can spawn in a chunk in this world.
        // -->
        registerTag("water_animal_spawn_limit", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((WorldTag) object).getWorld().getWaterAnimalSpawnLimit())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <WorldTag.difficulty>
        // @returns ElementTag
        // @description
        // Returns the name of the difficulty level.
        // -->
        registerTag("difficulty", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((WorldTag) object).getWorld().getDifficulty().name())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <WorldTag.keep_spawn>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the world's spawn area should be kept loaded into memory.
        // -->
        registerTag("keep_spawn", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((WorldTag) object).getWorld().getKeepSpawnInMemory())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <WorldTag.max_height>
        // @returns ElementTag(Number)
        // @description
        // Returns the maximum height of this world.
        // -->
        registerTag("max_height", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((WorldTag) object).getWorld().getMaxHeight())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <WorldTag.ticks_per_animal_spawn>
        // @returns DurationTag
        // @description
        // Returns the world's ticks per animal spawn mechanism.getValue().
        // -->
        registerTag("ticks_per_animal_spawn", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                return new DurationTag(((WorldTag) object).getWorld().getTicksPerAnimalSpawns())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <WorldTag.ticks_per_monster_spawn>
        // @returns DurationTag
        // @description
        // Returns the world's ticks per monster spawn mechanism.getValue().
        // -->
        registerTag("ticks_per_monster_spawn", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                return new DurationTag(((WorldTag) object).getWorld().getTicksPerMonsterSpawns())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        /////////////////////
        //   TIME ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <WorldTag.time>
        // @returns ElementTag(Number)
        // @description
        // Returns the relative in-game time of this world.
        // -->
        registerTag("time", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                attribute = attribute.fulfill(1);

                // <--[tag]
                // @attribute <WorldTag.time.duration>
                // @returns DurationTags
                // @description
                // Returns the relative in-game time of this world as a duration.
                // -->
                if (attribute.startsWith("duration")) {
                    return new DurationTag(((WorldTag) object).getWorld().getTime())
                            .getAttribute(attribute.fulfill(1));
                }

                // <--[tag]
                // @attribute <WorldTag.time.full>
                // @returns DurationTag
                // @description
                // Returns the in-game time of this world.
                // -->
                else if (attribute.startsWith("full")) {
                    return new ElementTag(((WorldTag) object).getWorld().getFullTime())
                            .getAttribute(attribute.fulfill(1));
                }

                // <--[tag]
                // @attribute <WorldTag.time.period>
                // @returns ElementTag
                // @description
                // Returns the time as 'day', 'night', 'dawn', or 'dusk'.
                // -->
                else if (attribute.startsWith("period")) {

                    long time = ((WorldTag) object).getWorld().getTime();
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

                    return new ElementTag(period).getAttribute(attribute.fulfill(1));
                }
                else {
                    return new ElementTag(((WorldTag) object).getWorld().getTime()).getAttribute(attribute);
                }
            }
        });

        // <--[tag]
        // @attribute <WorldTag.moon_phase>
        // @returns ElementTag(Number)
        // @description
        // Returns the current phase of the moon, as an integer from 1 to 8.
        // -->
        registerTag("moon_phase", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                return new ElementTag((int) ((((WorldTag) object).getWorld().getFullTime() / 24000) % 8) + 1)
                        .getAttribute(attribute.fulfill(1));
            }
        });
        registerTag("moonphase", registeredTags.get("moon_phase"));

        /////////////////////
        //   WEATHER ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <WorldTag.has_storm>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether there is currently a storm in this world.
        // ie, whether it is raining. To check for thunder, use <@link tag WorldTag.thundering>.
        // -->
        registerTag("has_storm", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((WorldTag) object).getWorld().hasStorm())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <WorldTag.thunder_duration>
        // @returns DurationTag
        // @description
        // Returns the duration of thunder.
        // -->
        registerTag("thunder_duration", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                return new DurationTag((long) ((WorldTag) object).getWorld().getThunderDuration())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <WorldTag.thundering>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether it is currently thundering in this world.
        // -->
        registerTag("thundering", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((WorldTag) object).getWorld().isThundering())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <WorldTag.weather_duration>
        // @returns DurationTag
        // @description
        // Returns the duration of storms.
        // -->
        registerTag("weather_duration", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                return new DurationTag((long) ((WorldTag) object).getWorld().getWeatherDuration())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <WorldTag.environment>
        // @returns ElementTag
        // @description
        // Returns the environment of the world: NORMAL, NETHER, or THE_END.
        // -->
        registerTag("environment", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((WorldTag) object).getWorld().getEnvironment().name())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <WorldTag.type>
        // @returns ElementTag
        // @description
        // Always returns 'World' for WorldTag objects. All objects fetchable by the Object Fetcher will return the
        // type of object that is fulfilling this attribute.
        // -->
        registerTag("type", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                return new ElementTag("World").getAttribute(attribute.fulfill(1));
            }
        });

        /////////////////////
        //   WORLD BORDER ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <WorldTag.border_size>
        // @returns ElementTag
        // @description
        // returns the size of the world border in this world.
        // -->
        registerTag("border_size", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((WorldTag) object).getWorld().getWorldBorder().getSize())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <WorldTag.border_center>
        // @returns LocationTag
        // @description
        // returns the center of the world border in this world.
        // -->
        registerTag("border_center", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                return new LocationTag(((WorldTag) object).getWorld().getWorldBorder().getCenter())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <WorldTag.border_damage>
        // @returns ElementTag
        // @description
        // returns the size of the world border in this world.
        // -->
        registerTag("border_damage", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((WorldTag) object).getWorld().getWorldBorder().getDamageAmount())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <WorldTag.border_damage_buffer>
        // @returns ElementTag
        // @description
        // returns the damage buffer of the world border in this world.
        // -->
        registerTag("border_damage_buffer", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((WorldTag) object).getWorld().getWorldBorder().getDamageBuffer())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <WorldTag.border_warning_distance>
        // @returns ElementTag
        // @description
        // returns the warning distance of the world border in this world.
        // -->
        registerTag("border_warning_distance", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((WorldTag) object).getWorld().getWorldBorder().getWarningDistance())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <WorldTag.border_warning_time>
        // @returns DurationTag
        // @description
        // returns warning time of the world border in this world as a duration.
        // -->
        registerTag("border_warning_time", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                return new DurationTag(((WorldTag) object).getWorld().getWorldBorder().getWarningTime())
                        .getAttribute(attribute.fulfill(1));
            }
        });

    }

    public static HashMap<String, TagRunnable> registeredTags = new HashMap<>();

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
                com.denizenscript.denizencore.utilities.debugging.Debug.echoError(attribute.getScriptEntry() != null ? attribute.getScriptEntry().getResidingQueue() : null,
                        "Using deprecated form of tag '" + tr.name + "': '" + attrLow + "'.");
            }
            return tr.run(attribute, this);
        }

        String returned = CoreUtilities.autoPropertyTag(this, attribute);
        if (returned != null) {
            return returned;
        }

        return new ElementTag(identify()).getAttribute(attribute);
    }


    public void applyProperty(Mechanism mechanism) {
        Debug.echoError("Cannot apply properties to a world!");
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object WorldTag
        // @name ambient_spawn_limit
        // @input Element(Number)
        // @description
        // Sets the limit for number of ambient mobs that can spawn in a chunk in this world.
        // @tags
        // <WorldTag.ambient_spawn_limit>
        // -->
        if (mechanism.matches("ambient_spawn_limit")
                && mechanism.requireInteger()) {
            getWorld().setAmbientSpawnLimit(mechanism.getValue().asInt());
        }

        // <--[mechanism]
        // @object WorldTag
        // @name animal_spawn_limit
        // @input Element(Number)
        // @description
        // Sets the limit for number of animals that can spawn in a chunk in this world.
        // @tags
        // <WorldTag.animal_spawn_limit>
        // -->
        if (mechanism.matches("animal_spawn_limit")
                && mechanism.requireInteger()) {
            getWorld().setAnimalSpawnLimit(mechanism.getValue().asInt());
        }

        // <--[mechanism]
        // @object WorldTag
        // @name auto_save
        // @input Element(Boolean)
        // @description
        // Sets whether the world will automatically save edits.
        // @tags
        // <WorldTag.auto_save>
        // -->
        if (mechanism.matches("auto_save")
                && mechanism.requireBoolean()) {
            getWorld().setAutoSave(mechanism.getValue().asBoolean());
        }

        // <--[mechanism]
        // @object WorldTag
        // @name difficulty
        // @input Element
        // @description
        // Sets the difficulty level of this world.
        // Possible values: Peaceful, Easy, Normal, Hard.
        // @tags
        // <WorldTag.difficulty>
        // -->
        if (mechanism.matches("difficulty") && mechanism.requireEnum(true, Difficulty.values())) {
            String upper = mechanism.getValue().asString().toUpperCase();
            Difficulty diff;
            if (upper.matches("(PEACEFUL|EASY|NORMAL|HARD)")) {
                diff = Difficulty.valueOf(upper);
            }
            else {
                diff = Difficulty.getByValue(mechanism.getValue().asInt());
            }
            if (diff != null) {
                getWorld().setDifficulty(diff);
            }
        }

        // <--[mechanism]
        // @object WorldTag
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
        // @object WorldTag
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
                Debug.echoError("Unable to delete due to config.");
                return;
            }
            File folder = new File(getWorld().getName());
            Bukkit.getServer().unloadWorld(getWorld(), false);
            try {
                CoreUtilities.deleteDirectory(folder);
            }
            catch (Exception ex) {
                Debug.echoError(ex);
            }
            return;
        }

        // <--[mechanism]
        // @object WorldTag
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
        // @object WorldTag
        // @name full_time
        // @input Element(Number)
        // @description
        // Sets the in-game time on the server.
        // @tags
        // <WorldTag.time.full>
        // -->
        if (mechanism.matches("full_time") && mechanism.requireInteger()) {
            getWorld().setFullTime(mechanism.getValue().asInt());
        }

        // <--[mechanism]
        // @object WorldTag
        // @name keep_spawn
        // @input Element(Boolean)
        // @description
        // Sets whether the world's spawn area should be kept loaded into memory.
        // @tags
        // <WorldTag.time.full>
        // -->
        if (mechanism.matches("keep_spawn") && mechanism.requireBoolean()) {
            getWorld().setKeepSpawnInMemory(mechanism.getValue().asBoolean());
        }

        // <--[mechanism]
        // @object WorldTag
        // @name monster_spawn_limit
        // @input Element(Number)
        // @description
        // Sets the limit for number of monsters that can spawn in a chunk in this world.
        // @tags
        // <WorldTag.monster_spawn_limit>
        // -->
        if (mechanism.matches("monster_spawn_limit") && mechanism.requireInteger()) {
            getWorld().setMonsterSpawnLimit(mechanism.getValue().asInt());
        }

        // <--[mechanism]
        // @object WorldTag
        // @name allow_pvp
        // @input Element(Boolean)
        // @description
        // Sets whether player versus player combat is allowed in this world.
        // @tags
        // <WorldTag.allows_pvp>
        // -->
        if (mechanism.matches("allow_pvp") && mechanism.requireBoolean()) {
            getWorld().setPVP(mechanism.getValue().asBoolean());
        }

        // <--[mechanism]
        // @object WorldTag
        // @name spawn_location
        // @input LocationTag
        // @description
        // Sets the spawn location of this world. (This ignores the world value of the LocationTag.)
        // @tags
        // <WorldTag.spawn_location>
        // -->
        if (mechanism.matches("spawn_location") && mechanism.requireObject(LocationTag.class)) {
            LocationTag loc = mechanism.valueAsType(LocationTag.class);
            getWorld().setSpawnLocation(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        }

        // <--[mechanism]
        // @object WorldTag
        // @name storming
        // @input Element(Boolean)
        // @description
        // Sets whether there is a storm.
        // @tags
        // <WorldTag.has_storm>
        // -->
        if (mechanism.matches("storming") && mechanism.requireBoolean()) {
            getWorld().setStorm(mechanism.getValue().asBoolean());
        }

        // <--[mechanism]
        // @object WorldTag
        // @name thunder_duration
        // @input Duration
        // @description
        // Sets the duration of thunder.
        // @tags
        // <WorldTag.thunder_duration>
        // -->
        if (mechanism.matches("thunder_duration") && mechanism.requireObject(DurationTag.class)) {
            getWorld().setThunderDuration(mechanism.valueAsType(DurationTag.class).getTicksAsInt());
        }

        // <--[mechanism]
        // @object WorldTag
        // @name thundering
        // @input Element(Boolean)
        // @description
        // Sets whether it is thundering.
        // @tags
        // <WorldTag.thundering>
        // -->
        if (mechanism.matches("thundering") && mechanism.requireBoolean()) {
            getWorld().setThundering(mechanism.getValue().asBoolean());
        }

        // <--[mechanism]
        // @object WorldTag
        // @name ticks_per_animal_spawns
        // @input Duration
        // @description
        // Sets the time between animal spawns.
        // @tags
        // <WorldTag.ticks_per_animal_spawns>
        // -->
        if (mechanism.matches("ticks_per_animal_spawns") && mechanism.requireObject(DurationTag.class)) {
            getWorld().setTicksPerAnimalSpawns(mechanism.valueAsType(DurationTag.class).getTicksAsInt());
        }

        // <--[mechanism]
        // @object WorldTag
        // @name ticks_per_monster_spawns
        // @input Duration
        // @description
        // Sets the time between monster spawns.
        // @tags
        // <WorldTag.ticks_per_monster_spawns>
        // -->
        if (mechanism.matches("ticks_per_monster_spawns") && mechanism.requireObject(DurationTag.class)) {
            getWorld().setTicksPerMonsterSpawns(mechanism.valueAsType(DurationTag.class).getTicksAsInt());
        }

        // <--[mechanism]
        // @object WorldTag
        // @name time
        // @input Element(Number)
        // @description
        // Sets the relative in-game time on the server.
        // @tags
        // <WorldTag.time>
        // -->
        if (mechanism.matches("time") && mechanism.requireInteger()) {
            getWorld().setTime(mechanism.getValue().asInt());
        }

        // <--[mechanism]
        // @object WorldTag
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
        // @object WorldTag
        // @name water_animal_spawn_limit
        // @input Element(Number)
        // @description
        // Sets the limit for number of water animals that can spawn in a chunk in this world.
        // @tags
        // <WorldTag.water_animal_spawn_limit>
        // -->
        if (mechanism.matches("water_animal_spawn_limit") && mechanism.requireInteger()) {
            getWorld().setWaterAnimalSpawnLimit(mechanism.getValue().asInt());
        }

        // <--[mechanism]
        // @object WorldTag
        // @name weather_duration
        // @input Duration
        // @description
        // Set the remaining time in ticks of the current conditions.
        // @tags
        // <WorldTag.weather_duration>
        // -->
        if (mechanism.matches("weather_duration") && mechanism.requireObject(DurationTag.class)) {
            getWorld().setWeatherDuration(mechanism.valueAsType(DurationTag.class).getTicksAsInt());
        }

        CoreUtilities.autoPropertyMechanism(this, mechanism);

    }
}
