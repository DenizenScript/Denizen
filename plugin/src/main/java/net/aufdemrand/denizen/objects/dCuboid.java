package net.aufdemrand.denizen.objects;

import net.aufdemrand.denizen.Settings;
import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.nms.interfaces.BlockData;
import net.aufdemrand.denizen.nms.interfaces.BlockHelper;
import net.aufdemrand.denizen.objects.notable.NotableManager;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.depends.Depends;
import net.aufdemrand.denizencore.objects.*;
import net.aufdemrand.denizencore.objects.notable.Notable;
import net.aufdemrand.denizencore.objects.notable.Note;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.objects.properties.PropertyParser;
import net.aufdemrand.denizencore.tags.Attribute;
import net.aufdemrand.denizencore.tags.TagContext;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class dCuboid implements dObject, Cloneable, Notable, Adjustable {

    // Cloning
    @Override
    public dCuboid clone() throws CloneNotSupportedException {
        return (dCuboid) super.clone();
    }

    /////////////////////
    //   STATIC METHODS
    /////////////////

    public static List<dCuboid> getNotableCuboidsContaining(Location location) {
        List<dCuboid> cuboids = new ArrayList<dCuboid>();
        for (dCuboid cuboid : NotableManager.getAllType(dCuboid.class)) {
            if (cuboid.isInsideCuboid(location)) {
                cuboids.add(cuboid);
            }
        }

        return cuboids;
    }


    //////////////////
    //    OBJECT FETCHER
    ////////////////

    public static dCuboid valueOf(String string) {
        return valueOf(string, null);
    }

    final static Pattern cuboid_by_saved = Pattern.compile("(cu@)?(.+)");

    /**
     * Gets a Location Object from a string form of id,x,y,z,world
     * or a dScript argument (location:)x,y,z,world. If including an Id,
     * this location will persist and can be recalled at any time.
     *
     * @param string the string or dScript argument String
     * @return a Location, or null if incorrectly formatted
     */
    @Fetchable("cu")
    public static dCuboid valueOf(String string, TagContext context) {
        if (string == null) {
            return null;
        }

        ////////
        // Match location formats

        // Split values
        dList positions = dList.valueOf(string.replace("cu@", ""));

        // If there's a | and the first two points look like locations, assume it's a valid location-list constructor.
        if (positions.size() > 1
                && dLocation.matches(positions.get(0))
                && dLocation.matches(positions.get(1))) {
            if (positions.size() % 2 != 0) {
                if (context == null || context.debug) {
                    dB.echoError("valueOf dCuboid returning null (Uneven number of locations): '" + string + "'.");
                }
                return null;
            }

            // Form a cuboid to add to
            dCuboid toReturn = new dCuboid();

            // Add to the cuboid
            for (int i = 0; i < positions.size(); i += 2) {
                dLocation pos_1 = dLocation.valueOf(positions.get(i));
                dLocation pos_2 = dLocation.valueOf(positions.get(i + 1));

                // Must be valid locations
                if (pos_1 == null || pos_2 == null) {
                    if (context == null || context.debug) {
                        dB.echoError("valueOf in dCuboid returning null (null locations): '" + string + "'.");
                    }
                    return null;
                }
                // Must have valid worlds
                if (pos_1.getWorld() == null || pos_2.getWorld() == null) {
                    if (context == null || context.debug) {
                        dB.echoError("valueOf in dCuboid returning null (null worlds): '" + string + "'.");
                    }
                    return null;
                }
                toReturn.addPair(pos_1, pos_2);
            }

            // Ensure validity and return the created cuboid
            if (toReturn.pairs.size() > 0) {
                return toReturn;
            }
        }

        ////////
        // Match @object format for Notable dCuboids
        Matcher m;

        m = cuboid_by_saved.matcher(string);

        if (m.matches() && NotableManager.isType(m.group(2), dCuboid.class)) {
            return (dCuboid) NotableManager.getSavedObject(m.group(2));
        }

        if (context == null || context.debug) {
            dB.echoError("valueOf dCuboid returning null: " + string);
        }

        return null;
    }

    // The regex below: optional-"|" + "<#.#>," x3 + <text> + "|" + "<#.#>," x3 + <text> -- repeating
    final static Pattern cuboidLocations =
            Pattern.compile("(\\|?([-\\d\\.]+,){3}[\\w\\s]+\\|([-\\d\\.]+,){3}[\\w\\s]+)+",
                    Pattern.CASE_INSENSITIVE);


    public static boolean matches(String string) {
        // Starts with cu@? Assume match.
        if (CoreUtilities.toLowerCase(string).startsWith("cu@")) {
            return true;
        }

        Matcher m;

        // Check for named cuboid: cu@notable_cuboid
        m = cuboid_by_saved.matcher(string);
        if (m.matches() && NotableManager.isType(m.group(2), dCuboid.class)) {
            return true;
        }

        // Check for standard cuboid format: cu@x,y,z,world|x,y,z,world|...
        m = cuboidLocations.matcher(string.replace("cu@", ""));
        return m.matches();
    }


    ///////////////
    //  LocationPairs
    /////////////


    public static class LocationPair {
        public dLocation low;
        public dLocation high;
        dLocation point_1;
        dLocation point_2;
        int x_distance;
        int y_distance;
        int z_distance;

        public LocationPair(dLocation point_1, dLocation point_2) {
            this.point_1 = point_1;
            this.point_2 = point_2;
            regenerate();
        }

        public void changePoint(int number, dLocation point) {
            if (number == 1) {
                this.point_1 = point;
            }
            else if (number == 2) {
                this.point_2 = point;
            }
            regenerate();
        }

        public void regenerate() {
            World world = point_1.getWorld();

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
            low = new dLocation(world, x_low, y_low, z_low);
            high = new dLocation(world, x_high, y_high, z_high);
            generateDistances();
        }

        public void generateDistances() {
            x_distance = high.getBlockX() - low.getBlockX();
            y_distance = high.getBlockY() - low.getBlockY();
            z_distance = high.getBlockZ() - low.getBlockZ();
        }
    }


    ///////////////////
    //  Constructors/Instance Methods
    //////////////////

    // Location Pairs (low, high) that make up the dCuboid
    public List<LocationPair> pairs = new ArrayList<LocationPair>();

    // Only put dMaterials in filter.
    ArrayList<dObject> filter = new ArrayList<dObject>();

    /**
     * Construct the cuboid without adding pairs
     * ONLY use this if addPair will be called immediately after!
     */
    public dCuboid() {
    }

    public dCuboid(Location point_1, Location point_2) {
        addPair(point_1, point_2);
    }


    public void addPair(Location point_1, Location point_2) {
        if (point_1.getWorld() != point_2.getWorld()) {
            dB.echoError("Tried to make cross-world cuboid!");
            return;
        }
        if (pairs.size() > 0 && point_1.getWorld() != getWorld()) {
            dB.echoError("Tried to make cross-world cuboid set!");
            return;
        }
        // Make a new pair
        LocationPair pair = new LocationPair(new dLocation(point_1), new dLocation(point_2));
        // Add it to the Cuboid pairs list
        pairs.add(pair);
    }


    public boolean isInsideCuboid(Location location) {
        for (LocationPair pair : pairs) {
            if (!location.getWorld().equals(pair.low.getWorld())) {
                continue;
            }
            if (!Utilities.isBetween(pair.low.getBlockX(), pair.high.getBlockX() + 1, location.getBlockX())) {
                continue;
            }
            if (!Utilities.isBetween(pair.low.getBlockY(), pair.high.getBlockY() + 1, location.getBlockY())) {
                continue;
            }
            if (Utilities.isBetween(pair.low.getBlockZ(), pair.high.getBlockZ() + 1, location.getBlockZ())) {
                return true;
            }
        }

        // Does not match any of the pairs
        return false;
    }


    public dCuboid addBlocksToFilter(List<dMaterial> addl) {
        filter.addAll(addl);
        return this;
    }


    public dCuboid removeBlocksFromFilter(List<dMaterial> addl) {
        filter.removeAll(addl);
        return this;
    }


    public dCuboid removeFilter() {
        filter.clear();
        return this;
    }


    public dCuboid setAsFilter(List<dMaterial> list) {
        filter.clear();
        filter.addAll(list);
        return this;
    }

    public dList getOutline() {

        //    +-----2
        //   /|    /|
        //  +-----+ |
        //  | +---|-+
        //  |/    |/
        //  1-----+
        int max = Settings.blockTagsMaxBlocks();
        int index = 0;

        dList list = new dList();

        for (LocationPair pair : pairs) {

            dLocation loc_1 = pair.low;
            dLocation loc_2 = pair.high;
            int y_distance = pair.y_distance;
            int z_distance = pair.z_distance;
            int x_distance = pair.x_distance;

            for (int y = loc_1.getBlockY(); y < loc_1.getBlockY() + y_distance; y++) {
                list.add(new dLocation(loc_1.getWorld(),
                        loc_1.getBlockX(),
                        y,
                        loc_1.getBlockZ()).identify());

                list.add(new dLocation(loc_1.getWorld(),
                        loc_2.getBlockX(),
                        y,
                        loc_2.getBlockZ()).identify());

                list.add(new dLocation(loc_1.getWorld(),
                        loc_1.getBlockX(),
                        y,
                        loc_2.getBlockZ()).identify());

                list.add(new dLocation(loc_1.getWorld(),
                        loc_2.getBlockX(),
                        y,
                        loc_1.getBlockZ()).identify());
                index++;
                if (index > max) {
                    return list;
                }
            }

            for (int x = loc_1.getBlockX(); x < loc_1.getBlockX() + x_distance; x++) {
                list.add(new dLocation(loc_1.getWorld(),
                        x,
                        loc_1.getBlockY(),
                        loc_1.getBlockZ()).identify());

                list.add(new dLocation(loc_1.getWorld(),
                        x,
                        loc_1.getBlockY(),
                        loc_2.getBlockZ()).identify());

                list.add(new dLocation(loc_1.getWorld(),
                        x,
                        loc_2.getBlockY(),
                        loc_2.getBlockZ()).identify());

                list.add(new dLocation(loc_1.getWorld(),
                        x,
                        loc_2.getBlockY(),
                        loc_1.getBlockZ()).identify());
                index++;
                if (index > max) {
                    return list;
                }
            }

            for (int z = loc_1.getBlockZ(); z < loc_1.getBlockZ() + z_distance; z++) {
                list.add(new dLocation(loc_1.getWorld(),
                        loc_1.getBlockX(),
                        loc_1.getBlockY(),
                        z).identify());

                list.add(new dLocation(loc_1.getWorld(),
                        loc_2.getBlockX(),
                        loc_2.getBlockY(),
                        z).identify());

                list.add(new dLocation(loc_1.getWorld(),
                        loc_1.getBlockX(),
                        loc_2.getBlockY(),
                        z).identify());

                list.add(new dLocation(loc_1.getWorld(),
                        loc_2.getBlockX(),
                        loc_1.getBlockY(),
                        z).identify());
                index++;
                if (index > max) {
                    return list;
                }
            }
            list.add(pair.high.identify());
        }
        return list;
    }


    public dList getBlocks() {
        return getBlocks(null);
    }

    private boolean matchesMaterialList(Location loc, List<dMaterial> materials) {
        if (materials == null) {
            return true;
        }
        dMaterial mat = dMaterial.getMaterialFrom(loc.getBlock().getType(), loc.getBlock().getData());
        for (dMaterial material : materials) {
            if (mat.equals(material) || (mat.getMaterial() == material.getMaterial()
                    && (material.getData() == null || material.getData() == 0))) { // TODO: only if null?
                return true;
            }
        }
        return false;
    }

    public dList getBlocks(List<dMaterial> materials) {
        List<dLocation> locs = getBlocks_internal(materials);
        dList list = new dList();
        for (dLocation loc : locs) {
            list.add(loc.identify());
        }
        return list;
    }

    public List<dLocation> getBlocks_internal(List<dMaterial> materials) {
        int max = Settings.blockTagsMaxBlocks();
        dLocation loc;
        List<dLocation> list = new ArrayList<dLocation>();
        int index = 0;

        for (LocationPair pair : pairs) {

            dLocation loc_1 = pair.low;
            int y_distance = pair.y_distance;
            int z_distance = pair.z_distance;
            int x_distance = pair.x_distance;

            for (int x = 0; x != x_distance + 1; x++) {
                for (int y = 0; y != y_distance + 1; y++) {
                    for (int z = 0; z != z_distance + 1; z++) {
                        loc = new dLocation(loc_1.clone().add(x, y, z));
                        if (loc.getY() < 0 || loc.getY() > 255) {
                            continue; // TODO: Why is this ever possible?
                        }
                        if (!filter.isEmpty()) { // TODO: Should 'filter' exist?
                            // Check filter
                            for (dObject material : filter) {
                                if (((dMaterial) material).matchesMaterialData(
                                        new MaterialData(loc.getBlock().getType(), loc.getBlock().getData()))) {
                                    if (matchesMaterialList(loc, materials)) {
                                        list.add(loc);
                                    }
                                }
                            }
                        }
                        else {
                            if (matchesMaterialList(loc, materials)) {
                                list.add(loc);
                            }
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

    public void setBlocks_internal(List<BlockData> materials) {
        dLocation loc;
        int index = 0;
        for (LocationPair pair : pairs) {
            dLocation loc_1 = pair.low;
            int y_distance = pair.y_distance;
            int z_distance = pair.z_distance;
            int x_distance = pair.x_distance;

            for (int x = 0; x != x_distance + 1; x++) {
                for (int y = 0; y != y_distance + 1; y++) {
                    for (int z = 0; z != z_distance + 1; z++) {
                        if (loc_1.getY() + y >= 0 && loc_1.getY() + y < 256) {
                            materials.get(index).setBlock(loc_1.clone().add(x, y, z).getBlock(), false);
                        }
                        index++;
                    }
                }
            }
        }
    }

    public BlockData getBlockAt(double nX, double nY, double nZ, List<BlockData> materials) {
        dLocation loc;
        int index = 0;
        // TODO: calculate rather than cheat
        for (LocationPair pair : pairs) {
            dLocation loc_1 = pair.low;
            int y_distance = pair.y_distance;
            int z_distance = pair.z_distance;
            int x_distance = pair.x_distance;

            for (int x = 0; x != x_distance + 1; x++) {
                for (int y = 0; y != y_distance + 1; y++) {
                    for (int z = 0; z != z_distance + 1; z++) {
                        if (x == nX && nY == y && z == nZ) {
                            return materials.get(index);
                        }
                        index++;
                    }
                }
            }
        }
        return null;
    }

    public List<dLocation> getBlockLocations() {
        int max = Settings.blockTagsMaxBlocks();
        dLocation loc;
        List<dLocation> list = new ArrayList<dLocation>();
        int index = 0;

        for (LocationPair pair : pairs) {

            dLocation loc_1 = pair.low;
            int y_distance = pair.y_distance;
            int z_distance = pair.z_distance;
            int x_distance = pair.x_distance;

            for (int x = 0; x != x_distance + 1; x++) {
                for (int z = 0; z != z_distance + 1; z++) {
                    for (int y = 0; y != y_distance + 1; y++) {
                        loc = new dLocation(loc_1.clone()
                                .add(x, y, z));
                        if (!filter.isEmpty()) {
                            // Check filter
                            for (dObject material : filter) {
                                if (loc.getBlock().getType().name().equalsIgnoreCase(((dMaterial) material)
                                        .getMaterial().name())) {
                                    list.add(loc);
                                }
                            }
                        }
                        else {
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


    public dList getSpawnableBlocks() {
        return getSpawnableBlocks(null);
    }

    /**
     * Returns a dList of dLocations with 2 vertical blocks of air
     * that are safe for players and similar entities to spawn in,
     * but ignoring blocks in midair
     *
     * @return The dList
     */

    public dList getSpawnableBlocks(List<dMaterial> mats) {
        int max = Settings.blockTagsMaxBlocks();
        dLocation loc;
        dList list = new dList();
        int index = 0;

        BlockHelper blockHelper = NMSHandler.getInstance().getBlockHelper();
        for (LocationPair pair : pairs) {

            dLocation loc_1 = pair.low;
            int y_distance = pair.y_distance;
            int z_distance = pair.z_distance;
            int x_distance = pair.x_distance;

            for (int x = 0; x != x_distance + 1; x++) {
                for (int y = 0; y != y_distance + 1; y++) {
                    for (int z = 0; z != z_distance + 1; z++) {
                        loc = new dLocation(loc_1.clone()
                                .add(x, y, z));

                        if (blockHelper.isSafeBlock(loc.getBlock().getType())
                                && blockHelper.isSafeBlock(loc.clone().add(0, 1, 0).getBlock().getType())
                                && loc.clone().add(0, -1, 0).getBlock().getType().isSolid()
                                && matchesMaterialList(loc.clone().add(0, -1, 0), mats)) {
                            // Get the center of the block, so the entity won't suffocate
                            // inside the edges for a couple of seconds
                            loc.add(0.5, 0, 0.5);
                            list.add(loc.identify());
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
        if (pairs.size() == 0) {
            return null;
        }
        return pairs.get(0).high.getWorld();
    }

    public dLocation getHigh(int index) {
        if (index < 0) {
            return null;
        }
        if (index >= pairs.size()) {
            return null;
        }
        return pairs.get(index).high;
    }

    public dLocation getLow(int index) {
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
    // dObject
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
    public dCuboid setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }


    @Override
    public String debug() {
        return (isUnique()
                ? "<G>" + prefix + "='<A>" + NotableManager.getSavedId(this)
                + "(<Y>" + identify() + "<A>)<G>'  "
                : "<G>" + prefix + "='<Y>" + identify() + "<G>'  ");
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
        sb.append("cu@");

        for (LocationPair pair : pairs) {
            if (pair.low.getWorld() == null || pair.high.getWorld() == null) {
                dB.echoError("Null world for cuboid, returning invalid identity!");
                return "cu@null";
            }
            sb.append(pair.low.getBlockX()).append(',').append(pair.low.getBlockY())
                    .append(',').append(pair.low.getBlockZ()).append(',').append(pair.low.getWorld().getName())
                    .append('|').append(pair.high.getBlockX()).append(',').append(pair.high.getBlockY())
                    .append(',').append(pair.high.getBlockZ()).append(',').append(pair.high.getWorld().getName()).append('|');
        }

        return sb.toString().substring(0, sb.toString().length() - 1);
    }


    @Override
    public String toString() {
        return identify();
    }


    /////////////////////
    // dObject Tag Management
    /////////////////////


    public static void registerTags() {

        // <--[tag]
        // @attribute <cu@cuboid.blocks[<material>|...]>
        // @returns dList(dLocation)
        // @description
        // Returns each block location within the dCuboid.
        // Optionally, specify a list of materials to only return locations
        // with that block type.
        // -->
        registerTag("blocks", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                if (attribute.hasContext(1)) {
                    return new dList(((dCuboid) object).getBlocks(dList.valueOf(attribute.getContext(1)).filter(dMaterial.class)))
                            .getAttribute(attribute.fulfill(1));
                }
                else {
                    return new dList(((dCuboid) object).getBlocks())
                            .getAttribute(attribute.fulfill(1));
                }
            }
        });
        registerTag("get_blocks", registeredTags.get("blocks"));

        // <--[tag]
        // @attribute <cu@cuboid.members_size>
        // @returns Element(Number)
        // @description
        // Returns the number of cuboids defined in the dCuboid.
        // -->
        registerTag("members_size", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dCuboid) object).pairs.size())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <cu@cuboid.member[<#>]>
        // @returns dCuboid
        // @description
        // Returns a new dCuboid of a single member of this dCuboid. Just specify an index.
        // -->
        registerTag("member", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                int member = attribute.getIntContext(1);
                if (member < 1) {
                    member = 1;
                }
                if (member > ((dCuboid) object).pairs.size()) {
                    member = ((dCuboid) object).pairs.size();
                }
                return new dCuboid(((dCuboid) object).pairs.get(member - 1).low, ((dCuboid) object).pairs.get(member - 1).high)
                        .getAttribute(attribute.fulfill(1));
            }
        });
        registerTag("get_member", registeredTags.get("member"));

        // <--[tag]
        // @attribute <cu@cuboid.spawnable_blocks[<Material>|...]>
        // @returns dList(dLocation)
        // @description
        // Returns each dLocation within the dCuboid that is
        // safe for players or similar entities to spawn in.
        // Optionally, specify a list of materials to only return locations
        // with that block type.
        // -->
        registerTag("spawnable_blocks", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                if (attribute.hasContext(1)) {
                    return new dList(((dCuboid) object).getSpawnableBlocks(dList.valueOf(attribute.getContext(1)).filter(dMaterial.class)))
                            .getAttribute(attribute.fulfill(1));
                }
                else {
                    return new dList(((dCuboid) object).getSpawnableBlocks())
                            .getAttribute(attribute.fulfill(1));
                }
            }
        });
        registerTag("get_spawnable_blocks", registeredTags.get("spawnable_blocks"));

        // <--[tag]
        // @attribute <cu@cuboid.outline>
        // @returns dList(dLocation)
        // @description
        // Returns each block location on the outline of the dCuboid.
        // -->
        registerTag("outline", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new dList(((dCuboid) object).getOutline())
                        .getAttribute(attribute.fulfill(1));
            }
        });
        registerTag("get_outline", registeredTags.get("outline"));

        // <--[tag]
        // @attribute <cu@cuboid.filter>
        // @returns dList(dLocation)
        // @description
        // Returns the block locations from the dCuboid's filter.
        // -->
        registerTag("filter", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new dList(((dCuboid) object).filter)
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <cu@cuboid.intersects[<cuboid>]>
        // @returns Element(Boolean)
        // @description
        // Returns whether this cuboid and another intersect.
        // -->
        registerTag("intersects", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                if (!attribute.hasContext(1)) {
                    dB.echoError("The tag cu@cuboid.intersects[...] must have a value.");
                    return null;
                }
                dCuboid cub2 = dCuboid.valueOf(attribute.getContext(1));
                if (cub2 != null) {
                    boolean intersects = false;
                    whole_loop:
                    for (LocationPair pair : ((dCuboid) object).pairs) {
                        for (LocationPair pair2 : cub2.pairs) {
                            if (!pair.low.getWorld().getName().equalsIgnoreCase(pair2.low.getWorld().getName())) {
                                return new Element("false").getAttribute(attribute.fulfill(1));
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
                    return new Element(intersects).getAttribute(attribute.fulfill(1));
                }
                return null;
            }
        });

        // <--[tag]
        // @attribute <cu@cuboid.contains_location[<location>]>
        // @returns Element(Boolean)
        // @description
        // Returns whether this cuboid contains a location.
        // -->
        registerTag("contains_location", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                if (!attribute.hasContext(1)) {
                    dB.echoError("The tag cu@cuboid.contains_location[...] must have a value.");
                    return null;
                }
                dLocation loc = dLocation.valueOf(attribute.getContext(1));
                return new Element(((dCuboid) object).isInsideCuboid(loc)).getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <cu@cuboid.is_within[<cuboid>]>
        // @returns Element(Boolean)
        // @description
        // Returns whether this cuboid is fully inside another cuboid.
        // -->
        registerTag("is_within", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                if (!attribute.hasContext(1)) {
                    dB.echoError("The tag cu@cuboid.is_within[...] must have a value.");
                    return null;
                }
                dCuboid cub2 = dCuboid.valueOf(attribute.getContext(1));
                if (cub2 != null) {
                    boolean contains = true;
                    for (LocationPair pair2 : ((dCuboid) object).pairs) {
                        boolean contained = false;
                        for (LocationPair pair : cub2.pairs) {
                            if (!pair.low.getWorld().getName().equalsIgnoreCase(pair2.low.getWorld().getName())) {
                                if (net.aufdemrand.denizencore.utilities.debugging.dB.verbose) {
                                    dB.log("Worlds don't match!");
                                }
                                return new Element("false").getAttribute(attribute.fulfill(1));
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
                    return new Element(contains).getAttribute(attribute.fulfill(1));
                }
                return null;
            }
        });

        // <--[tag]
        // @attribute <cu@cuboid.center[<index>]>
        // @returns dLocation
        // @description
        // Returns the center location. If a single-member dCuboid, no index is required.
        // If wanting the center of a specific member, just specify an index.
        // -->
        registerTag("center", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                LocationPair pair;
                if (!attribute.hasContext(1)) {
                    pair = ((dCuboid) object).pairs.get(0);
                }
                else {
                    int member = attribute.getIntContext(1);
                    if (member < 1) {
                        member = 1;
                    }
                    if (member > ((dCuboid) object).pairs.size()) {
                        member = ((dCuboid) object).pairs.size();
                    }
                    pair = ((dCuboid) object).pairs.get(member - 1);
                }
                Location base = pair.high.clone().add(pair.low.clone()).add(1.0, 1.0, 1.0);
                base.setX(base.getX() / 2.0);
                base.setY(base.getY() / 2.0);
                base.setZ(base.getZ() / 2.0);
                return new dLocation(base).getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <cu@cuboid.size[<index>]>
        // @returns dLocation
        // @description
        // Returns the size of the cuboid. If a single-member dCuboid, no index is required.
        // If wanting the center of a specific member, just specify an index.
        // Effectively equivalent to: (max - min) + (1,1,1)
        // -->
        registerTag("size", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                LocationPair pair;
                if (!attribute.hasContext(1)) {
                    pair = ((dCuboid) object).pairs.get(0);
                }
                else {
                    int member = attribute.getIntContext(1);
                    if (member < 1) {
                        member = 1;
                    }
                    if (member > ((dCuboid) object).pairs.size()) {
                        member = ((dCuboid) object).pairs.size();
                    }
                    pair = ((dCuboid) object).pairs.get(member - 1);
                }
                Location base = pair.high.clone().subtract(pair.low.clone()).add(1, 1, 1);
                return new dLocation(base).getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <cu@cuboid.max[<index>]>
        // @returns dLocation
        // @description
        // Returns the highest-numbered corner location. If a single-member dCuboid, no
        // index is required. If wanting the max of a specific member, just specify an index.
        // -->
        registerTag("max", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                if (!attribute.hasContext(1)) {
                    return ((dCuboid) object).pairs.get(0).high.getAttribute(attribute.fulfill(1));
                }
                else {
                    int member = attribute.getIntContext(1);
                    if (member < 1) {
                        member = 1;
                    }
                    if (member > ((dCuboid) object).pairs.size()) {
                        member = ((dCuboid) object).pairs.size();
                    }
                    return ((dCuboid) object).pairs.get(member - 1).high.getAttribute(attribute.fulfill(1));
                }
            }
        });

        // <--[tag]
        // @attribute <cu@cuboid.min[<index>]>
        // @returns dLocation
        // @description
        // Returns the lowest-numbered corner location. If a single-member dCuboid, no
        // index is required. If wanting the min of a specific member, just specify an index.
        // -->
        registerTag("min", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                if (!attribute.hasContext(1)) {
                    return ((dCuboid) object).pairs.get(0).low.getAttribute(attribute.fulfill(1));
                }
                else {
                    int member = attribute.getIntContext(1);
                    if (member < 1) {
                        member = 1;
                    }
                    if (member > ((dCuboid) object).pairs.size()) {
                        member = ((dCuboid) object).pairs.size();
                    }
                    return ((dCuboid) object).pairs.get(member - 1).low.getAttribute(attribute.fulfill(1));
                }
            }
        });

        // <--[tag]
        // @attribute <cu@cuboid.include[<location>]>
        // @returns dCuboid
        // @description
        // Expands the first member of the dCuboid to contain the given location, and returns the expanded cuboid.
        // -->
        registerTag("include", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                if (!attribute.hasContext(1)) {
                    dB.echoError("The tag cu@cuboid.include[...] must have a value.");
                    return null;
                }
                try {
                    dLocation loc = dLocation.valueOf(attribute.getContext(1));
                    dCuboid cuboid = ((dCuboid) object).clone();
                    if (loc != null) {
                        if (loc.getX() < cuboid.pairs.get(0).low.getX()) {
                            cuboid.pairs.get(0).low = new dLocation(cuboid.pairs.get(0).low.getWorld(), loc.getX(), cuboid.pairs.get(0).low.getY(), cuboid.pairs.get(0).low.getZ());
                        }
                        if (loc.getY() < cuboid.pairs.get(0).low.getY()) {
                            cuboid.pairs.get(0).low = new dLocation(cuboid.pairs.get(0).low.getWorld(), cuboid.pairs.get(0).low.getX(), loc.getY(), cuboid.pairs.get(0).low.getZ());
                        }
                        if (loc.getZ() < cuboid.pairs.get(0).low.getZ()) {
                            cuboid.pairs.get(0).low = new dLocation(cuboid.pairs.get(0).low.getWorld(), cuboid.pairs.get(0).low.getX(), cuboid.pairs.get(0).low.getY(), loc.getZ());
                        }
                        if (loc.getX() > cuboid.pairs.get(0).high.getX()) {
                            cuboid.pairs.get(0).high = new dLocation(cuboid.pairs.get(0).high.getWorld(), loc.getX(), cuboid.pairs.get(0).high.getY(), cuboid.pairs.get(0).high.getZ());
                        }
                        if (loc.getY() > cuboid.pairs.get(0).high.getY()) {
                            cuboid.pairs.get(0).high = new dLocation(cuboid.pairs.get(0).high.getWorld(), cuboid.pairs.get(0).high.getX(), loc.getY(), cuboid.pairs.get(0).high.getZ());
                        }
                        if (loc.getZ() > cuboid.pairs.get(0).high.getZ()) {
                            cuboid.pairs.get(0).high = new dLocation(cuboid.pairs.get(0).high.getWorld(), cuboid.pairs.get(0).high.getX(), cuboid.pairs.get(0).high.getY(), loc.getZ());
                        }
                        return cuboid.getAttribute(attribute.fulfill(1));
                    }
                }
                catch (CloneNotSupportedException ex) {
                    dB.echoError(ex); // This should never happen
                }
                return null;
            }
        });

        // <--[tag]
        // @attribute <cu@cuboid.list_players>
        // @returns dList(dPlayer)
        // @description
        // Gets a list of all players currently within the dCuboid.
        // -->
        registerTag("list_players", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                ArrayList<dPlayer> players = new ArrayList<dPlayer>();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (((dCuboid) object).isInsideCuboid(player.getLocation())) {
                        players.add(dPlayer.mirrorBukkitPlayer(player));
                    }
                }
                return new dList(players).getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <cu@cuboid.list_npcs>
        // @returns dList(dNPC)
        // @description
        // Gets a list of all NPCs currently within the dCuboid.
        // -->
        if (Depends.citizens != null) {
            registerTag("list_npcs", new TagRunnable() {
                @Override
                public String run(Attribute attribute, dObject object) {
                    ArrayList<dNPC> npcs = new ArrayList<dNPC>();
                    for (NPC npc : CitizensAPI.getNPCRegistry()) {
                        dNPC dnpc = dNPC.mirrorCitizensNPC(npc);
                        if (((dCuboid) object).isInsideCuboid(dnpc.getLocation())) {
                            npcs.add(dnpc);
                        }
                    }
                    return new dList(npcs).getAttribute(attribute.fulfill(1));
                }
            });
        }

        // <--[tag]
        // @attribute <cu@cuboid.list_entities[<entity>|...]>
        // @returns dList(dEntity)
        // @description
        // Gets a list of all entities currently within the dCuboid, with
        // an optional search parameter for the entity type.
        // -->
        registerTag("list_entities", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                ArrayList<dEntity> entities = new ArrayList<dEntity>();
                dList types = new dList();
                if (attribute.hasContext(1)) {
                    types = dList.valueOf(attribute.getContext(1));
                }
                for (Entity ent : ((dCuboid) object).getWorld().getEntities()) {
                    dEntity current = new dEntity(ent);
                    if (ent.isValid() && ((dCuboid) object).isInsideCuboid(ent.getLocation())) {
                        if (!types.isEmpty()) {
                            for (String type : types) {
                                if (current.identifySimpleType().equalsIgnoreCase(type)) {
                                    entities.add(current);
                                    break;
                                }
                            }
                        }
                        else {
                            entities.add(new dEntity(ent));
                        }
                    }
                }
                return new dList(entities).getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <cu@cuboid.list_living_entities>
        // @returns dList(dEntity)
        // @description
        // Gets a list of all living entities currently within the dCuboid.
        // -->
        registerTag("list_living_entities", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                ArrayList<dEntity> entities = new ArrayList<dEntity>();
                for (Entity ent : ((dCuboid) object).getWorld().getLivingEntities()) {
                    if (ent.isValid() && ((dCuboid) object).isInsideCuboid(ent.getLocation()) && !dEntity.isCitizensNPC(ent)) {
                        entities.add(new dEntity(ent));
                    }
                }
                return new dList(entities).getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <cu@cuboid.list_chunks>
        // @returns dList(dChunk)
        // @description
        // Gets a list of all chunks entirely within the dCuboid.
        // -->
        registerTag("list_chunks", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                Set<Chunk> chunks = new HashSet<Chunk>();
                dCuboid obj = (dCuboid) object;
                for (LocationPair pair : obj.pairs) {
                    int minY = pair.low.getBlockY();
                    Chunk minChunk = pair.low.getChunk();
                    if (obj.isInsideCuboid(new Location(obj.getWorld(), minChunk.getX() * 16, minY, minChunk.getZ() * 16))) {
                        chunks.add(minChunk);
                    }
                    Chunk maxChunk = pair.high.getChunk();
                    if (obj.isInsideCuboid(new Location(obj.getWorld(), maxChunk.getX() * 16 + 15, minY, maxChunk.getZ() * 16 + 15))) {
                        chunks.add(maxChunk);
                    }
                    for (int x = minChunk.getX() + 1; x <= maxChunk.getX() - 1; x++) {
                        for (int z = minChunk.getZ() + 1; z <= maxChunk.getZ() - 1; z++) {
                            chunks.add(obj.getWorld().getChunkAt(x, z));
                        }
                    }
                }
                dList list = new dList();
                for (Chunk chunk : chunks) {
                    list.add(new dChunk(chunk).identify());
                }
                return list.getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <cu@cuboid.list_partial_chunks>
        // @returns dList(dChunk)
        // @description
        // Gets a list of all chunks partially or entirely within the dCuboid.
        // -->
        registerTag("list_partial_chunks", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                Set<Chunk> chunks = new HashSet<Chunk>();
                for (LocationPair pair : ((dCuboid) object).pairs) {
                    Chunk minChunk = pair.low.getChunk();
                    Chunk maxChunk = pair.high.getChunk();
                    for (int x = minChunk.getX(); x <= maxChunk.getX(); x++) {
                        for (int z = minChunk.getZ(); z <= maxChunk.getZ(); z++) {
                            chunks.add(((dCuboid) object).getWorld().getChunkAt(x, z));
                        }
                    }
                }
                dList list = new dList();
                for (Chunk chunk : chunks) {
                    list.add(new dChunk(chunk).identify());
                }
                return list.getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <cu@cuboid.notable_name>
        // @returns Element
        // @description
        // Gets the name of a Notable dCuboid. If the cuboid isn't noted,
        // this is null.
        // -->
        registerTag("notable_name", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                String notname = NotableManager.getSavedId((dCuboid) object);
                if (notname == null) {
                    return null;
                }
                return new Element(notname).getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <cu@cuboid.full>
        // @returns Element
        // @group conversion
        // @description
        // Returns a full reusable identification for this cuboid, with extra, generally useless data.
        // -->
        registerTag("full", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dCuboid) object).identifyFull()).getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <cu@cuboid.type>
        // @returns Element
        // @description
        // Always returns 'Cuboid' for dCuboid objects. All objects fetchable by the Object Fetcher will return the
        // type of object that is fulfilling this attribute.
        // -->
        registerTag("type", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element("Cuboid").getAttribute(attribute.fulfill(1));
            }
        });
    }

    public static HashMap<String, TagRunnable> registeredTags = new HashMap<String, TagRunnable>();

    public static void registerTag(String name, TagRunnable runnable) {
        if (runnable.name == null) {
            runnable.name = name;
        }
        registeredTags.put(name, runnable);
    }

    /////////////////////
    // dObject Attributes
    /////////////////////

    @Override
    public String getAttribute(Attribute attribute) {
        if (attribute == null) {
            return null;
        }

        // TODO: Scrap getAttribute, make this functionality a core system
        String attrLow = CoreUtilities.toLowerCase(attribute.getAttributeWithoutContext(1));
        TagRunnable tr = registeredTags.get(attrLow);
        if (tr != null) {
            if (!tr.name.equals(attrLow)) {
                dB.echoError(attribute.getScriptEntry() != null ? attribute.getScriptEntry().getResidingQueue() : null,
                        "Using deprecated form of tag '" + tr.name + "': '" + attrLow + "'.");
            }
            return tr.run(attribute, this);
        }

        // Iterate through this object's properties' attributes
        for (Property property : PropertyParser.getProperties(this)) {
            String returned = property.getAttribute(attribute);
            if (returned != null) {
                return returned;
            }
        }

        return new Element(identify()).getAttribute(attribute);
    }

    public void applyProperty(Mechanism mechanism) {
        adjust(mechanism);
    }

    @Override
    public void adjust(Mechanism mechanism) {

        Element value = mechanism.getValue();

        // TODO: Better mechanisms!

        if (mechanism.matches("outset")) {
            int mod = 1;
            if (value != null && mechanism.requireInteger("Invalid integer specified. Assuming '1'.")) {
                mod = value.asInt();
            }
            for (LocationPair pair : pairs) {
                pair.low.add(-1 * mod, -1 * mod, -1 * mod);
                pair.high.add(mod, mod, mod);
                // Modify the locations, need to readjust the distances generated
                pair.generateDistances();
            }

            // TODO: Make sure negative numbers don't collapse (and invert) the Cuboid
            return;
        }

        if (mechanism.matches("expand")) {
            int mod = 1;
            if (value != null && mechanism.requireInteger("Invalid integer specified. Assuming '1'.")) {
                mod = value.asInt();
            }
            for (LocationPair pair : pairs) {
                pair.low.add(-1 * mod, -1 * mod, -1 * mod);
                pair.high.add(mod, mod, mod);
                // Modify the locations, need to readjust the distances generated
                pair.generateDistances();
            }

            // TODO: Make sure negative numbers don't collapse (and invert)
            // the Cuboid
            return;
        }

        if (mechanism.matches("set_location")) {
            int mod = 1;
            if (value != null && mechanism.requireInteger("Invalid integer specified. Assuming '1'.")) {
                mod = value.asInt();
            }
            for (LocationPair pair : pairs) {
                pair.low.add(-1 * mod, -1 * mod, -1 * mod);
                pair.high.add(mod, mod, mod);
                // Modify the locations, need to readjust the distances generated
                pair.generateDistances();
            }

            // TODO: Make sure negative numbers don't collapse (and invert)
            // the Cuboid
            return;
        }

        // Iterate through this object's properties' mechanisms
        for (Property property : PropertyParser.getProperties(this)) {
            property.adjust(mechanism);
            if (mechanism.fulfilled()) {
                break;
            }
        }

        if (!mechanism.fulfilled()) {
            mechanism.reportInvalid();
        }

    }
}
