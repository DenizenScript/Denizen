package net.aufdemrand.denizen.triggers.core;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Location;
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
		if (!event.getTo().getBlock().equals(event.getFrom().getBlock())) {

			ScriptHelper sE = plugin.getScriptEngine().helper;

			/* Do not run any code if there aren't any location triggers */
			if (!plugin.bookmarks.getLocationTriggerList().isEmpty()) {

				boolean hasLocation = false;

				/* Check player location against each Location Trigger */
				for (Location theLocation : plugin.bookmarks.getLocationTriggerList().keySet()) {

					hasLocation = true;

					if (plugin.bookmarks.checkLocation(event.getPlayer(), theLocation, plugin.settings.LocationTriggerRangeInBlocks())) {

						DenizenNPC theDenizen = null;
						String locationTriggered = plugin.bookmarks.getLocationTriggerList().get(theLocation).split(":")[2];

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
	}



	private void parseLocationTrigger(DenizenNPC theDenizen, Player thePlayer, String theLocationName) {

		/* Find script and run it */	
		if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "Parsing Location Trigger.");

		ScriptHelper sE = plugin.getScriptEngine().helper;

		String theScriptName = theDenizen.getInteractScript(thePlayer, this.getClass());
		if (theScriptName == null) return;

		Integer theStep = sE.getCurrentStep(thePlayer, theScriptName);

		boolean foundScript = false;
		boolean noMatch = false;
		int x = 1;

		do {
			foundScript = true;
			if (plugin.getScripts().contains(sE.getTriggerPath(theScriptName, theStep, triggerName) + x + ".Trigger")) {
				if (plugin.getScripts().getString(sE.getTriggerPath(theScriptName, theStep, triggerName) + x + ".Trigger").equals(theLocationName)) {
					foundScript = true;
					if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...found matching Location!");
				} else {
					foundScript = false;
					x++;
				}
			}
			else noMatch = true;
		} while (foundScript == false);

		if (!noMatch) {
			List<String> theScript = sE.getScript(sE.getTriggerPath(theScriptName, theStep, triggerName) + x + sE.scriptString);
			sE.queueScriptEntries(thePlayer, sE.buildScriptEntries(thePlayer, theDenizen, theScript, theScriptName, theStep), QueueType.TRIGGER);
		} 
		else if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...no matching Triggers found for this Location.");

		return;
	}


}
