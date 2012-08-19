package net.aufdemrand.denizen.triggers.core;

import java.util.ArrayList;
import java.util.List;

import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.npc.SpeechEngine.Reason;
import net.aufdemrand.denizen.npc.SpeechEngine.TalkType;
import net.aufdemrand.denizen.scripts.ScriptHelper;
import net.aufdemrand.denizen.scripts.ScriptEngine.QueueType;
import net.aufdemrand.denizen.triggers.AbstractTrigger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;

public class ChatTrigger extends AbstractTrigger implements Listener {

	/* Listens for player chat and determines if player is near a Denizen, and if so,
	 * checks if there are scripts to interact with. */

	@EventHandler
	public void chatTrigger(PlayerChatEvent event) {


		// Check for denizen in range.
		DenizenNPC theDenizen = plugin.getDenizenNPCRegistry().getClosest(event.getPlayer(), 
				plugin.settings.PlayerToNpcChatRangeInBlocks());

		// If none, chat as normal.
		if (theDenizen == null)
			return;

		// Check if trigger is enabled for this Denizen.
		if (!theDenizen.hasTrigger(triggerName))
			return;

		echoDebug("Found nearby NPC, interrupting chat...", triggerName);

		// If Denizen is not interactable (ie. Denizen is toggled off, engaged or not cooled down)
		if (!theDenizen.IsInteractable(triggerName, event.getPlayer())) {
			if (!plugin.settings.ChatGloballyIfNotInteractable()) {
				event.setCancelled(true);
				plugin.getSpeechEngine().talkToDenizen(theDenizen, event.getPlayer(), event.getMessage());
				theDenizen.talk(TalkType.CHAT_PLAYERONLY, event.getPlayer(), Reason.DenizenIsUnavailable);
				return;
			}
			// Denizen isn't interactable, and the config.yml specifies that we 
			// should just treat chat as normal.
			echoDebug("Not interactable, resuming chat...", triggerName);
			return;
		}

		// Denizen should be good to interact with. Let's get the script.
		String theScript = theDenizen.getInteractScript(event.getPlayer(), this.getClass());

		/* No script matches, should we still show the player talking to the Denizen? */
		if (theScript == null) {
			if (!plugin.settings.ChatGloballyIfNoChatTriggers()) { 
				event.setCancelled(true);
				plugin.getSpeechEngine().talkToDenizen(theDenizen, event.getPlayer(), event.getMessage());
				theDenizen.talk(TalkType.CHAT, event.getPlayer(), Reason.NoMatchingChatTriggers);
				return;
			}
			// Denizen doesn't have a script, and the config.yml specifies that
			// we should just treat chat normal.
			echoDebug("No script, resuming chat...", triggerName);
			return;
		}

		/* Okay! We have a script, awesome! Let's parse the script to see if chat triggers match */

		// Parse the script and match Triggers.. if found, cancel the text! The parser will
		// take care of everything else.
		if (parseChatScript(theDenizen, event.getPlayer(), theScript, event.getMessage())) {
			event.setCancelled(true);
			return;
		} 

		// No matching chat Triggers... handle according to 
		else { 

			CommandSender cs = Bukkit.getConsoleSender();
			if (plugin.debugMode) cs.sendMessage(ChatColor.LIGHT_PURPLE + "| " + ChatColor.YELLOW + "INFO! " + ChatColor.WHITE + "No matching chat trigger.");
			if (plugin.debugMode) cs.sendMessage(ChatColor.LIGHT_PURPLE + "+---------------------+");

			if (!plugin.settings.ChatGloballyIfFailedChatTriggers()) {
				plugin.getSpeechEngine().talkToDenizen(theDenizen, event.getPlayer(), event.getMessage());
				theDenizen.talk(TalkType.CHAT, event.getPlayer(), Reason.NoMatchingChatTriggers);
				event.setCancelled(true);
				return;
			}
			// No matching chat triggers, and the config.yml 
			// says we should just ignore the interaction...
			echoDebug("No matching triggers in script, resuming chat...", triggerName);
			return;
		}
	}




	/* Parses the scripts for Chat Triggers and sends new ScriptCommands to the queue if
	 * found matched. Returning FALSE will cancel intervention and allow the PlayerChatEvent
	 * to pass through.	 
	 */

	public boolean parseChatScript(DenizenNPC theDenizen, Player thePlayer, String theScriptName, String playerMessage) {

		ScriptHelper sE = plugin.getScriptEngine().helper;
		CommandSender cs = Bukkit.getConsoleSender();

		if (plugin.debugMode) cs.sendMessage(ChatColor.LIGHT_PURPLE + "+- Parsing chat trigger: " + theDenizen.getName() + "/" + thePlayer.getName() + " -+");
		if (plugin.debugMode) cs.sendMessage(ChatColor.LIGHT_PURPLE + "| " + ChatColor.WHITE + "Getting current step:");

		/* Get Player's current step */
		Integer theStep = sE.getCurrentStep(thePlayer, theScriptName);

		/* Get Chat Triggers and check each to see if there are any matches. */
		List<String> ChatTriggerList = getChatTriggers(theScriptName, theStep);
		for (int x = 0; x < ChatTriggerList.size(); x++ ) {

			// The texts required to trigger.
			String chatTriggers = ChatTriggerList.get(x)
					.replace("<PLAYER>", thePlayer.getName())
					.replace("<DISPLAYNAME>", ChatColor.stripColor(thePlayer.getDisplayName())).toLowerCase();

			// The in-game friendly Chat Trigger text to display if triggered.
			String chatText = plugin.getScripts()
					.getString(theScriptName + ".Steps." + theStep + ".Chat Trigger." + String.valueOf(x + 1) + ".Trigger")
					.replace("/", "");

			// Find a matching trigger
			boolean letsProceed = false;
			for (String chatTrigger : chatTriggers.split(":")) {
				if (playerMessage.toLowerCase().contains(chatTrigger)) letsProceed = true;
				if (chatTrigger.contains("*")) {
					chatText = chatText.replace("*", playerMessage);
					letsProceed = true;
				}

			}

			// If a matching trigger is found...
			if (letsProceed) {
				/* Trigger matches, let's talk to the Denizen and send the script to the 
				 * triggerQue. No need to continue the loop. */
				List<String> theScript = sE.getScript(sE.getTriggerPath(theScriptName, theStep, triggerName) + String.valueOf(x + 1) + sE.scriptString);
				if (theScript.isEmpty()) return false;

				// Chat to the Denizen, then queue the scrip entries!
				plugin.getSpeechEngine().talkToDenizen(theDenizen, thePlayer, chatText);
				sE.queueScriptEntries(thePlayer, sE.buildScriptEntries(thePlayer, theDenizen, theScript, theScriptName, theStep, playerMessage, chatText), QueueType.TRIGGER);
				return true;
			}
		}

		// Else, no matching trigger found...
		return false;
	}


	/* 
	 * GetChatTriggers
	 *
	 * Gets a list of Chat Triggers for the step of the script specified.
	 * Chat Triggers are words required to trigger one of the chat.
	 * 
	 */

	public List<String> getChatTriggers(String theScript, Integer currentStep) {

		// TODO: Cleanup, this seems kind of ghetto.

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

				CommandSender cs = Bukkit.getConsoleSender();
				if (plugin.debugMode) cs.sendMessage(ChatColor.LIGHT_PURPLE + "| " + ChatColor.WHITE + "Found chat trigger: " + triggerBuilder);
				ChatTriggers.add(triggerBuilder);
				currentTrigger = x + 1; 
			}
			else currentTrigger = -1;
		}

		return ChatTriggers;
	}


}
