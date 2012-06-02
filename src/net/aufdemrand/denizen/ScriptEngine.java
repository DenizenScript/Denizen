package net.aufdemrand.denizen;

import java.util.*;

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
			do {
				Denizen.commandExecuter.execute(theEntry.getKey(), theEntry.getValue().get(0));
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
	 * Runs every Minecraft half-hour. 
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
			List<String> denizenActivities = plugin.getConfig().getStringList("Denizens." + aDenizen.getName() + ".Scheduled Activities");
			if (!denizenActivities.isEmpty()) {
				for (String activity : denizenActivities) {
				if (activity.startsWith(String.valueOf(denizenTime))) {
					// plugin.getServer().broadcastMessage("Updating Activity Script for " + aDenizen.getName());
					plugin.getConfig().set("Denizens." + aDenizen.getName() + ".Active Activity Script", activity.split(" ", 2)[1]);
					plugin.saveConfig();
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

	public void parseScript(NPC theDenizen, Player thePlayer, String theScript, String theMessage,  Trigger theTrigger) {
		Denizen plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");		
		int CurrentStep = Denizen.getScript.getCurrentStep(thePlayer, theScript);

		switch (theTrigger) {
		case CHAT:
			List<String> ChatTriggerList = Denizen.getScript.getChatTriggers(theScript, CurrentStep);
			for (int l=0; l < ChatTriggerList.size(); l++ ) {
				if (theMessage.toLowerCase().contains(ChatTriggerList.get(l).replace("<PLAYER>", thePlayer.getName()).toLowerCase())) {

					Denizen.getPlayer.talkToDenizen(theDenizen, thePlayer, plugin.getScripts().getString("" + theScript + ".Steps."
							+ CurrentStep + ".Chat Trigger." + String.valueOf(l + 1) + ".Trigger").replace("/", ""));

					triggerToQue(theScript, plugin.getScripts().getStringList("" + theScript + ".Steps."
							+ CurrentStep + ".Chat Trigger." + String.valueOf(l + 1) + ".Script"), CurrentStep, thePlayer, theDenizen);
					return;
				}
			}
			Denizen.getPlayer.talkToDenizen(theDenizen, thePlayer, theMessage);

			if(plugin.getConfig().getBoolean("chat_globably_if_no_chat_triggers", false)) return;
			
			List<String> CurrentPlayerQue = new ArrayList<String>();
			if (Denizen.playerQue.get(thePlayer) != null) CurrentPlayerQue = Denizen.playerQue.get(thePlayer);
			Denizen.playerQue.remove(thePlayer);  // Should keep the talk queue from triggering mid-add

			CurrentPlayerQue.add(Integer.toString(theDenizen.getId()) + ";" + theScript + ";"
					+ 0 + ";" + String.valueOf(System.currentTimeMillis()) + ";" + "CHAT " + plugin.getConfig().getString("Denizens." + theDenizen.getId() 
							+ ".Texts.No Script Interact", "I have nothing to say to you at this time."));

			Denizen.playerQue.put(thePlayer, CurrentPlayerQue);
			return;

		case CLICK:
			triggerToQue(theScript, plugin.getScripts().getStringList("" + theScript + ".Steps."
					+ CurrentStep + ".Click Trigger.Script"), CurrentStep, thePlayer, theDenizen);
			return;

		case FINISH:


		}
	}

	

	/* 
	 * triggerToQue
	 *
	 * Calls ScriptHandler to handle the commands in the script. ScriptHandler returns any
	 * raw text that needs to be sent to the player which is put in the PlayerQue for
	 * output.
 	 *
	 */

	public void triggerToQue(String theScript, List<String> AddedToPlayerQue, int CurrentStep, Player thePlayer, NPC theDenizen) {

		List<String> CurrentPlayerQue = new ArrayList<String>();
		if (Denizen.playerQue.get(thePlayer) != null) CurrentPlayerQue = Denizen.playerQue.get(thePlayer);
		Denizen.playerQue.remove(thePlayer);  // Should keep the talk queue from triggering mid-add

		if (!AddedToPlayerQue.isEmpty()) {

			for (String theCommand : AddedToPlayerQue) {

				String[] theCommandText;
				theCommandText = theCommand.split(" ");

				// Longer than 40, probably a long chat that needs multiline formatting.
				if (theCommand.length() > 40) {

					switch (CommandExecuter.Command.valueOf(theCommandText[0].toUpperCase())) {
					case SHOUT:	case CHAT: case WHISPER: case ANNOUNCE:	case NARRATE:
						int word = 1; int line = 0;
						ArrayList<String> multiLineCommand = new ArrayList<String>();
						multiLineCommand.add(theCommandText[0]);
						while (word < theCommandText.length) {
							if (line==0) {
								if (multiLineCommand.get(line).length() + theCommandText[word].length() + theDenizen.getName().length() < 48) {
									multiLineCommand.set(line, multiLineCommand.get(line) + " " + theCommandText[word]);
									word++;
								}
								else { line++; multiLineCommand.add(theCommandText[0] + " *"); }
							}
							else {
								if (multiLineCommand.get(line).length() + theCommandText[word].length() < 58) {
									multiLineCommand.set(line, multiLineCommand.get(line) + " " + theCommandText[word]);
									word++;
								}
								else { line++; multiLineCommand.add(theCommandText[0] + " *"); }
							}
						}
						for (String eachCommand : multiLineCommand) {
							CurrentPlayerQue.add(Integer.toString(theDenizen.getId()) + ";" + theScript + ";" + Integer.toString(CurrentStep) + ";" + String.valueOf(System.currentTimeMillis()) + ";" + eachCommand);
						}
					}
				}

				else CurrentPlayerQue.add(Integer.toString(theDenizen.getId()) + ";" + theScript + ";" + Integer.toString(CurrentStep) + ";" + String.valueOf(System.currentTimeMillis()) + ";" + theCommand);	
			}
			Denizen.playerQue.put(thePlayer, CurrentPlayerQue);
		}
	}






}