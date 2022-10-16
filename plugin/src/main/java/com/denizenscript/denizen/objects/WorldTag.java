package com.denizenscript.denizen.objects;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.nms.abstracts.BiomeNMS;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.flags.WorldFlagHandler;
import com.denizenscript.denizencore.flags.AbstractFlagTracker;
import com.denizenscript.denizencore.flags.FlaggableObject;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizen.utilities.Settings;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.tags.TagRunnable;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.*;
import org.bukkit.boss.DragonBattle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.world.TimeSkipEvent;
import org.bukkit.util.BoundingBox;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class WorldTag implements ObjectTag, Adjustable, FlaggableObject {

    /////////////////////
    //   STATIC METHODS
    /////////////////

    public static WorldTag mirrorBukkitWorld(World world) {
        if (world == null) {
            return null;
        }
        return new WorldTag(world);
    }

    /////////////////////
    //   OBJECT FETCHER
    /////////////////

    // <--[ObjectType]
    // @name WorldTag
    // @prefix w
    // @base ElementTag
    // @implements FlaggableObject
    // @ExampleTagBase player.location.world
    // @ExampleValues <player.location.world>,space
    // @ExampleForReturns
    // - adjust %VALUE% destroy
    // @ExampleForReturns
    // - adjust %VALUE% full_time:0
    // @format
    // The identity format for worlds is the name of the world it should be associated with.
    // For example, to reference the world named 'world1', use simply 'world1'.
    // World names are case insensitive.
    //
    // @description
    // A WorldTag represents a world on the server.
    //
    // This object type is flaggable.
    // Flags on this object type will be stored in the world folder in a file named 'denizen_flags.dat', like "server/world/denizen_flags.dat".
    //
    // @Matchable
    // WorldTag matchers, sometimes identified as "<world>":
    // "world" plaintext: always matches.
    // World name: matches if the world has the given world name, using advanced matchers.
    // "world_flagged:<flag>": a Flag Matchable for WorldTag flags.
    //
    // -->

    @Deprecated
    public static WorldTag valueOf(String string) {
        return valueOf(string, null);
    }

    @Fetchable("w")
    public static WorldTag valueOf(String string, TagContext context) {
        return valueOf(string, context == null || context.showErrors());
    }

    public static WorldTag valueOf(String string, boolean announce) {
        if (string == null) {
            return null;
        }
        string = string.replace("w@", "");
        World returnable = null;
        for (World world : Bukkit.getWorlds()) {
            if (world.getName().equalsIgnoreCase(string)) {
                returnable = world;
            }
        }
        if (returnable != null) {
            return new WorldTag(returnable);
        }
        else if (announce) {
            Debug.echoError("Invalid World! '" + string + "' could not be found.");
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

    @Override
    public AbstractFlagTracker getFlagTracker() {
        return WorldFlagHandler.worldFlagTrackers.get(getName());
    }

    @Override
    public void reapplyTracker(AbstractFlagTracker tracker) {
        // Nothing to do.
    }

    @Override
    public String getReasonNotFlaggable() {
        return "is the world loaded?";
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
        NMSHandler.chunkHelper.changeChunkServerThread(getWorld());
        try {
            return getWorld().getEntities();
        }
        finally {
            NMSHandler.chunkHelper.restoreServerThread(getWorld());
        }
    }

    public final Collection<Entity> getPossibleEntitiesForBoundary(BoundingBox box) {
        // Bork-prevention: getNearbyEntities loops over chunks, so for large boxes just get the direct entity list, as that's probably better than a loop over unloaded chunks
        if (box.getWidthX() > 512 || box.getWidthZ() > 512) {
            return getWorld().getEntities();
        }
        return getWorld().getNearbyEntities(box);
    }

    public Collection<Entity> getPossibleEntitiesForBoundaryForTag(BoundingBox box) {
        NMSHandler.chunkHelper.changeChunkServerThread(getWorld());
        try {
            return getPossibleEntitiesForBoundary(box);
        }
        finally {
            NMSHandler.chunkHelper.restoreServerThread(getWorld());
        }
    }

    public List<LivingEntity> getLivingEntitiesForTag() {
        NMSHandler.chunkHelper.changeChunkServerThread(getWorld());
        try {
            return getWorld().getLivingEntities();
        }
        finally {
            NMSHandler.chunkHelper.restoreServerThread(getWorld());
        }
    }

    public <T> T getGameRuleOrDefault(GameRule<T> gameRule) {
        World world = getWorld();
        T value = world.getGameRuleValue(gameRule);
        if (value == null) {
            value = world.getGameRuleDefault(gameRule);
            if (value == null) {
                throw new IllegalStateException("World " + world_name + " contains no GameRule " + gameRule.getName());
            }
        }
        return value;
    }

    private String prefix;
    String world_name;

    public WorldTag(World world) {
        this(null, world);
    }

    public WorldTag(String worldName) {
        prefix = "World";
        this.world_name = worldName;
    }

    public WorldTag(String prefix, World world) {
        if (prefix == null) {
            this.prefix = "World";
        }
        else {
            this.prefix = prefix;
        }
        this.world_name = world.getName();
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
    public Object getJavaObject() {
        return getWorld();
    }

    @Override
    public ObjectTag setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public static void registerTags() {

        AbstractFlagTracker.registerFlagHandlers(tagProcessor);

        /////////////////////
        //   ENTITY LIST ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <WorldTag.entities[(<matcher>)]>
        // @returns ListTag(EntityTag)
        // @description
        // Returns a list of entities in this world.
        // Optionally specify an entity type matcher to filter down to.
        // -->
        registerTag(ListTag.class, "entities", (attribute, object) -> {
            ListTag entities = new ListTag();
            String matcher = attribute.hasParam() ? attribute.getParam() : null;
            for (Entity entity : object.getEntitiesForTag()) {
                EntityTag current = new EntityTag(entity);
                if (matcher == null || current.tryAdvancedMatcher(matcher)) {
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
        registerTag(ListTag.class, "living_entities", (attribute, object) -> {
            ListTag entities = new ListTag();
            for (Entity entity : object.getLivingEntitiesForTag()) {
                entities.addObject(new EntityTag(entity).getDenizenObject());
            }
            return entities;
        });

        // <--[tag]
        // @attribute <WorldTag.players>
        // @returns ListTag(PlayerTag)
        // @description
        // Returns a list of online players in this world.
        // -->
        registerTag(ListTag.class, "players", (attribute, object) -> {
            ListTag players = new ListTag();
            for (Player player : object.getWorld().getPlayers()) {
                if (!EntityTag.isNPC(player)) {
                    players.addObject(new PlayerTag(player));
                }
            }
            return players;
        });

        // <--[tag]
        // @attribute <WorldTag.spawned_npcs>
        // @returns ListTag(NPCTag)
        // @description
        // Returns a list of spawned NPCs in this world.
        // -->
        registerTag(ListTag.class, "spawned_npcs", (attribute, object) -> {
            ListTag npcs = new ListTag();
            World thisWorld = object.getWorld();
            for (NPC npc : CitizensAPI.getNPCRegistry()) {
                if (npc.isSpawned() && npc.getStoredLocation().getWorld().equals(thisWorld)) {
                    npcs.addObject(new NPCTag(npc));
                }
            }
            return npcs;
        });

        // <--[tag]
        // @attribute <WorldTag.npcs>
        // @returns ListTag(NPCTag)
        // @description
        // Returns a list of all NPCs in this world.
        // -->
        registerTag(ListTag.class, "npcs", (attribute, object) -> {
            ListTag npcs = new ListTag();
            World thisWorld = object.getWorld();
            for (NPC npc : CitizensAPI.getNPCRegistry()) {
                Location location = npc.getStoredLocation();
                if (location != null) {
                    World world = location.getWorld();
                    if (world != null && world.equals(thisWorld)) {
                        npcs.addObject(new NPCTag(npc));
                    }
                }
            }
            return npcs;
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
        registerTag(ElementTag.class, "can_generate_structures", (attribute, object) -> {
            return new ElementTag(object.getWorld().canGenerateStructures());
        });

        // <--[tag]
        // @attribute <WorldTag.loaded_chunks>
        // @returns ListTag(ChunkTag)
        // @description
        // Returns a list of all the currently loaded chunks.
        // -->
        registerTag(ListTag.class, "loaded_chunks", (attribute, object) -> {
            ListTag chunks = new ListTag();
            for (Chunk ent : object.getWorld().getLoadedChunks()) {
                chunks.addObject(new ChunkTag(ent));
            }

            return chunks;
        });

        registerTag(ChunkTag.class, "random_loaded_chunk", (attribute, object) -> {
            BukkitImplDeprecations.worldRandomLoadedChunkTag.warn(attribute.context);
            int random = CoreUtilities.getRandom().nextInt(object.getWorld().getLoadedChunks().length);
            return new ChunkTag(object.getWorld().getLoadedChunks()[random]);
        });

        // <--[tag]
        // @attribute <WorldTag.sea_level>
        // @returns ElementTag(Number)
        // @description
        // Returns the level of the sea.
        // -->
        registerTag(ElementTag.class, "sea_level", (attribute, object) -> {
            return new ElementTag(object.getWorld().getSeaLevel());
        });

        // <--[tag]
        // @attribute <WorldTag.max_height>
        // @returns ElementTag(Number)
        // @description
        // Returns the maximum block height of the world.
        // -->
        registerTag(ElementTag.class, "max_height", (attribute, object) -> {
            return new ElementTag(object.getWorld().getMaxHeight());
        });

        // <--[tag]
        // @attribute <WorldTag.min_height>
        // @returns ElementTag(Number)
        // @description
        // Returns the minimum block height of the world.
        // -->
        registerTag(ElementTag.class, "min_height", (attribute, object) -> {
            return new ElementTag(object.getWorld().getMinHeight());
        });

        // <--[tag]
        // @attribute <WorldTag.spawn_location>
        // @returns LocationTag
        // @mechanism WorldTag.spawn_location
        // @description
        // Returns the spawn location of the world.
        // -->
        registerTag(LocationTag.class, "spawn_location", (attribute, object) -> {
            return new LocationTag(object.getWorld().getSpawnLocation());
        });

        // <--[tag]
        // @attribute <WorldTag.world_type>
        // @returns ElementTag
        // @description
        // Returns the world type of the world.
        // Can return any enum from: <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/WorldType.html>
        // -->
        registerTag(ElementTag.class, "world_type", (attribute, object) -> {
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
        tagProcessor.registerTag(ElementTag.class, "name", (attribute, object) -> {
            return new ElementTag(object.world_name);
        });

        // <--[tag]
        // @attribute <WorldTag.seed>
        // @returns ElementTag
        // @description
        // Returns the world seed.
        // -->
        registerTag(ElementTag.class, "seed", (attribute, object) -> {
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
        registerTag(ElementTag.class, "allows_animals", (attribute, object) -> {
            return new ElementTag(object.getWorld().getAllowAnimals());
        });

        // <--[tag]
        // @attribute <WorldTag.allows_monsters>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether monsters can spawn in this world.
        // -->
        registerTag(ElementTag.class, "allows_monsters", (attribute, object) -> {
            return new ElementTag(object.getWorld().getAllowMonsters());
        });

        // <--[tag]
        // @attribute <WorldTag.allows_pvp>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether player versus player combat is allowed in this world.
        // -->
        registerTag(ElementTag.class, "allows_pvp", (attribute, object) -> {
            return new ElementTag(object.getWorld().getPVP());
        });

        // <--[tag]
        // @attribute <WorldTag.auto_save>
        // @returns ElementTag(Boolean)
        // @mechanism WorldTag.auto_save
        // @description
        // Returns whether the world automatically saves.
        // -->
        registerTag(ElementTag.class, "auto_save", (attribute, object) -> {
            return new ElementTag(object.getWorld().isAutoSave());
        });

        // <--[tag]
        // @attribute <WorldTag.ambient_spawn_limit>
        // @returns ElementTag(Number)
        // @mechanism WorldTag.ambient_spawn_limit
        // @description
        // Returns the number of ambient mobs that can spawn in a chunk in this world.
        // -->
        registerTag(ElementTag.class, "ambient_spawn_limit", (attribute, object) -> {
            return new ElementTag(object.getWorld().getAmbientSpawnLimit());
        });

        // <--[tag]
        // @attribute <WorldTag.animal_spawn_limit>
        // @returns ElementTag(Number)
        // @mechanism WorldTag.animal_spawn_limit
        // @description
        // Returns the number of animals that can spawn in a chunk in this world.
        // -->
        registerTag(ElementTag.class, "animal_spawn_limit", (attribute, object) -> {
            return new ElementTag(object.getWorld().getAnimalSpawnLimit());
        });

        // <--[tag]
        // @attribute <WorldTag.monster_spawn_limit>
        // @returns ElementTag(Number)
        // @mechanism WorldTag.monster_spawn_limit
        // @description
        // Returns the number of monsters that can spawn in a chunk in this world.
        // -->
        registerTag(ElementTag.class, "monster_spawn_limit", (attribute, object) -> {
            return new ElementTag(object.getWorld().getMonsterSpawnLimit());
        });

        // <--[tag]
        // @attribute <WorldTag.water_animal_spawn_limit>
        // @returns ElementTag(Number)
        // @mechanism WorldTag.water_animal_spawn_limit
        // @description
        // Returns the number of water animals that can spawn in a chunk in this world.
        // -->
        registerTag(ElementTag.class, "water_animal_spawn_limit", (attribute, object) -> {
            return new ElementTag(object.getWorld().getWaterAnimalSpawnLimit());
        });

        // <--[tag]
        // @attribute <WorldTag.difficulty>
        // @returns ElementTag
        // @mechanism WorldTag.difficulty
        // @description
        // Returns the name of the difficulty level.
        // -->
        registerTag(ElementTag.class, "difficulty", (attribute, object) -> {
            return new ElementTag(object.getWorld().getDifficulty());
        });

        // <--[tag]
        // @attribute <WorldTag.hardcore>
        // @returns ElementTag(Boolean)
        // @mechanism WorldTag.hardcore
        // @description
        // Returns whether the world is in hardcore mode.
        // -->
        registerTag(ElementTag.class, "hardcore", (attribute, object) -> {
            return new ElementTag(object.getWorld().isHardcore());
        });

        // <--[tag]
        // @attribute <WorldTag.keep_spawn>
        // @returns ElementTag(Boolean)
        // @mechanism WorldTag.keep_spawn
        // @description
        // Returns whether the world's spawn area should be kept loaded into memory.
        // -->
        registerTag(ElementTag.class, "keep_spawn", (attribute, object) -> {
            return new ElementTag(object.getWorld().getKeepSpawnInMemory());
        });

        // <--[tag]
        // @attribute <WorldTag.ticks_per_animal_spawn>
        // @returns DurationTag
        // @mechanism WorldTag.ticks_per_animal_spawns
        // @description
        // Returns the world's ticks per animal spawn value.
        // -->
        registerTag(DurationTag.class, "ticks_per_animal_spawn", (attribute, object) -> {
            return new DurationTag(object.getWorld().getTicksPerAnimalSpawns());
        });

        // <--[tag]
        // @attribute <WorldTag.ticks_per_monster_spawn>
        // @returns DurationTag
        // @mechanism WorldTag.ticks_per_monster_spawns
        // @description
        // Returns the world's ticks per monster spawn value.
        // -->
        registerTag(DurationTag.class, "ticks_per_monster_spawn", (attribute, object) -> {
            return new DurationTag(object.getWorld().getTicksPerMonsterSpawns());
        });

        // <--[tag]
        // @attribute <WorldTag.duration_since_created>
        // @returns DurationTag
        // @description
        // Returns the total duration of time since this world was first created.
        // -->
        registerTag(DurationTag.class, "duration_since_created", (attribute, object) -> {
            return new DurationTag(object.getWorld().getGameTime());
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
        registerTag(ObjectTag.class, "time", (attribute, object) -> {
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
        // Returns the current phase of the moon, as a number from 1 to 8.
        // -->
        registerTag(ElementTag.class, "moon_phase", (attribute, object) -> {
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
        registerTag(ElementTag.class, "has_storm", (attribute, object) -> {
            return new ElementTag(object.getWorld().hasStorm());
        });

        // <--[tag]
        // @attribute <WorldTag.thunder_duration>
        // @returns DurationTag
        // @mechanism WorldTag.thunder_duration
        // @description
        // Returns the duration of thunder.
        // -->
        registerTag(DurationTag.class, "thunder_duration", (attribute, object) -> {
            return new DurationTag((long) object.getWorld().getThunderDuration());
        });

        // <--[tag]
        // @attribute <WorldTag.thundering>
        // @returns ElementTag(Boolean)
        // @mechanism WorldTag.thundering
        // @description
        // Returns whether it is currently thundering in this world.
        // -->
        registerTag(ElementTag.class, "thundering", (attribute, object) -> {
            return new ElementTag(object.getWorld().isThundering());
        });

        // <--[tag]
        // @attribute <WorldTag.weather_duration>
        // @returns DurationTag
        // @mechanism WorldTag.weather_duration
        // @description
        // Returns the duration of storms.
        // -->
        registerTag(DurationTag.class, "weather_duration", (attribute, object) -> {
            return new DurationTag((long) object.getWorld().getWeatherDuration());
        });

        // <--[tag]
        // @attribute <WorldTag.environment>
        // @returns ElementTag
        // @description
        // Returns the environment of the world: NORMAL, NETHER, or THE_END.
        // -->
        registerTag(ElementTag.class, "environment", (attribute, object) -> {
            return new ElementTag(object.getWorld().getEnvironment());
        });

        /////////////////////
        //   WORLD BORDER ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <WorldTag.border_size>
        // @returns ElementTag(Decimal)
        // @description
        // Returns the size of the world border in this world.
        // -->
        registerTag(ElementTag.class, "border_size", (attribute, object) -> {
            return new ElementTag(object.getWorld().getWorldBorder().getSize());
        });

        // <--[tag]
        // @attribute <WorldTag.border_center>
        // @returns LocationTag
        // @description
        // Returns the center of the world border in this world.
        // -->
        registerTag(LocationTag.class, "border_center", (attribute, object) -> {
            return new LocationTag(object.getWorld().getWorldBorder().getCenter());
        });

        // <--[tag]
        // @attribute <WorldTag.border_damage>
        // @returns ElementTag(Decimal)
        // @description
        // Returns the amount of damage caused by crossing the world border in this world.
        // -->
        registerTag(ElementTag.class, "border_damage", (attribute, object) -> {
            return new ElementTag(object.getWorld().getWorldBorder().getDamageAmount());
        });

        // <--[tag]
        // @attribute <WorldTag.border_damage_buffer>
        // @returns ElementTag(Decimal)
        // @description
        // Returns the damage buffer of the world border in this world.
        // -->
        registerTag(ElementTag.class, "border_damage_buffer", (attribute, object) -> {
            return new ElementTag(object.getWorld().getWorldBorder().getDamageBuffer());
        });

        // <--[tag]
        // @attribute <WorldTag.border_warning_distance>
        // @returns ElementTag(Number)
        // @description
        // Returns the warning distance of the world border in this world.
        // -->
        registerTag(ElementTag.class, "border_warning_distance", (attribute, object) -> {
            return new ElementTag(object.getWorld().getWorldBorder().getWarningDistance());
        });

        // <--[tag]
        // @attribute <WorldTag.border_warning_time>
        // @returns DurationTag
        // @description
        // Returns warning time of the world border in this world as a duration.
        // -->
        registerTag(DurationTag.class, "border_warning_time", (attribute, object) -> {
            return new DurationTag(object.getWorld().getWorldBorder().getWarningTime());
        });

        // <--[tag]
        // @attribute <WorldTag.gamerule[<gamerule>]>
        // @returns ElementTag
        // @description
        // Returns the current value of the specified gamerule in the world.
        // Note that the name is case-sensitive... so "doFireTick" is correct, but "dofiretick" is not.
        // -->
        registerTag(ElementTag.class, "gamerule", (attribute, object) -> {
            if (!attribute.hasParam()) {
                attribute.echoError("The tag 'worldtag.gamerule[...]' must have an input value.");
                return null;
            }
            GameRule rule = GameRule.getByName(attribute.getParam());
            Object result = object.getWorld().getGameRuleValue(rule);
            return new ElementTag(result == null ? "null" : result.toString());
        });

        // <--[tag]
        // @attribute <WorldTag.gamerule_map>
        // @returns MapTag
        // @description
        // Returns a map of all the current values of all gamerules in the world.
        // -->
        registerTag(MapTag.class, "gamerule_map", (attribute, object) -> {
            MapTag map = new MapTag();
            for (GameRule rule : GameRule.values()) {
                Object result = object.getWorld().getGameRuleValue(rule);
                if (result != null) {
                    map.putObject(rule.getName(), new ElementTag(result.toString()));
                }
            }
            return map;
        });

        // <--[tag]
        // @attribute <WorldTag.dragon_portal_location>
        // @returns LocationTag
        // @description
        // Returns the location of the ender dragon exit portal, if any (only for end worlds).
        // -->
        registerTag(LocationTag.class, "dragon_portal_location", (attribute, object) -> {
            DragonBattle battle = object.getWorld().getEnderDragonBattle();
            if (battle == null) {
                return null;
            }
            if (battle.getEndPortalLocation() == null) {
                return null;
            }
            return new LocationTag(battle.getEndPortalLocation());
        });

        // <--[tag]
        // @attribute <WorldTag.ender_dragon>
        // @returns EntityTag
        // @description
        // Returns the ender dragon entity currently fighting in this world, if any (only for end worlds).
        // -->
        registerTag(EntityTag.class, "ender_dragon", (attribute, object) -> {
            DragonBattle battle = object.getWorld().getEnderDragonBattle();
            if (battle == null) {
                return null;
            }
            if (battle.getEnderDragon() == null) {
                return null;
            }
            return new EntityTag(battle.getEnderDragon());
        });

        // <--[tag]
        // @attribute <WorldTag.gateway_locations>
        // @returns ListTag(LocationTag)
        // @description
        // Returns a list of possible gateway portal locations, if any (only for end worlds).
        // Not all of these will necessarily generate.
        // In current implementation, this is a list of exactly 20 locations in a circle around the world origin (with radius of 96 blocks).
        // -->
        registerTag(ListTag.class, "gateway_locations", (attribute, object) -> {
            DragonBattle battle = object.getWorld().getEnderDragonBattle();
            if (battle == null) {
                return null;
            }
            ListTag list = new ListTag();
            for (int i = 0; i < 20; i++) {
                // This math based on EndDragonFight#spawnNewGateway
                int x = (int) Math.floor(96.0D * Math.cos(2.0D * (-Math.PI + (Math.PI / 20.0) * i)));
                int z = (int) Math.floor(96.0D * Math.sin(2.0D * (-Math.PI + (Math.PI / 20.0) * i)));
                list.addObject(new LocationTag(object.getWorld(), x, 75, z));
            }
            return list;
        });

        // <--[tag]
        // @attribute <WorldTag.biomes>
        // @returns ListTag(BiomeTag)
        // @description
        // Returns a list of all biomes in this world (including custom biomes).
        // -->
        registerTag(ListTag.class, "biomes", (attribute, object) -> {
            ListTag output = new ListTag();
            for (BiomeNMS biome : NMSHandler.instance.getBiomes(object.getWorld())) {
                output.addObject(new BiomeTag(biome));
            }
            return output;
        });

        // <--[tag]
        // @attribute <WorldTag.view_distance>
        // @returns ElementTag(Number)
        // @mechanism WorldTag.view_distance
        // @group properties
        // @description
        // Returns the view distance of this world. Chunks are visible to players inside this radius.
        // See also <@link tag WorldTag.simulation_distance>
        // -->
        registerTag(ElementTag.class, "view_distance", (attribute, world) -> {
            // Note: mechanism is paper-only, in PaperWorldProperties
            return new ElementTag(world.getWorld().getViewDistance());
        });

        // <--[tag]
        // @attribute <WorldTag.simulation_distance>
        // @returns ElementTag(Number)
        // @mechanism WorldTag.simulation_distance
        // @group properties
        // @description
        // Returns the simulation distance of this world. Chunks inside of this radius to players are ticked and processed.
        // See also <@link tag WorldTag.view_distance>
        // -->
        registerTag(ElementTag.class, "simulation_distance", (attribute, world) -> {
            // Note: mechanism is paper-only, in PaperWorldProperties
            return new ElementTag(world.getWorld().getSimulationDistance());
        });

        // <--[tag]
        // @attribute <WorldTag.enough_sleeping[(<#>)]>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether enough players are sleeping to prepare for the night to advance.
        // Typically used before checking <@link tag WorldTag.enough_deep_sleeping>
        // By default, automatically checks the playersSleepingPercentage gamerule,
        // but this can optionally be overridden by specifying a percentage integer.
        // Any integer above 100 will always yield 'false'. Requires at least one player to be sleeping to return 'true'.
        // NOTE: In 1.16, input is ignored and assumed to be 100%.
        // -->
        registerTag(ElementTag.class, "enough_sleeping", (attribute, world) -> {
            int percentage = 100;
            if (attribute.hasParam()) {
                percentage = attribute.getIntParam();
            }
            else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_17)) {
                percentage = world.getGameRuleOrDefault(GameRule.PLAYERS_SLEEPING_PERCENTAGE);
            }
            return new ElementTag(NMSHandler.worldHelper.areEnoughSleeping(world.getWorld(), percentage));
        });

        // <--[tag]
        // @attribute <WorldTag.enough_deep_sleeping[(<#>)]>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether enough players have been in bed long enough for the night to advance (generally 100 ticks).
        // Loops through all online players, so is typically used after checking <@link tag WorldTag.enough_sleeping>
        // By default, automatically checks the playersSleepingPercentage gamerule,
        // but this can optionally be overridden by specifying a percentage integer.
        // Any integer above 100 will always yield 'false'. Requires at least one player to be sleeping to return 'true'.
        // NOTE: In 1.16, input is ignored and assumed to be 100%.
        // -->
        registerTag(ElementTag.class, "enough_deep_sleeping", (attribute, world) -> {
            int percentage = 100;
            if (attribute.hasParam()) {
                percentage = attribute.getIntParam();
            }
            else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_17)) {
                percentage = world.getGameRuleOrDefault(GameRule.PLAYERS_SLEEPING_PERCENTAGE);
            }
            return new ElementTag(NMSHandler.worldHelper.areEnoughDeepSleeping(world.getWorld(), percentage));
        });

        // <--[tag]
        // @attribute <WorldTag.sky_darkness>
        // @returns ElementTag(Number)
        // @description
        // Returns the current darkness level of the sky in this world.
        // This is determined by an equation that factors in rain, thunder, and time of day.
        // When 4 or higher, players are typically allowed to sleep through the night.
        // -->
        registerTag(ElementTag.class, "sky_darkness", (attribute, world) -> {
            return new ElementTag(NMSHandler.worldHelper.getSkyDarken(world.getWorld()));
        });

        // <--[tag]
        // @attribute <WorldTag.is_day>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether it is considered day in this world. Players are not allowed to sleep at this time.
        // Note that in certain worlds, this and <@link tag WorldTag.is_night> can both be 'false'! (The nether, for example!)
        // In typical worlds, this is 'true' if <@link tag WorldTag.sky_darkness> is less than 4.
        // To check the current time without storm interference, see <@link tag WorldTag.time> and related tags.
        // -->
        registerTag(ElementTag.class, "is_day", (attribute, world) -> {
            return new ElementTag(NMSHandler.worldHelper.isDay(world.getWorld()));
        });

        // <--[tag]
        // @attribute <WorldTag.is_night>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether it is considered night in this world. Players are typically allowed to sleep at this time.
        // Note that in certain worlds, this and <@link tag WorldTag.is_day> can both be 'false'! (The nether, for example!)
        // In typical worlds, this is 'true' if <@link tag WorldTag.sky_darkness> is 4 or higher.
        // To check the current time without storm interference, see <@link tag WorldTag.time> and related tags.
        // -->
        registerTag(ElementTag.class, "is_night", (attribute, world) -> {
            return new ElementTag(NMSHandler.worldHelper.isNight(world.getWorld()));
        });
    }

    public static ObjectTagProcessor<WorldTag> tagProcessor = new ObjectTagProcessor<>();

    public static <R extends ObjectTag> void registerTag(Class<R> returnType, String name, TagRunnable.ObjectInterface<WorldTag, R> runnable, String... variants) {
        tagProcessor.registerTag(returnType, name, (attribute, object) -> {
            if (object.getWorld() == null) {
                attribute.echoError("World '" + object.world_name + "' is unloaded, cannot process tag.");
                return null;
            }
            return runnable.run(attribute, object);
        }, variants);
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {
        return tagProcessor.getObjectAttribute(this, attribute);
    }

    public void applyProperty(Mechanism mechanism) {
        mechanism.echoError("Cannot apply properties to a world!");
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
        if (mechanism.matches("difficulty") && mechanism.requireEnum(Difficulty.class)) {
            Difficulty diff = mechanism.getValue().asEnum(Difficulty.class);
            if (diff != null) {
                getWorld().setDifficulty(diff);
            }
        }

        // <--[mechanism]
        // @object WorldTag
        // @name hardcore
        // @input ElementTag(Boolean)
        // @description
        // Sets whether the world is hardcore mode.
        // @tags
        // <WorldTag.hardcore>
        // -->
        if (mechanism.matches("hardcore") && mechanism.requireBoolean()) {
            getWorld().setHardcore(mechanism.getValue().asBoolean());
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
            File folder = getWorld().getWorldFolder();
            unloadWorldClean(mechanism, false);
            if (getWorld() != null) {
                return;
            }
            if (!Settings.allowDelete()) {
                mechanism.echoError("Unable to destroy world due to config setting, refer to 'WorldTag.destroy' meta documentation.");
                return;
            }
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
            unloadWorldClean(mechanism, false);
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
            getWorld().setSpawnLocation(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getYaw());
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
            unloadWorldClean(mechanism, true);
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

        // <--[mechanism]
        // @object WorldTag
        // @name advance_ticks
        // @input ElementTag(Number)
        // @description
        // Advances this world's day the specified number of ticks WITHOUT firing any events.
        // Useful for manually adjusting the daylight cycle without firing an event every tick, for example.
        // -->
        if (mechanism.matches("advance_ticks") && mechanism.requireInteger()) {
            World world = getWorld();
            NMSHandler.worldHelper.setDayTime(world, world.getFullTime() + mechanism.getValue().asInt());
        }

        // <--[mechanism]
        // @object WorldTag
        // @name skip_night
        // @input None
        // @description
        // Skips to the next day as if enough players slept through the night.
        // NOTE: This ignores the doDaylightCycle gamerule!
        // -->
        if (mechanism.matches("skip_night")) {
            // general logic from NMS world tick
            World world = getWorld();
            long worldTime = world.getFullTime();
            long nextDay = worldTime + 24000L;
            TimeSkipEvent event = new TimeSkipEvent(world, TimeSkipEvent.SkipReason.NIGHT_SKIP, nextDay - nextDay % 24000L - worldTime);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                NMSHandler.worldHelper.setDayTime(world, worldTime + event.getSkipAmount());
            }
            if (!event.isCancelled()) {
                NMSHandler.worldHelper.wakeUpAllPlayers(world);
            }
            // minor change: prior to 1.18, hasStorm/isRaining was not checked
            if (getGameRuleOrDefault(GameRule.DO_WEATHER_CYCLE) && world.hasStorm()) {
                NMSHandler.worldHelper.clearWeather(world);
            }
        }

        tagProcessor.processMechanism(this, mechanism);
    }

    public void unloadWorldClean(Mechanism mechanism, boolean doSave) {
        for (Player pl : new ArrayList<>(getWorld().getPlayers())) {
            if (pl.isOnline()) {
                mechanism.echoError("For WorldTag." + mechanism.getName() + " mechanism, Player " + pl.getUniqueId() + "/" + pl.getName() + " is inside world and will be kicked.");
                pl.kickPlayer("World being destroyed.");
            }
        }
        if (!Bukkit.getServer().unloadWorld(getWorld(), doSave)) {
            mechanism.echoError("WorldTag." + mechanism.getName() + " for world " + world_name + " was refused by the System. Are you sure (A) this world is even loaded, (B) all players have been removed, and (C) this is not the default world?");
        }
    }

    @Override
    public boolean advancedMatches(String matcher) {
        String matcherLow = CoreUtilities.toLowerCase(matcher);
        if (matcherLow.equals("world")) {
            return true;
        }
        if (matcherLow.startsWith("world_flagged:")) {
            return BukkitScriptEvent.coreFlaggedCheck(matcher.substring("world_flagged:".length()), getFlagTracker());
        }
        return BukkitScriptEvent.runGenericCheck(matcher, getName());
    }
}
