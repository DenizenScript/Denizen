package net.aufdemrand.denizen.scripts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.bookmarks.BookmarkHelper.BookmarkType;
import net.aufdemrand.denizen.commands.core.EngageCommand;
import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.npc.DenizenTrait;
import net.aufdemrand.denizen.scripts.ScriptEngine.QueueType;
import net.aufdemrand.denizen.triggers.AbstractTrigger;
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
	 * Checks cooldowns/set cooldowns.
	 */


	Map<DenizenNPC, Map<Class<?>, Long>> triggerCooldowns = new ConcurrentHashMap<DenizenNPC, Map<Class<?>,Long>>();

	// Trigger cooldown
	public boolean checkCooldown(DenizenNPC theDenizen, Class<?> theTrigger) {

		if (!triggerCooldowns.containsKey(theDenizen)) return true;
		if (!triggerCooldowns.get(theDenizen).containsKey(theTrigger)) return true;
		if (System.currentTimeMillis() >= triggerCooldowns.get(theDenizen).get(theTrigger)) return true;

		return false;
	}

	public void setCooldown(DenizenNPC theDenizen, Class<?> triggerType, Long millis) {
		Map<Class<?>, Long> triggerMap = new HashMap<Class<?>, Long>();
		triggerMap.put(triggerType, System.currentTimeMillis() + millis);
		triggerCooldowns.put(theDenizen, triggerMap);
	}

	// Script cooldown
	public boolean checkCooldown(Player thePlayer, String theScript) {

		if (plugin.getSaves().contains("Global.Scripts." + theScript + ".Cooldown Time")) {
			if (System.currentTimeMillis() < plugin.getSaves().getLong("Global.Scripts." + theScript + ".Cooldown Time"))
				return false;
			else plugin.getSaves().set("Global.Scripts." + theScript + ".Cooldown Time", null);
		}

		// If no entry for the script, return true;
		if (!plugin.getSaves().contains("Players." + thePlayer.getName() + ".Scripts." + theScript + ".Cooldown Time")) 
			return true;
		// If there is an entry, check against the time. 
		if (System.currentTimeMillis() >= plugin.getSaves().getLong("Players." + thePlayer.getName() + ".Scripts." + theScript + ".Cooldown Time"))	{
			plugin.getSaves().set("Players." + thePlayer.getName() + ".Scripts." + theScript + ".Cooldown Time", null);
			return true;
		}


		return false;
	}

	public void setooldown(Player thePlayer, String theScript, Long millis) {
		plugin.getSaves().set("Players." + thePlayer.getName() + "." + theScript + ".Cooldown Time", System.currentTimeMillis() + millis);
	}






	/* 
	 *  ScriptEngine helper methods help choose which script Players should be interacting with.
	 *
	 */

	// Gets the InteractScript from a NPC Denizen for a Player and returns the appropriate Script.
	// Returns null if no script found.

	public String getInteractScript(NPC theDenizen, Player thePlayer, Class<? extends AbstractTrigger> theTrigger) {

		String theScript = null;
		List<String> assignedScripts = plugin.getAssignments().getStringList("Denizens." + theDenizen.getName() + ".Interact Scripts");

		if (assignedScripts.isEmpty()) { 
			if (plugin.debugMode) plugin.getLogger().info("Getting interact script... no interact scripts found!");
			return null; 
		}

		if (plugin.debugMode) plugin.getLogger().info("Getting interact script... ");

		/* Get scripts that meet requirements and add them to interactableScripts. */

		List<PriorityPair> interactableScripts = new ArrayList<PriorityPair>();

		LivingEntity theEntity = null;
		Boolean isPlayer = false;
		if (thePlayer != null) {
			theEntity = (LivingEntity) thePlayer;
			isPlayer = true;
		}
		else theEntity = theDenizen.getBukkitEntity();

		for (String scriptAssignment : assignedScripts) {
			Integer priority = Integer.valueOf(scriptAssignment.split(" ", 2)[0]);
			String script = scriptAssignment.split(" ", 2)[1].replace("^", "");

			try {
				if (plugin.getRequirements.check(script, theEntity, isPlayer)) {

					// Meets requirements, but we need to check cooldown, too.
					if (plugin.debugMode) 
						plugin.getLogger().log(Level.INFO, "..." + ChatColor.GREEN + scriptAssignment + ChatColor.WHITE + " meets requirements.");

					if (thePlayer != null) {
						if (checkCooldown(thePlayer, script)) {
							// Cooldown is good, add script!
							interactableScripts.add(new PriorityPair(priority, scriptAssignment.split(" ", 2)[1]));
						} else {
							// Cooldown failed, alert console!
							if (plugin.debugMode) 
								plugin.getLogger().log(Level.INFO, "   ...but, isn't cooled down, yet! Skipping.");
						}

					} else {
						// Entity is not a Player, not sure how to handle this, yet, or if it's even neccesary.
						// Build this in thinking that maybe DenizenNPCs may need to be checked for requirements
						// sometime in the future.
					}
				} else {

					// Does not meet requirements, alert the console!
					if (plugin.debugMode) 
						plugin.getLogger().log(Level.INFO, "..." + ChatColor.YELLOW + scriptAssignment + ChatColor.WHITE + " does not meet requirements.");
				}

			} catch (RequirementMissingException e) {

				// Had a problem checking requirements, most likely a Legacy Requirement with bad
				// syntax. Alert the console!
				if (plugin.debugMode) 
					plugin.getLogger().log(Level.INFO, "..." + ChatColor.RED + scriptAssignment + ChatColor.WHITE + " had a bad requirement, skipping.");
			}

		}

		// If list has only one entry, this is it!
		if (interactableScripts.size() == 1) {
			theScript = interactableScripts.get(0).name;
			if (plugin.debugMode) 
				plugin.getLogger().info("...highest scoring script is " + theScript + ".");

			return theScript;
		}

		// Or, if list is empty.. uh oh!
		else if (interactableScripts.isEmpty()) {
			if (plugin.debugMode) 
				plugin.getLogger().info("...no interact scripts found!");
			return null;
		}

		// If we have more than 2 script, let's sort the list from lowest to highest scoring script.
		else Collections.sort(interactableScripts);

		// Let's find which script to return since there are multiple.
		for (int a = interactableScripts.size() - 1; a > 0; a--) {

			// Check for Overlay Assignment...
			if (interactableScripts.get(a).name.startsWith("^")) {

				// This is an Overlay Assignment, check for the appropriate Trigger Script...
				String theScriptName = interactableScripts.get(a).name.substring(1);
				String triggerString = String.valueOf(plugin.getTriggerRegistry().getTrigger(theTrigger).triggerName.charAt(0)).toUpperCase() + plugin.getTriggerRegistry().getTrigger(theTrigger).triggerName.substring(1).toLowerCase() + " Trigger"; 

				// If Trigger exists, cool, this is our script.
				if (plugin.getScripts().contains(theScriptName + ".Steps." + getCurrentStep(thePlayer, theScriptName) + "." + triggerString + ".Script")) {
					if (plugin.debugMode) plugin.getLogger().info("...highest scoring script is " + theScriptName + ".");
					return theScriptName;
				}
				else {
					if (plugin.debugMode) plugin.getLogger().info("...no trigger, next script!");
					// Trigger does not exist. Next script!
				}
			}

			// Not an Overlay Assignment, so return this script, which is the higest scoring.
			else { 
				if (plugin.debugMode) 
					plugin.getLogger().info("...highest scoring script is " + interactableScripts.get(a).name + ".");
				
				return interactableScripts.get(a).name.replace("^", "");
			}
		}

		// If we got here, something is wrong.
		if (plugin.debugMode) 
			plugin.getLogger().info("...no interact scripts found! (There may have been a problem!)");
		return null;
	}

	// Gets the current step from saves.yml

	public int getCurrentStep(Player thePlayer, String theScript) {
		int currentStep = 1;
		if (plugin.getSaves().getString("Players." + thePlayer.getName() + "." + theScript + "." + "Current Step") != null) {
			currentStep =  plugin.getSaves().getInt("Players." + thePlayer.getName() + "." + theScript	+ "." + "Current Step");
			if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...finding current step... found info in saves.yml. Current step is: " + currentStep + ".");
			return currentStep;
		}

		else if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...finding current step... no step found in saves.yml! Assuming '1'.");
		return currentStep;
	}



	/* 
	 * Builds arguments array, recognizing items in quotes as a single item 
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

		List<String> scriptList = new ArrayList<String>();

		if (plugin.getScripts().contains(triggerPath.replace("..", "."))) {
			scriptList = plugin.getScripts().getStringList(triggerPath.replace("..", "."));
		}

		if (scriptList.isEmpty())
			if (plugin.debugMode) plugin.getLogger().info("...could not find script @ " + triggerPath.replace("..", ".") + "... is something spelled wrong in your script?");

		return scriptList;
	}



	/*
	 * Checks whether a Denizen NPC should be interact-able.
	 */

	public boolean denizenIsInteractable(String triggerName, DenizenNPC theDenizen) {

		// NPC must be a Denizen

		if (theDenizen.getCitizensEntity().getTrait(DenizenTrait.class).isToggled()
				// The Player must be cooled down for this type of Trigger
				&& plugin.getScriptEngine().helper.checkCooldown(theDenizen, plugin.getTriggerRegistry().getTrigger(triggerName).getClass())
				// and finally the NPC must not be engaged
				&& !plugin.getCommandRegistry().getCommand(EngageCommand.class).getEngaged(theDenizen.getCitizensEntity()))
			return true;

		/* For debugging */

		if (plugin.debugMode) {
			plugin.getLogger().log(Level.INFO, theDenizen.getName() + " is not interactable.");
			if (!plugin.getScriptEngine().helper.checkCooldown(theDenizen, plugin.getTriggerRegistry().getTrigger(triggerName).getClass())) plugin.getLogger().log(Level.INFO, "...the Player has not yet met cool-down.");
			if (plugin.getCommandRegistry().getCommand(EngageCommand.class).getEngaged(theDenizen.getCitizensEntity())) plugin.getLogger().log(Level.INFO, "...the Denizen is ENGAGED.");
		}

		return false;
	}



	/* 
	 * Builds/Queues ScriptEntry(ies) of items read from a script. 
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



	/*
	 * ConcatenateScripts
	 * 
	 * Combines script files into one YML file for Denizen to read from.
	 * Code mostly borrowed from: 
	 * http://www.roseindia.net/tutorial/java/core/files/fileconcatenation.html
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
					plugin.getLogger().log(Level.INFO, "Processing script " + files[i].getName() + "... ");
					BufferedReader br = new BufferedReader(new FileReader(files[i]
							.getPath()));
					String line = br.readLine();
					while (line != null) {
						pw.println(line);
						line = br.readLine();
					}   br.close();
				}
			}
			pw.close();

			plugin.getLogger().log(Level.INFO, "OK! All scripts loaded!");

		} catch (Throwable error) {
			plugin.getLogger().log(Level.WARNING, "Woah! No scripts in /plugins/Denizen/scripts/ to load!");		
		}
	}



}
