package net.aufdemrand.denizen;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Random;
import net.aufdemrand.denizen.Denizen;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;

public class DenizenListener implements Listener {

	Denizen plugin;
	public DenizenListener(Denizen instance) { plugin = instance; }



	public enum ScriptBehavior {
		CONTROLLED, REPEAT, LOOP;
	}

	public enum RequirementMode {
		NONE, ALL, ANY; 
	}

	public enum Requirement {
		NONE, TIME, PRECIPITATION, STORMY, SUNNY, HUNGER, WORLD, PERMISSION, LEVEL, SCRIPT, NOTABLE, GROUP, MONEY, ITEM, QUEST;
	}

	public enum Trigger {
		CHAT, CLICK, RIGHT_CLICK, LEFT_CLICK, FINISH, START, FAIL;
	}

	public enum Command {
		GIVE, TAKE, WALK, PAUSE, CHAT, WHISPER, SHOUT, NARRARATE, TELEPORT, PERMISS, EXECUTE, ZAP; 
	}



	/* PlayerChatListener
	 * 
	 * Called when the player chats.  Determines if player is near a Denizen, and if so, checks if there
	 * are scripts to interact with.  Also handles the chat output for the Player talking to the Denizen.
	 * 
	 * Calls GetDenizensWithinRange, TalkToNPC, GetInteractScript, ParseScript
	 */

	@EventHandler
	public void PlayerChatListener(PlayerChatEvent event) {

		List<net.citizensnpcs.api.npc.NPC> DenizenList = GetDenizensWithinRange(event.getPlayer().getLocation(), event.getPlayer().getWorld(), plugin.PlayerChatRangeInBlocks);
		if (DenizenList.isEmpty()) { return; }
		event.setCancelled(true);
		for (net.citizensnpcs.api.npc.NPC thisDenizen : DenizenList) {
			TalkToNPC(thisDenizen, event.getPlayer(), event.getMessage());
			String theScript = GetInteractScript(thisDenizen, event.getPlayer());
			if (theScript.equals("none")) thisDenizen.chat(event.getPlayer(), plugin.getConfig().getString("Denizens." + thisDenizen.getId() + ".Default Texts.No Script Interact", "I have nothing to say to you at this time."));
			else if (!theScript.equals("none")) ParseScript(event.getPlayer(), GetScriptName(theScript), event.getMessage(), Trigger.CHAT);
		}
	}



	/* GetDenizensWithinRange
	 * 
	 * Requires Player Location, Player World, Range in blocks.
	 * Compiles a list of NPCs with a character type of Denizen
	 * within range of the Player.
	 * 
	 * Returns DenizensWithinRange List<NPC>
	 */

	// Is this the best way to do this?  Should we instead use getNearbyEntities?  Probably.  Can change later if becomes an issue.

	public List<net.citizensnpcs.api.npc.NPC> GetDenizensWithinRange (Location PlayerLocation, World PlayerWorld, int Range) {

		List<net.citizensnpcs.api.npc.NPC> DenizensWithinRange = new ArrayList<net.citizensnpcs.api.npc.NPC>();
		Collection<net.citizensnpcs.api.npc.NPC> DenizenNPCs = CitizensAPI.getNPCManager().getNPCs(DenizenCharacter.class); 
		if (DenizenNPCs.isEmpty()) return DenizensWithinRange;
		List<net.citizensnpcs.api.npc.NPC> DenizenList = new ArrayList<NPC>(DenizenNPCs);
		for (int x = 0; x < DenizenList.size(); x++) {
			if (DenizenList.get(x).getBukkitEntity().getWorld().equals(PlayerWorld)) {
				if (DenizenList.get(x).getBukkitEntity().getLocation().distance(PlayerLocation) < Range) DenizensWithinRange.add(DenizenList.get(x));
			}
		}
		return DenizensWithinRange;
	}



	/* TalkToNPC
	 * 
	 * Requires the NPC Denizen, Player, and the Message to relay.
	 * Sends the message from Player to Denizen with the formatting
	 * as specified in the config.yml talk_to_npc_string.
	 * 
	 * <NPC> and <TEXT> are replaced with corresponding information.
	 */

	public void TalkToNPC(net.citizensnpcs.api.npc.NPC theDenizen, Player thePlayer, String theMessage)
	{
		thePlayer.sendMessage(plugin.TalkToNPCString.replace("<NPC>", theDenizen.getName().toString()).replace("<TEXT>", theMessage));
	}



	/* GetInteractScript
	 * 
	 * Requires the Denizen and the Player
	 * Checks the Denizens scripts and returns the script that meets requirements and has
	 * the highest weight.  If no script matches, returns "none".
	 * 
	 * Returns theScript
	 * Calls CheckRequirements
	 */

	public String GetInteractScript(net.citizensnpcs.api.npc.NPC thisDenizen, Player thisPlayer) {
		String theScript = "none";
		List<String> ScriptList = plugin.getConfig().getStringList("Denizens." + thisDenizen.getName() + ".Scripts");
		if (ScriptList.isEmpty()) { return theScript; }
		List<String> ScriptsThatMeetRequirements = new ArrayList<String>();
		// Get scripts that meet requirements
		for (String thisScript : ScriptList) {
			String [] thisScriptArray = thisScript.split(" ", 2);
			if (CheckRequirements(thisScriptArray[1], thisPlayer) == true) { ScriptsThatMeetRequirements.add(thisScript); }
		}
		// Get highest scoring script
		if (ScriptsThatMeetRequirements.size() > 1) {

			int ScriptPriority = -1;

			for (String thisScript : ScriptsThatMeetRequirements) {
				String [] thisScriptArray = thisScript.split(" ", 2);
				if (Integer.parseInt(thisScriptArray[0]) > ScriptPriority) {ScriptPriority = Integer.parseInt(thisScriptArray[0]); theScript = thisScriptArray[1]; }
			}
		}
		else if (ScriptsThatMeetRequirements.size() == 1) { theScript = ScriptsThatMeetRequirements.get(0); }

		return theScript;
	}



	/* ParseScript
	 * 
	 * Requires the Player, the Script Name, the chat message (if Chat Trigger, otherwise send null), 
	 * and the Trigger ENUM type.
	 * Sends out methods that take action based on the Trigger types. 
	 * 
	 * case CHAT calls GetCurrentStep, GetChatTriggers, TriggerChatToQue
	 * case CLICK,RIGHT_CLICK,LEFT_CLICK calls
	 * case FINISH calls
	 * case START calls
	 * case FAIL calls
	 */


	public void ParseScript(Player thePlayer, String theScript, String theMessage,  Trigger theTrigger) {

		switch (theTrigger) {

		case CHAT:
			int CurrentStep = GetCurrentStep(thePlayer, theScript);
			List<String> ChatTriggerList = GetChatTriggers(theScript, CurrentStep);
			for (int l=0; l < ChatTriggerList.size(); l++ ) {
				if (theMessage.matches(ChatTriggerList.get(l))) {
					TriggerChatToQue(theScript, CurrentStep, l, thePlayer);
				}
			}
			return;

		case CLICK:
			// get current progression
			// send script
			return;

		case FINISH:
			// get current progressions
			// send script
			return;

		}
	}



	/* TriggerChatToQue
	 * 
	 * Requires the Script, the Current Step, the Chat Trigger to trigger, and the Player
	 * Triggers the script for the chat trigger of the step and script specified.
	 * 
	 * Calls ScriptHandler to handle the commands in the script. ScriptHandler returns any
	 * raw text that needs to be sent to the player which is put in the PlayerQue for
	 * output. 
	 */

	public void TriggerChatToQue(String theScript, int CurrentStep, int ChatTrigger, Player thePlayer) {

		List<String> CurrentPlayerQue = new ArrayList<String>();
		if (plugin.PlayerQue.get(thePlayer) != null) CurrentPlayerQue = plugin.PlayerQue.get(thePlayer);
		plugin.PlayerQue.remove(thePlayer);  // Should keep the talk queue from triggering mid-add
		List<String> AddedToPlayerQue = plugin.getConfig().getStringList("Scripts." + theScript + ".Progression." + CurrentStep + ".Interact.Chat Trigger." + ChatTrigger + ".Script");
		if (!AddedToPlayerQue.isEmpty()) {
			for (int entry = 0; entry < AddedToPlayerQue.size(); entry++) CurrentPlayerQue.add(AddedToPlayerQue.get(0));
		}
		ScriptHandler(theScript, CurrentStep, thePlayer, CurrentPlayerQue, Trigger.CHAT);
		plugin.PlayerQue.put(thePlayer, CurrentPlayerQue);
		return;
	}



	public List<String> ScriptHandler(String theScript, int CurrentStep, Player thePlayer, List<String> ScriptToHandle, Trigger theTrigger) {

		return ScriptToHandle;
	}



	/* GetCurrentStep
	 * 
	 * Requires the Player and the Script.
	 * Reads the config.yml to find the current step that the player is on
	 * for the specified script.
	 * 
	 * Returns currentStep
	 */

	public int GetCurrentStep(Player thePlayer, String theScript) {
		int currentStep = 0;
		if (plugin.getConfig().getString(thePlayer + "." + theScript + "." + "CurrentStep") != null) currentStep =  plugin.getConfig().getInt(thePlayer + "." + theScript + "." + "CurrentStep"); 
		return currentStep;
	}



	/* GetChatTriggers
	 * 
	 * Requires the Script and the Current Step.
	 * Gets a list of Chat Triggers for the step of the script specified.
	 * Chat Triggers are words required to trigger one of the chat scripts.
	 * 
	 * Returns ChatTriggers
	 */

	public List<String> GetChatTriggers(String theScript, Integer currentStep) {
		List<String> ChatTriggers = new ArrayList<String>();
		int currentTrigger = 0;
		// Add triggers to list
		for (int x=0; currentTrigger >= 0; x++) {
			String theChatTrigger = plugin.getConfig().getString("Scripts." + theScript + ".Progression." + currentStep + ".Interact.Chat Trigger." + String.valueOf(currentTrigger) + ".Trigger");
			if (theChatTrigger != null) { ChatTriggers.add(theChatTrigger); currentTrigger = x + 1; } 
			else currentTrigger = -1;
		}
		return ChatTriggers;
	}



    /* GetScriptName
     * 
     * Requires the raw script entry from the config.
     * Strips the priority number from the beginning of the script name.
     * 
     * Returns the Script Name
     */

	public String GetScriptName(String thisScript) {
		if (thisScript.equals("none")) { return thisScript; }
		else {
			String [] thisScriptArray = thisScript.split(" ", 2);
			return thisScriptArray[1]; }
	}

	

	// CHECK REQUIREMENTS  (Checks if the requirements of a script are met when given Script/Player)

	public boolean CheckRequirements(String thisScript, Player thisPlayer) {

		String RequirementsMode = plugin.getConfig().getString("Scripts." + thisScript + ".Requirements.Mode");

		List<String> RequirementsList = plugin.getConfig().getStringList("Scripts." + thisScript + ".Requirements.List");
		if (RequirementsList.isEmpty()) { 				
			return true; }

		int NumberOfMetRequirements = 0;

		for (String RequirementArgs : RequirementsList) {
			String[] RequirementWithSplitArgs = RequirementArgs.split(" ", 2);

			switch (Requirement.valueOf(RequirementWithSplitArgs[0].toUpperCase())) {

			case NONE:
				return true;

			case TIME:
			    
				
			case PERMISSION:
				
				
			case PRECIPITATION:
				
				
			case HUNGER:
				
				
			case LEVEL:  // LEVEL [#]
				if (Array.getLength(RequirementWithSplitArgs[1].split(" ")) == 1) { 
					if (thisPlayer.getLevel() >= Integer.parseInt(RequirementWithSplitArgs[1])) NumberOfMetRequirements++; 
				} else {
					
				}
			case QUEST:
				
				
			case NOTABLE:
				
				
			case WORLD:  // WORLD [World Name]
				if (thisPlayer.getWorld().getName().equalsIgnoreCase(RequirementWithSplitArgs[1])) NumberOfMetRequirements++;
				
			case STORMY:
				if (thisPlayer.getWorld().isThundering()) NumberOfMetRequirements++;
				
			case SUNNY:
				if (!thisPlayer.getWorld().hasStorm()) NumberOfMetRequirements++;
				
			case MONEY:
				if (plugin.econ.has(thisPlayer.toString(), Integer.parseInt(RequirementWithSplitArgs[1]))) NumberOfMetRequirements++;
				
			case ITEM:
				
				
			case SCRIPT:
				
				
			case GROUP:
				if (plugin.perms.playerInGroup(thisPlayer.getWorld(), thisPlayer.toString(), RequirementWithSplitArgs[1])) NumberOfMetRequirements++;		
			}
		}

		if (RequirementsMode.equalsIgnoreCase("all") && NumberOfMetRequirements == RequirementsList.size()) { 
			return true; 
		}

		String[] ModeArgs = RequirementsMode.split(" ");
		if (ModeArgs[0].equalsIgnoreCase("any") && NumberOfMetRequirements >= Integer.parseInt(ModeArgs[1])) { return true;	}
		return false;
	}



}