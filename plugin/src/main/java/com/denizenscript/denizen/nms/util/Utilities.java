package com.denizenscript.denizen.nms.util;

import com.denizenscript.denizen.nms.interfaces.BlockHelper;
import com.denizenscript.denizen.nms.NMSHandler;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Utilities {

    private static final Random random = new Random();

    /**
     * Gets a Location within a range that an entity can walk in.
     *
     * @param location the Location to check with
     * @param range    the range around the Location
     * @return a random Location within range, or null if no Location within range is safe
     */
    public static Location getWalkableLocationNear(Location location, int range) {
        List<Location> locations = new ArrayList<>();
        location = location.getBlock().getLocation();

        // Loop through each location within the range
        for (double x = -(range); x <= range; x++) {
            for (double y = -(range); y <= range; y++) {
                for (double z = -(range); z <= range; z++) {
                    // Add each block location within range
                    Location loc = location.clone().add(x, y, z);
                    if (checkLocation(location, loc, range) && isWalkable(loc)) {
                        locations.add(loc);
                    }
                }
            }
        }

        // No safe Locations found
        if (locations.isEmpty()) {
            return null;
        }

        // Return a random Location from the list
        return locations.get(random.nextInt(locations.size()));
    }

    public static boolean isWalkable(Location location) {
        BlockHelper blockHelper = NMSHandler.getInstance().getBlockHelper();
        return !blockHelper.isSafeBlock(location.clone().subtract(0, 1, 0).getBlock().getType())
                && blockHelper.isSafeBlock(location.getBlock().getType())
                && blockHelper.isSafeBlock(location.clone().add(0, 1, 0).getBlock().getType());
    }

    /**
     * Checks entity's location against a Location (with leeway). Should be faster than
     * bukkit's built in Location.distance(Location) since there's no sqrt math.
     *
     * @return true if within the specified location, false otherwise.
     */
    public static boolean checkLocation(Location baseLocation, Location theLocation, double theLeeway) {

        if (!baseLocation.getWorld().getName().equals(theLocation.getWorld().getName())) {
            return false;
        }

        return baseLocation.distanceSquared(theLocation) < theLeeway * theLeeway;
    }
}
