package net.aufdemrand.denizen.utilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.aufdemrand.denizen.npc.DenizenNPC;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
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


    // TODO: Use paginator on info-click

    //    public void showInfo(Player thePlayer, DenizenNPC theDenizen) {
    //
    //        thePlayer.sendMessage(ChatColor.GOLD + "------ Denizen Info ------");
    //
    //        /* Show Citizens NPC info. */
    //
    //        thePlayer.sendMessage(ChatColor.GRAY + "C2 NPCID: " + ChatColor.GREEN + theDenizen.getId() + ChatColor.GRAY + "   Name: " + ChatColor.GREEN + theDenizen.getName() + ChatColor.GRAY + "   HPs: " + ChatColor.GREEN + theDenizen.getEntity().getHealth() + ChatColor.GRAY + "   GOAL CNTRLR: " + ChatColor.GREEN + String.valueOf(!Boolean.valueOf(theDenizen.getCitizen().getDefaultGoalController().isPaused())));
    //        thePlayer.sendMessage(ChatColor.GRAY + "PF RANGE: " + ChatColor.GREEN + theDenizen.getNavigator().getDefaultParameters().range() +  "   " + ChatColor.GRAY + "SPEED: " + ChatColor.GREEN + String.valueOf(theDenizen.getNavigator().getDefaultParameters().speed()) + "    " + ChatColor.GRAY + "AVOID WATER: " + ChatColor.GREEN + theDenizen.getNavigator().getDefaultParameters().avoidWater());
    //        thePlayer.sendMessage(ChatColor.GRAY + "NAVIGATING: " + ChatColor.GREEN + theDenizen.getNavigator().isNavigating() +  "   " + ChatColor.GRAY + "STATIONARY TICKS: " + ChatColor.GREEN + theDenizen.getNavigator().getDefaultParameters().stationaryTicks() +  "   " + ChatColor.GRAY + "PUSHABLE: " + ChatColor.GREEN + theDenizen.getCitizen().getTrait(PushableTrait.class).isToggled());
    //
    //        thePlayer.sendMessage("");
    //
    //        thePlayer.sendMessage(ChatColor.GRAY + "Trigger Status:");
    //        // for (String line : plugin.getSpeechEngine().getMultilineText(theDenizen.getCitizensEntity().getTrait(DenizenTrait.class).listTriggers()))
    //        //    thePlayer.sendMessage(line);
    //        thePlayer.sendMessage("");
    //
    //        /* Show Assigned Scripts. */
    //
    //        boolean scriptsPresent = false;
    //        thePlayer.sendMessage(ChatColor.GRAY + "Interact Scripts:");
    //        if (plugin.getAssignments().contains("Denizens." + theDenizen.getName() + ".Interact Scripts")) {
    //            if (!plugin.getAssignments().getStringList("Denizens." + theDenizen.getName() + ".Interact Scripts").isEmpty()) scriptsPresent = true;
    //            for (String scriptEntry : plugin.getAssignments().getStringList("Denizens." + theDenizen.getName() + ".Interact Scripts"))
    //                thePlayer.sendMessage(ChatColor.GRAY + "- " + ChatColor.GREEN + scriptEntry);
    //        }
    //        if (plugin.getAssignments().contains("Denizens." + theDenizen.getId() + ".Interact Scripts")) {
    //            if (!plugin.getAssignments().getStringList("Denizens." + theDenizen.getId() + ".Interact Scripts").isEmpty()) scriptsPresent = true;
    //            for (String scriptEntry : plugin.getAssignments().getStringList("Denizens." + theDenizen.getId() + ".Interact Scripts"))
    //                thePlayer.sendMessage(ChatColor.GRAY + "- " + ChatColor.YELLOW + scriptEntry);
    //        }
    //        if (!scriptsPresent) thePlayer.sendMessage(ChatColor.RED + "  No scripts assigned!");
    //
    //        thePlayer.sendMessage("");
    //
    //        /* Show Scheduled Activities */
    //        boolean activitiesPresent = false;
    //        thePlayer.sendMessage(ChatColor.GRAY + "Scheduled Activities:");
    //        if (plugin.getAssignments().contains("Denizens." + theDenizen.getName() + ".Scheduled Activities")) {
    //            if (!plugin.getAssignments().getStringList("Denizens." + theDenizen.getName() + ".Scheduled Activities").isEmpty()) activitiesPresent = true;
    //            for (String scriptEntry : plugin.getAssignments().getStringList("Denizens." + theDenizen.getName() + ".Scheduled Activities"))
    //                thePlayer.sendMessage(ChatColor.GRAY + "- " + ChatColor.GREEN + scriptEntry);
    //        }
    //        if (!activitiesPresent) thePlayer.sendMessage(ChatColor.RED + "  No activities scheduled!");
    //        thePlayer.sendMessage("");
    //
    //        /* Show Bookmarks */
    //
    //        DecimalFormat lf = new DecimalFormat("###.##");
    //        boolean bookmarksPresent = false;
    //        thePlayer.sendMessage(ChatColor.GRAY + "Bookmarks:");
    //
    //        /* Location Bookmarks */
    //        if (plugin.getSaves().contains("Denizens." + theDenizen.getName() + ".Bookmarks.Location")) {
    //            if (!plugin.getSaves().getStringList("Denizens." + theDenizen.getName() + ".Bookmarks.Location").isEmpty()) bookmarksPresent = true;
    //            for (String bookmarkEntry : plugin.getSaves().getStringList("Denizens." + theDenizen.getName() + ".Bookmarks.Location")) {
    //                if (bookmarkEntry.split(";").length >= 6) {
    //                    thePlayer.sendMessage(ChatColor.GRAY + "- Type: " + ChatColor.GREEN + "LOCATION " + ChatColor.GRAY + "Name: " + ChatColor.GREEN + bookmarkEntry.split(" ")[0]
    //                            + ChatColor.GRAY + " in World: " + ChatColor.GREEN + bookmarkEntry.split(" ")[1].split(";")[0]);
    //                    thePlayer.sendMessage(" "
    //                            + ChatColor.GRAY + "  at X: " + ChatColor.GREEN + lf.format(Double.valueOf(bookmarkEntry.split(";")[1]))
    //                            + ChatColor.GRAY + " Y: " + ChatColor.GREEN + lf.format(Double.valueOf(bookmarkEntry.split(";")[2]))
    //                            + ChatColor.GRAY + " Z: " + ChatColor.GREEN + lf.format(Double.valueOf(bookmarkEntry.split(";")[3]))
    //                            + ChatColor.GRAY + " Pitch: " + ChatColor.GREEN + lf.format(Double.valueOf(bookmarkEntry.split(";")[4]))
    //                            + ChatColor.GRAY + " Yaw: " + ChatColor.GREEN + lf.format(Double.valueOf(bookmarkEntry.split(";")[5])));
    //                }
    //            }
    //        }
    //
    //        if (plugin.getSaves().contains("Denizens." + theDenizen.getId() + ".Bookmarks.Location")) {
    //            if (!plugin.getSaves().getStringList("Denizens." + theDenizen.getId() + ".Bookmarks.Location").isEmpty()) bookmarksPresent = true;
    //            for (String bookmarkEntry : plugin.getSaves().getStringList("Denizens." + theDenizen.getId() + ".Bookmarks.Location")) {
    //                if (bookmarkEntry.split(";").length >= 6) {
    //                    thePlayer.sendMessage(ChatColor.GRAY + "- Type: " + ChatColor.YELLOW + "LOCATION " + ChatColor.GRAY + "Name: " + ChatColor.YELLOW + bookmarkEntry.split(" ")[0]
    //                            + ChatColor.GRAY + " in World: " + ChatColor.YELLOW + bookmarkEntry.split(" ")[1].split(";")[0]);
    //                    thePlayer.sendMessage(" "
    //                            + ChatColor.GRAY + "  at X: " + ChatColor.YELLOW + lf.format(Double.valueOf(bookmarkEntry.split(";")[1]))
    //                            + ChatColor.GRAY + " Y: " + ChatColor.YELLOW + lf.format(Double.valueOf(bookmarkEntry.split(";")[2]))
    //                            + ChatColor.GRAY + " Z: " + ChatColor.YELLOW + lf.format(Double.valueOf(bookmarkEntry.split(";")[3]))
    //                            + ChatColor.GRAY + " Pitch: " + ChatColor.YELLOW + lf.format(Double.valueOf(bookmarkEntry.split(";")[4]))
    //                            + ChatColor.GRAY + " Yaw: " + ChatColor.YELLOW + lf.format(Double.valueOf(bookmarkEntry.split(";")[5])));
    //                }
    //            }
    //        }
    //
    //        /* Block Bookmarks */
    //        if (plugin.getSaves().contains("Denizens." + theDenizen.getName() + ".Bookmarks.Block")) {
    //            if (!plugin.getSaves().getStringList("Denizens." + theDenizen.getName() + ".Bookmarks.Block").isEmpty()) bookmarksPresent = true;
    //            for (String bookmarkEntry : plugin.getSaves().getStringList("Denizens." + theDenizen.getName() + ".Bookmarks.Block")) {
    //                if (bookmarkEntry.split(";").length >= 4) {
    //                    thePlayer.sendMessage(ChatColor.GRAY + "- Type: " + ChatColor.GREEN + "BLOCK " + ChatColor.GRAY + "Name: " + ChatColor.GREEN + bookmarkEntry.split(" ")[0]
    //                            + ChatColor.GRAY + " in World: " + ChatColor.GREEN + bookmarkEntry.split(" ")[1].split(";")[0]);
    //                    thePlayer.sendMessage(" "
    //                            + ChatColor.GRAY + "  at X: " + ChatColor.GREEN + lf.format(Double.valueOf(bookmarkEntry.split(";")[1]))
    //                            + ChatColor.GRAY + " Y: " + ChatColor.GREEN + lf.format(Double.valueOf(bookmarkEntry.split(";")[2]))
    //                            + ChatColor.GRAY + " Z: " + ChatColor.GREEN + lf.format(Double.valueOf(bookmarkEntry.split(";")[3]))
    //                            + ChatColor.GRAY + " Material: " + ChatColor.GREEN + plugin.bookmarks.get(theDenizen, bookmarkEntry.split(" ")[0], BookmarkType.BLOCK).getBlock().getType().toString());
    //                }
    //            }
    //        }
    //
    //        if (plugin.getSaves().contains("Denizens." + theDenizen.getId() + ".Bookmarks.Block")) {
    //            if (!plugin.getSaves().getStringList("Denizens." + theDenizen.getId() + ".Bookmarks.Block").isEmpty()) bookmarksPresent = true;
    //            for (String bookmarkEntry : plugin.getSaves().getStringList("Denizens." + theDenizen.getId() + ".Bookmarks.Block")) {
    //                if (bookmarkEntry.split(";").length >= 4) {
    //                    thePlayer.sendMessage(ChatColor.GRAY + "- Type: " + ChatColor.YELLOW + "BLOCK " + ChatColor.GRAY + "Name: " + ChatColor.YELLOW + bookmarkEntry.split(" ")[0]
    //                            + ChatColor.GRAY + " in World: " + ChatColor.GREEN + bookmarkEntry.split(" ")[1].split(";")[0]);
    //                    thePlayer.sendMessage(" "
    //                            + ChatColor.GRAY + "  at X: " + ChatColor.YELLOW + lf.format(Double.valueOf(bookmarkEntry.split(";")[1]))
    //                            + ChatColor.GRAY + " Y: " + ChatColor.YELLOW + lf.format(Double.valueOf(bookmarkEntry.split(";")[2]))
    //                            + ChatColor.GRAY + " Z: " + ChatColor.YELLOW + lf.format(Double.valueOf(bookmarkEntry.split(";")[3]))
    //                            + ChatColor.GRAY + " Material: " + ChatColor.YELLOW + plugin.bookmarks.get(theDenizen, bookmarkEntry.split(" ")[0], BookmarkType.BLOCK).getBlock().getType().toString());
    //                }
    //            }
    //        }
    //
    //        if (!bookmarksPresent) thePlayer.sendMessage(ChatColor.RED + "  No bookmarks defined!");
    //        thePlayer.sendMessage("");        
    //    }




	
}
