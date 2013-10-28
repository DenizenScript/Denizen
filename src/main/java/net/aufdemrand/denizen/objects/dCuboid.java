package net.aufdemrand.denizen.objects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.aufdemrand.denizen.objects.notable.Notable;
import net.aufdemrand.denizen.objects.notable.NotableManager;
import net.aufdemrand.denizen.objects.notable.Note;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.utilities.blocks.SafeBlock;
import net.aufdemrand.denizen.utilities.debugging.dB;

import org.bukkit.Location;
import org.bukkit.World;

public class dCuboid implements dObject, Notable {


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

        // regex patterns used for matching
        final Pattern location_by_saved = Pattern.compile("(cu@)(.+)");
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


    public static class LocationPair {
        dLocation loc_1;
        dLocation loc_2;
        int x_distance;
        int y_distance;
        int z_distance;
    }


    ///////////////////
    //  Constructors/Instance Methods
    //////////////////

    // Location Pairs (loc_1, loc_2) that make up the dCuboid
    List<LocationPair> pairs = new ArrayList<LocationPair>();

    // Only put dMaterials in filter.
    ArrayList<dObject> filter = new ArrayList<dObject>();


    private dCuboid(Location point_1, Location point_2) {
        addPair(point_1, point_2);
    }


    public void addPair(Location point_1, Location point_2) {
        // Make a new pair
        LocationPair pair = new LocationPair();

        World world = point_1.getWorld();

        // Calculate distances in the pair
        int x_high = (point_1.getBlockX() >= point_2.getBlockX()
                ? point_1.getBlockX() : point_2.getBlockX());
        int x_low = (point_1.getBlockX() <= point_2.getBlockX()
                ? point_1.getBlockX() : point_2.getBlockX());
        pair.x_distance = x_high - x_low;

        int y_high = (point_1.getBlockY() >= point_2.getBlockY()
                ? point_1.getBlockY() : point_2.getBlockY());
        int y_low = (point_1.getBlockY() <= point_2.getBlockY()
                ? point_1.getBlockY() : point_2.getBlockY());
        pair.y_distance = y_high - y_low;

        int z_high = (point_1.getBlockZ() >= point_2.getBlockZ()
                ? point_1.getBlockZ() : point_2.getBlockZ());
        int z_low = (point_1.getBlockZ() <= point_2.getBlockZ()
                ? point_1.getBlockZ() : point_2.getBlockZ());
        pair.z_distance = z_high - z_low;

        // Add defining locations to the pair
        pair.loc_1 = new dLocation(world, x_low, y_low, z_low);
        pair.loc_2 = new dLocation(world, x_high, y_high, z_high);

        // Add pair to pairs array
        pairs.add(pair);
    }


    public boolean isInsideCuboid(Location location) {
        for (LocationPair pair : pairs) {
            if (!location.getWorld().equals(pair.loc_1.getWorld()))
                continue;
            if (!Utilities.isBetween(pair.loc_1.getX(), pair.loc_2.getX(), location.getX()))
                continue;
            if (!Utilities.isBetween(pair.loc_1.getY(), pair.loc_2.getY(), location.getY()))
                continue;
            if (Utilities.isBetween(pair.loc_1.getZ(), pair.loc_2.getZ(), location.getZ()))
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

        //  +-----+
        //  |     |
        //  |     |
        //  1-----+

        //  +     +
        //
        //
        //  +     +

        //  +-----2
        //  |     |
        //  |     |
        //  +-----+

        dList list = new dList();

        for (LocationPair pair : pairs) {

            dLocation loc_1 = pair.loc_1;
            dLocation loc_2 = pair.loc_2;
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

            dLocation loc_1 = pair.loc_1;
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

            dLocation loc_1 = pair.loc_1;
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
            sb.append(pair.loc_1.getBlockX() + ',' + pair.loc_1.getBlockY()
                    + "," + pair.loc_1.getBlockZ() + ',' + pair.loc_1.getWorld().getName()
                    + '|'
                    + pair.loc_2.getBlockX() + ',' + pair.loc_2.getBlockY()
                    + ',' + pair.loc_2.getBlockZ() + ',' + pair.loc_2.getWorld().getName()
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
                sb.append(pair.loc_1.getBlockX() + ',' + pair.loc_1.getBlockY()
                        + "," + pair.loc_1.getBlockZ() + ',' + pair.loc_1.getWorld().getName()
                        + '|'
                        + pair.loc_2.getBlockX() + ',' + pair.loc_2.getBlockY()
                        + ',' + pair.loc_2.getBlockZ() + ',' + pair.loc_2.getWorld().getName()
                        + '|');
            }

            return sb.toString().substring(0, sb.toString().length() - 1);
        }
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
            return new dCuboid(pairs.get(member - 1).loc_1, pairs.get(member - 1).loc_2)
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
            return pairs.get(0).loc_2.getAttribute(attribute.fulfill(1));
            else {
                int member = attribute.getIntContext(1);
                if (member == 0)
                    return "null";
                if (member - 1 > pairs.size())
                    return "null";
                return pairs.get(member - 1).loc_2.getAttribute(attribute.fulfill(1));
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
            return pairs.get(0).loc_1.getAttribute(attribute.fulfill(1));
            else {
                int member = attribute.getIntContext(1);
                if (member == 0)
                    return "null";
                if (member - 1 > pairs.size())
                    return "null";
                return pairs.get(member - 1).loc_1.getAttribute(attribute.fulfill(1));
            }
        }



        return new Element(identify()).getAttribute(attribute);
    }

}
