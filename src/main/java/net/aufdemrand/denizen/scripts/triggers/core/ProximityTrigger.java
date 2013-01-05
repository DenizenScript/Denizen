package net.aufdemrand.denizen.scripts.triggers.core;

import java.util.Arrays;
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
import net.aufdemrand.denizen.scripts.ScriptHelper;
import net.aufdemrand.denizen.scripts.triggers.AbstractTrigger;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

/**
 * <p>The Proximity Trigger is used to execute a script when a player moves
 * within a certain radius of a location.</p>
 * 
 * Example Usage:<br/>
 * <ol>
 * <tt>
 * Proximity Trigger:<br/>
 * &nbsp;&nbsp;Entry:<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;Script:<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;- CHAT "Hello <PLAYER.NAME>! Welcome to my shop!"<br/>
 * &nbsp;&nbsp;Exit:<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;Script:<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;- CHAT "Thanks for visiting <PLAYER.NAME>"<br/>
 * </tt>
 * </ol>
 * 
 * TODO:  Currently the range that triggers the proximity trigger is hard-coded
 * to 3 blocks.<br/>
 * 
 * @author dbixler
 */
public class ProximityTrigger extends AbstractTrigger implements Listener {
	@Override
	public void onEnable() {
    denizen.getServer().getPluginManager().registerEvents(this, denizen);
	}
	
	// TODO: This goes into settings.
	public Integer getProximityRangeInBlocks () {
		// plugin.settings.ProximityTriggerRangeInBlocks()
		return 3;
	}
	
	/**
	 * <p> This is the trigger that fires when any player moves in the entire
	 * world.  The trigger ONLY checks if the player moves to a new BLOCK in the 
	 * world</p>
	 *
	 * When the trigger determines that the player has moved to a different block
	 * in the world, all of the NPCs are checked for the following criteria:
	 * <ol>
	 * <li>Does the NPC have the trigger trait?</li>
	 * <li>Is the trigger enabled?</li>
	 * <li>Is the NPC available (i.e. not busy)?</li>
	 * <li>Is the NPC Spawned?</li>
	 * <li>Is the NPC in the same World as the player</li>
	 * </ol>
	 * 
	 * If the NPC passes all of these criteria, there are two events that can
	 * occur (one or the other):
	 * 
	 * <ol>
	 * <li>If the player was outside of the NPC's radius, and moved inside the
	 * radius, and there's a SCRIPT or an ENTRY SCRIPT, then execute that entry
	 * script.</li>
	 * <li>If the player was INSIDE of the NPC's radius, and moved OUTSIDE the
	 * radius, and there's an EXIT SCRIPT, then execute that exit script.
	 * </ol>
	 * 
	 * @param event	The player's move event (which includes their location).
	 */
	@EventHandler
	public void proximityTrigger(PlayerMoveEvent event) {
		//
		// Make sure that the player actually moved to a different block.
		//
		if (!event.getTo ().getBlock ().equals (event.getFrom ().getBlock ())) {
			//
			// Get the player's location.
			//
			Location	playerLocation = event.getTo ();
			
			Location	fromBlockLocation = event.getFrom ().getBlock().getLocation();
			Location	toBlockLocation = event.getTo ().getBlock ().getLocation ();

			//
			// Iterate over all of the NPCs
			//
			Iterator<NPC>	it = CitizensAPI.getNPCRegistry().iterator();
			while (it.hasNext ()) {
				NPC	npc = it.next ();

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
				// If this NPC is not spawned or in a different world, no need to 
				// check.
				//
				if (npc.isSpawned() == false ||
						npc.getBukkitEntity().getLocation().getWorld().equals(playerLocation.getWorld()) == false) {
					continue;
				}

				//
				// If the user entered the range and were not previously within the
				// range, then execute the "Entry" script.
				//
				// If the user is outside the range, and was previously within the
				// range, then execute the "Exit" script.
				//
				if (npc.getBukkitEntity().getLocation().distance(toBlockLocation) <= this.getProximityRangeInBlocks ()	&&
						npc.getBukkitEntity().getLocation().distance(fromBlockLocation) > this.getProximityRangeInBlocks ()) {
					dB.echoDebug(ChatColor.GOLD + " FOUND NPC IN ENTERING RANGE: " + npc.getFullName());
					DenizenNPC denizenNPC = denizen.getNPCRegistry().getDenizen(npc);
					String theScript = denizenNPC.getInteractScript(event.getPlayer(), this.getClass());
					this.parse(denizenNPC, event.getPlayer(), theScript, true);
				} else if (npc.getBukkitEntity().getLocation().distance(fromBlockLocation) <= this.getProximityRangeInBlocks ()	&&
									 npc.getBukkitEntity().getLocation().distance(toBlockLocation) > this.getProximityRangeInBlocks ()) {
					dB.echoDebug(ChatColor.GOLD + " FOUND NPC IN EXITING RANGE: " + npc.getFullName());
					DenizenNPC denizenNPC = denizen.getNPCRegistry().getDenizen(npc);
					String theScript = denizenNPC.getInteractScript(event.getPlayer(), this.getClass());
					this.parse(denizenNPC, event.getPlayer(), theScript, false);
				}
			}
		}
	}

	/**
	 * This parses the ProximityTrigger's script.
	 * 
	 * @param theDenizen	The Denizen that has the proximity trigger.
	 * @param thePlayer	The Player that caused the trigger to fire.
	 * @param theScriptName	The script that is being executed.
	 * @param entry	True if this should fire an entry script, or false if this
	 * 							should fire an exit script.
	 * 
	 * @return
	 */
	public boolean parse (DenizenNPC theDenizen, Player thePlayer, String theScriptName, boolean entry) {
		if (theScriptName == null) {
			return false;
		}

		//
		// Get the path to the step that the player is currently on.
		//
		String	theStep = sH.getCurrentStep(thePlayer, theScriptName);
		
		//
		// Determine which scripts need to be executed:  Either the entry scripts
		// or the exit scripts.  To maintain backwards compatibility, the entry
		// scripts can either be in a section called "Entry" or as a "Script" under
		// the "Proximity Trigger" setting.
		//
		List<String> scriptsToParse;
		if (entry) {
			scriptsToParse = Arrays.asList(
				(theScriptName + ".Steps." + theStep + ".Proximity Trigger." + ScriptHelper.scriptKey).toUpperCase(),
				(theScriptName + ".Steps." + theStep + ".Proximity Trigger.Entry." + ScriptHelper.scriptKey).toUpperCase()
			);
		} else {
			scriptsToParse = Arrays.asList(
				(theScriptName + ".Steps." + theStep + ".Proximity Trigger.Exit" + ScriptHelper.scriptKey).toUpperCase()
			);
		}
		
		//
		// Parse the scripts.
		//
		for (String path : scriptsToParse) {
			List<String> theScript = sH.getScriptContents (path);
			if (theScript != null && theScript.isEmpty() == false) {
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
			}
		}
		
		return true;
	}
	
}
