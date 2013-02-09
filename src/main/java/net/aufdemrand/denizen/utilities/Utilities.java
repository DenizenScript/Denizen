package net.aufdemrand.denizen.utilities;

import net.aufdemrand.denizen.npc.dNPC;
import net.minecraft.server.v1_4_R1.EntityLiving;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_4_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_4_R1.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

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


	/**
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
	public static dNPC getClosestNPC (Location location, int range) {
		dNPC closestNPC = null;
		Double	closestDistance = Double.valueOf(range);
		Iterator<dNPC>	it = DenizenAPI.getSpawnedNPCs().iterator();
		while (it.hasNext ()) {
			dNPC	npc = it.next ();
			if (npc.getLocation().getWorld().equals(location.getWorld())
				&& npc.getLocation().distance(location) < closestDistance) {
				closestNPC = npc;
				closestDistance = npc.getLocation().distance(location);
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
	public static Set<dNPC> getClosestNPCs (Location location, int maxRange) {
		Set<dNPC> closestNPCs = new HashSet<dNPC> ();
        Iterator<dNPC> it = DenizenAPI.getSpawnedNPCs().iterator();
		while (it.hasNext ()) {
			dNPC npc = it.next ();
			if (npc.getLocation().getWorld().equals(location.getWorld())
				&& npc.getLocation().distance(location) < maxRange) {
				closestNPCs.add (npc);
			}
		}
		return closestNPCs;
	}

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
