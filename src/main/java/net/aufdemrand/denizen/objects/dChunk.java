package net.aufdemrand.denizen.objects;

import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_6_R3.CraftChunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.*;

public class dChunk extends CraftChunk implements dObject, Adjustable {

    //////////////////
    //    OBJECT FETCHER
    ////////////////

    /**
     * Gets a Chunk Object from a string form of x,z,world.
     * This is not to be confused with the 'x,y,z,world' of a
     * location, which is a finer grain of unit in a dWorlds.
     *
     * @param string  the string or dScript argument String
     * @return  a dChunk, or null if incorrectly formatted
     *
     */
    @Fetchable("ch")
    public static dChunk valueOf(String string) {
        if (string == null) return null;

        string = string.toLowerCase().replace("ch@", "");

        ////////
        // Match location formats

        // Get a location to fetch its chunk, return if null
        String[] parts = string.split(",");
        if (parts.length == 3) {
            try {
                return new dChunk((CraftChunk) dWorld.valueOf(parts[2]).getWorld()
                        .getChunkAt(Integer.valueOf(parts[0]), Integer.valueOf(parts[1])));
            } catch (Exception e) {
                dB.log("valueOf dChunk returning null: " + "ch@" + string);
                return null;
            }

        } else
            dB.log("valueOf dChunk unable to handle malformed format: " + "ch@" + string);

        return null;
    }


    public static boolean matches(String string) {
        if (string.toLowerCase().startsWith("ch@"))
            return true;

        else return false;
    }


    /**
     * dChunk can be constructed with a CraftChunk
     *
     * @param chunk
     */
    public dChunk(CraftChunk chunk) {
        super(chunk.getHandle());
    }

    /**
     * dChunk can be constructed with a Location (or dLocation)
     *
     * @param location
     */
    public dChunk(Location location) {
        super (((CraftChunk) location.getChunk()).getHandle());
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
    public String debug() {
        return ("<G>" + prefix + "='<Y>" + identify() + "<G>'  ");
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
    public String toString() {
        return identify();
    }

    @Override
    public String getAttribute(Attribute attribute) {
        if (attribute == null) return null;

        // <--[tag]
        // @attribute <ch@chunk.is_loaded>
        // @returns Element(Boolean)
        // @description
        // returns true if the chunk is currently loaded into memory.
        // -->
        if (attribute.startsWith("is_loaded"))
            return new Element(this.isLoaded()).getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <ch@chunk.x>
        // @returns Element(Number)
        // @description
        // returns the x coordinate of the chunk.
        // -->
        if (attribute.startsWith("x"))
            return new Element(this.getX()).getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <ch@chunk.z>
        // @returns Element(Number)
        // @description
        // returns the z coordinate of the chunk.
        // -->
        if (attribute.startsWith("z"))
            return new Element(this.getZ()).getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <ch@chunk.world>
        // @returns dWorld
        // @description
        // returns the world associated with the chunk.
        // -->
        if (attribute.startsWith("world"))
            return dWorld.mirrorBukkitWorld(getWorld()).getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <ch@chunk.entities>
        // @returns dList(dEntity)
        // @description
        // returns a list of entities in the chunk.
        // -->
        if (attribute.startsWith("entities")) {
            dList entities = new dList();
            for (Entity ent : this.getEntities())
                entities.add(new dEntity(ent).identify());

            return entities.getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <ch@chunk.living_entities>
        // @returns dList(dEntity)
        // @description
        // returns a list of living entities in the chunk. This includes Players, mobs, NPCs, etc., but excludes
        // dropped items, experience orbs, etc.
        // -->
        if (attribute.startsWith("living_entities")) {
            dList entities = new dList();
            for (Entity ent : this.getEntities())
                if (ent instanceof LivingEntity)
                    entities.add(new dEntity(ent).identify());

            return entities.getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <ch@chunk.players>
        // @returns dList(dPlayer)
        // @description
        // returns a list of players in the chunk.
        // -->
        if (attribute.startsWith("players")) {
            dList entities = new dList();
            for (Entity ent : this.getEntities())
                if (ent instanceof Player)
                    entities.add(new dPlayer((Player) ent).identify());

            return entities.getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <ch@chunk.height_map>
        // @returns dList(Element)
        // @description
        // returns a list of the height of each block in the chunk.
        // -->
        if (attribute.startsWith("height_map")) {
            List<String> height_map = new ArrayList<String>(this.getHandle().heightMap.length);
            for (int i : this.getHandle().heightMap)
                height_map.add(String.valueOf(i));
            return new dList(height_map).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <ch@chunk.average_height>
        // @returns Element(Decimal)
        // @description
        // returns the average height of the blocks in the chunk.
        // -->
        if (attribute.startsWith("average_height")) {
            int sum = 0;
            for (int i : this.getHandle().heightMap) sum += i;
            return new Element(((double) sum)/getHandle().heightMap.length).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <ch@chunk.is_flat[#]>
        // @returns Element(Boolean)
        // @description
        // scans the heights of the blocks to check variance between them. If no number is supplied, is_flat will return
        // true if all the blocks are less than 2 blocks apart in height. Specifying a number will modify the number
        // criteria for determining if it is flat.
        // -->
        if (attribute.startsWith("is_flat")) {
            int tolerance = attribute.hasContext(1) && aH.matchesInteger(attribute.getContext(1)) ?
                    Integer.valueOf(attribute.getContext(1)) : 2;
            int x = this.getHandle().heightMap[0];
            for (int i : this.getHandle().heightMap)
                if (Math.abs(x - i) > tolerance)
                    return Element.FALSE.getAttribute(attribute.fulfill(1));

            return Element.TRUE.getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <ch@chunk.surface_blocks>
        // @returns dList(dLocation)
        // @description
        // returns a list of the highest non-air surface blocks in the chunk.
        // -->
        if (attribute.startsWith("surface_blocks")) {
            dList surface_blocks = new dList();
            for (int x = 0; x < 16; x++)
                for (int z = 0; z < 16; z++)
                    surface_blocks.add(new dLocation(getBlock(x, getChunkSnapshot().getHighestBlockYAt(x, z) - 1, z).getLocation()).identify());

            return surface_blocks.getAttribute(attribute.fulfill(1));
        }

        return new Element(identify()).getAttribute(attribute);
    }

    @Override
    public void adjust(Mechanism mechanism, Element value) {

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
            unload(true);
            return;
        }

        // <--[mechanism]
        // @object dChunk
        // @name unload_safely
        // @input None
        // @description
        // Removes a chunk from memory in a safe manner.
        // @tags
        // <chunk.is_loaded>
        // -->
        if (mechanism.matches("unload_safely")) {
            unload(true, true);
            return;
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
            unload(false);
            return;
        }

        // <--[mechanism]
        // @object dChunk
        // @name unload
        // @input None
        // @description
        // Loads a chunk into memory.
        // @tags
        // <chunk.is_loaded>
        // -->
        if (mechanism.matches("load")) {
            load(true);
            return;
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
            return;
        }


    }

}
