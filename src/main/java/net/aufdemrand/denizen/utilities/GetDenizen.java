package net.aufdemrand.denizen.utilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.DenizenCharacter;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class GetDenizen {

	private Denizen plugin;

	public GetDenizen(Denizen denizen) {
		plugin = denizen;
	}


	/*
	 * getClosest
	 * 
	 * Gets a NPC object of the closest Denizen to the specified Player.
	 * 
	 */

	public NPC getClosest (Player thePlayer, int Range) {

		Double closestDistance = Double.valueOf(String.valueOf(Range));
		NPC closestDenizen = null;

		if (plugin.utilities.getDenizens().isEmpty()) return null;

		for (NPC aDenizen : plugin.utilities.getDenizens()) {
			if (aDenizen.isSpawned()
					&& aDenizen.getBukkitEntity().getWorld().equals(thePlayer.getWorld())
					&& aDenizen.getBukkitEntity().getLocation().distance(thePlayer.getLocation()) < closestDistance ) {
				closestDenizen = aDenizen; 
				closestDistance = aDenizen.getBukkitEntity().getLocation().distance(thePlayer.getLocation());
			}
		}

		return closestDenizen;
	}

	
	


	/*
	 * getInRange
	 * 
	 * Gets a List<NPC> of Denizens within a range of the specified Player.
	 * 
	 */

	public List<NPC> getInRange (Player thePlayer, int theRange) {

		List<NPC> DenizensWithinRange = new ArrayList<NPC>();

		if (plugin.utilities.getDenizens().isEmpty()) return DenizensWithinRange;

		for (NPC aDenizenList : plugin.utilities.getDenizens()) {
			if (aDenizenList.isSpawned()
					&& aDenizenList.getBukkitEntity().getWorld().equals(thePlayer.getWorld()) 
					&& aDenizenList.getBukkitEntity().getLocation().distance(thePlayer.getLocation()) < theRange)

				DenizensWithinRange.add(aDenizenList);
		}

		return DenizensWithinRange;
	}




	
	
	
	
	
	
	
	
	
}
