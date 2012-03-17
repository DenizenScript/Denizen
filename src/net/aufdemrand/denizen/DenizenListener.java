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
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

public class DenizenListener implements Listener {

	Denizen plugin;
	public DenizenListener(Denizen instance) { plugin = instance; }
	
	DenizenParser parser;
	public DenizenListener(DenizenParser instance) { parser = instance; }
		

	@EventHandler
	public void PlayerChatListener(PlayerChatEvent event) {

		List<net.citizensnpcs.api.npc.NPC> DenizenList = GetDenizensWithinRange(event.getPlayer().getLocation(), event.getPlayer().getWorld(), plugin.PlayerChatRangeInBlocks);
		
		event.getPlayer().sendMessage(DenizenList.toString());
		
		if (DenizenList.isEmpty()) { return; }

		for (net.citizensnpcs.api.npc.NPC thisDenizen : DenizenList) {

			TalkToNPC(thisDenizen, event.getPlayer(), event.getMessage());
			
			String theScript = GetInteractScript(thisDenizen, event.getPlayer());
			if (theScript.isEmpty()) { 

				// Let's parse the script!
				
				ParseScript(event.getMessage(), event.getPlayer(), theScript, "Chat");
				
			}
		}
	}

	
	public void TalkToNPC(net.citizensnpcs.api.npc.NPC theDenizen, Player thePlayer, String theMessage)
	{
		thePlayer.sendMessage(plugin.TalkToNPCString.replace("<NPC>", theDenizen.toString()).replace("<TEXT>", theMessage));
	}
	
	
	
	public List<net.citizensnpcs.api.npc.NPC> GetDenizensWithinRange (Location PlayerLocation, World PlayerWorld, int Distance) {

		List<net.citizensnpcs.api.npc.NPC> DenizensWithinRange = new ArrayList<net.citizensnpcs.api.npc.NPC>();
		
		plugin.getServer().broadcastMessage(DenizensWithinRange.toString());
		
		Collection<net.citizensnpcs.api.npc.NPC> DenizenNPCs = CitizensAPI.getNPCManager().getNPCs(DenizenCharacter.class); 
		if (DenizenNPCs.isEmpty()) { return DenizensWithinRange; }
		List<net.citizensnpcs.api.npc.NPC> DenizenList = new ArrayList(DenizenNPCs);
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

		if (InteractionType.equalsIgnoreCase("Chat"))
		{
			int CurrentStep = GetCurrentStep(thePlayer, theScript);
			List<String> ChatTriggerList = GetChatTriggers(theScript, CurrentStep);
			
			// get triggers
			for (int l=0; l < ChatTriggerList.size(); l++ ) {
				if (theMessage.equals(ChatTriggerList.get(l))) {
					TriggerChat(theScript, CurrentStep, l);
				}
			}
			
			// match triggers
			// send script
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
	
	
	public void TriggerChat(String theScript, int CurrentStep, int ChatTrigger) {
		
		
		
	}
	
	
		
	// GET CURRENT STEP  (Gets the player's current step on a script when given Player and Script)
	
	public int GetCurrentStep(Player thePlayer, String theScript) {
		
		int currentStep = 0;
		if (!plugin.getConfig().getString(thePlayer + "." + theScript + "." + "CurrentStep").isEmpty()) 
		{ 
			currentStep =  plugin.getConfig().getInt(thePlayer + "." + theScript + "." + "CurrentStep"); 
		}

		return currentStep;
		
	}
	
	
	
	// GET CURRENT STEP CHAT TRIGGERS
	
	public List<String> GetChatTriggers(String theScript, Integer currentStep) {
		
		List<String> ChatTriggers = new ArrayList<String>();
		
		String currentTrigger = "0";
		
		// Add triggers to list
		for (int x=0; currentTrigger.isEmpty(); x++) {
			currentTrigger = "";
			List<String> theTrigger = plugin.getConfig().getStringList("Scripts." + theScript + ".Progression." + currentStep + ".Interact.Chat Trigger." + currentTrigger);
            if (theTrigger.get(0).length() > 0) {ChatTriggers.add(theTrigger.get(0)); currentTrigger = String.valueOf(x + 1); 
            }
		}
		
		return ChatTriggers;
		
		
		
	}
	
	
	
	// GET SCRIPT  (Gets the script to interact with when given Player/Denizen)
	
	public String GetInteractScript(net.citizensnpcs.api.npc.NPC thisDenizen, Player thisPlayer) {
		String theScript = "";
		List<String> ScriptList = plugin.getConfig().getStringList("Denizens." + thisDenizen.getId() + ".Scripts");
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
		else { theScript = ScriptsThatMeetRequirements.get(0); }
		
		return theScript;
	}


	
	// CHECK REQUIREMENTS  (Checks if the requirements of a script are met when given Script/Player)

	public boolean CheckRequirements(String thisScript, Player thisPlayer) {

		String RequirementsMode = plugin.getConfig().getString("Scripts." + thisScript + ".Requirements.Mode");
		List<String> RequirementsList = plugin.getConfig().getStringList("Scripts." + thisScript + ".Requirements.List");
		if (RequirementsList.isEmpty()) { return true; }

		int NumberOfMetRequirements = 0;

		for (String Requirement : RequirementsList) {
			//	None, Time Day, Time Night, Precipitation, No Precipitation, permission, group, level, full, starving, hungry
			String[] RequirementArgs = Requirement.split(" ");
			if (Requirement.equalsIgnoreCase("none")) { return true; }
			if (Requirement.equalsIgnoreCase("time day") && thisPlayer.getWorld().getTime() < 13500) { NumberOfMetRequirements++; }
			if (Requirement.equalsIgnoreCase("time night") && thisPlayer.getWorld().getTime() > 13500) { NumberOfMetRequirements++; }
			if (RequirementArgs[0].equalsIgnoreCase("permission") && thisPlayer.hasPermission(RequirementArgs[1]) == true) { NumberOfMetRequirements++; }
			if (Requirement.equalsIgnoreCase("precipitation") && thisPlayer.getWorld().hasStorm() == true) { NumberOfMetRequirements++; }
			if (Requirement.equalsIgnoreCase("no precipitation") && thisPlayer.getWorld().hasStorm() == false) { NumberOfMetRequirements++; }
			if (RequirementArgs[0].equalsIgnoreCase("level") && thisPlayer.getLevel() >= Integer.parseInt(RequirementArgs[1])) { NumberOfMetRequirements++; }
			if (Requirement.equalsIgnoreCase("starving") && thisPlayer.getSaturation() == 0) { NumberOfMetRequirements++; }
			if (Requirement.equalsIgnoreCase("hungry") && thisPlayer.getSaturation() < 8) { NumberOfMetRequirements++; }
			if (Requirement.equalsIgnoreCase("full") && thisPlayer.getSaturation() > 10) { NumberOfMetRequirements++; }
			if (RequirementArgs[0].equalsIgnoreCase("world") && thisPlayer.getWorld().getName()  == RequirementArgs[1]) { NumberOfMetRequirements++; }
		}

		if (RequirementsMode.equalsIgnoreCase("all") && NumberOfMetRequirements == RequirementsList.size()) { return true; }

		String[] ModeArgs = RequirementsMode.split(" ");

		if (ModeArgs[0].equalsIgnoreCase("any") && NumberOfMetRequirements >= Integer.parseInt(ModeArgs[1])) { return true;	}

		return false;
	}
	
	
	
}