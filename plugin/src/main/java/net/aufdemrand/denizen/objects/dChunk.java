package net.aufdemrand.denizen.objects;

import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.blocks.FakeBlock;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.objects.*;
import net.aufdemrand.denizencore.tags.Attribute;
import net.aufdemrand.denizencore.tags.TagContext;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class dChunk implements dObject, Adjustable {

    // <--[language]
    // @name dChunk
    // @group Object System
    // @description
    // A dChunk represents a chunk in the world.
    //
    // For format info, see <@link language ch@>
    //
    // -->

    // <--[language]
    // @name ch@
    // @group Object Fetcher System
    // @description
    // ch@ refers to the 'object identifier' of a dChunk. The 'ch@' is notation for Denizen's Object
    // Fetcher. The constructor for a dChunk is <x>,<z>,<world>.
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
    //
    // For example, block at X,Z 32,67 is in the chunk at X,Z 2,4
    // And the block at X,Z -32,-67 is in the chunk at X,Z -2,-5
    //
    // For general info, see <@link language dChunk>
    //
    // -->

    //////////////////
    //    OBJECT FETCHER
    ////////////////

    public static dChunk valueOf(String string) {
        return valueOf(string, null);
    }

    /**
     * Gets a Chunk Object from a string form of x,z,world.
     * This is not to be confused with the 'x,y,z,world' of a
     * location, which is a finer grain of unit in a dWorlds.
     *
     * @param string the string or dScript argument String
     * @return a dChunk, or null if incorrectly formatted
     */
    @Fetchable("ch")
    public static dChunk valueOf(String string, TagContext context) {
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
                return new dChunk(dWorld.valueOf(parts[2], context), Integer.valueOf(parts[0]), Integer.valueOf(parts[1]));
            }
            catch (Exception e) {
                if (context == null || context.debug) {
                    dB.log("Minor: valueOf dChunk returning null: " + "ch@" + string);
                }
                return null;
            }

        }
        else {
            if (context == null || context.debug) {
                dB.log("Minor: valueOf dChunk unable to handle malformed format: " + "ch@" + string);
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

    dWorld world;

    Chunk cachedChunk;

    public Chunk getChunk() {
        if (cachedChunk == null) {
            cachedChunk = world.getWorld().getChunkAt(chunkX, chunkZ);
        }
        return cachedChunk;
    }

    /**
     * dChunk can be constructed with a Chunk
     *
     * @param chunk The chunk to use.
     */
    public dChunk(Chunk chunk) {
        this.cachedChunk = chunk;
        world = new dWorld(chunk.getWorld());
        chunkX = chunk.getX();
        chunkZ = chunk.getZ();
    }

    public dChunk(dWorld world, int x, int z) {
        this.world = world;
        chunkX = x;
        chunkZ = z;
    }

    /**
     * dChunk can be constructed with a Location (or dLocation)
     *
     * @param location The location of the chunk.
     */
    public dChunk(Location location) {
        world = new dWorld(location.getWorld());
        chunkX = location.getBlockX() >> 4;
        chunkZ = location.getBlockZ() >> 4;
    }

    public dLocation getCenter() {
        return new dLocation(getWorld(), getX() * 16 + 8, 128, getZ() * 16 + 8);
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

    public ChunkSnapshot getSnapshot() {
        return getChunk().getChunkSnapshot();
    }

    public int[] getHeightMap() {
        return NMSHandler.getInstance().getChunkHelper().getHeightMap(getChunk());
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
    public dChunk setPrefix(String prefix) {
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

    public static void registerTags() {

        // <--[tag]
        // @attribute <ch@chunk.add[<#>,<#>]>
        // @returns dChunk
        // @description
        // Returns the chunk with the specified coordinates added to it.
        // -->
        registerTag("add", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                if (!attribute.hasContext(1)) {
                    dB.echoError("The tag ch@chunk.add[<#>,<#>] must have a value.");
                    return null;
                }
                List<String> coords = CoreUtilities.split(attribute.getContext(1), ',');
                if (coords.size() < 2) {
                    dB.echoError("The tag ch@chunk.add[<#>,<#>] requires two values!");
                    return null;
                }
                int x = aH.getIntegerFrom(coords.get(0));
                int z = aH.getIntegerFrom(coords.get(1));
                dChunk chunk = (dChunk) object;

                return new dChunk(chunk.world, chunk.chunkX + x, chunk.chunkZ + z)
                        .getAttribute(attribute.fulfill(1));

            }
        });

        // <--[tag]
        // @attribute <ch@chunk.sub[<#>,<#>]>
        // @returns dChunk
        // @description
        // Returns the chunk with the specified coordinates subtracted from it.
        // -->
        registerTag("sub", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                if (!attribute.hasContext(1)) {
                    dB.echoError("The tag ch@chunk.add[<#>,<#>] must have a value.");
                    return null;
                }
                List<String> coords = CoreUtilities.split(attribute.getContext(1), ',');
                if (coords.size() < 2) {
                    dB.echoError("The tag ch@chunk.sub[<#>,<#>] requires two values!");
                    return null;
                }
                int x = aH.getIntegerFrom(coords.get(0));
                int z = aH.getIntegerFrom(coords.get(1));
                dChunk chunk = (dChunk) object;

                return new dChunk(chunk.world, chunk.chunkX - x, chunk.chunkZ - z)
                        .getAttribute(attribute.fulfill(1));

            }
        });

        // <--[tag]
        // @attribute <ch@chunk.is_loaded>
        // @returns Element(Boolean)
        // @description
        // Returns true if the chunk is currently loaded into memory.
        // -->
        registerTag("is_loaded", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dChunk) object).isLoaded())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <ch@chunk.x>
        // @returns Element(Number)
        // @description
        // Returns the x coordinate of the chunk.
        // -->
        registerTag("x", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dChunk) object).chunkX).getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <ch@chunk.z>
        // @returns Element(Number)
        // @description
        // Returns the z coordinate of the chunk.
        // -->
        registerTag("z", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dChunk) object).chunkZ).getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <ch@chunk.world>
        // @returns dWorld
        // @description
        // Returns the world associated with the chunk.
        // -->
        registerTag("world", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return ((dChunk) object).world.getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <ch@chunk.cuboid>
        // @returns dCuboid
        // @description
        // Returns a cuboid of this chunk.
        // -->
        registerTag("cuboid", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                dChunk chunk = (dChunk) object;
                return new dCuboid(new Location(chunk.getWorld(), chunk.getX() * 16, 0, chunk.getZ() * 16),
                        new Location(chunk.getWorld(), chunk.getX() * 16 + 15, 255, chunk.getZ() * 16 + 15))
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <ch@chunk.entities>
        // @returns dList(dEntity)
        // @description
        // Returns a list of entities in the chunk.
        // -->
        registerTag("entities", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                dList entities = new dList();
                if (((dChunk) object).isLoaded()) {
                    for (Entity ent : ((dChunk) object).getChunk().getEntities()) {
                        entities.addObject(new dEntity(ent).getDenizenObject());
                    }
                }

                return entities.getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <ch@chunk.living_entities>
        // @returns dList(dEntity)
        // @description
        // Returns a list of living entities in the chunk. This includes Players, mobs, NPCs, etc., but excludes
        // dropped items, experience orbs, etc.
        // -->
        registerTag("living_entities", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                dList entities = new dList();
                if (((dChunk) object).isLoaded()) {
                    for (Entity ent : ((dChunk) object).getChunk().getEntities()) {
                        if (ent instanceof LivingEntity) {
                            entities.addObject(new dEntity(ent).getDenizenObject());
                        }
                    }
                }

                return entities.getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <ch@chunk.players>
        // @returns dList(dPlayer)
        // @description
        // Returns a list of players in the chunk.
        // -->
        registerTag("players", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                dList entities = new dList();
                if (((dChunk) object).isLoaded()) {
                    for (Entity ent : ((dChunk) object).getChunk().getEntities()) {
                        if (dEntity.isPlayer(ent)) {
                            entities.addObject(new dPlayer((Player) ent));
                        }
                    }
                }

                return entities.getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <ch@chunk.height_map>
        // @returns dList
        // @description
        // Returns a list of the height of each block in the chunk.
        // -->
        registerTag("height_map", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                int[] heightMap = ((dChunk) object).getHeightMap();
                List<String> height_map = new ArrayList<>(heightMap.length);
                for (int i : heightMap) {
                    height_map.add(String.valueOf(i));
                }
                return new dList(height_map).getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <ch@chunk.average_height>
        // @returns Element(Decimal)
        // @description
        // Returns the average height of the blocks in the chunk.
        // -->
        registerTag("average_height", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                int sum = 0;
                int[] heightMap = ((dChunk) object).getHeightMap();
                for (int i : heightMap) {
                    sum += i;
                }
                return new Element(((double) sum) / heightMap.length).getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <ch@chunk.is_flat[#]>
        // @returns Element(Boolean)
        // @description
        // scans the heights of the blocks to check variance between them. If no number is supplied, is_flat will return
        // true if all the blocks are less than 2 blocks apart in height. Specifying a number will modify the number
        // criteria for determining if it is flat.
        // -->
        registerTag("is_flat", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                int tolerance = attribute.hasContext(1) && aH.matchesInteger(attribute.getContext(1)) ?
                        Integer.valueOf(attribute.getContext(1)) : 2;
                int[] heightMap = ((dChunk) object).getHeightMap();
                int x = heightMap[0];
                for (int i : heightMap) {
                    if (Math.abs(x - i) > tolerance) {
                        return new Element(false).getAttribute(attribute.fulfill(1));
                    }
                }

                return new Element(true).getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <ch@chunk.surface_blocks>
        // @returns dList(dLocation)
        // @description
        // Returns a list of the highest non-air surface blocks in the chunk.
        // -->
        registerTag("surface_blocks", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                dList surface_blocks = new dList();
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        surface_blocks.add(new dLocation(((dChunk) object).getChunk().getBlock(x, ((dChunk) object)
                                .getSnapshot().getHighestBlockYAt(x, z) - 1, z).getLocation()).identify());
                    }
                }

                return surface_blocks.getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <ch@chunk.spawn_slimes>
        // @returns dList(dLocation)
        // @description
        // Returns whether the chunk is a specially located 'slime spawner' chunk.
        // -->
        registerTag("spawn_slimes", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                dChunk chunk = (dChunk) object;
                Random random = new Random(chunk.getWorld().getSeed() +
                        chunk.getX() * chunk.getX() * 4987142 +
                        chunk.getX() * 5947611 +
                        chunk.getZ() * chunk.getZ() * 4392871L +
                        chunk.getZ() * 389711 ^ 0x3AD8025F);
                return new Element(random.nextInt(10) == 0).getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <ch@chunk.type>
        // @returns Element
        // @description
        // Always returns 'Chunk' for dChunk objects. All objects fetchable by the Object Fetcher will return the
        // type of object that is fulfilling this attribute.
        // -->
        registerTag("type", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element("Chunk").getAttribute(attribute.fulfill(1));
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
                net.aufdemrand.denizencore.utilities.debugging.dB.echoError(attribute.getScriptEntry() != null ? attribute.getScriptEntry().getResidingQueue() : null,
                        "Using deprecated form of tag '" + tr.name + "': '" + attrLow + "'.");
            }
            return tr.run(attribute, this);
        }

        String returned = CoreUtilities.autoPropertyTag(this, attribute);
        if (returned != null) {
            return returned;
        }

        return new Element(identify()).getAttribute(attribute);
    }

    public void applyProperty(Mechanism mechanism) {
        dB.echoError("Cannot apply properties to a chunk!");
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dChunk
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
            dB.echoError("Mechanism 'dChunk.unload_safely' is not valid: It is never safe to remove a chunk in use.");
            getChunk().unload(true);
        }

        // <--[mechanism]
        // @object dChunk
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
        // @object dChunk
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
        // @object dChunk
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
        // @object dChunk
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
                    for (Map<dLocation, FakeBlock> blocks : FakeBlock.getBlocks().values()) {
                        for (Map.Entry<dLocation, FakeBlock> locBlock : blocks.entrySet()) {
                            dLocation location = locBlock.getKey();
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
        // @object dChunk
        // @name refresh_chunk_sections
        // @input None
        // @description
        // Refreshes all 16x16x16 chunk sections within the chunk.
        // @tags
        // None
        // -->
        if (mechanism.matches("refresh_chunk_sections")) {
            NMSHandler.getInstance().getChunkHelper().refreshChunkSections(getChunk());
        }

        CoreUtilities.autoPropertyMechanism(this, mechanism);
    }
}
