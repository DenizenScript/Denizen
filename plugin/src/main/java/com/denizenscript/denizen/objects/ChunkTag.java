package com.denizenscript.denizen.objects;

import com.denizenscript.denizen.utilities.DenizenAPI;
import com.denizenscript.denizen.utilities.blocks.FakeBlock;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.tags.TagRunnable;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class ChunkTag implements ObjectTag, Adjustable {

    // <--[language]
    // @name ChunkTag
    // @group Object System
    // @description
    // A ChunkTag represents a chunk in the world.
    //
    // For format info, see <@link language ch@>
    //
    // -->

    // <--[language]
    // @name ch@
    // @group Object Fetcher System
    // @description
    // ch@ refers to the 'object identifier' of a ChunkTag. The 'ch@' is notation for Denizen's Object
    // Fetcher. The constructor for a ChunkTag is <x>,<z>,<world>.
    // For example, 'ch@5,3,world'.
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
    // For general info, see <@link language ChunkTag>
    //
    // -->

    //////////////////
    //    OBJECT FETCHER
    ////////////////

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
                return new ChunkTag(WorldTag.valueOf(parts[2], context), Integer.valueOf(parts[0]), Integer.valueOf(parts[1]));
            }
            catch (Exception e) {
                if (context == null || context.debug) {
                    Debug.log("Minor: valueOf ChunkTag returning null: " + "ch@" + string);
                }
                return null;
            }

        }
        else {
            if (context == null || context.debug) {
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
        NMSHandler.getChunkHelper().changeChunkServerThread(getWorld());
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
            NMSHandler.getChunkHelper().restoreServerThread(getWorld());
        }
    }

    public Chunk getChunk() {
        if (cachedChunk == null) {
            cachedChunk = world.getWorld().getChunkAt(chunkX, chunkZ);
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
        world = new WorldTag(location.getWorld());
        chunkX = location.getBlockX() >> 4;
        chunkZ = location.getBlockZ() >> 4;
    }

    public LocationTag getCenter() {
        return new LocationTag(getWorld(), getX() * 16 + 8, 128, getZ() * 16 + 8);
    }

    public int getX() {
        return chunkX;
    }

    public int getZ() {
        return chunkZ;
    }

    public World getWorld() {
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
        return "ch@" + getX() + ',' + getZ() + ',' + getWorld().getName();
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
        return world.getWorld().isChunkLoaded(chunkX, chunkZ);
    }

    public boolean isLoadedSafe() {
        try {
            NMSHandler.getChunkHelper().changeChunkServerThread(getWorld());
            return isLoaded();
        }
        finally {
            NMSHandler.getChunkHelper().restoreServerThread(getWorld());
        }
    }

    public static void registerTags() {

        // <--[tag]
        // @attribute <ChunkTag.add[<#>,<#>]>
        // @returns ChunkTag
        // @description
        // Returns the chunk with the specified coordinates added to it.
        // -->
        registerTag("add", new TagRunnable.ObjectForm<ChunkTag>() {
            @Override
            public ObjectTag run(Attribute attribute, ChunkTag object) {
                if (!attribute.hasContext(1)) {
                    Debug.echoError("The tag ChunkTag.add[<#>,<#>] must have a value.");
                    return null;
                }
                List<String> coords = CoreUtilities.split(attribute.getContext(1), ',');
                if (coords.size() < 2) {
                    Debug.echoError("The tag ChunkTag.add[<#>,<#>] requires two values!");
                    return null;
                }
                int x = ArgumentHelper.getIntegerFrom(coords.get(0));
                int z = ArgumentHelper.getIntegerFrom(coords.get(1));
                ChunkTag chunk = object;

                return new ChunkTag(chunk.world, chunk.chunkX + x, chunk.chunkZ + z);

            }
        });

        // <--[tag]
        // @attribute <ChunkTag.sub[<#>,<#>]>
        // @returns ChunkTag
        // @description
        // Returns the chunk with the specified coordinates subtracted from it.
        // -->
        registerTag("sub", new TagRunnable.ObjectForm<ChunkTag>() {
            @Override
            public ObjectTag run(Attribute attribute, ChunkTag object) {
                if (!attribute.hasContext(1)) {
                    Debug.echoError("The tag ChunkTag.add[<#>,<#>] must have a value.");
                    return null;
                }
                List<String> coords = CoreUtilities.split(attribute.getContext(1), ',');
                if (coords.size() < 2) {
                    Debug.echoError("The tag ChunkTag.sub[<#>,<#>] requires two values!");
                    return null;
                }
                int x = ArgumentHelper.getIntegerFrom(coords.get(0));
                int z = ArgumentHelper.getIntegerFrom(coords.get(1));
                ChunkTag chunk = object;

                return new ChunkTag(chunk.world, chunk.chunkX - x, chunk.chunkZ - z);

            }
        });

        // <--[tag]
        // @attribute <ChunkTag.is_loaded>
        // @returns ElementTag(Boolean)
        // @description
        // Returns true if the chunk is currently loaded into memory.
        // -->
        registerTag("is_loaded", new TagRunnable.ObjectForm<ChunkTag>() {
            @Override
            public ObjectTag run(Attribute attribute, ChunkTag object) {
                return new ElementTag(object.isLoadedSafe());
            }
        });

        // <--[tag]
        // @attribute <ChunkTag.x>
        // @returns ElementTag(Number)
        // @description
        // Returns the x coordinate of the chunk.
        // -->
        registerTag("x", new TagRunnable.ObjectForm<ChunkTag>() {
            @Override
            public ObjectTag run(Attribute attribute, ChunkTag object) {
                return new ElementTag(object.chunkX);
            }
        });

        // <--[tag]
        // @attribute <ChunkTag.z>
        // @returns ElementTag(Number)
        // @description
        // Returns the z coordinate of the chunk.
        // -->
        registerTag("z", new TagRunnable.ObjectForm<ChunkTag>() {
            @Override
            public ObjectTag run(Attribute attribute, ChunkTag object) {
                return new ElementTag(object.chunkZ);
            }
        });

        // <--[tag]
        // @attribute <ChunkTag.world>
        // @returns WorldTag
        // @description
        // Returns the world associated with the chunk.
        // -->
        registerTag("world", new TagRunnable.ObjectForm<ChunkTag>() {
            @Override
            public ObjectTag run(Attribute attribute, ChunkTag object) {
                return object.world;
            }
        });

        // <--[tag]
        // @attribute <ChunkTag.cuboid>
        // @returns CuboidTag
        // @description
        // Returns a cuboid of this chunk.
        // -->
        registerTag("cuboid", new TagRunnable.ObjectForm<ChunkTag>() {
            @Override
            public ObjectTag run(Attribute attribute, ChunkTag object) {
                ChunkTag chunk = object;
                return new CuboidTag(new Location(chunk.getWorld(), chunk.getX() * 16, 0, chunk.getZ() * 16),
                        new Location(chunk.getWorld(), chunk.getX() * 16 + 15, 255, chunk.getZ() * 16 + 15));
            }
        });

        // <--[tag]
        // @attribute <ChunkTag.tile_entities>
        // @returns ListTag(LocationTag)
        // @description
        // Returns a list of tile entity locations in the chunk.
        // -->
        registerTag("tile_entities", new TagRunnable.ObjectForm<ChunkTag>() {
            @Override
            public ObjectTag run(Attribute attribute, ChunkTag object) {
                ListTag tiles = new ListTag();
                Chunk chunk = object.getChunkForTag(attribute);
                if (chunk == null) {
                    return null;
                }
                try {
                    NMSHandler.getChunkHelper().changeChunkServerThread(object.getWorld());
                    for (BlockState block : chunk.getTileEntities()) {
                        tiles.addObject(new LocationTag(block.getLocation()));
                    }
                }
                finally {
                    NMSHandler.getChunkHelper().restoreServerThread(object.getWorld());
                }
                return tiles;
            }
        });

        // <--[tag]
        // @attribute <ChunkTag.entities>
        // @returns ListTag(EntityTag)
        // @description
        // Returns a list of entities in the chunk.
        // -->
        registerTag("entities", new TagRunnable.ObjectForm<ChunkTag>() {
            @Override
            public ObjectTag run(Attribute attribute, ChunkTag object) {
                ListTag entities = new ListTag();
                Chunk chunk = object.getChunkForTag(attribute);
                if (chunk == null) {
                    return null;
                }
                try {
                    NMSHandler.getChunkHelper().changeChunkServerThread(object.getWorld());
                    for (Entity ent : chunk.getEntities()) {
                        entities.addObject(new EntityTag(ent).getDenizenObject());
                    }
                }
                finally {
                    NMSHandler.getChunkHelper().restoreServerThread(object.getWorld());
                }
                return entities;
            }
        });

        // <--[tag]
        // @attribute <ChunkTag.living_entities>
        // @returns ListTag(EntityTag)
        // @description
        // Returns a list of living entities in the chunk. This includes Players, mobs, NPCs, etc., but excludes
        // dropped items, experience orbs, etc.
        // -->
        registerTag("living_entities", new TagRunnable.ObjectForm<ChunkTag>() {
            @Override
            public ObjectTag run(Attribute attribute, ChunkTag object) {
                ListTag entities = new ListTag();
                Chunk chunk = object.getChunkForTag(attribute);
                if (chunk == null) {
                    return null;
                }
                try {
                    NMSHandler.getChunkHelper().changeChunkServerThread(object.getWorld());
                    for (Entity ent : chunk.getEntities()) {
                        if (ent instanceof LivingEntity) {
                            entities.addObject(new EntityTag(ent).getDenizenObject());
                        }
                    }
                }
                finally {
                    NMSHandler.getChunkHelper().restoreServerThread(object.getWorld());
                }
                return entities;
            }
        });

        // <--[tag]
        // @attribute <ChunkTag.players>
        // @returns ListTag(PlayerTag)
        // @description
        // Returns a list of players in the chunk.
        // -->
        registerTag("players", new TagRunnable.ObjectForm<ChunkTag>() {
            @Override
            public ObjectTag run(Attribute attribute, ChunkTag object) {
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
            }
        });

        // <--[tag]
        // @attribute <ChunkTag.height_map>
        // @returns ListTag
        // @description
        // Returns a list of the height of each block in the chunk.
        // -->
        registerTag("height_map", new TagRunnable.ObjectForm<ChunkTag>() {
            @Override
            public ObjectTag run(Attribute attribute, ChunkTag object) {
                Chunk chunk = object.getChunkForTag(attribute);
                if (chunk == null) {
                    return null;
                }
                int[] heightMap = NMSHandler.getChunkHelper().getHeightMap(chunk);
                List<String> height_map = new ArrayList<>(heightMap.length);
                for (int i : heightMap) {
                    height_map.add(String.valueOf(i));
                }
                return new ListTag(height_map);
            }
        });

        // <--[tag]
        // @attribute <ChunkTag.average_height>
        // @returns ElementTag(Decimal)
        // @description
        // Returns the average height of the blocks in the chunk.
        // -->
        registerTag("average_height", new TagRunnable.ObjectForm<ChunkTag>() {
            @Override
            public ObjectTag run(Attribute attribute, ChunkTag object) {
                Chunk chunk = object.getChunkForTag(attribute);
                if (chunk == null) {
                    return null;
                }
                int[] heightMap = NMSHandler.getChunkHelper().getHeightMap(chunk);
                int sum = 0;
                for (int i : heightMap) {
                    sum += i;
                }
                return new ElementTag(((double) sum) / heightMap.length);
            }
        });

        // <--[tag]
        // @attribute <ChunkTag.is_flat[#]>
        // @returns ElementTag(Boolean)
        // @description
        // scans the heights of the blocks to check variance between them. If no number is supplied, is_flat will return
        // true if all the blocks are less than 2 blocks apart in height. Specifying a number will modify the number
        // criteria for determining if it is flat.
        // -->
        registerTag("is_flat", new TagRunnable.ObjectForm<ChunkTag>() {
            @Override
            public ObjectTag run(Attribute attribute, ChunkTag object) {
                Chunk chunk = object.getChunkForTag(attribute);
                if (chunk == null) {
                    return null;
                }
                int[] heightMap = NMSHandler.getChunkHelper().getHeightMap(chunk);
                int tolerance = attribute.hasContext(1) ? ArgumentHelper.getIntegerFrom(attribute.getContext(1)) : 2;
                int x = heightMap[0];
                for (int i : heightMap) {
                    if (Math.abs(x - i) > tolerance) {
                        return new ElementTag(false);
                    }
                }

                return new ElementTag(true);
            }
        });

        // <--[tag]
        // @attribute <ChunkTag.surface_blocks>
        // @returns ListTag(LocationTag)
        // @description
        // Returns a list of the highest non-air surface blocks in the chunk.
        // -->
        registerTag("surface_blocks", new TagRunnable.ObjectForm<ChunkTag>() {
            @Override
            public ObjectTag run(Attribute attribute, ChunkTag object) {
                ListTag surface_blocks = new ListTag();
                Chunk chunk = object.getChunkForTag(attribute);
                if (chunk == null) {
                    return null;
                }
                ChunkSnapshot snapshot = chunk.getChunkSnapshot();
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        surface_blocks.add(new LocationTag(chunk.getBlock(x, snapshot.getHighestBlockYAt(x, z) - 1, z).getLocation()).identify());
                    }
                }

                return surface_blocks;
            }
        });

        // <--[tag]
        // @attribute <ChunkTag.spawn_slimes>
        // @returns ListTag(LocationTag)
        // @description
        // Returns whether the chunk is a specially located 'slime spawner' chunk.
        // -->
        registerTag("spawn_slimes", new TagRunnable.ObjectForm<ChunkTag>() {
            @Override
            public ObjectTag run(Attribute attribute, ChunkTag object) {
                Chunk chunk = object.getChunkForTag(attribute);
                if (chunk == null) {
                    return null;
                }
                return new ElementTag(chunk.isSlimeChunk());
            }
        });

        // <--[tag]
        // @attribute <ChunkTag.type>
        // @returns ElementTag
        // @description
        // Always returns 'Chunk' for ChunkTag objects. All objects fetchable by the Object Fetcher will return the
        // type of object that is fulfilling this attribute.
        // -->
        registerTag("type", new TagRunnable.ObjectForm<ChunkTag>() {
            @Override
            public ObjectTag run(Attribute attribute, ChunkTag object) {
                return new ElementTag("Chunk");
            }
        });

    }

    public static ObjectTagProcessor<ChunkTag> tagProcessor = new ObjectTagProcessor<>();

    public static void registerTag(String name, TagRunnable.ObjectForm<ChunkTag> runnable) {
        tagProcessor.registerTag(name, runnable);
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {
        return tagProcessor.getObjectAttribute(this, attribute);
    }

    public void applyProperty(Mechanism mechanism) {
        Debug.echoError("Cannot apply properties to a chunk!");
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object ChunkTag
        // @name unload
        // @input None
        // @description
        // Removes a chunk from memory.
        // @tags
        // <chunk.is_loaded>
        // -->
        if (mechanism.matches("unload")) {
            getChunk().unload(true);
        }

        if (mechanism.matches("unload_safely")) {
            Debug.echoError("Mechanism 'dChunk.unload_safely' is not valid: It is never safe to remove a chunk in use.");
            getChunk().unload(true);
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
            getChunk().unload(false);
        }

        // <--[mechanism]
        // @object ChunkTag
        // @name load
        // @input None
        // @description
        // Loads a chunk into memory.
        // @tags
        // <chunk.is_loaded>
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
        // @tags
        // None
        // -->
        if (mechanism.matches("regenerate")) {
            getWorld().regenerateChunk(getX(), getZ());
        }

        // <--[mechanism]
        // @object ChunkTag
        // @name refresh_chunk
        // @input None
        // @description
        // Refreshes the chunk, sending any changed properties to players.
        // @tags
        // None
        // -->
        if (mechanism.matches("refresh_chunk")) {
            final int chunkX = getX();
            final int chunkZ = getZ();
            getWorld().refreshChunk(chunkX, chunkZ);
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (Map<LocationTag, FakeBlock> blocks : FakeBlock.getBlocks().values()) {
                        for (Map.Entry<LocationTag, FakeBlock> locBlock : blocks.entrySet()) {
                            LocationTag location = locBlock.getKey();
                            if (Math.floor(location.getX() / 16) == chunkX
                                    && Math.floor(location.getZ() / 16) == chunkZ) {
                                locBlock.getValue().updateBlock();
                            }
                        }
                    }
                }
            }.runTaskLater(DenizenAPI.getCurrentInstance(), 2);
        }

        // <--[mechanism]
        // @object ChunkTag
        // @name refresh_chunk_sections
        // @input None
        // @description
        // Refreshes all 16x16x16 chunk sections within the chunk.
        // @tags
        // None
        // -->
        if (mechanism.matches("refresh_chunk_sections")) {
            NMSHandler.getChunkHelper().refreshChunkSections(getChunk());
        }

        CoreUtilities.autoPropertyMechanism(this, mechanism);
    }
}
