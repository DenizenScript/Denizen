package net.aufdemrand.denizen.arguments;

import net.aufdemrand.denizen.interfaces.dScriptArgument;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class dLocation extends org.bukkit.Location implements dScriptArgument {


    /////////////////////
    //   STATIC METHODS
    /////////////////

    public static Map<String, dLocation> uniqueObjects = new HashMap<String, dLocation>();

    public static boolean isSaved(String id) {
        return uniqueObjects.containsKey(id.toUpperCase());
    }

    public static boolean isSaved(dLocation location) {
        return uniqueObjects.containsValue(location);
    }

    public static dLocation getSaved(String id) {
        if (uniqueObjects.containsKey(id.toUpperCase()))
            return uniqueObjects.get(id.toUpperCase());
        else return null;
    }

    public static String getSaved(dLocation location) {
        for (Map.Entry<String, dLocation> i : uniqueObjects.entrySet())
            if (i.getValue() == location) return i.getKey();
        return null;
    }

    public static void saveAs(dLocation location, String id) {
        if (location == null) return;
        uniqueObjects.put(id.toUpperCase(), location);
    }

    public static void remove(String id) {
        uniqueObjects.remove(id.toUpperCase());
    }

    /*
     * Called on server startup or /denizen reload locations. Should probably not be called manually.
     */
    public static void _recallLocations() {
        List<String> loclist = DenizenAPI.getCurrentInstance().getSaves().getStringList("dScript.Locations");
        uniqueObjects.clear();
        for (String location : loclist) {
            dLocation loc = (dLocation) valueOf(location);
            // TODO: Finish this
        }
    }

    /*
     * Called by Denizen internally on a server shutdown or /denizen save. Should probably
     * not be called manually.
     */
    public static void _saveLocations() {
        List<String> loclist = new ArrayList<String>();
        for (Map.Entry<String, dLocation> entry : uniqueObjects.entrySet())
            loclist.add(entry.getValue().toString());

        DenizenAPI.getCurrentInstance().getSaves().set("dScript.Locations", loclist);
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
    public static dLocation valueOf(String string) {
        if (string == null) return null;

        ////////
        // Match @object format for saved dLocations
        Matcher m;

        final Pattern item_by_saved = Pattern.compile("(l@)(.+)");
        m = item_by_saved.matcher(string);

        if (m.matches())
            return getSaved(m.group(2));


        ////////
        // Match location formats

        // Split values
        String[] split = string.split(",");

        if (split.length == 4)
            // If 4 values, standard dScript location format
            // x,y,z,world
            try {
                return new dLocation(Bukkit.getWorld(split[3]),
                        Double.valueOf(split[0]),
                        Double.valueOf(split[1]),
                        Double.valueOf(split[2]));
            } catch(Exception e) {
                return null;
            }


        else if (split.length == 6)
            // If 6 values, location with pitch/yaw
            // x,y,z,yaw,pitch,world
            try {
                return new dLocation(Bukkit.getWorld(split[4]),
                        Double.valueOf(split[1]),
                        Double.valueOf(split[2]),
                        Double.valueOf(split[3])).rememberAs(split[0]);
            } catch(Exception e) {
                return null;
            }

        return null;
    }

    public static dScriptArgument fetchAsArg(String arg) {
        return valueOf(arg);
    }

    /**
     * Turns a Bukkit Location into a Location, which has some helpful methods
     * for working with dScript.
     *
     * @param location the Bukkit Location to reference
     */
    public dLocation(Location location) {
        super(location.getWorld(), location.getX(), location.getY(), location.getZ());
    }

    /**
     * Turns a world and coordinates into a Location, which has some helpful methods
     * for working with dScript. If working with temporary locations, this is
     * a much better method to use than {@link #dLocation(org.bukkit.World, double, double, double)}.
     *
     * @param world  the world in which the location resides
     * @param x  x-coordinate of the location
     * @param y  y-coordinate of the location
     * @param z  z-coordinate of the location
     *
     */
    public dLocation(World world, double x, double y, double z) {
        super(world, x, y, z);
    }

    public dLocation(World world, double x, double y, double z, float yaw, float pitch) {
        super(world, x, y, z, yaw, pitch);
    }

    public dLocation rememberAs(String id) {
        dLocation.saveAs(this, id);
        return this;
    }



    String prefix = "Location";

    @Override
    public String getType() {
        return "dLocation";
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public dLocation setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    @Override
    public String debug() {
        return (isSaved(this) ? "<G>" + prefix + "='<A>" + getSaved(this) + "(<Y>" + getX() + "," + getY()
                + "," + getZ() + "," + getWorld().getName() + "<A>)<G>'  "
                : "<G>" + prefix + "='<Y>" + getX() + "," + getY()
                + "," + getZ() + "," + getWorld().getName() + "<G>'  ");
    }

    @Override
    public boolean isUnique() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String identify() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String toString() {
        if (isSaved(this))
            return "l@" + getSaved(this);
        else return "l@" + getX() + "," + getY()
                + "," + getZ() + "," + getWorld().getName();
    }



    @Override
    public String getAttribute(Attribute attribute) {


        if (attribute == null) return null;

        if (attribute.startsWith("biome.formatted"))
            return new Element(getBlock().getBiome().name().toLowerCase().replace('_', ' '))
                    .getAttribute(attribute.fulfill(2));

        if (attribute.startsWith("biome.humidity"))
            return new Element(String.valueOf(getBlock().getHumidity()))
                    .getAttribute(attribute.fulfill(2));

        if (attribute.startsWith("biome.temperature"))
            return new Element(String.valueOf(getBlock().getTemperature()))
                    .getAttribute(attribute.fulfill(2));

        if (attribute.startsWith("biome"))
            return new Element(String.valueOf(getBlock().getBiome().name()))
                    .getAttribute(attribute.fulfill(1));

//        else if (type.equals("BLOCK"))
//        {
//            if (subType.equals("BELOW"))
//            {
//                fromLocation = new Location(fromLocation.add(0, -1, 0));
//            }
//
//            else if (subType.equals("MATERIAL") || specifier.equals("MATERIAL"))
//            {
//                event.setReplaced(fromLocation.getBlock().getType().toString());
//            }
//        }
//
//        else if (type.equals("DIRECTION"))
//        {
//            if (fromLocation != null && toLocation != null)
//            {
//                event.setReplaced(Utilities.getCardinal(Utilities.getYaw
//                        (toLocation.toVector().subtract
//                                (fromLocation.toVector()).normalize())));
//            }
//        }
//
//        else if (type.equals("DISTANCE"))
//        {
//            if (fromLocation != null && toLocation != null)
//            {
//                if (subType.equals("ASINT"))
//                {
//                    event.setReplaced(String.valueOf((int)fromLocation.distance(toLocation)));
//                }
//                else if (subType.equals("VERTICAL"))
//                {
//                    if (fromLocation.getWorld().getName() == toLocation.getWorld().getName()
//                            || specifier.equals("MULTIWORLD"))
//                    {
//                        // Only calculate distance between locations on different worlds
//                        // if the MULTIWORLD specifier is used
//                        event.setReplaced(String.valueOf(Math.abs(
//                                fromLocation.getY() - toLocation.getY())));
//                    }
//                }
//                else if (subType.equals("HORIZONTAL"))
//                {
//                    if (fromLocation.getWorld().getName() == toLocation.getWorld().getName()
//                            || specifier.equals("MULTIWORLD"))
//                    {
//                        // Only calculate distance between locations on different worlds
//                        // if the MULTIWORLD specifier is used
//                        event.setReplaced(String.valueOf(Math.sqrt(
//                                Math.pow(fromLocation.getX() - toLocation.getX(), 2) +
//                                        Math.pow(fromLocation.getZ() - toLocation.getZ(), 2))));
//                    }
//                }
//                else
//                    event.setReplaced(String.valueOf(fromLocation.distance(toLocation)));
//            }
//        }
//
//        else if (type.equals("FORMATTED"))
//            event.setReplaced("X '" + fromLocation.getX()
//                    + "', Y '" + fromLocation.getY()
//                    + "', Z '" + fromLocation.getZ()
//                    + "', in world '" + fromLocation.getWorld().getName() + "'");
//
//        else if (type.equals("IS_LIQUID"))
//        {
//            event.setReplaced(String.valueOf(fromLocation.getBlock().isLiquid()));
//        }
//
//        else if (type.equals("LIGHT"))
//        {
//            if (subType.equals("BLOCKS"))
//                event.setReplaced(String.valueOf((int) fromLocation.getBlock().getLightFromBlocks()));
//            else if (subType.equals("SKY"))
//                event.setReplaced(String.valueOf((int) fromLocation.getBlock().getLightFromSky()));
//            else
//                event.setReplaced(String.valueOf((int) fromLocation.getBlock().getLightLevel()));
//        }
//
//        else if (type.equals("POWER"))
//        {
//            event.setReplaced(String.valueOf((int) fromLocation.getBlock().getBlockPower()));
//        }
//
//        else if (type.equals("TIME"))
//        {
//            if (subType.equals("PERIOD"))
//                if (fromLocation.getWorld().getTime() < 13500 ||
//                        fromLocation.getWorld().getTime() > 23000)
//                    event.setReplaced("day");
//                else if (fromLocation.getWorld().getTime() > 13500)
//                    event.setReplaced("night");
//        }
//
//        else if (type.equals("WORLD"))
//            event.setReplaced(fromLocation.getWorld().getName());
//
//        else if (type.equals("X"))
//            event.setReplaced(String.valueOf(fromLocation.getX()));
//
//        else if (type.equals("Y"))
//            event.setReplaced(String.valueOf(fromLocation.getY()));
//
//        else if (type.equals("Z"))
//            event.setReplaced(String.valueOf(fromLocation.getZ()));

        return new Element(identify()).getAttribute(attribute.fulfill(0));
    }

}
