package net.aufdemrand.denizen.scripts.triggers;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.npc.SpeechEngine.Reason;
import net.aufdemrand.denizen.npc.SpeechEngine.TalkType;
import net.aufdemrand.denizen.scripts.AbstractTrigger;
import net.aufdemrand.denizen.scripts.ScriptHelper;
import net.aufdemrand.denizen.scripts.ScriptEngine.QueueType;

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

		DenizenNPC theDenizen = plugin.getDenizenNPCRegistry().getClosest(event.getPlayer(), 
				plugin.settings.PlayerToNpcChatRangeInBlocks());
		
		if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "Found nearby NPC, interrupting chat...");

		/* If no Denizen in range, or the Denizen closest is engaged, return */
		if (theDenizen != null) {

			if (theDenizen.IsInteractable(triggerName, event.getPlayer())) {
				
				/* Get the script to use */
				String theScript = theDenizen.getInteractScript(event.getPlayer());

				/* No script matches, should we still show the player talking to the Denizen? */
				if (theScript == null && !plugin.settings.ChatGloballyIfNoChatTriggers()) { 
					event.setCancelled(true);
					theDenizen.talk(TalkType.CHAT, event.getPlayer(), Reason.NoMatchingChatTriggers);
				}

				/* Awesome! There's a matching script, let's parse the script to see if chat triggers match */
				if (theScript != null) {
					if (parseChatScript(theDenizen, event.getPlayer(), theScript, event.getMessage()))
						event.setCancelled(true);
					else 
						if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...resuming chat, no chat triggers found!");
				
				}
			}
			
			else {
				if (!plugin.settings.ChatGloballyIfNotInteractable())
				plugin.getSpeechEngine().talkToDenizen(theDenizen, event.getPlayer(), event.getMessage());
				theDenizen.talk(TalkType.CHAT, event.getPlayer(), Reason.DenizenIsUnavailable);
			}
		}
		
		if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...resuming chat, no interactable script found!");
		
	}




	/* Parses the scripts for Chat Triggers and sends new ScriptCommands to the queue if
	 * found matched. Returning FALSE will cancel intervention and allow the PlayerChatEvent
	 * to pass through.	 
	 */

	public boolean parseChatScript(DenizenNPC theDenizen, Player thePlayer, String theScriptName, String playerMessage) {

		ScriptHelper sE = plugin.getScriptEngine().helper;

		/* Get Player's current step */
		Integer theStep = sE.getCurrentStep(thePlayer, theScriptName);

		// List<ScriptEntry> scriptCommands = new ArrayList<ScriptEntry>();

		/* Get Chat Triggers and check each to see if there are any matches. */
		List<String> ChatTriggerList = getChatTriggers(theScriptName, theStep);
		for (int x = 0; x < ChatTriggerList.size(); x++ ) {

			/* The texts required to trigger. */
			String chatTriggers = ChatTriggerList.get(x)
					.replace("<PLAYER>", thePlayer.getName())
					.replace("<DISPLAYNAME>", ChatColor.stripColor(thePlayer.getDisplayName())).toLowerCase();

			/* The in-game friendly Chat Trigger text to display if triggered. */
			String chatText = plugin.getScripts()
					.getString(theScriptName + ".Steps." + theStep + ".Chat Trigger." + String.valueOf(x + 1) + ".Trigger")
					.replace("/", "");

			boolean letsProceed = false;
			for (String chatTrigger : chatTriggers.split(":")) {
				if (playerMessage.toLowerCase().contains(chatTrigger)) letsProceed = true;
			}

			if (letsProceed) {

				/* Trigger matches, let's talk to the Denizen and send the script to the 
				 * triggerQue. No need to continue the loop. */
				plugin.getSpeechEngine().talkToDenizen(theDenizen, thePlayer, chatText);

				List<String> theScript = sE.getScript(sE.getTriggerPath(theScriptName, theStep, triggerName) + String.valueOf(x + 1) + sE.scriptString);

				sE.queueScriptEntries(thePlayer, sE.buildScriptEntries(thePlayer, theDenizen, theScript, theScriptName, theStep, playerMessage, chatText), QueueType.TRIGGER);

				return true;
			}
		}

		/* If we have made it to this point, there were no matching triggers. */
		if (plugin.settings.ChatGloballyIfFailedChatTriggers()) return false;

		else {
			plugin.getSpeechEngine().talkToDenizen(theDenizen, thePlayer, playerMessage);
			theDenizen.talk(TalkType.CHAT, thePlayer, Reason.NoMatchingChatTriggers);
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
