package net.aufdemrand.denizen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Random;
import net.aufdemrand.denizen.DenizenParser;
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

	DenizenParser parser;
	public DenizenListener(DenizenParser instance) { parser = instance; }


	@EventHandler
	public void PlayerChatListener(PlayerChatEvent event) {

		List<net.citizensnpcs.api.npc.NPC> DenizenList = GetDenizensWithinRange(event.getPlayer().getLocation(), event.getPlayer().getWorld(), plugin.PlayerChatRangeInBlocks);
		if (DenizenList.isEmpty()) { return; }
		/* Debugging */	if (plugin.DebugMode) { plugin.getServer().broadcastMessage("** DEBUG - # of Denizens in the area: " + DenizenList.size()); }
		event.setCancelled(true);
		for (net.citizensnpcs.api.npc.NPC thisDenizen : DenizenList) {
			/* Debugging */ if (plugin.DebugMode) { plugin.getServer().broadcastMessage("** DEBUG - Currently working with Denizen: " + thisDenizen.getName()); }
			TalkToNPC(thisDenizen, event.getPlayer(), event.getMessage());
			String theScript = GetInteractScript(thisDenizen, event.getPlayer());
			if (theScript.equals("none")) { 
				thisDenizen.chat(event.getPlayer(), plugin.getConfig().getString("Denizens." + thisDenizen.getId() + ".Default Texts.No Script Interact", "I have nothing to say to you at this time."));
				/* Debugging */	if (plugin.DebugMode) { plugin.getServer().broadcastMessage("** DEBUG - No scripts meet requirements!"); } }
			else if (!theScript.equals("none")) { 
				/* Debugging */	if (plugin.DebugMode) { plugin.getServer().broadcastMessage("** DEBUG - Currently working with Script: " + GetScriptName(theScript)); }

				ParseScript(event.getMessage(), event.getPlayer(), GetScriptName(theScript), "Chat");
			}
		}
	}


	public void TalkToNPC(net.citizensnpcs.api.npc.NPC theDenizen, Player thePlayer, String theMessage)
	{
		thePlayer.sendMessage(plugin.TalkToNPCString.replace("<NPC>", theDenizen.getName().toString()).replace("<TEXT>", theMessage));
	}


	// GET DENIZENS WITHIN RANGE OF PLAYER
	public List<net.citizensnpcs.api.npc.NPC> GetDenizensWithinRange (Location PlayerLocation, World PlayerWorld, int Distance) {

		List<net.citizensnpcs.api.npc.NPC> DenizensWithinRange = new ArrayList<net.citizensnpcs.api.npc.NPC>();
		Collection<net.citizensnpcs.api.npc.NPC> DenizenNPCs = CitizensAPI.getNPCManager().getNPCs(DenizenCharacter.class); 
		if (DenizenNPCs.isEmpty()) { return DenizensWithinRange; }
		List<net.citizensnpcs.api.npc.NPC> DenizenList = new ArrayList<NPC>(DenizenNPCs);
		for (int x = 0; x < DenizenList.size(); x++) {
			if (DenizenList.get(x).getBukkitEntity().getWorld().equals(PlayerWorld)) {
				if (DenizenList.get(x).getBukkitEntity().getLocation().distance(PlayerLocation) < Distance) {
					DenizensWithinRange.add(DenizenList.get(x));
				}
			}
		}
		return DenizensWithinRange;
	}




	// PARSE SCRIPT
	public void ParseScript(String theMessage, Player thePlayer, String theScript, String InteractionType) {

		if (plugin.DebugMode) { plugin.getServer().broadcastMessage("** DEBUG - ParseScript called and passed: " + thePlayer.getName() + ", " + theScript + ", " + InteractionType); }

		if (InteractionType.equalsIgnoreCase("Chat"))
		{
			int CurrentStep = GetCurrentStep(thePlayer, theScript);
			List<String> ChatTriggerList = GetChatTriggers(theScript, CurrentStep);

			for (int l=0; l < ChatTriggerList.size(); l++ ) {
				if (theMessage.matches(ChatTriggerList.get(l))) {
					TriggerChatToQue(theScript, CurrentStep, l, thePlayer);
				}
			}

			return;

		}

		if (InteractionType.equalsIgnoreCase("Click"))
		{
			// get current progression
			// send script
		}

		if (InteractionType.equalsIgnoreCase("OnFinish")) 
		{
			// get current progressions
			// send script
		}

		return;
	}


	// Send a chat trigger to the Player Talk Que

	public void TriggerChatToQue(String theScript, int CurrentStep, int ChatTrigger, Player thePlayer) {

		if (plugin.DebugMode) { plugin.getServer().broadcastMessage("** DEBUG - TriggerChat called and passed: " + theScript + ", " + CurrentStep + ", " + ChatTrigger); }

		List<String> CurrentPlayerQue = new ArrayList();
		CurrentPlayerQue = plugin.PlayerQue.get(thePlayer);

		plugin.PlayerQue.remove(thePlayer);  // Should keep the talk que from triggering mid-add

		List<String> AddedToPlayerQue = plugin.getConfig().getStringList("Scripts." + theScript + ".Progression." + CurrentStep + ".Chat Trigger." + ChatTrigger + ".Script");

		if (AddedToPlayerQue != null) {

			for (String AddThis : AddedToPlayerQue) {

				CurrentPlayerQue.add(AddThis);

			} }

		plugin.PlayerQue.put(thePlayer, CurrentPlayerQue);

		return;

	}



	// GET CURRENT STEP  (Gets the player's current step on a script when given Player and Script)

	public int GetCurrentStep(Player thePlayer, String theScript) {

		if (plugin.DebugMode) { plugin.getServer().broadcastMessage("** DEBUG - GetCurrentStep called and passed:" + thePlayer.getName() + ", " + theScript); }

		int currentStep = 0;
		if (plugin.getConfig().getString(thePlayer + "." + theScript + "." + "CurrentStep") != null) 
		{ 
			currentStep =  plugin.getConfig().getInt(thePlayer + "." + theScript + "." + "CurrentStep"); 
		}

		if (plugin.DebugMode) { plugin.getServer().broadcastMessage("** DEBUG - GetCurrentStep returning: " + currentStep); }

		return currentStep;

	}



	public List<String> GetChatTriggers(String theScript, Integer currentStep) {

		if (plugin.DebugMode) { plugin.getServer().broadcastMessage("** DEBUG - GetChatTriggers called and passed: " + theScript + ", " + currentStep); }

		List<String> ChatTriggers = new ArrayList<String>();

		int currentTrigger = 0;

		if (plugin.DebugMode) { plugin.getServer().broadcastMessage("** DEBUG - Current Chat Trigger: " + String.valueOf(currentTrigger));}

		// Add triggers to list
		for (int x=0; currentTrigger >= 0; x++) {
			String theChatTrigger = plugin.getConfig().getString("Scripts." + theScript + ".Progression." + currentStep + ".Interact.Chat Trigger." + String.valueOf(currentTrigger) + ".Trigger");
			if (plugin.DebugMode) { plugin.getServer().broadcastMessage("** DEBUG - GetChatTrigger: " + theChatTrigger); }
			if (theChatTrigger != null) {ChatTriggers.add(theChatTrigger); currentTrigger = x + 1;} 
			else {currentTrigger = -1;}

		}

		if (plugin.DebugMode) { plugin.getServer().broadcastMessage("** DEBUG - GetChatTriggers found: " + ChatTriggers.toString()); }

		return ChatTriggers;



	}



	// GET SCRIPT  (Gets the script to interact with when given Player/Denizen)

	public String GetInteractScript(net.citizensnpcs.api.npc.NPC thisDenizen, Player thisPlayer) {
		/* Debugging */ if (plugin.DebugMode) { plugin.getServer().broadcastMessage("** DEBUG - GetInteractScript called and passed: " + thisDenizen.getName() + ", " + thisPlayer.getName()); }
		String theScript = "none";
		List<String> ScriptList = plugin.getConfig().getStringList("Denizens." + thisDenizen.getId() + ".Scripts");
		/* Debugging */ if (plugin.DebugMode) { plugin.getServer().broadcastMessage("** DEBUG - List of scripts found: " + ScriptList.toString()); }
		if (ScriptList.isEmpty()) { return theScript; }
		List<String> ScriptsThatMeetRequirements = new ArrayList<String>();
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


	// GET SCRIPT NAME

	public String GetScriptName(String thisScript) {
		if (thisScript.equals("none")) { return thisScript; }
		else {
			String [] thisScriptArray = thisScript.split(" ", 2);
			return thisScriptArray[1]; }
	}


	// CHECK REQUIREMENTS  (Checks if the requirements of a script are met when given Script/Player)

	public boolean CheckRequirements(String thisScript, Player thisPlayer) {

		String RequirementsMode = plugin.getConfig().getString("Scripts." + thisScript + ".Requirements.Mode");
		if (plugin.DebugMode) { plugin.getServer().broadcastMessage("** DEBUG - CheckRequirements called and passed: " + thisScript.toString() + ", " + thisPlayer.toString()); }

		List<String> RequirementsList = plugin.getConfig().getStringList("Scripts." + thisScript + ".Requirements.List");
		if (RequirementsList.isEmpty()) { 				
			if (plugin.DebugMode) { plugin.getServer().broadcastMessage("** Debug - No requirements for " + thisScript + ", passing True." ); }
			return true; }

		if (plugin.DebugMode) { plugin.getServer().broadcastMessage("** DEBUG - Requirements mode for script: " + RequirementsMode.toString()); }

		int NumberOfMetRequirements = 0;

		if (plugin.DebugMode) { plugin.getServer().broadcastMessage("** DEBUG - Requirement list for " + thisScript + ": " + RequirementsList.toString() ); }

		for (String Requirement : RequirementsList) {
			//	None, Time Day, Time Night, Precipitation, No Precipitation, permission, group, level, full, starving, hungry
			String[] RequirementArgs = Requirement.split(" ");

			if (plugin.DebugMode) { plugin.getServer().broadcastMessage("** DEBUG - Checking requirement: " + Requirement.toString() ); }

			if (Requirement.equalsIgnoreCase("none")) { return true; }
			if (Requirement.equalsIgnoreCase("time day") && thisPlayer.getWorld().getTime() < 13500) { if (plugin.DebugMode) { plugin.getServer().broadcastMessage("** DEBUG - Requirement met."); } NumberOfMetRequirements++; }
			if (Requirement.equalsIgnoreCase("time night") && thisPlayer.getWorld().getTime() > 13500) { if (plugin.DebugMode) { plugin.getServer().broadcastMessage("** DEBUG - Requirement met."); } NumberOfMetRequirements++; }
			if (RequirementArgs[0].equalsIgnoreCase("permission") && thisPlayer.hasPermission(RequirementArgs[1]) == true) { if (plugin.DebugMode) { plugin.getServer().broadcastMessage("** DEBUG - Requirement met."); } NumberOfMetRequirements++; }
			if (Requirement.equalsIgnoreCase("precipitation") && thisPlayer.getWorld().hasStorm() == true) { if (plugin.DebugMode) { plugin.getServer().broadcastMessage("** DEBUG - Requirement met."); } NumberOfMetRequirements++; }
			if (Requirement.equalsIgnoreCase("no precipitation") && thisPlayer.getWorld().hasStorm() == false) { if (plugin.DebugMode) { plugin.getServer().broadcastMessage("** DEBUG - Requirement met."); } NumberOfMetRequirements++; }
			if (RequirementArgs[0].equalsIgnoreCase("level") && thisPlayer.getLevel() >= Integer.parseInt(RequirementArgs[1])) { if (plugin.DebugMode) { plugin.getServer().broadcastMessage("** DEBUG - Requirement met."); } NumberOfMetRequirements++; }
			if (Requirement.equalsIgnoreCase("starving") && thisPlayer.getSaturation() == 0) { if (plugin.DebugMode) { plugin.getServer().broadcastMessage("** DEBUG - Requirement met."); } NumberOfMetRequirements++; }
			if (Requirement.equalsIgnoreCase("hungry") && thisPlayer.getSaturation() < 8) { if (plugin.DebugMode) { plugin.getServer().broadcastMessage("** DEBUG - Requirement met."); } NumberOfMetRequirements++; }
			if (Requirement.equalsIgnoreCase("full") && thisPlayer.getSaturation() > 10) { if (plugin.DebugMode) { plugin.getServer().broadcastMessage("** DEBUG - Requirement met."); } NumberOfMetRequirements++; }
			if (RequirementArgs[0].equalsIgnoreCase("world") && thisPlayer.getWorld().getName().equalsIgnoreCase(RequirementArgs[1])) { if (plugin.DebugMode) { plugin.getServer().broadcastMessage("** DEBUG - Requirement met."); } NumberOfMetRequirements++; }		


		}

		if (RequirementsMode.equalsIgnoreCase("all") && NumberOfMetRequirements == RequirementsList.size()) { 
			if (plugin.DebugMode) { plugin.getServer().broadcastMessage("** DEBUG - Requirements met: Mode All"); }
			return true; 
		}
		String[] ModeArgs = RequirementsMode.split(" ");
		if (ModeArgs[0].equalsIgnoreCase("any") && NumberOfMetRequirements >= Integer.parseInt(ModeArgs[1])) { return true;	}
		return false;
	}



}