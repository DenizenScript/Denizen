package com.denizenscript.denizen.objects;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.notable.NotableManager;
import com.denizenscript.denizen.utilities.Settings;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.depends.Depends;
import com.denizenscript.denizen.utilities.flags.LocationFlagSearchHelper;
import com.denizenscript.denizencore.flags.AbstractFlagTracker;
import com.denizenscript.denizencore.flags.FlaggableObject;
import com.denizenscript.denizencore.flags.SavableMapFlagTracker;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.notable.Notable;
import com.denizenscript.denizencore.objects.notable.Note;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.tags.TagRunnable;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PolygonTag implements ObjectTag, Cloneable, Notable, Adjustable, AreaContainmentObject, FlaggableObject {

    // <--[language]
    // @name PolygonTag Objects
    // @group Object System
    // @description
    // A PolygonTag represents a polygonal region in the world.
    //
    // The word 'polygon' means an arbitrary 2D shape.
    // PolygonTags, in addition to a 2D polygon, contain a minimum and maximum Y coordinate, to allow them to function in 3D.
    //
    // PolygonTags are NOT polyhedra.
    //
    // These use the object notation "polygon@".
    // The identity format for cuboids is <world>,<y-min>,<y-max>,<x1>,<z1>,... (the x,z pair repeats for as many points as the polygon has).
    //
    // A PolygonTag with 4 points at right angles would cover an area also possible to be defined by a CuboidTag, however all other shapes a PolygonTag can form are unique.
    //
    // Compared to CuboidTags, PolygonTags are generally slower to process and more complex to work with, but offer the benefit of supporting more intricate shapes.
    //
    // Note that forming invalid polygons (duplicate corners, impossible shapes, etc) will not necessarily give any error message, and may cause weird results.
    //
    // This object type can be noted.
    //
    // This object type is flaggable when it is noted.
    // Flags on this object type will be stored in the notables.yml file.
    //
    // -->

    public WorldTag world;

    public double yMin = 0, yMax = 0;

    public List<Corner> corners = new ArrayList<>();

    public Corner boxMin = new Corner(), boxMax = new Corner();

    public String noteName = null;

    public AbstractFlagTracker flagTracker = null;

    public static class Corner {
        public double x, z;

        public Corner() {
        }

        public Corner(double x, double z) {
            this.x = x;
            this.z = z;
        }
    }

    public PolygonTag(WorldTag world) {
        this.world = world;
    }

    public PolygonTag(WorldTag world, double yMin, double yMax, List<Corner> corners) {
        this.world = world;
        this.yMin = yMin;
        this.yMax = yMax;
        for (Corner corner : corners) {
            this.corners.add(new Corner(corner.x, corner.z));
        }
        recalculateBox();
    }

    public void recalculateBox() {
        if (corners.size() == 0) {
            return;
        }
        Corner firstCorner = corners.get(0);
        boxMin.x = firstCorner.x;
        boxMin.z = firstCorner.z;
        boxMax.x = firstCorner.x;
        boxMax.z = firstCorner.z;
        for (Corner corner : corners) {
            recalculateToFit(corner);
        }
    }

    public void recalculateToFit(Corner corner) {
        boxMin.x = Math.min(boxMin.x, corner.x);
        boxMin.z = Math.min(boxMin.z, corner.z);
        boxMax.x = Math.max(boxMax.x, corner.x);
        boxMax.z = Math.max(boxMax.z, corner.z);
    }

    @Override
    public PolygonTag clone() {
        return new PolygonTag(world, yMin, yMax, corners);
    }

    @Fetchable("polygon")
    public static PolygonTag valueOf(String string, TagContext context) {
        if (string.startsWith("polygon@")) {
            string = string.substring("polygon@".length());
        }
        if (string.contains("@")) {
            return null;
        }
        Notable saved = NotableManager.getSavedObject(string);
        if (saved instanceof PolygonTag) {
            return (PolygonTag) saved;
        }
        List<String> parts = CoreUtilities.split(string, ',');
        if (parts.size() < 3) {
            return null;
        }
        WorldTag world = new WorldTag(parts.get(0));
        for (int i = 1; i < parts.size(); i++) {
            if (!ArgumentHelper.matchesDouble(parts.get(i))) {
                return null;
            }
        }
        PolygonTag toReturn = new PolygonTag(world);
        toReturn.yMin = Double.parseDouble(parts.get(1));
        toReturn.yMax = Double.parseDouble(parts.get(2));
        for (int i = 3; i < parts.size(); i += 2) {
            Corner corner = new Corner(Double.parseDouble(parts.get(i)), Double.parseDouble(parts.get(i + 1)));
            toReturn.corners.add(corner);
        }
        toReturn.recalculateBox();
        return toReturn;
    }

    public static boolean matches(String string) {
        if (valueOf(string, CoreUtilities.noDebugContext) != null) {
            return true;
        }
        return false;
    }

    @Override
    public ObjectTag duplicate() {
        if (noteName != null) {
            return this;
        }
        return clone();
    }

    @Override
    public int hashCode() {
        if (noteName != null) {
            return noteName.hashCode();
        }
        return (int) (boxMin.x + boxMin.z * 7 + boxMax.x * 29 + boxMax.z * 61);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof PolygonTag)) {
            return false;
        }
        PolygonTag poly2 = (PolygonTag) other;
        if (poly2.corners.size() != corners.size()) {
            return false;
        }
        if ((noteName == null) != (poly2.noteName == null)) {
            return false;
        }
        if (noteName != null && !noteName.equals(poly2.noteName)) {
            return false;
        }
        if (!world.getName().equals(poly2.world.getName())) {
            return false;
        }
        if (yMin != poly2.yMin || yMax != poly2.yMax) {
            return false;
        }
        for (int i = 0; i < corners.size(); i++) {
            Corner corner1 = corners.get(i);
            Corner corner2 = poly2.corners.get(i);
            if (corner1.x != corner2.x || corner1.z != corner2.z) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String getNoteName() {
        return noteName;
    }

    public static List<PolygonTag> getNotedPolygonsContaining(Location location) {
        List<PolygonTag> polygons = new ArrayList<>();
        for (PolygonTag polygon : NotableManager.getAllType(PolygonTag.class)) {
            if (polygon.doesContainLocation(location)) {
                polygons.add(polygon);
            }
        }
        return polygons;
    }

    @Override
    public boolean doesContainLocation(Location loc) {
        if (loc.getWorld() == null) {
            return false;
        }
        if (!loc.getWorld().getName().equals(world.getName())) {
            return false;
        }
        double x = loc.getX();
        double z = loc.getZ();
        if (x < boxMin.x || x > boxMax.x || z < boxMin.z || z > boxMax.z) {
            return false;
        }
        double y = loc.getY();
        if (y < yMin || y > yMax) {
            return false;
        }
        boolean isInside = false;
        for (int i = 0; i < corners.size(); i++) {
            Corner start = corners.get(i);
            Corner end = (i + 1 == corners.size() ? corners.get(0) : corners.get(i + 1));
            if (((start.z > z) != (end.z > z)) && (x < (end.x - start.x) * (z - start.z) / (end.z - start.z) + start.x)) {
                isInside = !isInside;
            }
        }
        return isInside;
    }

    public List<LocationTag> generateFlatBlockShell(double y) {
        int max = Settings.blockTagsMaxBlocks();
        ArrayList<LocationTag> toOutput = new ArrayList<>();
        for (int x = (int) Math.floor(boxMin.x); x < boxMax.x; x++) {
            for (int z = (int) Math.floor(boxMin.z); z < boxMax.z; z++) {
                LocationTag possible = new LocationTag(x + 0.5, y, z + 0.5, world.getName());
                if (doesContainLocation(possible)) {
                    toOutput.add(possible);
                }
                max--;
                if (max <= 0) {
                    return toOutput;
                }
            }
        }
        return toOutput;
    }

    public ListTag getShell() {
        int max = Settings.blockTagsMaxBlocks();
        ListTag addTo = new ListTag();
        List<LocationTag> flatShell = generateFlatBlockShell(yMin);
        for (LocationTag loc : flatShell) {
            addTo.addObject(loc.clone());
        }
        max -= flatShell.size();
        if (max <= 0) {
            return addTo;
        }
        for (LocationTag loc : flatShell) {
            LocationTag newLoc = loc.clone();
            newLoc.setY(yMax);
            addTo.addObject(newLoc);
        }
        max -= flatShell.size();
        if (max <= 0) {
            return addTo;
        }
        int per = (int) (yMax - yMin);
        if (per == 0) {
            return addTo;
        }
        for (int i = 0; i < corners.size(); i++) {
            Corner start = corners.get(i);
            Corner end = (i + 1 == corners.size() ? corners.get(0) : corners.get(i + 1));
            double xMove = (end.x - start.x);
            double zMove = (end.z - start.z);
            double len = Math.sqrt(xMove * xMove + zMove * zMove);
            if (len < 0.1) {
                continue;
            }
            xMove /= len;
            zMove /= len;
            double xSpot = start.x;
            double zSpot = start.z;
            max -= (int) (len + 1);
            if (max <= 0) {
                return addTo;
            }
            for (int j = 0; j < len; j++) {
                for (double y = yMin + 1; y < yMax; y++) {
                    addTo.addObject(new LocationTag(xSpot, y, zSpot, world.getName()));
                }
                max -= per;
                if (max <= 0) {
                    return addTo;
                }
                xSpot += xMove;
                zSpot += zMove;
            }
        }
        return addTo;
    }

    public ListTag getBlocks(String matcher, Attribute attribute) {
        int max = Settings.blockTagsMaxBlocks();
        ListTag addTo = new ListTag();
        List<LocationTag> flatShell = generateFlatBlockShell(yMin);
        for (double y = yMin; y < yMax; y++) {
            for (LocationTag loc : flatShell) {
                LocationTag newLoc = loc.clone();
                newLoc.setY(y);
                if (matcher == null || BukkitScriptEvent.tryMaterial(newLoc.getBlockTypeForTag(attribute), matcher)) {
                    addTo.addObject(newLoc);
                }
            }
            max -= flatShell.size();
            if (max <= 0) {
                return addTo;
            }
        }
        return addTo;
    }

    public void addOutline2D(double y, ListTag addTo) {
        int max = Settings.blockTagsMaxBlocks();
        for (int i = 0; i < corners.size(); i++) {
            Corner start = corners.get(i);
            Corner end = (i + 1 == corners.size() ? corners.get(0) : corners.get(i + 1));
            double xMove = (end.x - start.x);
            double zMove = (end.z - start.z);
            double len = Math.sqrt(xMove * xMove + zMove * zMove);
            if (len < 0.1) {
                continue;
            }
            xMove /= len;
            zMove /= len;
            double xSpot = start.x;
            double zSpot = start.z;
            max -= (int) (len + 1);
            if (max <= 0) {
                return;
            }
            for (int j = 0; j < len; j++) {
                addTo.addObject(new LocationTag(xSpot, y, zSpot, world.getName()));
                xSpot += xMove;
                zSpot += zMove;
            }
        }
    }

    public ListTag getOutline() {
        int max = Settings.blockTagsMaxBlocks();
        ListTag output = new ListTag();
        addOutline2D(yMin, output);
        if (max <= output.size()) {
            return output;
        }
        if (yMin != yMax) {
            addOutline2D(yMax, output);
        }
        max -= output.size();
        if (max <= 0) {
            return output;
        }
        int per = (int) (yMax - yMin);
        if (per == 0) {
            return output;
        }
        for (Corner corner : corners) {
            for (double y = yMin + 1; y < yMax; y++) {
                output.addObject(new LocationTag(corner.x, y, corner.z, world.getName()));
            }
            max -= per;
            if (max <= 0) {
                return output;
            }
        }
        return output;
    }

    @Override
    public boolean isUnique() {
        return noteName != null;
    }

    @Override
    @Note("Polygons")
    public Object getSaveObject() {
        ConfigurationSection section = new YamlConfiguration();
        section.set("object", identifyFull());
        section.set("flags", flagTracker.toString());
        return section;
    }

    @Override
    public void makeUnique(String id) {
        PolygonTag toNote = clone();
        toNote.noteName = id;
        toNote.flagTracker = new SavableMapFlagTracker();
        NotableManager.saveAs(toNote, id);
    }

    @Override
    public void forget() {
        NotableManager.remove(this);
        noteName = null;
        flagTracker = null;
    }

    String prefix = "Polygon";

    @Override
    public String getObjectType() {
        return "polygon";
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public PolygonTag setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    @Override
    public String debuggable() {
        if (isUnique()) {
            return "polygon@" + noteName + " <GR>(" + identifyFull() + ")";
        }
        else {
            return identifyFull();
        }
    }

    @Override
    public String identify() {
        if (isUnique()) {
            return "polygon@" + noteName;
        }
        else {
            return identifyFull();
        }
    }

    @Override
    public String identifySimple() {
        return identify();
    }

    public String identifyFull() {
        StringBuilder sb = new StringBuilder();
        sb.append("polygon@").append(world.getName()).append(",").append(yMin).append(",").append(yMax).append(",");
        for (Corner corner : corners) {
            sb.append(corner.x).append(",").append(corner.z).append(",");
        }
        if (corners.size() > 0) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return identify();
    }

    @Override
    public AbstractFlagTracker getFlagTracker() {
        return flagTracker;
    }

    @Override
    public void reapplyTracker(AbstractFlagTracker tracker) {
        if (noteName != null) {
            this.flagTracker = tracker;
        }
    }

    @Override
    public String getReasonNotFlaggable() {
        if (noteName == null) {
            return "the area is not noted - only noted areas can hold flags";
        }
        return "unknown reason - something went wrong";
    }

    public static void registerTags() {

        AbstractFlagTracker.registerFlagHandlers(tagProcessor);

        // <--[tag]
        // @attribute <PolygonTag.contains[<location>]>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the specified location is within the boundaries of the polygon.
        // -->
        registerTag("contains", (attribute, polygon) -> {
            if (!attribute.hasContext(1)) {
                attribute.echoError("PolygonTag.contains[...] tag must have an input.");
                return null;
            }
            LocationTag toCheck = attribute.contextAsType(1, LocationTag.class);
            return new ElementTag(polygon.doesContainLocation(toCheck));
        });

        // <--[tag]
        // @attribute <PolygonTag.bounding_box>
        // @returns CuboidTag
        // @description
        // Returns a cuboid approximately representing the maximal bounding box of the polygon (anything this cuboid does not contain, is also not contained by the polygon, but not vice versa).
        // -->
        registerTag("bounding_box", (attribute, polygon) -> {
            LocationTag min = new LocationTag(Math.floor(polygon.boxMin.x), Math.floor(polygon.yMin), Math.floor(polygon.boxMin.z), polygon.world.getName());
            LocationTag max = new LocationTag(Math.ceil(polygon.boxMax.x), Math.ceil(polygon.yMax), Math.ceil(polygon.boxMax.z), polygon.world.getName());
            return new CuboidTag(min, max);
        });

        // <--[tag]
        // @attribute <PolygonTag.max_y>
        // @returns ElementTag(Decimal)
        // @description
        // Returns the maximum Y level for this polygon.
        // -->
        registerTag("max_y", (attribute, polygon) -> {
            return new ElementTag(polygon.yMax);
        });

        // <--[tag]
        // @attribute <PolygonTag.min_y>
        // @returns ElementTag(Decimal)
        // @description
        // Returns the minimum Y level for this polygon.
        // -->
        registerTag("min_y", (attribute, polygon) -> {
            return new ElementTag(polygon.yMin);
        });

        // <--[tag]
        // @attribute <PolygonTag.world>
        // @returns WorldTag
        // @description
        // Returns the polygon's world.
        // -->
        registerTag("world", (attribute, polygon) -> {
            return polygon.world;
        });

        // <--[tag]
        // @attribute <PolygonTag.players>
        // @returns ListTag(PlayerTag)
        // @description
        // Gets a list of all players currently within the PolygonTag.
        // -->
        registerTag("players", (attribute, polygon) -> {
            ArrayList<PlayerTag> players = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (polygon.doesContainLocation(player.getLocation())) {
                    players.add(PlayerTag.mirrorBukkitPlayer(player));
                }
            }
            return new ListTag(players);
        });

        // <--[tag]
        // @attribute <PolygonTag.npcs>
        // @returns ListTag(NPCTag)
        // @description
        // Gets a list of all NPCs currently within the PolygonTag.
        // -->
        if (Depends.citizens != null) {
            registerTag("npcs", (attribute, polygon) -> {
                ArrayList<NPCTag> npcs = new ArrayList<>();
                for (NPC npc : CitizensAPI.getNPCRegistry()) {
                    NPCTag dnpc = new NPCTag(npc);
                    if (polygon.doesContainLocation(dnpc.getLocation())) {
                        npcs.add(dnpc);
                    }
                }
                return new ListTag(npcs);
            });
        }

        // <--[tag]
        // @attribute <PolygonTag.entities[(<matcher>)]>
        // @returns ListTag(EntityTag)
        // @description
        // Gets a list of all entities currently within the PolygonTag, with an optional search parameter for the entity.
        // -->
        registerTag("entities", (attribute, polygon) -> {
            String matcher = attribute.hasContext(1) ? attribute.getContext(1) : null;
            ListTag entities = new ListTag();
            for (Entity ent : polygon.world.getEntitiesForTag()) {
                EntityTag current = new EntityTag(ent);
                if (polygon.doesContainLocation(ent.getLocation())) {
                    if (matcher == null || BukkitScriptEvent.tryEntity(current, matcher)) {
                        entities.addObject(new EntityTag(ent).getDenizenObject());
                    }
                }
            }
            return entities;
        });

        // <--[tag]
        // @attribute <PolygonTag.living_entities>
        // @returns ListTag(EntityTag)
        // @description
        // Gets a list of all living entities currently within the PolygonTag.
        // This includes Players, mobs, NPCs, etc., but excludes dropped items, experience orbs, etc.
        // -->
        registerTag("living_entities", (attribute, polygon) -> {
            ArrayList<EntityTag> entities = new ArrayList<>();
            for (Entity ent : polygon.world.getWorld().getLivingEntities()) {
                if (polygon.doesContainLocation(ent.getLocation()) && !EntityTag.isCitizensNPC(ent)) {
                    entities.add(new EntityTag(ent));
                }
            }
            return new ListTag(entities);
        });

        // <--[tag]
        // @attribute <PolygonTag.note_name>
        // @returns ElementTag
        // @description
        // Gets the name of a noted PolygonTag. If the polygon isn't noted, this is null.
        // -->
        registerTag("note_name", (attribute, polygon) -> {
            String noteName = NotableManager.getSavedId(polygon);
            if (noteName == null) {
                return null;
            }
            return new ElementTag(noteName);
        });

        // <--[tag]
        // @attribute <PolygonTag.corners>
        // @returns ListTag(LocationTag)
        // @description
        // Returns a list of the polygon's corners, as locations with Y coordinate set to the y-min value.
        // -->
        registerTag("corners", (attribute, polygon) -> {
            ListTag list = new ListTag();
            for (Corner corner : polygon.corners) {
                list.addObject(new LocationTag(corner.x, polygon.yMin, corner.z, polygon.world.getName()));
            }
            return list;
        });

        // <--[tag]
        // @attribute <PolygonTag.shift[<vector>]>
        // @returns PolygonTag
        // @description
        // Returns a copy of the polygon, with all coordinates shifted by the given location-vector.
        // -->
        registerTag("shift", (attribute, polygon) -> {
            if (!attribute.hasContext(1)) {
                attribute.echoError("PolygonTag.shift[...] tag must have an input.");
                return null;
            }
            LocationTag shift = attribute.contextAsType(1, LocationTag.class);
            PolygonTag toReturn = polygon.clone();
            toReturn.yMin += shift.getY();
            toReturn.yMax += shift.getY();
            for (Corner corner : toReturn.corners) {
                corner.x += shift.getX();
                corner.z += shift.getZ();
            }
            toReturn.boxMin.x += shift.getX();
            toReturn.boxMin.z += shift.getZ();
            toReturn.boxMax.x += shift.getX();
            toReturn.boxMax.z += shift.getZ();
            return toReturn;
        });

        // <--[tag]
        // @attribute <PolygonTag.with_corner[<location>]>
        // @returns PolygonTag
        // @mechanism PolygonTag.add_corner
        // @description
        // Returns a copy of the polygon, with the specified corner added to the end of the corner list.
        // -->
        registerTag("with_corner", (attribute, polygon) -> {
            if (!attribute.hasContext(1)) {
                attribute.echoError("PolygonTag.with_corner[...] tag must have an input.");
                return null;
            }
            LocationTag corner = attribute.contextAsType(1, LocationTag.class);
            PolygonTag toReturn = polygon.clone();
            Corner added = new Corner(corner.getX(), corner.getZ());
            toReturn.corners.add(added);
            toReturn.recalculateToFit(added);
            return toReturn;
        });

        // <--[tag]
        // @attribute <PolygonTag.with_y_min[<#.#>]>
        // @returns PolygonTag
        // @description
        // Returns a copy of the polygon, with the specified minimum-Y value.
        // -->
        registerTag("with_y_min", (attribute, polygon) -> {
            if (!attribute.hasContext(1)) {
                attribute.echoError("PolygonTag.with_y_min[...] tag must have an input.");
                return null;
            }
            PolygonTag toReturn = polygon.clone();
            toReturn.yMin = attribute.getDoubleContext(1);
            return toReturn;
        });

        // <--[tag]
        // @attribute <PolygonTag.with_y_max[<#.#>]>
        // @returns PolygonTag
        // @description
        // Returns a copy of the polygon, with the specified maximum-Y value.
        // -->
        registerTag("with_y_max", (attribute, polygon) -> {
            if (!attribute.hasContext(1)) {
                attribute.echoError("PolygonTag.with_y_max[...] tag must have an input.");
                return null;
            }
            PolygonTag toReturn = polygon.clone();
            toReturn.yMax = attribute.getDoubleContext(1);
            return toReturn;
        });

        // <--[tag]
        // @attribute <PolygonTag.with_world[<world>]>
        // @returns PolygonTag
        // @description
        // Returns a copy of the polygon, with the specified world.
        // -->
        registerTag("with_world", (attribute, polygon) -> {
            if (!attribute.hasContext(1)) {
                attribute.echoError("PolygonTag.with_world[...] tag must have an input.");
                return null;
            }
            PolygonTag toReturn = polygon.clone();
            toReturn.world = attribute.contextAsType(1, WorldTag.class);
            if (toReturn.world == null) {
                return null;
            }
            return toReturn;
        });

        // <--[tag]
        // @attribute <PolygonTag.include_y[<#.#>]>
        // @returns PolygonTag
        // @description
        // Returns a copy of the polygon, with the specified Y value included as part of the Y range (expanding the Y-min or Y-max as needed).
        // -->
        registerTag("include_y", (attribute, polygon) -> {
            if (!attribute.hasContext(1)) {
                attribute.echoError("PolygonTag.include_y[...] tag must have an input.");
                return null;
            }
            PolygonTag toReturn = polygon.clone();
            double y = attribute.getDoubleContext(1);
            toReturn.yMin = Math.min(y, toReturn.yMin);
            toReturn.yMax = Math.max(y, toReturn.yMax);
            return toReturn;
        });

        // <--[tag]
        // @attribute <PolygonTag.outline_2d[<#.#>]>
        // @returns ListTag(LocationTag)
        // @description
        // Returns a list of locations along the 2D outline of this polygon, at the specified Y level (roughly a block-width of separation between each).
        // -->
        registerTag("outline_2d", (attribute, polygon) -> {
            if (!attribute.hasContext(1)) {
                attribute.echoError("PolygonTag.outline_2d[...] tag must have an input.");
                return null;
            }
            double y = attribute.getDoubleContext(1);
            ListTag output = new ListTag();
            polygon.addOutline2D(y, output);
            return output;
        });

        // <--[tag]
        // @attribute <PolygonTag.outline>
        // @returns ListTag(LocationTag)
        // @description
        // Returns a list of locations along the 3D outline of this polygon (roughly a block-width of separation between each).
        // -->
        registerTag("outline", (attribute, polygon) -> {
            return polygon.getOutline();
        });

        // <--[tag]
        // @attribute <PolygonTag.shell>
        // @returns ListTag(LocationTag)
        // @description
        // Returns a list of locations along the 3D shell of this polygon (roughly a block-width of separation between each).
        // -->
        registerTag("shell", (attribute, polygon) -> {
            return polygon.getShell();
        });

        // <--[tag]
        // @attribute <PolygonTag.blocks[(<matcher>)]>
        // @returns ListTag(LocationTag)
        // @description
        // Returns a list of block locations within the polygon.
        // Optionally, specify a list of materials to only return locations with that block type.
        // -->
        registerTag("blocks", (attribute, polygon) -> {
            return polygon.getBlocks(attribute.hasContext(1) ? attribute.getContext(1) : null, attribute);
        });

        // <--[tag]
        // @attribute <PolygonTag.blocks_flagged[<flag_name>]>
        // @returns ListTag(LocationTag)
        // @description
        // Gets a list of all block locations with a specified flag within the polygon.
        // Searches the internal flag lists, rather than through all possible blocks.
        // -->
        registerTag("blocks_flagged", (attribute, polygon) -> {
            if (!attribute.hasContext(1)) {
                attribute.echoError("PolygonTag.blocks_flagged[...] must have an input value.");
                return null;
            }
            String flagName = CoreUtilities.toLowerCase(attribute.getContext(1));
            ListTag blocks = new ListTag();
            int chunkMinX = ((int) Math.floor(polygon.boxMin.x)) >> 4;
            int chunkMinZ = ((int) Math.floor(polygon.boxMin.z)) >> 4;
            int chunkMaxX = ((int) Math.ceil(polygon.boxMax.x)) >> 4;
            int chunkMaxZ = ((int) Math.ceil(polygon.boxMax.z)) >> 4;
            ChunkTag testChunk = new ChunkTag(polygon.world, chunkMinX, chunkMinZ);
            for (int x = chunkMinX; x <= chunkMaxX; x++) {
                testChunk.chunkX = x;
                for (int z = chunkMinZ; z <= chunkMaxZ; z++) {
                    testChunk.chunkZ = z;
                    testChunk.cachedChunk = null;
                    if (testChunk.isLoadedSafe()) {
                        LocationFlagSearchHelper.getFlaggedLocations(testChunk.getChunkForTag(attribute), flagName, (loc) -> {
                            if (polygon.doesContainLocation(loc)) {
                                blocks.addObject(new LocationTag(loc));
                            }
                        });
                    }
                }
            }
            return blocks;
        });
    }

    public static ObjectTagProcessor<PolygonTag> tagProcessor = new ObjectTagProcessor<>();

    public static void registerTag(String name, TagRunnable.ObjectInterface<PolygonTag> runnable, String... variants) {
        tagProcessor.registerTag(name, runnable, variants);
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {
        return tagProcessor.getObjectAttribute(this, attribute);
    }

    public void applyProperty(Mechanism mechanism) {
        if (NotableManager.isExactSavedObject(this)) {
            Debug.echoError("Cannot apply properties to noted objects.");
            return;
        }
        adjust(mechanism);
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object PolygonTag
        // @name add_corner
        // @input LocationTag
        // @description
        // Adds a corner to the end of the polygon's corner list.
        // @tags
        // <PolygonTag.with_corner[<location>]>
        // -->
        if (mechanism.matches("add_corner") && mechanism.requireObject(LocationTag.class)) {
            LocationTag loc = mechanism.valueAsType(LocationTag.class);
            Corner newCorner = new Corner(loc.getX(), loc.getZ());
            corners.add(newCorner);
            recalculateToFit(newCorner);
        }
    }
}
