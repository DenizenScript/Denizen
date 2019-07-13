package net.aufdemrand.denizen.objects;

import com.denizenscript.denizencore.objects.*;
import net.aufdemrand.denizen.Settings;
import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.nms.interfaces.BlockData;
import net.aufdemrand.denizen.nms.interfaces.BlockHelper;
import net.aufdemrand.denizen.objects.notable.NotableManager;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.depends.Depends;
import com.denizenscript.denizencore.objects.notable.Notable;
import com.denizenscript.denizencore.objects.notable.Note;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class dCuboid implements dObject, Cloneable, Notable, Adjustable {

    // <--[language]
    // @name dCuboid
    // @group Object System
    // @description
    // A dCuboid represents a cuboidal region in the world.
    //
    // The word 'cuboid' means a less strict cube.
    // Basically: a "cuboid" is to a 3D "cube" what a "rectangle" is to a 2D "square".
    //
    // One 'cuboid' consists of two points: the low point and a high point.
    // a dCuboid can contain as many cuboids within itself as needed (this allows forming more complex shapes from a single dCuboid).
    //
    // For format info, see <@link language cu@>
    //
    // -->

    // <--[language]
    // @name cu@
    // @group Object Fetcher System
    // @description
    // cu@ refers to the 'object identifier' of a dCuboid. The 'cu@' is notation for Denizen's Object
    // Fetcher. The constructor for a dCuboid is <x>,<y>,<z>,<world>|...
    // For example, 'cu@1,2,3,space|4,5,6,space'.
    //
    // For general info, see <@link language dCuboid>
    //
    // -->

    // Cloning
    @Override
    public dCuboid clone() throws CloneNotSupportedException {
        dCuboid cuboid = (dCuboid) super.clone();
        cuboid.pairs = new ArrayList<>(pairs.size());
        for (LocationPair pair : pairs) {
            cuboid.pairs.add(new LocationPair(pair.point_1.clone(), pair.point_2.clone()));
        }
        cuboid.filter = new ArrayList<>(filter);
        return cuboid;
    }

    /////////////////////
    //   STATIC METHODS
    /////////////////

    public static List<dCuboid> getNotableCuboidsContaining(Location location) {
        List<dCuboid> cuboids = new ArrayList<>();
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
                // Must have worlds
                if (pos_1.getWorldName() == null || pos_2.getWorldName() == null) {
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
    public List<LocationPair> pairs = new ArrayList<>();

    // Only put dMaterials in filter.
    ArrayList<dObject> filter = new ArrayList<>();

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

    public dList getShell() {
        int max = Settings.blockTagsMaxBlocks();
        int index = 0;

        dList list = new dList();

        for (LocationPair pair : pairs) {
            dLocation low = pair.low;
            dLocation high = pair.high;
            int y_distance = pair.y_distance;
            int z_distance = pair.z_distance;
            int x_distance = pair.x_distance;

            for (int x = 0; x < x_distance; x++) {
                for (int y = 0; y < y_distance; y++) {
                    list.addObject(new dLocation(low.getWorld(), low.getBlockX() + x, low.getBlockY() + y, low.getBlockZ()));
                    list.addObject(new dLocation(low.getWorld(), low.getBlockX() + x, low.getBlockY() + y, high.getBlockZ()));
                    index++;
                    if (index > max) {
                        return list;
                    }
                }
                for (int z = 0; z < z_distance; z++) {
                    list.addObject(new dLocation(low.getWorld(), low.getBlockX() + x, low.getBlockY(), low.getBlockZ() + z));
                    list.addObject(new dLocation(low.getWorld(), low.getBlockX() + x, high.getBlockY(), low.getBlockZ() + z));
                    index++;
                    if (index > max) {
                        return list;
                    }
                }
            }
            for (int y = 0; y < y_distance; y++) {
                for (int z = 0; z < z_distance; z++) {
                    list.addObject(new dLocation(low.getWorld(), low.getBlockX(), low.getBlockY() + y, low.getBlockZ() + z));
                    list.addObject(new dLocation(low.getWorld(), high.getBlockX(), low.getBlockY() + y, low.getBlockZ() + z));
                    index++;
                    if (index > max) {
                        return list;
                    }
                }
            }
        }

        return list;
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
                list.addObject(new dLocation(loc_1.getWorld(),
                        loc_1.getBlockX(),
                        y,
                        loc_1.getBlockZ()));

                list.addObject(new dLocation(loc_1.getWorld(),
                        loc_2.getBlockX(),
                        y,
                        loc_2.getBlockZ()));

                list.addObject(new dLocation(loc_1.getWorld(),
                        loc_1.getBlockX(),
                        y,
                        loc_2.getBlockZ()));

                list.addObject(new dLocation(loc_1.getWorld(),
                        loc_2.getBlockX(),
                        y,
                        loc_1.getBlockZ()));
                index++;
                if (index > max) {
                    return list;
                }
            }

            for (int x = loc_1.getBlockX(); x < loc_1.getBlockX() + x_distance; x++) {
                list.addObject(new dLocation(loc_1.getWorld(),
                        x,
                        loc_1.getBlockY(),
                        loc_1.getBlockZ()));

                list.addObject(new dLocation(loc_1.getWorld(),
                        x,
                        loc_1.getBlockY(),
                        loc_2.getBlockZ()));

                list.addObject(new dLocation(loc_1.getWorld(),
                        x,
                        loc_2.getBlockY(),
                        loc_2.getBlockZ()));

                list.addObject(new dLocation(loc_1.getWorld(),
                        x,
                        loc_2.getBlockY(),
                        loc_1.getBlockZ()));
                index++;
                if (index > max) {
                    return list;
                }
            }

            for (int z = loc_1.getBlockZ(); z < loc_1.getBlockZ() + z_distance; z++) {
                list.addObject(new dLocation(loc_1.getWorld(),
                        loc_1.getBlockX(),
                        loc_1.getBlockY(),
                        z));

                list.addObject(new dLocation(loc_1.getWorld(),
                        loc_2.getBlockX(),
                        loc_2.getBlockY(),
                        z));

                list.addObject(new dLocation(loc_1.getWorld(),
                        loc_1.getBlockX(),
                        loc_2.getBlockY(),
                        z));

                list.addObject(new dLocation(loc_1.getWorld(),
                        loc_2.getBlockX(),
                        loc_1.getBlockY(),
                        z));
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
        dMaterial mat = new dMaterial(loc.getBlock());
        for (dMaterial material : materials) {
            if (mat.equals(material) || mat.getMaterial() == material.getMaterial()) {
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
        List<dLocation> list = new ArrayList<>();
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
                            continue;
                        }
                        if (!filter.isEmpty()) { // TODO: Should 'filter' exist?
                            // Check filter
                            for (dObject material : filter) {
                                if (((dMaterial) material).matchesBlock(loc.getBlock())) {
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
        List<dLocation> list = new ArrayList<>();
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
    public String debuggable() {
        if (isUnique()) {
            return "cu@" + NotableManager.getSavedId(this) + " (" + identifyFull() + ")";
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
                    return new dList(((dCuboid) object).getBlocks(dList.valueOf(attribute.getContext(1)).filter(dMaterial.class, attribute.context)))
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
                    return new dList(((dCuboid) object).getSpawnableBlocks(dList.valueOf(attribute.getContext(1)).filter(dMaterial.class, attribute.context)))
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
        // @attribute <cu@cuboid.shell>
        // @returns dList(dLocation)
        // @description
        // Returns each block location on the shell of the dCuboid.
        // -->
        registerTag("shell", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return ((dCuboid) object).getShell()
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <cu@cuboid.outline>
        // @returns dList(dLocation)
        // @description
        // Returns each block location on the outline of the dCuboid.
        // -->
        registerTag("outline", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return ((dCuboid) object).getOutline()
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
                                if (com.denizenscript.denizencore.utilities.debugging.dB.verbose) {
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
        // @attribute <cu@cuboid.list_members>
        // @returns dList(dCuboid)
        // @description
        // Returns a list of all sub-cuboids in this dCuboid (for cuboids that contain multiple sub-cuboids).
        // -->
        registerTag("list_members", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                List<LocationPair> pairs = ((dCuboid) object).pairs;
                dList list = new dList();
                for (LocationPair pair : pairs) {
                    list.addObject(new dCuboid(pair.low, pair.high));
                }
                return list.getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <cu@cuboid.get[<index>]>
        // @returns dCuboid
        // @description
        // Returns a cuboid representing the one component of this cuboid (for cuboids that contain multiple sub-cuboids).
        // -->
        registerTag("get", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                if (!attribute.hasContext(1)) {
                    dB.echoError("The tag cu@cuboid.get[...] must have a value.");
                    return null;
                }
                else {
                    int member = attribute.getIntContext(1);
                    if (member < 1) {
                        member = 1;
                    }
                    if (member > ((dCuboid) object).pairs.size()) {
                        member = ((dCuboid) object).pairs.size();
                    }
                    LocationPair pair = ((dCuboid) object).pairs.get(member - 1);
                    return new dCuboid(pair.point_1, pair.point_2).getAttribute(attribute.fulfill(1));
                }
            }
        });
        registerTag("member", registeredTags.get("get"));
        registerTag("get_member", registeredTags.get("get"));

        // <--[tag]
        // @attribute <cu@cuboid.set[<cuboid>].at[<index>]>
        // @returns dCuboid
        // @description
        // Returns a modified copy of this cuboid, with the specific sub-cuboid index changed to hold the input cuboid.
        // -->
        registerTag("set", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                if (!attribute.hasContext(1)) {
                    dB.echoError("The tag cu@cuboid.set[...] must have a value.");
                    return null;
                }
                else {
                    dCuboid subCuboid = dCuboid.valueOf(attribute.getContext(1));
                    attribute = attribute.fulfill(1);
                    if (!attribute.matches("at")) {
                        attribute.fulfill(1);
                        dB.echoError("The tag cu@cuboid.set[...] must be followed by an 'at'.");
                        return null;
                    }
                    if (!attribute.hasContext(1)) {
                        attribute.fulfill(1);
                        dB.echoError("The tag cu@cuboid.set[...].at[...] must have an 'at' value.");
                        return null;
                    }
                    int member = attribute.getIntContext(1);
                    if (member < 1) {
                        member = 1;
                    }
                    if (member > ((dCuboid) object).pairs.size()) {
                        member = ((dCuboid) object).pairs.size();
                    }
                    LocationPair pair = subCuboid.pairs.get(0);
                    try {
                        dCuboid cloned = ((dCuboid) object).clone();
                        cloned.pairs.set(member - 1, new LocationPair(pair.point_1, pair.point_2));
                        return cloned.getAttribute(attribute.fulfill(1));
                    }
                    catch (CloneNotSupportedException ex) {
                        dB.echoError(ex); // This should never happen
                        return null;
                    }
                }
            }
        });

        // <--[tag]
        // @attribute <cu@cuboid.center>
        // @returns dLocation
        // @description
        // Returns the location of the exact center of the cuboid.
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
        // @attribute <cu@cuboid.volume>
        // @returns Element(Number)
        // @description
        // Returns the volume of the cuboid.
        // Effectively equivalent to: (size.x * size.y * size.z).
        // -->
        registerTag("volume", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                LocationPair pair = ((dCuboid) object).pairs.get(0);
                Location base = pair.high.clone().subtract(pair.low.clone()).add(1, 1, 1);
                return new Element(base.getX() * base.getY() * base.getZ()).getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <cu@cuboid.size>
        // @returns dLocation
        // @description
        // Returns the size of the cuboid.
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
        // @attribute <cu@cuboid.max>
        // @returns dLocation
        // @description
        // Returns the highest-numbered (maximum) corner location.
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
        // @attribute <cu@cuboid.min>
        // @returns dLocation
        // @description
        // Returns the lowest-numbered (minimum) corner location.
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
        // @attribute <cu@cuboid.include_x[<number>]>
        // @returns dCuboid
        // @description
        // Expands the first member of the dCuboid to contain the given X value, and returns the expanded cuboid.
        // -->
        registerTag("include_x", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                if (!attribute.hasContext(1)) {
                    dB.echoError("The tag cu@cuboid.include_x[...] must have a value.");
                    return null;
                }
                try {
                    double x = attribute.getDoubleContext(1);
                    dCuboid cuboid = ((dCuboid) object).clone();
                    if (x < cuboid.pairs.get(0).low.getX()) {
                        cuboid.pairs.get(0).low = new dLocation(cuboid.pairs.get(0).low.getWorld(), x, cuboid.pairs.get(0).low.getY(), cuboid.pairs.get(0).low.getZ());
                    }
                    if (x > cuboid.pairs.get(0).high.getX()) {
                        cuboid.pairs.get(0).high = new dLocation(cuboid.pairs.get(0).high.getWorld(), x, cuboid.pairs.get(0).high.getY(), cuboid.pairs.get(0).high.getZ());
                    }
                    return cuboid.getAttribute(attribute.fulfill(1));
                }
                catch (CloneNotSupportedException ex) {
                    dB.echoError(ex); // This should never happen
                }
                return null;
            }
        });

        // <--[tag]
        // @attribute <cu@cuboid.include_y[<number>]>
        // @returns dCuboid
        // @description
        // Expands the first member of the dCuboid to contain the given Y value, and returns the expanded cuboid.
        // -->
        registerTag("include_y", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                if (!attribute.hasContext(1)) {
                    dB.echoError("The tag cu@cuboid.include_y[...] must have a value.");
                    return null;
                }
                try {
                    double y = attribute.getDoubleContext(1);
                    dCuboid cuboid = ((dCuboid) object).clone();
                    if (y < cuboid.pairs.get(0).low.getY()) {
                        cuboid.pairs.get(0).low = new dLocation(cuboid.pairs.get(0).low.getWorld(), cuboid.pairs.get(0).low.getX(), y, cuboid.pairs.get(0).low.getZ());
                    }
                    if (y > cuboid.pairs.get(0).high.getY()) {
                        cuboid.pairs.get(0).high = new dLocation(cuboid.pairs.get(0).high.getWorld(), cuboid.pairs.get(0).high.getX(), y, cuboid.pairs.get(0).high.getZ());
                    }
                    return cuboid.getAttribute(attribute.fulfill(1));
                }
                catch (CloneNotSupportedException ex) {
                    dB.echoError(ex); // This should never happen
                }
                return null;
            }
        });

        // <--[tag]
        // @attribute <cu@cuboid.include_z[<number>]>
        // @returns dCuboid
        // @description
        // Expands the first member of the dCuboid to contain the given Z value, and returns the expanded cuboid.
        // -->
        registerTag("include_z", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                if (!attribute.hasContext(1)) {
                    dB.echoError("The tag cu@cuboid.include_z[...] must have a value.");
                    return null;
                }
                try {
                    double z = attribute.getDoubleContext(1);
                    dCuboid cuboid = ((dCuboid) object).clone();
                    if (z < cuboid.pairs.get(0).low.getZ()) {
                        cuboid.pairs.get(0).low = new dLocation(cuboid.pairs.get(0).low.getWorld(), cuboid.pairs.get(0).low.getX(), cuboid.pairs.get(0).low.getY(), z);
                    }
                    if (z > cuboid.pairs.get(0).high.getZ()) {
                        cuboid.pairs.get(0).high = new dLocation(cuboid.pairs.get(0).high.getWorld(), cuboid.pairs.get(0).high.getX(), cuboid.pairs.get(0).high.getY(), z);
                    }
                    return cuboid.getAttribute(attribute.fulfill(1));
                }
                catch (CloneNotSupportedException ex) {
                    dB.echoError(ex); // This should never happen
                }
                return null;
            }
        });

        // <--[tag]
        // @attribute <cu@cuboid.with_min[<location>]>
        // @returns dCuboid
        // @description
        // Changes the first member of the dCuboid to have the given minimum location, and returns the changed cuboid.
        // If values in the new min are higher than the existing max, the output max will contain the new min values,
        // and the output min will contain the old max values.
        // Note that this is equivalent to constructing a cuboid with the input value and the original cuboids max value.
        // -->
        registerTag("with_min", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                if (!attribute.hasContext(1)) {
                    dB.echoError("The tag cu@cuboid.with_min[...] must have a value.");
                    return null;
                }
                dCuboid cuboid = (dCuboid) object;
                dLocation location = dLocation.valueOf(attribute.getContext(1));
                return new dCuboid(location, cuboid.pairs.get(0).high).getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <cu@cuboid.with_max[<location>]>
        // @returns dCuboid
        // @description
        // Changes the first member of the dCuboid to have the given maximum location, and returns the changed cuboid.
        // If values in the new max are lower than the existing min, the output min will contain the new max values,
        // and the output max will contain the old min values.
        // Note that this is equivalent to constructing a cuboid with the input value and the original cuboids min value.
        // -->
        registerTag("with_max", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                if (!attribute.hasContext(1)) {
                    dB.echoError("The tag cu@cuboid.with_max[...] must have a value.");
                    return null;
                }
                dCuboid cuboid = (dCuboid) object;
                dLocation location = dLocation.valueOf(attribute.getContext(1));
                return new dCuboid(location, cuboid.pairs.get(0).low).getAttribute(attribute.fulfill(1));
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
                ArrayList<dPlayer> players = new ArrayList<>();
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
                    ArrayList<dNPC> npcs = new ArrayList<>();
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
                ArrayList<dEntity> entities = new ArrayList<>();
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
                ArrayList<dEntity> entities = new ArrayList<>();
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
                dList chunks = new dList();
                dCuboid obj = (dCuboid) object;
                for (LocationPair pair : obj.pairs) {
                    int minY = pair.low.getBlockY();
                    dChunk minChunk = new dChunk(pair.low);
                    if (obj.isInsideCuboid(new Location(obj.getWorld(), minChunk.getX() * 16, minY, minChunk.getZ() * 16))) {
                        chunks.addObject(minChunk);
                    }
                    dChunk maxChunk = new dChunk(pair.high);
                    if (obj.isInsideCuboid(new Location(obj.getWorld(), maxChunk.getX() * 16 + 15, minY, maxChunk.getZ() * 16 + 15))) {
                        chunks.addObject(maxChunk);
                    }
                    for (int x = minChunk.getX() + 1; x < maxChunk.getX(); x++) {
                        for (int z = minChunk.getZ() + 1; z < maxChunk.getZ(); z++) {
                            chunks.addObject(new dChunk(new dWorld(((dCuboid) object).getWorld()), x, z));
                        }
                    }
                }
                return chunks.getAttribute(attribute.fulfill(1));
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
                dList chunks = new dList();
                for (LocationPair pair : ((dCuboid) object).pairs) {
                    dChunk minChunk = new dChunk(pair.low);
                    dChunk maxChunk = new dChunk(pair.high);
                    for (int x = minChunk.getX(); x <= maxChunk.getX(); x++) {
                        for (int z = minChunk.getZ(); z <= maxChunk.getZ(); z++) {
                            chunks.addObject(new dChunk(new dWorld(((dCuboid) object).getWorld()), x, z));
                        }
                    }
                }
                return chunks.getAttribute(attribute.fulfill(1));
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

    public static HashMap<String, TagRunnable> registeredTags = new HashMap<>();

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

        String returned = CoreUtilities.autoPropertyTag(this, attribute);
        if (returned != null) {
            return returned;
        }

        return new Element(identify()).getAttribute(attribute);
    }

    public void applyProperty(Mechanism mechanism) {
        if (NotableManager.isExactSavedObject(this)) {
            dB.echoError("Cannot apply properties to noted objects.");
            return;
        }
        adjust(mechanism);
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dCuboid
        // @name set_member
        // @input (#,)dCuboid
        // @description
        // Sets a given sub-cuboid of the cuboid.
        // Input is of the form like "2,cu@..." where 2 is the sub-cuboid index or just a direct dCuboid input.
        // @tags
        // <cu@cuboid.get>
        // <cu@cuboid.set[<cuboid>].at[<#>]>
        // -->
        if (mechanism.matches("set_member")) {
            String value = mechanism.getValue().asString();
            int comma = value.indexOf(',');
            int member = 1;
            if (comma > 0) {
                member = new Element(value.substring(0, comma)).asInt();
            }
            dCuboid subCuboid = dCuboid.valueOf(comma == -1 ? value : value.substring(comma + 1));
            if (member < 1) {
                member = 1;
            }
            if (member > pairs.size()) {
                member = pairs.size();
            }
            LocationPair pair = subCuboid.pairs.get(0);
            pairs.set(member - 1, new LocationPair(pair.point_1, pair.point_2));
        }

        // TODO: Better mechanisms!

        if (mechanism.matches("outset")) {
            int mod = 1;
            if (mechanism.hasValue() && mechanism.requireInteger("Invalid integer specified. Assuming '1'.")) {
                mod = mechanism.getValue().asInt();
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
            if (mechanism.hasValue() && mechanism.requireInteger("Invalid integer specified. Assuming '1'.")) {
                mod = mechanism.getValue().asInt();
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
            if (mechanism.hasValue() && mechanism.requireInteger("Invalid integer specified. Assuming '1'.")) {
                mod = mechanism.getValue().asInt();
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

        CoreUtilities.autoPropertyMechanism(this, mechanism);

    }
}
