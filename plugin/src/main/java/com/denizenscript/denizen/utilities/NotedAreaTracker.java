package com.denizenscript.denizen.utilities;

import com.denizenscript.denizen.objects.AreaContainmentObject;
import com.denizenscript.denizen.objects.CuboidTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;

/**
 * Special helper class that tracks noted areas in a way that allows for very rapid "get all areas containing" checks, within the confines of the scales that Minecraft normally operates at.
 * This divides all notes first into one distinct set per world, and then within worlds it will sub-divide into 5 sets:
 * The "global" set, a 50x50 set, a 200x200 set, and both those sets again but with a half-width offset.
 * Any area that fits within a 50x50 grid gets added to the 50x50 grid... any area below 50x50 in scale that doesn't fit the grid will likely fit the offset grid instead.
 * If the 50x50 grids can't be fit, the 200x200 grids are tried. If those fail, the global set is used.
 * Note that vertical position (Y coordinate) is entirely ignored.
 * Because most noted areas are likely to fit into one of these grids, any lookups can confine themselves to only looking at the Areas defined within the same grid cell.
 * This uses multiple layers of imperfect checks before doing the final exact-containment check, as the imperfect checks are significantly faster to run, especially for complex area shapes like polygons.
 */
public class NotedAreaTracker {

    public static final class TrackedArea {

        public TrackedArea(AreaContainmentObject area, LocationTag low, LocationTag high) {
            this.area = area;
            lowX = low.getBlockX();
            lowY = low.getBlockY();
            lowZ = low.getBlockZ();
            highX = high.getBlockX();
            highY = high.getBlockY();
            highZ = high.getBlockZ();
        }

        public TrackedArea(AreaContainmentObject area) {
            CuboidTag boundary = area.getCuboidBoundary();
            LocationTag low = boundary.getLow(0), high = boundary.getHigh(0);
            this.area = area;
            lowX = low.getBlockX();
            lowY = low.getBlockY();
            lowZ = low.getBlockZ();
            highX = high.getBlockX();
            highY = high.getBlockY();
            highZ = high.getBlockZ();
        }

        public final AreaContainmentObject area;

        public final int lowX, lowY, lowZ, highX, highY, highZ;

        public boolean mightContain(int x, int y, int z) {
            return x >= lowX && x <= highX && z >= lowZ && z <= highZ && y >= lowY && y <= highY;
        }

        public boolean mightIntersect(TrackedArea area2) {
            return area2.lowX <= highX && area2.highX >= lowX && area2.lowZ <= highZ && area2.highZ >= lowZ && area2.lowY <= highY && area2.highY >= lowY;
        }

        @Override
        public int hashCode() {
            return area.hashCode();
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof TrackedArea compareTo)) {
                return false;
            }
            return lowX == compareTo.lowX && lowZ == compareTo.lowZ && lowY == compareTo.lowY
                    && highX == compareTo.highX && highZ == compareTo.highZ && highY == compareTo.highY
                    && area.equals(compareTo.area);
        }
    }

    public static final class AreaSet {

        public AreaSet(int type, int index) {
            this.type = type;
            this.index = index;
        }

        public final ArrayList<TrackedArea> list = new ArrayList<>();

        public final int index;

        public final int type;

        public boolean isEmpty() {
            return list.isEmpty();
        }
    }

    public static final class PerWorldSet {

        public final AreaSet globalSet = new AreaSet(0, 0);

        public final Int2ObjectOpenHashMap<AreaSet> sets50 = new Int2ObjectOpenHashMap<>(), sets50_offset = new Int2ObjectOpenHashMap<>(), sets200 = new Int2ObjectOpenHashMap<>(), sets200_offset = new Int2ObjectOpenHashMap<>();

        public static boolean doesFit(TrackedArea area, int scale, int offset) {
            int lowX = (area.lowX + offset) / scale, lowZ = (area.lowZ + offset) / scale, highX = (area.highX + offset) / scale, highZ = (area.highZ + offset) / scale;
            return lowX == highX && lowZ == highZ;
        }

        public static int getIndex(int x, int z, int scale, int offset) {
            // Index is unique "enough", these lists don't need to be perfect, and an int key is significantly faster than constructing an exact coordinate object.
            int cleanX = (x + offset) / scale, cleanZ = (z + offset) / scale;
            return cleanX + (cleanZ << 16);
        }

        public AreaSet getOrGenSetFor(Int2ObjectOpenHashMap<AreaSet> sets, int type, TrackedArea area, int scale, int offset, boolean generate) {
            int index = getIndex(area.lowX, area.lowZ, scale, offset);
            AreaSet set = sets.get(index);
            if (set == null && generate) {
                set = new AreaSet(type, index);
                sets.put(index, set);
            }
            return set;
        }

        public AreaSet bestSetFor(TrackedArea area, boolean generate) {
            if (doesFit(area, 50, 0)) {
                return getOrGenSetFor(sets50, 1, area, 50, 0, generate);
            }
            else if (doesFit(area, 50, 25)) {
                return getOrGenSetFor(sets50_offset, 2, area, 50, 25, generate);
            }
            else if (doesFit(area, 200, 0)) {
                return getOrGenSetFor(sets200, 3, area, 200, 0, generate);
            }
            else if (doesFit(area, 200, 100)) {
                return getOrGenSetFor(sets200_offset, 4, area, 200, 100, generate);
            }
            return globalSet;
        }

        public boolean isEmpty() {
            return globalSet.isEmpty() && sets50.isEmpty() && sets50_offset.isEmpty() && sets200.isEmpty() && sets200_offset.isEmpty();
        }

        public void remove(AreaSet set) {
            switch (set.type) {
                case 1 -> sets50.remove(set.index);
                case 2 -> sets50_offset.remove(set.index);
                case 3 -> sets200.remove(set.index);
                case 4 -> sets200_offset.remove(set.index);
            }
        }
    }

    public static HashMap<String, PerWorldSet> worlds = new HashMap<>();

    /**
     * Call to add an area into the tracker.
     */
    public static void add(AreaContainmentObject area) {
        String worldName = CoreUtilities.toLowerCase(area.getWorld().getName());
        PerWorldSet set = worlds.get(worldName);
        if (set == null) {
            set = new PerWorldSet();
            worlds.put(worldName, set);
        }
        TrackedArea tracker = new TrackedArea(area);
        AreaSet areaSet = set.bestSetFor(tracker, true);
        areaSet.list.add(tracker);
    }

    /**
     * Call to remove an area from the tracker.
     */
    public static void remove(AreaContainmentObject area) {
        String worldName = CoreUtilities.toLowerCase(area.getWorld().getName());
        PerWorldSet set = worlds.get(worldName);
        if (set == null) {
            return;
        }
        TrackedArea tracker = new TrackedArea(area);
        AreaSet areaSet = set.bestSetFor(tracker, false);
        if (areaSet == null) {
            return;
        }
        areaSet.list.remove(tracker);
        if (areaSet.isEmpty()) {
            set.remove(areaSet);
            if (set.isEmpty()) {
                worlds.remove(worldName);
            }
        }
    }

    public static void forEachAreaInSetThatContains(int x, int y, int z, LocationTag location, AreaSet set, Consumer<AreaContainmentObject> action) {
        if (set == null) {
            return;
        }
        for (TrackedArea area : set.list) {
            if (area.mightContain(x, y, z) && area.area.doesContainLocation(location)) {
                action.accept(area.area);
            }
        }
    }

    /**
     * Call to run an action over every Area that contains a given location.
     */
    public static void forEachAreaThatContains(LocationTag location, Consumer<AreaContainmentObject> action) {
        int x = location.getBlockX(), y = location.getBlockY(), z = location.getBlockZ();
        PerWorldSet set = worlds.get(CoreUtilities.toLowerCase(location.getWorldName()));
        if (set == null) {
            return;
        }
        forEachAreaInSetThatContains(x, y, z, location, set.globalSet, action);
        forEachAreaInSetThatContains(x, y, z, location, set.sets50.get(PerWorldSet.getIndex(x, z, 50, 0)), action);
        forEachAreaInSetThatContains(x, y, z, location, set.sets50_offset.get(PerWorldSet.getIndex(x, z, 50, 25)), action);
        forEachAreaInSetThatContains(x, y, z, location, set.sets200.get(PerWorldSet.getIndex(x, z, 200, 0)), action);
        forEachAreaInSetThatContains(x, y, z, location, set.sets200_offset.get(PerWorldSet.getIndex(x, z, 200, 100)), action);
    }

    public static void forEachAreaInSetThatIntersects(TrackedArea area2, AreaSet set, Consumer<AreaContainmentObject> action) {
        if (set == null) {
            return;
        }
        for (TrackedArea area : set.list) {
            if (area.mightIntersect(area2)) {
                action.accept(area.area);
            }
        }
    }

    public static void forEachAreaThatIntersects(LocationTag min, LocationTag max, Consumer<AreaContainmentObject> action) {
        CuboidTag.LocationPair pair = new CuboidTag.LocationPair(min, max);
        min = pair.low;
        max = pair.high;
        TrackedArea area2 = new TrackedArea(null, min, max);
        PerWorldSet set = worlds.get(CoreUtilities.toLowerCase(min.getWorldName()));
        if (set == null) {
            return;
        }
        forEachAreaInSetThatIntersects(area2, set.globalSet, action);
        // Loose heuristic for when a regional-indexed loop is probably counterproductive - total looped regions exceeds 10x actual total areas available
        if (pair.xDistance() * pair.zDistance() / (50 * 50) > (set.sets50.size() + set.sets50_offset.size()) * 10) {
            for (AreaSet areaSet : set.sets50.values()) {
                forEachAreaInSetThatIntersects(area2, areaSet, action);
            }
            for (AreaSet areaSet : set.sets50_offset.values()) {
                forEachAreaInSetThatIntersects(area2, areaSet, action);
            }
        }
        else {
            for (int x = area2.lowX - 50; x <= area2.highX + 50; x += 50) {
                for (int z = area2.lowZ - 50; z <= area2.highZ + 50; z += 50) {
                    forEachAreaInSetThatIntersects(area2, set.sets50.get(PerWorldSet.getIndex(x, z, 50, 0)), action);
                    forEachAreaInSetThatIntersects(area2, set.sets50_offset.get(PerWorldSet.getIndex(x, z, 50, 25)), action);
                }
            }
        }
        if (pair.xDistance() * pair.zDistance() / (288 * 200) > (set.sets200.size() + set.sets200_offset.size()) * 10) {
            for (AreaSet areaSet : set.sets200.values()) {
                forEachAreaInSetThatIntersects(area2, areaSet, action);
            }
            for (AreaSet areaSet : set.sets200_offset.values()) {
                forEachAreaInSetThatIntersects(area2, areaSet, action);
            }
        }
        else {
            for (int x = area2.lowX - 200; x <= area2.highX + 200; x += 200) {
                for (int z = area2.lowZ - 200; z <= area2.highZ + 200; z += 200) {
                    forEachAreaInSetThatIntersects(area2, set.sets200.get(PerWorldSet.getIndex(x, z, 200, 0)), action);
                    forEachAreaInSetThatIntersects(area2, set.sets200_offset.get(PerWorldSet.getIndex(x, z, 200, 100)), action);
                }
            }
        }
    }
}
