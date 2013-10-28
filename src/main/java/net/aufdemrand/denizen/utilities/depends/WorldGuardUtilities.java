package net.aufdemrand.denizen.utilities.depends;

import java.util.ArrayList;
import java.util.List;

import net.aufdemrand.denizen.objects.dList;
import net.aufdemrand.denizen.utilities.debugging.dB;

import org.bukkit.Location;

import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;


public class WorldGuardUtilities {

    /**
     * Get all the regions a location is in, as a dList.
     *
     * @param location The location to check
     *
     * @return A dList of the regions
     */

    public static dList getRegions(Location location) {
        if (Depends.worldGuard == null) return null;

        List<String> regionList = new ArrayList<String>();

        ApplicableRegionSet currentRegions = Depends.worldGuard.getRegionManager
                (location.getWorld()).getApplicableRegions(location);

        for(ProtectedRegion thisRegion: currentRegions) {
            regionList.add(thisRegion.getId());
        }

        return new dList(regionList);
    }

    /**
     * Determine if a location is inside any Worldguard
     * region.
     *
     * @param location The location to check
     *
     * @return Returns a boolean value
     */

    public static boolean inRegion(Location location) {
        if (Depends.worldGuard == null) return false;

        ApplicableRegionSet currentRegions = Depends.worldGuard.getRegionManager
                (location.getWorld()).getApplicableRegions(location);

        return (currentRegions.size() > 0);
    }

    /**
     * Determine if a location is inside a specific WorldGuard
     * region.
     *
     * @param location The location to check
     * @param region The WorldGuard region to check
     *
     * @return Returns a boolean value
     */

    public static boolean inRegion(Location location, String region) {
        if (Depends.worldGuard == null) return false;

        ApplicableRegionSet currentRegions = Depends.worldGuard.getRegionManager
                (location.getWorld()).getApplicableRegions(location);

        for(ProtectedRegion thisRegion: currentRegions) {
            if (thisRegion.getId().equalsIgnoreCase(region)) {
                return true;
            }
        }
        return false;
    }
}
