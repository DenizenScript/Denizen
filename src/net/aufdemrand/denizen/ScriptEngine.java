package net.aufdemrand.denizen;

import java.util.*;
import java.util.logging.Level;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.aufdemrand.denizen.Denizen;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;


public class ScriptEngine extends Denizen {

	public enum Trigger {
		CHAT, CLICK, PROXIMITY, FAIL, FINISH, TASK, LOCATION
	}

	

	/*
	 * commandQue
	 * 
	 * Performs Denizen commands from each Player's Queue.
	 * 
	 */

	public void commandQue() {

		boolean instantCommand = false;
		if (!playerQue.isEmpty()) {	

			for (Map.Entry<Player, List<String>> theEntry :playerQue.entrySet()) {
				if (!theEntry.getValue().isEmpty()) {
					if (Long.valueOf(theEntry.getValue().get(0).split(";")[3]) < System.currentTimeMillis()) {
						do { commandExecuter.execute(theEntry.getKey(), theEntry.getValue().get(0));
						instantCommand = false;
						if (theEntry.getValue().get(0).split(";")[4].startsWith("^")) instantCommand = true;
						theEntry.getValue().remove(0);
						playerQue.put(theEntry.getKey(), theEntry.getValue());
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
	 * This will be the backbone to automated activity scripts. Currently this is not used
	 * any further than what's in this method, but will be build upon soon.
	 */

	public void scheduleScripts() {

		Collection<NPC> DenizenNPCs = CitizensAPI.getNPCRegistry().getNPCs(DenizenCharacter.class);
		if (DenizenNPCs.isEmpty()) return;
		List<NPC> DenizenList = new ArrayList<NPC>(DenizenNPCs);
		for (NPC aDenizen : DenizenList) {
			if (aDenizen.isSpawned())	{
				int denizenTime = Math.round(aDenizen.getBukkitEntity().getWorld().getTime() / 1000);
				List<String> denizenActivities = getAssignments().getStringList("Denizens." + aDenizen.getName() + ".Scheduled Activities");
				if (!denizenActivities.isEmpty()) {
					for (String activity : denizenActivities) {
						if (activity.startsWith(String.valueOf(denizenTime))) {
							// getServer().broadcastMessage("Updating Activity Script for " + aDenizen.getName());
							getSaves().set("Denizens." + aDenizen.getName() + ".Active Activity Script", activity.split(" ", 2)[1]);
							saveSaves();
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

	public boolean parseScript(NPC theDenizen, Player thePlayer, String theScript, String theMessage, Trigger theTrigger) {
				
		int theStep = getScript.getCurrentStep(thePlayer, theScript);

		switch (theTrigger) {

		case CHAT:

			/* 
			 * Get Chat Triggers and check each to see if there are any matches. 
			 */
			List<String> ChatTriggerList = getScript.getChatTriggers(theScript, theStep);
			for (int x=0; x < ChatTriggerList.size(); x++ ) {

				/* 
				 * The texts required to trigger.
				 */
				String chatTriggers = ChatTriggerList.get(x)
						.replace("<PLAYER>", thePlayer.getName())
						.replace("<DISPLAYNAME>", ChatColor.stripColor(thePlayer.getDisplayName())).toLowerCase();
				/* 
				 * The in-game friendly Chat Trigger text to display if triggered. 
				 */
				String chatText = getScripts()
						.getString(theScript + ".Steps." + theStep + ".Chat Trigger." + String.valueOf(x + 1) + ".Trigger")
						.replace("/", "");

				boolean letsProceed = false;

				for (String chatTrigger : chatTriggers.substring(0, chatTriggers.length() - 1).split(":")) {
					if (theMessage.toLowerCase().contains(chatTrigger)) letsProceed = true;
				}

				if (letsProceed) {
					/* 
					 * Trigger matches, let's talk to the Denizen and send the script to the PlayerQueue. 
					 */
					getPlayer.talkToDenizen(theDenizen, thePlayer, chatText);
					triggerToQue(theScript, theStep, thePlayer, theDenizen,
							getScripts().getStringList(theScript + ".Steps." + theStep + ".Chat Trigger." + String.valueOf(x + 1) + ".Script"));
					return true;
				}
			}

			/* 
			 * No matching triggers. 
			 */

			if(settings.ChatGloballyIfFailedChatTriggers()) return false;
			else {
				getPlayer.talkToDenizen(theDenizen, thePlayer, theMessage);

				String noscriptChat = null;

				if (getAssignments().contains("Denizens." + theDenizen.getName() 
						+ ".Texts.No Requirements Met")) 
					noscriptChat = getAssignments().getString("Denizens." + theDenizen.getName() 
							+ ".Texts.No Requirements Met");
				else
					noscriptChat = settings.DefaultNoRequirementsMetText();

				getDenizen.talkToPlayer(theDenizen, thePlayer, scriptEngine.formatChatText(noscriptChat, "CHAT", thePlayer, theDenizen)[0], null, "CHAT");

				return true;
			}

		case CLICK:
			triggerToQue(theScript, theStep, thePlayer, theDenizen, 
					getScripts().getStringList("" + theScript + ".Steps." + theStep + ".Click Trigger.Script"));
			return true;

		case TASK:
			triggerToQue(theScript, 0, thePlayer, null, 
					getScripts().getStringList(theScript + ".Script"));
			return true;

		case LOCATION:
			if (getScripts().contains(theScript + ".Steps." + theStep + ".Location Trigger")) {

				if (getScripts().getString(theScript + ".Steps." + theStep + ".Location Trigger.1.Trigger")
						.equalsIgnoreCase(theMessage)) {
					triggerToQue(theScript, theStep, thePlayer, theDenizen, 
							getScripts().getStringList(theScript + ".Steps." + theStep + ".Location Trigger.1.Script"));
					return true;
				}
			}
			else return false;

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
	 * Places items (addedToPlayerQue) into the playerQue for command execution. 
	 *
	 */

	public void triggerToQue(String theScript, int CurrentStep, Player thePlayer, NPC theDenizen, List<String> addedToPlayerQue) {

		List<String> currentPlayerQue = new ArrayList<String>();
		if (playerQue.get(thePlayer) != null) currentPlayerQue =playerQue.get(thePlayer);

		String denizenId = "none";
		if (theDenizen != null) denizenId = String.valueOf(theDenizen.getId()); 

		if (!addedToPlayerQue.isEmpty()) {

			/* 
			 * Temporarily take away the playerQue for the Player to make sure nothing gets
			 * removed while working with it.
			 */
			playerQue.remove(thePlayer);

			for (String theCommand : addedToPlayerQue) {
				/* PlayerQue format: DENIZEN ID; THE SCRIPT NAME; THE STEP; SYSTEM TIME; THE COMMAND */
				currentPlayerQue.add(denizenId + ";" + theScript + ";" + CurrentStep + ";" + String.valueOf(System.currentTimeMillis()) + ";" + theCommand);	
			}

			playerQue.put(thePlayer, currentPlayerQue);
		}
	}


	
	/*  
	 * Injects commands into the playerQue. Originally made for working with multiline text,
	 * but currently unused. I'm sure it will be useful again. This is different than 
	 * triggerToQue in the sense that triggerToQue adds elements to the end of the playerQue
	 * and this adds the items to the beginning of the queue.
	 */

	public void injectToQue(String theScript, int CurrentStep, Player thePlayer, NPC theDenizen, List<String> addedToPlayerQue) {

		List<String> currentPlayerQue = new ArrayList<String>();
		List<String> injectToPlayerQue = new ArrayList<String>();
		if (playerQue.get(thePlayer) != null) currentPlayerQue = playerQue.get(thePlayer);

		if (!addedToPlayerQue.isEmpty()) {

			/* 
			 * Temporarily take away the playerQue for the Player to make sure nothing gets
			 * removed while working with it.
			 */
			playerQue.remove(thePlayer);

			for (String theCommand : addedToPlayerQue) {
				/* PlayerQue format: DENIZEN ID; THE SCRIPT NAME; THE STEP; SYSTEM TIME; THE COMMAND */
				injectToPlayerQue.add(Integer.toString(theDenizen.getId()) + ";" + theScript + ";" + Integer.toString(CurrentStep) + ";" + String.valueOf(System.currentTimeMillis()) + ";" + theCommand);	
			}

			currentPlayerQue.addAll(1, injectToPlayerQue);
			playerQue.put(thePlayer, currentPlayerQue);
		}
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

		if (theText.length() > settings.MultiLineTextMaximumLength()) {

			processedText.add(0, "");

			int word = 0; int line = 0;

			while (word < text.length) {
				if (processedText.get(line).length() + text[word].length() < settings.MultiLineTextMaximumLength()) {
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
			playerMessageFormat = settings.NpcShoutToPlayer();
			bystanderMessageFormat = settings.NpcShoutToPlayerBystander();
			if (!toPlayer) bystanderMessageFormat = settings.NpcShoutToBystanders();
		}

		else if (messageType.equalsIgnoreCase("WHISPER")) {
			playerMessageFormat = settings.NpcWhisperToPlayer();
			bystanderMessageFormat = settings.NpcWhisperToPlayerBystander();
			if (!toPlayer) bystanderMessageFormat = settings.NpcWhisperToBystanders();
		}

		else if (messageType.equalsIgnoreCase("EMOTE")) {
			toPlayer = false;
			bystanderMessageFormat = "<NPC> <TEXT>";
		}

		else if (messageType.equalsIgnoreCase("NARRATE")) {
			playerMessageFormat = "<TEXT>";
		}

		else { /* CHAT */
			playerMessageFormat = settings.NpcChatToPlayer();
			bystanderMessageFormat = settings.NpcChatToPlayerBystander();
			if (!toPlayer) bystanderMessageFormat = settings.NpcChatToBystanders();
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
		 *         
		 */

		long taskId = System.currentTimeMillis();

		/* Add new task to list */
		List<String> listAll = getSaves().getStringList("Players." + thePlayer.getName() + ".Tasks.List All.Locations");
		listAll.add(theLocation + ";" + theDenizen.getName() + ";" + taskId);
		getSaves().set("Players." + thePlayer.getName() + ".Tasks.List All.Locations", listAll);

		/* Populate task entry */
		String taskString = "Players." + thePlayer.getName() + ".Tasks.List Entries." + taskId + ".";

		getSaves().set(taskString + "Type", "Location");
		getSaves().set(taskString + "Leeway", theLeeway);
		getSaves().set(taskString + "Duration", theDuration);
		getSaves().set(taskString + "Script", theScript);

		saveSaves();

	}

	public void finishLocationTask(Player thePlayer, String taskId) {

		List<String> listAll = getSaves().getStringList("Players." + thePlayer.getName() + ".Tasks.List All.Locations");			
		List<String> newList = new ArrayList<String>();

		for (String theTask : listAll) {
			if (!theTask.contains(taskId)) newList.add(theTask); 
		}

		if (newList.isEmpty()) getSaves().set("Players." + thePlayer.getName() + ".Tasks.List All.Locations", null);
		else getSaves().set("Players." + thePlayer.getName() + ".Tasks.List All.Locations", newList);

		String theScript = getSaves().getString("Players." + thePlayer.getName() + ".Tasks.List Entries." + taskId + ".Script");

		getSaves().set("Players." + thePlayer.getName() + ".Tasks.List Entries." + taskId, null);

		saveSaves();

		parseScript(null, thePlayer, theScript, null, Trigger.TASK);

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
			if (getSaves().contains("Denizens." + theDenizen.getName() + ".Bookmarks.Location")) {
				List<String> locationsToAdd = getSaves().getStringList("Denizens." + theDenizen.getName() + ".Bookmarks.Location");

				for (String thisLocation : locationsToAdd) {
					if (!thisLocation.isEmpty()) {
						Location theLocation = getDenizen.getBookmark(theDenizen.getName(), thisLocation.split(" ", 2)[0], "LOCATION");
						String theInfo = theDenizen.getId() + ":" + thisLocation.split(" ", 2)[0];
						Denizen.validLocations.put(theLocation, theInfo);
					}
				}
			}
		}	

		getLogger().log(Level.INFO, "Trigger list built. Size: " + Denizen.validLocations.size());

		return;
	}




	/*
	 * Testing method to help with keeping Denizen NPCs in place since in C2 you can
	 * push them. This will change.
	 * 
	 */

	public void enforcePosition() {

		Collection<NPC> DenizenNPCs = CitizensAPI.getNPCRegistry().getNPCs(DenizenCharacter.class);

		for (NPC theDenizen : DenizenNPCs) {
			if (getSaves().contains("Denizens." + theDenizen.getName() + ".Position.Standing")) {
				if (!getSaves().getString("Denizens." + theDenizen.getName() + ".Position.Standing").isEmpty()) {
					Location enforcedLoc = getDenizen.getBookmark(theDenizen.getName(), 
							getSaves().getString("Denizens." + theDenizen.getName() + ".Position.Standing"), 
							"LOCATION");
					if (!checkLocation(theDenizen, enforcedLoc, 0) && !theDenizen.getAI().hasDestination())
						theDenizen.getAI().setDestination(enforcedLoc);
				}
			}
		}
	}








}