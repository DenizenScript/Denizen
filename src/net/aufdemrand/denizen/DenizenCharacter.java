package net.aufdemrand.denizen;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.character.Character;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.Bukkit;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class DenizenCharacter extends Character implements Listener {

	private Denizen plugin;
	
	
	/*
	 * DenizenClicked
	 * 
	 * Called when a click trigger is sent to a Denizen. Handles fetching of the script.
	 * 
	 */

	public void DenizenClicked(NPC theDenizen, Player thePlayer) {
		plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");		
		String theScript = Denizen.getScript.getInteractScript(theDenizen, thePlayer);

		if (theScript.equals("none")) {

			String noscriptChat = null;

			if (plugin.getAssignments().contains("Denizens." + theDenizen.getName() 
					+ ".Texts.No Requirements Met")) 
				noscriptChat = plugin.getAssignments().getString("Denizens." + theDenizen.getName() 
						+ ".Texts.No Requirements Met");
			else
				noscriptChat = Denizen.settings.DefaultNoRequirementsMetText();

			Denizen.getDenizen.talkToPlayer(theDenizen, thePlayer, Denizen.scriptEngine.formatChatText(noscriptChat, "CHAT", thePlayer, theDenizen)[0], null, "CHAT");

		}

		else if (!theScript.equals("none")) {
			Denizen.scriptEngine.parseScript(theDenizen, thePlayer,	Denizen.getScript.getNameFromEntry(theScript), "", ScriptEngine.Trigger.CLICK);
		}
	}

	
	
	
	
	/*
	 * Will be used for the Proximity trigger for Trigger scripts.
	 * Currently unused (obviously)
	 *  
	 */

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
		plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");		

		NPC theDenizen = Denizen.getDenizen.getClosest(event.getPlayer(), 
				Denizen.settings.PlayerToNpcChatRangeInBlocks());

		if (theDenizen == null) return;

		String theScript = Denizen.getScript.getInteractScript(theDenizen, event.getPlayer());

		if (theScript.equalsIgnoreCase("NONE") && !Denizen.settings.ChatGloballyIfNoChatTriggers()) { 
			event.setCancelled(true);
			String noscriptChat = null;

			if (plugin.getAssignments().contains("Denizens." + theDenizen.getId() 
					+ ".Texts.No Requirements Met")) 
				noscriptChat = plugin.getAssignments().getString("Denizens." + theDenizen.getId() 
						+ ".Texts.No Requirements Met");
			else
				noscriptChat = Denizen.settings.DefaultNoRequirementsMetText();

			Denizen.getDenizen.talkToPlayer(theDenizen, event.getPlayer(), Denizen.scriptEngine.formatChatText(noscriptChat, "CHAT", event.getPlayer(), theDenizen)[0], null, "CHAT");

		}

		if (!theScript.equalsIgnoreCase("NONE")) {
			if (Denizen.scriptEngine.parseScript(theDenizen, event.getPlayer(),	Denizen.getScript.getNameFromEntry(theScript), event.getMessage(), ScriptEngine.Trigger.CHAT))
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
		if(npc.getCharacter() == CitizensAPI.getCharacterManager().getCharacter("denizen") && Denizen.getDenizen.checkCooldown(player)) {
			Denizen.interactCooldown.put(player, System.currentTimeMillis() + 2000);
			DenizenClicked(npc, player);
		}
	}



	@Override
	public void onLeftClick(NPC npc, Player player) {
		if(npc.getCharacter() == CitizensAPI.getCharacterManager().getCharacter("denizen") && Denizen.getDenizen.checkCooldown(player)) {
			Denizen.interactCooldown.put(player, System.currentTimeMillis() + 2000);
			DenizenClicked(npc, player);
		}
	}





}

