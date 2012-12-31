package net.aufdemrand.denizen.scripts.triggers.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
  
  private Boolean isKeywordRegex (String keyWord) {
  	return keyWord.toUpperCase().startsWith("REGEX:");
  }
  
	@EventHandler
	public void chatTrigger(AsyncPlayerChatEvent event) {
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
		
		//
		// If engaged or not cool, calls On Unavailable, if cool, calls On Click
		// If available (not engaged, and cool) sets cool down and returns true.
		//
		if (!closestNPC.getTrait(TriggerTrait.class).trigger(this, event.getPlayer())) {
			dB.echoDebug ("  The NPC is currently unavailable.");
			return;
		}

		//
		// Get the denizen that is associated to this NPC and see if it has a chat
		// trigger.
		//
		DenizenNPC denizenNPC = denizen.getNPCRegistry().getDenizen(closestNPC);
		dB.echoDebug("Found nearby NPC, interrupting chat...", this.name);

		//
		// If Denizen is not interactable (ie. Denizen is toggled off, engaged or
		// not cooled down)
		//
		if (!this.isInteractable(denizenNPC, event.getPlayer())) {
			if (!this.chatGloballyIfNotInteractable ()) {
				event.setCancelled(true);
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
		String theScript = denizenNPC.getInteractScript(event.getPlayer(), this.getClass());

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

				dB.echoDebug (ChatColor.LIGHT_PURPLE
							+ "+- Parsing QUICK CHAT script: " + denizenNPC.getName() + "/"
							+ event.getPlayer().getName() + " -+");

				/* Get the contents of the Script. */
				List<String> theScript1 = denizen.getAssignments().getStringList(
						"Denizens." + denizenNPC.getName()
								+ ".Quick Scripts.Chat Trigger.Script");

				if (theScript1.isEmpty()) {
					return;
				}

				//
				// Build the script entries and add them all to the player's task
				// queue.
				//
				ScriptEngine 	sE = denizen.getScriptEngine();
				sE.getPlayerQueue (
					event.getPlayer(), 
					QueueType.PLAYER_TASK)
					.addAll(
						sE.getScriptBuilder ().buildScriptEntries(
							event.getPlayer(), 
							denizenNPC, 
							theScript1, 
							denizenNPC.getName() + " Quick Chat", "1"));

				return;
			}

			if (!this.chatGloballyIfNoChatTriggers ()) {
				event.setCancelled(true);
				return;
			}

			dB.echoDebug("No script, resuming chat...", this.name);
			return;
		}

		//
		// Parse the script and match Triggers.. if found, cancel the text! The
		// parser will take care of everything else.
		//
		// List<MetadataValue>	metaData = event.getPlayer().getMetadata("denizen.chatmessage");
		// metaData.add(new FixedMetadataValue(denizen, event.getMessage()));
		this.playerMessage = event.getMessage ();
		
		if (this.parse (denizenNPC, event.getPlayer(), theScript)) {
			event.setCancelled(true);
			return;
		} else {
			dB.echoDebug(ChatColor.LIGHT_PURPLE + "| " + ChatColor.YELLOW
						+ "INFO! " + ChatColor.WHITE + "No matching chat trigger.");
			dB.echoDebug(ChatColor.LIGHT_PURPLE + "+---------------------+");

			if (!this.ChatGloballyIfFailedChatTriggers ()) {
				event.setCancelled(true);
				return;
			}

			// No matching chat triggers, and the config.yml says we should just 
			// ignore the interaction...
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
		Boolean	foundTrigger = false;
		ScriptEngine	sE = denizen.getScriptEngine();
		ScriptHelper	sH = sE.getScriptHelper();

		dB.echoDebug(ChatColor.LIGHT_PURPLE + "+- Parsing chat trigger: "
					+ theDenizen.getName() + "/" + thePlayer.getName() + " -+");
		dB.echoDebug(ChatColor.LIGHT_PURPLE + "| " + ChatColor.WHITE
					+ "Getting current step:");
		dB.echoDebug (ChatColor.LIGHT_PURPLE + "| Script Name:" + ChatColor.WHITE + theScriptName);

		//
		// Get Player's current step.
		//
		String	theStep = sH.getCurrentStep(thePlayer, theScriptName);
		
		//
		// Figure out if any of the triggers fired.
		//
		Map<String,List<String>> triggerMap = this.getChatTriggers(theScriptName, theStep);
		for (String triggerStep : triggerMap.keySet()) {
			dB.echoDebug ("Checking step: " + triggerStep);

			//
			// Iterate over the keywords that can trigger this step and see if all of
			// them match what the user typed.  All of the keywords must match in
			// order for the trigger to fire.
			//
			Boolean	foundMatch = true;
			for (String keyWord : triggerMap.get(triggerStep)) {
				dB.echoDebug(ChatColor.LIGHT_PURPLE + "Checking: " + keyWord);

				//
				// Is this a REGEX keyword?  If so, and it doesn't match what the user
				// entered, we can stop looking.
				//
				if (this.isKeywordRegex(keyWord)) {
					dB.echoDebug ("REGEX");
					Pattern	pattern = Pattern.compile(keyWord.substring(6));
					if (pattern.matcher(playerMessage).find () ) {
						continue;
					}
					dB.echoDebug (ChatColor.GOLD + "  " + playerMessage + " does not match regex: " + keyWord.substring(6) + ".");
					foundMatch = false;
					break;
				}

				//
				// This is a normal keyword.  If the player's message doesn't match the
				// keyword, then stop looking.
				//
				if (playerMessage.toLowerCase().contains(keyWord.toLowerCase()) == false) {
					dB.echoDebug (ChatColor.GOLD + "  \"" + playerMessage + "\" does not match.");
					foundMatch = false;
					break;
				}
			}

			//
			// Did we find a match?
			//
			if (foundMatch) {
				//
				// Found a match to the keyword.  Now get the script that needs to be
				// executed by using the triggerStep that we're on.
				//
				dB.echoDebug(ChatColor.GREEN + "  found match.");
				foundTrigger = true;
				List<String> theScript = sE.getScriptHelper().getScriptContents (triggerStep + sE.getScriptHelper().scriptKey);
				if (theScript == null || theScript.isEmpty()) {
					dB.echoDebug ("    No script found for: " + triggerStep + sE.getScriptHelper().scriptKey);
					continue;
				}
				
				//
				// Queue the script in the player's queue.
				//
				sB.queueScriptEntries (
					thePlayer, 
					sB.buildScriptEntries (
						thePlayer, 
						theDenizen, 
						theScript, 
						theScriptName, 
						theStep), 
					QueueType.PLAYER);
			}
		}

		return foundTrigger;
	}

	/**
	 * This method will return all of the steps of a script that have chat
	 * triggers associated to them.  This only returns those steps that have
	 * associated 'Chat Trigger' sections.
	 * 
	 * @param theScript	The script being processed.
	 * @param currentStep	The current step.
	 * 
	 * @return	This will return a map of script paths to the chat triggers that
	 * 					cause the step to process.
	 */
	public Map<String,List<String>> getChatTriggers(String theScript, String currentStep) {
		//
		// This is the REGEX for extracting the "key words" from a trigger.
		// Keywords are denoted by surround them with forward slahes, such as:
		//
		//		/Yes/ I'll help.
		//
		Pattern	triggerPattern = Pattern.compile ("\\/([^/]*)\\/");

		//
		// This is the path to the "Chat Trigger" we're processing.
		//
		String	path = (theScript + ".Steps." + currentStep + ".Chat Trigger").toUpperCase();
		
		//
		// This is the map of the script keys to the keywords that can trigger the
		// step.
		//
		Map<String,List<String>> triggerMap = new HashMap<String,List<String>> ();

		//
		// Iterate over all of this step's keys looking for chat triggers.
		//
		for (String key : denizen.getScripts().getConfigurationSection(path).getKeys(false)) {
			//
			// Build the key to the trigger and attempt to get it for the step that
			// we're currently processing.
			//
			String	stepKey = (path + "." + key + ".Trigger").toUpperCase();
			String	triggerValue = denizen.getScripts ().getString (stepKey);
			dB.echoDebug("stepKey: " + stepKey);
			dB.echoDebug("  triggerValue: " + triggerValue);
			
			//
			// Did we find a trigger for the current step that we're on and does this
			// trigger contain a "/" character (which is used for designating the key
			// text that causes the chat trigger to fire)?
			//
			if (triggerValue != null && triggerValue.contains("/")) {
				List<String>	keyWords = new ArrayList<String> ();
				//
				// Now find all of the keywords in the trigger.  Make sure to strip off
				// the slashes when building the list of key words.
				//
				Matcher matcher = triggerPattern.matcher(triggerValue);
				while (matcher.find ()) {
					String keyWord = matcher.group ();
					keyWords.add(keyWord.substring(1, keyWord.length() - 1));
				}
				
				triggerMap.put(path + "." + key, keyWords);
			}
		}
		
		return triggerMap;
	}
}