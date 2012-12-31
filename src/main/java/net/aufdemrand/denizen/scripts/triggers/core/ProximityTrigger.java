package net.aufdemrand.denizen.scripts.triggers.core;

import java.util.Iterator;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.npc.traits.TriggerTrait;
import net.aufdemrand.denizen.scripts.ScriptEngine.QueueType;
import net.aufdemrand.denizen.scripts.triggers.AbstractTrigger;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

public class ProximityTrigger extends AbstractTrigger implements Listener {
	@Override
	public void onEnable() {
	}
	
	public Integer getProximityRangeInBlocks () {
		// plugin.settings.ProximityTriggerRangeInBlocks()
		return 2;
	}
	
	@EventHandler
	public void proximityTrigger(PlayerMoveEvent event) {
		dB.echoDebug("proximityTrigger()");
		//
		// Make sure that the player actually moved.
		//
		if (!event.getTo ().getBlock ().equals (event.getFrom ().getBlock ())) {
			//
			// Get the player's location.
			//
			Location	playerLocation = event.getTo ();
			Integer	maxRange = 3;

			//
			// Iterate over all of the NPCs
			//
			Iterator<NPC>	it = CitizensAPI.getNPCRegistry().iterator();
			while (it.hasNext ()) {
				NPC	npc = it.next ();

				//
				// If this NPC is not spawned, in a different world, or too far away,
				// then ignore it.
				//
				if (npc.isSpawned()	&&
						npc.getBukkitEntity().getLocation().getWorld().equals(playerLocation.getWorld())	&&
						npc.getBukkitEntity().getLocation().distance(playerLocation) < maxRange) {
					continue;
				}

				//
				// If the NPC doesn't have triggers, or the triggers are not enabled, 
				// then just return.
				//
				if (!npc.hasTrait(TriggerTrait.class)) {
					continue;
				}
	
				if (!npc.getTrait(TriggerTrait.class).isEnabled(name)) {
					continue;
				}

				//
				// Can the NPC be interacted with or is it busy?
				//
				if (!npc.getTrait(TriggerTrait.class).trigger(this, event.getPlayer())) {
					continue;
				}
				
				//
				// If the npc was within range previously, then don't fire the trigger
				// again.
				//
				if (npc.getBukkitEntity().getLocation().distance(event.getFrom ()) < maxRange) {
					continue;
				}
				
				dB.echoDebug(ChatColor.GOLD + " FOUND NPC IN RANGE.");

				//
				// Fire the trigger.
				//
				DenizenNPC denizenNPC = denizen.getNPCRegistry().getDenizen(npc);
				String theScript = denizenNPC.getInteractScript(event.getPlayer(), this.getClass());
				this.parse(denizenNPC, event.getPlayer(), theScript);
			}
		}
	}
	
	public boolean parse (DenizenNPC theDenizen, Player thePlayer, String theScriptName) {
		String	theStep = sH.getCurrentStep(thePlayer, theScriptName);
		String	path = (theScriptName + ".Steps." + theStep + ".Proximity Trigger." + sH.scriptKey).toUpperCase();
		List<String> theScript = sH.getScriptContents (path);
		if (theScript == null || theScript.isEmpty()) {
			dB.echoDebug ("    No script found for: " + path);
			return false;
		}

		//
		// Queue the script in the player's queue.
		//
		sB.queueScriptEntries (
			thePlayer, 
			sB.buildScriptEntries (
				thePlayer, 
				theDenizen, 
				theScript, 
				theScriptName, 
				theStep), 
			QueueType.PLAYER);	

		return true;
	}
	
}
