package net.aufdemrand.denizen.npc;

import java.rmi.activation.ActivationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.scriptEngine.triggers.ChatTrigger;
import net.aufdemrand.denizen.scriptEngine.triggers.ClickTrigger;
import net.aufdemrand.denizen.scriptEngine.triggers.DamageTrigger;
import net.aufdemrand.denizen.scriptEngine.triggers.LocationTrigger;
import net.aufdemrand.denizen.scriptEngine.triggers.ProximityTrigger;
import net.citizensnpcs.api.event.NPCEvent;
import net.citizensnpcs.api.event.NPCRemoveEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.event.NPCSpawnEvent;
import net.citizensnpcs.api.npc.NPC;

public class DenizenNPCRegistry implements Listener {

	private Map<NPC, DenizenNPC> denizenNPCs = new HashMap<NPC, DenizenNPC>();

	public Denizen plugin;

	public DenizenNPCRegistry(Denizen denizen) {
		plugin = denizen;
	}


	public void registerNPC(NPC citizensNPC) {
		if (!denizenNPCs.containsKey(citizensNPC)) {
			denizenNPCs.put(citizensNPC, new DenizenNPC(citizensNPC));

		}
	}

	
	public void removeNPC(NPCRemoveEvent event) {
		if (isDenizenNPC(event.getNPC()))
			denizenNPCs.remove(event.getNPC());
	}

	
	public DenizenNPC getDenizen(NPC citizensNPC) {
		if (citizensNPC.hasTrait(DenizenTrait.class))
			if (denizenNPCs.containsKey(citizensNPC))
				return denizenNPCs.get(citizensNPC);
		return null;
	}

	
	public boolean isDenizenNPC (NPC citizensNPC) {
		if (denizenNPCs.containsKey(citizensNPC)) 
			return true;
		return false;
	}

	
	public Map<NPC, DenizenNPC> getDenizens() {
		return denizenNPCs;
	}

	
	/*
	 * getClosest
	 * 
	 * Gets a NPC object of the closest Denizen to the specified Player.
	 * 
	 */

	public DenizenNPC getClosest (Player thePlayer, int Range) {

		Double closestDistance = Double.valueOf(String.valueOf(Range));
		DenizenNPC closestDenizen = null;

		if (getDenizens().isEmpty()) return null;

		for (DenizenNPC aDenizen : getDenizens().values()) {
			if (aDenizen.isSpawned()
					&& aDenizen.getWorld().equals(thePlayer.getWorld())
					&& aDenizen.getLocation().distance(thePlayer.getLocation()) < closestDistance ) {
				closestDenizen = aDenizen; 
				closestDistance = aDenizen.getLocation().distance(thePlayer.getLocation());
			}
		}

		return closestDenizen;
	}

	


	/*
	 * getInRange
	 * 
	 * Gets a List<DenizenNPC> of Denizens within a range of the specified Player.
	 * 
	 */

	public List<DenizenNPC> getInRange (Player thePlayer, int theRange) {

		List<DenizenNPC> DenizensWithinRange = new ArrayList<DenizenNPC>();

		if (plugin.getDenizenNPCRegistry().getDenizens().isEmpty()) return DenizensWithinRange;

		for (DenizenNPC aDenizenList : plugin.getDenizenNPCRegistry().getDenizens().values()) {
			if (aDenizenList.isSpawned()
					&& aDenizenList.getWorld().equals(thePlayer.getWorld()) 
					&& aDenizenList.getLocation().distance(thePlayer.getLocation()) < theRange)

				DenizensWithinRange.add(aDenizenList);
		}

		return DenizensWithinRange;
	}


}
