package net.aufdemrand.denizen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class DenizenParser {

	Denizen plugin;
	public DenizenParser(Denizen instance) { plugin = instance; }
	
	
	
	
	// PARSE SCRIPT
	public void ParseScript(Player thePlayer, String theScript, String InteractionType) {

		if (InteractionType.equalsIgnoreCase("Chat"))
		{
			
			GetCurrentStep(thePlayer, theScript);
			
			// get triggers
			
			
			
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
	
	
	
	// GET CURRENT STEP  (Gets the player's current step on a script when given Player and Script)
	
	public int GetCurrentStep(Player thePlayer, String theScript) {
		
		int currentStep = 0;
		if (plugin.getConfig().getString(thePlayer + "." + theScript + "." + "CurrentStep") != null ) 
		{ 
			currentStep =  plugin.getConfig().getInt(thePlayer + "." + theScript + "." + "CurrentStep"); 
		}

		return currentStep;
		
	}
	
	
	
	// GET CURRENT STEP CHAT TRIGGERS
	
	public List<String> GetChatTriggers(String theScript, Integer currentStep) {
		
		List<String> ChatTriggers = null;
		
		String currentTrigger = "0";
		
		// Add triggers to list
		for (int x=0; currentTrigger.isEmpty(); x++) {
			currentTrigger = null;
			plugin.getConfig().getStringList("Scripts." + theScript + ".Progression." + currentStep + ".Interact.Chat Trigger." + currentTrigger);
			
		}
		
		
		
	}
	
	
	
	// GET SCRIPT  (Gets the script to interact with when given Player/Denizen)
	
	public String GetInteractScript(net.citizensnpcs.api.npc.NPC thisDenizen, Player thisPlayer) {
		String theScript = null;
		List<String> ScriptList = plugin.getConfig().getStringList("Denizens." + thisDenizen.getId() + ".Scripts");
		if (ScriptList.isEmpty()) { return null; }
		List<String> ScriptsThatMeetRequirements = null;

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



	// GET DENIZENS WITHIN RANGE  (Returns a list of Denizens in a specific location when given Location, World and Distance)

	public List<net.citizensnpcs.api.npc.NPC> GetDenizensWithinRange (Location PlayerLocation, World PlayerWorld, int Distance) {

		List<net.citizensnpcs.api.npc.NPC> DenizensWithinRange = null;
		Collection<net.citizensnpcs.api.npc.NPC> DenizenNPCs = CitizensAPI.getNPCManager().getNPCs(DenizenCharacter.class); 
		if (DenizenNPCs.isEmpty()) { return null; }
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

	
}
