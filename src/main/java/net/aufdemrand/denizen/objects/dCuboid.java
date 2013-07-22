package net.aufdemrand.denizen.objects;

import net.aufdemrand.denizen.objects.notable.Notable;
import net.aufdemrand.denizen.objects.notable.NotableManager;
import net.aufdemrand.denizen.objects.notable.Note;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.util.cuboid.QuadCuboid;
import net.citizensnpcs.util.Util;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class dCuboid implements dObject, Notable {


    /////////////////////
    //   STATIC METHODS
    /////////////////




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
    @ObjectFetcher("cu")
    public static dCuboid valueOf(String string) {
        if (string == null) return null;

        ////////
        // Match location formats

        // Split values
        String[] positions = string.replace("cu@", "").split("\\|");

        dB.echoDebug(Arrays.asList(positions).toString());

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
                Pattern.compile("((-?\\d+,){3})\\w+\\|((-?\\d+,){3})\\w+",
                        Pattern.CASE_INSENSITIVE);

        Matcher m;

        // Check for named cuboid: cu@notable_cuboid
        m = location_by_saved.matcher(string);
        if (m.matches() && NotableManager.isType(m.group(2), dCuboid.class)) return true;

        // Check for standard cuboid format: cu@x,y,z,world|x,y,z,world
        m = location.matcher(string.replace("cu@", ""));
        if (m.matches()) return true;

        return false;
    }


    ///////////////////
    //  Constructors/Instance Methods
    //////////////////


    dLocation loc_1;
    dLocation loc_2;
    int x_distance;
    int y_distance;
    int z_distance;


    // Only put dMaterials in filter.
    ArrayList<dObject> filter = new ArrayList<dObject>();

    private dCuboid(Location point_1, Location point_2) {

        World world = point_1.getWorld();

        int x_high = (point_1.getBlockX() >= point_2.getBlockX()
                ? point_1.getBlockX() : point_2.getBlockX());
        int x_low = (point_1.getBlockX() <= point_2.getBlockX()
                ? point_1.getBlockX() : point_2.getBlockX());
        x_distance = x_high - x_low;

        int y_high = (point_1.getBlockY() >= point_2.getBlockY()
                ? point_1.getBlockY() : point_2.getBlockY());
        int y_low = (point_1.getBlockY() <= point_2.getBlockY()
                ? point_1.getBlockY() : point_2.getBlockY());
        y_distance = y_high - y_low;

        int z_high = (point_1.getBlockZ() >= point_2.getBlockZ()
                ? point_1.getBlockZ() : point_2.getBlockZ());
        int z_low = (point_1.getBlockZ() <= point_2.getBlockZ()
                ? point_1.getBlockZ() : point_2.getBlockZ());
        z_distance = z_high - z_low;


        loc_1 = new dLocation(world, x_low, y_low, z_low);
        loc_2 = new dLocation(world, x_high, y_high, z_high);

    }


    public boolean isInsideCuboid(Location location) {
        if (location.getWorld() != loc_1.getWorld()) return false;
        if (!Utilities.isBetween(loc_1.getX(), loc_2.getX(), location.getX()))
            return false;
        if (!Utilities.isBetween(loc_1.getY(), loc_2.getY(), location.getY()))
            return false;
        if (!Utilities.isBetween(loc_1.getZ(), loc_2.getZ(), location.getZ()))
            return false;
        return true;
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

        dList list = new dList("");

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

        return list;
    }


    public dList getBlocks() {
        dLocation loc;
        dList list = new dList("");

        for (int x = 0; x != x_distance + 1; x++) {
            for (int y = 0; y != y_distance + 1; y++) {
                for (int z = 0; z != z_distance + 1; z++) {
                    loc = new dLocation(loc_1.clone()
                            .add((double) x, (double) y, (double) z));
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
    public String getSaveString() {
        return loc_1.getBlockX() + "," + loc_1.getBlockY()
                + "," + loc_1.getBlockZ() + "," + loc_1.getWorld().getName()
                + "|"
                + loc_2.getBlockX() + "," + loc_2.getBlockY()
                + "," + loc_2.getBlockZ() + "," + loc_2.getWorld().getName();
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
    public String getType() {
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
        else return "cu@" + loc_1.getBlockX() + "," + loc_1.getBlockY()
                + "," + loc_1.getBlockZ() + "," + loc_1.getWorld().getName()
                + "|"
                + loc_2.getBlockX() + "," + loc_2.getBlockY()
                + "," + loc_2.getBlockZ() + "," + loc_2.getWorld().getName();
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


        if (attribute.startsWith("get_blocks"))
            return new dList(getBlocks())
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("get_outline"))
            return new dList(getOutline())
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("filter"))
            return new dList(filter)
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("is_within")) {
            dLocation loc = dLocation.valueOf(attribute.getContext(1));
            return new Element(isInsideCuboid(loc))
                    .getAttribute(attribute.fulfill(1));
        }

        return new Element(identify()).getAttribute(attribute.fulfill(0));
    }




}
