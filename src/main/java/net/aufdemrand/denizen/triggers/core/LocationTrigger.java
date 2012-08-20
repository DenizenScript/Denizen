package net.aufdemrand.denizen.triggers.core;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.metadata.FixedMetadataValue;

import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.npc.DenizenTrait;
import net.aufdemrand.denizen.scripts.ScriptHelper;
import net.aufdemrand.denizen.scripts.ScriptEngine.QueueType;
import net.aufdemrand.denizen.triggers.AbstractTrigger;
import net.citizensnpcs.api.CitizensAPI;

public class LocationTrigger extends AbstractTrigger implements Listener {

	/* Listens for the PlayerMoveEvent to see if a player is within range
	 * of a Denizen to trigger a Location Trigger */

	@EventHandler
	public void locationTrigger(PlayerMoveEvent event) {


		/* Do not run any code unless the player actually moves blocks */
		if (event.getTo().getBlock().equals(event.getFrom().getBlock()))
			return;

		ScriptHelper sE = plugin.getScriptEngine().helper;

		/* Do not run any code if there aren't any location triggers */
		if (!plugin.bookmarks.getLocationTriggerList().isEmpty()) {

			boolean hasLocation = false;

			/* Check player location against each Location Trigger */
			for (Location theLocation : plugin.bookmarks.getLocationTriggerList().keySet()) {

				if (plugin.bookmarks.checkLocation(event.getPlayer(), theLocation, plugin.settings.LocationTriggerRangeInBlocks())) {

					hasLocation = true;


					DenizenNPC theDenizen = null;
					String locationTriggered = plugin.bookmarks.getLocationTriggerList().get(theLocation).split(":")[2];

					if (plugin.debugMode) 	plugin.getLogger().info("Found location: " + locationTriggered);

					/* Player matches Location, find NPC it belongs to */

					if (plugin.bookmarks.getLocationTriggerList().get(theLocation).contains("ID:"))
						theDenizen = plugin.getDenizenNPCRegistry().getDenizen(CitizensAPI.getNPCRegistry().getById(Integer.valueOf(plugin.bookmarks.getLocationTriggerList().get(theLocation).split(":")[1])));


					else if (plugin.bookmarks.getLocationTriggerList().get(theLocation).contains("NAME:")) {
						List<DenizenNPC> denizenList = new ArrayList<DenizenNPC>();

						/* Find all the NPCs with the name */
						for (DenizenNPC npc : plugin.getDenizenNPCRegistry().getDenizens().values()) {
							if(npc.getName().equals(plugin.bookmarks.getLocationTriggerList().get(theLocation).split(":")[1])) {
								denizenList.add(npc);
								theDenizen = npc;
							}
						}

						/* Check which NPC is closest */
						for (DenizenNPC npc : denizenList) {
							if(npc.getEntity() == null) continue;
							if (npc.getEntity().getLocation().distance(event.getPlayer().getLocation())
									< theDenizen.getEntity().getLocation().distance(event.getPlayer().getLocation()))
								theDenizen = npc;
						}
					} 

					/* Cancel out if for some reason no denizen can be found */
					if (theDenizen == null) return;

					if (theDenizen.getCitizensEntity().getTrait(DenizenTrait.class).triggerIsEnabled("location")) {

						/* Set MetaData and Trigger */
						if (event.getPlayer().hasMetadata("locationtrigger")) {

							/* Unless current MetaData already contains the location trigger. This means the player
							 * is still in the location... */
							if (locationTriggered.equals(event.getPlayer().getMetadata("locationtrigger").get(0).asString()))
								return;

							/* Set Metadata value to avoid retrigger. */
							event.getPlayer().setMetadata("locationtrigger", new FixedMetadataValue(plugin, locationTriggered));
							if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...location metadata now: '" + event.getPlayer().getMetadata("locationtrigger").get(0).asString() + "'");

							/* Before triggering, check if LocationTriggers are enabled, cooldown is met, and NPC
							 * is not already engaged... */
							if (theDenizen.IsInteractable(triggerName, event.getPlayer())) {

								/* TRIGGER! */
								sE.setCooldown(theDenizen, LocationTrigger.class, plugin.settings.DefaultLocationCooldown());
								parseLocationTrigger(theDenizen, event.getPlayer(), locationTriggered);
							}

						} else {

							/* Set Metadata value to avoid retrigger. */
							event.getPlayer().setMetadata("locationtrigger", new FixedMetadataValue(plugin, locationTriggered));
							if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...location metadata was empty, now: " + event.getPlayer().getMetadata("locationtrigger").get(0).asString() + "'");

							/* Check if proximity triggers are enabled, check player cooldown, check if NPC is engaged... */
							if (theDenizen.IsInteractable(triggerName, event.getPlayer())) {

								/* TRIGGER! */
								sE.setCooldown(theDenizen, LocationTrigger.class, plugin.settings.DefaultLocationCooldown());
								parseLocationTrigger(theDenizen, event.getPlayer(), locationTriggered);
							}
						}
					}
				}
			}

			if (!hasLocation) {
				if (event.getPlayer().hasMetadata("locationtrigger")) event.getPlayer().removeMetadata("locationtrigger", plugin);
			}
		}

	}



	private void parseLocationTrigger(DenizenNPC theDenizen, Player thePlayer, String theLocationName) {

		/* Find script and run it */	
		if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "Parsing Location Trigger.");

		ScriptHelper sE = plugin.getScriptEngine().helper;

		String theScriptName = theDenizen.getInteractScript(thePlayer, this.getClass());
		if (theScriptName == null) return;

		CommandSender cs = Bukkit.getConsoleSender();

		if (plugin.debugMode) cs.sendMessage(ChatColor.LIGHT_PURPLE + "+- Parsing location trigger: " + theDenizen.getName() + "/" + thePlayer.getName() + " -+");
		if (plugin.debugMode) cs.sendMessage(ChatColor.LIGHT_PURPLE + "| " + ChatColor.WHITE + "Getting current step:");

		Integer theStep = sE.getCurrentStep(thePlayer, theScriptName);

		boolean foundScript = false;
		boolean noMatch = true;
		int x = 1;

		do {
			foundScript = true;
			if (plugin.getScripts().contains(sE.getTriggerPath(theScriptName, theStep, triggerName) + x + ".Trigger")) {
				if (plugin.getScripts().getString(sE.getTriggerPath(theScriptName, theStep, triggerName) + x + ".Trigger").equals(theLocationName)) {
					noMatch = false;
					if (plugin.debugMode) cs.sendMessage(ChatColor.LIGHT_PURPLE + "| " + ChatColor.GREEN + "OKAY! " + ChatColor.WHITE + "Found location in the script.");
				} else {
					foundScript = false;
					x++;
				}
			}
		} while (foundScript == false);

		if (!noMatch) {
			List<String> theScript = sE.getScript(sE.getTriggerPath(theScriptName, theStep, triggerName) + x + sE.scriptString);
			sE.queueScriptEntries(thePlayer, sE.buildScriptEntries(thePlayer, theDenizen, theScript, theScriptName, theStep), QueueType.TRIGGER);
		} 
		else {
			if (plugin.debugMode) cs.sendMessage(ChatColor.LIGHT_PURPLE + "| " + ChatColor.YELLOW + "INFO! " + ChatColor.WHITE + "No matching Triggers found for this Location.");
			if (plugin.debugMode) {
				String locationbookmarks = "Available triggers on this NPC: ";
				boolean hasbookmark = false;
				for (String triggerlistitem : plugin.bookmarks.getLocationTriggerList().values()) {
					hasbookmark = true;
					if (triggerlistitem.contains(theDenizen.getName())) locationbookmarks = locationbookmarks + triggerlistitem.split(":")[2] + ", ";
				}
				if (!hasbookmark) locationbookmarks = locationbookmarks + "NONE!";
				else locationbookmarks = locationbookmarks.substring(0, locationbookmarks.length() - 2);
				cs.sendMessage(ChatColor.LIGHT_PURPLE + "| " + ChatColor.YELLOW + "INFO! " + ChatColor.WHITE + locationbookmarks);
			}
			if (plugin.debugMode) cs.sendMessage(ChatColor.LIGHT_PURPLE + "+---------------------+");
		}

		return;
	}


}
