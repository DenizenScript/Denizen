package net.aufdemrand.denizen.utilities.depends;

import net.aufdemrand.denizen.utilities.debugging.dB;

import org.bukkit.Location;

import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;


public class WorldGuardUtilities {
	
	/**
	 * This method is used to determine if the player is in
	 * a specified WorldGuard region.
	 * 
	 * @param location The location to check
	 * @param region The WorldGuard region to check
	 * 
	 * @return returns a boolean value
	 */
	public static boolean checkWGRegion(Location location, String region) {
		if (Depends.worldGuard == null) return false;

		ApplicableRegionSet currentRegions = Depends.worldGuard.getRegionManager
				(location.getWorld()).getApplicableRegions(location);
		
		for(ProtectedRegion thisRegion: currentRegions){
			dB.echoDebug("...checking current region: " + thisRegion.getId());
			if (thisRegion.getId().equalsIgnoreCase(region)) {
				
				dB.echoDebug("...matched region");
				return true;
			} 
		}
		return false;
	}
}
