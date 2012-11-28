package net.aufdemrand.denizen.utilities;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

public class Utilities {

	/* 
	 * Used to check against, same as EntityType, but minus a few that are useless and/or could cause problems spawning
	 * arbitrarily, such as FISHING_HOOK, UNKNOWN, COMPLEX_PART, etc.
	 */
	
	private enum ValidEntities {
		BLAZE, BOAT, CAVE_SPIDER, CHICKEN, COW, CREEPER, ENDER_DRAGON, ENDERMAN, GHAST, GIANT, IRON_GOLEM, MAGMA_CUBE,
		MUSHROOM_COW, MINECART, OCELOT, PIG, PIG_ZOMBIE, PRIMED_TNT, SHEEP, SILVERFISH, SKELETON, SLIME, SNOWMAN,
		SQUID, VILLAGER, WOLF, ZOMBIE
	}

	public boolean isEntity(String theString) {

		for (ValidEntities entity : ValidEntities.values()) {
			if (entity.name().equals(theString.toUpperCase()))
				return true;
		}

		return false;
	}
	
	public int lastIndexOfUCL(String str) {        
	    for(int i=str.length()-1; i>=0; i--) {
	        if(Character.isUpperCase(str.charAt(i))) {
	            return i;
	        }
	    }
	    return -1;
	}
	
	public int lastIndexOfLCL(String str) {        
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



	
}
