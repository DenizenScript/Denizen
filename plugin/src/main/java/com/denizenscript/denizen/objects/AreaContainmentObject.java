package com.denizenscript.denizen.objects;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.depends.Depends;
import com.denizenscript.denizen.utilities.flags.LocationFlagSearchHelper;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.function.Predicate;

public interface AreaContainmentObject extends ObjectTag {

    // <--[ObjectType]
    // @name AreaObject
    // @prefix None
    // @base None
    // @format
    // N/A
    //
    // @description
    // "AreaObject" is a pseudo-ObjectType that represents any object that indicates a world-space area, such as a CuboidTag.
    //
    // -->

    String getNoteName();

    boolean doesContainLocation(Location loc);

    CuboidTag getCuboidBoundary();

    WorldTag getWorld();

    ListTag getShell();

    ListTag getBlocks(Predicate<Location> test);

    AreaContainmentObject withWorld(WorldTag world);

    default ListTag getBlocksFlagged(String flagName, Attribute attribute) {
        CuboidTag cuboid = getCuboidBoundary();
        ListTag blocks = new ListTag();
        for (CuboidTag.LocationPair pair : cuboid.pairs) {
            ChunkTag minChunk = new ChunkTag(pair.low);
            ChunkTag maxChunk = new ChunkTag(pair.high);
            ChunkTag subChunk = new ChunkTag(pair.low);
            for (int x = minChunk.getX(); x <= maxChunk.getX(); x++) {
                subChunk.chunkX = x;
                for (int z = minChunk.getZ(); z <= maxChunk.getZ(); z++) {
                    subChunk.chunkZ = z;
                    subChunk.cachedChunk = null;
                    if (subChunk.isLoadedSafe()) {
                        LocationFlagSearchHelper.getFlaggedLocations(subChunk.getChunkForTag(attribute), flagName, (loc) -> {
                            if (doesContainLocation(loc)) {
                                blocks.addObject(new LocationTag(loc));
                            }
                        });
                    }
                }
            }
        }
        return blocks;
    }

    public static void registerTags(ObjectTagProcessor<? extends AreaContainmentObject> processor) {

        // <--[tag]
        // @attribute <AreaObject.bounding_box>
        // @returns CuboidTag
        // @description
        // Returns a cuboid approximately representing the maximal bounding box of the area (anything this cuboid does not contain, is also not contained by the area, but not vice versa).
        // For single-member CuboidTags, this tag returns a copy of the cuboid.
        // -->
        processor.registerTag("bounding_box", (attribute, area) -> {
            return area.getCuboidBoundary();
        });


        // <--[tag]
        // @attribute <AreaObject.world>
        // @returns WorldTag
        // @description
        // Returns the area's world.
        // -->
        processor.registerTag("world", (attribute, area) -> {
            return area.getWorld();
        });

        // <--[tag]
        // @attribute <AreaObject.players>
        // @returns ListTag(PlayerTag)
        // @description
        // Gets a list of all players currently within the area.
        // -->
        processor.registerTag("players", (attribute, area) -> {
            ListTag result = new ListTag();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (area.doesContainLocation(player.getLocation())) {
                    result.addObject(PlayerTag.mirrorBukkitPlayer(player));
                }
            }
            return result;
        });

        // <--[tag]
        // @attribute <AreaObject.npcs>
        // @returns ListTag(NPCTag)
        // @description
        // Gets a list of all NPCs currently within the area.
        // -->
        if (Depends.citizens != null) {
            processor.registerTag("npcs", (attribute, area) -> {
                ListTag result = new ListTag();
                for (NPC npc : CitizensAPI.getNPCRegistry()) {
                    NPCTag dnpc = new NPCTag(npc);
                    if (area.doesContainLocation(dnpc.getLocation())) {
                        result.addObject(dnpc);
                    }
                }
                return result;
            });
        }

        // <--[tag]
        // @attribute <AreaObject.entities[(<matcher>)]>
        // @returns ListTag(EntityTag)
        // @description
        // Gets a list of all entities currently within the area, with an optional search parameter for the entity.
        // -->
        processor.registerTag("entities", (attribute, area) -> {
            String matcher = attribute.hasContext(1) ? attribute.getContext(1) : null;
            ListTag entities = new ListTag();
            for (Entity ent : area.getCuboidBoundary().getEntitiesPossiblyWithinForTag()) {
                if (area.doesContainLocation(ent.getLocation())) {
                    EntityTag current = new EntityTag(ent);
                    if (matcher == null || BukkitScriptEvent.tryEntity(current, matcher)) {
                        entities.addObject(current.getDenizenObject());
                    }
                }
            }
            return entities;
        });

        // <--[tag]
        // @attribute <AreaObject.living_entities>
        // @returns ListTag(EntityTag)
        // @description
        // Gets a list of all living entities currently within the area.
        // This includes Players, mobs, NPCs, etc., but excludes dropped items, experience orbs, etc.
        // -->
        processor.registerTag("living_entities", (attribute, area) -> {
            ListTag result = new ListTag();
            for (Entity ent : area.getCuboidBoundary().getEntitiesPossiblyWithinForTag()) {
                if (ent instanceof LivingEntity && area.doesContainLocation(ent.getLocation()) && !EntityTag.isCitizensNPC(ent)) {
                    result.addObject(new EntityTag(ent).getDenizenObject());
                }
            }
            return result;
        });

        // <--[tag]
        // @attribute <AreaObject.contains[<location>]>
        // @returns ElementTag(Boolean)
        // @description
        // Returns a boolean indicating whether the specified location is inside this area.
        // -->
        processor.registerTag("contains", (attribute, area) -> {
            if (!attribute.hasContext(1)) {
                return null;
            }
            LocationTag loc = attribute.contextAsType(1, LocationTag.class);
            if (loc == null) {
                return null;
            }
            return new ElementTag(area.doesContainLocation(loc));
        }, "contains_location");

        // <--[tag]
        // @attribute <AreaObject.blocks[(<matcher>)]>
        // @returns ListTag(LocationTag)
        // @description
        // Returns each block location within the area.
        // Optionally, specify a material match to only return locations with that block type.
        // -->
        processor.registerTag("blocks", (attribute, area) -> {
            if (attribute.hasContext(1)) {
                NMSHandler.getChunkHelper().changeChunkServerThread(area.getWorld().getWorld());
                try {
                    String matcher = attribute.getContext(1);
                    Predicate<Location> predicate = (l) -> BukkitScriptEvent.tryMaterial(l.getBlock().getType(), matcher);
                    return area.getBlocks(predicate);
                }
                finally {
                    NMSHandler.getChunkHelper().restoreServerThread(area.getWorld().getWorld());
                }
            }
            return area.getBlocks(null);
        }, "get_blocks");

        // <--[tag]
        // @attribute <AreaObject.spawnable_blocks[(<matcher>)]>
        // @returns ListTag(LocationTag)
        // @description
        // Returns each LocationTag within the area that is safe for players or similar entities to spawn in.
        // Optionally, specify a material matcher to only return locations with that block type.
        // -->
        processor.registerTag("spawnable_blocks", (attribute, area) -> {
                NMSHandler.getChunkHelper().changeChunkServerThread(area.getWorld().getWorld());
                try {
                    if (attribute.hasContext(1)) {
                        String matcher = attribute.getContext(1);
                        Predicate<Location> predicate = (l) -> isSpawnable(l) && BukkitScriptEvent.tryMaterial(l.getBlock().getType(), matcher);
                        return area.getBlocks(predicate);
                    }
                    return area.getBlocks(AreaContainmentObject::isSpawnable);
                }
                finally {
                    NMSHandler.getChunkHelper().restoreServerThread(area.getWorld().getWorld());
                }
        }, "get_spawnable_blocks");

        // <--[tag]
        // @attribute <AreaObject.blocks_flagged[<flag_name>]>
        // @returns ListTag(LocationTag)
        // @description
        // Gets a list of all block locations with a specified flag within the area.
        // Searches the internal flag lists, rather than through all possible blocks.
        // -->
        processor.registerTag("blocks_flagged", (attribute, area) -> {
            if (!attribute.hasContext(1)) {
                return null;
            }
            return area.getBlocksFlagged(CoreUtilities.toLowerCase(attribute.getContext(1)), attribute);
        });

        // <--[tag]
        // @attribute <AreaObject.shell>
        // @returns ListTag(LocationTag)
        // @description
        // Returns each block location on the 3D outer shell of the area.
        // This tag is useful for displaying particles or blocks to mark the boundary of the area.
        // -->
        processor.registerTag("shell", (attribute, area) -> {
            return area.getShell();
        });
        // <--[tag]
        // @attribute <AreaObject.is_within[<cuboid>]>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether this area is fully inside another cuboid.
        // -->
        processor.registerTag("is_within", (attribute, area) -> {
            if (!attribute.hasContext(1)) {
                return null;
            }
            CuboidTag cub2 = attribute.contextAsType(1, CuboidTag.class);
            if (cub2 == null) {
                return null;
            }
            CuboidTag cuboid = area instanceof CuboidTag ? (CuboidTag) area : area.getCuboidBoundary();
            if (cub2 != null) {
                boolean contains = true;
                for (CuboidTag.LocationPair pair2 : cuboid.pairs) {
                    boolean contained = false;
                    for (CuboidTag.LocationPair pair : cub2.pairs) {
                        if (!pair.low.getWorld().getName().equalsIgnoreCase(pair2.low.getWorld().getName())) {
                            return new ElementTag(false);
                        }
                        if (pair2.low.getX() >= pair.low.getX()
                                && pair2.low.getY() >= pair.low.getY()
                                && pair2.low.getZ() >= pair.low.getZ()
                                && pair2.high.getX() <= pair.high.getX()
                                && pair2.high.getY() <= pair.high.getY()
                                && pair2.high.getZ() <= pair.high.getZ()) {
                            contained = true;
                            break;
                        }
                    }
                    if (!contained) {
                        contains = false;
                        break;
                    }
                }
                return new ElementTag(contains);
            }
            return null;
        });

        // <--[tag]
        // @attribute <AreaObject.with_world[<world>]>
        // @returns AreaObject
        // @description
        // Returns a copy of the area, with the specified world.
        // -->
        processor.registerTag("with_world", (attribute, area) -> {
            if (!attribute.hasContext(1)) {
                return null;
            }
            WorldTag world = attribute.contextAsType(1, WorldTag.class);
            if (world == null) {
                return null;
            }
            return area.withWorld(world);
        });
    }

    static boolean isSpawnable(Location loc) {
        return loc.getBlock().getType().isAir()
                && (loc.clone().add(0, 1, 0).getBlock().getType().isAir()
                && (loc.clone().add(0, -1, 0)).getBlock().getType().isSolid());
    }
}
