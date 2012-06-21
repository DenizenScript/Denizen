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

public class DenizenCharacter extends Character implements Listener {


	/*
	 * DenizenClicked
	 * 
	 * Called when a click trigger is sent to a Denizen. Handles fetching of the script.
	 * 
	 */

	public void DenizenClicked(NPC theDenizen, Player thePlayer) {

		Denizen plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");
		
		String theScript = plugin.getScript.getInteractScript(theDenizen, thePlayer);

		if (theScript.equals("none")) {

			String noscriptChat = null;

			if (plugin.getAssignments().contains("Denizens." + theDenizen.getName() 
					+ ".Texts.No Requirements Met")) 
				noscriptChat = plugin.getAssignments().getString("Denizens." + theDenizen.getName() 
						+ ".Texts.No Requirements Met");
			else
				noscriptChat = plugin.settings.DefaultNoRequirementsMetText();

			plugin.getDenizen.talkToPlayer(theDenizen, thePlayer, plugin.scriptEngine.formatChatText(noscriptChat, "CHAT", thePlayer, theDenizen)[0], null, "CHAT");

		}

		else if (!theScript.equals("none")) {
			plugin.scriptEngine.parseScript(theDenizen, thePlayer, plugin.getScript.getNameFromEntry(theScript), "", ScriptEngine.Trigger.CLICK);
		}
	}





	/*
	 * Will be used for the Proximity trigger for Trigger scripts.
	 * Currently unused (obviously)
	 *  
	 */

	@EventHandler
	public void PlayerProximityListener(PlayerMoveEvent event) {

		Denizen plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");

		/* Do not run any code unless the player actually moves blocks */

		if (!event.getTo().getBlock().equals(event.getFrom().getBlock())) {

			/* 
			 * TODO: Denizen Proximity Trigger 
			 */ 

			if (!Denizen.validLocations.isEmpty()) {
				

				for (Location theLocation : Denizen.validLocations.keySet()) {
					if (plugin.scriptEngine.checkLocation(event.getPlayer(), theLocation, 1)
							&& plugin.getDenizen.checkLocationCooldown(event.getPlayer())) {

						String theScript = plugin.getScript.getInteractScript(CitizensAPI.getNPCRegistry().getNPC(Integer.valueOf(Denizen.validLocations.get(theLocation).split(":")[0])), event.getPlayer());
						if (!theScript.equals("none")) {
							plugin.scriptEngine.parseScript(
									CitizensAPI.getNPCRegistry().getNPC(Integer.valueOf(Denizen.validLocations.get(theLocation).split(":")[0])), 
									event.getPlayer(), 
									plugin.getScript.getNameFromEntry(theScript), 
									Denizen.validLocations.get(theLocation).split(":")[1],
									ScriptEngine.Trigger.LOCATION);
							
							Denizen.locationCooldown.put(event.getPlayer(), System.currentTimeMillis() + 30000);

							break;
						}
					}
				}
			}



			/* Location Task Listener */

			/* 
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

			if (plugin.getSaves().contains("Players." + event.getPlayer().getName() + ".Tasks.List All.Locations")) {
				List<String> listAll = plugin.getSaves().getStringList("Players." + event.getPlayer().getName() + ".Tasks.List All.Locations");			

				if (!listAll.isEmpty()) {
					for (String theTask : listAll) {
						String[] taskArgs = theTask.split(";");
						Location theLocation = plugin.getDenizen.getBookmark(taskArgs[1], taskArgs[0], "LOCATION");
						int theLeeway = plugin.getSaves().getInt("Players." + event.getPlayer().getName() + ".Tasks.List Entries." + taskArgs[2] + ".Leeway");
						long theDuration = plugin.getSaves().getLong("Players." + event.getPlayer().getName() + ".Tasks.List Entries." + taskArgs[2] + ".Duration");
						if (plugin.scriptEngine.checkLocation(event.getPlayer(), theLocation, theLeeway)) {
							if (plugin.getSaves().contains("Players." + event.getPlayer().getName() + ".Tasks.List Entries." + taskArgs[2] + ".Initiated")) {
								if (plugin.getSaves().getLong("Players." + event.getPlayer().getName() + ".Tasks.List Entries." + taskArgs[2] + ".Initiated")
										+ (theDuration * 1000) <= System.currentTimeMillis()) plugin.scriptEngine.finishLocationTask(event.getPlayer(), taskArgs[2]);
							}
							else {
								plugin.getSaves().set("Players." + event.getPlayer().getName() + ".Tasks.List Entries." + taskArgs[2] + ".Initiated", System.currentTimeMillis());
								plugin.saveSaves();
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
	 * are scripts to interact with.  Also handles the chat output for the Player talking to the Denizen.
	 *
	 */

	@EventHandler
	public void PlayerChatListener(PlayerChatEvent event) {

		Denizen plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");
		
		NPC theDenizen = plugin.getDenizen.getClosest(event.getPlayer(), 
				plugin.settings.PlayerToNpcChatRangeInBlocks());

		if (theDenizen == null || Denizen.engagedNPC.contains(theDenizen)) return;

		String theScript = plugin.getScript.getInteractScript(theDenizen, event.getPlayer());

		if (theScript.equalsIgnoreCase("NONE") && !plugin.settings.ChatGloballyIfNoChatTriggers()) { 
			event.setCancelled(true);
			String noscriptChat = null;

			if (plugin.getAssignments().contains("Denizens." + theDenizen.getId() 
					+ ".Texts.No Requirements Met")) 
				noscriptChat = plugin.getAssignments().getString("Denizens." + theDenizen.getId() 
						+ ".Texts.No Requirements Met");
			else
				noscriptChat = plugin.settings.DefaultNoRequirementsMetText();

			plugin.getDenizen.talkToPlayer(theDenizen, event.getPlayer(), plugin.scriptEngine.formatChatText(noscriptChat, "CHAT", event.getPlayer(), theDenizen)[0], null, "CHAT");

		}

		if (!theScript.equalsIgnoreCase("NONE")) {
			if (plugin.scriptEngine.parseScript(theDenizen, event.getPlayer(),	plugin.getScript.getNameFromEntry(theScript), event.getMessage(), ScriptEngine.Trigger.CHAT))
				event.setCancelled(true);
		}

		return;
	}




	@Override
	public void load(DataKey arg0) throws NPCLoadException {

		/* Nothing to do here, yet. */
		
	}

	@Override
	public void save(DataKey arg0) {

		/* Nothing to do here, yet. */

	}





	/*
	 * onRightClick/onLeftClick
	 * 
	 * Initiates the Click Trigger event when clicking on a Denizen.
	 * 
	 * Note: Soon turning left click into a Damage trigger, if Denizen is set to 'damageable'.
	 * If Denizen is not 'damageable', left click will mimic right click, that is, trigger the
	 * Click Trigger for the script.
	 * 
	 * Right click will remain Click Trigger.
	 * 
	 */

	@Override
	public void onRightClick(NPC npc, Player player) {

		Denizen plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");
		
		if(npc.getCharacter() == CitizensAPI.getCharacterManager().getCharacter("denizen") 
				&& plugin.getDenizen.checkCooldown(player)
				&& !Denizen.engagedNPC.contains(npc)) {
			Denizen.interactCooldown.put(player, System.currentTimeMillis() + 2000);
			DenizenClicked(npc, player);
		}
	}



	@Override
	public void onLeftClick(NPC npc, Player player) {

		Denizen plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");
		
		if(npc.getCharacter() == CitizensAPI.getCharacterManager().getCharacter("denizen") 
				&& plugin.getDenizen.checkCooldown(player)
				&& !Denizen.engagedNPC.contains(npc)) {
			Denizen.interactCooldown.put(player, System.currentTimeMillis() + 2000);
			DenizenClicked(npc, player);

		}
	}





}

