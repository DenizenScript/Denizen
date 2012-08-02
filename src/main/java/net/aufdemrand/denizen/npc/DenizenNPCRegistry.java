package net.aufdemrand.denizen.npc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import net.aufdemrand.denizen.Denizen;

import net.citizensnpcs.api.event.NPCRemoveEvent;
import net.citizensnpcs.api.npc.NPC;

public class DenizenNPCRegistry implements Listener {

	private Map<NPC, DenizenNPC> denizenNPCs = new ConcurrentHashMap<NPC, DenizenNPC>();

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
	    Iterator<Entry<NPC, DenizenNPC>> it = denizenNPCs.entrySet().iterator();

	    while (it.hasNext()) {
	        Map.Entry<NPC, DenizenNPC> npc = (Map.Entry<NPC, DenizenNPC>)it.next();
	        
	    	try {
				npc.getKey().getBukkitEntity();
				} catch (NullPointerException e) {
					denizenNPCs.remove(npc.getKey());
					if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "Removed NPC from DenizenRegistry. The bukkit entity has been removed.");
				}
	    }
		
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


	
	/**
	 * Gets players in range of a bukkit Entity. 
	 *
	 * @param  theEntity  the bukkit Entity to check for players around.
	 * @param  theRange  the Range, in blocks, to check around theEntity.
	 * @return  returns a list of Players around theEntity.
	 */

	public List<Player> getInRange (LivingEntity theEntity, int theRange) {

		List<Player> PlayersWithinRange = new ArrayList<Player>();

		Player[] DenizenPlayers = plugin.getServer().getOnlinePlayers();

		for (Player aPlayer : DenizenPlayers) {
			if (aPlayer.isOnline() 
					&& aPlayer.getWorld().equals(theEntity.getWorld()) 
					&& aPlayer.getLocation().distance(theEntity.getLocation()) < theRange)
				PlayersWithinRange.add(aPlayer);
		}

		return PlayersWithinRange;
	}

	
	
	/**
	 * Gets players in range of a bukkit Entity, excluding a specified Player. 
	 *
	 * @param  theEntity  the bukkit Entity to check for players around.
	 * @param  theRange  the Range, in blocks, to check around theEntity.
	 * @param  excludePlayer  the bukkit Player to exclude from the returned list.
	 * @return  returns a list of Players around theEntity, excluding the excludePlayer.
	 */

	public List<Player> getInRange (LivingEntity theEntity, int theRange, Player excludePlayer) {

		List<Player> PlayersWithinRange = getInRange(theEntity, theRange);
		PlayersWithinRange.remove(excludePlayer);

		return PlayersWithinRange;
	}


	
	/**
	 * Checks a Player's location against a Location (with leeway). Should be faster than
	 * bukkit's built in Location.distance(Location) since there's no sqrt math.
	 * 
	 * Thanks chainsol :)
	 */

	public boolean checkLocation(Player thePlayer, Location theLocation, int theLeeway) {

		if (!thePlayer.getWorld().getName().equals(theLocation.getWorld().getName()))
			return false;
		
		if (Math.abs(thePlayer.getLocation().getBlockX() - theLocation.getBlockX()) 
				> theLeeway) return false;
		if (Math.abs(thePlayer.getLocation().getBlockY() - theLocation.getBlockY()) 
				> theLeeway) return false;
		if (Math.abs(thePlayer.getLocation().getBlockZ() - theLocation.getBlockZ()) 
				> theLeeway) return false;

		return true;
	}


	
	/**
	 * Checks a Denizen's location against a Location (with leeway). Should be faster than
	 * bukkit's built in Location.distance(Location) since there's no sqrt math.
	 * 
	 * Thanks chainsol :)
	 */

	public boolean checkLocation(DenizenNPC theDenizen, Location theLocation, int theLeeway) {

		if (!theDenizen.getWorld().getName().equals(theLocation.getWorld().getName()))
			return false;
		
		if (Math.abs(theDenizen.getLocation().getBlockX() - theLocation.getBlockX()) 
				> theLeeway) return false;
		if (Math.abs(theDenizen.getLocation().getBlockY() - theLocation.getBlockY()) 
				> theLeeway) return false;
		if (Math.abs(theDenizen.getLocation().getBlockZ() - theLocation.getBlockZ()) 
				> theLeeway) return false;

		return true;
	}
}
