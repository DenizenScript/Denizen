package net.aufdemrand.denizen.utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.DenizenTrait;
import net.aufdemrand.denizen.bookmarks.Bookmarks.BookmarkType;
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

	public List<NPC> getDenizens() {

		List<NPC> denizenList = new ArrayList<NPC>();
		
		for(NPC npc : CitizensAPI.getNPCRegistry()) {
			if(npc.hasTrait(DenizenTrait.class)) {
				denizenList.add(npc);
			}
		}
		return denizenList;

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

										plugin.scriptEngine.finishLocationTask(event.getPlayer(), taskArgs[2]);
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

	
	
}
