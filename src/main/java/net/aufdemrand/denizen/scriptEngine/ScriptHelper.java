package net.aufdemrand.denizen.scriptEngine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.bookmarks.Bookmarks.BookmarkType;
import net.aufdemrand.denizen.command.core.EngageCommand;
import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.npc.DenizenTrait;
import net.aufdemrand.denizen.scriptEngine.ScriptEngine.QueueType;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.command.exception.RequirementMissingException;

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
	 * Code mostly borrowed from: http://www.roseindia.net/tutorial/java/core/files/fileconcatenation.html
	 * 
	 * Thanks!
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



	/*
	 * Checks cooldowns/set cooldowns.
	 */

	Map<DenizenNPC, Map<Class<?>, Long>> triggerCooldowns = new ConcurrentHashMap<DenizenNPC, Map<Class<?>,Long>>();

	public boolean checkCooldown(DenizenNPC theDenizen, Class<?> theTrigger) {

		if (!triggerCooldowns.containsKey(theDenizen)) return true;
		if (!triggerCooldowns.get(theDenizen).containsKey(theTrigger)) return true;
		if (System.currentTimeMillis() >= triggerCooldowns.get(theDenizen).get(theTrigger)) return true;

		return false;
	}

	public boolean checkCooldown(Player thePlayer, String theScript) {

		if (!plugin.getSaves().contains("Players." + thePlayer.getName() + "." + theScript + ".Cooldown Time")) return true;
		if (System.currentTimeMillis() >= plugin.getSaves().getLong("Players." + thePlayer.getName() + "." + theScript + ".Cooldown Time"))	return true;

		return false;
	}

	public void setCooldown(DenizenNPC theDenizen, Class<?> triggerType, Long millis) {
		Map<Class<?>, Long> triggerMap = new HashMap<Class<?>, Long>();
		triggerMap.put(triggerType, System.currentTimeMillis() + millis);
		triggerCooldowns.put(theDenizen, triggerMap);
	}

	public void setCooldown(Player thePlayer, String theScript, Long millis) {
		plugin.getSaves().set("Players." + thePlayer.getName() + "." + theScript + ".Cooldown Time", System.currentTimeMillis() + millis);
	}



	/* 
	 * Denizen Info
	 */

	public void showInfo(Player thePlayer, DenizenNPC theDenizen) {

		thePlayer.sendMessage(ChatColor.GOLD + "------ Denizen Info ------");

		/* Show Citizens NPC info. */

		thePlayer.sendMessage(ChatColor.GRAY + "C2 NPCID: " + ChatColor.GREEN + theDenizen.getId() + ChatColor.GRAY + "   Name: " + ChatColor.GREEN + theDenizen.getName() + ChatColor.GRAY + "   HPs: " + ChatColor.GREEN + theDenizen.getEntity().getHealth());
		if (plugin.newbMode) thePlayer.sendMessage(ChatColor.GRAY + "Tip: Use " + ChatColor.WHITE + "/denizen setname" + ChatColor.GRAY + " to change the Denizen's name.");
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
		if (plugin.newbMode) thePlayer.sendMessage(ChatColor.GRAY + "Turn on precision mode with " + ChatColor.WHITE + "/denizen precision" + ChatColor.GRAY + " to assign to Id.");
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



	/* 
	 *  GetInteractScript
	 *
	 *  Requires the Denizen and the Player
	 *  Checks the Denizens scripts and returns the script that meets requirements and has
	 *  the highest weight.  If no script matches, returns "none".
	 *
	 */

	public String getInteractScript(NPC theDenizen, Player thePlayer) {

		if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "Getting interact script.");

		String theScript = null;
		List<String> scriptList = plugin.getAssignments().getStringList("Denizens." + theDenizen.getName() + ".Interact Scripts");
		if (scriptList.isEmpty()) { 
			if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...no interact scripts found!");
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

			try {
			if (plugin.getRequirements.check(thisScriptArray[1], theEntity, isPlayer)) {
				if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "..." + thisScript + " meets requirements.");
				interactScripts.add(thisScript);
			} else {
				if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "..." + thisScript + " does not meet requirements.");
			}
			} catch (RequirementMissingException e) {
				if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "..." + thisScript + " had a bad requirement, skipping.");
				if (plugin.showStackTraces) e.printStackTrace();
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

		if (interactScripts.size() >= 1) {
		if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...highest scoring script is " + theScript.split(" ", 2)[1] + ".");
		return theScript.split(" ", 2)[1];
		}
		
		else {
			if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...no interact scripts found!");
			return null;
		}
	}



	public int getCurrentStep(Player thePlayer, String theScript) {

		if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "Finding current step for " + theScript + ".");

		int currentStep = 1;
		if (plugin.getSaves().getString("Players." + thePlayer.getName() + "." + theScript + "." + "Current Step") != null) {
			currentStep =  plugin.getSaves().getInt("Players." + thePlayer.getName() + "." + theScript	+ "." + "Current Step");
			if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...found info in saves.yml. Current step is: " + currentStep + ".");
			return currentStep;
		}

		else if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...no step found in saves.yml! Assuming '1'.");
		return currentStep;
	}



	/* Builds arguments array, recognizing items in quotes as a single item 
	 * 
	 * Thanks to Jan Goyvaerts from 
	 * http://stackoverflow.com/questions/366202/regex-for-splitting-a-string-using-space-when-not-surrounded-by-single-or-double
	 * as this is pretty much a copy/paste.
	 */

	public String[] buildArgs(String stringArgs) {

		if (stringArgs == null) return null;

		List<String> matchList = new ArrayList<String>();
		Pattern regex = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");
		Matcher regexMatcher = regex.matcher(stringArgs);
		while (regexMatcher.find()) {
			if (regexMatcher.group(1) != null) {
				// Add double-quoted string without the quotes
				matchList.add(regexMatcher.group(1));
			} else if (regexMatcher.group(2) != null) {
				// Add single-quoted string without the quotes
				matchList.add(regexMatcher.group(2));
			} else {
				// Add unquoted word
				matchList.add(regexMatcher.group());
			}
		} 
		String[] split = new String[matchList.size()];
		matchList.toArray(split);

		if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "   ...built arguments: " + Arrays.toString(split));

		return split;
	}



	/*
	 * Methods to help get String script entries from a YAML script.
	 */
	public String scriptString = ".Script";

	public String getTriggerPath(String theScript, int theStep,	String triggerName) {
		return theScript + ".Steps." + theStep + "." +  triggerName + " Trigger.";
	}

	public List<String> getScript(String triggerPath) {
		if (plugin.getScripts().contains(triggerPath.replace("..", "."))) {
			return plugin.getScripts().getStringList(triggerPath.replace("..", "."));
		}

		else return null;
	}



	/*
	 * Checks whether a Denizen NPC should be interact-able.
	 */

	public boolean denizenIsInteractable(String triggerName, DenizenNPC theDenizen) {

		// NPC must be a Denizen

		if (theDenizen.getCitizensEntity().hasTrait(DenizenTrait.class))
			if (theDenizen.getCitizensEntity().getTrait(DenizenTrait.class).isToggled()
					// The Denizen NPC must have the trigger enabled
					&& theDenizen.getCitizensEntity().getTrait(DenizenTrait.class).triggerIsEnabled(triggerName.toUpperCase())
					// The Player must be cooled down for this type of Trigger
					&& plugin.getScriptEngine().helper.checkCooldown(theDenizen, plugin.getTriggerRegistry().getTrigger(triggerName).getClass())
					// and finally the NPC must not be engaged
					&& !plugin.getCommandRegistry().getCommand(EngageCommand.class).getEngaged(theDenizen.getCitizensEntity()))
				return true;

		/* For debugging */

		if (plugin.debugMode) {
			plugin.getLogger().log(Level.INFO, theDenizen.getName() + " is not interactable.");

			if (!theDenizen.getCitizensEntity().hasTrait(DenizenTrait.class)) plugin.getLogger().log(Level.INFO, "...no Denizen Trait.");
			if (!theDenizen.getCitizensEntity().getTrait(DenizenTrait.class).triggerIsEnabled(triggerName.toUpperCase())) plugin.getLogger().log(Level.INFO, "..." + triggerName.toLowerCase() + " trigger is not enabled on this NPC.");
			if (!plugin.getScriptEngine().helper.checkCooldown(theDenizen, plugin.getTriggerRegistry().getTrigger(triggerName).getClass())) plugin.getLogger().log(Level.INFO, "...the Player has not yet met cool-down.");
			if (plugin.getCommandRegistry().getCommand(EngageCommand.class).getEngaged(theDenizen.getCitizensEntity())) plugin.getLogger().log(Level.INFO, "...the Denizen is ENGAGED.");
		}

		return false;
	}



	/* 
	 * Builds a list of ScriptEntry(ies) from a List<String> of items read from a script. 
	 */

	public List<ScriptEntry> buildScriptEntries(Player thePlayer, DenizenNPC theDenizen, List<String> theScript, String theScriptName, Integer theStep) {

		if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "Building Script Entries...");

		if (theScript == null) {
			if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...no entries to build!");
			return null;
		}

		if (theScript.isEmpty()) {
			if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...no entries to build!");
			return null;
		}

		List<ScriptEntry> scriptCommands = new ArrayList<ScriptEntry>();

		for (String thisItem : theScript) {
			String[] scriptEntry = new String[2];
			if (thisItem.split(" ", 2).length == 1) {
				scriptEntry[0] = thisItem;
				scriptEntry[1] = null;
			} else {
				scriptEntry = thisItem.split(" ", 2);
			}

			try {
				/* Build new script commands */
				if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...building " + scriptEntry[0]);
				scriptCommands.add(new ScriptEntry(scriptEntry[0], buildArgs(scriptEntry[1]), thePlayer, theDenizen, theScriptName, theStep));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return scriptCommands;

	}


	public List<ScriptEntry> buildScriptEntries(Player thePlayer, DenizenNPC theDenizen, List<String> theScript, String theScriptName, Integer theStep, String playerMessage, String theText) {

		List<ScriptEntry> scriptCommands = new ArrayList<ScriptEntry>();

		if (theScript == null) {
			if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...no entries to build!");
			return null;
		}

		if (theScript.isEmpty()) {
			if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...no entries to build!");
			return null;
		}

		for (String thisItem : theScript) {
			String[] scriptEntry = new String[2];
			if (thisItem.split(" ", 2).length == 1) {
				scriptEntry[0] = thisItem;
				scriptEntry[1] = null;
			} else {
				scriptEntry = thisItem.split(" ", 2);
			}

			try {
				/* Build new script commands */
				scriptCommands.add(new ScriptEntry(scriptEntry[0], buildArgs(scriptEntry[1]), thePlayer, theDenizen, theScriptName, theStep, playerMessage, theText));
				if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "Building ScriptCommand with " + thisItem);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return scriptCommands;

	}



	public void queueScriptEntries(Player thePlayer, List<ScriptEntry> scriptEntries, QueueType queueType) {

		if (scriptEntries == null) {
			if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...no entries to queue!");
			return;
		}

		if (scriptEntries.isEmpty()) {
			if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...no entries to queue!");
			return;
		}

		Map<Player, List<ScriptEntry>> thisQueue = plugin.getScriptEngine().getQueue(queueType);
		List<ScriptEntry> existingScriptEntries = new ArrayList<ScriptEntry>();

		if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "Queueing ScriptEntries...");

		if (thisQueue.containsKey(thePlayer))
			existingScriptEntries.addAll(thisQueue.get(thePlayer));

		/* Keeps the commandQue from removing items while
		working on them here. They will be added back in. */ 
		thisQueue.remove(thePlayer); 

		if (!scriptEntries.isEmpty())
			existingScriptEntries.addAll(scriptEntries);
		else
			if (plugin.debugMode) plugin.getLogger().log(Level.SEVERE, "...no items to add!");

		thisQueue.put(thePlayer, existingScriptEntries);
		if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...success!");

	}


}
