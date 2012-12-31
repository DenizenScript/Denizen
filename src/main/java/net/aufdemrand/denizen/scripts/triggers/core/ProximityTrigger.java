package net.aufdemrand.denizen.scripts.triggers.core;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.metadata.FixedMetadataValue;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.npc.traits.TriggerTrait;
import net.aufdemrand.denizen.scripts.ScriptHelper;
import net.aufdemrand.denizen.scripts.triggers.AbstractTrigger;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.citizensnpcs.api.npc.NPC;

public class ProximityTrigger extends AbstractTrigger implements Listener {
	@Override
	public void onEnable() {
	}
	
	public Integer getProximityRageInBlocks () {
		// plugin.settings.ProximityTriggerRangeInBlocks()
		return 2;
	}
	
	@EventHandler
	public void proximityTrigger(PlayerMoveEvent event) {
		//
		// Make sure that the player actually moved.
		//
		if (!event.getTo ().getBlock ().equals (event.getFrom ().getBlock ())) {
			ScriptHelper	sh = denizen.getScriptEngine().getScriptHelper();

			//
			// Get a list of all NPCs within range that should get the proximity
			// trigger.
			//
			List<NPC> npcsInRange = Utilities.getClosestNPCs(event.getPlayer().getLocation(), this.getProximityRageInBlocks());

			for (NPC npc : npcsInRange) {
				//
				// If the NPC doesn't have triggers, or the triggers are not enabled, then
				// just return.
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

				if (event.getPlayer().hasMetadata("proximity")) {
					if (event.getPlayer().getMetadata("proximity").get(0).asString().equals(npc.toString())) {
						continue;
					}
				} else {
					
				}
				event.getPlayer().setMetadata("proximity", new FixedMetadataValue(denizen, npc.toString()));
				
			}
/*
			if (plugin.getDenizenNPCRegistry().getClosest(event.getPlayer(), this.getProximityRageInBlocks()) != null) {
				DenizenNPC theDenizen = plugin.getDenizenNPCRegistry().getClosest(event.getPlayer(), plugin.settings.ProximityTriggerRangeInBlocks());

				if (theDenizen.getCitizensEntity().getTrait(DenizenTrait.class).triggerIsEnabled("proximity")) {

					if (event.getPlayer().hasMetadata("proximity")) {

						// If closest is same as stored metadata, avoid retrigger.
						if (event.getPlayer().getMetadata("proximity").get(0).asString().equals(theDenizen.toString())) {
							if (plugin.debugMode) if (!event.getPlayer().hasMetadata("proximitydebug")) {
								event.getPlayer().setMetadata("proximitydebug", new FixedMetadataValue(plugin, true));
								return;
							}
							else return;
						}

						// Set Metadata value to avoid retrigger.
						event.getPlayer().setMetadata("proximity", new FixedMetadataValue(plugin, theDenizen.toString()));
						if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...proximity metadata now: '" + event.getPlayer().getMetadata("proximity").get(0).asString() + "'");

						// If closest is different than stored metadata and proximity trigger is enabled for said NPC, trigger
						if (theDenizen.isInteractable(triggerName, event.getPlayer())) {

							// TRIGGER!
							sE.setCooldown(theDenizen, ProximityTrigger.class, plugin.settings.DefaultProximityCooldown());
							parseProximityTrigger(theDenizen, event.getPlayer());
						}

					} else {// Player does not have metadata

						// Check if proximity triggers are enabled, check player cooldown, check if NPC is engaged...
						if (theDenizen.isInteractable(triggerName, event.getPlayer())) {

							// Set Metadata value to avoid retrigger.
							event.getPlayer().setMetadata("proximity", new FixedMetadataValue(plugin, theDenizen.toString()));
							if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...proximity metadata was empty, now: " + event.getPlayer().getMetadata("proximity").get(0).asString() + "'");

							// TRIGGER!
							sE.setCooldown(theDenizen, ProximityTrigger.class, plugin.settings.DefaultProximityCooldown());
							parseProximityTrigger(theDenizen, event.getPlayer());
						}
					}
				}
			}

			else {
				if (event.getPlayer().hasMetadata("proximity")) event.getPlayer().removeMetadata("proximity", plugin);
				if (plugin.debugMode) if (event.getPlayer().hasMetadata("proximitydebug")) event.getPlayer().removeMetadata("proximitydebug", plugin);
			}
			*/
		}
	}
	
	public boolean parse (DenizenNPC theDenizen, Player thePlayer, String theScriptName) {
/*
		if (cs == null) cs = Bukkit.getConsoleSender();
		ScriptHelper sE = plugin.getScriptEngine().helper;

		// Get Interact Script, if any. 
		String theScriptName = theDenizen.getInteractScript(thePlayer, this.getClass());

		if (theScriptName == null) {
			
			// Check for Quick Script
			if (plugin.getAssignments().contains("Denizens." + theDenizen.getName() + ".Quick Scripts.Proximity Trigger.Script")) {

				if (plugin.debugMode) cs.sendMessage(ChatColor.LIGHT_PURPLE + "+- Parsing QUICK PROXIMITY script: " + theDenizen.getName() + "/" + thePlayer.getName() + " -+");
				
				// Get the contents of the Script. 
				List<String> theScript = plugin.getAssignments().getStringList("Denizens." + theDenizen.getName() + ".Quick Scripts.Proximity Trigger.Script");

				if (theScript.isEmpty()) return false;

				// Build scriptEntries from theScript and add it into the queue 
				sE.queueScriptEntries(thePlayer, sE.buildScriptEntries(thePlayer, theDenizen, theScript, theDenizen.getName() + " Quick Proximity", 1), QueueType.TASK);
				
				return true;
			}
		}

		if (plugin.debugMode) cs.sendMessage(ChatColor.LIGHT_PURPLE + "+- Parsing proximity trigger: " + theDenizen.getName() + "/" + thePlayer.getName() + " -+");
		if (plugin.debugMode) cs.sendMessage(ChatColor.LIGHT_PURPLE + "| " + ChatColor.WHITE + "Getting current step:");
		
		// Get Player's current step 
		Integer theStep = sE.getCurrentStep(thePlayer, theScriptName);

		// Get the contents of the Script. 
		List<String> theScript = sE.getScript(sE.getTriggerPath(theScriptName, theStep, triggerName) + sE.scriptString);
		
		if (theScript.isEmpty()) return false;

		// Build scriptEntries from theScript and add it into the queue 
		sE.queueScriptEntries(thePlayer, sE.buildScriptEntries(thePlayer, theDenizen, theScript, theScriptName, theStep), QueueType.TRIGGER);

*/
		return true;
	}
	
}
