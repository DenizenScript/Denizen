package net.aufdemrand.denizen.utilities.depends;

import net.aufdemrand.denizen.utilities.debugging.dB;

import org.bukkit.entity.Player;

import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;


public class WorldGuardUtilities {
	
	/**
	 * This method is used to determine if the player is in
	 * a specified WorldGuard region.
	 * 
	 * @param thePlayer The player to check
	 * @param region The WorldGuard region to check
	 * 
	 * @return returns a boolean value
	 */
	public static boolean checkPlayerWGRegion(Player thePlayer, String region) {
		if (Depends.worldGuard == null) return false;
		boolean inRegion = false;
		ApplicableRegionSet currentRegions = Depends.worldGuard.getRegionManager(thePlayer.getWorld()).getApplicableRegions(thePlayer.getLocation());
		for(ProtectedRegion thisRegion: currentRegions){
			dB.echoDebug("...checking current player region: " + thisRegion.getId());
			if (thisRegion.getId().equalsIgnoreCase(region)) {
				inRegion = true;
				dB.echoDebug("...matched region");
			} 
		}
		return inRegion;
	}
}
