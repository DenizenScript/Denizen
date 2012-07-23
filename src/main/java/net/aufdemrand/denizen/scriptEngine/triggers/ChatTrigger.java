package net.aufdemrand.denizen.scriptEngine.triggers;

import java.util.logging.Level;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.scriptEngine.Trigger;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;

public class ChatTrigger extends Trigger implements Listener {

	/* Listens for player chat and determines if player is near a Denizen, and if so,
	 * checks if there are scripts to interact with. */

	@EventHandler
	public void chatTrigger(PlayerChatEvent event) {

		try {
			NPC theDenizen = plugin.getDenizen.getClosest(event.getPlayer(), 
					plugin.settings.PlayerToNpcChatRangeInBlocks());

			/* If no Denizen in range, or the Denizen closest is engaged, return */
			if (theDenizen == null || !plugin.scriptEngine.getEngaged(theDenizen)) return;

			/* Get the script to use */
			String theScript = plugin.getScript.getInteractScript(theDenizen, event.getPlayer());

			/* No script matches, should we still show the player talking to the Denizen? */
			if (theScript.equalsIgnoreCase("NONE") && !plugin.settings.ChatGloballyIfNoChatTriggers()) { 
				event.setCancelled(true);
				String noscriptChat = null;
				if (plugin.getAssignments().contains("Denizens." + theDenizen.getId() + ".Texts.No Requirements Met")) 
					noscriptChat = plugin.getAssignments().getString("Denizens." + theDenizen.getId() + ".Texts.No Requirements Met");
				else noscriptChat = plugin.settings.DefaultNoRequirementsMetText();
				plugin.getDenizen.talkToPlayer(theDenizen, event.getPlayer(), plugin.getDenizen.formatChatText(noscriptChat, "CHAT", event.getPlayer(), theDenizen)[0], null, "CHAT");
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
	
}
