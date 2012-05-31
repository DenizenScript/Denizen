package net.aufdemrand.denizen.utilities;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.aufdemrand.denizen.Denizen;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class GetScript extends JavaPlugin {

	Plugin plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");	
	GetRequirements getRequirements = new GetRequirements();
	

	private FileConfiguration customConfig = null;
	private File customConfigFile = null;

	
	
	/* 
	 * GetInteractScript
	 *
	 * Requires the Denizen and the Player
	 * Checks the Denizens scripts and returns the script that meets requirements and has
	 * the highest weight.  If no script matches, returns "none".
	 *
	 */

	public String getInteractScript(NPC thisDenizen, Player thisPlayer) {
		String theScript = "none";
		List<String> ScriptList = plugin.getConfig().getStringList("Denizens." + thisDenizen.getName() + ".Interact Scripts");
		if (ScriptList.isEmpty()) { return theScript; }
		List<String> ScriptsThatMeetRequirements = new ArrayList<String>();

		/*
		 *  Get scripts that meet requirements
		 */
		
		for (String thisScript : ScriptList) {
			String [] thisScriptArray = thisScript.split(" ", 2);
			if (getRequirements.check(thisScriptArray[1], thisPlayer)) ScriptsThatMeetRequirements.add(thisScript);
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
		plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");
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
		boolean ScriptComplete = false;
		if (plugin.getConfig().getString("Players." + thePlayer.getName() + "." + theScript + "." + "Completed") != null) { 
			if (plugin.getConfig().getBoolean("Players." + thePlayer.getName() + "." + theScript + "." + "Completed") == true) ScriptComplete = true;
		}

		return ScriptComplete;
	}

	public boolean getScriptFail(Player thePlayer, String theScript) {
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
		List<String> ChatTriggers = new ArrayList<String>();
		int currentTrigger = 1;
		for (int x=1; currentTrigger >= 0; x++) {
			String theChatTrigger = getScripts().getString("" + theScript + ".Steps."
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
		plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");
		if (thisScript.equals("none")) { return thisScript; }
		else {
			String [] thisScriptArray = thisScript.split(" ", 2);
			return thisScriptArray[1]; }
	}


	
	
	/*
	 * reloadScripts/getScripts
	 * 
	 * Reloads and retrieves information from the Denizen/scripts.yml.
	 * 
	 */
	
	
	public void reloadScripts() {
		if (customConfigFile == null) {
			customConfigFile = new File(getDataFolder(), "scripts.yml");
		}
		customConfig = YamlConfiguration.loadConfiguration(customConfigFile);

		// Look for defaults in the jar
		InputStream defConfigStream = getResource("scripts.yml");
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			customConfig.setDefaults(defConfig);
		}
	}

	public FileConfiguration getScripts() {
		if (customConfig == null) {
			reloadScripts();
		}
		return customConfig;
	}

	
	
}
