package net.aufdemrand.denizen.scriptEngine.triggers;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.scriptEngine.ScriptEntry;
import net.aufdemrand.denizen.scriptEngine.AbstractTrigger;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;

public class ChatTrigger extends AbstractTrigger implements Listener {

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
				if (parseChatScript(theDenizen, event.getPlayer(), plugin.getScript.getNameFromEntry(theScript), event.getMessage()))
					event.setCancelled(true);
			}

		} catch (Exception e) {
			plugin.getLogger().log(Level.SEVERE, "Error processing chat event.", e);
		}
	}
	
	
	
	
	/* Parses the scripts for Chat Triggers and sends new ScriptCommands to the queue if
	 * found matched. Returning FALSE will cancel intervention and allow the PlayerChatEvent
	 * to pass through.	 
	 */

	public boolean parseChatScript(NPC theDenizen, Player thePlayer, String theScript, String playerMessage) {

		int theStep = getCurrentStep(thePlayer, theScript);
		List<ScriptEntry> scriptCommands = new ArrayList<ScriptEntry>();

		/* Get Chat Triggers and check each to see if there are any matches. */
		List<String> ChatTriggerList = plugin.getScript.getChatTriggers(theScript, theStep);
		for (int x = 0; x < ChatTriggerList.size(); x++ ) {

			/* The texts required to trigger. */
			String chatTriggers = ChatTriggerList.get(x)
					.replace("<PLAYER>", thePlayer.getName())
					.replace("<DISPLAYNAME>", ChatColor.stripColor(thePlayer.getDisplayName())).toLowerCase();

			/* The in-game friendly Chat Trigger text to display if triggered. */
			String chatText = plugin.getScripts()
					.getString(theScript + ".Steps." + theStep + ".Chat Trigger." + String.valueOf(x + 1) + ".Trigger")
					.replace("/", "");

			boolean letsProceed = false;
			for (String chatTrigger : chatTriggers.split(":")) {
				if (playerMessage.toLowerCase().contains(chatTrigger)) letsProceed = true;
			}

			if (letsProceed) {

				/* Trigger matches, let's talk to the Denizen and send the script to the 
				 * triggerQue. No need to continue the loop. */
				plugin.getPlayer.talkToDenizen(theDenizen, thePlayer, chatText);

				List<String> chatScriptItems = plugin.getScripts().getStringList(theScript + ".Steps." + theStep + ".Chat Trigger." + String.valueOf(x + 1) + ".Script");
				for (String thisItem : chatScriptItems) {
					String[] scriptEntry = new String[2];
					scriptEntry = thisItem.split(" ", 2);
					try {
						/* Build new script commands */
						scriptCommands.add(new ScriptEntry(scriptEntry[0], buildArgs(scriptEntry[1]), thePlayer, theDenizen, theScript, theStep, playerMessage, chatText));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				/* New ScriptCommand list built, now let's add it into the queue */
				List<ScriptEntry> scriptCommandList = triggerQue.get(thePlayer);

				/* Keeps the commandQue from removing items while
				working on them here. They will be added back in. */ 
				triggerQue.remove(thePlayer); 

				scriptCommandList.addAll(scriptCommands);
				triggerQue.put(thePlayer, scriptCommandList);

				return true;
			}
		}

		/* If we have made it to this point, there were no matching triggers. */
		if (plugin.settings.ChatGloballyIfFailedChatTriggers()) return false;

		else {
			plugin.getPlayer.talkToDenizen(theDenizen, thePlayer, playerMessage);
			String noscriptChat = null;

			/* Checks the denizen for a custom message, else uses the default */
			if (plugin.getAssignments().contains("Denizens." + theDenizen.getName() + ".Texts.No Chat Triggers Met")) 
				noscriptChat = plugin.getAssignments().getString("Denizens." + theDenizen.getName()	+ ".Texts.No Chat Triggers Met");
			else noscriptChat = plugin.settings.DefaultNoChatTriggersMetText();

			plugin.getDenizen.talkToPlayer(theDenizen, thePlayer, plugin.getDenizen.formatChatText(noscriptChat, "CHAT", thePlayer, theDenizen)[0], null, "CHAT");
			return true;
		}
	}
	
	
	/* GetChatTriggers
	 *
	 * Requires the Script and the Current Step.
	 * Gets a list of Chat Triggers for the step of the script specified.
	 * Chat Triggers are words required to trigger one of the chat 
	 *
	 * Returns ChatTriggers
	 */

	public List<String> getChatTriggers(String theScript, Integer currentStep) {

		List<String> ChatTriggers = new ArrayList<String>();
		int currentTrigger = 1;
		for (int x=1; currentTrigger >= 0; x++) {
			String theChatTrigger = plugin.getScripts().getString(theScript + ".Steps."
					+ currentStep + ".Chat Trigger." + String.valueOf(currentTrigger) + ".Trigger");
			if (theChatTrigger != null) { 
				boolean isTrigger = false;
				String triggerBuilder = "";

				for (String trigger : theChatTrigger.split("/")) {
					if (isTrigger) {
						triggerBuilder = triggerBuilder + trigger + ":";
						isTrigger = false;
					}
					else isTrigger = true;
				}

				/* Take off excess ":" before adding it to the list */
				triggerBuilder = triggerBuilder.substring(0, triggerBuilder.length() - 1);

				if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "Found chat trigger: " + triggerBuilder);

				ChatTriggers.add(triggerBuilder);

				currentTrigger = x + 1; 
			}
			else currentTrigger = -1;
		}

		return ChatTriggers;
	}

	
}
