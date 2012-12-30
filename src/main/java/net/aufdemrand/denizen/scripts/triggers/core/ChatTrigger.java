package net.aufdemrand.denizen.scripts.triggers.core;

import java.util.ArrayList;
import java.util.List;

import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.npc.traits.TriggerTrait;
import net.aufdemrand.denizen.scripts.ScriptEngine;
import net.aufdemrand.denizen.scripts.ScriptEngine.QueueType;
import net.aufdemrand.denizen.scripts.ScriptHelper;
import net.aufdemrand.denizen.scripts.triggers.AbstractTrigger;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

public class ChatTrigger extends AbstractTrigger implements Listener {
	private	String	playerMessage;
	
  @Override
  public void onEnable() {
      denizen.getServer().getPluginManager().registerEvents(this, denizen);
  }
  
  private	Boolean ChatGloballyIfFailedChatTriggers () {
  	// denizen.settings.ChatGloballyIfFailedChatTriggers
  	return true;
  }
  
  private Boolean chatGloballyIfNotInteractable () {
  	// denizen.settings.ChatGloballyIfNotInteractable
  	return true;
  }
  
  private Boolean chatGloballyIfNoChatTriggers () {
  	// denizen.settings.ChatGloballyIfNoChatTriggers
  	return true;
  }
  
  private Boolean isInteractable (DenizenNPC npc, Player player) {
  	return true;
  }

	@EventHandler
	public void chatTrigger(AsyncPlayerChatEvent event) {
		dB.echoDebug("chatTrigger (" + event.toString () + ")");

		//
		// Try to find the closest NPC to the player's location.
		//
		NPC	closestNPC = Utilities.getClosestNPC(event.getPlayer().getLocation(), 3);
		if (closestNPC == null) {
			return;
		}

		//
		// If the NPC doesn't have triggers, or the triggers are not enabled, then
		// just return.
		//
		dB.echoDebug ("  Closest NPC: " + closestNPC.getFullName());
		if (!closestNPC.hasTrait(TriggerTrait.class)) {
			dB.echoDebug("  NPC does not have the trigger trait: " + TriggerTrait.class);
			return;
		}
		
		if (!closestNPC.getTrait(TriggerTrait.class).isEnabled(name)) {
			dB.echoDebug("  Trigger " + this.name + " is not enabled.");
			return;
		}
		
		// If engaged or not cool, calls On Unavailable, if cool, calls On Click
		// If available (not engaged, and cool) sets cool down and returns true. 
		if (!closestNPC.getTrait(TriggerTrait.class).trigger(this, event.getPlayer())) {
//			return;
		}

		//
		// Get the denizen that is associated to this NPC and see if it has a chat
		// trigger.
		//
		DenizenNPC denizenNPC = denizen.getNPCRegistry().getDenizen(closestNPC);
		/*
		if (!denizenNPC.hasTrigger(triggerName)) {
			return;
		}
*/

		dB.echoDebug("Found nearby NPC, interrupting chat...", this.name);

		// If Denizen is not interactable (ie. Denizen is toggled off, engaged or
		// not cooled down)
		if (!this.isInteractable(denizenNPC, event.getPlayer())) {
			if (!this.chatGloballyIfNotInteractable ()) {
				event.setCancelled(true);

				denizen.getSpeechEngine().whisper(denizenNPC.getEntity(), event.getMessage(), event.getPlayer());
//				denizenNPC.talk(TalkType.CHAT_PLAYERONLY, event.getPlayer(), Reason.DenizenIsUnavailable);
				denizen.getSpeechEngine().whisper(denizenNPC.getEntity(), "Denizen is unavailable", event.getPlayer());
				
				return;
			}
			
			//
			// Denizen isn't interactable, and the config.yml specifies that we
			// should just treat chat as normal.
			//
			dB.echoDebug("Not interactable, resuming chat...", this.name);
			return;
		}

		// Denizen should be good to interact with. Let's get the script.
		String theScript = denizenNPC.getInteractScript(event.getPlayer(),
				this.getClass());

		/*
		 * No script matches, should we still show the player talking to the
		 * Denizen?
		 */
		if (theScript == null) {
			// Check for Quick Script
			if (denizen.getAssignments().contains(
					"Denizens." + denizenNPC.getName()
							+ ".Quick Scripts.Chat Trigger.Script")) {

				event.setCancelled(true);
				
				//
				// Whisper the message to the player.
				//
				denizen.getSpeechEngine().whisper (denizenNPC.getEntity(), event.getMessage(), event.getPlayer());

				dB.echoDebug (ChatColor.LIGHT_PURPLE
							+ "+- Parsing QUICK CHAT script: " + denizenNPC.getName() + "/"
							+ event.getPlayer().getName() + " -+");

				/* Get the contents of the Script. */
				List<String> theScript1 = denizen.getAssignments().getStringList(
						"Denizens." + denizenNPC.getName()
								+ ".Quick Scripts.Chat Trigger.Script");

				if (theScript1.isEmpty()) {
					denizen.getSpeechEngine().whisper(closestNPC.getBukkitEntity(), "No matching chat triggers.", event.getPlayer ());
//					denizenNPC.talk(TalkType.CHAT, event.getPlayer(), Reason.NoMatchingChatTriggers);
					return;
				}

				//
				// Build the script entries and add them all to the player's task
				// queue.
				//
				ScriptEngine 	sE = denizen.getScriptEngine();
				sE.getPlayerQueue(event.getPlayer(), QueueType.PLAYER_TASK).addAll(sE.getScriptBuilder ().buildScriptEntries(
						event.getPlayer(), denizenNPC, theScript1, denizenNPC.getName()
								+ " Quick Chat", "1"));

				return;
			}

			if (!this.chatGloballyIfNoChatTriggers ()) {
				event.setCancelled(true);
				denizen.getSpeechEngine().whisper (denizenNPC.getEntity(), event.getMessage(), event.getPlayer());
				denizen.getSpeechEngine().whisper (denizenNPC.getEntity(), "No matching chat triggers.", event.getPlayer());
//				denizenNPC.talk(TalkType.CHAT, event.getPlayer(), Reason.NoMatchingChatTriggers);
				return;
			}
			// Denizen doesn't have a script, and the config.yml specifies that
			// we should just treat chat normal.
			dB.echoDebug("No script, resuming chat...", this.name);
			return;
		}

		/*
		 * Okay! We have a script, awesome! Let's parse the script to see if chat
		 * triggers match
		 */

		//
		// Parse the script and match Triggers.. if found, cancel the text! The
		// parser will take care of everything else.
		//
		// List<MetadataValue>	metaData = event.getPlayer().getMetadata("denizen.chatmessage");
		// metaData.add(new FixedMetadataValue(denizen, event.getMessage()));
		this.playerMessage = event.getMessage ();
		
		if (this.parse (denizenNPC, event.getPlayer(), theScript /*, event.getMessage() */)) {
			event.setCancelled(true);
			return;
		} else {
			dB.echoDebug(ChatColor.LIGHT_PURPLE + "| " + ChatColor.YELLOW
						+ "INFO! " + ChatColor.WHITE + "No matching chat trigger.");
			dB.echoDebug(ChatColor.LIGHT_PURPLE + "+---------------------+");

			if (!this.ChatGloballyIfFailedChatTriggers ()) {
				denizen.getSpeechEngine().whisper (denizenNPC.getEntity(), event.getMessage(), event.getPlayer());

				denizen.getSpeechEngine().whisper (denizenNPC.getEntity(), "No matching chat triggers", event.getPlayer());
//				denizenNPC.talk(TalkType.CHAT, event.getPlayer(), Reason.NoMatchingChatTriggers);

				event.setCancelled(true);
				return;
			}

			// No matching chat triggers, and the config.yml
			// says we should just ignore the interaction...
			dB.echoDebug("No matching triggers in script, resuming chat...", this.name);
			return;
		}
	}

	/*
	 * Parses the scripts for Chat Triggers and sends new ScriptCommands to the
	 * queue if found matched. Returning FALSE will cancel intervention and allow
	 * the PlayerChatEvent to pass through.
	 */

	public boolean parse (DenizenNPC theDenizen, Player thePlayer, String theScriptName) {
		ScriptEngine	sE = denizen.getScriptEngine();
		ScriptHelper	sH = sE.getScriptHelper();

		dB.echoDebug(ChatColor.LIGHT_PURPLE + "+- Parsing chat trigger: "
					+ theDenizen.getName() + "/" + thePlayer.getName() + " -+");
		dB.echoDebug(ChatColor.LIGHT_PURPLE + "| " + ChatColor.WHITE
					+ "Getting current step:");
		dB.echoDebug (ChatColor.LIGHT_PURPLE + "| Script Name:" + ChatColor.WHITE + theScriptName);

		/* Get Player's current step */
		String	theStep = sH.getCurrentStep(thePlayer, theScriptName);

		/* Get Chat Triggers and check each to see if there are any matches. */
		List<String> ChatTriggerList = getChatTriggers(theScriptName, theStep);
		if (ChatTriggerList.size () == 0) {
			dB.echoDebug(ChatColor.RED + "There are no chat triggers defined.");
		}
		for (int x = 0; x < ChatTriggerList.size(); x++) {
			dB.echoDebug ("  Checking: " + ChatTriggerList.get (x));

			// The texts required to trigger.
			String chatTriggers = ChatTriggerList
					.get(x)
					.replace("<PLAYER>", thePlayer.getName())
					.replace("<DISPLAYNAME>",
							ChatColor.stripColor(thePlayer.getDisplayName())).toLowerCase();

			// The in-game friendly Chat Trigger text to display if triggered.
			String chatText = denizen
					.getScripts()
					.getString(
							theScriptName + ".Steps." + theStep + ".Chat Trigger."
									+ String.valueOf(x + 1) + ".Trigger").replace("/", "");

			// Find a matching trigger
			boolean letsProceed = false;
			for (String chatTrigger : chatTriggers.split(":")) {
				if (playerMessage.toLowerCase().contains(chatTrigger))
					letsProceed = true;

			}

			// If a matching trigger is found...
			if (letsProceed) {
				/*
				 * Trigger matches, let's talk to the Denizen and send the script to the
				 * triggerQue. No need to continue the loop if the script is empty.
				 */
				List<String> theScript = 
					sE.getScriptHelper().getScriptContents (
							sE.getScriptHelper().getTriggerScriptPath (
							theScriptName,
							theStep, 
							this.name) + String.valueOf(x + 1) + sE.getScriptHelper().scriptKey);

				if (theScript.isEmpty()) {
					return false;
				}

				// Chat to the Denizen, then queue the scrip entries!
				denizen.getSpeechEngine().whisper (theDenizen.getEntity(), chatText, thePlayer);

				sE.getPlayerQueue(thePlayer, QueueType.PLAYER)
					.addAll(sE.getScriptBuilder().buildScriptEntries (
						thePlayer,
						theDenizen, 
						theScript, 
						theScriptName, 
						theStep, 
						playerMessage, 
						chatText));

				return true;
			}
		}

		for (int x = 0; x < ChatTriggerList.size(); x++) {

			// The texts required to trigger.
			String chatTriggers = ChatTriggerList
					.get(x)
					.replace("<PLAYER>", thePlayer.getName())
					.replace("<DISPLAYNAME>",
							ChatColor.stripColor(thePlayer.getDisplayName())).toLowerCase();

			// The in-game friendly Chat Trigger text to display if triggered.
			String chatText = denizen
					.getScripts()
					.getString(
							theScriptName + ".Steps." + theStep + ".Chat Trigger."
									+ String.valueOf(x + 1) + ".Trigger").replace("/", "");

			//
			// Find a matching trigger.
			//
			boolean letsProceed = false;
			for (String chatTrigger : chatTriggers.split(":")) {
				if (chatTrigger.contains("*")) {
					chatText = chatText.replace("*", playerMessage);
					letsProceed = true;
				}
			}

			//
			// If a matching trigger is found...
			//
			if (letsProceed) {
				//
				// Trigger matches, let's talk to the Denizen and send the script to the
				// triggerQue. No need to continue the loop if the script is empty.
				//
				List<String> theScript = sE.getScriptHelper().getScriptContents(
					sE.getScriptHelper().getTriggerScriptPath (
						theScriptName,
						theStep, 
						this.name) 
						+ String.valueOf (x + 1) + sE.getScriptHelper().scriptKey);

				if (theScript.isEmpty())
					return false;

				// Chat to the Denizen, then queue the scrip entries!
				denizen.getSpeechEngine().whisper (theDenizen.getEntity(), chatText, thePlayer);

				sE.getPlayerQueue(thePlayer, QueueType.PLAYER)
					.addAll(sE.getScriptBuilder().buildScriptEntries (
						thePlayer,
						theDenizen, 
						theScript, 
						theScriptName, 
						theStep, 
						playerMessage, 
						chatText));

				return true;
			}
		}

		// Else, no matching trigger found...
		return false;
	}

	/**
	 * Gets a list of Chat Triggers for the step of the script specified. Chat
	 * Triggers are words required to trigger one of the chat.
	 * 
	 * @param theScript	The name of the script that is running.
	 * @param currentStep	The name of the step to get the chat triggers for.
	 * 
	 * @return	The list of chat triggers for the given script and step.
	 */
	public List<String> getChatTriggers(String theScript, String currentStep) {
		// TODO: Cleanup, this seems kind of ghetto?
		List<String> ChatTriggers = new ArrayList<String>();
		int currentTrigger = 1;
		for (int x = 1; currentTrigger >= 0; x++) {
			String	scriptKey =
				theScript + 
				".Steps." + 
				currentStep + 
				".Chat Trigger."	+
				String.valueOf(currentTrigger) + 
				".Trigger";
			
			String theChatTrigger = denizen.getScripts().getString(scriptKey);
			
			dB.echoDebug ("Getting chat trigger for: " + scriptKey);
			if (theChatTrigger != null) {
				boolean isTrigger = false;
				String triggerBuilder = "";

				for (String trigger : theChatTrigger.split("/")) {
					if (isTrigger) {
						triggerBuilder = triggerBuilder + trigger + ":";
						isTrigger = false;
					} else
						isTrigger = true;
				}

				//
				// Take off the excess colon before adding it to the list.
				//
				triggerBuilder = triggerBuilder.substring(0, triggerBuilder.length() - 1);

				dB.echoDebug (ChatColor.LIGHT_PURPLE + "| " + ChatColor.WHITE
						+ "Found chat trigger: " + triggerBuilder);
				ChatTriggers.add(triggerBuilder);
				currentTrigger = x + 1;
			} else {
				dB.echoDebug ("No chat triggers found.");
				dB.echoDebug (denizen.getScripts ().toString ());
				currentTrigger = -1;
			}
		}

		return ChatTriggers;
	}
}