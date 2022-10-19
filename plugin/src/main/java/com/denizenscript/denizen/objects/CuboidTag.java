package com.denizenscript.denizen.objects;

import com.denizenscript.denizen.utilities.NotedAreaTracker;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.flags.AbstractFlagTracker;
import com.denizenscript.denizencore.flags.FlaggableObject;
import com.denizenscript.denizencore.flags.SavableMapFlagTracker;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizen.utilities.Settings;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.notable.Notable;
import com.denizenscript.denizencore.objects.notable.Note;
import com.denizenscript.denizencore.objects.notable.NoteManager;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import com.denizenscript.denizencore.utilities.YamlConfiguration;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class CuboidTag implements ObjectTag, Cloneable, Notable, Adjustable, AreaContainmentObject, FlaggableObject {

    // <--[ObjectType]
    // @name CuboidTag
    // @prefix cu
    // @base ElementTag
    // @implements FlaggableObject, AreaObject
    // @ExampleTagBase cuboid[my_noted_cuboid]
    // @ExampleValues my_cuboid_note
    // @ExampleForReturns
    // - note %VALUE% as:my_new_cuboid
    // @format
    // The identity format for cuboids is <world>,<x1>,<y1>,<z1>,<x2>,<y2>,<z2>
    // Multi-member cuboids can simply continue listing x,y,z pairs.
    // For example, 'cu@space,1,2,3,4,5,6'.
    //
    // @description
    // A CuboidTag represents a cuboidal region in the world.
    //
    // The word 'cuboid' means a less strict cube.
    // Basically: a "cuboid" is to a 3D "cube" what a "rectangle" is to a 2D "square".
    //
    // One 'cuboid' consists of two points: the low point and a high point.
    // a CuboidTag can contain as many cuboids within itself as needed (this allows forming more complex shapes from a single CuboidTag).
    //
    // Note that the coordinates used are inclusive, meaning that a CuboidTag always includes the blocks identified as the low and high corner points.
    // This means for example that a cuboid from "5,5,5" to "5,5,5" will contain one full block, and have a size of "1,1,1".
    //
    // This object type can be noted.
    //
    // This object type is flaggable when it is noted.
    // Flags on this object type will be stored in the notables.yml file.
    //
    // @Matchable
    // Refer to <@link objecttype areaobject>'s matchable list.
    // -->

    @Override
    public CuboidTag clone() {
        CuboidTag cuboid;
        try {
            cuboid = (CuboidTag) super.clone();
        }
        catch (CloneNotSupportedException ex) { // Should never happen.
            Debug.echoError(ex);
            cuboid = new CuboidTag();
        }
        cuboid.noteName = null;
        cuboid.priorNoteName = null;
        cuboid.flagTracker = null;
        cuboid.pairs = new ArrayList<>(pairs.size());
        for (LocationPair pair : pairs) {
            cuboid.pairs.add(new LocationPair(pair.low.clone(), pair.high.clone()));
        }
        return cuboid;
    }

    @Deprecated
    public static CuboidTag valueOf(String string) {
        return valueOf(string, null);
    }

    @Fetchable("cu")
    public static CuboidTag valueOf(String string, TagContext context) {
        if (string == null) {
            return null;
        }
        if (CoreUtilities.toLowerCase(string).startsWith("cu@")) {
            string = string.substring("cu@".length());
        }
        if (!TagManager.isStaticParsing) {
            Notable noted = NoteManager.getSavedObject(string);
            if (noted instanceof CuboidTag) {
                return (CuboidTag) noted;
            }
        }
        if (CoreUtilities.contains(string, '@')) {
            if (CoreUtilities.contains(string, '|') && string.contains("l@")) {
                Debug.echoError("Warning: likely improperly constructed CuboidTag '" + string + "' - use to_cuboid");
            }
            else {
                return null;
            }
        }
        if (CoreUtilities.contains(string, '|')) {
            ListTag positions = ListTag.valueOf(string, context);
            if (positions.size() > 1
                    && LocationTag.matches(positions.get(0))
                    && LocationTag.matches(positions.get(1))) {
                if (positions.size() % 2 != 0) {
                    if (context == null || context.showErrors()) {
                        Debug.echoError("valueOf CuboidTag returning null (Uneven number of locations): '" + string + "'.");
                    }
                    return null;
                }
                CuboidTag toReturn = new CuboidTag();
                for (int i = 0; i < positions.size(); i += 2) {
                    LocationTag pos_1 = LocationTag.valueOf(positions.get(i), context);
                    LocationTag pos_2 = LocationTag.valueOf(positions.get(i + 1), context);
                    if (pos_1 == null || pos_2 == null) {
                        if ((context == null || context.showErrors()) && !TagManager.isStaticParsing) {
                            Debug.echoError("valueOf in CuboidTag returning null (null locations): '" + string + "'.");
                        }
                        return null;
                    }
                    if (pos_1.getWorldName() == null || pos_2.getWorldName() == null) {
                        if ((context == null || context.showErrors()) && !TagManager.isStaticParsing) {
                            Debug.echoError("valueOf in CuboidTag returning null (null worlds): '" + string + "'.");
                        }
                        return null;
                    }
                    toReturn.addPair(pos_1, pos_2);
                }
                if (toReturn.pairs.size() > 0) {
                    return toReturn;
                }
            }
        }
        else if (CoreUtilities.contains(string, ',')) {
            List<String> subStrs = CoreUtilities.split(string, ',');
            if (subStrs.size() < 7 || (subStrs.size() - 1) % 6 != 0) {
                if ((context == null || context.showErrors()) && !TagManager.isStaticParsing) {
                    Debug.echoError("valueOf CuboidTag returning null (Improper number of commas): '" + string + "'.");
                }
                return null;
            }
            CuboidTag toReturn = new CuboidTag();
            String worldName = subStrs.get(0);
            if (worldName.startsWith("w@")) {
                worldName = worldName.substring("w@".length());
            }
            try {
                for (int i = 0; i < subStrs.size() - 1; i += 6) {
                    LocationTag locationOne = new LocationTag(parseRoundDouble(subStrs.get(i + 1)),
                            parseRoundDouble(subStrs.get(i + 2)), parseRoundDouble(subStrs.get(i + 3)), worldName);
                    LocationTag locationTwo = new LocationTag(parseRoundDouble(subStrs.get(i + 4)),
                            parseRoundDouble(subStrs.get(i + 5)), parseRoundDouble(subStrs.get(i + 6)), worldName);
                    toReturn.addPair(locationOne, locationTwo);
                }
            }
            catch (NumberFormatException ex) {
                if ((context == null || context.showErrors()) && !TagManager.isStaticParsing) {
                    Debug.echoError("valueOf CuboidTag returning null (Improper number value inputs): '" + ex.getMessage() + "'.");
                }
                return null;
            }
            if (toReturn.pairs.size() > 0) {
                return toReturn;
            }
        }
        if ((context == null || context.showErrors()) && !TagManager.isStaticParsing) {
            Debug.echoError("Minor: valueOf CuboidTag returning null: " + string);
        }
        return null;
    }

    public static double parseRoundDouble(String str) {
        return Math.floor(Double.parseDouble(str));
    }

    public static boolean matches(String string) {
        if (valueOf(string, CoreUtilities.noDebugContext) != null) {
            return true;
        }
        return false;
    }

    @Override
    public CuboidTag duplicate() {
        CuboidTag self = refreshState();
        if (self.noteName != null) {
            return this;
        }
        return self.clone();
    }

    @Override
    public int hashCode() {
        if (noteName != null) {
            return noteName.hashCode();
        }
        return pairs.size() + pairs.get(0).low.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof CuboidTag)) {
            return false;
        }
        CuboidTag cuboid2 = (CuboidTag) other;
        if (cuboid2.pairs.size() != pairs.size()) {
            return false;
        }
        if ((noteName == null) != (cuboid2.noteName == null)) {
            return false;
        }
        if (noteName != null) {
            return noteName.equals(cuboid2.noteName);
        }
        for (int i = 0; i < pairs.size(); i++) {
            LocationPair pair1 = pairs.get(i);
            LocationPair pair2 = cuboid2.pairs.get(i);
            if (!pair1.low.getWorldName().equals(pair2.low.getWorldName())) {
                return false;
            }
            if (pair1.low.distanceSquared(pair2.low) >= 0.5) {
                return false;
            }
            if (pair1.high.distanceSquared(pair2.high) >= 0.5) {
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
        return isInsideCuboid(loc);
    }

    @Override
    public CuboidTag getCuboidBoundary() {
        CuboidTag result = new CuboidTag(getLow(0), getHigh(0));
        for (int i = 1; i < pairs.size(); i++) {
            result = result.including(getLow(i)).including(getHigh(i));
        }
        return result;
    }

    public static class LocationPair {
        public LocationTag low;
        public LocationTag high;

        public int xDistance() {
            return high.getBlockX() - low.getBlockX();
        }

        public int yDistance() {
            return high.getBlockY() - low.getBlockY();
        }

        public int zDistance() {
            return high.getBlockZ() - low.getBlockZ();
        }

        public LocationPair(LocationTag point_1, LocationTag point_2) {
            regenerate(point_1, point_2);
        }

        public void regenerate(LocationTag point_1, LocationTag point_2) {
            String world = point_1.getWorldName();
            int x_high = Math.max(point_1.getBlockX(), point_2.getBlockX());
            int x_low = Math.min(point_1.getBlockX(), point_2.getBlockX());
            int y_high = Math.max(point_1.getBlockY(), point_2.getBlockY());
            int y_low = Math.min(point_1.getBlockY(), point_2.getBlockY());
            int z_high = Math.max(point_1.getBlockZ(), point_2.getBlockZ());
            int z_low = Math.min(point_1.getBlockZ(), point_2.getBlockZ());
            low = new LocationTag(x_low, y_low, z_low, world);
            high = new LocationTag(x_high, y_high, z_high, world);
        }
    }

    public List<LocationPair> pairs = new ArrayList<>();

    public String noteName = null, priorNoteName = null;

    public AbstractFlagTracker flagTracker = null;

    /**
     * Construct the cuboid without adding pairs
     * ONLY use this if addPair will be called immediately after!
     */
    public CuboidTag() {
    }

    public CuboidTag(Location point_1, Location point_2) {
        addPair(new LocationTag(point_1), new LocationTag(point_2));
    }

    public void addPair(LocationTag point_1, LocationTag point_2) {
        if (point_1.getWorldName() == null) {
            Debug.echoError("Tried to make cuboid without a world!");
            return;
        }
        if (!point_1.getWorldName().equals(point_2.getWorldName())) {
            Debug.echoError("Tried to make cross-world cuboid!");
            return;
        }
        if (pairs.size() > 0 && !point_1.getWorldName().equals(getWorld().getName())) {
            Debug.echoError("Tried to make cross-world cuboid set!");
            return;
        }
        LocationPair pair = new LocationPair(point_1, point_2);
        pairs.add(pair);
    }

    private static boolean isBetween(int low, int high, int pos) {
        return pos >= low && pos <= high;
    }

    public boolean isInsideCuboid(Location location) {
        if (location.getWorld() == null) {
            return false;
        }
        for (LocationPair pair : pairs) {
            if (location.getWorld().getName().equals(pair.low.getWorldName())
                && isBetween(pair.low.getBlockX(), pair.high.getBlockX(), location.getBlockX())
                && isBetween(pair.low.getBlockY(), pair.high.getBlockY(), location.getBlockY())
                && isBetween(pair.low.getBlockZ(), pair.high.getBlockZ(), location.getBlockZ())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ListTag getShell() {
        int max = Settings.blockTagsMaxBlocks();
        int index = 0;
        ListTag list = new ListTag();
        for (LocationPair pair : pairs) {
            LocationTag low = pair.low;
            LocationTag high = pair.high;
            int y_distance = pair.yDistance();
            int z_distance = pair.zDistance();
            int x_distance = pair.xDistance();
            for (int x = 0; x <= x_distance; x++) {
                for (int y = 0; y <= y_distance; y++) {
                    list.addObject(new LocationTag(low.getWorld(), low.getBlockX() + x, low.getBlockY() + y, low.getBlockZ()));
                    list.addObject(new LocationTag(low.getWorld(), low.getBlockX() + x, low.getBlockY() + y, high.getBlockZ()));
                    index++;
                    if (index > max) {
                        return list;
                    }
                }
                for (int z = 1; z < z_distance; z++) {
                    list.addObject(new LocationTag(low.getWorld(), low.getBlockX() + x, low.getBlockY(), low.getBlockZ() + z));
                    list.addObject(new LocationTag(low.getWorld(), low.getBlockX() + x, high.getBlockY(), low.getBlockZ() + z));
                    index++;
                    if (index > max) {
                        return list;
                    }
                }
            }
            for (int y = 1; y < y_distance; y++) {
                for (int z = 1; z < z_distance; z++) {
                    list.addObject(new LocationTag(low.getWorld(), low.getBlockX(), low.getBlockY() + y, low.getBlockZ() + z));
                    list.addObject(new LocationTag(low.getWorld(), high.getBlockX(), low.getBlockY() + y, low.getBlockZ() + z));
                    index++;
                    if (index > max) {
                        return list;
                    }
                }
            }
        }

        return list;
    }

    public ListTag getOutline2D(double y) {
        int max = Settings.blockTagsMaxBlocks();
        int index = 0;
        ListTag list = new ListTag();
        for (LocationPair pair : pairs) {
            LocationTag loc_1 = pair.low;
            LocationTag loc_2 = pair.high;
            int z_distance = pair.zDistance();
            int x_distance = pair.xDistance();
            list.addObject(new LocationTag(loc_2.getWorld(), loc_2.getBlockX(), y, loc_2.getBlockZ()));
            for (int x = loc_1.getBlockX(); x < loc_1.getBlockX() + x_distance; x++) {
                list.addObject(new LocationTag(loc_1.getWorld(), x, y, loc_2.getBlockZ()));
                list.addObject(new LocationTag(loc_1.getWorld(), x, y, loc_1.getBlockZ()));
                index++;
                if (index > max) {
                    return list;
                }
            }
            for (int z = loc_1.getBlockZ(); z < loc_1.getBlockZ() + z_distance; z++) {
                list.addObject(new LocationTag(loc_1.getWorld(), loc_2.getBlockX(), y, z));
                list.addObject(new LocationTag(loc_1.getWorld(), loc_1.getBlockX(), y, z));
                index++;
                if (index > max) {
                    return list;
                }
            }
        }
        return list;
    }

    public ListTag getOutline() {
        int max = Settings.blockTagsMaxBlocks();
        int index = 0;
        ListTag list = new ListTag();
        for (LocationPair pair : pairs) {
            LocationTag loc_1 = pair.low;
            LocationTag loc_2 = pair.high;
            int y_distance = pair.yDistance();
            int z_distance = pair.zDistance();
            int x_distance = pair.xDistance();
            for (int y = loc_1.getBlockY(); y < loc_1.getBlockY() + y_distance; y++) {
                list.addObject(new LocationTag(loc_1.getWorld(), loc_1.getBlockX(), y, loc_1.getBlockZ()));
                list.addObject(new LocationTag(loc_1.getWorld(), loc_2.getBlockX(), y, loc_2.getBlockZ()));
                list.addObject(new LocationTag(loc_1.getWorld(), loc_1.getBlockX(), y, loc_2.getBlockZ()));
                list.addObject(new LocationTag(loc_1.getWorld(), loc_2.getBlockX(), y, loc_1.getBlockZ()));
                index++;
                if (index > max) {
                    return list;
                }
            }
            for (int x = loc_1.getBlockX(); x < loc_1.getBlockX() + x_distance; x++) {
                list.addObject(new LocationTag(loc_1.getWorld(), x, loc_1.getBlockY(), loc_1.getBlockZ()));
                list.addObject(new LocationTag(loc_1.getWorld(), x, loc_1.getBlockY(), loc_2.getBlockZ()));
                list.addObject(new LocationTag(loc_1.getWorld(), x, loc_2.getBlockY(), loc_2.getBlockZ()));
                list.addObject(new LocationTag(loc_1.getWorld(), x, loc_2.getBlockY(), loc_1.getBlockZ()));
                index++;
                if (index > max) {
                    return list;
                }
            }
            for (int z = loc_1.getBlockZ(); z < loc_1.getBlockZ() + z_distance; z++) {
                list.addObject(new LocationTag(loc_1.getWorld(), loc_1.getBlockX(), loc_1.getBlockY(), z));
                list.addObject(new LocationTag(loc_1.getWorld(), loc_2.getBlockX(), loc_2.getBlockY(), z));
                list.addObject(new LocationTag(loc_1.getWorld(), loc_1.getBlockX(), loc_2.getBlockY(), z));
                list.addObject(new LocationTag(loc_1.getWorld(), loc_2.getBlockX(), loc_1.getBlockY(), z));
                index++;
                if (index > max) {
                    return list;
                }
            }
            list.addObject(pair.high);
        }
        return list;
    }

    @Override
    public ListTag getBlocks(Predicate<Location> test) {
        List<LocationTag> locs = getBlocks_internal(test);
        ListTag list = new ListTag();
        for (LocationTag loc : locs) {
            list.addObject(loc);
        }
        return list;
    }

    public List<LocationTag> getBlocks_internal(Predicate<Location> test) {
        if (test == null) {
            return getBlockLocationsUnfiltered(true);
        }
        int yMin = getWorld().getWorld().getMinHeight(), yMax = getWorld().getWorld().getMaxHeight();
        int max = Settings.blockTagsMaxBlocks();
        LocationTag loc;
        List<LocationTag> list = new ArrayList<>();
        int index = 0;
        for (LocationPair pair : pairs) {
            LocationTag loc_1 = pair.low;
            int y_distance = pair.yDistance();
            int z_distance = pair.zDistance();
            int x_distance = pair.xDistance();
            for (int x = 0; x != x_distance + 1; x++) {
                for (int y = 0; y != y_distance + 1; y++) {
                    if (loc_1.getY() + y < yMin || loc_1.getY() + y > yMax) {
                        continue;
                    }
                    for (int z = 0; z != z_distance + 1; z++) {
                        loc = new LocationTag(loc_1.clone().add(x, y, z));
                        if (index++ > max) {
                            return list;
                        }
                        if (test.test(loc)) {
                            list.add(loc);
                        }
                    }
                }
            }
        }
        return list;
    }

    public List<LocationTag> getBlockLocationsUnfiltered(boolean doMax) {
        int max = doMax ? Settings.blockTagsMaxBlocks() : Integer.MAX_VALUE;
        List<LocationTag> list = new ArrayList<>();
        int index = 0;
        for (LocationPair pair : pairs) {
            LocationTag loc_1 = pair.low;
            int y_distance = pair.yDistance();
            int z_distance = pair.zDistance();
            int x_distance = pair.xDistance();
            for (int x = 0; x <= x_distance; x++) {
                for (int y = 0; y <= y_distance; y++) {
                    for (int z = 0; z <= z_distance; z++) {
                        LocationTag loc = new LocationTag(loc_1.clone().add(x, y, z));
                        list.add(loc);
                        if (index++ > max) {
                            return list;
                        }
                    }
                }
            }
        }
        return list;
    }

    public final Collection<Entity> getEntitiesPossiblyWithin() {
        WorldTag world = getWorld();
        if (pairs.size() != 1) {
            return world.getEntities();
        }
        BoundingBox box = BoundingBox.of(getLow(0).toVector(), getHigh(0).toVector().add(new Vector(1, 1, 1)));
        return world.getPossibleEntitiesForBoundary(box);
    }

    public Collection<Entity> getEntitiesPossiblyWithinForTag() {
        WorldTag world = getWorld();
        if (pairs.size() != 1) {
            return world.getEntitiesForTag();
        }
        BoundingBox box = BoundingBox.of(getLow(0).toVector(), getHigh(0).toVector().add(new Vector(1, 1, 1)));
        return world.getPossibleEntitiesForBoundaryForTag(box);
    }

    @Override
    public WorldTag getWorld() {
        if (pairs.isEmpty()) {
            return null;
        }
        return new WorldTag(pairs.get(0).high.getWorldName());
    }

    public LocationTag getHigh(int index) {
        if (index < 0) {
            return null;
        }
        if (index >= pairs.size()) {
            return null;
        }
        return pairs.get(index).high;
    }

    public LocationTag getLow(int index) {
        if (index < 0) {
            return null;
        }
        if (index >= pairs.size()) {
            return null;
        }
        return pairs.get(index).low;
    }

    @Override
    public boolean isUnique() {
        return noteName != null;
    }

    @Override
    @Note("Cuboids")
    public Object getSaveObject() {
        YamlConfiguration section = new YamlConfiguration();
        section.set("object", identifyFull());
        section.set("flags", flagTracker.toString());
        return section;
    }

    @Override
    public void makeUnique(String id) {
        CuboidTag toNote = clone();
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
    public CuboidTag refreshState() {
        if (noteName == null && priorNoteName != null) {
            Notable note = NoteManager.getSavedObject(priorNoteName);
            if (note instanceof CuboidTag) {
                return (CuboidTag) note;
            }
            priorNoteName = null;
        }
        return this;
    }

    String prefix = "Cuboid";

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public CuboidTag setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    @Override
    public String debuggable() {
        CuboidTag self = refreshState();
        if (self.isUnique()) {
            return "<LG>cu@<Y>" + self.noteName + " <GR>(" + self.identifyFull() + ")";
        }
        else {
            return self.identifyFull();
        }
    }

    @Override
    public String identify() {
        CuboidTag self = refreshState();
        if (self.isUnique()) {
            return "cu@" + self.noteName;
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
        sb.append("cu@").append(pairs.get(0).low.getWorldName());
        for (LocationPair pair : pairs) {
            sb.append(',').append(pair.low.getBlockX()).append(',').append(pair.low.getBlockY()).append(',').append(pair.low.getBlockZ())
                    .append(',').append(pair.high.getBlockX()).append(',').append(pair.high.getBlockY()).append(',').append(pair.high.getBlockZ());
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
    public CuboidTag withWorld(WorldTag world) {
        CuboidTag newCuboid = clone();
        for (LocationPair pair : newCuboid.pairs) {
            pair.low.setWorld(world.getWorld());
            pair.high.setWorld(world.getWorld());
        }
        return newCuboid;
    }

    public static void registerTags() {

        AbstractFlagTracker.registerFlagHandlers(tagProcessor);
        AreaContainmentObject.registerTags(CuboidTag.class, tagProcessor);

        // <--[tag]
        // @attribute <CuboidTag.random>
        // @returns LocationTag
        // @description
        // Returns a random block location within the cuboid.
        // (Note: random selection will not be fairly weighted for multi-member cuboids).
        // -->
        tagProcessor.registerTag(LocationTag.class, "random", (attribute, cuboid) -> {
            LocationPair pair = cuboid.pairs.get(CoreUtilities.getRandom().nextInt(cuboid.pairs.size()));
            Vector range = pair.high.toVector().subtract(pair.low.toVector()).add(new Vector(1, 1, 1));
            range.setX(CoreUtilities.getRandom().nextInt(range.getBlockX()));
            range.setY(CoreUtilities.getRandom().nextInt(range.getBlockY()));
            range.setZ(CoreUtilities.getRandom().nextInt(range.getBlockZ()));
            LocationTag out = pair.low.clone();
            out.add(range);
            return out;
        });

        // <--[tag]
        // @attribute <CuboidTag.members_size>
        // @returns ElementTag(Number)
        // @description
        // Returns the number of cuboids defined in the CuboidTag.
        // -->
        tagProcessor.registerTag(ElementTag.class, "members_size", (attribute, cuboid) -> {
            return new ElementTag(cuboid.pairs.size());
        });

        // <--[tag]
        // @attribute <CuboidTag.outline>
        // @returns ListTag(LocationTag)
        // @description
        // Returns each block location on the outline of the CuboidTag.
        // -->
        tagProcessor.registerTag(ListTag.class, "outline", (attribute, cuboid) -> {
            return cuboid.getOutline();
        }, "get_outline");

        // <--[tag]
        // @attribute <CuboidTag.outline_2d[<#.#>]>
        // @returns ListTag(LocationTag)
        // @description
        // Returns a list of block locations along the 2D outline of this CuboidTag, at the specified Y level.
        // -->
        tagProcessor.registerTag(ListTag.class, "outline_2d", (attribute, cuboid) -> {
            if (!attribute.hasParam()) {
                attribute.echoError("CuboidTag.outline_2d[...] tag must have an input.");
                return null;
            }
            double y = attribute.getDoubleParam();
            return cuboid.getOutline2D(y);
        });

        // <--[tag]
        // @attribute <CuboidTag.intersects[<cuboid>]>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether this cuboid and another intersect.
        // -->
        tagProcessor.registerTag(ElementTag.class, "intersects", (attribute, cuboid) -> {
            if (!attribute.hasParam()) {
                attribute.echoError("The tag CuboidTag.intersects[...] must have a value.");
                return null;
            }
            CuboidTag cub2 = attribute.paramAsType(CuboidTag.class);
            if (cub2 != null) {
                boolean intersects = false;
                whole_loop:
                for (LocationPair pair : cuboid.pairs) {
                    for (LocationPair pair2 : cub2.pairs) {
                        if (!pair.low.getWorld().getName().equalsIgnoreCase(pair2.low.getWorld().getName())) {
                            return new ElementTag("false");
                        }
                        if (pair2.low.getX() <= pair.high.getX()
                                && pair2.low.getY() <= pair.high.getY()
                                && pair2.low.getZ() <= pair.high.getZ()
                                && pair2.high.getX() >= pair.low.getX()
                                && pair2.high.getY() >= pair.low.getY()
                                && pair2.high.getZ() >= pair.low.getZ()) {
                            intersects = true;
                            break whole_loop;
                        }
                    }
                }
                return new ElementTag(intersects);
            }
            return null;
        });

        // <--[tag]
        // @attribute <CuboidTag.list_members>
        // @returns ListTag(CuboidTag)
        // @description
        // Returns a list of all sub-cuboids in this CuboidTag (for cuboids that contain multiple sub-cuboids).
        // -->
        tagProcessor.registerTag(ListTag.class, "list_members", (attribute, cuboid) -> {
            List<LocationPair> pairs = cuboid.pairs;
            ListTag list = new ListTag();
            for (LocationPair pair : pairs) {
                list.addObject(new CuboidTag(pair.low.clone(), pair.high.clone()));
            }
            return list;
        });

        // <--[tag]
        // @attribute <CuboidTag.get[<index>]>
        // @returns CuboidTag
        // @description
        // Returns a cuboid representing the one component of this cuboid (for cuboids that contain multiple sub-cuboids).
        // -->
        tagProcessor.registerTag(CuboidTag.class, "get", (attribute, cuboid) -> {
            if (!attribute.hasParam()) {
                attribute.echoError("The tag CuboidTag.get[...] must have a value.");
                return null;
            }
            else {
                int member = attribute.getIntParam();
                if (member < 1) {
                    member = 1;
                }
                if (member > cuboid.pairs.size()) {
                    member = cuboid.pairs.size();
                }
                LocationPair pair = cuboid.pairs.get(member - 1);
                return new CuboidTag(pair.low.clone(), pair.high.clone());
            }
        }, "member", "get_member");

        // <--[tag]
        // @attribute <CuboidTag.set[<cuboid>].at[<index>]>
        // @returns CuboidTag
        // @mechanism CuboidTag.set_member
        // @description
        // Returns a modified copy of this cuboid, with the specific sub-cuboid index changed to hold the input cuboid.
        // -->
        tagProcessor.registerTag(CuboidTag.class, "set", (attribute, cuboid) -> {
            if (!attribute.hasParam()) {
                attribute.echoError("The tag CuboidTag.set[...] must have a value.");
                return null;
            }
            else {
                CuboidTag subCuboid = attribute.paramAsType(CuboidTag.class);
                if (!attribute.startsWith("at", 2)) {
                    attribute.echoError("The tag CuboidTag.set[...] must be followed by an 'at'.");
                    return null;
                }
                if (!attribute.hasContext(2)) {
                    attribute.echoError("The tag CuboidTag.set[...].at[...] must have an 'at' value.");
                    return null;
                }
                int member = attribute.getIntContext(2);
                if (member < 1) {
                    member = 1;
                }
                if (member > cuboid.pairs.size()) {
                    member = cuboid.pairs.size();
                }
                attribute.fulfill(1);
                LocationPair pair = subCuboid.pairs.get(0);
                CuboidTag cloned = cuboid.clone();
                cloned.pairs.set(member - 1, new LocationPair(pair.low.clone(), pair.high.clone()));
                return cloned;
            }
        });

        // <--[tag]
        // @attribute <CuboidTag.add_member[<cuboid>|...]>
        // @returns CuboidTag
        // @mechanism CuboidTag.add_member
        // @description
        // Returns a modified copy of this cuboid, with the input cuboid(s) added at the end.
        // -->
        tagProcessor.registerTag(CuboidTag.class, "add_member", (attribute, cuboid) -> {
            if (!attribute.hasParam()) {
                attribute.echoError("The tag CuboidTag.add_member[...] must have a value.");
                return null;
            }
            cuboid = cuboid.clone();
            int member = cuboid.pairs.size() + 1;
            ObjectTag param = attribute.getParamObject();

            // <--[tag]
            // @attribute <CuboidTag.add_member[<cuboid>|...].at[<index>]>
            // @returns CuboidTag
            // @mechanism CuboidTag.add_member
            // @description
            // Returns a modified copy of this cuboid, with the input cuboid(s) added at the specified index.
            // -->
            if (attribute.startsWith("at", 2)) {
                if (!attribute.hasContext(2)) {
                    attribute.echoError("The tag CuboidTag.add_member[...].at[...] must have an 'at' value.");
                    return null;
                }
                member = attribute.getIntContext(2);
                attribute.fulfill(1);
            }
            if (member < 1) {
                member = 1;
            }
            if (member > cuboid.pairs.size() + 1) {
                member = cuboid.pairs.size() + 1;
            }
            if (!(param instanceof CuboidTag) && param.toString().startsWith("li@")) { // Old cuboid identity used '|' symbol, so require 'li@' to be a list
                for (CuboidTag subCuboid : param.asType(ListTag.class, attribute.context).filter(CuboidTag.class, attribute.context)) {
                    LocationPair pair = subCuboid.pairs.get(0);
                    cuboid.pairs.add(member - 1, new LocationPair(pair.low.clone(), pair.high.clone()));
                    member++;
                }
            }
            else {
                CuboidTag subCuboid = param.asType(CuboidTag.class, attribute.context);
                LocationPair pair = subCuboid.pairs.get(0);
                cuboid.pairs.add(member - 1, new LocationPair(pair.low.clone(), pair.high.clone()));
            }
            return cuboid;
        });

        // <--[tag]
        // @attribute <CuboidTag.remove_member[<#>]>
        // @returns CuboidTag
        // @mechanism CuboidTag.remove_member
        // @description
        // Returns a modified copy of this cuboid, with member at the input index removed.
        // -->
        tagProcessor.registerTag(CuboidTag.class, "remove_member", (attribute, cuboid) -> {
            if (!attribute.hasParam()) {
                attribute.echoError("The tag CuboidTag.remove_member[...] must have a value.");
                return null;
            }
            cuboid = cuboid.clone();
            int member = attribute.getIntParam();
            if (member < 1) {
                member = 1;
            }
            if (member > cuboid.pairs.size() + 1) {
                member = cuboid.pairs.size() + 1;
            }
            cuboid.pairs.remove(member - 1);
            if (cuboid.pairs.isEmpty()) {
                return null;
            }
            return cuboid;
        });

        // <--[tag]
        // @attribute <CuboidTag.center>
        // @returns LocationTag
        // @description
        // Returns the location of the exact center of the cuboid.
        // Not valid for multi-member CuboidTags.
        // -->
        tagProcessor.registerTag(LocationTag.class, "center", (attribute, cuboid) -> {
            LocationPair pair;
            if (!attribute.hasParam()) {
                pair = cuboid.pairs.get(0);
            }
            else { // legacy
                int member = attribute.getIntParam();
                if (member < 1) {
                    member = 1;
                }
                if (member > cuboid.pairs.size()) {
                    member = cuboid.pairs.size();
                }
                pair = cuboid.pairs.get(member - 1);
            }
            LocationTag base = pair.high.clone().add(pair.low).add(1.0, 1.0, 1.0);
            base.setX(base.getX() / 2.0);
            base.setY(base.getY() / 2.0);
            base.setZ(base.getZ() / 2.0);
            return base;
        });

        // <--[tag]
        // @attribute <CuboidTag.volume>
        // @returns ElementTag(Number)
        // @description
        // Returns the volume of the cuboid.
        // Effectively equivalent to: (size.x * size.y * size.z).
        // Not valid for multi-member CuboidTags.
        // -->
        tagProcessor.registerTag(ElementTag.class, "volume", (attribute, cuboid) -> {
            LocationPair pair = cuboid.pairs.get(0);
            Location base = pair.high.clone().subtract(pair.low.clone()).add(1, 1, 1);
            return new ElementTag(base.getX() * base.getY() * base.getZ());
        });

        // <--[tag]
        // @attribute <CuboidTag.size>
        // @returns LocationTag
        // @description
        // Returns the size of the cuboid.
        // Effectively equivalent to: (max - min) + (1,1,1)
        // Not valid for multi-member CuboidTags.
        // -->
        tagProcessor.registerTag(LocationTag.class, "size", (attribute, cuboid) -> {
            LocationPair pair;
            if (!attribute.hasParam()) {
                pair = cuboid.pairs.get(0);
            }
            else { // legacy
                int member = attribute.getIntParam();
                if (member < 1) {
                    member = 1;
                }
                if (member > cuboid.pairs.size()) {
                    member = cuboid.pairs.size();
                }
                pair = cuboid.pairs.get(member - 1);
            }
            Location base = pair.high.clone().subtract(pair.low.clone()).add(1, 1, 1);
            return new LocationTag(base);
        });

        // <--[tag]
        // @attribute <CuboidTag.max>
        // @returns LocationTag
        // @description
        // Returns the highest-numbered (maximum) corner location.
        // Not valid for multi-member CuboidTags.
        // -->
        tagProcessor.registerTag(LocationTag.class, "max", (attribute, cuboid) -> {
            if (!attribute.hasParam()) {
                return cuboid.pairs.get(0).high;
            }
            else { // legacy
                int member = attribute.getIntParam();
                if (member < 1) {
                    member = 1;
                }
                if (member > cuboid.pairs.size()) {
                    member = cuboid.pairs.size();
                }
                return cuboid.pairs.get(member - 1).high;
            }
        });

        // <--[tag]
        // @attribute <CuboidTag.min>
        // @returns LocationTag
        // @description
        // Returns the lowest-numbered (minimum) corner location.
        // Not valid for multi-member CuboidTags.
        // -->
        tagProcessor.registerTag(LocationTag.class, "min", (attribute, cuboid) -> {
            if (!attribute.hasParam()) {
                return cuboid.pairs.get(0).low;
            }
            else { // legacy
                int member = attribute.getIntParam();
                if (member < 1) {
                    member = 1;
                }
                if (member > cuboid.pairs.size()) {
                    member = cuboid.pairs.size();
                }
                return cuboid.pairs.get(member - 1).low;
            }
        });

        // <--[tag]
        // @attribute <CuboidTag.corners>
        // @returns ListTag(LocationTag)
        // @description
        // Returns all 8 corners of the cuboid.
        // The 4 low corners, then the 4 high corners.
        // In order X-Z-, X+Z-, X-Z+, X+Z+
        // If the object is a multi-member cuboid, returns corners for all members in sequence.
        // -->
        tagProcessor.registerTag(ListTag.class, "corners", (attribute, cuboid) -> {
            ListTag output = new ListTag();
            for (LocationPair pair : cuboid.pairs) {
                output.addObject(new LocationTag(pair.low.getX(), pair.low.getY(), pair.low.getZ(), pair.low.getWorldName()));
                output.addObject(new LocationTag(pair.high.getX(), pair.low.getY(), pair.low.getZ(), pair.low.getWorldName()));
                output.addObject(new LocationTag(pair.low.getX(), pair.low.getY(), pair.high.getZ(), pair.low.getWorldName()));
                output.addObject(new LocationTag(pair.high.getX(), pair.low.getY(), pair.high.getZ(), pair.low.getWorldName()));
                output.addObject(new LocationTag(pair.low.getX(), pair.high.getY(), pair.low.getZ(), pair.low.getWorldName()));
                output.addObject(new LocationTag(pair.high.getX(), pair.high.getY(), pair.low.getZ(), pair.low.getWorldName()));
                output.addObject(new LocationTag(pair.low.getX(), pair.high.getY(), pair.high.getZ(), pair.low.getWorldName()));
                output.addObject(new LocationTag(pair.high.getX(), pair.high.getY(), pair.high.getZ(), pair.low.getWorldName()));
            }
            return output;
        });

        // <--[tag]
        // @attribute <CuboidTag.shift[<vector>]>
        // @returns CuboidTag
        // @description
        // Returns a copy of this cuboid, with all members shifted by the given vector LocationTag.
        // For example, a cuboid from 5,5,5 to 10,10,10, shifted 100,0,100, would return a cuboid from 105,5,105 to 110,10,110.
        // -->
        tagProcessor.registerTag(CuboidTag.class, "shift", (attribute, cuboid) -> {
            if (!attribute.hasParam()) {
                attribute.echoError("The tag CuboidTag.shift[...] must have a value.");
                return null;
            }
            LocationTag vector = attribute.paramAsType(LocationTag.class);
            if (vector != null) {
                return cuboid.shifted(vector);
            }
            return null;
        });

        // <--[tag]
        // @attribute <CuboidTag.include[<location>/<cuboid>]>
        // @returns CuboidTag
        // @description
        // Expands the first member of the CuboidTag to contain the given location (or entire cuboid), and returns the expanded cuboid.
        // -->
        tagProcessor.registerTag(CuboidTag.class, "include", (attribute, cuboid) -> {
            if (!attribute.hasParam()) {
                attribute.echoError("The tag CuboidTag.include[...] must have a value.");
                return null;
            }
            CuboidTag newCuboid = CuboidTag.valueOf(attribute.getParam(), CoreUtilities.noDebugContext);
            if (newCuboid != null) {
                return cuboid.including(newCuboid.getLow(0)).including(newCuboid.getHigh(0));
            }
            LocationTag loc = attribute.paramAsType(LocationTag.class);
            if (loc != null) {
                return cuboid.including(loc);
            }
            return null;
        });

        // <--[tag]
        // @attribute <CuboidTag.include_x[<number>]>
        // @returns CuboidTag
        // @description
        // Expands the first member of the CuboidTag to contain the given X value, and returns the expanded cuboid.
        // -->
        tagProcessor.registerTag(CuboidTag.class, "include_x", (attribute, cuboid) -> {
            cuboid = cuboid.clone();
            if (!attribute.hasParam()) {
                attribute.echoError("The tag CuboidTag.include_x[...] must have a value.");
                return null;
            }
            double x = attribute.getDoubleParam();
            if (x < cuboid.pairs.get(0).low.getX()) {
                cuboid.pairs.get(0).low = new LocationTag(cuboid.pairs.get(0).low.getWorld(), x, cuboid.pairs.get(0).low.getY(), cuboid.pairs.get(0).low.getZ());
            }
            if (x > cuboid.pairs.get(0).high.getX()) {
                cuboid.pairs.get(0).high = new LocationTag(cuboid.pairs.get(0).high.getWorld(), x, cuboid.pairs.get(0).high.getY(), cuboid.pairs.get(0).high.getZ());
            }
            return cuboid;
        });

        // <--[tag]
        // @attribute <CuboidTag.include_y[<number>]>
        // @returns CuboidTag
        // @description
        // Expands the first member of the CuboidTag to contain the given Y value, and returns the expanded cuboid.
        // -->
        tagProcessor.registerTag(CuboidTag.class, "include_y", (attribute, cuboid) -> {
            cuboid = cuboid.clone();
            if (!attribute.hasParam()) {
                attribute.echoError("The tag CuboidTag.include_y[...] must have a value.");
                return null;
            }
            double y = attribute.getDoubleParam();
            if (y < cuboid.pairs.get(0).low.getY()) {
                cuboid.pairs.get(0).low = new LocationTag(cuboid.pairs.get(0).low.getWorld(), cuboid.pairs.get(0).low.getX(), y, cuboid.pairs.get(0).low.getZ());
            }
            if (y > cuboid.pairs.get(0).high.getY()) {
                cuboid.pairs.get(0).high = new LocationTag(cuboid.pairs.get(0).high.getWorld(), cuboid.pairs.get(0).high.getX(), y, cuboid.pairs.get(0).high.getZ());
            }
            return cuboid;
        });

        // <--[tag]
        // @attribute <CuboidTag.include_z[<number>]>
        // @returns CuboidTag
        // @description
        // Expands the first member of the CuboidTag to contain the given Z value, and returns the expanded cuboid.
        // -->
        tagProcessor.registerTag(CuboidTag.class, "include_z", (attribute, cuboid) -> {
            cuboid = cuboid.clone();
            if (!attribute.hasParam()) {
                attribute.echoError("The tag CuboidTag.include_z[...] must have a value.");
                return null;
            }
            double z = attribute.getDoubleParam();
            if (z < cuboid.pairs.get(0).low.getZ()) {
                cuboid.pairs.get(0).low = new LocationTag(cuboid.pairs.get(0).low.getWorld(), cuboid.pairs.get(0).low.getX(), cuboid.pairs.get(0).low.getY(), z);
            }
            if (z > cuboid.pairs.get(0).high.getZ()) {
                cuboid.pairs.get(0).high = new LocationTag(cuboid.pairs.get(0).high.getWorld(), cuboid.pairs.get(0).high.getX(), cuboid.pairs.get(0).high.getY(), z);
            }
            return cuboid;
        });

        // <--[tag]
        // @attribute <CuboidTag.with_min[<location>]>
        // @returns CuboidTag
        // @description
        // Changes the cuboid to have the given minimum location, and returns the changed cuboid.
        // If values in the new min are higher than the existing max, the output max will contain the new min values,
        // and the output min will contain the old max values.
        // Note that this is equivalent to constructing a cuboid with the input value and the original cuboids max value.
        // Not valid for multi-member CuboidTags.
        // -->
        tagProcessor.registerTag(CuboidTag.class, "with_min", (attribute, cuboid) -> {
            if (!attribute.hasParam()) {
                attribute.echoError("The tag CuboidTag.with_min[...] must have a value.");
                return null;
            }
            LocationTag location = attribute.paramAsType(LocationTag.class);
            return new CuboidTag(location, cuboid.pairs.get(0).high);
        });

        // <--[tag]
        // @attribute <CuboidTag.with_max[<location>]>
        // @returns CuboidTag
        // @description
        // Changes the cuboid to have the given maximum location, and returns the changed cuboid.
        // If values in the new max are lower than the existing min, the output min will contain the new max values,
        // and the output max will contain the old min values.
        // Note that this is equivalent to constructing a cuboid with the input value and the original cuboids min value.
        // Not valid for multi-member CuboidTags.
        // -->
        tagProcessor.registerTag(CuboidTag.class, "with_max", (attribute, cuboid) -> {
            if (!attribute.hasParam()) {
                attribute.echoError("The tag CuboidTag.with_max[...] must have a value.");
                return null;
            }
            LocationTag location = attribute.paramAsType(LocationTag.class);
            return new CuboidTag(location, cuboid.pairs.get(0).low);
        });

        // <--[tag]
        // @attribute <CuboidTag.expand[<location>]>
        // @returns CuboidTag
        // @description
        // Expands the cuboid by the given amount, and returns the changed cuboid.
        // This will decrease the min coordinates by the given vector location, and increase the max coordinates by it.
        // Supplying a negative input will therefore contract the cuboid.
        // Note that you can also specify a single number to expand all coordinates by the same amount (equivalent to specifying a location that is that value on X, Y, and Z).
        // Not valid for multi-member CuboidTags.
        // -->
        tagProcessor.registerTag(CuboidTag.class, "expand", (attribute, cuboid) -> {
            if (!attribute.hasParam()) {
                attribute.echoError("The tag CuboidTag.expand[...] must have a value.");
                return null;
            }
            Vector expandBy;
            if (ArgumentHelper.matchesInteger(attribute.getParam())) {
                int val = attribute.getIntParam();
                expandBy = new Vector(val, val, val);
            }
            else {
                expandBy = attribute.paramAsType(LocationTag.class).toVector();
            }
            LocationPair pair = cuboid.pairs.get(0);
            return new CuboidTag(pair.low.clone().subtract(expandBy), pair.high.clone().add(expandBy));
        });

        // <--[tag]
        // @attribute <CuboidTag.expand_one_side[<location>]>
        // @returns CuboidTag
        // @description
        // Expands the cuboid by the given amount in just one direction, and returns the changed cuboid.
        // If a coordinate is positive, it will expand the high value. If it is negative, it will expand the low value.
        // Note that you can also specify a single number to expand all coordinates by the same amount (equivalent to specifying a location that is that value on X, Y, and Z).
        // Inverted by <@link tag CuboidTag.shrink_one_side>
        // Not valid for multi-member CuboidTags.
        // -->
        tagProcessor.registerTag(CuboidTag.class, "expand_one_side", (attribute, cuboid) -> {
            if (!attribute.hasParam()) {
                attribute.echoError("The tag CuboidTag.expand_one_side[...] must have a value.");
                return null;
            }
            Vector expandBy;
            if (ArgumentHelper.matchesInteger(attribute.getParam())) {
                int val = attribute.getIntParam();
                expandBy = new Vector(val, val, val);
            }
            else {
                expandBy = attribute.paramAsType(LocationTag.class).toVector();
            }
            LocationPair pair = cuboid.pairs.get(0);
            LocationTag low = pair.low.clone();
            LocationTag high = pair.high.clone();
            if (expandBy.getBlockX() < 0) {
                low.setX(low.getBlockX() + expandBy.getBlockX());
            }
            else {
                high.setX(high.getBlockX() + expandBy.getBlockX());
            }
            if (expandBy.getBlockY() < 0) {
                low.setY(low.getBlockY() + expandBy.getBlockY());
            }
            else {
                high.setY(high.getBlockY() + expandBy.getBlockY());
            }
            if (expandBy.getBlockZ() < 0) {
                low.setZ(low.getBlockZ() + expandBy.getBlockZ());
            }
            else {
                high.setZ(high.getBlockZ() + expandBy.getBlockZ());
            }
            return new CuboidTag(low, high);
        });

        // <--[tag]
        // @attribute <CuboidTag.shrink_one_side[<location>]>
        // @returns CuboidTag
        // @description
        // Shrinks the cuboid by the given amount in just one direction, and returns the changed cuboid.
        // If a coordinate is positive, it will shrink the high value. If it is negative, it will shrink the low value.
        // Note that you can also specify a single number to expand all coordinates by the same amount (equivalent to specifying a location that is that value on X, Y, and Z).
        // Inverted by <@link tag CuboidTag.expand_one_side>
        // Not valid for multi-member CuboidTags.
        // If you shrink past the limits of the cuboid's size, the cuboid will flip and start expanding the opposite direction.
        // -->
        tagProcessor.registerTag(CuboidTag.class, "shrink_one_side", (attribute, cuboid) -> {
            if (!attribute.hasParam()) {
                attribute.echoError("The tag CuboidTag.shrink_one_side[...] must have a value.");
                return null;
            }
            Vector expandBy;
            if (ArgumentHelper.matchesInteger(attribute.getParam())) {
                int val = attribute.getIntParam();
                expandBy = new Vector(val, val, val);
            }
            else {
                expandBy = attribute.paramAsType(LocationTag.class).toVector();
            }
            LocationPair pair = cuboid.pairs.get(0);
            LocationTag low = pair.low.clone();
            LocationTag high = pair.high.clone();
            if (expandBy.getBlockX() < 0) {
                low.setX(low.getBlockX() - expandBy.getBlockX());
            }
            else {
                high.setX(high.getBlockX() - expandBy.getBlockX());
            }
            if (expandBy.getBlockY() < 0) {
                low.setY(low.getBlockY() - expandBy.getBlockY());
            }
            else {
                high.setY(high.getBlockY() - expandBy.getBlockY());
            }
            if (expandBy.getBlockZ() < 0) {
                low.setZ(low.getBlockZ() - expandBy.getBlockZ());
            }
            else {
                high.setZ(high.getBlockZ() - expandBy.getBlockZ());
            }
            return new CuboidTag(low, high);
        });

        // <--[tag]
        // @attribute <CuboidTag.chunks>
        // @returns ListTag(ChunkTag)
        // @description
        // Gets a list of all chunks entirely within the CuboidTag (ignoring the Y axis).
        // -->
        tagProcessor.registerTag(ListTag.class, "chunks", (attribute, cuboid) -> {
            ListTag chunks = new ListTag();
            for (LocationPair pair : cuboid.pairs) {
                int minY = pair.low.getBlockY();
                ChunkTag minChunk = new ChunkTag(pair.low);
                int minX = minChunk.getX();
                int minZ = minChunk.getZ();
                if (!cuboid.isInsideCuboid(new Location(cuboid.getWorld().getWorld(), minChunk.getX() * 16, minY, minChunk.getZ() * 16))) {
                    minX++;
                    minZ++;
                }
                ChunkTag maxChunk = new ChunkTag(pair.high);
                int maxX = maxChunk.getX();
                int maxZ = maxChunk.getZ();
                if (cuboid.isInsideCuboid(new Location(cuboid.getWorld().getWorld(), maxChunk.getX() * 16 + 15, minY, maxChunk.getZ() * 16 + 15))) {
                    maxX++;
                    maxZ++;
                }
                for (int x = minX; x < maxX; x++) {
                    for (int z = minZ; z < maxZ; z++) {
                        chunks.addObject(new ChunkTag(cuboid.getWorld(), x, z));
                    }
                }
            }
            return chunks.deduplicate();
        }, "list_chunks");

        // <--[tag]
        // @attribute <CuboidTag.partial_chunks>
        // @returns ListTag(ChunkTag)
        // @description
        // Gets a list of all chunks partially or entirely within the CuboidTag.
        // -->
        tagProcessor.registerTag(ListTag.class, "partial_chunks", (attribute, cuboid) -> {
            ListTag chunks = new ListTag();
            for (LocationPair pair : cuboid.pairs) {
                ChunkTag minChunk = new ChunkTag(pair.low);
                ChunkTag maxChunk = new ChunkTag(pair.high);
                for (int x = minChunk.getX(); x <= maxChunk.getX(); x++) {
                    for (int z = minChunk.getZ(); z <= maxChunk.getZ(); z++) {
                        chunks.addObject(new ChunkTag(cuboid.getWorld(), x, z));
                    }
                }
            }
            return chunks;
        }, "list_partial_chunks");

        // <--[tag]
        // @attribute <CuboidTag.note_name>
        // @returns ElementTag
        // @description
        // Gets the name of a noted CuboidTag. If the cuboid isn't noted, this is null.
        // -->
        tagProcessor.registerTag(ElementTag.class, "note_name", (attribute, cuboid) -> {
            String noteName = NoteManager.getSavedId(cuboid);
            if (noteName == null) {
                return null;
            }
            return new ElementTag(noteName);
        }, "notable_name");

        tagProcessor.registerTag(ElementTag.class, "full", (attribute, cuboid) -> {
            BukkitImplDeprecations.cuboidFullTag.warn(attribute.context);
            return new ElementTag(cuboid.identifyFull());
        });
    }

    public CuboidTag shifted(LocationTag vec) {
        CuboidTag cuboid = clone();
        for (LocationPair pair : cuboid.pairs) {
            LocationTag low = pair.low.clone().add(vec.toVector());
            LocationTag high = pair.high.clone().add(vec.toVector());
            pair.regenerate(low, high);
        }
        return cuboid;
    }

    public CuboidTag including(Location loc) {
        loc = loc.clone();
        CuboidTag cuboid = clone();
        LocationTag low = cuboid.pairs.get(0).low;
        LocationTag high = cuboid.pairs.get(0).high;
        if (loc.getX() < low.getX()) {
            low = new LocationTag(low.getWorld(), loc.getX(), low.getY(), low.getZ());
        }
        if (loc.getY() < low.getY()) {
            low = new LocationTag(low.getWorld(), low.getX(), loc.getY(), low.getZ());
        }
        if (loc.getZ() < low.getZ()) {
            low = new LocationTag(low.getWorld(), low.getX(), low.getY(), loc.getZ());
        }
        if (loc.getX() > high.getX()) {
            high = new LocationTag(high.getWorld(), loc.getX(), high.getY(), high.getZ());
        }
        if (loc.getY() > high.getY()) {
            high = new LocationTag(high.getWorld(), high.getX(), loc.getY(), high.getZ());
        }
        if (loc.getZ() > high.getZ()) {
            high = new LocationTag(high.getWorld(), high.getX(), high.getY(), loc.getZ());
        }
        cuboid.pairs.get(0).regenerate(low, high);
        return cuboid;
    }

    public static ObjectTagProcessor<CuboidTag> tagProcessor = new ObjectTagProcessor<>();

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

        // <--[mechanism]
        // @object CuboidTag
        // @name set_member
        // @input (#,)CuboidTag
        // @description
        // Sets a given sub-cuboid of the cuboid.
        // Input is of the form like "2,cu@..." where 2 is the sub-cuboid index, or just a direct CuboidTag input.
        // The default index, if unspecified, is 1 (ie the first member).
        // @tags
        // <CuboidTag.get>
        // <CuboidTag.set[<cuboid>].at[<#>]>
        // -->
        if (mechanism.matches("set_member")) {
            if (noteName != null) {
                NotedAreaTracker.remove(this);
            }
            String value = mechanism.getValue().asString();
            int comma = value.indexOf(',');
            int member = 1;
            if (comma > 0 && !value.startsWith("cu@")) {
                member = new ElementTag(value.substring(0, comma)).asInt();
                value = value.substring(comma + 1);
            }
            CuboidTag subCuboid = CuboidTag.valueOf(value, mechanism.context);
            if (member < 1) {
                member = 1;
            }
            if (member > pairs.size()) {
                member = pairs.size();
            }
            LocationPair pair = subCuboid.pairs.get(0);
            pairs.set(member - 1, new LocationPair(pair.low.clone(), pair.high.clone()));
            if (noteName != null) {
                NotedAreaTracker.add(this);
            }
        }

        // <--[mechanism]
        // @object CuboidTag
        // @name add_member
        // @input (#,)CuboidTag
        // @description
        // Adds a sub-member to the cuboid (optionally at a specified index - otherwise, at the end).
        // Input is of the form like "2,cu@..." where 2 is the sub-cuboid index, or just a direct CuboidTag input.
        // Note that the index is where the member will end up. So, index 1 will add the cuboid as the very first member (moving the rest up +1 index value).
        // @tags
        // <CuboidTag.get>
        // <CuboidTag.add_member[<cuboid>]>
        // <CuboidTag.add_member[<cuboid>].at[<#>]>
        // -->
        if (mechanism.matches("add_member")) {
            if (noteName != null) {
                NotedAreaTracker.remove(this);
            }
            String value = mechanism.getValue().asString();
            int comma = value.indexOf(',');
            int member = pairs.size() + 1;
            if (comma > 0 && !value.startsWith("cu@")) {
                member = new ElementTag(value.substring(0, comma)).asInt();
                value = value.substring(comma + 1);
            }
            CuboidTag subCuboid = CuboidTag.valueOf(value, mechanism.context);
            if (member < 1) {
                member = 1;
            }
            if (member > pairs.size()) {
                member = pairs.size();
            }
            LocationPair pair = subCuboid.pairs.get(0);
            pairs.add(member - 1, new LocationPair(pair.low.clone(), pair.high.clone()));
            if (noteName != null) {
                NotedAreaTracker.add(this);
            }
        }

        // <--[mechanism]
        // @object CuboidTag
        // @name remove_member
        // @input ElementTag(Number)
        // @description
        // Remove a sub-member from the cuboid at the specified index.
        // @tags
        // <CuboidTag.remove_member[<#>]>
        // -->
        if (mechanism.matches("remove_member") && mechanism.requireInteger()) {
            if (pairs.size() == 1) {
                Debug.echoError("Cannot remove_member: CuboidTag only has 1 member left.");
                return;
            }
            if (noteName != null) {
                NotedAreaTracker.remove(this);
            }
            int member = mechanism.getValue().asInt();
            if (member < 1) {
                member = 1;
            }
            if (member > pairs.size()) {
                member = pairs.size();
            }
            pairs.remove(member - 1);
            if (noteName != null) {
                NotedAreaTracker.add(this);
            }
        }

        tagProcessor.processMechanism(this, mechanism);
    }

    @Override
    public boolean advancedMatches(String matcher) {
        String matcherLow = CoreUtilities.toLowerCase(matcher);
        if (matcherLow.equals("cuboid")) {
            return true;
        }
        return areaBaseAdvancedMatches(matcher);
    }
}
