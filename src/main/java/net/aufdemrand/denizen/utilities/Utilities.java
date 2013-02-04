package net.aufdemrand.denizen.utilities;

import net.aufdemrand.denizen.utilities.debugging.dB;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.craftbukkit.v1_4_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_4_R1.entity.CraftLivingEntity;
import net.minecraft.server.v1_4_R1.EntityLiving;
import java.util.*;

/**
 * This class has utility methods for various tasks.
 * 
 * @author aufdemrand, dbixler, AgentK
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
	 * Gets a Map of a player's inventory with a bukkit Material and Integer amount 
	 * for each item. Unlike bukkit's build in getInventory, this will add up the 
	 * total number of each Material. 
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
	 * Alternate usage that gets a Map of a player's inventory with a 
	 * String representation of itemID:data and Integer amount for each 
	 * item. Unlike bukkit's build in getInventory, this will add up 
	 * the total number of each itemID. 
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
		
	/*
	 * This utility changes an entity's yaw and pitch to make it face
	 * a location.
	 * 
	 * Thanks to fullwall for it.
	 * 
	 * @param  from  the Entity whose yaw and pitch you want to change
	 * @param at  the Location it should be looking at
	 */
	
	public static void faceLocation(Entity from, Location at) {
		if (from.getWorld() != at.getWorld())
			return;
		Location loc = from.getLocation();

		double xDiff = at.getX() - loc.getX();
		double yDiff = at.getY() - loc.getY();
		double zDiff = at.getZ() - loc.getZ();

		double distanceXZ = Math.sqrt(xDiff * xDiff + zDiff * zDiff);
		double distanceY = Math.sqrt(distanceXZ * distanceXZ + yDiff * yDiff);

		double yaw = (Math.acos(xDiff / distanceXZ) * 180 / Math.PI);
		double pitch = (Math.acos(yDiff / distanceY) * 180 / Math.PI) - 90;
		if (zDiff < 0.0) {
			yaw = yaw + (Math.abs(180 - yaw) * 2);
		}
				
		if (from instanceof LivingEntity)
		{
			EntityLiving handle = ((CraftLivingEntity) from).getHandle();
			handle.yaw = (float) yaw - 90;
			handle.pitch = (float) pitch;
			handle.az = handle.yaw;
		}
		else
		{
			net.minecraft.server.v1_4_R1.Entity handle = ((CraftEntity) from).getHandle();
			handle.yaw = (float) yaw - 90;
			handle.pitch = (float) pitch;
		}

	}

	/*
	 * This utility changes an entity's yaw and pitch to make it face
	 * another entity.
	 * 
	 * Thanks to fullwall for it.
	 * 
	 * @param  from  the Entity whose yaw and pitch you want to change
	 * @param at  the Entity it should be looking at
	 */
	
	public static void faceEntity(Entity from, Entity at) {
		
		faceLocation(from, at.getLocation());
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
		List<NPC> closestNPCs = new ArrayList<NPC> ();

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
	
//    public dNPC getClosestDenizen (Player thePlayer, int Range) {
//        Double closestDistance = Double.valueOf(String.valueOf(Range));
//        dNPC closestDenizen = null;
//        if (getDenizens().isEmpty()) return null;
//        for (dNPC aDenizen : getDenizens().values()) {
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
//    public List<dNPC> getDenizensInRange (Player thePlayer, int theRange) {
//        List<dNPC> DenizensWithinRange = new ArrayList<dNPC>();
//        if (denizen.getNPCRegistry().getDenizens().isEmpty()) return DenizensWithinRange;
//        for (dNPC aDenizenList : denizen.getNPCRegistry().getDenizens().values()) {
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
