package net.aufdemrand.denizen.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import net.aufdemrand.denizen.Denizen;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class GetScript {

	
	/*
	 * ConcatenateScripts
	 * 
	 * Combines script files into one YML file for Denizen to read from.
	 * Code borrowed from: http://www.roseindia.net/tutorial/java/core/files/fileconcatenation.html
	 * 
	 * Thank you!
	 * 
	 */


	public void ConcatenateScripts() throws IOException {

		Denizen plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");		
		
		try {
		
		PrintWriter pw = new PrintWriter(new FileOutputStream(plugin.getDataFolder() + File.separator + "read-only-scripts.yml"));
		File file = new File(plugin.getDataFolder() + File.separator + "scripts");
		File[] files = file.listFiles();
		for (int i = 0; i < files.length; i++) {

			String fileName = files[i].getName();
			if (fileName.substring(fileName.length() - 4, fileName.length() - 1).equalsIgnoreCase("YML")) {
			
			System.out.println("Processing " + files[i].getPath() + "... ");
			BufferedReader br = new BufferedReader(new FileReader(files[i]
					.getPath()));
			String line = br.readLine();
			while (line != null) {
				pw.println(line);
				line = br.readLine();
			}
			br.close();
			
			}
		}
		pw.close();
		System.out.println("OK! Scripts loaded!");
		
		} catch (Throwable error) {
			System.out.println("Woah! No scripts to load!");		
		}
		
	}

	
	
	/* 
	 * GetInteractScript
	 *
	 * Requires the Denizen and the Player
	 * Checks the Denizens scripts and returns the script that meets requirements and has
	 * the highest weight.  If no script matches, returns "none".
	 *
	 */

	public String getInteractScript(NPC theDenizen, Player thePlayer) {

		Denizen plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");		

		String theScript = "none";
		List<String> scriptList = plugin.getConfig().getStringList("Denizens." + theDenizen.getName() + ".Interact Scripts");
		if (scriptList.isEmpty()) { return theScript; }
		List<String> interactScripts = new ArrayList<String>();

		/*
		 *  Get scripts that meet requirements
		 */

		LivingEntity theEntity = null;
		Boolean isPlayer = false;
		if (thePlayer != null) {
			theEntity = (LivingEntity) thePlayer;
			isPlayer = true;
		}
		else theEntity = theDenizen.getBukkitEntity();
		
		for (String thisScript : scriptList) {
			String [] thisScriptArray = thisScript.split(" ", 2);
			if (Denizen.getRequirements.check(thisScriptArray[1], theEntity, isPlayer)) interactScripts.add(thisScript);
		}

		/*
		 *  Get highest scoring script
		 */

		if (interactScripts.size() > 1) {
			int ScriptPriority = -1; // The number to beat
			for (String thisScript : interactScripts) {
				String [] thisScriptArray = thisScript.split(" ", 2);
				if (Integer.parseInt(thisScriptArray[0]) > ScriptPriority) {
					ScriptPriority = Integer.parseInt(thisScriptArray[0]); theScript = thisScript; }
			}
		}
		else if (interactScripts.size() == 1) theScript = interactScripts.get(0);

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

	public boolean getScriptCompletes(Player thePlayer, String theScript, String theAmount, boolean negativeRequirement) {

		Plugin plugin = Bukkit.getPluginManager().getPlugin("Denizen");		
		boolean outcome = false;

		/*
		 * (-)FINISHED (#) [Name of Script]
		 */

		try {

			if (Character.isDigit(theAmount.charAt(0))) theScript = theScript.split(" ", 2)[0];
			else theAmount = "1";
			
			if (plugin.getConfig().getString("Players." + thePlayer.getName() + "." + theScript + "." + "Completed") != null) { 
				if (plugin.getConfig().getInt("Players." + thePlayer.getName() + "." + theScript + "." + "Completed") >= Integer.valueOf(theAmount)) outcome = true;
			}
			
		} catch(Throwable error) {
			Bukkit.getLogger().info("Denizen: An error has occured.");
			Bukkit.getLogger().info("--- Error follows: " + error);
		}

		if (negativeRequirement != outcome) return true;

		return false;
	}

	
	public boolean getScriptFail(Player thePlayer, String theScript, boolean negativeRequirement) {

		Plugin plugin = Bukkit.getPluginManager().getPlugin("Denizen");		

		boolean outcome = false;
		
		if (plugin.getConfig().getString("Players." + thePlayer.getName() + "." + theScript + "." + "Failed") != null) { 
			if (plugin.getConfig().getBoolean("Players." + thePlayer.getName() + "." + theScript + "." + "Failed") == true) outcome = true;
		}

		if (negativeRequirement != outcome) return true;
		
		return false;
		
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
