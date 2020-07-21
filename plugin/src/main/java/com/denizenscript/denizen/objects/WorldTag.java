package com.denizenscript.denizen.objects;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizen.utilities.Settings;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.tags.TagRunnable;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.Deprecations;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
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
    // @name WorldTag Objects
    // @group Object System
    // @description
    // A WorldTag represents a world on the server.
    //
    // These use the object notation "w@".
    // The identity format for worlds is the name of the world it should be
    // associated with. For example, to reference the world named 'world1', use simply 'world1'.
    // World names are case insensitive.
    //
    // -->

    @Deprecated
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

    public List<LivingEntity> getLivingEntitiesForTag() {
        NMSHandler.getChunkHelper().changeChunkServerThread(getWorld());
        try {
            return getWorld().getLivingEntities();
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
        // @attribute <WorldTag.entities[(<entity>|...)]>
        // @returns ListTag(EntityTag)
        // @description
        // Returns a list of entities in this world.
        // Optionally specify entity types to filter down to.
        // -->
        registerTag("entities", (attribute, object) -> {
            ListTag entities = new ListTag();
            ListTag typeFilter = attribute.hasContext(1) ? attribute.contextAsType(1, ListTag.class) : null;
            for (Entity entity : object.getEntitiesForTag()) {
                EntityTag current = new EntityTag(entity);
                if (typeFilter != null) {
                    for (String type : typeFilter) {
                        if (current.comparedTo(type)) {
                            entities.addObject(current.getDenizenObject());
                            break;
                        }
                    }
                }
                else {
                    entities.addObject(current.getDenizenObject());
                }
            }
            return entities;
        });

        // <--[tag]
        // @attribute <WorldTag.living_entities>
        // @returns ListTag(EntityTag)
        // @description
        // Returns a list of living entities in this world.
        // This includes Players, mobs, NPCs, etc., but excludes dropped items, experience orbs, etc.
        // -->
        registerTag("living_entities", (attribute, object) -> {
            ArrayList<EntityTag> entities = new ArrayList<>();

            for (Entity entity : object.getLivingEntitiesForTag()) {
                entities.add(new EntityTag(entity));
            }

            return new ListTag(entities);
        });

        // <--[tag]
        // @attribute <WorldTag.players>
        // @returns ListTag(PlayerTag)
        // @description
        // Returns a list of online players in this world.
        // -->
        registerTag("players", (attribute, object) -> {
            ArrayList<PlayerTag> players = new ArrayList<>();

            for (Player player : object.getWorld().getPlayers()) {
                if (!EntityTag.isNPC(player)) {
                    players.add(new PlayerTag(player));
                }
            }

            return new ListTag(players);
        });

        // <--[tag]
        // @attribute <WorldTag.spawned_npcs>
        // @returns ListTag(NPCTag)
        // @description
        // Returns a list of spawned NPCs in this world.
        // -->
        registerTag("spawned_npcs", (attribute, object) -> {
            ArrayList<NPCTag> npcs = new ArrayList<>();

            World thisWorld = object.getWorld();

            for (NPC npc : CitizensAPI.getNPCRegistry()) {
                if (npc.isSpawned() && npc.getEntity().getLocation().getWorld().equals(thisWorld)) {
                    npcs.add(new NPCTag(npc));
                }
            }

            return new ListTag(npcs);
        });

        // <--[tag]
        // @attribute <WorldTag.npcs>
        // @returns ListTag(NPCTag)
        // @description
        // Returns a list of all NPCs in this world.
        // -->
        registerTag("npcs", (attribute, object) -> {
            ArrayList<NPCTag> npcs = new ArrayList<>();

            World thisWorld = object.getWorld();

            for (NPC npc : CitizensAPI.getNPCRegistry()) {
                Location location = npc.getStoredLocation();
                if (location != null) {
                    World world = location.getWorld();
                    if (world != null && world.equals(thisWorld)) {
                        npcs.add(new NPCTag(npc));
                    }
                }
            }

            return new ListTag(npcs);
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
        registerTag("can_generate_structures", (attribute, object) -> {
            return new ElementTag(object.getWorld().canGenerateStructures());
        });

        // <--[tag]
        // @attribute <WorldTag.loaded_chunks>
        // @returns ListTag(ChunkTag)
        // @description
        // Returns a list of all the currently loaded chunks.
        // -->
        registerTag("loaded_chunks", (attribute, object) -> {
            ListTag chunks = new ListTag();
            for (Chunk ent : object.getWorld().getLoadedChunks()) {
                chunks.addObject(new ChunkTag(ent));
            }

            return chunks;
        });

        registerTag("random_loaded_chunk", (attribute, object) -> {
            Deprecations.worldRandomLoadedChunkTag.warn(attribute.context);
            int random = CoreUtilities.getRandom().nextInt(object.getWorld().getLoadedChunks().length);
            return new ChunkTag(object.getWorld().getLoadedChunks()[random]);
        });

        // <--[tag]
        // @attribute <WorldTag.sea_level>
        // @returns ElementTag(Number)
        // @description
        // Returns the level of the sea.
        // -->
        registerTag("sea_level", (attribute, object) -> {
            return new ElementTag(object.getWorld().getSeaLevel());
        });

        // <--[tag]
        // @attribute <WorldTag.spawn_location>
        // @returns LocationTag
        // @mechanism WorldTag.spawn_location
        // @description
        // Returns the spawn location of the world.
        // -->
        registerTag("spawn_location", (attribute, object) -> {
            return new LocationTag(object.getWorld().getSpawnLocation());
        });

        // <--[tag]
        // @attribute <WorldTag.world_type>
        // @returns ElementTag
        // @description
        // Returns the world type of the world.
        // Can return any enum from: <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/WorldType.html>
        // -->
        registerTag("world_type", (attribute, object) -> {
            return new ElementTag(object.getWorld().getWorldType().getName());
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
        registerTag("name", (attribute, object) -> {
            return new ElementTag(object.getWorld().getName());
        });

        // <--[tag]
        // @attribute <WorldTag.seed>
        // @returns ElementTag
        // @description
        // Returns the world seed.
        // -->
        registerTag("seed", (attribute, object) -> {
            return new ElementTag(object.getWorld().getSeed());
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
        registerTag("allows_animals", (attribute, object) -> {
            return new ElementTag(object.getWorld().getAllowAnimals());
        });

        // <--[tag]
        // @attribute <WorldTag.allows_monsters>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether monsters can spawn in this world.
        // -->
        registerTag("allows_monsters", (attribute, object) -> {
            return new ElementTag(object.getWorld().getAllowMonsters());
        });

        // <--[tag]
        // @attribute <WorldTag.allows_pvp>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether player versus player combat is allowed in this world.
        // -->
        registerTag("allows_pvp", (attribute, object) -> {
            return new ElementTag(object.getWorld().getPVP());
        });

        // <--[tag]
        // @attribute <WorldTag.auto_save>
        // @returns ElementTag(Boolean)
        // @mechanism WorldTag.auto_save
        // @description
        // Returns whether the world automatically saves.
        // -->
        registerTag("auto_save", (attribute, object) -> {
            return new ElementTag(object.getWorld().isAutoSave());
        });

        // <--[tag]
        // @attribute <WorldTag.ambient_spawn_limit>
        // @returns ElementTag(Number)
        // @mechanism WorldTag.ambient_spawn_limit
        // @description
        // Returns the number of ambient mobs that can spawn in a chunk in this world.
        // -->
        registerTag("ambient_spawn_limit", (attribute, object) -> {
            return new ElementTag(object.getWorld().getAmbientSpawnLimit());
        });

        // <--[tag]
        // @attribute <WorldTag.animal_spawn_limit>
        // @returns ElementTag(Number)
        // @mechanism WorldTag.animal_spawn_limit
        // @description
        // Returns the number of animals that can spawn in a chunk in this world.
        // -->
        registerTag("animal_spawn_limit", (attribute, object) -> {
            return new ElementTag(object.getWorld().getAnimalSpawnLimit());
        });

        // <--[tag]
        // @attribute <WorldTag.monster_spawn_limit>
        // @returns ElementTag(Number)
        // @mechanism WorldTag.monster_spawn_limit
        // @description
        // Returns the number of monsters that can spawn in a chunk in this world.
        // -->
        registerTag("monster_spawn_limit", (attribute, object) -> {
            return new ElementTag(object.getWorld().getMonsterSpawnLimit());
        });

        // <--[tag]
        // @attribute <WorldTag.water_animal_spawn_limit>
        // @returns ElementTag(Number)
        // @mechanism WorldTag.water_animal_spawn_limit
        // @description
        // Returns the number of water animals that can spawn in a chunk in this world.
        // -->
        registerTag("water_animal_spawn_limit", (attribute, object) -> {
            return new ElementTag(object.getWorld().getWaterAnimalSpawnLimit());
        });

        // <--[tag]
        // @attribute <WorldTag.difficulty>
        // @returns ElementTag
        // @mechanism WorldTag.difficulty
        // @description
        // Returns the name of the difficulty level.
        // -->
        registerTag("difficulty", (attribute, object) -> {
            return new ElementTag(object.getWorld().getDifficulty().name());
        });

        // <--[tag]
        // @attribute <WorldTag.keep_spawn>
        // @returns ElementTag(Boolean)
        // @mechanism WorldTag.keep_spawn
        // @description
        // Returns whether the world's spawn area should be kept loaded into memory.
        // -->
        registerTag("keep_spawn", (attribute, object) -> {
            return new ElementTag(object.getWorld().getKeepSpawnInMemory());
        });

        // <--[tag]
        // @attribute <WorldTag.max_height>
        // @returns ElementTag(Number)
        // @description
        // Returns the maximum height of this world.
        // -->
        registerTag("max_height", (attribute, object) -> {
            return new ElementTag(object.getWorld().getMaxHeight());
        });

        // <--[tag]
        // @attribute <WorldTag.ticks_per_animal_spawn>
        // @returns DurationTag
        // @Mechanism WorldTag.ticks_per_animal_spawns
        // @description
        // Returns the world's ticks per animal spawn value.
        // -->
        registerTag("ticks_per_animal_spawn", (attribute, object) -> {
            return new DurationTag(object.getWorld().getTicksPerAnimalSpawns());
        });

        // <--[tag]
        // @attribute <WorldTag.ticks_per_monster_spawn>
        // @returns DurationTag
        // @Mechanism WorldTag.ticks_per_monster_spawns
        // @description
        // Returns the world's ticks per monster spawn value.
        // -->
        registerTag("ticks_per_monster_spawn", (attribute, object) -> {
            return new DurationTag(object.getWorld().getTicksPerMonsterSpawns());
        });

        /////////////////////
        //   TIME ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <WorldTag.time>
        // @returns ElementTag(Number)
        // @mechanism WorldTag.time
        // @description
        // Returns the relative in-game time of this world.
        // -->
        registerTag("time", (attribute, object) -> {
            // <--[tag]
            // @attribute <WorldTag.time.duration>
            // @returns DurationTag
            // @description
            // Returns the relative in-game time of this world as a duration.
            // -->
            if (attribute.startsWith("duration", 2)) {
                attribute.fulfill(1);
                return new DurationTag(object.getWorld().getTime());
            }

            // <--[tag]
            // @attribute <WorldTag.time.full>
            // @returns DurationTag
            // @description
            // Returns the in-game time of this world.
            // -->
            else if (attribute.startsWith("full", 2)) {
                attribute.fulfill(1);
                return new DurationTag(object.getWorld().getFullTime());
            }

            // <--[tag]
            // @attribute <WorldTag.time.period>
            // @returns ElementTag
            // @description
            // Returns the time as 'day', 'night', 'dawn', or 'dusk'.
            // -->
            else if (attribute.startsWith("period", 2)) {
                attribute.fulfill(1);

                long time = object.getWorld().getTime();
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

                return new ElementTag(period);
            }
            else {
                return new ElementTag(object.getWorld().getTime());
            }
        });

        // <--[tag]
        // @attribute <WorldTag.moon_phase>
        // @returns ElementTag(Number)
        // @description
        // Returns the current phase of the moon, as an integer from 1 to 8.
        // -->
        registerTag("moon_phase", (attribute, object) -> {
            return new ElementTag((int) ((object.getWorld().getFullTime() / 24000) % 8) + 1);
        }, "moonphase");

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
        registerTag("has_storm", (attribute, object) -> {
            return new ElementTag(object.getWorld().hasStorm());
        });

        // <--[tag]
        // @attribute <WorldTag.thunder_duration>
        // @returns DurationTag
        // @mechanism WorldTag.thunder_duration
        // @description
        // Returns the duration of thunder.
        // -->
        registerTag("thunder_duration", (attribute, object) -> {
            return new DurationTag((long) object.getWorld().getThunderDuration());
        });

        // <--[tag]
        // @attribute <WorldTag.thundering>
        // @returns ElementTag(Boolean)
        // @mechanism WorldTag.thundering
        // @description
        // Returns whether it is currently thundering in this world.
        // -->
        registerTag("thundering", (attribute, object) -> {
            return new ElementTag(object.getWorld().isThundering());
        });

        // <--[tag]
        // @attribute <WorldTag.weather_duration>
        // @returns DurationTag
        // @mechanism WorldTag.weather_duration
        // @description
        // Returns the duration of storms.
        // -->
        registerTag("weather_duration", (attribute, object) -> {
            return new DurationTag((long) object.getWorld().getWeatherDuration());
        });

        // <--[tag]
        // @attribute <WorldTag.environment>
        // @returns ElementTag
        // @description
        // Returns the environment of the world: NORMAL, NETHER, or THE_END.
        // -->
        registerTag("environment", (attribute, object) -> {
            return new ElementTag(object.getWorld().getEnvironment().name());
        });

        /////////////////////
        //   WORLD BORDER ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <WorldTag.border_size>
        // @returns ElementTag(Decimal)
        // @description
        // returns the size of the world border in this world.
        // -->
        registerTag("border_size", (attribute, object) -> {
            return new ElementTag(object.getWorld().getWorldBorder().getSize());
        });

        // <--[tag]
        // @attribute <WorldTag.border_center>
        // @returns LocationTag
        // @description
        // returns the center of the world border in this world.
        // -->
        registerTag("border_center", (attribute, object) -> {
            return new LocationTag(object.getWorld().getWorldBorder().getCenter());
        });

        // <--[tag]
        // @attribute <WorldTag.border_damage>
        // @returns ElementTag(Decimal)
        // @description
        // returns the amount of damage caused by crossing the world border in this world.
        // -->
        registerTag("border_damage", (attribute, object) -> {
            return new ElementTag(object.getWorld().getWorldBorder().getDamageAmount());
        });

        // <--[tag]
        // @attribute <WorldTag.border_damage_buffer>
        // @returns ElementTag(Decimal)
        // @description
        // returns the damage buffer of the world border in this world.
        // -->
        registerTag("border_damage_buffer", (attribute, object) -> {
            return new ElementTag(object.getWorld().getWorldBorder().getDamageBuffer());
        });

        // <--[tag]
        // @attribute <WorldTag.border_warning_distance>
        // @returns ElementTag(Number)
        // @description
        // returns the warning distance of the world border in this world.
        // -->
        registerTag("border_warning_distance", (attribute, object) -> {
            return new ElementTag(object.getWorld().getWorldBorder().getWarningDistance());
        });

        // <--[tag]
        // @attribute <WorldTag.border_warning_time>
        // @returns DurationTag
        // @description
        // returns warning time of the world border in this world as a duration.
        // -->
        registerTag("border_warning_time", (attribute, object) -> {
            return new DurationTag(object.getWorld().getWorldBorder().getWarningTime());
        });

        // <--[tag]
        // @attribute <WorldTag.gamerule[<gamerule>]>
        // @returns ElementTag
        // @description
        // returns the current value of the specified gamerule in the world.
        // Note that the name is case-sensitive... so "doFireTick" is correct, but "dofiretick" is not.
        // -->
        registerTag("gamerule", (attribute, object) -> {
            GameRule rule = GameRule.getByName(attribute.getContext(1));
            Object result = object.getWorld().getGameRuleValue(rule);
            return new ElementTag(result == null ? "null" : result.toString());
        });
    }

    public static ObjectTagProcessor<WorldTag> tagProcessor = new ObjectTagProcessor<>();

    public static void registerTag(String name, TagRunnable.ObjectInterface<WorldTag> runnable, String... variants) {
        tagProcessor.registerTag(name, runnable, variants);
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {
        return tagProcessor.getObjectAttribute(this, attribute);
    }

    public void applyProperty(Mechanism mechanism) {
        Debug.echoError("Cannot apply properties to a world!");
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object WorldTag
        // @name ambient_spawn_limit
        // @input ElementTag(Number)
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
        // @input ElementTag(Number)
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
        // @input ElementTag(Boolean)
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
        // @input ElementTag
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
        // -->
        if (mechanism.matches("force_unload")) {
            Bukkit.getServer().unloadWorld(getWorld(), false);
            return;
        }

        // <--[mechanism]
        // @object WorldTag
        // @name full_time
        // @input ElementTag(Number)
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
        // @input ElementTag(Boolean)
        // @description
        // Sets whether the world's spawn area should be kept loaded into memory.
        // @tags
        // <WorldTag.keep_spawn>
        // -->
        if (mechanism.matches("keep_spawn") && mechanism.requireBoolean()) {
            getWorld().setKeepSpawnInMemory(mechanism.getValue().asBoolean());
        }

        // <--[mechanism]
        // @object WorldTag
        // @name monster_spawn_limit
        // @input ElementTag(Number)
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
        // @input ElementTag(Boolean)
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
        // @input ElementTag(Boolean)
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
        // @input DurationTag
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
        // @input ElementTag(Boolean)
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
        // @input DurationTag
        // @description
        // Sets the time between animal spawns.
        // @tags
        // <WorldTag.ticks_per_animal_spawn>
        // -->
        if (mechanism.matches("ticks_per_animal_spawns") && mechanism.requireObject(DurationTag.class)) {
            getWorld().setTicksPerAnimalSpawns(mechanism.valueAsType(DurationTag.class).getTicksAsInt());
        }

        // <--[mechanism]
        // @object WorldTag
        // @name ticks_per_monster_spawns
        // @input DurationTag
        // @description
        // Sets the time between monster spawns.
        // @tags
        // <WorldTag.ticks_per_monster_spawn>
        // -->
        if (mechanism.matches("ticks_per_monster_spawns") && mechanism.requireObject(DurationTag.class)) {
            getWorld().setTicksPerMonsterSpawns(mechanism.valueAsType(DurationTag.class).getTicksAsInt());
        }

        // <--[mechanism]
        // @object WorldTag
        // @name time
        // @input ElementTag(Number)
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
        // -->
        if (mechanism.matches("unload")) {
            Bukkit.getServer().unloadWorld(getWorld(), true);
            return;
        }

        // <--[mechanism]
        // @object WorldTag
        // @name water_animal_spawn_limit
        // @input ElementTag(Number)
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
        // @input DurationTag
        // @description
        // Set the remaining time of the current conditions.
        // @tags
        // <WorldTag.weather_duration>
        // -->
        if (mechanism.matches("weather_duration") && mechanism.requireObject(DurationTag.class)) {
            getWorld().setWeatherDuration(mechanism.valueAsType(DurationTag.class).getTicksAsInt());
        }

        CoreUtilities.autoPropertyMechanism(this, mechanism);

    }
}
