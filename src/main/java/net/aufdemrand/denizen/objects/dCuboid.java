package net.aufdemrand.denizen.objects;

import net.aufdemrand.denizen.scripts.ScriptQueue;
import net.aufdemrand.denizen.scripts.containers.core.TaskScriptContainer;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.depends.Depends;
import net.aufdemrand.denizen.utilities.depends.WorldGuardUtilities;
import net.aufdemrand.denizen.utilities.entity.Rotation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Sign;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class dCuboid implements dObject {


    /////////////////////
    //   STATIC METHODS
    /////////////////

    public static Map<String, dCuboid> uniqueObjects = new HashMap<String, dCuboid>();

    public static boolean isSaved(String id) {
        return uniqueObjects.containsKey(id.toUpperCase());
    }

    public static boolean isSaved(dCuboid location) {
        return uniqueObjects.containsValue(location);
    }

    public static dCuboid getSaved(String id) {
        if (uniqueObjects.containsKey(id.toUpperCase()))
            return uniqueObjects.get(id.toUpperCase());
        else return null;
    }

    public static String getSaved(dCuboid cuboid) {
        for (Map.Entry<String, dCuboid> i : uniqueObjects.entrySet()) {
            if (i.getValue().loc_1.getBlockX() != cuboid.loc_1.getBlockX()) continue;
            if (i.getValue().loc_1.getBlockY() != cuboid.loc_1.getBlockY()) continue;
            if (i.getValue().loc_1.getBlockZ() != cuboid.loc_1.getBlockZ()) continue;
            if (i.getValue().loc_1.getWorld().getName() != cuboid.loc_1.getWorld().getName()) continue;
            if (i.getValue().loc_2.getBlockX() != cuboid.loc_2.getBlockX()) continue;
            if (i.getValue().loc_2.getBlockY() != cuboid.loc_2.getBlockY()) continue;
            if (i.getValue().loc_2.getBlockZ() != cuboid.loc_2.getBlockZ()) continue;
            if (i.getValue().loc_2.getWorld().getName() != cuboid.loc_2.getWorld().getName()) continue;
            return i.getKey();
        }
        return null;
    }

    public static void saveAs(dCuboid location, String id) {
        if (location == null) return;
        uniqueObjects.put(id.toUpperCase(), location);
    }

    public static void remove(String id) {
        uniqueObjects.remove(id.toUpperCase());
    }

    /*
     * Called on server startup or /denizen reload locations. Should probably not be called manually.
     */
    public static void _recallCuboids() {
        List<String> cublist = DenizenAPI.getCurrentInstance().getSaves().getStringList("dScript.Cuboids");
        uniqueObjects.clear();
        for (String cuboid : cublist) {
            String id = cuboid.split(";")[0];
            dCuboid cub = valueOf(cuboid.split(";")[1]);
            uniqueObjects.put(id, cub);
        }
    }

    /*
     * Called by Denizen internally on a server shutdown or /denizen save. Should probably
     * not be called manually.
     */
    public static void _saveCuboids() {
        List<String> cublist = new ArrayList<String>();
        for (Map.Entry<String, dCuboid> entry : uniqueObjects.entrySet())
            cublist.add(entry.getKey() + ";"
                    + entry.getValue().loc_1.getBlockX()
                    + "," + entry.getValue().loc_1.getBlockY()
                    + "," + entry.getValue().loc_1.getBlockZ()
                    + "," + entry.getValue().loc_1.getWorld().getName()
                    + "->"
                    + entry.getValue().loc_2.getBlockX()
                    + "," + entry.getValue().loc_2.getBlockY()
                    + "," + entry.getValue().loc_2.getBlockZ()
                    + "," + entry.getValue().loc_2.getWorld().getName());

        DenizenAPI.getCurrentInstance().getSaves().set("dScript.Cuboids", cublist);
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
    @ObjectFetcher("cu")
    public static dCuboid valueOf(String string) {
        if (string == null) return null;

        ////////
        // Match location formats

        // Split values
        String[] positions = string.replace("cu@", "").split("\\|");

        dLocation pos_1;
        dLocation pos_2;

        if (dLocation.matches(positions[0]) && dLocation.matches(positions[1])) {
            pos_1 = dLocation.valueOf(positions[0]);
            pos_2 = dLocation.valueOf(positions[1]);

            if (pos_1 == null || pos_2 == null) {
                dB.log("valueOf dCuboid returning null: '" + string + "'.");
                return null;
            }

            else
                return new dCuboid(pos_1, pos_2);
        }



        ////////
        // Match @object format for saved dLocations
        Matcher m;

        final Pattern item_by_saved = Pattern.compile("(cu@)(.+)");
        m = item_by_saved.matcher(string);

        if (m.matches() && isSaved(m.group(2)))
            return getSaved(m.group(2));



        dB.log("valueOf dCuboid returning null: " + string);

        return null;
    }


    public static boolean matches(String string) {
        final Pattern location_by_saved = Pattern.compile("(cu@)(.+)");
        Matcher m = location_by_saved.matcher(string);
        if (m.matches())
            return true;

        final Pattern location =
                Pattern.compile("((-?\\d+(\\.\\d+)?,){3})\\w+\\|((-?\\d+(\\.\\d+)?,){3})\\w+",
                        Pattern.CASE_INSENSITIVE);
        m = location.matcher(string);
        if (m.matches())
            return true;

        return false;
    }


    ///////////////////
    //  CONSTRUCTORS/INSTANCE METHODS


    dLocation loc_1;
    dLocation loc_2;

    // Warning! Only put dMaterials in filter.
    ArrayList<dObject> filter = new ArrayList<dObject>();

    private dCuboid(Location point_1, Location point_2) {
        loc_1 = new dLocation(point_1);
        loc_2 = new dLocation(point_2);
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

    public dList getBlocks() {
        int x_inc = -1;
        int y_inc = -1;
        int z_inc = -1;

        if (loc_1.getBlockX() <= loc_2.getBlockX()) x_inc = 1;
        if (loc_1.getBlockY() <= loc_2.getBlockY()) y_inc = 1;
        if (loc_1.getBlockZ() <= loc_2.getBlockZ()) z_inc = 1;

        int x_amt = Math.abs(loc_1.getBlockX() - loc_2.getBlockX());
        int y_amt = Math.abs(loc_1.getBlockY() - loc_2.getBlockY());
        int z_amt = Math.abs(loc_1.getBlockZ() - loc_2.getBlockZ());

        dLocation loc;
        dList list = new dList("");

        for (int x = 0; x != x_amt + 1; x++) {
            for (int y = 0; y != y_amt + 1; y++) {
                for (int z = 0; z != z_amt + 1; z++) {
                    loc = new dLocation(loc_1.clone().add((double) x * x_inc, (double) y * y_inc, (double) z * z_inc));
                    if (!filter.isEmpty()) {
                        // Check filter
                        for (dObject material : filter)
                            if (loc.getBlock().getType().name() == ((dMaterial) material).getMaterial().name())
                                list.add(loc.identify());
                    } else
                        list.add(loc.identify());
                }
            }
        }

        return list;
    }


    public dCuboid rememberAs(String id) {
        dCuboid.saveAs(this, id);
        return this;
    }

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
        return (isSaved(this) ? "<G>" + prefix + "='<A>" + getSaved(this) + "(<Y>" + identify()+ "<A>)<G>'  "
                : "<G>" + prefix + "='<Y>" + identify() + "<G>'  ");
    }

    @Override
    public boolean isUnique() {
        if (isSaved(this))
            return true;
        else
            return false;
    }

    @Override
    public String identify() {
        if (isSaved(this))
            return "cu@" + getSaved(this);
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

    @Override
    public String getAttribute(Attribute attribute) {
        if (attribute == null) return null;

        if (attribute.startsWith("get_blocks"))
            return new dList(getBlocks()).getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("filter"))
            return new dList(filter).getAttribute(attribute.fulfill(1));

        return new Element(identify()).getAttribute(attribute.fulfill(0));
    }

}
