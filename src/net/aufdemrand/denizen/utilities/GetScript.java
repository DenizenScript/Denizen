package net.aufdemrand.denizen.utilities;

import java.util.ArrayList;
import java.util.List;

import net.aufdemrand.denizen.Denizen;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class GetScript {

	
	
	
	/* 
	 * GetInteractScript
	 *
	 * Requires the Denizen and the Player
	 * Checks the Denizens scripts and returns the script that meets requirements and has
	 * the highest weight.  If no script matches, returns "none".
	 *
	 */

	public String getInteractScript(NPC thisDenizen, Player thisPlayer) {
		
		Denizen plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");		
		
		String theScript = "none";
		List<String> ScriptList = plugin.getConfig().getStringList("Denizens." + thisDenizen.getName() + ".Interact Scripts");
		if (ScriptList.isEmpty()) { return theScript; }
		List<String> ScriptsThatMeetRequirements = new ArrayList<String>();

		/*
		 *  Get scripts that meet requirements
		 */
		
		for (String thisScript : ScriptList) {
			String [] thisScriptArray = thisScript.split(" ", 2);
			if (Denizen.getRequirements.check(thisScriptArray[1], thisPlayer)) ScriptsThatMeetRequirements.add(thisScript);
		}

		/*
		 *  Get highest scoring script
		 */

		if (ScriptsThatMeetRequirements.size() > 1) {
			int ScriptPriority = -1; // The number to beat
			for (String thisScript : ScriptsThatMeetRequirements) {
				String [] thisScriptArray = thisScript.split(" ", 2);
				if (Integer.parseInt(thisScriptArray[0]) > ScriptPriority) {
					ScriptPriority = Integer.parseInt(thisScriptArray[0]); theScript = thisScript; }
			}
		}
		else if (ScriptsThatMeetRequirements.size() == 1) theScript = ScriptsThatMeetRequirements.get(0);

		return theScript;
	}

	

	/* 
	 * GetCurrentStep
	 *
	 * Requires the Player and the Script.
	 * Reads the config.yml to find the current step that the player is on
	 * for the specified script.
	 *
	 */

	public int getCurrentStep(Player thePlayer, String theScript) {

		Denizen plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");		

		int currentStep = 1;
		if (plugin.getConfig().getString("Players." + thePlayer.getName() + "." + theScript + "." + "Current Step") != null)
			currentStep =  plugin.getConfig().getInt("Players." + thePlayer.getName() + "." + theScript	+ "." + "Current Step");
		
		return currentStep;
	}
	
	
	

	/* 
	 * GetScriptComplete/GetScriptFail
	 *
	 * Requires the Player and the Script.
	 * Reads the config.yml to find if the player has completed or failed the specified script.
	 *
	 */

	public boolean getScriptComplete(Player thePlayer, String theScript) {

		Plugin plugin = Bukkit.getPluginManager().getPlugin("Denizen");		
		
		boolean ScriptComplete = false;
		if (plugin.getConfig().getString("Players." + thePlayer.getName() + "." + theScript + "." + "Completed") != null) { 
			if (plugin.getConfig().getBoolean("Players." + thePlayer.getName() + "." + theScript + "." + "Completed") == true) ScriptComplete = true;
		}

		return ScriptComplete;
	}

	public boolean getScriptFail(Player thePlayer, String theScript) {

		Plugin plugin = Bukkit.getPluginManager().getPlugin("Denizen");		
		
		boolean ScriptFailed = false;
		if (plugin.getConfig().getString("Players." + thePlayer.getName() + "." + theScript + "." + "Failed") != null) { 
			if (plugin.getConfig().getBoolean("Players." + thePlayer.getName() + "." + theScript + "." + "Failed") == true) ScriptFailed = true;
		}

		return ScriptFailed;
	}

	
	
	/* GetChatTriggers
	 *
	 * Requires the Script and the Current Step.
	 * Gets a list of Chat Triggers for the step of the script specified.
	 * Chat Triggers are words required to trigger one of the chat 
	 *
	 * Returns ChatTriggers
	 */

	public List<String> getChatTriggers(String theScript, Integer currentStep) {
		
		Denizen plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");		
		
		List<String> ChatTriggers = new ArrayList<String>();
		int currentTrigger = 1;
		for (int x=1; currentTrigger >= 0; x++) {
			String theChatTrigger = plugin.getScripts().getString("" + theScript + ".Steps."
					+ currentStep + ".Chat Trigger." + String.valueOf(currentTrigger) + ".Trigger");
			if (theChatTrigger != null) { 
				ChatTriggers.add(theChatTrigger.split("/")[1]); 
				currentTrigger = x + 1; 
			}
			else currentTrigger = -1;
		}

		return ChatTriggers;
	}

	

	/* 
	 * GetScriptName
	 *
	 * Requires the raw script entry from the config.
	 * Strips the priority number from the beginning of the script name.
	 *
	 */

	public String getNameFromEntry(String thisScript) {

		if (thisScript.equals("none")) { return thisScript; }
		else {
			String [] thisScriptArray = thisScript.split(" ", 2);
			return thisScriptArray[1]; }
	}


	
	


	
	
}
