package net.aufdemrand.denizen.scriptEngine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.DenizenCharacter;
import net.aufdemrand.denizen.commands.Executer.CommandType;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;


/**
 * Contains methods used to parse and execute scripts, 
 * initiated by some kind of event trigger or interaction.
 * 
 * @author Jeremy
 * 
 */

public class ScriptEngine {


	/* Denizen Constructor */

	private Denizen plugin;
	public ScriptEngine(Denizen denizen) {
		plugin = denizen;
	}	


	/* ENUMS to help with dealing with multiple types of Triggers/Queues */

	public enum TriggerType {
		ATTACK, CLICK, CHAT, PROXIMITY, TASK, LOCATION
	}

	public enum QueueType {
		TRIGGER, TASK, ACTIVITY, CUSTOM
	}

	private Map<Player, List<ScriptCommand>> triggerQue = new ConcurrentHashMap<Player, List<ScriptCommand>>();
	private Map<Player, List<ScriptCommand>>    taskQue = new ConcurrentHashMap<Player, List<ScriptCommand>>();
	private Map<NPC, List<ScriptCommand>>   activityQue = new ConcurrentHashMap<NPC, List<ScriptCommand>>();



	/* Processes commands from the Queues. */

	public void commandQueue() {

		/* First the triggerQue, primary script queue for Players */

		if (!taskQue.isEmpty()) {	

			/* Attempt to run a command for each player. The attempted command (and attached info) info is 
			 * in theEntry */
			for (Entry<Player, List<ScriptCommand>> theEntry : taskQue.entrySet()) {
				if (!theEntry.getValue().isEmpty()) {

					/* Check the time of the command to see if it has been delayed with a WAIT command. Only 
					 * proceed for the player if the time on the command is less than the current time. 
					 * If it's more, then this entry will be skipped and saved for next time. */
					if (theEntry.getValue().get(0).getDelayedTime() < System.currentTimeMillis()) {

						/* Feeds the executer ScriptCommands as long as they are instant commands ("^"), otherwise
						 * runs one command, removes it from the queue, and moves on to the next player. */
						do { 
							plugin.executer.execute(theEntry.getValue().get(0));
							theEntry.getValue().remove(0);

							/* Updates the triggerQue map */
							taskQue.put(theEntry.getKey(), theEntry.getValue());
						} while (theEntry.getValue().get(0).instant());
					}
				}
			}
			/* Next Player */
		}


		/* Now the taskQue, the alternate script queue for Players */

		if (!taskQue.isEmpty()) {	
			for (Entry<Player, List<ScriptCommand>> theEntry : taskQue.entrySet()) {
				if (!theEntry.getValue().isEmpty()) {
					if (theEntry.getValue().get(0).getDelayedTime() < System.currentTimeMillis()) {
						do { 
							plugin.executer.execute(theEntry.getValue().get(0));
							theEntry.getValue().remove(0);
							taskQue.put(theEntry.getKey(), theEntry.getValue());
						} while (theEntry.getValue().get(0).instant());
					}
				}
			}
			/* Next Player */
		}


		/* 
		 * TODO: activityQue
		 */

	}



	/* Schedules activity scripts to Denizens based on their schedule defined in the config.
	 * Runs every Minecraft hour. 
	 * 
	 * This will be the backbone to automated activity scripts. Currently this is not used
	 * any further than what's in this method, but will be build upon soon.	 */

	public void scheduleScripts() {

		Collection<NPC> DenizenNPCs = CitizensAPI.getNPCRegistry().getNPCs(DenizenCharacter.class);
		if (DenizenNPCs.isEmpty()) return;
		List<NPC> DenizenList = new ArrayList<NPC>(DenizenNPCs);
		for (NPC thisDenizen : DenizenList) {
			if (thisDenizen.isSpawned())	{
				int denizenTime = Math.round(thisDenizen.getBukkitEntity().getWorld().getTime() / 1000);
				List<String> denizenActivities = plugin.getAssignments().getStringList("Denizens." + thisDenizen.getName() + ".Scheduled Activities");
				if (!denizenActivities.isEmpty()) {
					for (String activity : denizenActivities) {
						if (activity.startsWith(String.valueOf(denizenTime))) {
							// plugin.getServer().broadcastMessage("Updating Activity Script for " + aDenizen.getName());
							plugin.getSaves().set("Denizens." + thisDenizen.getName() + ".Active Activity Script", activity.split(" ", 2)[1]);
							plugin.saveSaves();
						}
					}
				}
			}
		}
	}



	/* Parses the scripts for Chat Triggers and sends new ScriptCommands to the queue if
	 * found matched. Returning FALSE will cancel intervention and allow the PlayerChatEvent
	 * to pass through.	 */

	public boolean parseChatScript(NPC theDenizen, Player thePlayer, String theScript, String playerMessage) {

		int theStep = plugin.getScript.getCurrentStep(thePlayer, theScript);
		List<ScriptCommand> scriptCommands = new ArrayList<ScriptCommand>();

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
					String[] scriptEntry = thisItem.split(" ", 2);
					try {
						/* Build new script commands */
						scriptCommands.add(new ScriptCommand(scriptEntry[0], scriptEntry[1].split(" "), thePlayer, theDenizen, theScript, theStep, playerMessage, chatText));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				/* New ScriptCommand list built, now let's add it into the queue */
				List<ScriptCommand> scriptCommandList = taskQue.get(thePlayer);

				/* Keeps the commandQue from removing items while
				working on them here. They will be added back in. */ 
				taskQue.remove(thePlayer); 

				scriptCommandList.addAll(scriptCommands);
				taskQue.put(thePlayer, scriptCommandList);

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

			plugin.getDenizen.talkToPlayer(theDenizen, thePlayer, plugin.scriptEngine.formatChatText(noscriptChat, "CHAT", thePlayer, theDenizen)[0], null, "CHAT");
			return true;
		}
	}



	/* Parses the script for a click trigger */

	public boolean parseClickScript(NPC theDenizen, Player thePlayer, String theScript) {

		int theStep = plugin.getScript.getCurrentStep(thePlayer, theScript);
		List<ScriptCommand> scriptCommands = new ArrayList<ScriptCommand>();

		/* Let's get the Script from the file and turn it into ScriptCommands */
		List<String> chatScriptItems = plugin.getScripts().getStringList(theScript + ".Steps." + theStep + ".Click Trigger.Script");
		for (String thisItem : chatScriptItems) {
			String[] scriptEntry = thisItem.split(" ", 2);
			try {
				/* Build new script commands */
				scriptCommands.add(new ScriptCommand(scriptEntry[0], scriptEntry[1].split(" "), thePlayer, theDenizen, theScript, theStep));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		/* New ScriptCommand list built, now let's add it into the queue */
		List<ScriptCommand> scriptCommandList = taskQue.get(thePlayer);

		/* Keeps the commandQue from removing items while
		working on them here. They will be added back in. */ 
		taskQue.remove(thePlayer); 

		scriptCommandList.addAll(scriptCommands);
		taskQue.put(thePlayer, scriptCommandList);

		return true;
	}


	
	/* Parses the script for a task trigger */

	public boolean parseTaskScript(Player thePlayer, String theScript) {

		int theStep = plugin.getScript.getCurrentStep(thePlayer, theScript);
		List<ScriptCommand> scriptCommands = new ArrayList<ScriptCommand>();

		/* Let's get the Script from the file and turn it into ScriptCommands */
		List<String> chatScriptItems = plugin.getScripts().getStringList(theScript + ".Script");
		for (String thisItem : chatScriptItems) {
			String[] scriptEntry = thisItem.split(" ", 2);
			try {
				/* Build new script commands */
				scriptCommands.add(new ScriptCommand(scriptEntry[0], scriptEntry[1].split(" "), thePlayer, theScript, theStep));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		/* New ScriptCommand list built, now let's add it into the queue */
		List<ScriptCommand> scriptCommandList = taskQue.get(thePlayer);

		/* Keeps the commandQue from removing items while
		working on them here. They will be added back in. */ 
		taskQue.remove(thePlayer); 

		scriptCommandList.addAll(scriptCommands);
		taskQue.put(thePlayer, scriptCommandList);

		return true;
	}
	

	
	/* Injects commands into a QueueType  */

	public void injectToQue(Player thePlayer, List<ScriptCommand> scriptCommands, QueueType queueType, int thePosition) {

		List<ScriptCommand> scriptCommandList;
		
		switch (queueType) {

		case TRIGGER:
			scriptCommandList = taskQue.get(thePlayer);
			taskQue.remove(thePlayer); 
			if (thePosition > scriptCommandList.size() || thePosition < 0) thePosition = 1;
			if (scriptCommandList.size() == 0) thePosition = 0;
			scriptCommandList.addAll(thePosition, scriptCommands);
			taskQue.put(thePlayer, scriptCommandList);
			break;

		case TASK:
			scriptCommandList = taskQue.get(thePlayer);
			taskQue.remove(thePlayer); 
			if (thePosition > scriptCommandList.size() || thePosition < 0) thePosition = 1;
			if (scriptCommandList.size() == 0) thePosition = 0;
			scriptCommandList.addAll(thePosition, scriptCommands);
			taskQue.put(thePlayer, scriptCommandList);
			break;
		}

		return;
	}





	/* 
	 * Takes long text and splits it into multiple string elements in a list
	 * based on the config setting for MaximumLength, default 55.
	 * 
	 * Text should probably be formatted first with formatChatText, since 
	 * formatting usually adds some length to the beginning and end of the text.
	 */

	public List<String> getMultilineText (String theText) {


		List<String> processedText = new ArrayList<String>();

		if (theText == null) return processedText;

		String[] text = theText.split(" ");

		if (theText.length() > plugin.settings.MultiLineTextMaximumLength()) {

			processedText.add(0, "");

			int word = 0; int line = 0;

			while (word < text.length) {
				if (processedText.get(line).length() + text[word].length() < plugin.settings.MultiLineTextMaximumLength()) {
					processedText.set(line, processedText.get(line) + text[word] + " ");
					word++;
				}
				else { line++; processedText.add(""); }
			}
		}

		else processedText.add(0, theText);

		return processedText;
	}



	/* 
	 * Takes chat/whisper/etc. text and formats it based on the config settings. 
	 * Returns a String[]. 
	 * 
	 * Element [0] contains formatted text for the player interacting.
	 * Element [1] contains formatted text for bystanders.
	 * 
	 * Either can be null if only one type of text is required.
	 */

	public String[] formatChatText (String theMessage, String messageType, Player thePlayer, NPC theDenizen) {

		String playerMessageFormat = null;
		String bystanderMessageFormat = null;

		boolean toPlayer;

		if (thePlayer == null) toPlayer = false;
		else toPlayer = true;

		if (messageType.equalsIgnoreCase("SHOUT")) {
			playerMessageFormat = plugin.settings.NpcShoutToPlayer();
			bystanderMessageFormat = plugin.settings.NpcShoutToPlayerBystander();
			if (!toPlayer) bystanderMessageFormat = plugin.settings.NpcShoutToBystanders();
		}

		else if (messageType.equalsIgnoreCase("WHISPER")) {
			playerMessageFormat = plugin.settings.NpcWhisperToPlayer();
			bystanderMessageFormat = plugin.settings.NpcWhisperToPlayerBystander();
			if (!toPlayer) bystanderMessageFormat = plugin.settings.NpcWhisperToBystanders();
		}

		else if (messageType.equalsIgnoreCase("EMOTE")) {
			toPlayer = false;
			bystanderMessageFormat = "<NPC> <TEXT>";
		}

		else if (messageType.equalsIgnoreCase("NARRATE")) {
			playerMessageFormat = "<TEXT>";
		}

		else { /* CHAT */
			playerMessageFormat = plugin.settings.NpcChatToPlayer();
			bystanderMessageFormat = plugin.settings.NpcChatToPlayerBystander();
			if (!toPlayer) bystanderMessageFormat = plugin.settings.NpcChatToBystanders();
		}

		String denizenName = ""; 

		if (theDenizen != null) denizenName = theDenizen.getName();

		if (playerMessageFormat != null)
			playerMessageFormat = playerMessageFormat
			.replace("<NPC>", denizenName)
			.replace("<TEXT>", theMessage)
			.replace("<PLAYER>", thePlayer.getName())
			.replace("<DISPLAYNAME>", thePlayer.getDisplayName())
			.replace("<WORLD>", thePlayer.getWorld().getName())
			.replace("<HEALTH>", String.valueOf(thePlayer.getHealth()))
			.replace("%%", "\u00a7");

		if (bystanderMessageFormat != null)
			bystanderMessageFormat = bystanderMessageFormat
			.replace("<NPC>", denizenName)
			.replace("<TEXT>", theMessage)
			.replace("<PLAYER>", thePlayer.getName())
			.replace("<DISPLAYNAME>", thePlayer.getDisplayName())
			.replace("<WORLD>", thePlayer.getWorld().getName())
			.replace("<HEALTH>", String.valueOf(thePlayer.getHealth()))
			.replace("%%", "\u00a7");

		String[] returnedText = {playerMessageFormat, bystanderMessageFormat};

		return returnedText;
	}






	public void newLocationTask(Player thePlayer, NPC theDenizen,
			String theLocation, int theDuration, int theLeeway, String theScript) {


		/* 
		 * saves.yml
		 * 
		 * Players:
		 *   aufdemrand:
		 *     Tasks:
		 *       List All:
		 *         Locations:
		 *         - theLocation:theDenizen:theId
		 *       List Entries:
		 *         Id:
		 *           Type: Location
		 *           Leeway: in blocks
		 *           Duration: in seconds
		 *           Script to trigger: script name
		 *		     Initiated: System.currentTimeMillis 
		 * 		     Finished: true/false
		 *         
		 */

		long taskId = System.currentTimeMillis();

		/* Add new task to list */
		List<String> listAll = plugin.getSaves().getStringList("Players." + thePlayer.getName() + ".Tasks.List All.Locations");
		listAll.add(theLocation + ";" + theDenizen.getName() + ";" + taskId);
		plugin.getSaves().set("Players." + thePlayer.getName() + ".Tasks.List All.Locations", listAll);

		/* Populate task entry */
		String taskString = "Players." + thePlayer.getName() + ".Tasks.List Entries." + taskId + ".";

		plugin.getSaves().set(taskString + "Type", "Location");
		plugin.getSaves().set(taskString + "Leeway", theLeeway);
		plugin.getSaves().set(taskString + "Duration", theDuration);
		plugin.getSaves().set(taskString + "Script", theScript);

		plugin.saveSaves();

	}

	public void finishLocationTask(Player thePlayer, String taskId) {

		plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");

		List<String> listAll = plugin.getSaves().getStringList("Players." + thePlayer.getName() + ".Tasks.List All.Locations");			
		List<String> newList = new ArrayList<String>();

		for (String theTask : listAll) {
			if (!theTask.contains(taskId)) newList.add(theTask); 
		}

		if (newList.isEmpty()) plugin.getSaves().set("Players." + thePlayer.getName() + ".Tasks.List All.Locations", null);
		else plugin.getSaves().set("Players." + thePlayer.getName() + ".Tasks.List All.Locations", newList);

		String theScript = plugin.getSaves().getString("Players." + thePlayer.getName() + ".Tasks.List Entries." + taskId + ".Script");

		plugin.getSaves().set("Players." + thePlayer.getName() + ".Tasks.List Entries." + taskId, null);

		plugin.saveSaves();

		parseScript(null, thePlayer, theScript, null, TRIGGER.TASK);

	}



	/*
	 * Checks a Player's location against a Location (with leeway). Should be faster than
	 * bukkit's built in Location.distance(Location) since there's no sqrt math.
	 * 
	 * Thanks chainsol :)
	 */


	public boolean checkLocation(Player thePlayer, Location theLocation, int theLeeway) {
		if (Math.abs(thePlayer.getLocation().getBlockX() - theLocation.getBlockX()) 
				> theLeeway) return false;
		if (Math.abs(thePlayer.getLocation().getBlockY() - theLocation.getBlockY()) 
				> theLeeway) return false;
		if (Math.abs(thePlayer.getLocation().getBlockX() - theLocation.getBlockX()) 
				> theLeeway) return false;

		return true;
	}

	public boolean checkLocation(NPC theDenizen, Location theLocation, int theLeeway) {
		if (Math.abs(theDenizen.getBukkitEntity().getLocation().getBlockX() - theLocation.getBlockX()) 
				> theLeeway) return false;
		if (Math.abs(theDenizen.getBukkitEntity().getLocation().getBlockY() - theLocation.getBlockY()) 
				> theLeeway) return false;
		if (Math.abs(theDenizen.getBukkitEntity().getLocation().getBlockX() - theLocation.getBlockX()) 
				> theLeeway) return false;

		return true;
	}



	/*
	 * Builds a map<Location, "Denizen Id:location bookmark name"> of all the location bookmarks
	 * for matching location triggers.  
	 */

	public void buildLocationTriggerList() {
		Collection<NPC> DenizenNPCs = CitizensAPI.getNPCRegistry().getNPCs(DenizenCharacter.class);
		Denizen.validLocations.clear();

		for (NPC theDenizen : DenizenNPCs) {
			if (plugin.getSaves().contains("Denizens." + theDenizen.getName() + ".Bookmarks.Location")) {
				List<String> locationsToAdd = plugin.getSaves().getStringList("Denizens." + theDenizen.getName() + ".Bookmarks.Location");

				for (String thisLocation : locationsToAdd) {
					if (!thisLocation.isEmpty()) {
						Location theLocation = plugin.getDenizen.getBookmark(theDenizen.getName(), thisLocation.split(" ", 2)[0], "LOCATION");
						String theInfo = theDenizen.getId() + ":" + thisLocation.split(" ", 2)[0];
						Denizen.validLocations.put(theLocation, theInfo);
					}
				}
			}
		}	

		plugin.getLogger().log(Level.INFO, "Trigger list built. Size: " + Denizen.validLocations.size());

		return;
	}





	public void enforcePosition() {
		Collection<NPC> DenizenNPCs = CitizensAPI.getNPCRegistry().getNPCs(DenizenCharacter.class);

		for (NPC theDenizen : DenizenNPCs) {
			if (plugin.getSaves().contains("Denizens." + theDenizen.getName() + ".Position.Standing")) {
				if (!plugin.getSaves().getString("Denizens." + theDenizen.getName() + ".Position.Standing").isEmpty()) {

					Location enforcedLoc = plugin.getDenizen.getBookmark(theDenizen.getName(), 
							plugin.getSaves().getString("Denizens." + theDenizen.getName() + ".Position.Standing"), 
							"LOCATION");

					if (!checkLocation(theDenizen, enforcedLoc, 0) && !theDenizen.getAI().hasDestination())
						theDenizen.getAI().setDestination(enforcedLoc);

				}
			}
		}
	}








}