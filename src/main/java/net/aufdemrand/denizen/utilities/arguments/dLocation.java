package net.aufdemrand.denizen.utilities.arguments;

import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Press
 * Date: 1/25/13
 * Time: 7:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class dLocation extends Location {

    public static Map<String, dLocation> locations = new HashMap<String, dLocation>();

    /**
     * Gets a saved location based on an Id.
     *
     * @param id  the Id key of the location
     * @return  the dLocation associated
     */
    public static dLocation getSavedLocation(String id) {
        if (locations.containsKey(id.toLowerCase()))
            return locations.get(id.toLowerCase());
        else return null;
    }

    /**
     * Gets a dLocation from a string form of id;world;x;y;z
     *
     * @param string  the string
     * @return  a dLocation, or null if invalid
     *
     */
    public static dLocation valueOf(String string) {
        String[] split = string.split(";");
        try {
            return new dLocation(split[0].toLowerCase(), Bukkit.getWorld(split[1]),
                    Double.valueOf(split[2]),
                    Double.valueOf(split[3]),
                    Double.valueOf(split[4]));
        } catch(Exception e) {
            return null;
        }
    }

    /**
     * Called on server startup or /denizen reload locations
     */
    public static void recallLocations() {
        List<String> loclist = DenizenAPI.getCurrentInstance().getSaves().getStringList("dScript.Locations");
        for (String location : loclist) {
            dLocation loc = valueOf(location);
            if (loc != null)
                locations.put(location.split(";")[0], loc);
            else
                dB.log("<G>Invalid saved location in saves.yml: '<Y>" + location + "<G>'");
        }
    }




    private String Id;

    /**
     * Creates a new saved dLocation. dLocations should only be given an 'Id'
     * if they should be saved for later. dLocations with an 'Id' are persisted
     * and can be recalled at any time with {@link #getSavedLocation(String)}
     *
     * @param id saved Id of the dLocation
     * @param location the Bukkit Location to reference
     */
    public dLocation(String id, Location location) {
        super(location.getWorld(), location.getX(), location.getY(), location.getZ());
        this.Id = id.toLowerCase();
        locations.put(Id, this);
    }

    /**
     * Turns a Bukkit Location into a dLocation, which has some helpful methods
     * for working with dScript. If working with temporary locations, this is
     * a much better method to use than {@link #dLocation(String, org.bukkit.Location)}.
     *
     * @param location the Bukkit Location to reference
     */
    public dLocation(Location location) {
        super(location.getWorld(), location.getX(), location.getY(), location.getZ());
    }

    /**
     * Creates a new saved dLocation. dLocations should only be given an 'Id'
     * if they should be saved for later. dLocations with an 'Id' are persisted
     * and can be recalled at any time with {@link #getSavedLocation(String)}
     *
     * @param id  saved Id of the dLocation
     * @param world  the Bukkit World referenced
     * @param x  the x-coordinate of the location
     * @param y  the y-coordinate of the location
     * @param z  the z-coordinate of the location
     *
     */
    public dLocation(String id, World world, double x, double y, double z) {
        super(world, x, y, z);
        this.Id = id.toLowerCase();
        locations.put(Id, this);
    }

    /**
     * Converts the dLocation into a dB friendly format, including standard color codes.
     *
     * @return  a color coded context String of the dLocation
     *
     */
    @Override
    public String toString() {
        if (getLocation() == null) return null;
        return (Id != null ? "<G>Location='<A>" + Id + "(<Y>" + getBlockX() + "," + getBlockY()
                + "," + getBlockZ() + "," + getWorld().getName() + "<A>)<G>'"
                : "<G>Location='<Y>" + getBlockX() + "," + getBlockY()
                + "," + getBlockZ() + "," + getWorld().getName() + "<G>'");
    }

    /**
     * <p>Converts the dLocation into a valid dScript location argument.</p>
     *
     * <tt>location:x,y,z,world</tt>
     *
     * @return  dScript location argument
     *
     */
    public String as_dScript() {
        if (getLocation() == null) return null;
        return "location: " + getBlockX() + "," + getBlockY()
                + "," + getBlockZ() + "," + getWorld().getName();
    }

    /**
     * Formats the dLocation in a way in which it can be 'serialized' and later retrieved with
     * {@link #valueOf(String)}. The dLocation must have an Id.
     *
     * @return exact context of the dLocation
     */
    public String asString() {
        if (getLocation() == null || Id == null) return null;
        return Id + ";" + getX() + ";" + getY()
                + ";" + getZ() + ";" + getWorld().getName();
    }

    /**
     * Retrieves the Bukkit Location associated.
     *
     * @return a Bukkit Location
     */
    public Location getLocation() {
        return getLocation();
    }


}
