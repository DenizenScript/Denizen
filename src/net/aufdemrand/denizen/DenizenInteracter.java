package net.aufdemrand.denizen;

import java.util.List;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.character.Character;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class DenizenInteracter extends Denizen implements Listener  {



	/*
	 * DenizenClicked
	 * 
	 * Called when a click trigger is sent to a Handles fetching of the script.
	 * 
	 */

	public void DenizenClicked(NPC theDenizen, Player thePlayer) {
		String theScript =getScript.getInteractScript(theDenizen, thePlayer);

		if (theScript.equals("none")) {

			String noscriptChat = null;

			if (getAssignments().contains("Denizens." + theDenizen.getName() 
					+ ".Texts.No Requirements Met")) 
				noscriptChat = getAssignments().getString("Denizens." + theDenizen.getName() 
						+ ".Texts.No Requirements Met");
			else
				noscriptChat =settings.DefaultNoRequirementsMetText();

			getDenizen.talkToPlayer(theDenizen, thePlayer,scriptEngine.formatChatText(noscriptChat, "CHAT", thePlayer, theDenizen)[0], null, "CHAT");

		}

		else if (!theScript.equals("none")) {
			scriptEngine.parseScript(theDenizen, thePlayer,	getScript.getNameFromEntry(theScript), "", ScriptEngine.Trigger.CLICK);
		}
	}


	
	/*
	 * Will be used for the Proximity trigger for Trigger scripts.
	 * Currently unused (obviously)
	 *  
	 */

	@EventHandler
	public void PlayerProximityListener(PlayerMoveEvent event) {


		/* Do not run any code unless the player actually moves blocks */

		if (!event.getTo().getBlock().equals(event.getFrom().getBlock())) {

			/* 
			 * TODO: Denizen Proximity Trigger 
			 */ 

			if (!Denizen.validLocations.isEmpty()) {

				for (Location theLocation :validLocations.keySet()) {
					if (scriptEngine.checkLocation(event.getPlayer(), theLocation, 1)
							&& getDenizen.checkLocationCooldown(event.getPlayer())) {

						String theScript =getScript.getInteractScript(CitizensAPI.getNPCRegistry().getNPC(Integer.valueOf(Denizen.validLocations.get(theLocation).split(":")[0])), event.getPlayer());
						if (!theScript.equals("none")) {
							scriptEngine.parseScript(
									CitizensAPI.getNPCRegistry().getNPC(Integer.valueOf(Denizen.validLocations.get(theLocation).split(":")[0])), 
									event.getPlayer(), 
									getScript.getNameFromEntry(theScript), 
									validLocations.get(theLocation).split(":")[1],
									ScriptEngine.Trigger.LOCATION);
							
							Denizen.locationCooldown.put(event.getPlayer(), System.currentTimeMillis() + 30000);

							break;
						}
					}
				}
			}

			/* Location Task Listener
			 * 
			 * ------- saves.yml ----------------
			 * Players:
			 *   aufdemrand:
			 *     Tasks:
			 *       List All:
			 *         Locations:
			 *         - theLocation:theDenizen:theId
			 *       List Entries:
			 *         Id:
			 *           Type: Location
			 *           Leeway: in blocks
			 *           Duration: in seconds
			 *           Script to trigger: script name
			 *		     Initiated: System.currentTimeMillis 
			 */

			if (getSaves().contains("Players." + event.getPlayer().getName() + ".Tasks.List All.Locations")) {
				List<String> listAll = getSaves().getStringList("Players." + event.getPlayer().getName() + ".Tasks.List All.Locations");			

				if (!listAll.isEmpty()) {
					for (String theTask : listAll) {
						String[] taskArgs = theTask.split(";");
						Location theLocation =getDenizen.getBookmark(taskArgs[1], taskArgs[0], "LOCATION");
						int theLeeway = getSaves().getInt("Players." + event.getPlayer().getName() + ".Tasks.List Entries." + taskArgs[2] + ".Leeway");
						long theDuration = getSaves().getLong("Players." + event.getPlayer().getName() + ".Tasks.List Entries." + taskArgs[2] + ".Duration");
						if (scriptEngine.checkLocation(event.getPlayer(), theLocation, theLeeway)) {
							if (getSaves().contains("Players." + event.getPlayer().getName() + ".Tasks.List Entries." + taskArgs[2] + ".Initiated")) {
								if (getSaves().getLong("Players." + event.getPlayer().getName() + ".Tasks.List Entries." + taskArgs[2] + ".Initiated")
										+ (theDuration * 1000) <= System.currentTimeMillis())scriptEngine.finishLocationTask(event.getPlayer(), taskArgs[2]);
							}
							else {
								getSaves().set("Players." + event.getPlayer().getName() + ".Tasks.List Entries." + taskArgs[2] + ".Initiated", System.currentTimeMillis());
								saveSaves();
							}
						}
					}
				}
			}

			/* Location Listener END */

		}
	}


	
	/* 
	 * PlayerChatListener
	 *
	 * Called when the player chats.  Determines if player is near a Denizen, and if so, checks if there
	 * are scripts to interact with.  Also handles the chat output for the Player talking to the
	 *
	 */

	@EventHandler
	public void PlayerChatListener(PlayerChatEvent event) {		

		NPC theDenizen = getDenizen.getClosest(event.getPlayer(), 
				settings.PlayerToNpcChatRangeInBlocks());

		if (theDenizen == null ||engagedNPC.contains(theDenizen)) return;

		String theScript = getScript.getInteractScript(theDenizen, event.getPlayer());

		if (theScript.equalsIgnoreCase("NONE") && !settings.ChatGloballyIfNoChatTriggers()) { 
			event.setCancelled(true);
			String noscriptChat = null;

			if (getAssignments().contains("Denizens." + theDenizen.getId() 
					+ ".Texts.No Requirements Met")) 
				noscriptChat = getAssignments().getString("Denizens." + theDenizen.getId() 
						+ ".Texts.No Requirements Met");
			else
				noscriptChat =settings.DefaultNoRequirementsMetText();

			getDenizen.talkToPlayer(theDenizen, event.getPlayer(),scriptEngine.formatChatText(noscriptChat, "CHAT", event.getPlayer(), theDenizen)[0], null, "CHAT");

		}

		if (!theScript.equalsIgnoreCase("NONE")) {
			if (scriptEngine.parseScript(theDenizen, event.getPlayer(),	getScript.getNameFromEntry(theScript), event.getMessage(), ScriptEngine.Trigger.CHAT))
				event.setCancelled(true);
		}

		return;
	}


}

