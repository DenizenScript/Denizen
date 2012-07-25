package net.aufdemrand.denizen.scriptEngine.triggers;

import java.util.List;

import net.aufdemrand.denizen.scriptEngine.AbstractTrigger;
import net.aufdemrand.denizen.scriptEngine.ScriptHelper;
import net.aufdemrand.denizen.scriptEngine.ScriptEngine.QueueType;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.metadata.FixedMetadataValue;

public class ProximityTrigger extends AbstractTrigger implements Listener {

	/* Listens for the PlayerMoveEvent to see if a player is within range
	 * of a Denizen to trigger a Proximity Trigger */

	@EventHandler
	public void proximityTrigger(PlayerMoveEvent event) {

		/* Do not run any code unless the player actually moves blocks */
		if (!event.getTo().getBlock().equals(event.getFrom().getBlock())) {

			/* Do not run any further code if no Denizen is in range */
			if (plugin.getDenizen.getClosest(event.getPlayer(), plugin.settings.ProximityTriggerRangeInBlocks()) != null) {
				NPC theDenizen = plugin.getDenizen.getClosest(event.getPlayer(), plugin.settings.ProximityTriggerRangeInBlocks());
				ScriptHelper sE = plugin.getScriptEngine().helper;

				if (event.getPlayer().hasMetadata("npcinproximity")) {

					/* If closest is same as stored metadata, avoid retrigger. */
					if (theDenizen == event.getPlayer().getMetadata("npcinproximity"))
						return;

					/* If closest is different than stored metadata and proximity trigger is enabled for said NPC, trigger */
					else if (sE.denizenIsInteractable(triggerName, theDenizen, event.getPlayer())) {

						/* Set Metadata value to avoid retrigger. */
						event.getPlayer().setMetadata("npcinproximity", new FixedMetadataValue(plugin, plugin.getDenizen.getClosest(event.getPlayer(), plugin.settings.ProximityTriggerRangeInBlocks())));

						/* TRIGGER! */
						sE.setCooldown(event.getPlayer(), ProximityTrigger.class, plugin.settings.DefaultProximityCooldown());
						parseProximityTrigger(theDenizen, event.getPlayer());
					}

				} else { /* Player does not have metadata */

					/* Check if proximity triggers are enabled, check player cooldown, check if NPC is engaged... */
					if (sE.denizenIsInteractable(triggerName, theDenizen, event.getPlayer())) {

						/* Set Metadata value to avoid retrigger. */
						event.getPlayer().setMetadata("npcinproximity", new FixedMetadataValue(plugin, plugin.getDenizen.getClosest(event.getPlayer(), plugin.settings.ProximityTriggerRangeInBlocks())));

						/* TRIGGER! */
						sE.setCooldown(event.getPlayer(), ProximityTrigger.class, plugin.settings.DefaultProximityCooldown());
						parseProximityTrigger(theDenizen, event.getPlayer());
					}
				}
			}
		}
	}
	
	
	
	public boolean parseProximityTrigger(NPC theDenizen, Player thePlayer) {

		ScriptHelper sE = plugin.getScriptEngine().helper;

		/* Get Interact Script, if any. */
		String theScriptName = sE.getInteractScript(theDenizen, thePlayer);

		if (theScriptName == null) return false;

		/* Get Player's current step */
		Integer theStep = sE.getCurrentStep(thePlayer, theScriptName);

		/* Get the contents of the Script. */
		List<String> theScript = sE.getScript(sE.getTriggerPath(theScriptName, theStep, triggerName) + sE.scriptString);

		/* Build scriptEntries from theScript and add it into the queue */
		sE.queueScriptEntries(thePlayer, sE.buildScriptEntries(thePlayer, theDenizen, theScript, theScriptName, theStep), QueueType.TRIGGER);

		return true;
	}


}
