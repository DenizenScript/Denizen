package com.denizenscript.denizen.objects;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.utilities.NotedAreaTracker;
import com.denizenscript.denizen.utilities.Settings;
import com.denizenscript.denizencore.flags.AbstractFlagTracker;
import com.denizenscript.denizencore.flags.FlaggableObject;
import com.denizenscript.denizencore.flags.SavableMapFlagTracker;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.notable.Notable;
import com.denizenscript.denizencore.objects.notable.Note;
import com.denizenscript.denizencore.objects.notable.NoteManager;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.YamlConfiguration;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class PolygonTag implements ObjectTag, Cloneable, Notable, Adjustable, AreaContainmentObject, FlaggableObject {

    // <--[ObjectType]
    // @name PolygonTag
    // @prefix polygon
    // @base ElementTag
    // @implements FlaggableObject, AreaObject
    // @ExampleTagBase polygon[my_noted_polygon]
    // @ExampleValues my_polygon_note
    // @ExampleForReturns
    // - note %VALUE% as:my_new_polygon
    // @format
    // The identity format for polygons is <world>,<y-min>,<y-max>,<x1>,<z1>,... (the x,z pair repeats for as many points as the polygon has).
    //
    // @description
    // A PolygonTag represents a polygonal region in the world.
    //
    // The word 'polygon' means an arbitrary 2D shape.
    // PolygonTags, in addition to a 2D polygon, contain a minimum and maximum Y coordinate, to allow them to function in 3D.
    //
    // PolygonTags are NOT polyhedra.
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
    // @Matchable
    // Refer to <@link objecttype areaobject>'s matchable list.
    //
    // -->

    public WorldTag world;

    public double yMin = 0, yMax = 0;

    public List<Corner> corners = new ArrayList<>();

    public Corner boxMin = new Corner(), boxMax = new Corner();

    public String noteName = null, priorNoteName = null;

    public AbstractFlagTracker flagTracker = null;

    public static boolean preferInclusive = false;

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
        if (!TagManager.isStaticParsing) {
            Notable saved = NoteManager.getSavedObject(string);
            if (saved instanceof PolygonTag) {
                return (PolygonTag) saved;
            }
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
        PolygonTag self = refreshState();
        if (self.noteName != null) {
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
        if ((noteName == null) != (poly2.noteName == null)) {
            return false;
        }
        if (noteName != null) {
            return noteName.equals(poly2.noteName);
        }
        if (poly2.corners.size() != corners.size()) {
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

    @Override
    public boolean doesContainLocation(Location loc) {
        return preferInclusive ? containsInclusive(loc) : containsPrecise(loc);
    }

    public boolean containsPrecise(Location loc) {
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

    public boolean containsInclusive(Location loc) {
        if (loc.getWorld() == null) {
            return false;
        }
        if (!loc.getWorld().getName().equals(world.getName())) {
            return false;
        }
        int targetY = loc.getBlockY();
        if (targetY < Math.floor(yMin) || targetY > Math.floor(yMax)) {
            return false;
        }
        int targetX = loc.getBlockX();
        int targetZ = loc.getBlockZ();
        boolean isInside = false;
        for (int i = 0; i < corners.size(); i++) {
            Corner start = corners.get(i);
            Corner end = (i + 1 == corners.size() ? corners.get(0) : corners.get(i + 1));
            int xStart = (int) Math.floor(start.x);
            int zStart = (int) Math.floor(start.z);
            int xEnd = (int) Math.floor(end.x);
            int zEnd = (int) Math.floor(end.z);
            if (xEnd == targetX && zEnd == targetZ) {
                return true; // exactly on corner
            }
            int x1, x2, z1, z2;
            if (xEnd > xStart) {
                x1 = xStart;
                x2 = xEnd;
                z1 = zStart;
                z2 = zEnd;
            }
            else {
                x1 = xEnd;
                x2 = xStart;
                z1 = zEnd;
                z2 = zStart;
            }
            if (x1 <= targetX && targetX <= x2) {
                long crossProduct = ((long) targetZ - (long) z1) * (long) (x2 - x1) - ((long) z2 - (long) z1) * (long) (targetX - x1);
                if (crossProduct == 0) {
                    if ((z1 <= targetZ) == (targetZ <= z2)) {
                        return true; // exactly along edge
                    }
                }
                else if (crossProduct < 0 && (x1 != targetX)) {
                    isInside = !isInside;
                }
            }
        }
        return isInside;
    }

    public List<LocationTag> generateFlatBlockShell(double y, boolean inclusive) {
        int max = Settings.blockTagsMaxBlocks();
        ArrayList<LocationTag> toOutput = new ArrayList<>();
        for (int x = (int) Math.floor(boxMin.x); x < boxMax.x; x++) {
            for (int z = (int) Math.floor(boxMin.z); z < boxMax.z; z++) {
                LocationTag possible = new LocationTag(x + 0.5, y, z + 0.5, world.getName());
                if (inclusive ? containsInclusive(possible) : containsPrecise(possible)) {
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

    @Override
    public ListTag getShell() {
        return getShellInternal(preferInclusive);
    }

    public ListTag getShellInternal(boolean inclusive) {
        int max = Settings.blockTagsMaxBlocks();
        ListTag addTo = new ListTag();
        List<LocationTag> flatShell = generateFlatBlockShell(yMin, inclusive);
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

    @Override
    public ListTag getBlocks(Predicate<Location> test) {
        return getBlocksInternal(test, preferInclusive);
    }

    public ListTag getBlocksInternal(Predicate<Location> test, boolean inclusive) {
        int max = Settings.blockTagsMaxBlocks();
        ListTag addTo = new ListTag();
        List<LocationTag> flatShell = generateFlatBlockShell(yMin, inclusive);
        for (double y = yMin; y < yMax; y++) {
            for (LocationTag loc : flatShell) {
                LocationTag newLoc = loc.clone();
                newLoc.setY(y);
                if (test == null || test.test(newLoc)) {
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
        YamlConfiguration section = new YamlConfiguration();
        section.set("object", identifyFull());
        section.set("flags", flagTracker.toString());
        return section;
    }

    @Override
    public void makeUnique(String id) {
        PolygonTag toNote = clone();
        toNote.noteName = id;
        toNote.flagTracker = new SavableMapFlagTracker();
        NoteManager.saveAs(toNote, id);
        NotedAreaTracker.add(toNote);
    }

    @Override
    public void forget() {
        if (noteName == null) {
            return;
        }
        priorNoteName = noteName;
        NotedAreaTracker.remove(this);
        NoteManager.remove(this);
        noteName = null;
        flagTracker = null;
    }

    @Override
    public PolygonTag refreshState() {
        if (noteName == null && priorNoteName != null) {
            Notable note = NoteManager.getSavedObject(priorNoteName);
            if (note instanceof PolygonTag) {
                return (PolygonTag) note;
            }
            priorNoteName = null;
        }
        return this;
    }

    String prefix = "Polygon";

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
        PolygonTag self = refreshState();
        if (self.isUnique()) {
            return "<LG>polygon@<Y>" + self.noteName + " <GR>(" + self.identifyFull() + ")";
        }
        else {
            return self.identifyFull().replace(",", "<LG>, <Y>");
        }
    }

    @Override
    public String identify() {
        PolygonTag self = refreshState();
        if (self.isUnique()) {
            return "polygon@" + self.noteName;
        }
        else {
            return self.identifyFull();
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

    @Override
    public CuboidTag getCuboidBoundary() {
        LocationTag min = new LocationTag(Math.floor(boxMin.x), Math.floor(yMin), Math.floor(boxMin.z), world.getName());
        LocationTag max = new LocationTag(Math.ceil(boxMax.x), Math.ceil(yMax), Math.ceil(boxMax.z), world.getName());
        return new CuboidTag(min, max);
    }

    @Override
    public WorldTag getWorld() {
        return world;
    }

    @Override
    public PolygonTag withWorld(WorldTag world) {
        PolygonTag toReturn = clone();
        toReturn.world = world;
        return toReturn;
    }

    public static void registerTags() {

        AbstractFlagTracker.registerFlagHandlers(tagProcessor);
        AreaContainmentObject.registerTags(PolygonTag.class, tagProcessor);

        // <--[tag]
        // @attribute <PolygonTag.max_y>
        // @returns ElementTag(Decimal)
        // @description
        // Returns the maximum Y level for this polygon.
        // -->
        tagProcessor.registerTag(ElementTag.class, "max_y", (attribute, polygon) -> {
            return new ElementTag(polygon.yMax);
        });

        // <--[tag]
        // @attribute <PolygonTag.min_y>
        // @returns ElementTag(Decimal)
        // @description
        // Returns the minimum Y level for this polygon.
        // -->
        tagProcessor.registerTag(ElementTag.class, "min_y", (attribute, polygon) -> {
            return new ElementTag(polygon.yMin);
        });

        // <--[tag]
        // @attribute <PolygonTag.note_name>
        // @returns ElementTag
        // @description
        // Gets the name of a noted PolygonTag. If the polygon isn't noted, this is null.
        // -->
        tagProcessor.registerTag(ElementTag.class, "note_name", (attribute, polygon) -> {
            String noteName = NoteManager.getSavedId(polygon);
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
        tagProcessor.registerTag(ListTag.class, "corners", (attribute, polygon) -> {
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
        tagProcessor.registerTag(PolygonTag.class, "shift", (attribute, polygon) -> {
            if (!attribute.hasParam()) {
                attribute.echoError("PolygonTag.shift[...] tag must have an input.");
                return null;
            }
            LocationTag shift = attribute.paramAsType(LocationTag.class);
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
        tagProcessor.registerTag(PolygonTag.class, "with_corner", (attribute, polygon) -> {
            if (!attribute.hasParam()) {
                attribute.echoError("PolygonTag.with_corner[...] tag must have an input.");
                return null;
            }
            LocationTag corner = attribute.paramAsType(LocationTag.class);
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
        tagProcessor.registerTag(PolygonTag.class, "with_y_min", (attribute, polygon) -> {
            if (!attribute.hasParam()) {
                attribute.echoError("PolygonTag.with_y_min[...] tag must have an input.");
                return null;
            }
            PolygonTag toReturn = polygon.clone();
            toReturn.yMin = attribute.getDoubleParam();
            return toReturn;
        });

        // <--[tag]
        // @attribute <PolygonTag.with_y_max[<#.#>]>
        // @returns PolygonTag
        // @description
        // Returns a copy of the polygon, with the specified maximum-Y value.
        // -->
        tagProcessor.registerTag(PolygonTag.class, "with_y_max", (attribute, polygon) -> {
            if (!attribute.hasParam()) {
                attribute.echoError("PolygonTag.with_y_max[...] tag must have an input.");
                return null;
            }
            PolygonTag toReturn = polygon.clone();
            toReturn.yMax = attribute.getDoubleParam();
            return toReturn;
        });

        // <--[tag]
        // @attribute <PolygonTag.include_y[<#.#>]>
        // @returns PolygonTag
        // @description
        // Returns a copy of the polygon, with the specified Y value included as part of the Y range (expanding the Y-min or Y-max as needed).
        // -->
        tagProcessor.registerTag(PolygonTag.class, "include_y", (attribute, polygon) -> {
            if (!attribute.hasParam()) {
                attribute.echoError("PolygonTag.include_y[...] tag must have an input.");
                return null;
            }
            PolygonTag toReturn = polygon.clone();
            double y = attribute.getDoubleParam();
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
        tagProcessor.registerTag(ListTag.class, "outline_2d", (attribute, polygon) -> {
            if (!attribute.hasParam()) {
                attribute.echoError("PolygonTag.outline_2d[...] tag must have an input.");
                return null;
            }
            double y = attribute.getDoubleParam();
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
        tagProcessor.registerTag(ListTag.class, "outline", (attribute, polygon) -> {
            return polygon.getOutline();
        });

        // <--[mechanism]
        // @object PolygonTag
        // @name add_corner
        // @input LocationTag
        // @description
        // Adds a corner to the end of the polygon's corner list.
        // @tags
        // <PolygonTag.with_corner[<location>]>
        // -->
        tagProcessor.registerMechanism("add_corner", false, LocationTag.class, (object, mechanism, location) -> {
            if (object.noteName != null) {
                NotedAreaTracker.remove(object);
            }
            Corner newCorner = new Corner(location.getX(), location.getZ());
            object.corners.add(newCorner);
            object.recalculateToFit(newCorner);
            if (object.noteName != null) {
                NotedAreaTracker.add(object);
            }
        });

        // <--[tag]
        // @attribute <PolygonTag.contains_inclusive[<location>]>
        // @returns ElementTag(Boolean)
        // @description
        // Returns a boolean indicating whether the specified location is inside this polygon.
        // Uses block-inclusive containment: contains a wider section of blocks along the edge (this mode is equivalent to WorldEdit's block selection).
        // -->
        tagProcessor.registerTag(ElementTag.class, LocationTag.class, "contains_inclusive", (attribute, polygon, loc) -> {
            return new ElementTag(polygon.containsInclusive(loc));
        }, "contains_location");

        // <--[tag]
        // @attribute <PolygonTag.blocks_inclusive[(<matcher>)]>
        // @returns ListTag(LocationTag)
        // @description
        // Returns each block location within the polygon.
        // Optionally, specify a material matcher to only return locations with that block type.
        // Uses block-inclusive containment: contains a wider section of blocks along the edge (this mode is equivalent to WorldEdit's block selection).
        // -->
        tagProcessor.registerTag(ListTag.class, "blocks_inclusive", (attribute, polygon) -> {
            if (attribute.hasParam()) {
                NMSHandler.chunkHelper.changeChunkServerThread(polygon.getWorld().getWorld());
                try {
                    String matcher = attribute.getParam();
                    Predicate<Location> predicate = (l) -> new LocationTag(l).tryAdvancedMatcher(matcher);
                    return polygon.getBlocksInternal(predicate, true);
                }
                finally {
                    NMSHandler.chunkHelper.restoreServerThread(polygon.getWorld().getWorld());
                }
            }
            return polygon.getBlocksInternal(null, true);
        });

        // <--[tag]
        // @attribute <PolygonTag.shell_inclusive>
        // @returns ListTag(LocationTag)
        // @description
        // Returns each block location on the 3D outer shell of the polygon.
        // Uses block-inclusive containment: contains a wider section of blocks along the edge (this mode is equivalent to WorldEdit's block selection).
        // -->
        tagProcessor.registerTag(ListTag.class, "shell_inclusive", (attribute, polygon) -> {
            return polygon.getShellInternal(true);
        });
    }

    public static ObjectTagProcessor<PolygonTag> tagProcessor = new ObjectTagProcessor<>();

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {
        return tagProcessor.getObjectAttribute(this, attribute);
    }

    public void applyProperty(Mechanism mechanism) {
        if (noteName != null) {
            mechanism.echoError("Cannot apply properties to noted objects.");
            return;
        }
        adjust(mechanism);
    }

    @Override
    public void adjust(Mechanism mechanism) {
        tagProcessor.processMechanism(this, mechanism);
    }

    @Override
    public boolean advancedMatches(String matcher) {
        String matcherLow = CoreUtilities.toLowerCase(matcher);
        if (matcherLow.equals("polygon")) {
            return true;
        }
        return areaBaseAdvancedMatches(matcher);
    }
}
