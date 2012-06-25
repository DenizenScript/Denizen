package net.aufdemrand.denizen;

import java.util.List;
import java.util.logging.Level;

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


/**
 * Contains all the listeners and triggers for the Denizen Characters(NPCs).
 * Works with the ScriptEngine to carry out scripts.
 * 
 * @author Jeremy Schroeder
 *
 */

public class DenizenCharacter extends Character implements Listener {

	
	/* Listens for an NPC click. Right click sends out a Click Trigger, 
	 * left click sends out either a Damage Trigger or Click Trigger. */

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



	/* Listens for the PlayerMoveEvent to see if a player is within range
	 * of a Denizen to trigger a Proximity Trigger */

	@EventHandler
	public void PlayerProximityListener(PlayerMoveEvent event) {

		Denizen plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");

		try {

			/* Do not run any code unless the player actually moves blocks */
			if (!event.getTo().getBlock().equals(event.getFrom().getBlock())) {

				/* 
				 * TODO: Denizen Proximity Trigger 
				 */ 
	
			}
		} catch (Exception e) {
			plugin.getLogger().log(Level.SEVERE, "Error processing proximity event.", e);
		}
	}


	
	/* Listens for the PlayerMoveEvent to see if a player is within range
	 * of a Denizen to trigger a Location Trigger */

	@EventHandler
	public void PlayerLocationListener(PlayerMoveEvent event) {

		Denizen plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");

			/* Do not run any code unless the player actually moves blocks */
			if (!event.getTo().getBlock().equals(event.getFrom().getBlock())) {

				try {
				if (!Denizen.validLocations.isEmpty()) {

					for (Location theLocation : Denizen.validLocations.keySet()) {
						if (plugin.scriptEngine.checkLocation(event.getPlayer(), theLocation, 1) && plugin.getDenizen.checkLocationCooldown(event.getPlayer())) {

							String theScript = plugin.getScript.getInteractScript(CitizensAPI.getNPCRegistry().getNPC(Integer.valueOf(Denizen.validLocations.get(theLocation).split(":")[0])), event.getPlayer());
							if (!theScript.equals("none")) {
								plugin.scriptEngine.parseScript(
										CitizensAPI.getNPCRegistry().getNPC(Integer.valueOf(Denizen.validLocations.get(theLocation).split(":")[0])), 
										event.getPlayer(), 
										plugin.getScript.getNameFromEntry(theScript), 
										Denizen.validLocations.get(theLocation).split(":")[1],
										net.aufdemrand.denizen.scriptEngine.Trigger.LOCATION);

								Denizen.locationCooldown.put(event.getPlayer(), System.currentTimeMillis() + 30000);

								break;
							}
						}
					}
				}
			}
		} 
		
		catch (Exception e) {
			plugin.getLogger().log(Level.SEVERE, "Error processing proximity event.", e);
		}
	}

	
	/* Listens for player chat and determines if player is near a Denizen, and if so,
	 * checks if there are scripts to interact with. */

	@EventHandler
	public void PlayerChatListener(PlayerChatEvent event) {

		Denizen plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");
		try {
			NPC theDenizen = plugin.getDenizen.getClosest(event.getPlayer(), 
					plugin.settings.PlayerToNpcChatRangeInBlocks());

			/* If no Denizen in range, or the Denizen closest is engaged, return */
			if (theDenizen == null || Denizen.engagedNPC.contains(theDenizen)) return;

			/* Get the script to use */
			String theScript = plugin.getScript.getInteractScript(theDenizen, event.getPlayer());

			/* No script matches, should we still show the player talking to the Denizen? */
			if (theScript.equalsIgnoreCase("NONE") && !plugin.settings.ChatGloballyIfNoChatTriggers()) { 
				event.setCancelled(true);
				String noscriptChat = null;
				if (plugin.getAssignments().contains("Denizens." + theDenizen.getId() + ".Texts.No Requirements Met")) 
					noscriptChat = plugin.getAssignments().getString("Denizens." + theDenizen.getId() + ".Texts.No Requirements Met");
				else noscriptChat = plugin.settings.DefaultNoRequirementsMetText();
				plugin.getDenizen.talkToPlayer(theDenizen, event.getPlayer(), plugin.scriptEngine.formatChatText(noscriptChat, "CHAT", event.getPlayer(), theDenizen)[0], null, "CHAT");
			}

			/* Awesome! There's a matching script, let's parse the script to see if chat triggers match */
			if (!theScript.equalsIgnoreCase("NONE")) {
				if (plugin.scriptEngine.parseChatScript(theDenizen, event.getPlayer(), plugin.getScript.getNameFromEntry(theScript), event.getMessage()))
					event.setCancelled(true);
			}

		} catch (Exception e) {
			plugin.getLogger().log(Level.SEVERE, "Error processing chat event.", e);
		}
	}

	

	@Override
	public void load(DataKey arg0) throws NPCLoadException {

		/* Nothing to do here, yet. */

	}

	@Override
	public void save(DataKey arg0) {

		/* Nothing to do here, yet. */

	}



	/* Called when a click trigger is sent to a Denizen. Handles fetching of the script. */

	public void DenizenClicked(NPC theDenizen, Player thePlayer) {

		Denizen plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");
		try {

			/* Get the script to use */
			String theScript = plugin.getScript.getInteractScript(theDenizen, thePlayer);

			/* No script meets requirements, let's let the player know. */
			if (theScript.equals("none")) {
				String noscriptChat = null;
				if (plugin.getAssignments().contains("Denizens." + theDenizen.getName()	+ ".Texts.No Requirements Met")) 
					noscriptChat = plugin.getAssignments().getString("Denizens." + theDenizen.getName()	+ ".Texts.No Requirements Met");
				else noscriptChat = plugin.settings.DefaultNoRequirementsMetText();

				/* Make the Denizen chat to the Player */
				plugin.getDenizen.talkToPlayer(theDenizen, thePlayer, plugin.scriptEngine.formatChatText(noscriptChat, "CHAT", thePlayer, theDenizen)[0], null, "CHAT");
			}

			/* Script does match, let's send the script to the parser */
			else if (!theScript.equals("none")) 
				plugin.scriptEngine.parseClickScript(theDenizen, thePlayer, plugin.getScript.getNameFromEntry(theScript));

		} catch (Exception e) {
			plugin.getLogger().log(Level.SEVERE, "Error processing click event.", e);
		}
		
		return;
	}


	
	/* Called when a click trigger is sent to a Denizen. Handles fetching of the script. */

	public void DenizenDamaged(NPC theDenizen, Player thePlayer) {

		Denizen plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");
		try {

			/* Get the script to use */
			String theScript = plugin.getScript.getInteractScript(theDenizen, thePlayer);

			/* No script meets requirements, let's let the player know. */
			if (theScript.equals("none")) {
				String noscriptChat = null;
				if (plugin.getAssignments().contains("Denizens." + theDenizen.getName()	+ ".Texts.No Requirements Met")) 
					noscriptChat = plugin.getAssignments().getString("Denizens." + theDenizen.getName()	+ ".Texts.No Requirements Met");
				else noscriptChat = plugin.settings.DefaultNoRequirementsMetText();

				/* Make the Denizen chat to the Player */
				plugin.getDenizen.talkToPlayer(theDenizen, thePlayer, plugin.scriptEngine.formatChatText(noscriptChat, "CHAT", thePlayer, theDenizen)[0], null, "CHAT");
			}

			/* Script does match, let's send the script to the parser */
			else if (!theScript.equals("none")) 
				plugin.scriptEngine.parseClickScript(theDenizen, thePlayer, plugin.getScript.getNameFromEntry(theScript));

		} catch (Exception e) {
			plugin.getLogger().log(Level.SEVERE, "Error processing click event.", e);
		}
		
		return;
	}


}

