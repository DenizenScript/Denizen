package net.aufdemrand.denizen.scripts.triggers.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.aufdemrand.denizen.npc.traits.TriggerTrait;
import net.aufdemrand.denizen.scripts.triggers.AbstractTrigger;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class LocationTrigger extends AbstractTrigger implements Listener {


	//
	// Used as the key for the locations that should be triggered
	// 
	private class Trigger {

		private int x;
		private int y;
		private int z;
		private String world;
		
		public Trigger(Location location) {
			x = location.getBlockX();
			y = location.getBlockY();
			z = location.getBlockZ();
			world = location.getWorld().getName();
		}

		public boolean matches(Location location) {
			if (Math.abs(location.getBlockX() - x) > maximumLocationDistanceSetting()) return false;
			if (Math.abs(location.getBlockY() - y) > maximumLocationDistanceSetting()) return false;
			if (Math.abs(location.getBlockZ() - z) > maximumLocationDistanceSetting()) return false;
			if (!location.getWorld().getName().equals(world)) return false;
			return true;
		}
		
		public int getDistance(Player player) {
			// TODO: get distance
			return 0;
		}

	}


	//
	// Used to store the data about a specific Trigger along with some matching methods
	//
	private class LocationContext {

		private String scriptName;
		private String stepName;
		private String locationName;
		private int radius;
		private int npcid;

		public LocationContext(int npcid, int radius, String scriptName, String stepName, String locationName) {
			this.scriptName = scriptName;
			this.stepName = stepName;
			this.locationName = locationName;
			this.npcid = npcid;
		}

		public int getRadius() {
			return radius;
		}

		public String getPath() {
			return scriptName + ".steps." + stepName + ".location trigger." + locationName + ".script"; 
		}

		public boolean matches(InteractContext interactContext) {
			if (interactContext.npcid != npcid) return false;
			if (!interactContext.scriptName.equalsIgnoreCase(this.scriptName)) return false;
			if (!interactContext.stepName.equalsIgnoreCase(this.stepName)) return false;
			return true;
		}
	}

	
	//
	// Used to store the data about a specific Trigger along with some matching methods
	//
	private class InteractContext {
		private String scriptName;
		private String stepName;
		private int npcid;

		public InteractContext(NPC npc, String scriptName, String stepName) {
			this.npcid = npc.getId();
			this.scriptName = scriptName;
			this.stepName = stepName;
		}
	}


	//
	// Stores all the Location Trigger points with context
	//
	private Map<Trigger, LocationContext> locationTriggers = new ConcurrentHashMap<Trigger, LocationContext>();

	
	//
	// Adds a Location Trigger
	//
	public void addLocation(Location location, NPC npc, String scriptName, String stepName, int radius, String locationName) {
		locationTriggers.put(new Trigger(location), new LocationContext(npc.getId(), radius, scriptName, stepName, locationName));
	}

	
	//
	// Used on a reload to clear the list in anticipation for a rebuild
	//
	public void clearTriggers() {
		locationTriggers.clear();
	}


	public int maximumLocationDistanceSetting() {
		// TODO: hook into Settings 
		return 10;
	}

	
	//
	// Listens for a PlayerMoveEvent
	//
	@EventHandler
	public void checkLocation(PlayerMoveEvent event) {
		if (event.getFrom().getBlock() == event.getTo().getBlock()) return;
		Set<NPC> matchingNPCs = checkLocation (event.getPlayer().getLocation());
		
		
		if (matchingNPCs.size () > 0) {
			// Must be a return at this point, let's get some InteractContext from the matching NPCs
			List<InteractContext> interactContext = new ArrayList<InteractContext>();
	
			// Probe NPCs for context to compare with LocationContext (scriptName/stepName)
			for (NPC npc : matchingNPCs) {
				String interactScript = sH.getInteractScript(npc, event.getPlayer(), this.getClass());
				if (interactScript == null) continue;
				String step = denizen.getScriptEngine().getScriptHelper().getCurrentStep(event.getPlayer(), interactScript, false);
				interactContext.add(new InteractContext(npc, interactScript, step));
			}
	
			for (InteractContext ics : interactContext) {
			}
		}
	}

	/**
	 * This is a method that will find all matching NPCs that have location
	 * triggers based on a certain location.
	 * 
	 * @param location	The location to check
	 * 
	 * @return	The list of matching NPCs.
	 */
	public Set<NPC> checkLocation (Location location) {
		List<Trigger> matchingTriggers = new ArrayList<Trigger>();
		Set<NPC> matchingNPCs = new HashSet<NPC>();

		for (Trigger trigger : locationTriggers.keySet()) {
			if (trigger.matches(location)) {
				NPC npc = CitizensAPI.getNPCRegistry().getById(locationTriggers.get(trigger).npcid);
				// Check NPC (from npcID) for availability and overall trigger range
				// Has trait? Check. Trigger enabled? Check.
				if (!npc.hasTrait(TriggerTrait.class)) continue;
				if (!npc.getTrait(TriggerTrait.class).isEnabled(name)) continue;
				
				// Check location from LocationContext.. is it valid?
				
				// Add NPCID to list for checking interactScripts
				matchingNPCs.add(npc);
				
				// Location matches, add to list.
				matchingTriggers.add(trigger);
			}
		}
		
		return matchingNPCs;

		
		
		
	}


//				if (!npc.getTrait(TriggerTrait.class).triggerCooldownOnly(this, event.getPlayer())) continue;


	@EventHandler
	public void clickTrigger(NPCRightClickEvent event) {
		// Check if NPC has triggers.
		if (!event.getNPC().hasTrait(TriggerTrait.class)) return;
		// Check if trigger is enabled.
		if (!event.getNPC().getTrait(TriggerTrait.class).isEnabled(name)) return;

		// If engaged or not cool, calls On Unavailable, if cool, calls On Click
		// If available (not engaged, and cool) sets cool down and returns true. 
		if (!event.getNPC().getTrait(TriggerTrait.class).trigger(this, event.getClicker())) return;

		// Get Interact Script for Player/NPC
		String script = sH.getInteractScript(event.getNPC(), event.getClicker(), this.getClass());

		event.getNPC().getTrait(TriggerTrait.class).getRadius(name);
		// Parse Click Trigger, if unable to parse call No Click Trigger action
		if (!parse(denizen.getNPCRegistry().getDenizen(event.getNPC()), event.getClicker(), script))
			denizen.getNPCRegistry().getDenizen(event.getNPC()).action("no click trigger", event.getClicker());
	}
 
	@Override
	public void onEnable() {
		denizen.getServer().getPluginManager().registerEvents(this, denizen);
	}

}
