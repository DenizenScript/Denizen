package net.aufdemrand.denizen.scriptEngine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.DenizenTrait;
import net.aufdemrand.denizen.bookmarks.Bookmarks.BookmarkType;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class ScriptHelper {

	Denizen plugin;
	
	ScriptHelper (Denizen plugin) {
		this.plugin = plugin;
	}
	
	
	/*
	 * ConcatenateScripts
	 * 
	 * Combines script files into one YML file for Denizen to read from.
	 * Code borrowed from: http://www.roseindia.net/tutorial/java/core/files/fileconcatenation.html
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

					plugin.getLogger().log(Level.INFO, "Processing script " + files[i].getPath() + "... ");
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
			plugin.getLogger().log(Level.INFO, "OK! All scripts loaded!");

		} catch (Throwable error) {
			plugin.getLogger().log(Level.WARNING, "Woah! No scripts in /plugins/Denizen/scripts/ to load!");		
		}

	}

	
	public void showInfo(Player thePlayer, NPC theDenizen) {

		if (theDenizen.hasTrait(DenizenTrait.class))
			if (theDenizen.getTrait(DenizenTrait.class).isDenizen) {
				
				thePlayer.sendMessage(ChatColor.GOLD + "------ Denizen Info ------");

				/* Show Citizens NPC info. */

				thePlayer.sendMessage(ChatColor.GRAY + "C2 NPCID: " + ChatColor.GREEN + theDenizen.getId() + ChatColor.GRAY + "   Name: " + ChatColor.GREEN + theDenizen.getName());
				if (plugin.newbMode) thePlayer.sendMessage(ChatColor.GRAY + "Tip: Use " + ChatColor.WHITE + "/denizen setname" + ChatColor.GRAY + " to change the Denizen's name.");
				if (plugin.getSaves().contains("Denizens." + theDenizen.getId() + ".Position.Standing"))
					if (plugin.getSaves().getString("Denizens." + theDenizen.getId() + ".Position.Standing") != null)
						thePlayer.sendMessage(ChatColor.GRAY + "Current standing position: " + ChatColor.GREEN + plugin.getSaves().getString("Denizens." + theDenizen.getId() + ".Position.Standing"));
				thePlayer.sendMessage("");


				if (plugin.newbMode) thePlayer.sendMessage(ChatColor.GRAY + "Key: " + ChatColor.GREEN + "Assigned to Name. " + ChatColor.YELLOW + "Assigned to ID.");

				/* Show Assigned Scripts. */

				boolean scriptsPresent = false;
				thePlayer.sendMessage(ChatColor.GRAY + "Interact Scripts:");
				if (plugin.getAssignments().contains("Denizens." + theDenizen.getName() + ".Interact Scripts")) {
					if (!plugin.getAssignments().getStringList("Denizens." + theDenizen.getName() + ".Interact Scripts").isEmpty()) scriptsPresent = true;
					for (String scriptEntry : plugin.getAssignments().getStringList("Denizens." + theDenizen.getName() + ".Interact Scripts"))
						thePlayer.sendMessage(ChatColor.GRAY + "- " + ChatColor.GREEN + scriptEntry);
				}
				if (plugin.getAssignments().contains("Denizens." + theDenizen.getId() + ".Interact Scripts")) {
					if (!plugin.getAssignments().getStringList("Denizens." + theDenizen.getId() + ".Interact Scripts").isEmpty()) scriptsPresent = true;
					for (String scriptEntry : plugin.getAssignments().getStringList("Denizens." + theDenizen.getId() + ".Interact Scripts"))
						thePlayer.sendMessage(ChatColor.GRAY + "- " + ChatColor.YELLOW + scriptEntry);
				}
				if (!scriptsPresent) thePlayer.sendMessage(ChatColor.RED + "  No scripts assigned!");

				if (plugin.newbMode) thePlayer.sendMessage(ChatColor.GRAY + "Tip: Use " + ChatColor.WHITE + "/denizen assign" + ChatColor.GRAY + " to assign scripts.");
				if (plugin.newbMode) thePlayer.sendMessage(ChatColor.GRAY + "Turn on precision mode with " + ChatColor.WHITE + "/denizen precision" + ChatColor.GRAY + " to to assign to Id.");
				thePlayer.sendMessage("");

				/* Show Bookmarks */

				DecimalFormat lf = new DecimalFormat("###.##");
				boolean bookmarksPresent = false;
				thePlayer.sendMessage(ChatColor.GRAY + "Bookmarks:");

				/* Location Bookmarks */
				if (plugin.getSaves().contains("Denizens." + theDenizen.getName() + ".Bookmarks.Location")) {
					if (!plugin.getSaves().getStringList("Denizens." + theDenizen.getName() + ".Bookmarks.Location").isEmpty()) bookmarksPresent = true;
					for (String bookmarkEntry : plugin.getSaves().getStringList("Denizens." + theDenizen.getName() + ".Bookmarks.Location")) {
						if (bookmarkEntry.split(";").length >= 6) {
							thePlayer.sendMessage(ChatColor.GRAY + "- Type: " + ChatColor.GREEN + "LOCATION " + ChatColor.GRAY + "Name: " + ChatColor.GREEN + bookmarkEntry.split(" ")[0]
									+ ChatColor.GRAY + " in World: " + ChatColor.GREEN + bookmarkEntry.split(" ")[1].split(";")[0]);
							thePlayer.sendMessage(" "
									+ ChatColor.GRAY + "  at X: " + ChatColor.GREEN + lf.format(Double.valueOf(bookmarkEntry.split(";")[1]))
									+ ChatColor.GRAY + " Y: " + ChatColor.GREEN + lf.format(Double.valueOf(bookmarkEntry.split(";")[2]))
									+ ChatColor.GRAY + " Z: " + ChatColor.GREEN + lf.format(Double.valueOf(bookmarkEntry.split(";")[3]))
									+ ChatColor.GRAY + " Pitch: " + ChatColor.GREEN + lf.format(Double.valueOf(bookmarkEntry.split(";")[4]))
									+ ChatColor.GRAY + " Yaw: " + ChatColor.GREEN + lf.format(Double.valueOf(bookmarkEntry.split(";")[5])));
						}
					}
				}

				if (plugin.getSaves().contains("Denizens." + theDenizen.getId() + ".Bookmarks.Location")) {
					if (!plugin.getSaves().getStringList("Denizens." + theDenizen.getId() + ".Bookmarks.Location").isEmpty()) bookmarksPresent = true;
					for (String bookmarkEntry : plugin.getSaves().getStringList("Denizens." + theDenizen.getId() + ".Bookmarks.Location")) {
						if (bookmarkEntry.split(";").length >= 6) {
							thePlayer.sendMessage(ChatColor.GRAY + "- Type: " + ChatColor.YELLOW + "LOCATION " + ChatColor.GRAY + "Name: " + ChatColor.YELLOW + bookmarkEntry.split(" ")[0]
									+ ChatColor.GRAY + " in World: " + ChatColor.YELLOW + bookmarkEntry.split(" ")[1].split(";")[0]);
							thePlayer.sendMessage(" "
									+ ChatColor.GRAY + "  at X: " + ChatColor.YELLOW + lf.format(Double.valueOf(bookmarkEntry.split(";")[1]))
									+ ChatColor.GRAY + " Y: " + ChatColor.YELLOW + lf.format(Double.valueOf(bookmarkEntry.split(";")[2]))
									+ ChatColor.GRAY + " Z: " + ChatColor.YELLOW + lf.format(Double.valueOf(bookmarkEntry.split(";")[3]))
									+ ChatColor.GRAY + " Pitch: " + ChatColor.YELLOW + lf.format(Double.valueOf(bookmarkEntry.split(";")[4]))
									+ ChatColor.GRAY + " Yaw: " + ChatColor.YELLOW + lf.format(Double.valueOf(bookmarkEntry.split(";")[5])));
						}
					}
				}

				/* Block Bookmarks */
				if (plugin.getSaves().contains("Denizens." + theDenizen.getName() + ".Bookmarks.Block")) {
					if (!plugin.getSaves().getStringList("Denizens." + theDenizen.getName() + ".Bookmarks.Block").isEmpty()) bookmarksPresent = true;
					for (String bookmarkEntry : plugin.getSaves().getStringList("Denizens." + theDenizen.getName() + ".Bookmarks.Block")) {
						if (bookmarkEntry.split(";").length >= 4) {
							thePlayer.sendMessage(ChatColor.GRAY + "- Type: " + ChatColor.GREEN + "BLOCK " + ChatColor.GRAY + "Name: " + ChatColor.GREEN + bookmarkEntry.split(" ")[0]
									+ ChatColor.GRAY + " in World: " + ChatColor.GREEN + bookmarkEntry.split(" ")[1].split(";")[0]);
							thePlayer.sendMessage(" "
									+ ChatColor.GRAY + "  at X: " + ChatColor.GREEN + lf.format(Double.valueOf(bookmarkEntry.split(";")[1]))
									+ ChatColor.GRAY + " Y: " + ChatColor.GREEN + lf.format(Double.valueOf(bookmarkEntry.split(";")[2]))
									+ ChatColor.GRAY + " Z: " + ChatColor.GREEN + lf.format(Double.valueOf(bookmarkEntry.split(";")[3]))
									+ ChatColor.GRAY + " Material: " + ChatColor.GREEN + plugin.bookmarks.get(theDenizen, bookmarkEntry.split(" ")[0], BookmarkType.BLOCK).getBlock().getType().toString());
						}
					}
				}

				if (plugin.getSaves().contains("Denizens." + theDenizen.getId() + ".Bookmarks.Block")) {
					if (!plugin.getSaves().getStringList("Denizens." + theDenizen.getId() + ".Bookmarks.Block").isEmpty()) bookmarksPresent = true;
					for (String bookmarkEntry : plugin.getSaves().getStringList("Denizens." + theDenizen.getId() + ".Bookmarks.Block")) {
						if (bookmarkEntry.split(";").length >= 4) {
							thePlayer.sendMessage(ChatColor.GRAY + "- Type: " + ChatColor.YELLOW + "BLOCK " + ChatColor.GRAY + "Name: " + ChatColor.YELLOW + bookmarkEntry.split(" ")[0]
									+ ChatColor.GRAY + " in World: " + ChatColor.GREEN + bookmarkEntry.split(" ")[1].split(";")[0]);
							thePlayer.sendMessage(" "
									+ ChatColor.GRAY + "  at X: " + ChatColor.YELLOW + lf.format(Double.valueOf(bookmarkEntry.split(";")[1]))
									+ ChatColor.GRAY + " Y: " + ChatColor.YELLOW + lf.format(Double.valueOf(bookmarkEntry.split(";")[2]))
									+ ChatColor.GRAY + " Z: " + ChatColor.YELLOW + lf.format(Double.valueOf(bookmarkEntry.split(";")[3]))
									+ ChatColor.GRAY + " Material: " + ChatColor.YELLOW + plugin.bookmarks.get(theDenizen, bookmarkEntry.split(" ")[0], BookmarkType.BLOCK).getBlock().getType().toString());
						}
					}
				}


				if (!bookmarksPresent) thePlayer.sendMessage(ChatColor.RED + "  No bookmarks defined!");

				if (plugin.newbMode) thePlayer.sendMessage(ChatColor.GRAY + "Tip: Use " + ChatColor.WHITE + "/denizen bookmark" + ChatColor.GRAY + " to create bookmarks.");
				if (plugin.newbMode) thePlayer.sendMessage(ChatColor.GRAY + "Turn on precision mode with " + ChatColor.WHITE + "/denizen precision" + ChatColor.GRAY + " to assign to Id.");
				thePlayer.sendMessage("");		
			}
	}
	
	
	
	
	/* Called when a click trigger is sent to a Denizen. Handles fetching of the script. */

	public void parseClickTrigger(NPC theDenizen, Player thePlayer) {

		try {

			/* Get the script to use */
			String theScript = plugin.getScript.getInteractScript(theDenizen, thePlayer);

			/* No script meets requirements, let's let the player know. */
			if (theScript.equals("none")) {
				String noscriptChat = null;
				if (plugin.getAssignments().contains("Denizens." + theDenizen.getName()	+ ".Texts.No Requirements Met")) 
					noscriptChat = plugin.getAssignments().getString("Denizens." + theDenizen.getName()	+ ".Texts.No Requirements Met");
				else noscriptChat = plugin.settings.DefaultNoRequirementsMetText();

				/* Make the Denizen chat to the Player */
				plugin.getDenizen.talkToPlayer(theDenizen, thePlayer, plugin.getDenizen.formatChatText(noscriptChat, "CHAT", thePlayer, theDenizen)[0], null, "CHAT");
			}

			/* Script does match, let's send the script to the parser */
			else if (!theScript.equals("none")) 
				plugin.scriptEngine.parseClickScript(theDenizen, thePlayer, theScript);

		} catch (Exception e) {
			plugin.getLogger().log(Level.SEVERE, "Error processing click event.", e);
		}

		return;
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
			if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "Script is empty!");
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
				if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "Script " + thisScript + " meets requirements, adding to list.");
				interactScripts.add(thisScript);
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

		if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "Highest scoring script is " + theScript.split(" ", 2)[1]);
		
		return theScript.split(" ", 2)[1];
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
				
				if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "Found chat trigger: " + triggerBuilder);
				
				ChatTriggers.add(triggerBuilder);
				
				currentTrigger = x + 1; 
			}
			else currentTrigger = -1;
		}

		return ChatTriggers;
	}

	
}
