package net.aufdemrand.denizen.utilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

/**
 * This class has utility methods for various tasks.
 * 
 * @author Aufdemrand, dbixler, AgentK
 */
public class Utilities {

	public static String arrayToString(String[] input, String glue){
		String output="";
		int length = input.length;
		int i = 1;
		for(String s : input){
			output.concat(s);
			i++;
			if(i!=length){
				output.concat(glue);
			}
		}
		return output;
	}

	public static int lastIndexOfUCL(String str) {        
	    for(int i=str.length()-1; i>=0; i--) {
	        if(Character.isUpperCase(str.charAt(i))) {
	            return i;
	        }
	    }
	    return -1;
	}
	
	public static int lastIndexOfLCL(String str) {        
	    for(int i=str.length()-1; i>=0; i--) {
	        if(Character.isLowerCase(str.charAt(i))) {
	            return i;
	        }
	    }
	    return -1;
	}
	
	/**
	 * Gets the plugin version from the maven info in the jar, if available.
	 * @return
	 */

	public String getVersionNumber() {

		Properties props = new Properties();

		//Set a default just in case.
		props.put("version", "Unknown development build");

		try	{
			props.load(this.getClass().getResourceAsStream("/META-INF/maven/net.aufdemrand/denizen/pom.properties"));
		} 
		catch(Exception e) {
			//Maybe log?
		}

		return props.getProperty("version");
	}

	public String getVersionString() {

		return "Denizen version: " + getVersionNumber();
	}
	
	/*
	 * Gets a Map of a player's inventory with a bukkit Material and Integer amount for each item. Unlike bukkit's build in getInventory, this will add up the total number of each Material. 
	 *
	 * @param  thePlayer  the Player whose inventory is being checked.
	 * @return  returns a Map<Material, Integer>.
	 */

	public Map<Material, Integer> getInventoryMap(Player thePlayer) {
		Map<Material, Integer> playerInv = new HashMap<Material, Integer>();
		ItemStack[] getContentsArray = thePlayer.getInventory().getContents();
		List<ItemStack> getContents = Arrays.asList(getContentsArray);

		for (int x=0; x < getContents.size(); x++) {
			if (getContents.get(x) != null) {

				if (playerInv.containsKey(getContents.get(x).getType())) {
					int t = playerInv.get(getContents.get(x).getType());
					t = t + getContents.get(x).getAmount(); playerInv.put(getContents.get(x).getType(), t);
				}

				else playerInv.put(getContents.get(x).getType(), getContents.get(x).getAmount());
			}
		}

		return playerInv;
	}

	/*
	 * Alternate usage that gets a Map of a player's inventory with a String representation of itemID:data and Integer amount for each item. Unlike bukkit's build in getInventory, this will add up the total number of each itemID. 
	 *
	 * @param  thePlayer  the Player whose inventory is being checked.
	 * @return  returns a Map<String, Integer>.
	 */

	public Map<String, Integer> getInventoryIdMap(Player thePlayer) {

		Map<String, Integer> playerInv = new HashMap<String, Integer>();
		ItemStack[] getContentsArray = thePlayer.getInventory().getContents();
		List<ItemStack> getContents = Arrays.asList(getContentsArray);

		for (int x=0; x < getContents.size(); x++) {
			if (getContents.get(x) != null) {
				MaterialData specificItem = getContents.get(x).getData();
				String friendlyItem = specificItem.getItemTypeId() + ":" + specificItem.getData();

				if (playerInv.containsKey(friendlyItem)) {
					int t = playerInv.get(friendlyItem);
					t = t + getContents.get(x).getAmount(); playerInv.put(friendlyItem, t);
				}
				else playerInv.put(friendlyItem, getContents.get(x).getAmount());
			}
		}

		return playerInv;
	}
	
	/**
	 * This is a Utility method for finding the closest NPC to a particular
	 * location.
	 * 
	 * @param location	The location to find the closest NPC to.
	 * @param range	The maximum range to look for the NPC.
	 * 
	 * @return	The closest NPC to the location, or null if no NPC was found
	 * 					within the range specified.
	 */
	public static NPC getClosestNPC (Location location, int range) {
		NPC			closestNPC = null;
		Double	closestDistance = Double.valueOf(range);

		Iterator<NPC>	it = CitizensAPI.getNPCRegistry().iterator();
		while (it.hasNext ()) {
			NPC	npc = it.next ();
			if (npc.isSpawned()			&&
					npc.getBukkitEntity().getLocation().getWorld().equals(location.getWorld())	&&
					npc.getBukkitEntity().getLocation().distance(location) < closestDistance) {
				closestNPC = npc;
				closestDistance = npc.getBukkitEntity().getLocation().distance(location);
			}
		}
		
		return closestNPC;
	}

	/**
	 * Returns a list of all NPCs within a certain range.
	 * 
	 * @param location	The location to search.
	 * @param maxRange	The maximum range of the NPCs
	 * 
	 * @return	The list of NPCs within the max range.
	 */
	public static List<NPC> getClosestNPCs (Location location, int maxRange) {
		List<NPC> closestNPCs = new ArrayList<NPC> ();;

		Iterator<NPC>	it = CitizensAPI.getNPCRegistry().iterator();
		while (it.hasNext ()) {
			NPC	npc = it.next ();
			if (npc.isSpawned()			&&
					npc.getBukkitEntity().getLocation().getWorld().equals(location.getWorld())	&&
					npc.getBukkitEntity().getLocation().distance(location) < maxRange) {
				closestNPCs.add (npc);
			}
		}
		
		return closestNPCs;
	}
	
//    public DenizenNPC getClosestDenizen (Player thePlayer, int Range) {
//        Double closestDistance = Double.valueOf(String.valueOf(Range));
//        DenizenNPC closestDenizen = null;
//        if (getDenizens().isEmpty()) return null;
//        for (DenizenNPC aDenizen : getDenizens().values()) {
//            if (aDenizen.isSpawned()
//                    && aDenizen.getWorld().equals(thePlayer.getWorld())
//                    && aDenizen.getLocation().distance(thePlayer.getLocation()) < closestDistance ) {
//                closestDenizen = aDenizen; 
//                closestDistance = aDenizen.getLocation().distance(thePlayer.getLocation());
//            }
//        }
//        return closestDenizen;
//    }
//
//    public List<DenizenNPC> getDenizensInRange (Player thePlayer, int theRange) {
//        List<DenizenNPC> DenizensWithinRange = new ArrayList<DenizenNPC>();
//        if (denizen.getNPCRegistry().getDenizens().isEmpty()) return DenizensWithinRange;
//        for (DenizenNPC aDenizenList : denizen.getNPCRegistry().getDenizens().values()) {
//            if (aDenizenList.isSpawned()
//                    && aDenizenList.getWorld().equals(thePlayer.getWorld()) 
//                    && aDenizenList.getLocation().distance(thePlayer.getLocation()) < theRange)
//                DenizensWithinRange.add(aDenizenList);
//        }
//        return DenizensWithinRange;
//    }
//
//    public List<Player> getPlayersInRange (LivingEntity theEntity, int theRange) {
//        List<Player> PlayersWithinRange = new ArrayList<Player>();
//        Player[] DenizenPlayers = Bukkit.getServer().getOnlinePlayers();
//        for (Player aPlayer : DenizenPlayers) {
//            if (aPlayer.isOnline() 
//                    && aPlayer.getWorld().equals(theEntity.getWorld()) 
//                    && aPlayer.getLocation().distance(theEntity.getLocation()) < theRange)
//                PlayersWithinRange.add(aPlayer);
//        }
//        return PlayersWithinRange;
//    }
//
//    public List<Player> getPlayersInRange (LivingEntity theEntity, int theRange, Player excludePlayer) {
//        List<Player> PlayersWithinRange = getPlayersInRange(theEntity, theRange);
//        if (excludePlayer != null) PlayersWithinRange.remove(excludePlayer);
//        return PlayersWithinRange;
//    }
//
//    /**
//     * Checks entity's location against a Location (with leeway). Should be faster than
//     * bukkit's built in Location.distance(Location) since there's no sqrt math.
//     * 
//     * Thanks chainsol :)
//     */
//
//    public boolean checkLocation(LivingEntity entity, Location theLocation, int theLeeway) {
//
//        if (!entity.getWorld().getName().equals(theLocation.getWorld().getName()))
//            return false;
//
//        if (Math.abs(entity.getLocation().getBlockX() - theLocation.getBlockX()) 
//                > theLeeway) return false;
//        if (Math.abs(entity.getLocation().getBlockY() - theLocation.getBlockY()) 
//                > theLeeway) return false;
//        if (Math.abs(entity.getLocation().getBlockZ() - theLocation.getBlockZ()) 
//                > theLeeway) return false;
//
//        return true;
//    }

}
