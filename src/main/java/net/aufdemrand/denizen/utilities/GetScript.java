package net.aufdemrand.denizen.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import net.aufdemrand.denizen.Denizen;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class GetScript {

	private Denizen plugin;

	public GetScript(Denizen denizen) {
		plugin = denizen;
	}
	


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

		try {

			PrintWriter pw = new PrintWriter(new FileOutputStream(plugin.getDataFolder() + File.separator + "read-only-scripts.yml"));
			File file = new File(plugin.getDataFolder() + File.separator + "scripts");
			File[] files = file.listFiles();
			for (int i = 0; i < files.length; i++) {

				String fileName = files[i].getName();
				if (fileName.substring(fileName.lastIndexOf('.') + 1).equalsIgnoreCase("YML")) {

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

		String theScript = "none";
		List<String> scriptList = plugin.getAssignments().getStringList("Denizens." + theDenizen.getName() + ".Interact Scripts");
		if (scriptList.isEmpty()) { 
			if (plugin.DebugMode) plugin.getLogger().log(Level.INFO, "getInteractScript: scriptList = empty");
			return theScript; 
			}
		
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
			if (plugin.getRequirements.check(thisScriptArray[1], theEntity, isPlayer)) {
				if (plugin.DebugMode) plugin.getLogger().log(Level.INFO, "getInteractScript: Script " + thisScript + " meets requirements, adding to list.");
				interactScripts.add(thisScript.split(" ", 2)[1]);
			}
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

		int currentStep = 1;
		if (plugin.getSaves().getString("Players." + thePlayer.getName() + "." + theScript + "." + "Current Step") != null)
			currentStep =  plugin.getSaves().getInt("Players." + thePlayer.getName() + "." + theScript	+ "." + "Current Step");

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

		boolean outcome = false;

		/*
		 * (-)FINISHED (#) [Name of Script]
		 */

		try {

			if (Character.isDigit(theAmount.charAt(0))) theScript = theScript.split(" ", 2)[1];
			else theAmount = "1";

			if (plugin.getSaves().getString("Players." + thePlayer.getName() + "." + theScript + "." + "Completed") != null) { 
				if (plugin.getSaves().getInt("Players." + thePlayer.getName() + "." + theScript + "." + "Completed", 0) >= Integer.valueOf(theAmount)) outcome = true;
			}

		} catch(Throwable error) {
			Bukkit.getLogger().info("Denizen: An error has occured with the FINISHED requirement.");
			Bukkit.getLogger().info("Error follows: " + error);
		}

		if (negativeRequirement != outcome) return true;

		return false;
	}


	public boolean getScriptFail(Player thePlayer, String theScript, boolean negativeRequirement) {

		boolean outcome = false;

		if (plugin.getSaves().getString("Players." + thePlayer.getName() + "." + theScript + "." + "Failed") != null) { 
			if (plugin.getSaves().getBoolean("Players." + thePlayer.getName() + "." + theScript + "." + "Failed") == true) outcome = true;
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

		List<String> ChatTriggers = new ArrayList<String>();
		int currentTrigger = 1;
		for (int x=1; currentTrigger >= 0; x++) {
			String theChatTrigger = plugin.getScripts().getString(theScript + ".Steps."
					+ currentStep + ".Chat Trigger." + String.valueOf(currentTrigger) + ".Trigger");
			if (theChatTrigger != null) { 
				boolean isTrigger = false;
				String triggerBuilder = "";
				
				for (String trigger : theChatTrigger.split("/")) {
					if (isTrigger) {
						triggerBuilder = triggerBuilder + trigger + ":";
						isTrigger = false;
					}
					else isTrigger = true;
				}
				
				/* Take off excess ":" before adding it to the list */
				triggerBuilder = triggerBuilder.substring(0, triggerBuilder.length() - 1);
				
				ChatTriggers.add(triggerBuilder);
				
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





	public boolean zap(Player thePlayer, String theScript, String theStep, String newStep) {

		if (newStep == null) {
			plugin.getSaves().set("Players." + thePlayer.getName() + "." + theScript + ".Current Step", Integer.parseInt(theStep) + 1);
			plugin.saveSaves();
		}
		else { 
			plugin.getSaves().set("Players." + thePlayer.getName() + "." + theScript + ".Current Step", Integer.parseInt(newStep)); 
			plugin.saveSaves();
		}

		return true;
	}
}
