package net.aufdemrand.denizen.scripts.requirements.core;

import java.util.Iterator;
import java.util.List;

import net.aufdemrand.denizen.exceptions.RequirementCheckException;
import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.scripts.requirements.AbstractRequirement;
import net.citizensnpcs.command.exception.RequirementMissingException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class WorldGuardRegionRequirement extends AbstractRequirement {

	@Override
	public void onEnable() {
		// TODO Auto-generated method stub
		
	}
	
	/* INREGION [NAME:regionname]

	/* Arguments: [] - Required, () - Optional 
	 * [NAME:regionname] region to check if player is in.
	 * 
	 * Example usages:
	 * INREGION NAME:ilovejeebiss
	 */

	WorldGuardPlugin WorldGuard = null;

	@Override
	public boolean check(Player player, DenizenNPC npc, String scriptName,
			List<String> args) throws RequirementCheckException {
		
		//initialize variables
		boolean outcome = false;
		boolean inRegion = false;
		World theWorld = player.getWorld();
		Location playerLocation = player.getLocation();
		
		if (WorldGuard == null) WorldGuard = (WorldGuardPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
		
		if (args == null)
			throw new RequirementCheckException("Must provide a NAME:regionname!");
		
		//parse the args
		for (String thisArg : args) {

			if (thisArg.contains("NAME:")) {
				
				dB.echoDebug("...checking if player is in region!");
				String argRegion = aH.getStringFrom(thisArg);
				dB.echoDebug("...region to check: " + argRegion);
				ApplicableRegionSet currentRegions = WorldGuard.getRegionManager(theWorld).getApplicableRegions(playerLocation);
				
				//checks all regions player is currently in
				for(ProtectedRegion thisRegion: currentRegions){
					dB.echoDebug("...checking current player region: " + thisRegion.getId());
					if (thisRegion.getId().contains(argRegion)) {
						inRegion = true;
						dB.echoDebug("...matched region");
					} 
					if (inRegion == true) {
						//leave loop, region found
						break;
					}
				}
			}

			else dB.echoError("Could not match argument '%s'!", thisArg);
		}
		
		if (inRegion) outcome = true;
		
		//check the outcome
		if (outcome == true) dB.echoDebug("...player in region!");
		else dB.echoDebug("...player is not in region!");
		
		// No boolean for negative requirement like there was in 0.76?
		//if (negativeRequirement != outcome) return true;

		return false;
	}

}