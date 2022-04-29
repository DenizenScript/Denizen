package com.denizenscript.denizen.objects;

import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.flags.DataPersistenceFlagTracker;
import com.denizenscript.denizen.utilities.flags.LocationFlagSearchHelper;
import com.denizenscript.denizencore.flags.AbstractFlagTracker;
import com.denizenscript.denizencore.flags.FlaggableObject;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class ChunkTag implements ObjectTag, Adjustable, FlaggableObject {

    // <--[ObjectType]
    // @name ChunkTag
    // @prefix ch
    // @base ElementTag
    // @implements FlaggableObject
    // @ExampleTagBase player.location.chunk
    // @ExampleValues <player.location.chunk>
    // @ExampleForReturns
    // - chunkload %VALUE%
    // @ExampleForReturns
    // - adjust %VALUE% set_all_biomes:desert
    // @format
    // The identity format for chunks is <x>,<z>,<world>
    // For example, 'ch@5,3,world'.
    //
    // @description
    // A ChunkTag represents a chunk in the world.
    //
    // Note that the X/Z pair are chunk coordinates, not block coordinates.
    // To convert from block coordinates to chunk coordinates, divide by 16 and round downward.
    // Note that negative chunks are one unit lower than you might expect.
    // To understand why, simply look at chunks on a number line...
    //  x      x      x      x      x
    // -2     -1    b 0 a    1      2
    // The block 'a' at block position '1' is in chunk '0', but the block 'b' at block position '-1' is in chunk '-1'.
    // As otherwise (if 'b' was in chunk '0'), chunk '0' would be double-wide (32 blocks wide instead of the standard 16).
    //
    // For example, block at X,Z 32,67 is in the chunk at X,Z 2,4
    // And the block at X,Z -32,-67 is in the chunk at X,Z -2,-5
    //
    // This object type is flaggable.
    // Flags on this object type will be stored in the chunk file inside the world folder.
    //
    // -->

    //////////////////
    //    OBJECT FETCHER
    ////////////////

    @Deprecated
    public static ChunkTag valueOf(String string) {
        return valueOf(string, null);
    }

    /**
     * Gets a Chunk Object from a string form of x,z,world.
     * This is not to be confused with the 'x,y,z,world' of a
     * location, which is a finer grain of unit in a WorldTags.
     *
     * @param string the string or dScript argument String
     * @return a ChunkTag, or null if incorrectly formatted
     */
    @Fetchable("ch")
    public static ChunkTag valueOf(String string, TagContext context) {
        if (string == null) {
            return null;
        }

        string = CoreUtilities.toLowerCase(string).replace("ch@", "");

        ////////
        // Match location formats

        // Get a location to fetch its chunk, return if null
        String[] parts = string.split(",");
        if (parts.length == 3) {
            try {
                return new ChunkTag(new WorldTag(parts[2]), Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
            }
            catch (Exception e) {
                if (context == null || context.showErrors()) {
                    Debug.log("Minor: valueOf ChunkTag returning null: " + "ch@" + string);
                }
                return null;
            }

        }
        else {
            if (context == null || context.showErrors()) {
                Debug.log("Minor: valueOf ChunkTag unable to handle malformed format: " + "ch@" + string);
            }
        }

        return null;
    }

    public static boolean matches(String string) {
        if (CoreUtilities.toLowerCase(string).startsWith("ch@")) {
            return true;
        }
        else {
            return false;
        }
    }

    int chunkX, chunkZ;

    WorldTag world;

    Chunk cachedChunk;

    public Chunk getChunkForTag(Attribute attribute) {
        NMSHandler.chunkHelper.changeChunkServerThread(getBukkitWorld());
        try {
            if (!isLoaded()) {
                if (!attribute.hasAlternative()) {
                    Debug.echoError("Cannot get chunk at " + chunkX + ", " + chunkZ + ": Chunk is not loaded. Use the 'chunkload' command to ensure the chunk is loaded.");
                }
                return null;
            }
            return getChunk();
        }
        finally {
            NMSHandler.chunkHelper.restoreServerThread(getBukkitWorld());
        }
    }

    public Chunk getChunk() {
        if (cachedChunk == null) {
            cachedChunk = getBukkitWorld().getChunkAt(chunkX, chunkZ);
        }
        return cachedChunk;
    }

    /**
     * ChunkTag can be constructed with a Chunk
     *
     * @param chunk The chunk to use.
     */
    public ChunkTag(Chunk chunk) {
        this.cachedChunk = chunk;
        world = new WorldTag(chunk.getWorld());
        chunkX = chunk.getX();
        chunkZ = chunk.getZ();
    }

    public ChunkTag(WorldTag world, int x, int z) {
        this.world = world;
        chunkX = x;
        chunkZ = z;
    }

    /**
     * ChunkTag can be constructed with a Location (or LocationTag)
     *
     * @param location The location of the chunk.
     */
    public ChunkTag(Location location) {
        world = location instanceof LocationTag ? new WorldTag(((LocationTag) location).getWorldName()) : new WorldTag(location.getWorld());
        chunkX = location.getBlockX() >> 4;
        chunkZ = location.getBlockZ() >> 4;
    }

    public LocationTag getCenter() {
        return new LocationTag(getWorldName(), getX() * 16 + 8, 128, getZ() * 16 + 8, 0, 0);
    }

    public int getX() {
        return chunkX;
    }

    public int getZ() {
        return chunkZ;
    }

    public String getWorldName() {
        return world.getName();
    }

    public World getBukkitWorld() {
        return world.getWorld();
    }

    String prefix = "Chunk";

    @Override
    public String getObjectType() {
        return "Chunk";
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public ChunkTag setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    @Override
    public boolean isUnique() {
        return true;
    }

    @Override
    public String identify() {
        return "ch@" + getX() + ',' + getZ() + ',' + getWorldName();
    }

    @Override
    public String identifySimple() {
        return identify();
    }

    @Override
    public String toString() {
        return identify();
    }

    public boolean isLoaded() {
        if (getBukkitWorld() == null) {
            return false;
        }
        return getBukkitWorld().isChunkLoaded(chunkX, chunkZ);
    }

    public boolean isLoadedSafe() {
        try {
            NMSHandler.chunkHelper.changeChunkServerThread(getBukkitWorld());
            return isLoaded();
        }
        finally {
            NMSHandler.chunkHelper.restoreServerThread(getBukkitWorld());
        }
    }

    @Override
    public AbstractFlagTracker getFlagTracker() {
        return new DataPersistenceFlagTracker(getChunk(), "flag_chunk_");
    }

    @Override
    public AbstractFlagTracker getFlagTrackerForTag() {
        if (!isLoadedSafe()) {
            return null;
        }
        return getFlagTracker();
    }

    @Override
    public void reapplyTracker(AbstractFlagTracker tracker) {
        // Nothing to do.
    }

    @Override
    public String getReasonNotFlaggable() {
        return "is the chunk loaded?";
    }

    public static void registerTags() {

        AbstractFlagTracker.registerFlagHandlers(tagProcessor);

        // <--[tag]
        // @attribute <ChunkTag.add[<#>,<#>]>
        // @returns ChunkTag
        // @description
        // Returns the chunk with the specified coordinates added to it.
        // -->
        tagProcessor.registerTag(ChunkTag.class, "add", (attribute, object) -> {
            if (!attribute.hasParam()) {
                attribute.echoError("The tag ChunkTag.add[<#>,<#>] must have a value.");
                return null;
            }
            List<String> coords = CoreUtilities.split(attribute.getParam(), ',');
            if (coords.size() < 2) {
                attribute.echoError("The tag ChunkTag.add[<#>,<#>] requires two values!");
                return null;
            }
            if (!ArgumentHelper.matchesInteger(coords.get(0)) || !ArgumentHelper.matchesInteger(coords.get(1))) {
                attribute.echoError("Input to ChunkTag.add[x,z] is not a valid integer pair!");
                return null;
            }
            int x = Integer.parseInt(coords.get(0));
            int z = Integer.parseInt(coords.get(1));

            return new ChunkTag(object.world, object.chunkX + x, object.chunkZ + z);

        });

        // <--[tag]
        // @attribute <ChunkTag.sub[<#>,<#>]>
        // @returns ChunkTag
        // @description
        // Returns the chunk with the specified coordinates subtracted from it.
        // -->
        tagProcessor.registerTag(ChunkTag.class, "sub", (attribute, object) -> {
            if (!attribute.hasParam()) {
                attribute.echoError("The tag ChunkTag.add[<#>,<#>] must have a value.");
                return null;
            }
            List<String> coords = CoreUtilities.split(attribute.getParam(), ',');
            if (coords.size() < 2) {
                attribute.echoError("The tag ChunkTag.sub[<#>,<#>] requires two values!");
                return null;
            }
            if (!ArgumentHelper.matchesInteger(coords.get(0)) || !ArgumentHelper.matchesInteger(coords.get(1))) {
                attribute.echoError("Input to ChunkTag.sub[x,z] is not a valid integer pair!");
                return null;
            }
            int x = Integer.parseInt(coords.get(0));
            int z = Integer.parseInt(coords.get(1));

            return new ChunkTag(object.world, object.chunkX - x, object.chunkZ - z);

        });

        // <--[tag]
        // @attribute <ChunkTag.is_generated>
        // @returns ElementTag(Boolean)
        // @description
        // Returns true if the chunk has already been generated.
        // -->
        tagProcessor.registerTag(ElementTag.class, "is_generated", (attribute, object) -> {
            return new ElementTag(object.getBukkitWorld().isChunkGenerated(object.chunkX, object.chunkZ));
        });

        // <--[tag]
        // @attribute <ChunkTag.is_loaded>
        // @returns ElementTag(Boolean)
        // @description
        // Returns true if the chunk is currently loaded into memory.
        // -->
        tagProcessor.registerTag(ElementTag.class, "is_loaded", (attribute, object) -> {
            return new ElementTag(object.isLoadedSafe());
        });

        // <--[tag]
        // @attribute <ChunkTag.force_loaded>
        // @returns ElementTag(Boolean)
        // @mechanism ChunkTag.force_loaded
        // @description
        // Returns whether the chunk is forced to stay loaded at all times.
        // -->
        tagProcessor.registerTag(ElementTag.class, "force_loaded", (attribute, object) -> {
            if (!object.isLoadedSafe()) {
                return new ElementTag(false);
            }
            Chunk chunk = object.getChunkForTag(attribute);
            return new ElementTag(chunk != null && chunk.isForceLoaded());
        });

        // <--[tag]
        // @attribute <ChunkTag.plugin_tickets>
        // @returns ListTag(PluginTag)
        // @mechanism ChunkTag.clear_plugin_tickets
        // @description
        // Returns a list of plugins that are keeping this chunk loaded.
        // This is related to the <@link command chunkload> command.
        // -->
        tagProcessor.registerTag(ListTag.class, "plugin_tickets", (attribute, object) -> {
            if (!object.isLoadedSafe()) {
                return new ListTag();
            }
            Chunk chunk = object.getChunkForTag(attribute);
            ListTag result = new ListTag();
            for (Plugin plugin : chunk.getPluginChunkTickets()) {
                result.addObject(new PluginTag(plugin));
            }
            return result;
        });

        // <--[tag]
        // @attribute <ChunkTag.x>
        // @returns ElementTag(Number)
        // @description
        // Returns the x coordinate of the chunk.
        // -->
        tagProcessor.registerTag(ElementTag.class, "x", (attribute, object) -> {
            return new ElementTag(object.chunkX);
        });

        // <--[tag]
        // @attribute <ChunkTag.z>
        // @returns ElementTag(Number)
        // @description
        // Returns the z coordinate of the chunk.
        // -->
        tagProcessor.registerTag(ElementTag.class, "z", (attribute, object) -> {
            return new ElementTag(object.chunkZ);
        });

        // <--[tag]
        // @attribute <ChunkTag.world>
        // @returns WorldTag
        // @description
        // Returns the world associated with the chunk.
        // -->
        tagProcessor.registerTag(WorldTag.class, "world", (attribute, object) -> {
            return object.world;
        });

        // <--[tag]
        // @attribute <ChunkTag.cuboid>
        // @returns CuboidTag
        // @description
        // Returns a cuboid of this chunk.
        // -->
        tagProcessor.registerTag(CuboidTag.class, "cuboid", (attribute, object) -> {
            int yMin = 0, yMax = 255;
            if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_17)) {
                World world = object.getBukkitWorld();
                if (world != null) {
                    yMin = world.getMinHeight();
                    yMax = world.getMaxHeight();
                }
            }
            return new CuboidTag(new LocationTag(object.getWorldName(), object.getX() * 16, yMin, object.getZ() * 16, 0, 0),
                    new LocationTag(object.getWorldName(), object.getX() * 16 + 15, yMax, object.getZ() * 16 + 15, 0, 0));
        });

        // <--[tag]
        // @attribute <ChunkTag.tile_entities>
        // @returns ListTag(LocationTag)
        // @description
        // Returns a list of tile entity locations in the chunk.
        // -->
        tagProcessor.registerTag(ListTag.class, "tile_entities", (attribute, object) -> {
            ListTag tiles = new ListTag();
            Chunk chunk = object.getChunkForTag(attribute);
            if (chunk == null) {
                return null;
            }
            try {
                NMSHandler.chunkHelper.changeChunkServerThread(object.getBukkitWorld());
                for (BlockState block : chunk.getTileEntities()) {
                    tiles.addObject(new LocationTag(block.getLocation()));
                }
            }
            finally {
                NMSHandler.chunkHelper.restoreServerThread(object.getBukkitWorld());
            }
            return tiles;
        });

        // <--[tag]
        // @attribute <ChunkTag.entities[(<entity>|...)]>
        // @returns ListTag(EntityTag)
        // @description
        // Returns a list of entities in the chunk.
        // Optionally specify entity types to filter down to.
        // -->
        tagProcessor.registerTag(ListTag.class, "entities", (attribute, object) -> {
            ListTag entities = new ListTag();
            Chunk chunk = object.getChunkForTag(attribute);
            if (chunk == null) {
                return null;
            }
            ListTag typeFilter = attribute.hasParam() ? attribute.paramAsType(ListTag.class) : null;
            try {
                NMSHandler.chunkHelper.changeChunkServerThread(object.getBukkitWorld());
                for (Entity entity : chunk.getEntities()) {
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
            }
            finally {
                NMSHandler.chunkHelper.restoreServerThread(object.getBukkitWorld());
            }
            return entities;
        });

        // <--[tag]
        // @attribute <ChunkTag.living_entities>
        // @returns ListTag(EntityTag)
        // @description
        // Returns a list of living entities in the chunk.
        // This includes Players, mobs, NPCs, etc., but excludes dropped items, experience orbs, etc.
        // -->
        tagProcessor.registerTag(ListTag.class, "living_entities", (attribute, object) -> {
            ListTag entities = new ListTag();
            Chunk chunk = object.getChunkForTag(attribute);
            if (chunk == null) {
                return null;
            }
            try {
                NMSHandler.chunkHelper.changeChunkServerThread(object.getBukkitWorld());
                for (Entity ent : chunk.getEntities()) {
                    if (ent instanceof LivingEntity) {
                        entities.addObject(new EntityTag(ent).getDenizenObject());
                    }
                }
            }
            finally {
                NMSHandler.chunkHelper.restoreServerThread(object.getBukkitWorld());
            }
            return entities;
        });

        // <--[tag]
        // @attribute <ChunkTag.players>
        // @returns ListTag(PlayerTag)
        // @description
        // Returns a list of players in the chunk.
        // -->
        tagProcessor.registerTag(ListTag.class, "players", (attribute, object) -> {
            ListTag entities = new ListTag();
            Chunk chunk = object.getChunkForTag(attribute);
            if (chunk == null) {
                return null;
            }
            for (Entity ent : chunk.getEntities()) {
                if (EntityTag.isPlayer(ent)) {
                    entities.addObject(new PlayerTag((Player) ent));
                }
            }
            return entities;
        });

        // <--[tag]
        // @attribute <ChunkTag.height_map>
        // @returns ListTag
        // @description
        // Returns a list of the height of each block in the chunk.
        // -->
        tagProcessor.registerTag(ListTag.class, "height_map", (attribute, object) -> {
            Chunk chunk = object.getChunkForTag(attribute);
            if (chunk == null) {
                return null;
            }
            int[] heightMap = NMSHandler.chunkHelper.getHeightMap(chunk);
            List<String> height_map = new ArrayList<>(heightMap.length);
            for (int i : heightMap) {
                height_map.add(String.valueOf(i));
            }
            return new ListTag(height_map);
        });

        // <--[tag]
        // @attribute <ChunkTag.average_height>
        // @returns ElementTag(Decimal)
        // @description
        // Returns the average height of the blocks in the chunk.
        // -->
        tagProcessor.registerTag(ElementTag.class, "average_height", (attribute, object) -> {
            Chunk chunk = object.getChunkForTag(attribute);
            if (chunk == null) {
                return null;
            }
            int[] heightMap = NMSHandler.chunkHelper.getHeightMap(chunk);
            int sum = 0;
            for (int i : heightMap) {
                sum += i;
            }
            return new ElementTag(((double) sum) / heightMap.length);
        });

        // <--[tag]
        // @attribute <ChunkTag.is_flat[(<#>)]>
        // @returns ElementTag(Boolean)
        // @description
        // Scans the heights of the blocks to check variance between them.
        // If no number is supplied, is_flat will return true if all the blocks are less than 2 blocks apart in height.
        // Specifying a number will modify the number criteria for determining if it is flat.
        // -->
        tagProcessor.registerTag(ElementTag.class, "is_flat", (attribute, object) -> {
            Chunk chunk = object.getChunkForTag(attribute);
            if (chunk == null) {
                return null;
            }
            int[] heightMap = NMSHandler.chunkHelper.getHeightMap(chunk);
            int tolerance = 2;
            if (attribute.hasParam() && ArgumentHelper.matchesInteger(attribute.getParam())) {
                tolerance = attribute.getIntParam();
            }
            int x = heightMap[0];
            for (int i : heightMap) {
                if (Math.abs(x - i) > tolerance) {
                    return new ElementTag(false);
                }
            }

            return new ElementTag(true);
        });

        // <--[tag]
        // @attribute <ChunkTag.surface_blocks>
        // @returns ListTag(LocationTag)
        // @description
        // Returns a list of the highest non-air surface blocks in the chunk.
        // -->
        tagProcessor.registerTag(ListTag.class, "surface_blocks", (attribute, object) -> {
            ListTag surface_blocks = new ListTag();
            Chunk chunk = object.getChunkForTag(attribute);
            if (chunk == null) {
                return null;
            }
            ChunkSnapshot snapshot = chunk.getChunkSnapshot();
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    surface_blocks.addObject(new LocationTag(chunk.getWorld(), chunk.getX() << 4 | x, snapshot.getHighestBlockYAt(x, z) - 1, chunk.getZ() << 4 | z));
                }
            }
            return surface_blocks;
        });

        // <--[tag]
        // @attribute <ChunkTag.blocks_flagged[<flag_name>]>
        // @returns ListTag(LocationTag)
        // @description
        // Gets a list of all block locations with a specified flag within the CuboidTag.
        // Searches the internal flag lists, rather than through all possible blocks.
        // -->
        tagProcessor.registerTag(ListTag.class, "blocks_flagged", (attribute, object) -> {
            if (!attribute.hasParam()) {
                attribute.echoError("ChunkTag.blocks_flagged[...] must have an input value.");
                return null;
            }
            Chunk chunk = object.getChunkForTag(attribute);
            if (chunk == null) {
                return null;
            }
            String flagName = CoreUtilities.toLowerCase(attribute.getParam());
            ListTag blocks = new ListTag();
            LocationFlagSearchHelper.getFlaggedLocations(chunk, flagName, (loc) -> {
                blocks.addObject(new LocationTag(loc));
            });
            return blocks;
        });

        // <--[tag]
        // @attribute <ChunkTag.spawn_slimes>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the chunk is a specially located 'slime spawner' chunk.
        // -->
        tagProcessor.registerTag(ElementTag.class, "spawn_slimes", (attribute, object) -> {
            Chunk chunk = object.getChunkForTag(attribute);
            if (chunk == null) {
                return null;
            }
            return new ElementTag(chunk.isSlimeChunk());
        });

        // <--[tag]
        // @attribute <ChunkTag.inhabited_time>
        // @returns DurationTag
        // @mechanism ChunkTag.inhabited_time
        // @description
        // Returns the total time the chunk has been inhabited for.
        // This is a primary deciding factor in the "local difficulty" setting.
        // -->
        tagProcessor.registerTag(DurationTag.class, "inhabited_time", (attribute, object) -> {
            Chunk chunk = object.getChunkForTag(attribute);
            if (chunk == null) {
                return null;
            }
            return new DurationTag(chunk.getInhabitedTime());
        });
    }

    public static ObjectTagProcessor<ChunkTag> tagProcessor = new ObjectTagProcessor<>();

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {
        return tagProcessor.getObjectAttribute(this, attribute);
    }

    public void applyProperty(Mechanism mechanism) {
        mechanism.echoError("Cannot apply properties to a chunk!");
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object ChunkTag
        // @name inhabited_time
        // @input DurationTag
        // @description
        // Changes the amount of time the chunk has been inhabited for.
        // This is a primary deciding factor in the "local difficulty" setting.
        // @tags
        // <ChunkTag.inhabited_time>
        // -->
        if (mechanism.matches("inhabited_time") && mechanism.requireObject(DurationTag.class)) {
            getChunk().setInhabitedTime(mechanism.valueAsType(DurationTag.class).getTicks());
        }

        // <--[mechanism]
        // @object ChunkTag
        // @name unload
        // @input None
        // @description
        // Removes a chunk from memory.
        // @tags
        // <ChunkTag.is_loaded>
        // -->
        if (mechanism.matches("unload")) {
            getBukkitWorld().unloadChunk(getX(), getZ(), true);
        }

        // <--[mechanism]
        // @object ChunkTag
        // @name unload_without_saving
        // @input None
        // @description
        // Removes a chunk from memory without saving any recent changes.
        // @tags
        // <chunk.is_loaded>
        // -->
        if (mechanism.matches("unload_without_saving")) {
            getBukkitWorld().unloadChunk(getX(), getZ(), false);
        }

        // <--[mechanism]
        // @object ChunkTag
        // @name force_loaded
        // @input ElementTag(Boolean)
        // @description
        // Sets whether this plugin is force-loaded or not.
        // Unless you have a specific reason to use this, prefer <@link command chunkload>.
        // @tags
        // <ChunkTag.force_loaded>
        // -->
        if (mechanism.matches("force_loaded") && mechanism.requireBoolean()) {
            getChunk().setForceLoaded(mechanism.getValue().asBoolean());
        }

        // <--[mechanism]
        // @object ChunkTag
        // @name clear_plugin_tickets
        // @input None
        // @description
        // Forcibly removes all plugin tickets from this chunk, usually allowing it to unload.
        // This is usually a bad idea.
        // @tags
        // <ChunkTag.plugin_tickets>
        // -->
        if (mechanism.matches("clear_plugin_tickets")) {
            for (Plugin plugin : new ArrayList<>(getChunk().getPluginChunkTickets())) {
                getChunk().removePluginChunkTicket(plugin);
            }
        }

        // <--[mechanism]
        // @object ChunkTag
        // @name load
        // @input None
        // @description
        // Loads a chunk into memory.
        // @tags
        // <ChunkTag.is_loaded>
        // -->
        if (mechanism.matches("load")) {
            getChunk().load(true);
        }

        // <--[mechanism]
        // @object ChunkTag
        // @name regenerate
        // @input None
        // @description
        // Causes the chunk to be entirely deleted and reformed from the world's seed.
        // At time of writing this method only works as expected on Paper, and will error on Spigot.
        // -->
        if (mechanism.matches("regenerate")) {
            getBukkitWorld().regenerateChunk(getX(), getZ());
        }

        // <--[mechanism]
        // @object ChunkTag
        // @name refresh_chunk
        // @input None
        // @description
        // Refreshes the chunk, sending any changed properties to players.
        // -->
        if (mechanism.matches("refresh_chunk")) {
            final int chunkX = getX();
            final int chunkZ = getZ();
            getBukkitWorld().refreshChunk(chunkX, chunkZ);
        }

        // <--[mechanism]
        // @object ChunkTag
        // @name refresh_chunk_sections
        // @input None
        // @description
        // Refreshes all 16x16x16 chunk sections within the chunk.
        // -->
        if (mechanism.matches("refresh_chunk_sections")) {
            if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_18)) {
                getBukkitWorld().regenerateChunk(chunkX, chunkZ);
            }
            else {
                NMSHandler.chunkHelper.refreshChunkSections(getChunk());
            }
        }

        // <--[mechanism]
        // @object ChunkTag
        // @name set_all_biomes
        // @input BiomeTag
        // @description
        // Sets all biomes in the chunk to the given biome.
        // -->
        if (mechanism.matches("set_all_biomes") && mechanism.requireObject(BiomeTag.class)) {
            NMSHandler.chunkHelper.setAllBiomes(getChunk(), mechanism.valueAsType(BiomeTag.class).getBiome());
        }

        CoreUtilities.autoPropertyMechanism(this, mechanism);
    }
}
