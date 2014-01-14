package net.aufdemrand.denizen.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.aufdemrand.denizen.objects.notable.Notable;
import net.aufdemrand.denizen.objects.notable.NotableManager;
import net.aufdemrand.denizen.objects.notable.Note;
import net.aufdemrand.denizen.objects.properties.Property;
import net.aufdemrand.denizen.objects.properties.PropertyParser;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.utilities.blocks.SafeBlock;
import net.aufdemrand.denizen.utilities.debugging.dB;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class dCuboid implements dObject, Notable, Adjustable {


    /////////////////////
    //   STATIC METHODS
    /////////////////

    public static List<dCuboid> getNotableCuboidsContaining(Location location) {
        List<dCuboid> cuboids = new ArrayList<dCuboid>();
        for (dObject notable : NotableManager.getAllType(dCuboid.class))
            if (((dCuboid) notable).isInsideCuboid(location))
                cuboids.add((dCuboid) notable);

        return cuboids;
    }


    //////////////////
    //    OBJECT FETCHER
    ////////////////

    /**
     * Gets a Location Object from a string form of id,x,y,z,world
     * or a dScript argument (location:)x,y,z,world. If including an Id,
     * this location will persist and can be recalled at any time.
     *
     * @param string  the string or dScript argument String
     * @return  a Location, or null if incorrectly formatted
     *
     */
    @Fetchable("cu")
    public static dCuboid valueOf(String string) {
        if (string == null) return null;

        ////////
        // Match location formats

        // Split values
        String[] positions = string.replace("cu@", "").split("\\|");

        dLocation pos_1;
        dLocation pos_2;

        // Quality control
        if (dLocation.matches(positions[0]) && dLocation.matches(positions[1])) {
            pos_1 = dLocation.valueOf(positions[0]);
            pos_2 = dLocation.valueOf(positions[1]);

            // Must not be null
            if (pos_1 == null || pos_2 == null) {
                dB.echoError("valueOf in dCuboid returning null: '" + string + "'.");
                return null;

            } else if (pos_1.getWorld() != pos_2.getWorld()) {
                dB.echoError("Worlds must match on cuboid construction.");
                return null;
            }

            else
                return new dCuboid(pos_1, pos_2);
        }

        ////////
        // Match @object format for Notable dCuboids
        Matcher m;

        final Pattern item_by_saved = Pattern.compile("(cu@)(.+)");
        m = item_by_saved.matcher(string);

        if (m.matches() && NotableManager.isType(m.group(2), dCuboid.class))
            return (dCuboid) NotableManager.getSavedObject(m.group(2));

        dB.echoError("valueOf dCuboid returning null: " + string);

        return null;
    }


    public static boolean matches(String string) {
        // Starts with cu@? Assume match.
        if (string.toLowerCase().startsWith("cu@")) return true;

        // regex patterns used for matching
        final Pattern location_by_saved = Pattern.compile("(cu@)?(.+)");
        final Pattern location =
                Pattern.compile("((-?\\d+,){3})[\\w\\s]+\\|((-?\\d+,){3})[\\w\\s]+",
                        Pattern.CASE_INSENSITIVE);

        Matcher m;

        // Check for named cuboid: cu@notable_cuboid
        m = location_by_saved.matcher(string);
        if (m.matches() && NotableManager.isType(m.group(2), dCuboid.class)) return true;

        // Check for standard cuboid format: cu@x,y,z,world|x,y,z,world
        m = location.matcher(string.replace("cu@", ""));
        return m.matches();
    }


    ///////////////
    //  LocationPairs
    /////////////


    public static class LocationPair {
        dLocation  low;
        dLocation high;
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
            if (number == 1)
                this.point_1 = point;
            else if (number == 2)
                this.point_2 = point;
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
    List<LocationPair> pairs = new ArrayList<LocationPair>();

    // Only put dMaterials in filter.
    ArrayList<dObject> filter = new ArrayList<dObject>();


    private dCuboid(Location point_1, Location point_2) {
        addPair(point_1, point_2);
    }


    public void addPair(Location point_1, Location point_2) {
        // Make a new pair
        LocationPair pair = new LocationPair(new dLocation(point_1), new dLocation(point_2));
        // Add it to the Cuboid pairs list
        pairs.add(pair);
    }


    public boolean isInsideCuboid(Location location) {
        for (LocationPair pair : pairs) {
            if (!location.getWorld().equals(pair.low.getWorld()))
                continue;
            if (!Utilities.isBetween(pair.low.getBlockX(), pair.high.getBlockX(), location.getBlockX()))
                continue;
            if (!Utilities.isBetween(pair.low.getBlockY(), pair.high.getBlockY(), location.getBlockY()))
                continue;
            if (Utilities.isBetween(pair.low.getBlockZ(), pair.high.getBlockZ(), location.getZ()))
                return true;
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

        dList list = new dList();

        for (LocationPair pair : pairs) {

            dLocation loc_1 = pair.low;
            dLocation loc_2 = pair.high;
            int y_distance = pair.y_distance;
            int z_distance = pair.z_distance;
            int x_distance = pair.x_distance;

            for (int y = loc_1.getBlockY(); y <= loc_1.getBlockY() + y_distance; y++) {
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
            }

            for (int x = loc_1.getBlockX(); x <= loc_1.getBlockX() + x_distance; x++) {
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
            }

            for (int z = loc_1.getBlockZ(); z <= loc_1.getBlockZ() + z_distance; z++) {
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
            }
        }

        return list;
    }


    public dList getBlocks() {
        dLocation loc;
        dList list = new dList();

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
                            for (dObject material : filter)
                                if (loc.getBlock().getType().name().equalsIgnoreCase(((dMaterial) material)
                                        .getMaterial().name()))
                                    list.add(loc.identify());
                        } else
                            list.add(loc.identify());
                    }
                }
            }

        }

        return list;
    }

    public List<dLocation> getBlockLocations() {
        dLocation loc;
        List<dLocation> list = new ArrayList<dLocation>();

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
                            for (dObject material : filter)
                                if (loc.getBlock().getType().name().equalsIgnoreCase(((dMaterial) material)
                                        .getMaterial().name()))
                                    list.add(loc);
                        } else
                            list.add(loc);
                    }
                }
            }

        }

        return list;
    }


    /**
     * Returns a dList of dLocations with 2 vertical blocks of air
     * that are safe for players and similar entities to spawn in,
     * but ignoring blocks in midair
     *
     * @return  The dList
     */

    public dList getSpawnableBlocks() {
        dLocation loc;
        dList list = new dList();

        for (LocationPair pair : pairs) {

            dLocation loc_1 = pair.low;
            int y_distance = pair.y_distance;
            int z_distance = pair.z_distance;
            int x_distance = pair.x_distance;

            for (int x = 0; x != x_distance + 1; x++) {
                for (int z = 0; z != z_distance + 1; z++) {
                    for (int y = 0; y != y_distance; y++) {

                        loc = new dLocation(loc_1.clone()
                                .add(x, y, z));

                        if (SafeBlock.blockIsSafe(loc.getBlock().getType())
                                && SafeBlock.blockIsSafe(loc.clone().add(0, 1, 0).getBlock().getType())
                                && loc.clone().add(0, -1, 0).getBlock().getType().isSolid()) {
                            // Get the center of the block, so the entity won't suffocate
                            // inside the edges for a couple of seconds
                            loc.add(0.5, 0, 0.5);
                            list.add(loc.identify());
                        }
                    }
                }
            }
        }

        return list;
    }



    ///////////////////
    // Notable
    ///////////////////


    @Override
    public boolean isUnique() {
        return NotableManager.isSaved(this);
    }


    @Override
    @Note("cuboid")
    public String getSaveObject() {
        StringBuilder sb = new StringBuilder();

        for (LocationPair pair : pairs) {
            sb.append(pair.low.getBlockX() + ',' + pair.low.getBlockY()
                    + "," + pair.low.getBlockZ() + ',' + pair.low.getWorld().getName()
                    + '|'
                    + pair.high.getBlockX() + ',' + pair.high.getBlockY()
                    + ',' + pair.high.getBlockZ() + ',' + pair.high.getWorld().getName()
                    + '|');
        }

        return sb.toString().substring(0, sb.toString().length() - 1);
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
                + "(<Y>" + identify()+ "<A>)<G>'  "
                : "<G>" + prefix + "='<Y>" + identify() + "<G>'  ");
    }


    @Override
    public String identify() {
        if (isUnique())
            return "cu@" + NotableManager.getSavedId(this);

        else {
            StringBuilder sb = new StringBuilder();
            sb.append("cu@");

            for (LocationPair pair : pairs) {
                sb.append(pair.low.getBlockX() + ',' + pair.low.getBlockY()
                        + "," + pair.low.getBlockZ() + ',' + pair.low.getWorld().getName()
                        + '|'
                        + pair.high.getBlockX() + ',' + pair.high.getBlockY()
                        + ',' + pair.high.getBlockZ() + ',' + pair.high.getWorld().getName()
                        + '|');
            }

            return sb.toString().substring(0, sb.toString().length() - 1);
        }
    }


    @Override
    public String identifySimple() {
        return identify();
    }


    @Override
    public String toString() {
        return identify();
    }



    /////////////////////
    // dObject Attributes
    /////////////////////


    @Override
    public String getAttribute(Attribute attribute) {
        if (attribute == null) return null;

        // <--[tag]
        // @attribute <cu@cuboid.get_blocks>
        // @returns dList(dLocation)
        // @description
        // Returns each block location within the dCuboid.
        // -->
        if (attribute.startsWith("get_blocks"))
            return new dList(getBlocks())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <cu@cuboid.members_size>
        // @returns Element(Number)
        // @description
        // Returns the number of cuboids defined in the dCuboid.
        // -->
        if (attribute.startsWith("members_size"))
            return new Element(pairs.size())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <cu@cuboid.get_member[#]>
        // @returns dCuboid
        // @description
        // Returns a new dCuboid of a single member of this dCuboid. Just specify an index.
        // -->
        if (attribute.startsWith("get_member")) {
            int member = attribute.getIntContext(1);
            if (member == 0)
                return "null";
            if (member - 1 > pairs.size())
                return "null";
            return new dCuboid(pairs.get(member - 1).low, pairs.get(member - 1).high)
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <cu@cuboid.get_spawnable_blocks>
        // @returns dList(dLocation)
        // @description
        // Returns each dLocation within the dCuboid that is
        // safe for players or similar entities to spawn in.
        // -->
        if (attribute.startsWith("get_spawnable_blocks"))
            return new dList(getSpawnableBlocks())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <cu@cuboid.get_outline>
        // @returns dList(dLocation)
        // @description
        // Returns each block location on the outline of the dCuboid.
        // -->
        if (attribute.startsWith("get_outline"))
            return new dList(getOutline())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <cu@cuboid.filter>
        // @returns dList(dLocation)
        // @description
        // Returns the block locations from the dCuboid's filter.
        // -->
        if (attribute.startsWith("filter"))
            return new dList(filter)
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <cu@cuboid.is_within[<location>]>
        // @returns Element(Boolean)
        // @description
        // Returns whether if the dCuboid is within the location.
        // -->
        if (attribute.startsWith("is_within")) {
            dLocation loc = dLocation.valueOf(attribute.getContext(1));
            return new Element(isInsideCuboid(loc))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <cu@cuboid.max>
        // @returns dLocation
        // @description
        // Returns the highest-numbered corner location. If a single-member dCuboid, no
        // index is required. If wanting the max of a specific member, just specify an index.
        // -->
        if (attribute.startsWith("max")) {
            if (!attribute.hasContext(1))
                return pairs.get(0).high.getAttribute(attribute.fulfill(1));
            else {
                int member = attribute.getIntContext(1);
                if (member == 0)
                    return "null";
                if (member - 1 > pairs.size())
                    return "null";
                return pairs.get(member - 1).high.getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <cu@cuboid.min>
        // @returns dLocation
        // @description
        // Returns the lowest-numbered corner location. If a single-member dCuboid, no
        // index is required. If wanting the max of a specific member, just specify an index.
        // -->
        if (attribute.startsWith("min")) {
            if (!attribute.hasContext(1))
                return pairs.get(0).low.getAttribute(attribute.fulfill(1));
            else {
                int member = attribute.getIntContext(1);
                if (member == 0)
                    return "null";
                if (member - 1 > pairs.size())
                    return "null";
                return pairs.get(member - 1).low.getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <cu@cuboid.list_players>
        // @returns dList(dPlayer)
        // @description
        // Gets a list of all players currently within the dCuboid.
        // -->
        if (attribute.startsWith("list_players")) {
            ArrayList<dPlayer> players = new ArrayList<dPlayer>();
            for (Player player : Bukkit.getOnlinePlayers())
                if (isInsideCuboid(player.getLocation()))
                    players.add(dPlayer.mirrorBukkitPlayer(player));
            return new dList(players).getAttribute(attribute.fulfill(1));
        }

        // Iterate through this object's properties' attributes
        for (Property property : PropertyParser.getProperties(this)) {
            String returned = property.getAttribute(attribute);
            if (returned != null) return returned;
        }

        return new Element(identify()).getAttribute(attribute);
    }


    @Override
    public void adjust(Mechanism mechanism) {

        Element value = mechanism.getValue();

        // <--[mechanism]
        // @object dCuboid
        // @name outset
        // @input Element(Number)
        // @description
        // 'Outsets' the area of a dCuboid by the number specified, or 1 if not
        // specified. Example: - adjust cu@my_cuboid outset:5
        // Outsetting a cuboid expands it in all directions. Use negative numbers
        // to 'inset' instead.
        // @tags
        // <cu@cuboid.get_outline>
        // -->
        if (mechanism.matches("outset")) {
            int mod = 1;
            if (value != null && mechanism.requireInteger("Invalid integer specified. Assuming '1'."));
                mod = value.asInt();
            for (LocationPair pair : pairs) {
                pair.low.add(-1 * mod, -1 * mod, -1 * mod);
                pair.high.add(mod, mod, mod);
                // Modify the locations, need to readjust the distances generated
                pair.generateDistances();
            }

            // TODO: Make sure negative numbers don't collapse (and invert) the Cuboid
            return;
        }

        // <--[mechanism]
        // @object dCuboid
        // @name expand
        // @input Element(Number)
        // @description
        // Expands the area of a dCuboid by the number specified, or 1 if not
        // specified, in a specified direction. Example: - adjust cu@my_cuboid expand:5|north
        // Use negative numbers to 'reduce' instead.
        // @tags
        // <e@entity.location.direction>
        // <e@entity.location.pitch>
        // <cu@cuboid.get_outline>
        // -->
        if (mechanism.matches("expand")) {
            int mod = 1;
            if (value != null && mechanism.requireInteger("Invalid integer specified. Assuming '1'."))
                mod = value.asInt();
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

        // <--[mechanism]
        // @object dCuboid
        // @name set_location
        // @input Element(Number)
        // @description
        // Sets one of two defining locations. dCuboid will take the location into
        // account when recalculating the low and high locations as well as distances
        // belonging to the cuboid.
        // @tags
        // <cu@cuboid.low>
        // <cu@cuboid.high>
        // -->
        if (mechanism.matches("set_location")) {
            int mod = 1;
            if (value != null && mechanism.requireInteger("Invalid integer specified. Assuming '1'."))
                mod = value.asInt();
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

        if (!mechanism.fulfilled())
            mechanism.reportInvalid();

    }
}
