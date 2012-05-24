package net.aufdemrand.denizen;

import java.util.*;

import net.citizensnpcs.api.npc.NPC;
import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.InteractScriptEngine;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerMoveEvent;


public class DenizenListener implements Listener {

	static Denizen plugin;
	public DenizenListener(Denizen instance) { plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen"); }


	public static void DenizenClicked(NPC theDenizen, Player thePlayer) {

		String theScript = InteractScriptEngine.GetInteractScript(theDenizen, thePlayer);

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
			InteractScriptEngine.ParseScript(theDenizen, thePlayer,	InteractScriptEngine.GetScriptName(theScript), "", InteractScriptEngine.Trigger.CLICK);
		}
	}



	@EventHandler
	public void PlayerProximityListener(PlayerMoveEvent event) {

	}


	/* PlayerChatListener
	 *
	 * Called when the player chats.  Determines if player is near a Denizen, and if so, checks if there
	 * are scripts to interact with.  Also handles the chat output for the Player talking to the Denizen.
	 *
	 * Calls GetDenizensWithinRange, TalkToNPC, GetInteractScript, ParseScript
	 */

	@EventHandler
	public void PlayerChatListener(PlayerChatEvent event) {

		List<NPC> DenizenList = InteractScriptEngine.GetDenizensWithinRange(event.getPlayer().
				getLocation(), event.getPlayer().getWorld(), plugin.getConfig().getInt("player_chat_range_in_blocks", 3));
		if (DenizenList.isEmpty()) return;

		event.setCancelled(true);

		for (NPC thisDenizen : DenizenList) {
			String theScript = InteractScriptEngine.GetInteractScript(thisDenizen, event.getPlayer());

			if (theScript.equals("none")) { 

				InteractScriptEngine.TalkToNPC(thisDenizen, event.getPlayer(), event.getMessage());

				List<String> CurrentPlayerQue = new ArrayList<String>();
				if (Denizen.playerQue.get(event.getPlayer()) != null) CurrentPlayerQue = Denizen.playerQue.get(event.getPlayer());
				Denizen.playerQue.remove(event.getPlayer());  // Should keep the talk queue from triggering mid-add

				CurrentPlayerQue.add(Integer.toString(thisDenizen.getId()) + ";" + theScript + ";"
						+ 0 + ";" + String.valueOf(System.currentTimeMillis()) + ";" + "CHAT " + plugin.getConfig().getString("Denizens." + thisDenizen.getId() 
								+ ".Texts.No Script Interact", "I have nothing to say to you at this time."));

				Denizen.playerQue.put(event.getPlayer(), CurrentPlayerQue);

			}

			else if (!theScript.equals("none")) InteractScriptEngine.ParseScript(thisDenizen, event.getPlayer(),
					InteractScriptEngine.GetScriptName(theScript), event.getMessage(), InteractScriptEngine.Trigger.CHAT);
		}
	}



}