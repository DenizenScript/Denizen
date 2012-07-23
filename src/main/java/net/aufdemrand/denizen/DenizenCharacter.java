package net.aufdemrand.denizen;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import net.aufdemrand.denizen.bookmarks.Bookmarks.BookmarkType;
import net.aufdemrand.denizen.scriptEngine.ScriptEngine.TriggerType;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;


/**
 * Contains all the listeners/triggers for Denizen NPCs.
 * Works with the ScriptEngine to carry out scripts.
 * 
 * @author Jeremy Schroeder
 *
 */

public class DenizenCharacter implements Listener {

	Denizen plugin;

	public DenizenCharacter(Denizen plugin) {
		this.plugin = plugin;
	}


	/* Listens for an NPC click. Right click sends out a Click Trigger, 
	 * left click sends out either a Damage Trigger or Click Trigger. */

	@EventHandler
	public void clickTrigger(NPCRightClickEvent event) {

		/* Show NPC info if sneaking and right clicking */
		if (event.getClicker().isSneaking() 
				&& event.getClicker().isOp()
				&& plugin.settings.RightClickAndSneakInfoModeEnabled()) 
			showInfo(event.getClicker(), event.getNPC());

		/* Check if ... 1) isDenizen is true, 2) clickTrigger enabled, 3) is cooled down, 4) is not engaged */
		if (event.getNPC().getTrait(DenizenTrait.class).isDenizen
				&& event.getNPC().getTrait(DenizenTrait.class).enableClickTriggers
				&& plugin.scriptEngine.checkCooldown(event.getClicker(), TriggerType.CLICK)
				&& !plugin.scriptEngine.getEngaged(event.getNPC())) {

			/* Apply default cooldown to avoid click-spam, then send to parser. */
			plugin.scriptEngine.setCooldown(event.getClicker(), TriggerType.CLICK, plugin.settings.DefaultClickCooldown());
			plugin.scriptEngine.parseClickTrigger(event.getNPC(), event.getClicker());
		}
	}

	@EventHandler
	public void damageTrigger(NPCLeftClickEvent event) {

		/* Check if ... 1) isDenizen is true, 2) damageTrigger enabled, 3) is cooled down, 4) is not engaged 
		 * Special condition, as part of the design, if damageTrigger is disabled, this may trigger the
		 * click trigger if the config setting disabled_damage_trigger_instead_triggers_click is true */
		if (event.getNPC().getTrait(DenizenTrait.class).isDenizen
				&& event.getNPC().getTrait(DenizenTrait.class).enableDamageTriggers
				&& plugin.scriptEngine.checkCooldown(event.getClicker(), TriggerType.DAMAGE)
				&& !plugin.scriptEngine.getEngaged(event.getNPC())) {

			/* Apply default cooldown to avoid click-spam, then send to parser. */
			plugin.scriptEngine.setCooldown(event.getClicker(), TriggerType.DAMAGE, plugin.settings.DefaultDamageCooldown());
			plugin.scriptEngine.parseDamageTrigger(event.getNPC(), event.getClicker());
		}

		else if (plugin.settings.DisabledDamageTriggerInsteadTriggersClick())
			clickTrigger(new NPCRightClickEvent(event.getNPC(), event.getClicker())); 
	}



	/* Listens for the PlayerMoveEvent to see if a player is within range
	 * of a Denizen to trigger a Proximity Trigger */

	@EventHandler
	public void proximityTrigger(PlayerMoveEvent event) {

		/* Do not run any code unless the player actually moves blocks */
		if (!event.getTo().getBlock().equals(event.getFrom().getBlock())) {

			/* Do not run any further code if no Denizen is in range */
			if (plugin.getDenizen.getClosest(event.getPlayer(), plugin.settings.ProximityTriggerRangeInBlocks()) != null) {
				NPC theDenizen = plugin.getDenizen.getClosest(event.getPlayer(), plugin.settings.ProximityTriggerRangeInBlocks());
				if (event.getPlayer().hasMetadata("npcinproximity")) {

					/* If closest is same as stored metadata, avoid retrigger. */
					if (theDenizen == event.getPlayer().getMetadata("npcinproximity"))
						return;

					/* If closest is different than stored metadata and proximity trigger is enabled for said NPC, trigger */
					else if (theDenizen != event.getPlayer().getMetadata("npcinproximity")
							&& theDenizen.getTrait(DenizenTrait.class).enableProximityTriggers) {
						if (plugin.scriptEngine.checkCooldown(event.getPlayer(), TriggerType.PROXIMITY)
								&& !plugin.scriptEngine.getEngaged(theDenizen)) {

							/* Set Metadata value to avoid retrigger. */
							event.getPlayer().setMetadata("npcinproximity", new FixedMetadataValue(plugin, plugin.getDenizen.getClosest(event.getPlayer(), plugin.settings.ProximityTriggerRangeInBlocks())));

							/* TRIGGER! */
							plugin.scriptEngine.setCooldown(event.getPlayer(), TriggerType.PROXIMITY, plugin.settings.DefaultProximityCooldown());
							plugin.scriptEngine.parseProximityTrigger(theDenizen, event.getPlayer());
						}
					}

				} else { /* Player does not have metadata */

					/* Check if proximity triggers are enabled, check player cooldown, check if NPC is engaged... */
					if (theDenizen.getTrait(DenizenTrait.class).enableProximityTriggers) {
						if (plugin.scriptEngine.checkCooldown(event.getPlayer(), TriggerType.PROXIMITY)
								&& !plugin.scriptEngine.getEngaged(theDenizen)) {

							/* Set Metadata value to avoid retrigger. */
							event.getPlayer().setMetadata("npcinproximity", new FixedMetadataValue(plugin, plugin.getDenizen.getClosest(event.getPlayer(), plugin.settings.ProximityTriggerRangeInBlocks())));

							/* TRIGGER! */
							plugin.scriptEngine.setCooldown(event.getPlayer(), TriggerType.PROXIMITY, plugin.settings.DefaultProximityCooldown());
							plugin.scriptEngine.parseProximityTrigger(theDenizen, event.getPlayer());
						}
					}
				}
			}
		}
	}


	/* Listens for the PlayerMoveEvent to see if a player is within range
	 * of a Denizen to trigger a Location Trigger */

	@EventHandler
	public void locationTrigger(PlayerMoveEvent event) {

		/* Do not run any code unless the player actually moves blocks */
		if (!event.getTo().getBlock().equals(event.getFrom().getBlock())) {

			/* Do not run any code if there aren't any location triggers */
			if (!plugin.bookmarks.getLocationTriggerList().isEmpty()) {

				/* Check player location against each Location Trigger */
				for (Location theLocation : plugin.bookmarks.getLocationTriggerList().keySet()) {

					if (plugin.bookmarks.checkLocation(event.getPlayer(), theLocation, plugin.settings.LocationTriggerRangeInBlocks()) 
							&& plugin.scriptEngine.checkCooldown(event.getPlayer(), TriggerType.LOCATION)) {

						NPC theDenizen = null;
						String locationTriggered = plugin.bookmarks.getLocationTriggerList().get(theLocation).split(":")[2];


						/* Player matches Location, find NPC it belongs to */

						if (plugin.bookmarks.getLocationTriggerList().get(theLocation).contains("ID:"))
							theDenizen = CitizensAPI.getNPCRegistry().getById(Integer.valueOf(plugin.bookmarks.getLocationTriggerList().get(theLocation).split(":")[1]));

						else if (plugin.bookmarks.getLocationTriggerList().get(theLocation).contains("NAME:")) {
							List<NPC> denizenList = new ArrayList<NPC>();

							/* Find all the NPCs with the name */
							for (NPC npc : CitizensAPI.getNPCRegistry()) {
								if(npc.getName().equals(plugin.bookmarks.getLocationTriggerList().get(theLocation).split(":")[1])
										&& npc.hasTrait(DenizenTrait.class)) {
									denizenList.add(npc);
									theDenizen = npc;
								}
							}

							/* Check which NPC is closest */
							for (NPC npc : denizenList) {
								if (npc.getBukkitEntity().getLocation().distance(event.getPlayer().getLocation())
										< theDenizen.getBukkitEntity().getLocation().distance(event.getPlayer().getLocation()))
									theDenizen = npc;
							}
						} 

						/* Cancel out if for some reason no denizen can be found */
						if (theDenizen == null) return;

						/* Set MetaData and Trigger */
						if (event.getPlayer().hasMetadata("locationtrigger")) {

							/* Unless current MetaData already contains the location trigger. This means the player
							 * is still in the location... */
							if (locationTriggered.equals(event.getPlayer().getMetadata("locationtrigger")))
								return;

							/* Before triggering, check if LocationTriggers are enabled, cooldown is met, and NPC
							 * is not already engaged... */
							else if (!locationTriggered.equals(event.getPlayer().getMetadata("locationtrigger"))
									&& theDenizen.getTrait(DenizenTrait.class).enableLocationTriggers) {
								if (plugin.scriptEngine.checkCooldown(event.getPlayer(), TriggerType.LOCATION)
										&& !plugin.scriptEngine.getEngaged(theDenizen)) {

									/* Set Metadata value to avoid retrigger. */
									event.getPlayer().setMetadata("locationtrigger", new FixedMetadataValue(plugin, locationTriggered));

									/* TRIGGER! */
									plugin.scriptEngine.setCooldown(event.getPlayer(), TriggerType.LOCATION, plugin.settings.DefaultLocationCooldown());
									plugin.scriptEngine.parseLocationTrigger(theDenizen, event.getPlayer(), locationTriggered);
								}
							}

						} else {

							/* Check if proximity triggers are enabled, check player cooldown, check if NPC is engaged... */
							if (theDenizen.getTrait(DenizenTrait.class).enableLocationTriggers) {
								if (plugin.scriptEngine.checkCooldown(event.getPlayer(), TriggerType.LOCATION)
										&& !plugin.scriptEngine.getEngaged(theDenizen)) {

									/* Set Metadata value to avoid retrigger. */
									event.getPlayer().setMetadata("locationtrigger", new FixedMetadataValue(plugin, locationTriggered));

									/* TRIGGER! */
									plugin.scriptEngine.setCooldown(event.getPlayer(), TriggerType.LOCATION, plugin.settings.DefaultLocationCooldown());
									plugin.scriptEngine.parseLocationTrigger(theDenizen, event.getPlayer(), locationTriggered);
								}
							}
						}
					}
				}
			}
		}
	}



	/* Listens for the PlayerMoveEvent to see if a player is within range
	 * of a Location Bookmark for a PLAYERTASK */

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



	/* Listens for player chat and determines if player is near a Denizen, and if so,
	 * checks if there are scripts to interact with. */

	@EventHandler
	public void chatTrigger(PlayerChatEvent event) {

		Denizen plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");
		try {
			NPC theDenizen = plugin.getDenizen.getClosest(event.getPlayer(), 
					plugin.settings.PlayerToNpcChatRangeInBlocks());

			/* If no Denizen in range, or the Denizen closest is engaged, return */
			if (theDenizen == null || !plugin.scriptEngine.getEngaged(theDenizen)) return;

			/* Get the script to use */
			String theScript = plugin.getScript.getInteractScript(theDenizen, event.getPlayer());

			/* No script matches, should we still show the player talking to the Denizen? */
			if (theScript.equalsIgnoreCase("NONE") && !plugin.settings.ChatGloballyIfNoChatTriggers()) { 
				event.setCancelled(true);
				String noscriptChat = null;
				if (plugin.getAssignments().contains("Denizens." + theDenizen.getId() + ".Texts.No Requirements Met")) 
					noscriptChat = plugin.getAssignments().getString("Denizens." + theDenizen.getId() + ".Texts.No Requirements Met");
				else noscriptChat = plugin.settings.DefaultNoRequirementsMetText();
				plugin.getDenizen.talkToPlayer(theDenizen, event.getPlayer(), plugin.getDenizen.formatChatText(noscriptChat, "CHAT", event.getPlayer(), theDenizen)[0], null, "CHAT");
			}

			/* Awesome! There's a matching script, let's parse the script to see if chat triggers match */
			if (!theScript.equalsIgnoreCase("NONE")) {
				if (plugin.scriptEngine.parseChatScript(theDenizen, event.getPlayer(), plugin.getScript.getNameFromEntry(theScript), event.getMessage()))
					event.setCancelled(true);
			}

		} catch (Exception e) {
			plugin.getLogger().log(Level.SEVERE, "Error processing chat event.", e);
		}
	}


	/* 
	 * 
	 * END EVENT LISTENERS 
	 * 
	 * 
	 * */


	private void showInfo(Player thePlayer, NPC theDenizen) {

		if (theDenizen.hasTrait(DenizenTrait.class))
			if (theDenizen.getTrait(DenizenTrait.class).isDenizen) {
				
				thePlayer.sendMessage(ChatColor.GOLD + "------ Denizen Info ------");

				/* Show Citizens NPC info. */

				thePlayer.sendMessage(ChatColor.GRAY + "C2 NPCID: " + ChatColor.GREEN + theDenizen.getId() + ChatColor.GRAY + "   Name: " + ChatColor.GREEN + theDenizen.getName());
				if (plugin.newbMode) thePlayer.sendMessage(ChatColor.GRAY + "Tip: Use " + ChatColor.WHITE + "/denizen setname" + ChatColor.GRAY + " to change the Denizen's name.");
				if (plugin.getSaves().contains("Denizens." + theDenizen.getId() + ".Position.Standing"))
					if (plugin.getSaves().getString("Denizens." + theDenizen.getId() + ".Position.Standing") != null)
						thePlayer.sendMessage(ChatColor.GRAY + "Current standing position: " + ChatColor.GREEN + plugin.getSaves().getString("Denizens." + theDenizen.getId() + ".Position.Standing"));
				thePlayer.sendMessage("");


				if (plugin.newbMode) thePlayer.sendMessage(ChatColor.GRAY + "Key: " + ChatColor.GREEN + "Assigned to Name. " + ChatColor.YELLOW + "Assigned to ID.");

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

				if (plugin.newbMode) thePlayer.sendMessage(ChatColor.GRAY + "Tip: Use " + ChatColor.WHITE + "/denizen assign" + ChatColor.GRAY + " to assign scripts.");
				if (plugin.newbMode) thePlayer.sendMessage(ChatColor.GRAY + "Turn on precision mode with " + ChatColor.WHITE + "/denizen precision" + ChatColor.GRAY + " to to assign to Id.");
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

				if (plugin.newbMode) thePlayer.sendMessage(ChatColor.GRAY + "Tip: Use " + ChatColor.WHITE + "/denizen bookmark" + ChatColor.GRAY + " to create bookmarks.");
				if (plugin.newbMode) thePlayer.sendMessage(ChatColor.GRAY + "Turn on precision mode with " + ChatColor.WHITE + "/denizen precision" + ChatColor.GRAY + " to assign to Id.");
				thePlayer.sendMessage("");		
			}
	}

}
