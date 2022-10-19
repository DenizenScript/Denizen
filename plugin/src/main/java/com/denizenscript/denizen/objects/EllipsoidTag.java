package com.denizenscript.denizen.objects;

import com.denizenscript.denizen.utilities.NotedAreaTracker;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.flags.AbstractFlagTracker;
import com.denizenscript.denizencore.flags.FlaggableObject;
import com.denizenscript.denizencore.flags.SavableMapFlagTracker;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.notable.NoteManager;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.objects.notable.Notable;
import com.denizenscript.denizencore.objects.notable.Note;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.YamlConfiguration;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class EllipsoidTag implements ObjectTag, Notable, Cloneable, AreaContainmentObject, FlaggableObject, Adjustable {

    // <--[ObjectType]
    // @name EllipsoidTag
    // @prefix ellipsoid
    // @base ElementTag
    // @implements FlaggableObject, AreaObject
    // @ExampleTagBase ellipsoid[my_noted_ellipsoid]
    // @ExampleValues my_ellipsoid_note
    // @ExampleForReturns
    // - note %VALUE% as:my_new_ellipsoid
    // @format
    // The identity format for ellipsoids is <x>,<y>,<z>,<world>,<x-radius>,<y-radius>,<z-radius>
    // For example, 'ellipsoid@1,2,3,space,7,7,7'.
    //
    // @description
    // An EllipsoidTag represents an ellipsoidal region in the world.
    //
    // The word 'ellipsoid' means a less strict sphere.
    // Basically: an "ellipsoid" is to a 3D "sphere" what an "ellipse" (or "oval") is to a 2D "circle".
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

    //////////////////
    //    OBJECT FETCHER
    ////////////////

    @Deprecated
    public static EllipsoidTag valueOf(String string) {
        return valueOf(string, null);
    }

    /**
     * Gets an Ellipsoid Object from a string form.
     *
     * @param string the string
     */
    @Fetchable("ellipsoid")
    public static EllipsoidTag valueOf(String string, TagContext context) {
        if (string.startsWith("ellipsoid@")) {
            string = string.substring(10);
        }
        if (string.contains("@")) {
            return null;
        }
        if (!TagManager.isStaticParsing) {
            Notable noted = NoteManager.getSavedObject(string);
            if (noted instanceof EllipsoidTag) {
                return (EllipsoidTag) noted;
            }
        }
        List<String> split = CoreUtilities.split(string, ',');
        if (split.size() != 7) {
            return null;
        }
        String worldName = split.get(3);
        for (int i = 0; i < 7; i++) {
            if (i != 3 && !ArgumentHelper.matchesDouble(split.get(i))) {
                if (context == null || context.showErrors()) {
                    Debug.echoError("EllipsoidTag input is not a valid decimal number: " + split.get(i));
                    return null;
                }
            }
        }
        LocationTag location = new LocationTag(Double.parseDouble(split.get(0)), Double.parseDouble(split.get(1)), Double.parseDouble(split.get(2)), worldName);
        LocationTag size = new LocationTag(null, Double.parseDouble(split.get(4)), Double.parseDouble(split.get(5)), Double.parseDouble(split.get(6)));
        return new EllipsoidTag(location, size);
    }

    /**
     * Determines whether a string is a valid ellipsoid.
     *
     * @param arg the string
     * @return true if matched, otherwise false
     */
    public static boolean matches(String arg) {
        try {
            return EllipsoidTag.valueOf(arg, CoreUtilities.noDebugContext) != null;
        }
        catch (Exception e) {
            return false;
        }
    }
    @Override
    public EllipsoidTag clone() {
        return new EllipsoidTag(center.clone(), size.clone());
    }

    @Override
    public ObjectTag duplicate() {
        EllipsoidTag self = refreshState();
        if (self.noteName != null) {
            return this;
        }
        return self.clone();
    }

    ///////////////
    //   Constructors
    /////////////

    public EllipsoidTag(LocationTag center, LocationTag size) {
        this.center = center;
        this.size = size;
    }

    /////////////////////
    //   INSTANCE FIELDS/METHODS
    /////////////////

    public LocationTag center;

    public LocationTag size;

    public String noteName = null, priorNoteName = null;

    public AbstractFlagTracker flagTracker = null;

    @Override
    public ListTag getBlocks(Predicate<Location> test) {
        return getCuboidBoundary().getBlocks(test == null ? this::contains : (l) -> (test.test(l) && contains(l)));
    }

    public List<LocationTag> getBlockLocationsUnfiltered(boolean doMax) {
        List<LocationTag> initial = getCuboidBoundary().getBlockLocationsUnfiltered(doMax);
        List<LocationTag> locations = new ArrayList<>();
        for (LocationTag loc : initial) {
            if (contains(loc)) {
                locations.add(loc);
            }
        }
        return locations;
    }

    @Override
    public ListTag getShell() {
        ListTag output = new ListTag();
        double yScale = size.getY();
        int maxY = (int) Math.floor(yScale);
        output.addObject(new LocationTag(center.getBlockX(), center.getBlockY() - maxY, center.getBlockZ(), center.getWorldName()));
        if (maxY != 0) {
            output.addObject(new LocationTag(center.getBlockX(), center.getBlockY() + maxY, center.getBlockZ(), center.getWorldName()));
        }
        for (int y = -maxY; y <= maxY; y++) {
            double yProgMin = Math.min(1.0, (Math.abs(y) + 1) / yScale);
            double yProgMax = Math.abs(y) / yScale;
            double minSubWidth = Math.sqrt(1.0 - yProgMin * yProgMin);
            double maxSubWidth = Math.sqrt(1.0 - yProgMax * yProgMax);
            double minX = size.getX() * minSubWidth - 1;
            double minZ = size.getZ() * minSubWidth - 1;
            double maxX = size.getX() * maxSubWidth;
            double maxZ = size.getZ() * maxSubWidth;
            for (int x = 0; x < maxX; x++) {
                for (int z = 0; z < maxZ; z++) {
                    double scaleTestMin = (x * x) / (minX * minX) + (z * z) / (minZ * minZ);
                    double scaleTestMax = (x * x) / (maxX * maxX) + (z * z) / (maxZ * maxZ);
                    if (scaleTestMin >= 1.0 && scaleTestMax <= 1.0) {
                        output.addObject(new LocationTag(center.getBlockX() + x, center.getBlockY() + y, center.getBlockZ() + z, center.getWorldName()));
                        if (x != 0) {
                            output.addObject(new LocationTag(center.getBlockX() - x, center.getBlockY() + y, center.getBlockZ() + z, center.getWorldName()));
                        }
                        if (z != 0) {
                            output.addObject(new LocationTag(center.getBlockX() + x, center.getBlockY() + y, center.getBlockZ() - z, center.getWorldName()));
                        }
                        if (x != 0 && z != 0) {
                            output.addObject(new LocationTag(center.getBlockX() - x, center.getBlockY() + y, center.getBlockZ() - z, center.getWorldName()));
                        }
                    }
                }
            }
        }
        return output;
    }

    public boolean contains(Location test) {
        if (test.getWorld() == null || !test.getWorld().getName().equals(center.getWorld().getName())) {
            return false;
        }
        double xbase = test.getX() - center.getX();
        double ybase = test.getY() - center.getY();
        double zbase = test.getZ() - center.getZ();
        return ((xbase * xbase) / (size.getX() * size.getX())
                + (ybase * ybase) / (size.getY() * size.getY())
                + (zbase * zbase) / (size.getZ() * size.getZ()) <= 1);
    }

    public boolean intersects(ChunkTag chunk) {
        int xMin = chunk.getX() * 16;
        int zMin = chunk.getZ() * 16;
        LocationTag locTest = chunk.getCenter();
        // This mess gets a position within the chunk that is as closes as possible to the ellipsoid's center
        locTest.setY(center.getY());
        if (center.getX() > xMin) {
            if (center.getX() < xMin + 16) {
                locTest.setX(center.getX());
            }
            else {
                locTest.setX(center.getX());
            }
        }
        if (center.getZ() > zMin) {
            if (center.getZ() < zMin + 16) {
                locTest.setZ(center.getZ());
            }
            else {
                locTest.setZ(center.getZ());
            }
        }
        return contains(locTest);
    }

    String prefix = "ellipsoid";

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public String debuggable() {
        EllipsoidTag self = refreshState();
        if (self.isUnique()) {
            return "<LG>ellipsoid@<Y>" + self.noteName + "<GR> (" + self.identifyFull() + ")";
        }
        else {
            return self.identifyFull();
        }
    }

    @Override
    public boolean isUnique() {
        return noteName != null;
    }

    @Override
    @Note("Ellipsoids")
    public Object getSaveObject() {
        YamlConfiguration section = new YamlConfiguration();
        section.set("object", identifyFull());
        section.set("flags", flagTracker.toString());
        return section;
    }

    @Override
    public void makeUnique(String id) {
        EllipsoidTag toNote = clone();
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
    public EllipsoidTag refreshState() {
        if (noteName == null && priorNoteName != null) {
            Notable note = NoteManager.getSavedObject(priorNoteName);
            if (note instanceof EllipsoidTag) {
                return (EllipsoidTag) note;
            }
            priorNoteName = null;
        }
        return this;
    }

    @Override
    public int hashCode() {
        if (noteName != null) {
            return noteName.hashCode();
        }
        return center.hashCode() + size.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof EllipsoidTag)) {
            return false;
        }
        EllipsoidTag ellipsoid2 = (EllipsoidTag) other;
        if ((noteName == null) != (ellipsoid2.noteName == null)) {
            return false;
        }
        if (noteName != null) {
            return noteName.equals(ellipsoid2.noteName);
        }
        if (!center.getWorldName().equals(ellipsoid2.center.getWorldName())) {
            return false;
        }
        if (center.distanceSquaredNoWorld(ellipsoid2.center) >= 0.25) {
            return false;
        }
        if (size.distanceSquaredNoWorld(ellipsoid2.size) >= 0.25) {
            return false;
        }
        return true;
    }

    @Override
    public String identify() {
        EllipsoidTag self = refreshState();
        if (self.isUnique()) {
            return "ellipsoid@" + self.noteName;
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
        return "ellipsoid@" + center.getX() + "," + center.getY() + "," + center.getZ() + "," + center.getWorldName()
                + "," + size.getX() + "," + size.getY() + "," + size.getZ();
    }

    @Override
    public String toString() {
        return identify();
    }

    @Override
    public ObjectTag setPrefix(String prefix) {
        if (prefix != null) {
            this.prefix = prefix;
        }
        return this;
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
        return new CuboidTag(center.clone().subtract(size.toVector()), center.clone().add(size.toVector()));
    }

    @Override
    public WorldTag getWorld() {
        return new WorldTag(center.getWorldName());
    }

    @Override
    public EllipsoidTag withWorld(WorldTag world) {
        LocationTag loc = center.clone();
        loc.setWorld(world.getWorld());
        return new EllipsoidTag(loc, size.clone());
    }

    public static void registerTags() {

        AbstractFlagTracker.registerFlagHandlers(tagProcessor);
        AreaContainmentObject.registerTags(EllipsoidTag.class, tagProcessor);

        // <--[tag]
        // @attribute <EllipsoidTag.random>
        // @returns LocationTag
        // @description
        // Returns a random decimal location within the ellipsoid.
        // Note that distribution of results will not be completely even.
        // -->
        tagProcessor.registerTag(LocationTag.class, "random", (attribute, object) -> {
            // This is an awkward hack to try to weight towards the center a bit (to counteract the weight-away-from-center that would otherwise happen).
            double y = (Math.sqrt(CoreUtilities.getRandom().nextDouble()) * 2 - 1) * object.size.getY();
            Vector result = new Vector();
            result.setY(y);
            double yProg = Math.abs(y) / object.size.getY();
            double subWidth = Math.sqrt(1.0 - yProg * yProg);
            double maxX = object.size.getX() * subWidth;
            double maxZ = object.size.getZ() * subWidth;
            result.setX(maxX * (CoreUtilities.getRandom().nextDouble() * 2 - 1));
            result.setZ(maxZ * (CoreUtilities.getRandom().nextDouble() * 2 - 1));
            LocationTag out = object.center.clone();
            out.add(result);
            return out;
        });

        // <--[tag]
        // @attribute <EllipsoidTag.location>
        // @returns LocationTag
        // @description
        // Returns the center location of the ellipsoid.
        // -->
        tagProcessor.registerTag(LocationTag.class, "location", (attribute, object) -> {
            return object.center;
        });

        // <--[tag]
        // @attribute <EllipsoidTag.size>
        // @returns LocationTag
        // @description
        // Returns the size of the ellipsoid.
        // -->
        tagProcessor.registerTag(LocationTag.class, "size", (attribute, object) -> {
            return object.size;
        });

        // <--[tag]
        // @attribute <EllipsoidTag.add[<location>]>
        // @returns EllipsoidTag
        // @description
        // Returns a copy of this ellipsoid, shifted by the input location.
        // -->
        tagProcessor.registerTag(EllipsoidTag.class, "add", (attribute, object) -> {
            if (!attribute.hasParam()) {
                attribute.echoError("ellipsoid.add[...] tag must have an input.");
                return null;
            }
            return new EllipsoidTag(object.center.clone().add(attribute.paramAsType(LocationTag.class)), object.size.clone());
        });

        // <--[tag]
        // @attribute <EllipsoidTag.include[<location>]>
        // @returns EllipsoidTag
        // @description
        // Returns a copy of this ellipsoid, with the size value adapted to include the specified world location.
        // -->
        tagProcessor.registerTag(EllipsoidTag.class, "include", (attribute, object) -> {
            if (!attribute.hasParam()) {
                attribute.echoError("ellipsoid.include[...] tag must have an input.");
                return null;
            }
            LocationTag target = attribute.paramAsType(LocationTag.class);
            if (object.contains(target)) {
                return object;
            }
            LocationTag size = object.size.clone();
            Vector relative = target.toVector().subtract(object.center.toVector());
            // Cuboid minimum expansion
            size.setX(Math.max(size.getX(), Math.abs(relative.getX())));
            size.setY(Math.max(size.getY(), Math.abs(relative.getY())));
            size.setZ(Math.max(size.getZ(), Math.abs(relative.getZ())));
            EllipsoidTag result = new EllipsoidTag(object.center.clone(), new LocationTag(size));
            if (result.contains(target)) {
                return result;
            }
            double sizeLen = size.length();
            // Ellipsoid additional expand
            while (!result.contains(target)) {
                // I gave up on figuring out the math for this, so here's an awful loop-hack
                double projX = (relative.getX() * relative.getX()) / (size.getX() * size.getX());
                double projY = (relative.getY() * relative.getY()) / (size.getY() * size.getY());
                double projZ = (relative.getZ() * relative.getZ()) / (size.getZ() * size.getZ());
                double scale = Math.max(projX + projY + projZ, sizeLen * 0.01);
                if (projX >= projY && projX >= projZ) {
                    size.setX(size.getX() + scale);
                }
                else if (projY >= projX && projY >= projZ) {
                    size.setY(size.getY() + scale);
                }
                else if (projZ >= projX && projZ >= projY) {
                    size.setZ(size.getZ() + scale);
                }
                else {
                    size = size.add(scale, scale, scale);
                }
                result.size = size;
            }
            return result;
        });

        // <--[tag]
        // @attribute <EllipsoidTag.with_location[<location>]>
        // @returns EllipsoidTag
        // @description
        // Returns a copy of this ellipsoid, set to the specified location.
        // -->
        tagProcessor.registerTag(EllipsoidTag.class, "with_location", (attribute, object) -> {
            if (!attribute.hasParam()) {
                attribute.echoError("ellipsoid.with_location[...] tag must have an input.");
                return null;
            }
            return new EllipsoidTag(attribute.paramAsType(LocationTag.class), object.size.clone());
        });

        // <--[tag]
        // @attribute <EllipsoidTag.with_size[<location>]>
        // @returns EllipsoidTag
        // @description
        // Returns a copy of this ellipsoid, set to the specified size.
        // -->
        tagProcessor.registerTag(EllipsoidTag.class, "with_size", (attribute, object) -> {
            if (!attribute.hasParam()) {
                attribute.echoError("ellipsoid.with_size[...] tag must have an input.");
                return null;
            }
            return new EllipsoidTag(object.center.clone(), attribute.paramAsType(LocationTag.class));
        });

        // <--[tag]
        // @attribute <EllipsoidTag.chunks>
        // @returns ListTag(ChunkTag)
        // @description
        // Returns a list of all chunks that this ellipsoid touches at all (note that no valid ellipsoid tag can ever totally contain a chunk, due to vertical limits and roundness).
        // -->
        tagProcessor.registerTag(ListTag.class, "chunks", (attribute, object) -> {
            ListTag chunks = new ListTag();
            double minPossibleX = object.center.getX() - object.size.getX();
            double minPossibleZ = object.center.getZ() - object.size.getZ();
            double maxPossibleX = object.center.getX() + object.size.getX();
            double maxPossibleZ = object.center.getZ() + object.size.getZ();
            int minChunkX = (int) Math.floor(minPossibleX / 16);
            int minChunkZ = (int) Math.floor(minPossibleZ / 16);
            int maxChunkX = (int) Math.ceil(maxPossibleX / 16);
            int maxChunkZ = (int) Math.ceil(maxPossibleZ / 16);
            ChunkTag testChunk = new ChunkTag(object.center);
            for (int x = minChunkX; x <= maxChunkX; x++) {
                testChunk.chunkX = x;
                for (int z = minChunkZ; z <= maxChunkZ; z++) {
                    testChunk.chunkZ = z;
                    if (object.intersects(testChunk)) {
                        chunks.addObject(new ChunkTag(testChunk.world, testChunk.chunkX, testChunk.chunkZ));
                    }
                }
            }
            return chunks;
        });

        // <--[tag]
        // @attribute <EllipsoidTag.note_name>
        // @returns ElementTag
        // @description
        // Gets the name of a noted EllipsoidTag. If the ellipsoid isn't noted, this is null.
        // -->
        tagProcessor.registerTag(ElementTag.class, "note_name", (attribute, ellipsoid) -> {
            String noteName = NoteManager.getSavedId(ellipsoid);
            if (noteName == null) {
                return null;
            }
            return new ElementTag(noteName);
        });
    }

    public static ObjectTagProcessor<EllipsoidTag> tagProcessor = new ObjectTagProcessor<>();

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {
        return tagProcessor.getObjectAttribute(this, attribute);
    }

    @Override
    public String getNoteName() {
        return noteName;
    }

    @Override
    public boolean doesContainLocation(Location loc) {
        return contains(loc);
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
        if (matcherLow.equals("ellipsoid")) {
            return true;
        }
        return areaBaseAdvancedMatches(matcher);
    }
}
