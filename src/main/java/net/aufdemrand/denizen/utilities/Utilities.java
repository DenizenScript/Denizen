package net.aufdemrand.denizen.utilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.bookmarks.Bookmarks.BookmarkType;
import net.aufdemrand.denizen.npc.DenizenTrait;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

public class Utilities {

	private Denizen plugin;

	public Utilities(Denizen plugin) {
		this.plugin = plugin;
	}

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

	
	
	
	@EventHandler
	public void playerTaskLocationListener(PlayerMoveEvent event) {

		Denizen plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");

		/* Do not run any code unless the player actually moves blocks */
		if (!event.getTo().getBlock().equals(event.getFrom().getBlock())) {

			try {

				/* ---- saves.yml format ----
				 * Players:
				 *   [PlayerName]:
				 *     Tasks:
				 *       List All:
				 *         Locations:
				 *         - theLocation:theDenizen:theId
				 *       List Entries:
				 *         Id: theId
				 *         Type: Location
				 *         Leeway: in blocks
				 *         Duration: in seconds
				 *         Script to trigger: script name
				 *         Initiated: System.currentTimeMillis */

				if (plugin.getSaves().contains("Players." + event.getPlayer().getName() + ".Tasks.List All.Locations")) {
					List<String> listAll = plugin.getSaves().getStringList("Players." + event.getPlayer().getName() + ".Tasks.List All.Locations");      

					if (!listAll.isEmpty()) {
						for (String theTask : listAll) {
							String[] taskArgs = theTask.split(";");
							Location theLocation = plugin.bookmarks.get(taskArgs[1], taskArgs[0], BookmarkType.LOCATION);
							int theLeeway = plugin.getSaves().getInt("Players." + event.getPlayer().getName() + ".Tasks.List Entries." + taskArgs[2] + ".Leeway");
							long theDuration = plugin.getSaves().getLong("Players." + event.getPlayer().getName() + ".Tasks.List Entries." + taskArgs[2] + ".Duration");
							if (plugin.bookmarks.checkLocation(event.getPlayer(), theLocation, theLeeway)) {
								if (plugin.getSaves().contains("Players." + event.getPlayer().getName() + ".Tasks.List Entries." + taskArgs[2] + ".Initiated")) {
									if (plugin.getSaves().getLong("Players." + event.getPlayer().getName() + ".Tasks.List Entries." + taskArgs[2] + ".Initiated")
											+ (theDuration * 1000) <= System.currentTimeMillis()) 

										plugin.getScriptEngine().finishLocationTask(event.getPlayer(), taskArgs[2]);
								}
								else {
									plugin.getSaves().set("Players." + event.getPlayer().getName() + ".Tasks.List Entries." + taskArgs[2] + ".Initiated", System.currentTimeMillis());
									plugin.saveSaves();
								}
							}

						}
					}
				}
			}

			catch (Exception e) {
				plugin.getLogger().log(Level.SEVERE, "Error processing location task event.", e);
			}
		}
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

	public boolean checkLocation(NPC theDenizen, Location theLocation, int theLeeway) {

		if (!theDenizen.getBukkitEntity().getWorld().getName().equals(theLocation.getWorld().getName()))
			return false;
		
		if (Math.abs(theDenizen.getBukkitEntity().getLocation().getBlockX() - theLocation.getBlockX()) 
				> theLeeway) return false;
		if (Math.abs(theDenizen.getBukkitEntity().getLocation().getBlockY() - theLocation.getBlockY()) 
				> theLeeway) return false;
		if (Math.abs(theDenizen.getBukkitEntity().getLocation().getBlockZ() - theLocation.getBlockZ()) 
				> theLeeway) return false;

		return true;
	}

	
	
	/**
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

	/**
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
