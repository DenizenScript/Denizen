package net.aufdemrand.denizen;

import java.util.*;
import java.util.logging.Logger;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

import net.aufdemrand.denizen.Denizen;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;


public class ScriptEngine {

	public enum Trigger {
		CHAT, CLICK, PROXIMITY, FAIL, FINISH
	}



	/*
	 * commandQue
	 * 
	 * Performs Denizen commands from each Player's Queue.
	 * 
	 */

	public void commandQue() {

		boolean instantCommand = false;
		if (!Denizen.playerQue.isEmpty()) {	

			for (Map.Entry<Player, List<String>> theEntry : Denizen.playerQue.entrySet()) {
				if (!theEntry.getValue().isEmpty()) {
				if (Long.valueOf(theEntry.getValue().get(0).split(";")[3]) < System.currentTimeMillis()) {
				do { Denizen.commandExecuter.execute(theEntry.getKey(), theEntry.getValue().get(0));
					instantCommand = false;
					if (theEntry.getValue().get(0).split(";")[4].startsWith("^")) instantCommand = true;
					theEntry.getValue().remove(0);
					Denizen.playerQue.put(theEntry.getKey(), theEntry.getValue());
				} while (instantCommand == true);
				}
				}
			}
		}
	}



	/*
	 * scheduleScripts
	 * 
	 * Schedules activity scripts to Denizens based on their schedule defined in the config.
	 * Runs every Minecraft hour. 
	 * 
	 */

	public void scheduleScripts() {

		Collection<NPC> DenizenNPCs = CitizensAPI.getNPCRegistry().getNPCs(DenizenCharacter.class);
		if (DenizenNPCs.isEmpty()) return;
		List<NPC> DenizenList = new ArrayList<NPC>(DenizenNPCs);
		for (NPC aDenizen : DenizenList) {
			if (aDenizen.isSpawned())	{
				int denizenTime = Math.round(aDenizen.getBukkitEntity().getWorld().getTime() / 1000);
				Denizen plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");		
				List<String> denizenActivities = plugin.getAssignments().getStringList("Denizens." + aDenizen.getName() + ".Scheduled Activities");
				if (!denizenActivities.isEmpty()) {
					for (String activity : denizenActivities) {
						if (activity.startsWith(String.valueOf(denizenTime))) {
							// plugin.getServer().broadcastMessage("Updating Activity Script for " + aDenizen.getName());
							plugin.getAssignments().set("Denizens." + aDenizen.getName() + ".Active Activity Script", activity.split(" ", 2)[1]);
							plugin.saveAssignments();
						}
					}
				}
			}
		}
	}



	/* ParseScript
	 *
	 * Requires the Player, the Script Name, the chat message (if Chat Trigger, otherwise send null),
	 * and the Trigger ENUM type. Sends out methods that take action based on the Trigger types.
	 *
	 */

	public boolean parseScript(NPC theDenizen, Player thePlayer, String theScript, String theMessage,  Trigger theTrigger) {
		Denizen plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");		
		int theStep = Denizen.getScript.getCurrentStep(thePlayer, theScript);

		switch (theTrigger) {

		case CHAT:

			/* 
			 * Get Chat Triggers and check each to see if there are any matches. 
			 */
			List<String> ChatTriggerList = Denizen.getScript.getChatTriggers(theScript, theStep);
			for (int x=0; x < ChatTriggerList.size(); x++ ) {

				/* 
				 * The text required to trigger.
				 */
				String chatTrigger = ChatTriggerList.get(x)
						.replace("<PLAYER>", thePlayer.getName()).toLowerCase();
				/* 
				 * The in-game friendly Chat Trigger text to display if triggered. 
				 */
				String chatText = plugin.getScripts()
						.getString(theScript + ".Steps." + theStep + ".Chat Trigger." + String.valueOf(x + 1) + ".Trigger")
						.replace("/", "");

				if (theMessage.toLowerCase().contains(chatTrigger)) {
					/* 
					 * Trigger matches, let's talk to the Denizen and send the script to the PlayerQueue. 
					 */
					Denizen.getPlayer.talkToDenizen(theDenizen, thePlayer, chatText);
					triggerToQue(theScript, theStep, thePlayer, theDenizen,
							plugin.getScripts().getStringList(theScript + ".Steps." + theStep + ".Chat Trigger." + String.valueOf(x + 1) + ".Script"));
					return true;
				}
			}

			/* 
			 * No matching triggers. 
			 */

			if(Denizen.settings.ChatGloballyIfFailedChatTriggers()) return false;
			else {
				Denizen.getPlayer.talkToDenizen(theDenizen, thePlayer, theMessage);

				String noscriptChat = null;

				if (plugin.getAssignments().contains("Denizens." + theDenizen.getName() 
						+ ".Texts.No Requirements Met")) 
					noscriptChat = plugin.getAssignments().getString("Denizens." + theDenizen.getName() 
							+ ".Texts.No Requirements Met");
				else
					noscriptChat = Denizen.settings.DefaultNoRequirementsMetText();

				Denizen.getDenizen.talkToPlayer(theDenizen, thePlayer, noscriptChat, "CHAT");
				return true;
			}

		case CLICK:
			triggerToQue(theScript, theStep, thePlayer, theDenizen, 
					plugin.getScripts().getStringList("" + theScript + ".Steps." + theStep + ".Click Trigger.Script"));
			return true;

		case FINISH:
			break;

		case FAIL:
			break;

		case PROXIMITY:
			break;
		}

		return false;
	}



	/* 
	 * triggerToQue
	 *
	 * Calls ScriptHandler to handle the commands in the script. ScriptHandler returns any
	 * raw text that needs to be sent to the player which is put in the PlayerQue for
	 * output.
	 *
	 */

	public void triggerToQue(String theScript, int CurrentStep, Player thePlayer, NPC theDenizen, List<String> addedToPlayerQue) {

		List<String> currentPlayerQue = new ArrayList<String>();
		if (Denizen.playerQue.get(thePlayer) != null) currentPlayerQue = Denizen.playerQue.get(thePlayer);
		
		if (!addedToPlayerQue.isEmpty()) {

			/* 
			 * Temporarily take away the playerQue for the Player to make sure nothing gets
			 * removed while working with it.
			 */
			Denizen.playerQue.remove(thePlayer);
			
			for (String theCommand : addedToPlayerQue) {
				/* PlayerQue format: DENIZEN ID; THE SCRIPT NAME; THE STEP; SYSTEM TIME; THE COMMAND */
				currentPlayerQue.add(Integer.toString(theDenizen.getId()) + ";" + theScript + ";" + Integer.toString(CurrentStep) + ";" + String.valueOf(System.currentTimeMillis()) + ";" + theCommand);	
			}

			Denizen.playerQue.put(thePlayer, currentPlayerQue);
		}
	}

	
	public void injectToQue(String theScript, int CurrentStep, Player thePlayer, NPC theDenizen, List<String> addedToPlayerQue) {

		List<String> currentPlayerQue = new ArrayList<String>();
		List<String> injectToPlayerQue = new ArrayList<String>();
		if (Denizen.playerQue.get(thePlayer) != null) currentPlayerQue = Denizen.playerQue.get(thePlayer);
		
		if (!addedToPlayerQue.isEmpty()) {

			/* 
			 * Temporarily take away the playerQue for the Player to make sure nothing gets
			 * removed while working with it.
			 */
			Denizen.playerQue.remove(thePlayer);

			for (String theCommand : addedToPlayerQue) {
				/* PlayerQue format: DENIZEN ID; THE SCRIPT NAME; THE STEP; SYSTEM TIME; THE COMMAND */
				injectToPlayerQue.add(Integer.toString(theDenizen.getId()) + ";" + theScript + ";" + Integer.toString(CurrentStep) + ";" + String.valueOf(System.currentTimeMillis()) + ";" + theCommand);	
			}
			
			currentPlayerQue.addAll(1, injectToPlayerQue);
			Denizen.playerQue.put(thePlayer, currentPlayerQue);
		}
	}



	public List<String> getMultilineText (String theText) {

		String[] text = theText.split(" ");
		List<String> processedText = new ArrayList<String>();

		if (theText.length() > Denizen.settings.MultiLineTextMaximumLength()) {

			processedText.add(0, "");
			
			int word = 1; int line = 0;

			while (word < text.length) {
				if (processedText.get(line).length() + text[word].length() < Denizen.settings.MultiLineTextMaximumLength()) {
					processedText.set(line, processedText.get(line) + " " + text[word]);
					word++;
				}
				else { line++; processedText.add(""); }
			}
		}
		
		else processedText.add(0, theText);

		return processedText;
	}







}