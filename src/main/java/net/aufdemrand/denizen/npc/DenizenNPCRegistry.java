package net.aufdemrand.denizen.npc;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.bookmarks.BookmarkHelper.BookmarkType;

import net.citizensnpcs.api.event.NPCRemoveEvent;
import net.citizensnpcs.api.npc.NPC;

public class DenizenNPCRegistry {

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
	
	
	/* 
	 * Denizen Info-Click
	 */

	public void showInfo(Player thePlayer, DenizenNPC theDenizen) {

		thePlayer.sendMessage(ChatColor.GOLD + "------ Denizen Info ------");

		/* Show Citizens NPC info. */

		thePlayer.sendMessage(ChatColor.GRAY + "C2 NPCID: " + ChatColor.GREEN + theDenizen.getId() + ChatColor.GRAY + "   Name: " + ChatColor.GREEN + theDenizen.getName() + ChatColor.GRAY + "   HPs: " + ChatColor.GREEN + theDenizen.getEntity().getHealth());
		//if (plugin.newbMode) thePlayer.sendMessage(ChatColor.GRAY + "Tip: Use " + ChatColor.WHITE + "/denizen setname" + ChatColor.GRAY + " to change the Denizen's name.");
		thePlayer.sendMessage(ChatColor.GRAY + "PF RANGE: " + ChatColor.GREEN + theDenizen.getNavigator().getLocalParameters().range());
		thePlayer.sendMessage("");

		thePlayer.sendMessage(ChatColor.GRAY + "Trigger Status:");
		for (String line : plugin.getSpeechEngine().getMultilineText(theDenizen.getCitizensEntity().getTrait(DenizenTrait.class).listTriggers()))
			thePlayer.sendMessage(line);
		thePlayer.sendMessage("");

		/* Show Assigned Scripts. */

		boolean scriptsPresent = false;
		thePlayer.sendMessage(ChatColor.GRAY + "Interact Scripts:");
		if (plugin.getAssignments().contains("Denizens." + theDenizen.getName() + ".Interact Scripts")) {
			if (!plugin.getAssignments().getStringList("Denizens." + theDenizen.getName() + ".Interact Scripts").isEmpty()) scriptsPresent = true;
			for (String scriptEntry : plugin.getAssignments().getStringList("Denizens." + theDenizen.getName() + ".Interact Scripts"))
				thePlayer.sendMessage(ChatColor.GRAY + "- " + ChatColor.GREEN + scriptEntry);
		}
		if (plugin.getAssignments().contains("Denizens." + theDenizen.getId() + ".Interact Scripts")) {
			if (!plugin.getAssignments().getStringList("Denizens." + theDenizen.getId() + ".Interact Scripts").isEmpty()) scriptsPresent = true;
			for (String scriptEntry : plugin.getAssignments().getStringList("Denizens." + theDenizen.getId() + ".Interact Scripts"))
				thePlayer.sendMessage(ChatColor.GRAY + "- " + ChatColor.YELLOW + scriptEntry);
		}
		if (!scriptsPresent) thePlayer.sendMessage(ChatColor.RED + "  No scripts assigned!");

		//if (plugin.newbMode) thePlayer.sendMessage(ChatColor.GRAY + "Tip: Use " + ChatColor.WHITE + "/denizen assign" + ChatColor.GRAY + " to assign scripts.");
		//if (plugin.newbMode) thePlayer.sendMessage(ChatColor.GRAY + "Turn on precision mode with " + ChatColor.WHITE + "/denizen precision" + ChatColor.GRAY + " to assign to Id.");
		thePlayer.sendMessage("");
		
		/* Show Scheduled Activities */
		boolean activitiesPresent = false;
		thePlayer.sendMessage(ChatColor.GRAY + "Scheduled Activities:");
		if (plugin.getAssignments().contains("Denizens." + theDenizen.getName() + ".Scheduled Activities")) {
			if (!plugin.getAssignments().getStringList("Denizens." + theDenizen.getName() + ".Scheduled Activities").isEmpty()) activitiesPresent = true;
			for (String scriptEntry : plugin.getAssignments().getStringList("Denizens." + theDenizen.getName() + ".Scheduled Activities"))
				thePlayer.sendMessage(ChatColor.GRAY + "- " + ChatColor.GREEN + scriptEntry);
		}
		if (!activitiesPresent) thePlayer.sendMessage(ChatColor.RED + "  No activities scheduled!");
		thePlayer.sendMessage("");
		
		/* Show Bookmarks */

		DecimalFormat lf = new DecimalFormat("###.##");
		boolean bookmarksPresent = false;
		thePlayer.sendMessage(ChatColor.GRAY + "Bookmarks:");

		/* Location Bookmarks */
		if (plugin.getSaves().contains("Denizens." + theDenizen.getName() + ".Bookmarks.Location")) {
			if (!plugin.getSaves().getStringList("Denizens." + theDenizen.getName() + ".Bookmarks.Location").isEmpty()) bookmarksPresent = true;
			for (String bookmarkEntry : plugin.getSaves().getStringList("Denizens." + theDenizen.getName() + ".Bookmarks.Location")) {
				if (bookmarkEntry.split(";").length >= 6) {
					thePlayer.sendMessage(ChatColor.GRAY + "- Type: " + ChatColor.GREEN + "LOCATION " + ChatColor.GRAY + "Name: " + ChatColor.GREEN + bookmarkEntry.split(" ")[0]
							+ ChatColor.GRAY + " in World: " + ChatColor.GREEN + bookmarkEntry.split(" ")[1].split(";")[0]);
					thePlayer.sendMessage(" "
							+ ChatColor.GRAY + "  at X: " + ChatColor.GREEN + lf.format(Double.valueOf(bookmarkEntry.split(";")[1]))
							+ ChatColor.GRAY + " Y: " + ChatColor.GREEN + lf.format(Double.valueOf(bookmarkEntry.split(";")[2]))
							+ ChatColor.GRAY + " Z: " + ChatColor.GREEN + lf.format(Double.valueOf(bookmarkEntry.split(";")[3]))
							+ ChatColor.GRAY + " Pitch: " + ChatColor.GREEN + lf.format(Double.valueOf(bookmarkEntry.split(";")[4]))
							+ ChatColor.GRAY + " Yaw: " + ChatColor.GREEN + lf.format(Double.valueOf(bookmarkEntry.split(";")[5])));
				}
			}
		}

		if (plugin.getSaves().contains("Denizens." + theDenizen.getId() + ".Bookmarks.Location")) {
			if (!plugin.getSaves().getStringList("Denizens." + theDenizen.getId() + ".Bookmarks.Location").isEmpty()) bookmarksPresent = true;
			for (String bookmarkEntry : plugin.getSaves().getStringList("Denizens." + theDenizen.getId() + ".Bookmarks.Location")) {
				if (bookmarkEntry.split(";").length >= 6) {
					thePlayer.sendMessage(ChatColor.GRAY + "- Type: " + ChatColor.YELLOW + "LOCATION " + ChatColor.GRAY + "Name: " + ChatColor.YELLOW + bookmarkEntry.split(" ")[0]
							+ ChatColor.GRAY + " in World: " + ChatColor.YELLOW + bookmarkEntry.split(" ")[1].split(";")[0]);
					thePlayer.sendMessage(" "
							+ ChatColor.GRAY + "  at X: " + ChatColor.YELLOW + lf.format(Double.valueOf(bookmarkEntry.split(";")[1]))
							+ ChatColor.GRAY + " Y: " + ChatColor.YELLOW + lf.format(Double.valueOf(bookmarkEntry.split(";")[2]))
							+ ChatColor.GRAY + " Z: " + ChatColor.YELLOW + lf.format(Double.valueOf(bookmarkEntry.split(";")[3]))
							+ ChatColor.GRAY + " Pitch: " + ChatColor.YELLOW + lf.format(Double.valueOf(bookmarkEntry.split(";")[4]))
							+ ChatColor.GRAY + " Yaw: " + ChatColor.YELLOW + lf.format(Double.valueOf(bookmarkEntry.split(";")[5])));
				}
			}
		}

		/* Block Bookmarks */
		if (plugin.getSaves().contains("Denizens." + theDenizen.getName() + ".Bookmarks.Block")) {
			if (!plugin.getSaves().getStringList("Denizens." + theDenizen.getName() + ".Bookmarks.Block").isEmpty()) bookmarksPresent = true;
			for (String bookmarkEntry : plugin.getSaves().getStringList("Denizens." + theDenizen.getName() + ".Bookmarks.Block")) {
				if (bookmarkEntry.split(";").length >= 4) {
					thePlayer.sendMessage(ChatColor.GRAY + "- Type: " + ChatColor.GREEN + "BLOCK " + ChatColor.GRAY + "Name: " + ChatColor.GREEN + bookmarkEntry.split(" ")[0]
							+ ChatColor.GRAY + " in World: " + ChatColor.GREEN + bookmarkEntry.split(" ")[1].split(";")[0]);
					thePlayer.sendMessage(" "
							+ ChatColor.GRAY + "  at X: " + ChatColor.GREEN + lf.format(Double.valueOf(bookmarkEntry.split(";")[1]))
							+ ChatColor.GRAY + " Y: " + ChatColor.GREEN + lf.format(Double.valueOf(bookmarkEntry.split(";")[2]))
							+ ChatColor.GRAY + " Z: " + ChatColor.GREEN + lf.format(Double.valueOf(bookmarkEntry.split(";")[3]))
							+ ChatColor.GRAY + " Material: " + ChatColor.GREEN + plugin.bookmarks.get(theDenizen, bookmarkEntry.split(" ")[0], BookmarkType.BLOCK).getBlock().getType().toString());
				}
			}
		}

		if (plugin.getSaves().contains("Denizens." + theDenizen.getId() + ".Bookmarks.Block")) {
			if (!plugin.getSaves().getStringList("Denizens." + theDenizen.getId() + ".Bookmarks.Block").isEmpty()) bookmarksPresent = true;
			for (String bookmarkEntry : plugin.getSaves().getStringList("Denizens." + theDenizen.getId() + ".Bookmarks.Block")) {
				if (bookmarkEntry.split(";").length >= 4) {
					thePlayer.sendMessage(ChatColor.GRAY + "- Type: " + ChatColor.YELLOW + "BLOCK " + ChatColor.GRAY + "Name: " + ChatColor.YELLOW + bookmarkEntry.split(" ")[0]
							+ ChatColor.GRAY + " in World: " + ChatColor.GREEN + bookmarkEntry.split(" ")[1].split(";")[0]);
					thePlayer.sendMessage(" "
							+ ChatColor.GRAY + "  at X: " + ChatColor.YELLOW + lf.format(Double.valueOf(bookmarkEntry.split(";")[1]))
							+ ChatColor.GRAY + " Y: " + ChatColor.YELLOW + lf.format(Double.valueOf(bookmarkEntry.split(";")[2]))
							+ ChatColor.GRAY + " Z: " + ChatColor.YELLOW + lf.format(Double.valueOf(bookmarkEntry.split(";")[3]))
							+ ChatColor.GRAY + " Material: " + ChatColor.YELLOW + plugin.bookmarks.get(theDenizen, bookmarkEntry.split(" ")[0], BookmarkType.BLOCK).getBlock().getType().toString());
				}
			}
		}

		if (!bookmarksPresent) thePlayer.sendMessage(ChatColor.RED + "  No bookmarks defined!");
		//if (plugin.newbMode) thePlayer.sendMessage(ChatColor.GRAY + "Tip: Use " + ChatColor.WHITE + "/denizen bookmark" + ChatColor.GRAY + " to create bookmarks.");
		//if (plugin.newbMode) thePlayer.sendMessage(ChatColor.GRAY + "Turn on precision mode with " + ChatColor.WHITE + "/denizen precision" + ChatColor.GRAY + " to assign to Id.");
		thePlayer.sendMessage("");		
	}

}
