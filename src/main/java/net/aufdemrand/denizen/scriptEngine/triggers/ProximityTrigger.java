package net.aufdemrand.denizen.scriptEngine.triggers;

import net.aufdemrand.denizen.DenizenTrait;
import net.aufdemrand.denizen.scriptEngine.Trigger;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.metadata.FixedMetadataValue;

public class ProximityTrigger extends Trigger implements Listener {

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
						if (plugin.scriptEngine.checkCooldown(event.getPlayer(), ProximityTrigger.class)
								&& !plugin.scriptEngine.getEngaged(theDenizen)) {

							/* Set Metadata value to avoid retrigger. */
							event.getPlayer().setMetadata("npcinproximity", new FixedMetadataValue(plugin, plugin.getDenizen.getClosest(event.getPlayer(), plugin.settings.ProximityTriggerRangeInBlocks())));

							/* TRIGGER! */
							plugin.scriptEngine.setCooldown(event.getPlayer(), ProximityTrigger.class, plugin.settings.DefaultProximityCooldown());
							plugin.scriptEngine.parseProximityTrigger(theDenizen, event.getPlayer());
						}
					}

				} else { /* Player does not have metadata */

					/* Check if proximity triggers are enabled, check player cooldown, check if NPC is engaged... */
					if (theDenizen.getTrait(DenizenTrait.class).enableProximityTriggers) {
						if (plugin.scriptEngine.checkCooldown(event.getPlayer(), ProximityTrigger.class)
								&& !plugin.scriptEngine.getEngaged(theDenizen)) {

							/* Set Metadata value to avoid retrigger. */
							event.getPlayer().setMetadata("npcinproximity", new FixedMetadataValue(plugin, plugin.getDenizen.getClosest(event.getPlayer(), plugin.settings.ProximityTriggerRangeInBlocks())));

							/* TRIGGER! */
							plugin.scriptEngine.setCooldown(event.getPlayer(), ProximityTrigger.class, plugin.settings.DefaultProximityCooldown());
							plugin.scriptEngine.parseProximityTrigger(theDenizen, event.getPlayer());
						}
					}
				}
			}
		}
	}

	
}
