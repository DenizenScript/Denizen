package com.denizenscript.denizen.objects;

import com.denizenscript.denizen.objects.notable.NotableManager;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.depends.Depends;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizen.utilities.Settings;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.interfaces.BlockHelper;
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
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class CuboidTag implements ObjectTag, Cloneable, Notable, Adjustable {

    // <--[language]
    // @name CuboidTag Objects
    // @group Object System
    // @description
    // A CuboidTag represents a cuboidal region in the world.
    //
    // The word 'cuboid' means a less strict cube.
    // Basically: a "cuboid" is to a 3D "cube" what a "rectangle" is to a 2D "square".
    //
    // One 'cuboid' consists of two points: the low point and a high point.
    // a CuboidTag can contain as many cuboids within itself as needed (this allows forming more complex shapes from a single CuboidTag).
    //
    // These use the object notation "cu@".
    // The identity format for cuboids is <world>,<x1>,<y1>,<z1>,<x2>,<y2>,<z2>
    // Multi-member cuboids can simply continue listing x,y,z pairs.
    // For example, 'cu@space,1,2,3,4,5,6'.
    //
    // -->

    // Cloning
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
        cuboid.pairs = new ArrayList<>(pairs.size());
        for (LocationPair pair : pairs) {
            cuboid.pairs.add(new LocationPair(pair.low.clone(), pair.high.clone()));
        }
        return cuboid;
    }

    /////////////////////
    //   STATIC METHODS
    /////////////////

    public static List<CuboidTag> getNotableCuboidsContaining(Location location) {
        List<CuboidTag> cuboids = new ArrayList<>();
        for (CuboidTag cuboid : NotableManager.getAllType(CuboidTag.class)) {
            if (cuboid.isInsideCuboid(location)) {
                cuboids.add(cuboid);
            }
        }

        return cuboids;
    }

    //////////////////
    //    OBJECT FETCHER
    ////////////////

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
        if (CoreUtilities.contains(string, '|')) {
            ListTag positions = ListTag.valueOf(string, context);
            if (positions.size() > 1
                    && LocationTag.matches(positions.get(0))
                    && LocationTag.matches(positions.get(1))) {
                if (positions.size() % 2 != 0) {
                    if (context == null || context.debug) {
                        Debug.echoError("valueOf CuboidTag returning null (Uneven number of locations): '" + string + "'.");
                    }
                    return null;
                }
                CuboidTag toReturn = new CuboidTag();
                for (int i = 0; i < positions.size(); i += 2) {
                    LocationTag pos_1 = LocationTag.valueOf(positions.get(i));
                    LocationTag pos_2 = LocationTag.valueOf(positions.get(i + 1));
                    if (pos_1 == null || pos_2 == null) {
                        if (context == null || context.debug) {
                            Debug.echoError("valueOf in CuboidTag returning null (null locations): '" + string + "'.");
                        }
                        return null;
                    }
                    if (pos_1.getWorldName() == null || pos_2.getWorldName() == null) {
                        if (context == null || context.debug) {
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
                if (context == null || context.debug) {
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
                if (context == null || context.debug) {
                    Debug.echoError("valueOf CuboidTag returning null (Improper number value inputs): '" + ex.getMessage() + "'.");
                }
                return null;
            }
            if (toReturn.pairs.size() > 0) {
                return toReturn;
            }
        }

        Notable noted = NotableManager.getSavedObject(string);
        if (noted instanceof CuboidTag) {
            return (CuboidTag) noted;
        }

        if (context == null || context.debug) {
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
    public ObjectTag duplicate() {
        return clone();
    }

    @Override
    public int hashCode() {
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

    ///////////////
    //  LocationPairs
    /////////////

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

            // Find the low and high locations based on the points
            // specified
            int x_high = (point_1.getBlockX() >= point_2.getBlockX()
                    ? point_1.getBlockX() : point_2.getBlockX());
            int x_low = (point_1.getBlockX() <= point_2.getBlockX()
                    ? point_1.getBlockX() : point_2.getBlockX());

            int y_high = (point_1.getBlockY() >= point_2.getBlockY()
                    ? point_1.getBlockY() : point_2.getBlockY());
            int y_low = (point_1.getBlockY() <= point_2.getBlockY()
                    ? point_1.getBlockY() : point_2.getBlockY());

            int z_high = (point_1.getBlockZ() >= point_2.getBlockZ()
                    ? point_1.getBlockZ() : point_2.getBlockZ());
            int z_low = (point_1.getBlockZ() <= point_2.getBlockZ()
                    ? point_1.getBlockZ() : point_2.getBlockZ());

            // Specify defining locations to the pair
            low = new LocationTag(x_low, y_low, z_low, world);
            high = new LocationTag(x_high, y_high, z_high, world);
        }
    }

    ///////////////////
    //  Constructors/Instance Methods
    //////////////////

    // Location Pairs (low, high) that make up the CuboidTag
    public List<LocationPair> pairs = new ArrayList<>();

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
        if (point_1.getWorld() != point_2.getWorld()) {
            Debug.echoError("Tried to make cross-world cuboid!");
            return;
        }
        if (pairs.size() > 0 && point_1.getWorld() != getWorld()) {
            Debug.echoError("Tried to make cross-world cuboid set!");
            return;
        }
        // Make a new pair
        LocationPair pair = new LocationPair(point_1, point_2);
        // Add it to the Cuboid pairs list
        pairs.add(pair);
    }

    public static boolean isBetween(double a, double b, double c) {
        return b > a ? (c >= a && c < b) : (c >= b && c < a); // Cuboid's have to be compensated for weirdly
    }

    public boolean isInsideCuboid(Location location) {
        if (location.getWorld() == null) {
            return false;
        }
        for (LocationPair pair : pairs) {
            if (!location.getWorld().getName().equals(pair.low.getWorldName())) {
                continue;
            }
            if (!isBetween(pair.low.getBlockX(), pair.high.getBlockX() + 1, location.getBlockX())) {
                continue;
            }
            if (!isBetween(pair.low.getBlockY(), pair.high.getBlockY() + 1, location.getBlockY())) {
                continue;
            }
            if (isBetween(pair.low.getBlockZ(), pair.high.getBlockZ() + 1, location.getBlockZ())) {
                return true;
            }
        }

        // Does not match any of the pairs
        return false;
    }

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

            for (int x = 0; x < x_distance; x++) {
                for (int y = 0; y < y_distance; y++) {
                    list.addObject(new LocationTag(low.getWorld(), low.getBlockX() + x, low.getBlockY() + y, low.getBlockZ()));
                    list.addObject(new LocationTag(low.getWorld(), low.getBlockX() + x, low.getBlockY() + y, high.getBlockZ()));
                    index++;
                    if (index > max) {
                        return list;
                    }
                }
                for (int z = 0; z < z_distance; z++) {
                    list.addObject(new LocationTag(low.getWorld(), low.getBlockX() + x, low.getBlockY(), low.getBlockZ() + z));
                    list.addObject(new LocationTag(low.getWorld(), low.getBlockX() + x, high.getBlockY(), low.getBlockZ() + z));
                    index++;
                    if (index > max) {
                        return list;
                    }
                }
            }
            for (int y = 0; y < y_distance; y++) {
                for (int z = 0; z < z_distance; z++) {
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

    public ListTag getOutline() {

        //    +-----2
        //   /|    /|
        //  +-----+ |
        //  | +---|-+
        //  |/    |/
        //  1-----+
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
                list.addObject(new LocationTag(loc_1.getWorld(),
                        loc_1.getBlockX(),
                        y,
                        loc_1.getBlockZ()));

                list.addObject(new LocationTag(loc_1.getWorld(),
                        loc_2.getBlockX(),
                        y,
                        loc_2.getBlockZ()));

                list.addObject(new LocationTag(loc_1.getWorld(),
                        loc_1.getBlockX(),
                        y,
                        loc_2.getBlockZ()));

                list.addObject(new LocationTag(loc_1.getWorld(),
                        loc_2.getBlockX(),
                        y,
                        loc_1.getBlockZ()));
                index++;
                if (index > max) {
                    return list;
                }
            }

            for (int x = loc_1.getBlockX(); x < loc_1.getBlockX() + x_distance; x++) {
                list.addObject(new LocationTag(loc_1.getWorld(),
                        x,
                        loc_1.getBlockY(),
                        loc_1.getBlockZ()));

                list.addObject(new LocationTag(loc_1.getWorld(),
                        x,
                        loc_1.getBlockY(),
                        loc_2.getBlockZ()));

                list.addObject(new LocationTag(loc_1.getWorld(),
                        x,
                        loc_2.getBlockY(),
                        loc_2.getBlockZ()));

                list.addObject(new LocationTag(loc_1.getWorld(),
                        x,
                        loc_2.getBlockY(),
                        loc_1.getBlockZ()));
                index++;
                if (index > max) {
                    return list;
                }
            }

            for (int z = loc_1.getBlockZ(); z < loc_1.getBlockZ() + z_distance; z++) {
                list.addObject(new LocationTag(loc_1.getWorld(),
                        loc_1.getBlockX(),
                        loc_1.getBlockY(),
                        z));

                list.addObject(new LocationTag(loc_1.getWorld(),
                        loc_2.getBlockX(),
                        loc_2.getBlockY(),
                        z));

                list.addObject(new LocationTag(loc_1.getWorld(),
                        loc_1.getBlockX(),
                        loc_2.getBlockY(),
                        z));

                list.addObject(new LocationTag(loc_1.getWorld(),
                        loc_2.getBlockX(),
                        loc_1.getBlockY(),
                        z));
                index++;
                if (index > max) {
                    return list;
                }
            }
            list.addObject(pair.high);
        }
        return list;
    }

    public ListTag getBlocks(Attribute attribute) {
        return getBlocks(null, attribute);
    }

    private boolean matchesMaterialList(Location loc, List<MaterialTag> materials, Attribute attribute) {
        if (materials == null) {
            return true;
        }
        MaterialTag mat = new MaterialTag(new LocationTag(loc).getBlockForTag(attribute));
        for (MaterialTag material : materials) {
            if (mat.equals(material) || mat.getMaterial() == material.getMaterial()) {
                return true;
            }
        }
        return false;
    }

    public ListTag getBlocks(List<MaterialTag> materials, Attribute attribute) {
        List<LocationTag> locs = getBlocks_internal(materials, attribute);
        ListTag list = new ListTag();
        for (LocationTag loc : locs) {
            list.addObject(loc);
        }
        return list;
    }

    public List<LocationTag> getBlocks_internal(List<MaterialTag> materials, Attribute attribute) {
        if (materials == null) {
            return getBlockLocationsUnfiltered();
        }
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
                    for (int z = 0; z != z_distance + 1; z++) {
                        loc = new LocationTag(loc_1.clone().add(x, y, z));
                        if (loc.getY() < 0 || loc.getY() > 255) {
                            continue;
                        }
                        if (matchesMaterialList(loc, materials, attribute)) {
                            list.add(loc);
                        }
                        index++;
                        if (index > max) {
                            return list;
                        }
                    }
                }
            }

        }
        return list;
    }

    public List<LocationTag> getBlockLocationsUnfiltered() {
        int max = Settings.blockTagsMaxBlocks();
        LocationTag loc;
        List<LocationTag> list = new ArrayList<>();
        int index = 0;

        for (LocationPair pair : pairs) {
            LocationTag loc_1 = pair.low;
            int y_distance = pair.yDistance();
            int z_distance = pair.zDistance();
            int x_distance = pair.xDistance();
            for (int x = 0; x <= x_distance; x++) {
                for (int z = 0; z <= z_distance; z++) {
                    for (int y = 0; y <= y_distance; y++) {
                        loc = new LocationTag(loc_1.clone().add(x, y, z));
                        list.add(loc);
                        index++;
                        if (index > max) {
                            return list;
                        }
                    }
                }
            }
        }
        return list;
    }

    public ListTag getSpawnableBlocks(Attribute attribute) {
        return getSpawnableBlocks(null, attribute);
    }

    /**
     * Returns a ListTag of LocationTags with 2 vertical blocks of air
     * that are safe for players and similar entities to spawn in,
     * but ignoring blocks in midair
     *
     * @return The ListTag
     */

    public ListTag getSpawnableBlocks(List<MaterialTag> mats, Attribute attribute) {
        int max = Settings.blockTagsMaxBlocks();
        LocationTag loc;
        ListTag list = new ListTag();
        int index = 0;

        BlockHelper blockHelper = NMSHandler.getBlockHelper();
        for (LocationPair pair : pairs) {

            LocationTag loc_1 = pair.low;
            int y_distance = pair.yDistance();
            int z_distance = pair.zDistance();
            int x_distance = pair.xDistance();

            for (int x = 0; x != x_distance + 1; x++) {
                for (int y = 0; y != y_distance + 1; y++) {
                    for (int z = 0; z != z_distance + 1; z++) {
                        loc = new LocationTag(loc_1.clone()
                                .add(x, y, z));

                        if (blockHelper.isSafeBlock(loc.getBlockTypeForTag(attribute))
                                && blockHelper.isSafeBlock(new LocationTag(loc.clone().add(0, 1, 0)).getBlockTypeForTag(attribute))
                                && new LocationTag(loc.clone().add(0, -1, 0)).getBlockTypeForTag(attribute).isSolid()
                                && matchesMaterialList(loc.clone().add(0, -1, 0), mats, attribute)) {
                            // Get the center of the block, so the entity won't suffocate
                            // inside the edges for a couple of seconds
                            loc.add(0.5, 0, 0.5);
                            list.addObject(loc);
                        }
                        index++;
                        if (index > max) {
                            return list;
                        }
                    }
                }
            }
        }

        return list;
    }

    public World getWorld() {
        if (pairs.isEmpty()) {
            return null;
        }
        return pairs.get(0).high.getWorld();
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

    ///////////////////
    // Notable
    ///////////////////

    @Override
    public boolean isUnique() {
        return NotableManager.isSaved(this);
    }

    @Override
    @Note("Cuboids")
    public String getSaveObject() {
        return identifyFull().substring(3);
    }

    @Override
    public void makeUnique(String id) {
        NotableManager.saveAs(this, id);
    }

    @Override
    public void forget() {
        NotableManager.remove(this);
    }

    /////////////////////
    // ObjectTag
    ////////////////////

    String prefix = "Cuboid";

    @Override
    public String getObjectType() {
        return "cuboid";
    }

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
        if (isUnique()) {
            return "cu@" + NotableManager.getSavedId(this) + " <GR>(" + identifyFull() + ")";
        }
        else {
            return identifyFull();
        }
    }

    @Override
    public String identify() {
        if (isUnique()) {
            return "cu@" + NotableManager.getSavedId(this);
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

    /////////////////////
    // ObjectTag Tag Management
    /////////////////////

    public static void registerTags() {

        // <--[tag]
        // @attribute <CuboidTag.blocks[<material>|...]>
        // @returns ListTag(LocationTag)
        // @description
        // Returns each block location within the CuboidTag.
        // Optionally, specify a list of materials to only return locations
        // with that block type.
        // -->
        registerTag("blocks", (attribute, cuboid) -> {
            if (attribute.hasContext(1)) {
                return new ListTag(cuboid.getBlocks(ListTag.valueOf(attribute.getContext(1), attribute.context).filter(MaterialTag.class, attribute.context), attribute));
            }
            else {
                return new ListTag(cuboid.getBlocks(attribute));
            }
        }, "get_blocks");

        // <--[tag]
        // @attribute <CuboidTag.members_size>
        // @returns ElementTag(Number)
        // @description
        // Returns the number of cuboids defined in the CuboidTag.
        // -->
        registerTag("members_size", (attribute, cuboid) -> {
            return new ElementTag(cuboid.pairs.size());
        });

        // <--[tag]
        // @attribute <CuboidTag.spawnable_blocks[<Material>|...]>
        // @returns ListTag(LocationTag)
        // @description
        // Returns each LocationTag within the CuboidTag that is
        // safe for players or similar entities to spawn in.
        // Optionally, specify a list of materials to only return locations
        // with that block type.
        // -->
        registerTag("spawnable_blocks", (attribute, cuboid) -> {
            if (attribute.hasContext(1)) {
                return new ListTag(cuboid.getSpawnableBlocks(ListTag.valueOf(attribute.getContext(1), attribute.context).filter(MaterialTag.class, attribute.context), attribute));
            }
            else {
                return new ListTag(cuboid.getSpawnableBlocks(attribute));
            }
        }, "get_spawnable_blocks");

        // <--[tag]
        // @attribute <CuboidTag.shell>
        // @returns ListTag(LocationTag)
        // @description
        // Returns each block location on the shell of the CuboidTag.
        // -->
        registerTag("shell", (attribute, cuboid) -> {
            return cuboid.getShell();
        });

        // <--[tag]
        // @attribute <CuboidTag.outline>
        // @returns ListTag(LocationTag)
        // @description
        // Returns each block location on the outline of the CuboidTag.
        // -->
        registerTag("outline", (attribute, cuboid) -> {
            return cuboid.getOutline();
        }, "get_outline");

        // <--[tag]
        // @attribute <CuboidTag.intersects[<cuboid>]>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether this cuboid and another intersect.
        // -->
        registerTag("intersects", (attribute, cuboid) -> {
            if (!attribute.hasContext(1)) {
                attribute.echoError("The tag CuboidTag.intersects[...] must have a value.");
                return null;
            }
            CuboidTag cub2 = CuboidTag.valueOf(attribute.getContext(1));
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
        // @attribute <CuboidTag.contains_location[<location>]>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether this cuboid contains a location.
        // -->
        registerTag("contains_location", (attribute, cuboid) -> {
            if (!attribute.hasContext(1)) {
                attribute.echoError("The tag CuboidTag.contains_location[...] must have a value.");
                return null;
            }
            LocationTag loc = LocationTag.valueOf(attribute.getContext(1));
            return new ElementTag(cuboid.isInsideCuboid(loc));
        });

        // <--[tag]
        // @attribute <CuboidTag.is_within[<cuboid>]>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether this cuboid is fully inside another cuboid.
        // -->
        registerTag("is_within", (attribute, cuboid) -> {
            if (!attribute.hasContext(1)) {
                attribute.echoError("The tag CuboidTag.is_within[...] must have a value.");
                return null;
            }
            CuboidTag cub2 = CuboidTag.valueOf(attribute.getContext(1));
            if (cub2 != null) {
                boolean contains = true;
                for (LocationPair pair2 : cuboid.pairs) {
                    boolean contained = false;
                    for (LocationPair pair : cub2.pairs) {
                        if (!pair.low.getWorld().getName().equalsIgnoreCase(pair2.low.getWorld().getName())) {
                            if (com.denizenscript.denizencore.utilities.debugging.Debug.verbose) {
                                Debug.log("Worlds don't match!");
                            }
                            return new ElementTag("false");
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
        // @attribute <CuboidTag.list_members>
        // @returns ListTag(CuboidTag)
        // @description
        // Returns a list of all sub-cuboids in this CuboidTag (for cuboids that contain multiple sub-cuboids).
        // -->
        registerTag("list_members", (attribute, cuboid) -> {
            List<LocationPair> pairs = cuboid.pairs;
            ListTag list = new ListTag();
            for (LocationPair pair : pairs) {
                list.addObject(new CuboidTag(pair.low, pair.high));
            }
            return list;
        });

        // <--[tag]
        // @attribute <CuboidTag.get[<index>]>
        // @returns CuboidTag
        // @description
        // Returns a cuboid representing the one component of this cuboid (for cuboids that contain multiple sub-cuboids).
        // -->
        registerTag("get", (attribute, cuboid) -> {
            if (!attribute.hasContext(1)) {
                attribute.echoError("The tag CuboidTag.get[...] must have a value.");
                return null;
            }
            else {
                int member = attribute.getIntContext(1);
                if (member < 1) {
                    member = 1;
                }
                if (member > cuboid.pairs.size()) {
                    member = cuboid.pairs.size();
                }
                LocationPair pair = cuboid.pairs.get(member - 1);
                return new CuboidTag(pair.low, pair.high);
            }
        }, "member", "get_member");

        // <--[tag]
        // @attribute <CuboidTag.set[<cuboid>].at[<index>]>
        // @returns CuboidTag
        // @description
        // Returns a modified copy of this cuboid, with the specific sub-cuboid index changed to hold the input cuboid.
        // -->
        registerTag("set", (attribute, cuboid) -> {
            if (!attribute.hasContext(1)) {
                attribute.echoError("The tag CuboidTag.set[...] must have a value.");
                return null;
            }
            else {
                CuboidTag subCuboid = CuboidTag.valueOf(attribute.getContext(1));
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
                cloned.pairs.set(member - 1, new LocationPair(pair.low, pair.high));
                return cloned;
            }
        });

        // <--[tag]
        // @attribute <CuboidTag.add_member[<cuboid>]>
        // @returns CuboidTag
        // @mechanism CuboidTag.add_member
        // @description
        // Returns a modified copy of this cuboid, with the input cuboid added at the end.
        // -->
        registerTag("add_member", (attribute, cuboid) -> {
            if (!attribute.hasContext(1)) {
                attribute.echoError("The tag CuboidTag.add_member[...] must have a value.");
                return null;
            }
            cuboid = cuboid.clone();
            CuboidTag subCuboid = CuboidTag.valueOf(attribute.getContext(1));
            int member = cuboid.pairs.size() + 1;

            // <--[tag]
            // @attribute <CuboidTag.add_member[<cuboid>].at[<index>]>
            // @returns CuboidTag
            // @mechanism CuboidTag.add_member
            // @description
            // Returns a modified copy of this cuboid, with the input cuboid added at the specified index.
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
            LocationPair pair = subCuboid.pairs.get(0);
            cuboid.pairs.add(member - 1, new LocationPair(pair.low, pair.high));
            return cuboid;
        });

        // <--[tag]
        // @attribute <CuboidTag.remove_member[<#>]>
        // @returns CuboidTag
        // @mechanism CuboidTag.remove_member
        // @description
        // Returns a modified copy of this cuboid, with member at the input index removed.
        // -->
        registerTag("remove_member", (attribute, cuboid) -> {
            if (!attribute.hasContext(1)) {
                attribute.echoError("The tag CuboidTag.remove_member[...] must have a value.");
                return null;
            }
            cuboid = cuboid.clone();
            int member = attribute.getIntContext(1);
            if (member < 1) {
                member = 1;
            }
            if (member > cuboid.pairs.size() + 1) {
                member = cuboid.pairs.size() + 1;
            }
            cuboid.pairs.remove(member - 1);
            return cuboid;
        });

        // <--[tag]
        // @attribute <CuboidTag.center>
        // @returns LocationTag
        // @description
        // Returns the location of the exact center of the cuboid.
        // -->
        registerTag("center", (attribute, cuboid) -> {
            LocationPair pair;
            if (!attribute.hasContext(1)) {
                pair = cuboid.pairs.get(0);
            }
            else {
                int member = attribute.getIntContext(1);
                if (member < 1) {
                    member = 1;
                }
                if (member > cuboid.pairs.size()) {
                    member = cuboid.pairs.size();
                }
                pair = cuboid.pairs.get(member - 1);
            }
            Location base = pair.high.clone().add(pair.low.clone()).add(1.0, 1.0, 1.0);
            base.setX(base.getX() / 2.0);
            base.setY(base.getY() / 2.0);
            base.setZ(base.getZ() / 2.0);
            return new LocationTag(base);
        });

        // <--[tag]
        // @attribute <CuboidTag.volume>
        // @returns ElementTag(Number)
        // @description
        // Returns the volume of the cuboid.
        // Effectively equivalent to: (size.x * size.y * size.z).
        // -->
        registerTag("volume", (attribute, cuboid) -> {
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
        // -->
        registerTag("size", (attribute, cuboid) -> {
            LocationPair pair;
            if (!attribute.hasContext(1)) {
                pair = cuboid.pairs.get(0);
            }
            else {
                int member = attribute.getIntContext(1);
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
        // -->
        registerTag("max", (attribute, cuboid) -> {
            if (!attribute.hasContext(1)) {
                return cuboid.pairs.get(0).high;
            }
            else {
                int member = attribute.getIntContext(1);
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
        // -->
        registerTag("min", (attribute, cuboid) -> {
            if (!attribute.hasContext(1)) {
                return cuboid.pairs.get(0).low;
            }
            else {
                int member = attribute.getIntContext(1);
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
        // @attribute <CuboidTag.include[<location>/<cuboid>]>
        // @returns CuboidTag
        // @description
        // Expands the first member of the CuboidTag to contain the given location (or entire cuboid), and returns the expanded cuboid.
        // -->
        registerTag("include", (attribute, cuboid) -> {
            if (!attribute.hasContext(1)) {
                attribute.echoError("The tag CuboidTag.include[...] must have a value.");
                return null;
            }
            CuboidTag newCuboid = CuboidTag.valueOf(attribute.getContext(1), CoreUtilities.noDebugContext);
            if (newCuboid != null) {
                return cuboid.including(newCuboid.getLow(0)).including(newCuboid.getHigh(0));
            }
            LocationTag loc = LocationTag.valueOf(attribute.getContext(1));
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
        registerTag("include_x", (attribute, cuboid) -> {
            cuboid = cuboid.clone();
            if (!attribute.hasContext(1)) {
                attribute.echoError("The tag CuboidTag.include_x[...] must have a value.");
                return null;
            }
            double x = attribute.getDoubleContext(1);
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
        registerTag("include_y", (attribute, cuboid) -> {
            cuboid = cuboid.clone();
            if (!attribute.hasContext(1)) {
                attribute.echoError("The tag CuboidTag.include_y[...] must have a value.");
                return null;
            }
            double y = attribute.getDoubleContext(1);
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
        registerTag("include_z", (attribute, cuboid) -> {
            cuboid = cuboid.clone();
            if (!attribute.hasContext(1)) {
                attribute.echoError("The tag CuboidTag.include_z[...] must have a value.");
                return null;
            }
            double z = attribute.getDoubleContext(1);
            if (z < cuboid.pairs.get(0).low.getZ()) {
                cuboid.pairs.get(0).low = new LocationTag(cuboid.pairs.get(0).low.getWorld(), cuboid.pairs.get(0).low.getX(), cuboid.pairs.get(0).low.getY(), z);
            }
            if (z > cuboid.pairs.get(0).high.getZ()) {
                cuboid.pairs.get(0).high = new LocationTag(cuboid.pairs.get(0).high.getWorld(), cuboid.pairs.get(0).high.getX(), cuboid.pairs.get(0).high.getY(), z);
            }
            return cuboid;
        });

        // <--[tag]
        // @attribute <CuboidTag.with_world[<world>]>
        // @returns CuboidTag
        // @description
        // Changes the CuboidTag to have the given world, and returns the changed cuboid.
        // -->
        registerTag("with_world", (attribute, cuboid) -> {
            if (!attribute.hasContext(1)) {
                attribute.echoError("The tag CuboidTag.with_world[...] must have a value.");
                return null;
            }
            WorldTag world = WorldTag.valueOf(attribute.getContext(1), attribute.context);
            if (world == null) {
                attribute.echoError("World '" + attribute.getContext(1) + "' does not exist.");
                return null;
            }
            CuboidTag newCuboid = cuboid.clone();
            for (LocationPair pair : newCuboid.pairs) {
                pair.low.setWorld(world.getWorld());
                pair.high.setWorld(world.getWorld());
            }
            return newCuboid;
        });

        // <--[tag]
        // @attribute <CuboidTag.with_min[<location>]>
        // @returns CuboidTag
        // @description
        // Changes the first member of the CuboidTag to have the given minimum location, and returns the changed cuboid.
        // If values in the new min are higher than the existing max, the output max will contain the new min values,
        // and the output min will contain the old max values.
        // Note that this is equivalent to constructing a cuboid with the input value and the original cuboids max value.
        // -->
        registerTag("with_min", (attribute, cuboid) -> {
            if (!attribute.hasContext(1)) {
                attribute.echoError("The tag CuboidTag.with_min[...] must have a value.");
                return null;
            }
            LocationTag location = LocationTag.valueOf(attribute.getContext(1));
            return new CuboidTag(location, cuboid.pairs.get(0).high);
        });

        // <--[tag]
        // @attribute <CuboidTag.with_max[<location>]>
        // @returns CuboidTag
        // @description
        // Changes the first member of the CuboidTag to have the given maximum location, and returns the changed cuboid.
        // If values in the new max are lower than the existing min, the output min will contain the new max values,
        // and the output max will contain the old min values.
        // Note that this is equivalent to constructing a cuboid with the input value and the original cuboids min value.
        // -->
        registerTag("with_max", (attribute, cuboid) -> {
            if (!attribute.hasContext(1)) {
                attribute.echoError("The tag CuboidTag.with_max[...] must have a value.");
                return null;
            }
            LocationTag location = LocationTag.valueOf(attribute.getContext(1));
            return new CuboidTag(location, cuboid.pairs.get(0).low);
        });

        // <--[tag]
        // @attribute <CuboidTag.expand[<location>]>
        // @returns CuboidTag
        // @description
        // Changes the first member of the CuboidTag to be expanded by the given amount, and returns the changed cuboid.
        // This will decrease the min coordinates by the given vector location, and increase the max coordinates by it.
        // Supplying a negative input will therefore contract the cuboid.
        // Note that you can also specify a single number to expand all coordinates by the same amount (equivalent to specifying a location that is that value on X, Y, and Z).
        // -->
        registerTag("expand", (attribute, cuboid) -> {
            if (!attribute.hasContext(1)) {
                attribute.echoError("The tag CuboidTag.expand[...] must have a value.");
                return null;
            }
            Vector expandBy;
            if (ArgumentHelper.matchesInteger(attribute.getContext(1))) {
                int val = attribute.getIntContext(1);
                expandBy = new Vector(val, val, val);
            }
            else {
                expandBy = LocationTag.valueOf(attribute.getContext(1)).toVector();
            }
            LocationPair pair = cuboid.pairs.get(0);
            return new CuboidTag(pair.low.clone().subtract(expandBy), pair.high.clone().add(expandBy));
        });

        // <--[tag]
        // @attribute <CuboidTag.list_players>
        // @returns ListTag(PlayerTag)
        // @description
        // Gets a list of all players currently within the CuboidTag.
        // -->
        registerTag("list_players", (attribute, cuboid) -> {
            ArrayList<PlayerTag> players = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (cuboid.isInsideCuboid(player.getLocation())) {
                    players.add(PlayerTag.mirrorBukkitPlayer(player));
                }
            }
            return new ListTag(players);
        });

        // <--[tag]
        // @attribute <CuboidTag.list_npcs>
        // @returns ListTag(NPCTag)
        // @description
        // Gets a list of all NPCs currently within the CuboidTag.
        // -->
        if (Depends.citizens != null) {
            registerTag("list_npcs", (attribute, cuboid) -> {
                ArrayList<NPCTag> npcs = new ArrayList<>();
                for (NPC npc : CitizensAPI.getNPCRegistry()) {
                    NPCTag dnpc = NPCTag.mirrorCitizensNPC(npc);
                    if (cuboid.isInsideCuboid(dnpc.getLocation())) {
                        npcs.add(dnpc);
                    }
                }
                return new ListTag(npcs);
            });
        }

        // <--[tag]
        // @attribute <CuboidTag.list_entities[<entity>|...]>
        // @returns ListTag(EntityTag)
        // @description
        // Gets a list of all entities currently within the CuboidTag, with
        // an optional search parameter for the entity type.
        // -->
        registerTag("list_entities", (attribute, cuboid) -> {
            ArrayList<EntityTag> entities = new ArrayList<>();
            ListTag types = new ListTag();
            if (attribute.hasContext(1)) {
                types = ListTag.valueOf(attribute.getContext(1), attribute.context);
            }
            for (Entity ent : new WorldTag(cuboid.getWorld()).getEntitiesForTag()) {
                EntityTag current = new EntityTag(ent);
                if (cuboid.isInsideCuboid(ent.getLocation())) {
                    if (!types.isEmpty()) {
                        for (String type : types) {
                            if (current.identifySimpleType().equalsIgnoreCase(type)) {
                                entities.add(current);
                                break;
                            }
                        }
                    }
                    else {
                        entities.add(new EntityTag(ent));
                    }
                }
            }
            return new ListTag(entities);
        });

        // <--[tag]
        // @attribute <CuboidTag.list_living_entities>
        // @returns ListTag(EntityTag)
        // @description
        // Gets a list of all living entities currently within the CuboidTag.
        // This includes Players, mobs, NPCs, etc., but excludes dropped items, experience orbs, etc.
        // -->
        registerTag("list_living_entities", (attribute, cuboid) -> {
            ArrayList<EntityTag> entities = new ArrayList<>();
            for (Entity ent : cuboid.getWorld().getLivingEntities()) {
                if (cuboid.isInsideCuboid(ent.getLocation()) && !EntityTag.isCitizensNPC(ent)) {
                    entities.add(new EntityTag(ent));
                }
            }
            return new ListTag(entities);
        });

        // <--[tag]
        // @attribute <CuboidTag.list_chunks>
        // @returns ListTag(ChunkTag)
        // @description
        // Gets a list of all chunks entirely within the CuboidTag (ignoring the Y axis).
        // -->
        registerTag("list_chunks", (attribute, cuboid) -> {
            ListTag chunks = new ListTag();
            for (LocationPair pair : cuboid.pairs) {
                int minY = pair.low.getBlockY();
                ChunkTag minChunk = new ChunkTag(pair.low);
                int minX = minChunk.getX();
                int minZ = minChunk.getZ();
                if (!cuboid.isInsideCuboid(new Location(cuboid.getWorld(), minChunk.getX() * 16, minY, minChunk.getZ() * 16))) {
                    minX++;
                    minZ++;
                }
                ChunkTag maxChunk = new ChunkTag(pair.high);
                int maxX = maxChunk.getX();
                int maxZ = maxChunk.getZ();
                if (cuboid.isInsideCuboid(new Location(cuboid.getWorld(), maxChunk.getX() * 16 + 15, minY, maxChunk.getZ() * 16 + 15))) {
                    maxX++;
                    maxZ++;
                }
                for (int x = minX; x < maxX; x++) {
                    for (int z = minZ; z < maxZ; z++) {
                        chunks.addObject(new ChunkTag(new WorldTag(cuboid.getWorld()), x, z));
                    }
                }
            }
            return chunks.deduplicate();
        });

        // <--[tag]
        // @attribute <CuboidTag.list_partial_chunks>
        // @returns ListTag(ChunkTag)
        // @description
        // Gets a list of all chunks partially or entirely within the CuboidTag.
        // -->
        registerTag("list_partial_chunks", (attribute, cuboid) -> {
            ListTag chunks = new ListTag();
            for (LocationPair pair : cuboid.pairs) {
                ChunkTag minChunk = new ChunkTag(pair.low);
                ChunkTag maxChunk = new ChunkTag(pair.high);
                for (int x = minChunk.getX(); x <= maxChunk.getX(); x++) {
                    for (int z = minChunk.getZ(); z <= maxChunk.getZ(); z++) {
                        chunks.addObject(new ChunkTag(new WorldTag(cuboid.getWorld()), x, z));
                    }
                }
            }
            return chunks;
        });

        // <--[tag]
        // @attribute <CuboidTag.notable_name>
        // @returns ElementTag
        // @description
        // Gets the name of a Notable CuboidTag. If the cuboid isn't noted,
        // this is null.
        // -->
        registerTag("notable_name", (attribute, cuboid) -> {
            String notname = NotableManager.getSavedId(cuboid);
            if (notname == null) {
                return null;
            }
            return new ElementTag(notname);
        });

        // <--[tag]
        // @attribute <CuboidTag.full>
        // @returns ElementTag
        // @group conversion
        // @description
        // Returns a full reusable identification for this cuboid, without applying the notable name or other special-case formats.
        // -->
        registerTag("full", (attribute, cuboid) -> {
            return new ElementTag(cuboid.identifyFull());
        });

        // <--[tag]
        // @attribute <CuboidTag.type>
        // @returns ElementTag
        // @description
        // Always returns 'Cuboid' for CuboidTag objects. All objects fetchable by the Object Fetcher will return the
        // type of object that is fulfilling this attribute.
        // -->
        registerTag("type", (attribute, cuboid) -> {
            return new ElementTag("Cuboid");
        });
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

    public static void registerTag(String name, TagRunnable.ObjectInterface<CuboidTag> runnable, String... variants) {
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
            String value = mechanism.getValue().asString();
            int comma = value.indexOf(',');
            int member = 1;
            if (comma > 0 && !value.startsWith("cu@")) {
                member = new ElementTag(value.substring(0, comma)).asInt();
                value = value.substring(comma + 1);
            }
            CuboidTag subCuboid = CuboidTag.valueOf(value);
            if (member < 1) {
                member = 1;
            }
            if (member > pairs.size()) {
                member = pairs.size();
            }
            LocationPair pair = subCuboid.pairs.get(0);
            pairs.set(member - 1, new LocationPair(pair.low, pair.high));
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
            String value = mechanism.getValue().asString();
            int comma = value.indexOf(',');
            int member = pairs.size() + 1;
            if (comma > 0 && !value.startsWith("cu@")) {
                member = new ElementTag(value.substring(0, comma)).asInt();
                value = value.substring(comma + 1);
            }
            CuboidTag subCuboid = CuboidTag.valueOf(value);
            if (member < 1) {
                member = 1;
            }
            if (member > pairs.size()) {
                member = pairs.size();
            }
            LocationPair pair = subCuboid.pairs.get(0);
            pairs.add(member - 1, new LocationPair(pair.low, pair.high));
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
            int member = mechanism.getValue().asInt();
            if (member < 1) {
                member = 1;
            }
            if (member > pairs.size()) {
                member = pairs.size();
            }
            pairs.remove(member - 1);
        }

        CoreUtilities.autoPropertyMechanism(this, mechanism);

    }
}
