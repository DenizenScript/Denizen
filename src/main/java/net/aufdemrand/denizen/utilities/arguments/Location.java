package net.aufdemrand.denizen.utilities.arguments;

import net.aufdemrand.denizen.utilities.DenizenAPI;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Location extends org.bukkit.Location implements dScriptArgument {

    public static Map<String, Location> locations = new HashMap<String, Location>();

    /**
     * Gets a saved location based on an Id.
     *
     * @param id  the Id key of the location
     * @return  the Location associated
     */
    public static Location getSavedLocation(String id) {
        if (locations.containsKey(id.toLowerCase()))
            return locations.get(id.toLowerCase());
        else return null;
    }

    /**
     * Checks if there is a saved location with this Id.
     *
     * @param id  the Id to check
     * @return  true if it exists, false if not
     */
    public static boolean isSavedLocation(String id) {
        return locations.containsKey(id.toLowerCase());
    }

    /**
     * Called on server startup or /denizen reload locations. Should probably not be called manually.
     */
    public static void _recallLocations() {
        List<String> loclist = DenizenAPI.getCurrentInstance().getSaves().getStringList("dScript.Locations");
        locations.clear();
        for (String location : loclist) {
            Location loc = valueOf(location);
        }
    }

    /**
     * Called by Denizen internally on a server shutdown or /denizen save. Should probably
     * not be called manually.
     */
    public static void _saveLocations() {
        List<String> loclist = new ArrayList<String>();
        for (Map.Entry<String, Location> entry : locations.entrySet())
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
    public static Location valueOf(String string) {
        // Strip prefix (ie. location:...)
        if (string.split(":").length > 1)
            string = string.split(":", 2)[1];
        // Split values
        String[] split = string.split(",");
        // If 5 values, contains an id
        if (split.length == 5)
        try {
            return new Location(split[0], Bukkit.getWorld(split[4]),
                    Double.valueOf(split[1]),
                    Double.valueOf(split[2]),
                    Double.valueOf(split[3]));
        } catch(Exception e) {
            return null;
        }
        // If 4 values, standard id-less dScript location format
        else if (split.length == 4) {
            try {
                return new Location(Bukkit.getWorld(split[3]),
                        Double.valueOf(split[0]),
                        Double.valueOf(split[1]),
                        Double.valueOf(split[2]));
            } catch(Exception e) {
                return null;
            }
        }
        return null;
    }


    private String Id;
    private String prefix = "Location";

    /**
     * Creates a new saved Location. dLocations should only be given an 'Id'
     * if they should be saved for later. dLocations with an 'Id' are persisted
     * and can be recalled at any time with {@link #getSavedLocation(String)}
     *
     * @param id saved Id of the Location
     * @param location the Bukkit Location to reference
     */
    public Location(String id, org.bukkit.Location location) {
        super(location.getWorld(), location.getX(), location.getY(), location.getZ());
        this.Id = id.toLowerCase();
        locations.put(Id, this);
    }

    /**
     * Turns a Bukkit Location into a Location, which has some helpful methods
     * for working with dScript. If working with temporary locations, this is
     * a much better method to use than {@link #Location(String, org.bukkit.Location)}.
     *
     * @param location the Bukkit Location to reference
     */
    public Location(org.bukkit.Location location) {
        super(location.getWorld(), location.getX(), location.getY(), location.getZ());
    }

    /**
     * Creates a new saved Location. dLocations should only be given an 'Id'
     * if they should be saved for later. dLocations with an 'Id' are persisted
     * and can be recalled at any time with {@link #getSavedLocation(String)}
     *
     * @param id  saved Id of the Location
     * @param world  the Bukkit World referenced
     * @param x  the x-coordinate of the location
     * @param y  the y-coordinate of the location
     * @param z  the z-coordinate of the location
     *
     */
    public Location(String id, World world, double x, double y, double z) {
        super(world, x, y, z);
        this.Id = id.toLowerCase();
        locations.put(Id, this);
    }

    /**
     * Turns a world and coordinates into a Location, which has some helpful methods
     * for working with dScript. If working with temporary locations, this is
     * a much better method to use than {@link #Location(String, org.bukkit.World, double, double, double)}.
     *
     * @param world  the world in which the location resides
     * @param x  x-coordinate of the location
     * @param y  y-coordinate of the location
     * @param z  z-coordinate of the location
     *
     */
    public Location(World world, double x, double y, double z) {
        super(world, x, y, z);
    }

    @Override
    public String getDefaultPrefix() {
        return prefix;
    }

    @Override
    public String debug() {
        return (Id != null ? "<G>" + prefix + "='<A>" + Id + "(<Y>" + getBlockX() + "," + getBlockY()
                + "," + getBlockZ() + "," + getWorld().getName() + "<A>)<G>'  "
                : "<G>" + prefix + "='<Y>" + getBlockX() + "," + getBlockY()
                + "," + getBlockZ() + "," + getWorld().getName() + "<G>'  ");
    }

    @Override
    public String dScriptArg() {
        return getBlockX() + "," + getBlockY()
                + "," + getBlockZ() + "," + getWorld().getName();
    }

    @Override
    public String dScriptArgValue() {
        return getDefaultPrefix().toLowerCase() + ":" + dScriptArg();
    }

    @Override
    public String toString() {
        if (Id == null) return null;
        return Id + "," + getX() + "," + getY()
                + "," + getZ() + "," + getWorld().getName();
    }

    @Override
    public dScriptArgument setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

}
