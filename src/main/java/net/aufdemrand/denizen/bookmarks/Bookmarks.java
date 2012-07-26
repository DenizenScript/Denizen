package net.aufdemrand.denizen.bookmarks;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.npc.DenizenNPC;
import net.citizensnpcs.api.npc.NPC;

public class Bookmarks {

	private Denizen plugin;
	public Bookmarks(Denizen denizen) {
		plugin = denizen;
	}	



	/*
	 * Checks a Player's location against a Location (with leeway). Should be faster than
	 * bukkit's built in Location.distance(Location) since there's no sqrt math.
	 * 
	 * Thanks chainsol :)
	 */

	public boolean checkLocation(Player thePlayer, Location theLocation, int theLeeway) {
		if (Math.abs(thePlayer.getLocation().getBlockX() - theLocation.getBlockX()) 
				> theLeeway) return false;
		if (Math.abs(thePlayer.getLocation().getBlockY() - theLocation.getBlockY()) 
				> theLeeway) return false;
		if (Math.abs(thePlayer.getLocation().getBlockX() - theLocation.getBlockX()) 
				> theLeeway) return false;
		if (!thePlayer.getWorld().getName().equals(theLocation.getWorld().getName()))
			return false;


		return true;
	}

	public boolean checkLocation(NPC theDenizen, Location theLocation, int theLeeway) {
		if (Math.abs(theDenizen.getBukkitEntity().getLocation().getBlockX() - theLocation.getBlockX()) 
				> theLeeway) return false;
		if (Math.abs(theDenizen.getBukkitEntity().getLocation().getBlockY() - theLocation.getBlockY()) 
				> theLeeway) return false;
		if (Math.abs(theDenizen.getBukkitEntity().getLocation().getBlockX() - theLocation.getBlockX()) 
				> theLeeway) return false;
		if (!theDenizen.getBukkitEntity().getWorld().getName().equals(theLocation.getWorld().getName()))
			return false;

		return true;
	}



	/*
	 * Builds a HashMap of all the Location Bookmarks. This is used for quickly checking with the Location Trigger.
	 * 
	 */

	private Map<Location, String> locationTriggerList = new ConcurrentHashMap<Location, String>();

	public void buildLocationTriggerList() {
		locationTriggerList.clear();

		
		/* Find Location Bookmarks assigned to Names */
		for (DenizenNPC theDenizen : plugin.getDenizenNPCRegistry().getDenizens().values()) {
			if (plugin.getSaves().contains("Denizens." + theDenizen.getName() + ".Bookmarks.Location")) {
				List<String> locationsToAdd = plugin.getSaves().getStringList("Denizens." + theDenizen.getName() + ".Bookmarks.Location");

				for (String thisLocation : locationsToAdd) {
					if (!thisLocation.isEmpty()) {
						Location theLocation = get(theDenizen.getName(), thisLocation.split(" ", 2)[0], BookmarkType.LOCATION);
						String theInfo = "NAME:" + theDenizen.getName() + ":" + thisLocation.split(" ", 2)[0];
						locationTriggerList.put(theLocation, theInfo);
					}
				}
			}
		}	
		
		/* Find Location Bookmarks assigned to NPCIDs */
		for (DenizenNPC theDenizen : plugin.getDenizenNPCRegistry().getDenizens().values()) {
			if (plugin.getSaves().contains("Denizens." + theDenizen.getId() + ".Bookmarks.Location")) {
				List<String> locationsToAdd = plugin.getSaves().getStringList("Denizens." + theDenizen.getId() + ".Bookmarks.Location");

				for (String thisLocation : locationsToAdd) {
					if (!thisLocation.isEmpty()) {
						Location theLocation = get(theDenizen.getName(), thisLocation.split(" ", 2)[0], BookmarkType.LOCATION);
						String theInfo = "ID:" + theDenizen.getId() + ":" + thisLocation.split(" ", 2)[0];
						locationTriggerList.put(theLocation, theInfo);
					}
				}
			}
		}	

		plugin.getLogger().log(Level.INFO, "Building Location Bookmark Trigger List. Size: " + locationTriggerList.size());

		if (plugin.debugMode && locationTriggerList.size() > 0) {
			plugin.getLogger().log(Level.INFO, "Item Key: [Assignment Type]:[Assigned To]:[Name of Bookmark]" + locationTriggerList.size());
			for (String theEntry : locationTriggerList.values()) {
				plugin.getLogger().log(Level.INFO, "Item added to list: " + theEntry);
			}
		}
		
		return;
	}

	
	
	public Map<Location, String> getLocationTriggerList() {
		return locationTriggerList;
	}

	
	
	public enum BookmarkType {
		LOCATION, BLOCK
	}

	
	
	public boolean exists(String theDenizen, String nameOfLocation) {

		if (nameOfLocation.split(":").length == 2) theDenizen = nameOfLocation.split(":")[0];
		if (plugin.getSaves().contains("Denizens." + theDenizen + ".Bookmarks.Location"))
			for (String theLocationBookmark : plugin.getSaves().getStringList("Denizens." + theDenizen + ".Bookmarks.Location"))
				if (theLocationBookmark.contains(nameOfLocation)) return true;
		if (plugin.getSaves().contains("Denizens." + theDenizen + ".Bookmarks.Block"))
			for (String theLocationBookmark : plugin.getSaves().getStringList("Denizens." + theDenizen + ".Bookmarks.Block"))
				if (theLocationBookmark.contains(nameOfLocation)) return true;

		return false;
	}

	public boolean exists(NPC theDenizen, String nameOfLocation) {

		String theName = null;
		if (theDenizen == null) theName = "null";
		else theName = theDenizen.getName();
		return exists(theName, nameOfLocation);
	}

	
	
	public Location get(NPC theDenizen, String nameOfLocation, BookmarkType bookmarkType) {
		String theName = null;
		if (theDenizen == null) theName = "null";
		else theName = theDenizen.getName();
		return get(theName, nameOfLocation, bookmarkType);
	}

	
	
	public Location get(String theDenizen, String nameOfLocation, BookmarkType bookmarkType) {

		List<String> locationList = null;
		String[] theLocation = null;
		Location locationBookmark = null;

		try {

			if (nameOfLocation.split(":").length == 2) theDenizen = nameOfLocation.split(":")[0];

			if (bookmarkType == BookmarkType.BLOCK) locationList = plugin.getSaves().getStringList("Denizens." + theDenizen + ".Bookmarks.Block");	
			else if (bookmarkType == BookmarkType.LOCATION) locationList = plugin.getSaves().getStringList("Denizens." + theDenizen + ".Bookmarks.Location");



			for (String thisLocation : locationList) {
				String theName = thisLocation.split(" ", 2)[0];
				if (theName.equalsIgnoreCase(nameOfLocation)) theLocation = thisLocation.split(" ", 2)[1].split(";");
			}

			if (theLocation != null && bookmarkType == BookmarkType.LOCATION) {			
				locationBookmark = 
						new Location(plugin.getServer().getWorld(theLocation[0]),
								Double.parseDouble(theLocation[1]), Double.parseDouble(theLocation[2] + 1),
								Double.parseDouble(theLocation[3]), Float.parseFloat(theLocation[4]),
								Float.parseFloat(theLocation[5]));
			}

			else if (theLocation != null && bookmarkType == BookmarkType.BLOCK) {
				locationBookmark = 
						new Location(plugin.getServer().getWorld(theLocation[0]),
								Double.parseDouble(theLocation[1]), Double.parseDouble(theLocation[2]),
								Double.parseDouble(theLocation[3]));
			}

		} catch(Throwable error) {
			Bukkit.getLogger().info("Failed to get a Bookmark.");
			Bukkit.getLogger().info("--- Error follows: " + error);
		}

		return locationBookmark;		
	}

	
	
}
