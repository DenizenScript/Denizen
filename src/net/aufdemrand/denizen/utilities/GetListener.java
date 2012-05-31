package net.aufdemrand.denizen.utilities;

import java.util.ArrayList;
import java.util.List;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.ScriptEngine;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerMoveEvent;


public class GetListener implements Listener {

		
	
	/*
	 * DenizenClicked
	 * 
	 * Called when a click trigger is sent to a Denizen. Handles fetching of the script.
	 * 
	 */

	public void DenizenClicked(NPC theDenizen, Player thePlayer) {

		Denizen plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");		
		String theScript = Denizen.getScript.getInteractScript(theDenizen, thePlayer);

		if (theScript.equals("none")) {
			List<String> CurrentPlayerQue = new ArrayList<String>();
			if (Denizen.playerQue.get(thePlayer) != null) CurrentPlayerQue = Denizen.playerQue.get(thePlayer);
			Denizen.playerQue.remove(thePlayer);  // Should keep the talk queue from triggering mid-add
			CurrentPlayerQue.add(Integer.toString(theDenizen.getId()) + ";" + theScript + ";"
					+ 0 + ";" + String.valueOf(System.currentTimeMillis()) + ";" + "CHAT " + plugin.getConfig().getString("Denizens." + theDenizen.getId() 
							+ ".Texts.No Script Interact", "I have nothing to say to you at this time."));
			Denizen.playerQue.put(thePlayer, CurrentPlayerQue);
		}

		else if (!theScript.equals("none")) {
			Denizen.scriptEngine.parseScript(theDenizen, thePlayer,	Denizen.getScript.getNameFromEntry(theScript), "", ScriptEngine.Trigger.CLICK);
		}
	}



	@EventHandler
	public void PlayerProximityListener(PlayerMoveEvent event) {

		if (!event.getTo().getBlock().equals(event.getFrom().getBlock())) {
			
			
				
			
			
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
		boolean ignoreNoMatch = plugin.getConfig().getBoolean("chat_globably_if_no_chat_triggers", false);
		
		NPC theDenizen = Denizen.getDenizen.getClosest(event.getPlayer(), 
				plugin.getConfig().getInt("player_chat_range_in_blocks", 3));

		if (theDenizen == null) return;

			String theScript = Denizen.getScript.getInteractScript(theDenizen, event.getPlayer());

			if (theScript.equals("none") && !ignoreNoMatch) { 

				event.setCancelled(true);
				Denizen.getPlayer.talkToDenizen(theDenizen, event.getPlayer(), event.getMessage());

				List<String> CurrentPlayerQue = new ArrayList<String>();
				if (Denizen.playerQue.get(event.getPlayer()) != null) CurrentPlayerQue = Denizen.playerQue.get(event.getPlayer());
				Denizen.playerQue.remove(event.getPlayer());  // Should keep the talk queue from triggering mid-add

				CurrentPlayerQue.add(Integer.toString(theDenizen.getId()) + ";" + theScript + ";"
						+ 0 + ";" + String.valueOf(System.currentTimeMillis()) + ";" + "CHAT " + plugin.getConfig().getString("Denizens." + theDenizen.getId() 
								+ ".Texts.No Script Interact", "I have nothing to say to you at this time."));

				Denizen.playerQue.put(event.getPlayer(), CurrentPlayerQue);

			}

			else if (!theScript.equals("none")) Denizen.scriptEngine.parseScript(theDenizen, event.getPlayer(),
					Denizen.getScript.getNameFromEntry(theScript), event.getMessage(), ScriptEngine.Trigger.CHAT);
		
	}



}