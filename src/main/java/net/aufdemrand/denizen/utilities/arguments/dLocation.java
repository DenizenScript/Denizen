package net.aufdemrand.denizen.utilities.arguments;

import net.aufdemrand.denizen.interfaces.dScriptArgument;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class dLocation extends org.bukkit.Location implements dScriptArgument {

    public static Map<String, dLocation> locations = new ConcurrentHashMap<String, dLocation>(8, 0.9f, 1);

    /**
     * Gets a saved location based on an Id.
     *
     * @param id  the Id key of the location
     * @return  the Location associated
     */
    public static dLocation getSavedLocation(String id) {
        return locations.get(id.toLowerCase());
    }

    public static String isSavedLocation(Location location) {
        for (Map.Entry<String, dLocation> entry : locations.entrySet()) {
            if (Utilities.checkLocation(entry.getValue(), location, 1)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static dLocation saveLocationAs(dLocation location, String id) {
        locations.put(id.toLowerCase(), location);
        return location;
    }

    /**
     * Checks if there is a saved location with this Id.
     *
     * @param id  the Id to check
     * @return  true if it exists, false if not
     */
    public static boolean isSavedLocation(String id) {
        if (id == null) return false;
        return locations.containsKey(id.toLowerCase());
    }

    /**
     * Called on server startup or /denizen reload locations. Should probably not be called manually.
     */
    public static void _recallLocations() {
        List<String> loclist = DenizenAPI.getCurrentInstance().getSaves().getStringList("dScript.Locations");
        locations.clear();
        for (String location : loclist) {
            dLocation loc = valueOf(location);
            // TODO: Finish this
        }
    }

    /**
     * Called by Denizen internally on a server shutdown or /denizen save. Should probably
     * not be called manually.
     */
    public static void _saveLocations() {
        List<String> loclist = new ArrayList<String>();
        for (Map.Entry<String, dLocation> entry : locations.entrySet())
            loclist.add(entry.getValue().toString());

        DenizenAPI.getCurrentInstance().getSaves().set("dScript.Locations", loclist);
    }

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
        // Strip prefix (ie. location:...)
        if (string.split(":").length > 1)
            string = string.split(":", 2)[1];
        // Split values
        String[] split = string.split(",");

        if (split.length == 5)
            // If 5 values, contains an id with standard dScript location format
            try {
                return new dLocation(Bukkit.getWorld(split[4]),
                        Double.valueOf(split[1]),
                        Double.valueOf(split[2]),
                        Double.valueOf(split[3])).rememberAs(split[0]);
            } catch(Exception e) {
                return null;
            }

        else if (split.length == 4)
            // If 4 values, standard id-less dScript location format
            try {
                return new dLocation(Bukkit.getWorld(split[3]),
                        Double.valueOf(split[0]),
                        Double.valueOf(split[1]),
                        Double.valueOf(split[2]));
            } catch(Exception e) {
                return null;
            }


        else if (split.length == 6)
            // If 6 values, id-less location with pitch/yaw
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

    /**
     * Turns a Bukkit Location into a Location, which has some helpful methods
     * for working with dScript. If working with temporary locations, this is
     * a much better method to use than {@link #dLocation(Location)}.
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
        dLocation.saveLocationAs(this, id);
        return this;
    }

    String prefix = "Location";

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public String debug() {
        return (isSavedLocation(this) != null ? "<G>" + prefix + "='<A>" + isSavedLocation(this) + "(<Y>" + getX() + "," + getY()
                + "," + getZ() + "," + getWorld().getName() + "<A>)<G>'  "
                : "<G>" + prefix + "='<Y>" + getX() + "," + getY()
                + "," + getZ() + "," + getWorld().getName() + "<G>'  ");
    }

    @Override
    public String as_dScriptArgValue() {
        return getX() + "," + getY()
                + "," + getZ() + "," + getWorld().getName();
    }

    @Override
    public String toString() {
        if (isSavedLocation(this) != null)
            return "l@" + isSavedLocation(this);
        else return "l@" + getX() + "," + getY()
                + "," + getZ() + "," + getWorld().getName();
    }

    @Override
    public dLocation setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
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

        return dScriptArgValue();
    }

}
