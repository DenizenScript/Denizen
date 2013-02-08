package net.aufdemrand.denizen.utilities;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;
import org.bukkit.craftbukkit.v1_4_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_4_R1.entity.CraftLivingEntity;
import net.minecraft.server.v1_4_R1.EntityLiving;
import java.util.*;

/**
 * This class has utility methods for various tasks.
 * 
 * @author aufdemrand, dbixler, davidcernat, AgentK
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
		
	/**
	 * This utility changes an entity's yaw and pitch to make it face
	 * a location.
	 * 
	 * Thanks to fullwall for it.
	 * 
	 * @param  from  The Entity whose yaw and pitch you want to change.
	 * @param at  The Location it should be looking at.
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
	 * @param  from  The Entity whose yaw and pitch you want to change.
	 * @param at  The Entity it should be looking at.
	 */
	
	public static void faceEntity(Entity from, Entity at) {
		
		faceLocation(from, at.getLocation());
	}
	
	/**
	 * This utility normalizes Mincraft's yaws (which can be negative or
	 * can exceed 360) and turns them into proper yaw values that only go from
	 * 0 to 359.
	 * 
	 * @param  yaaw  The original yaw.
	 * @return  The normalized yaw.
	 */
	
	public static double normalizeYaw(double yaw)
	{
    	yaw = (yaw - 90) % 360;
    	if (yaw < 0)
            yaw += 360.0;
    	return yaw;
	}
	
	/**
	 * This utility checks if an Entity is facing a Location.
	 * 
	 * @param  from  The Entity we check.
	 * @param at  The Location we want to know if it is looking at.
	 * @return  Returns a boolean.
	 */
	
	public static boolean isFacingLocation(Entity from, Location at) {
		
		double currentYaw;
		
		if (from instanceof Player) // need to subtract 90 from player yaws
			currentYaw = normalizeYaw(from.getLocation().getYaw() - 90);
		else
			currentYaw = normalizeYaw(from.getLocation().getYaw());
		
		double requiredYaw = normalizeYaw(getYaw(at.toVector().subtract(
							 from.getLocation().toVector()).normalize()));
		
		if (Math.abs(requiredYaw - currentYaw) < 45 ||
			Math.abs(requiredYaw + 360 - currentYaw) < 45 ||
			Math.abs(currentYaw + 360 - requiredYaw) < 45)
			return true;
    	
		return false;
	}
	
	/**
	 * This utility checks if an Entity is facing another Entity.
	 * 
	 * @param  from  The Entity we check.
	 * @param at  The Entity we want to know if it is looking at.
	 * @return  Returns a boolean.
	 */
	
	public static boolean isFacingEntity(Entity from, Entity at) {
		
		return isFacingLocation(from, at.getLocation());
	}

    /**
     * Converts a vector to a yaw.
     * 
     * Thanks to bergerkiller
     * 
     * @param  vector  The vector you want to get a yaw from.
     * @return  The yaw.
     */
    
    public static float getYaw(Vector vector) {
        double dx = vector.getX();
        double dz = vector.getZ();
        double yaw = 0;
        // Set yaw
        if (dx != 0) {
            // Set yaw start value based on dx
            if (dx < 0) {
                yaw = 1.5 * Math.PI;
            } else {
                yaw = 0.5 * Math.PI;
            }
            yaw -= Math.atan(dz / dx);
        } else if (dz < 0) {
            yaw = Math.PI;
        }
        return (float) (-yaw * 180 / Math.PI - 90);
    }
    
    /**
     * Converts a yaw to a cardinal direction name.
     * 
     * Thanks to sk89qs
     * 
     * @param  yaw  The yaw you want to get a cardinal direction from.
     * @return  The name of the cardinal direction as a String.
     */
    
    public static String getCardinal(double yaw) {
    
    	yaw = normalizeYaw(yaw);
    	
        if (0 <= yaw && yaw < 22.5) {
            return "north";
        } else if (22.5 <= yaw && yaw < 67.5) {
            return "northeast";
        } else if (67.5 <= yaw && yaw < 112.5) {
            return "east";
        } else if (112.5 <= yaw && yaw < 157.5) {
            return "southeast";
        } else if (157.5 <= yaw && yaw < 202.5) {
            return "south";
        } else if (202.5 <= yaw && yaw < 247.5) {
            return "southwest";
        } else if (247.5 <= yaw && yaw < 292.5) {
            return "west";
        } else if (292.5 <= yaw && yaw < 337.5) {
            return "northwest";
        } else if (337.5 <= yaw && yaw < 360.0) {
            return "north";
        } else {
            return null;
        }
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
