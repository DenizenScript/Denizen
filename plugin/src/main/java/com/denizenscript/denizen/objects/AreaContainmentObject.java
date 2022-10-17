package com.denizenscript.denizen.objects;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.utilities.blocks.SpawnableHelper;
import com.denizenscript.denizen.utilities.depends.Depends;
import com.denizenscript.denizen.utilities.flags.LocationFlagSearchHelper;
import com.denizenscript.denizencore.flags.AbstractFlagTracker;
import com.denizenscript.denizencore.flags.FlaggableObject;
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
    // @ExampleTagBase cuboid[my_noted_cuboid]
    // @ExampleValues my_cuboid_note
    // @ExampleForReturns
    // - note %VALUE% as:my_new_area
    // @prefix None
    // @base None
    // @format
    // N/A
    //
    // @description
    // "AreaObject" is a pseudo-ObjectType that represents any object that indicates a world-space area, such as a CuboidTag.
    //
    // @Matchable
    // AreaObject matchers (applies to CuboidTag, EllipsoidTag, PolygonTag, ...), sometimes identified as "<area>":
    // "cuboid" plaintext: matches if the area is a CuboidTag.
    // "ellipsoid" plaintext: matches if the area is an EllipsoidTag.
    // "polygon" plaintext: matches if the area is a PolygonTag.
    // "area_flagged:<flag>": a Flag Matchable for AreaObject flags.
    // Area note name: matches if the AreaObject's note name matches the given advanced matcher.
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

    static <T extends AreaContainmentObject> void registerTags(Class<T> type,  ObjectTagProcessor<T> processor) {

        // <--[tag]
        // @attribute <AreaObject.bounding_box>
        // @returns CuboidTag
        // @description
        // Returns a cuboid approximately representing the maximal bounding box of the area (anything this cuboid does not contain, is also not contained by the area, but not vice versa).
        // For single-member CuboidTags, this tag returns a copy of the cuboid.
        // -->
        processor.registerTag(CuboidTag.class, "bounding_box", (attribute, area) -> {
            return area.getCuboidBoundary();
        });


        // <--[tag]
        // @attribute <AreaObject.world>
        // @returns WorldTag
        // @description
        // Returns the area's world.
        // -->
        processor.registerTag(WorldTag.class, "world", (attribute, area) -> {
            return area.getWorld();
        });

        // <--[tag]
        // @attribute <AreaObject.players>
        // @returns ListTag(PlayerTag)
        // @description
        // Gets a list of all players currently within the area.
        // -->
        processor.registerTag(ListTag.class, "players", (attribute, area) -> {
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
            processor.registerTag(ListTag.class, "npcs", (attribute, area) -> {
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
        processor.registerTag(ListTag.class, "entities", (attribute, area) -> {
            String matcher = attribute.hasParam() ? attribute.getParam() : null;
            ListTag entities = new ListTag();
            for (Entity ent : area.getCuboidBoundary().getEntitiesPossiblyWithinForTag()) {
                if (area.doesContainLocation(ent.getLocation())) {
                    EntityTag current = new EntityTag(ent);
                    if (matcher == null || current.tryAdvancedMatcher(matcher)) {
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
        processor.registerTag(ListTag.class, "living_entities", (attribute, area) -> {
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
        processor.registerTag(ElementTag.class, LocationTag.class, "contains", (attribute, area, loc) -> {
            return new ElementTag(area.doesContainLocation(loc));
        }, "contains_location");

        // <--[tag]
        // @attribute <AreaObject.blocks[(<matcher>)]>
        // @returns ListTag(LocationTag)
        // @description
        // Returns each block location within the area.
        // Optionally, specify a material matcher to only return locations with that block type.
        // -->
        processor.registerTag(ListTag.class, "blocks", (attribute, area) -> {
            if (attribute.hasParam()) {
                NMSHandler.chunkHelper.changeChunkServerThread(area.getWorld().getWorld());
                try {
                    String matcher = attribute.getParam();
                    Predicate<Location> predicate = (l) -> new LocationTag(l).tryAdvancedMatcher(matcher);
                    return area.getBlocks(predicate);
                }
                finally {
                    NMSHandler.chunkHelper.restoreServerThread(area.getWorld().getWorld());
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
        // Uses the same spawnable check as <@link tag LocationTag.is_spawnable>
        // -->
        processor.registerTag(ListTag.class, "spawnable_blocks", (attribute, area) -> {
                NMSHandler.chunkHelper.changeChunkServerThread(area.getWorld().getWorld());
                try {
                    if (attribute.hasParam()) {
                        String matcher = attribute.getParam();
                        Predicate<Location> predicate = (l) -> SpawnableHelper.isSpawnable(l) && new LocationTag(l.getBlock().getRelative(0, -1, 0).getLocation()).tryAdvancedMatcher(matcher);
                        return area.getBlocks(predicate);
                    }
                    return area.getBlocks(SpawnableHelper::isSpawnable);
                }
                finally {
                    NMSHandler.chunkHelper.restoreServerThread(area.getWorld().getWorld());
                }
        }, "get_spawnable_blocks");

        // <--[tag]
        // @attribute <AreaObject.blocks_flagged[<flag_name>]>
        // @returns ListTag(LocationTag)
        // @description
        // Gets a list of all block locations with a specified flag within the area.
        // Searches the internal flag lists, rather than through all possible blocks.
        // -->
        processor.registerTag(ListTag.class, ElementTag.class, "blocks_flagged", (attribute, area, flagName) -> {
            return area.getBlocksFlagged(CoreUtilities.toLowerCase(flagName.toString()), attribute);
        });

        // <--[tag]
        // @attribute <AreaObject.shell>
        // @returns ListTag(LocationTag)
        // @description
        // Returns each block location on the 3D outer shell of the area.
        // This tag is useful for displaying particles or blocks to mark the boundary of the area.
        // -->
        processor.registerTag(ListTag.class, "shell", (attribute, area) -> {
            return area.getShell();
        });

        // <--[tag]
        // @attribute <AreaObject.is_within[<cuboid>]>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether this area is fully inside another cuboid.
        // -->
        processor.registerTag(ElementTag.class, CuboidTag.class, "is_within", (attribute, area, cub2) -> {
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
        processor.registerTag(type, WorldTag.class, "with_world", (attribute, area, world) -> {
            return (T) area.withWorld(world);
        });
    }

    default boolean areaBaseAdvancedMatches(String matcher) {
        String matcherLow = CoreUtilities.toLowerCase(matcher);
        if (matcherLow.startsWith("area_flagged:")) {
            AbstractFlagTracker tracker = ((FlaggableObject) this).getFlagTracker();
            return tracker != null && tracker.hasFlag(matcher.substring("area_flagged:".length()));
        }
        if (getNoteName() != null && BukkitScriptEvent.runGenericCheck(matcher, getNoteName())) {
            return true;
        }
        return false;
    }
}
