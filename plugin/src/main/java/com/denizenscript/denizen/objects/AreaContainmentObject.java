package com.denizenscript.denizen.objects;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.utilities.NotedAreaTracker;
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

    static <T extends AreaContainmentObject> void register(Class<T> type, ObjectTagProcessor<T> processor) {

        // <--[tag]
        // @attribute <AreaObject.bounding_box>
        // @returns CuboidTag
        // @description
        // Returns a cuboid approximately representing the maximal bounding box of the area (anything this cuboid does not contain, is also not contained by the area, but not vice versa).
        // For single-member CuboidTags, this tag returns a copy of the cuboid.
        // @example
        // # Notes the polygon's bounding box to efficiently check when things are near the polygon, even if not exactly inside.
        // - note <polygon[my_poly].bounding_box> my_bound_box
        // -->
        processor.registerTag(CuboidTag.class, "bounding_box", (attribute, area) -> {
            return area.getCuboidBoundary();
        });


        // <--[tag]
        // @attribute <AreaObject.world>
        // @returns WorldTag
        // @description
        // Returns the area's world.
        // @example
        // - narrate "The cuboid, 'my_cuboid', is in world: <cuboid[my_cuboid].world.name>!"
        // -->
        processor.registerTag(WorldTag.class, "world", (attribute, area) -> {
            return area.getWorld();
        });

        // <--[tag]
        // @attribute <AreaObject.players>
        // @returns ListTag(PlayerTag)
        // @description
        // Gets a list of all players currently within the area.
        // @example
        // # Narrates a list of players' names that are within the area.
        // # For example: "List of players in 'my_cuboid': steve, alex, john, jane"
        // - narrate "List of players in 'my_cuboid': <cuboid[my_cuboid].players.formatted>"
        // -->
        processor.registerTag(ListTag.class, "players", (attribute, area) -> {
            return new ListTag(Bukkit.getOnlinePlayers(), player -> area.doesContainLocation(player.getLocation()), PlayerTag::new);
        });

        // <--[tag]
        // @attribute <AreaObject.npcs>
        // @returns ListTag(NPCTag)
        // @description
        // Gets a list of all NPCs currently within the area.
        // @example
        // # Narrates a list of NPCs' names that are within the area.
        // # For example: "List of NPCs in 'my_cuboid': steve, alex, john, jane"
        // - narrate "List of NPCs in 'my_cuboid': <cuboid[my_cuboid].npcs.formatted>"
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
        // @example
        // # Spawns a flash particle at the location of every axolotl in the cuboid.
        // - foreach <cuboid[my_cuboid].entities[axolotl]> as:entity:
        //     - playeffect effect:flash at:<[entity].location>
        // -->
        processor.registerTag(ListTag.class, "entities", (attribute, area) -> {
            String matcher = attribute.hasParam() ? attribute.getParam() : null;
            ListTag entities = new ListTag();
            for (Entity ent : area.getCuboidBoundary().getEntitiesPossiblyWithinForTag()) {
                if (area.doesContainLocation(ent.getLocation())) {
                    EntityTag current = new EntityTag(ent);
                    if (matcher == null || current.tryAdvancedMatcher(matcher, attribute.context)) {
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
        // @example
        // # Narrates the name of all the living entities within the area.
        // - foreach <cuboid[my_cuboid].living_entities> as:entity:
        //      - narrate <[entity].name>
        // -->
        processor.registerTag(ListTag.class, "living_entities", (attribute, area) -> {
            return new ListTag(area.getCuboidBoundary().getEntitiesPossiblyWithinForTag(),
                    entity -> entity instanceof LivingEntity && !EntityTag.isCitizensNPC(entity) && area.doesContainLocation(entity.getLocation()),
                    EntityTag::mirrorBukkitEntity);
        });

        // <--[tag]
        // @attribute <AreaObject.contains[<location>]>
        // @returns ElementTag(Boolean)
        // @description
        // Returns a boolean indicating whether the specified location is inside this area.
        // @example
        // # Checks to see if "my_cuboid" contains the player's location.
        // - if <cuboid[my_cuboid].contains[<player.location>]>:
        //      - narrate "You are within 'my_cuboid'!"
        // - else:
        //      - narrate "You are NOT within 'my_cuboid'!"
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
        // @example
        // # Spawns a debugblock to highlight every plank-type block in the area.
        // - debugblock <cuboid[my_cuboid].blocks[*planks]>
        // -->
        processor.registerTag(ListTag.class, "blocks", (attribute, area) -> {
            if (attribute.hasParam()) {
                NMSHandler.chunkHelper.changeChunkServerThread(area.getWorld().getWorld());
                try {
                    String matcher = attribute.getParam();
                    Predicate<Location> predicate = (l) -> new LocationTag(l).tryAdvancedMatcher(matcher, attribute.context);
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
        // @example
        // # Spawns a creeper at a random spawnable block within the area.
        // - spawn creeper <cuboid[my_cuboid].spawnable_blocks.random>
        // -->
        processor.registerTag(ListTag.class, "spawnable_blocks", (attribute, area) -> {
            NMSHandler.chunkHelper.changeChunkServerThread(area.getWorld().getWorld());
            try {
                if (attribute.hasParam()) {
                    String matcher = attribute.getParam();
                    Predicate<Location> predicate = (l) -> SpawnableHelper.isSpawnable(l) && new LocationTag(l.getBlock().getRelative(0, -1, 0).getLocation()).tryAdvancedMatcher(matcher, attribute.context);
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
        // @example
        // # Spawns a debugblock to highlight every block in the cuboid that has the location flag 'my_flag'.
        // - debugblock <cuboid[my_cuboid].blocks_flagged[my_flag]>
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
        // @example
        // # Spawns a hollow sphere of fire around the player.
        // - playeffect effect:flame at:<player.location.to_ellipsoid[5,5,5].shell> offset:0
        // -->
        processor.registerTag(ListTag.class, "shell", (attribute, area) -> {
            return area.getShell();
        });

        // <--[tag]
        // @attribute <AreaObject.is_within[<cuboid>]>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether this area is fully inside another cuboid.
        // @example
        // # Checks to see if "my_cuboid" is within "my_bigger_cuboid".
        // - if <cuboid[my_cuboid].is_within[<cuboid[my_bigger_cuboid]>]>:
        //      - narrate "It is fully within 'my_bigger_cuboid'!"
        // - else:
        //      - narrate "It is not fully within 'my_bigger_cuboid'!"
        // -->
        processor.registerTag(ElementTag.class, CuboidTag.class, "is_within", (attribute, area, cub2) -> {
            CuboidTag cuboid = area instanceof CuboidTag cuboidTag ? cuboidTag : area.getCuboidBoundary();
            if (cub2 != null) {
                boolean contains = true;
                for (CuboidTag.LocationPair pair2 : cuboid.pairs) {
                    boolean contained = false;
                    for (CuboidTag.LocationPair pair : cub2.pairs) {
                        if (!pair.low.getWorld().equals(pair2.low.getWorld())) {
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
        // @example
        // # Notes a copy of "my_cuboid" with the same coordinates transposed from the overworld to the end world.
        // - note my_new_cuboid <cuboid[my_cuboid].with_world[world_the_end]>
        // -->
        processor.registerTag(type, WorldTag.class, "with_world", (attribute, area, world) -> {
            return (T) area.withWorld(world);
        });

        // <--[tag]
        // @attribute <AreaObject.approximate_overlap_areas>
        // @returns ListTag(AreaObject)
        // @description
        // Returns a list of all noted areas that approximately overlap this area.
        // May be inaccurate for objects with complex shapes.
        // Errs on the side of over-inclusion (ie areas that don't overlap may be in the list, but areas that do overlap will never be excluded).
        // -->
        processor.registerTag(ListTag.class, "approximate_overlap_areas", (attribute, area) -> {
            ListTag list = new ListTag();
            CuboidTag.LocationPair pair = area.getCuboidBoundary().pairs.get(0);
            NotedAreaTracker.forEachAreaThatIntersects(pair.low, pair.high, list::addObject);
            return list;
        });
    }

    default boolean areaBaseAdvancedMatches(String matcher) {
        return getNoteName() != null && BukkitScriptEvent.createMatcher(matcher).doesMatch(getNoteName(), text -> {
            if (this instanceof FlaggableObject flaggableObject && text.startsWith("area_flagged:")) {
                AbstractFlagTracker tracker = flaggableObject.getFlagTracker();
                return tracker != null && tracker.hasFlag(text.substring("area_flagged:".length()));
            }
            return false;
        });
    }
}
