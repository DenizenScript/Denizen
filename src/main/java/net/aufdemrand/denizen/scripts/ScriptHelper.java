package net.aufdemrand.denizen.scripts;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.npc.traits.AssignmentTrait;
import net.aufdemrand.denizen.scripts.commands.core.CooldownCommand;
import net.aufdemrand.denizen.scripts.requirements.RequirementsContext;
import net.aufdemrand.denizen.scripts.requirements.RequirementsMode;
import net.aufdemrand.denizen.scripts.triggers.AbstractTrigger;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.DebugElement;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScriptHelper {

	Denizen denizen;

	public ScriptHelper (Denizen denizenPlugin) {
		denizen = denizenPlugin;
	}

	/*
	 * Gets the InteractScript from a NPC Denizen for a Player and returns the appropriate Script. Returns null if no script found.
	 */

	public String getInteractScript(NPC npc, Player player, Class<? extends AbstractTrigger> trigger) {

		if (npc == null || player == null) return null;
		
		String theScript = null;
		List<String> assignedScripts = denizen.getScriptEngine().getScriptHelper().getStringListIgnoreCase(npc.getTrait(AssignmentTrait.class).getAssignment() + ".INTERACT SCRIPTS");
		
        // No debug necessary if there are no Interact Scripts in this Assignment.
		if (assignedScripts.isEmpty()) { 
			return null;
		}

        dB.echoDebug(DebugElement.Header, "Getting interact script: " + npc.getName() + "/" + player.getName());

		/* Get scripts that meet requirements and add them to interactableScripts. */
		List<PriorityPair> interactableScripts = new ArrayList<PriorityPair>();

		for (String assignment : assignedScripts) {
			assignment = assignment.toUpperCase();
			String script = null;
			Integer priority;

			// Make sure a priority exists.
			if (Character.isDigit(assignment.charAt(0))) {
				try {
					priority = Integer.valueOf(assignment.split(" ", 2)[0]);
					script = assignment.split(" ", 2)[1].replace("^", "");
				} catch (Exception e) {
					dB.echoError("Invalid Interact assignment for '" + assignment + "'. Is the script name missing?");
					continue;
				}
			} else {
				dB.echoError("Script '" + script + "' has an invalid priority! Assuming '0'.");
				script = assignment;
				assignment = "0 " + assignment;
				priority = 0;
			}

			// Get requirements
			try {
				if (denizen.getScriptEngine().getRequirementChecker().check(
                        buildInteractScriptRequirementContext(player, npc, script))) {
					dB.echoApproval("'" + assignment + "' meets requirements.");

					// Meets requirements, but we need to check cool down, too.
					if (denizen.getCommandRegistry().get(CooldownCommand.class).checkCooldown(player.getName(), script)) { 
						interactableScripts.add(new PriorityPair(priority, assignment.split(" ", 2)[1]));
					}	else {
						dB.echoDebug(ChatColor.GOLD + " ...but, isn't cooled down, yet! Skipping.");
					}

				} else
					// Does not meet requirements, alert the console!
					dB.echoDebug("'" + assignment + "' does not meet requirements.");

			} catch (Exception e) {
				// Had a problem checking requirements, most likely a Legacy Requirement with bad syntax. Alert the console!
				dB.echoError(ChatColor.RED + "'" + assignment + "' has a bad requirement, skipping.");
                if (!dB.showStackTraces) dB.echoError("Enable '/denizen stacktrace' for the nitty-gritty.");
                else e.printStackTrace();
            }
			
			dB.echoDebug(DebugElement.Spacer);
		}

		// If list has only one entry, this is it!
		if (interactableScripts.size() == 1) {
			theScript = interactableScripts.get(0).getName();
			dB.echoApproval("Highest scoring script is " + theScript + ".");
			dB.echoDebug(DebugElement.Footer);
			return theScript.replace("^", "");
		}

		// Or, if list is empty.. uh oh!
		else if (interactableScripts.isEmpty()) {
			dB.echoDebug(ChatColor.YELLOW + "Uh oh!" + ChatColor.WHITE + " No scripts meet requirements!");
			dB.echoDebug(DebugElement.Footer);
			return null;
		}

		// If we have more than 2 script, let's sort the list from lowest to highest scoring script.
		else Collections.sort(interactableScripts);

		// Let's find which script to return since there are multiple.
		for (int a = interactableScripts.size() - 1; a >= 0; a--) {

			dB.echoDebug("Checking script '" + interactableScripts.get(a).getName() + "'.");

			// Check for Overlay Assignment...
			if (interactableScripts.get(a).getName().startsWith("^")) {

				// This is an Overlay Assignment, check for the appropriate Trigger Script...
				String scriptName = interactableScripts.get(a).getName().substring(1);
				String triggerString = denizen.getTriggerRegistry().get(trigger).getName().toUpperCase() + " TRIGGER"; 

				// If Trigger exists, cool, this is our script.
				if (denizen.getScripts().contains(scriptName.toUpperCase() + ".STEPS." + getCurrentStep(player, scriptName) + "." + triggerString)) {
					dB.echoDebug("...found trigger!");
					dB.echoApproval("Highest scoring script is " + scriptName + ".");
					dB.echoDebug(DebugElement.Footer);
					return scriptName.replace("^", "");
				}

				else dB.echoDebug("...no trigger on this overlay assignment. Skipping.");
			}

			// Not an Overlay Assignment, so return this script, which is the highest scoring.
			else { 
				dB.echoDebug("...script is good!");
				dB.echoApproval("Highest scoring script is " + interactableScripts.get(a).getName() + ".");
				dB.echoDebug(DebugElement.Footer);
				return interactableScripts.get(a).getName().replace("^", "");
			}
		}

		return null;
	}


    /**
     * Builds RequirementsContext for Interact-type Script requirements
     *
     * @param player The player interacting
     * @param npc The NPC interacting with
     * @param scriptName The name of the script
     * @return the RequirementsContext
     *
     */
    private RequirementsContext buildInteractScriptRequirementContext(Player player, NPC npc, String scriptName) {

        List<String> reqList = denizen.getScripts().getStringList(scriptName + ".REQUIREMENTS.LIST");
        RequirementsMode reqMode =
                new RequirementsMode(denizen.getScripts().getString(scriptName + ".REQUIREMENTS.MODE", "NONE"));

        // Requirements list null? This script is probably named wrong, or doesn't exist!
        if (reqList.isEmpty() && reqMode.getMode() != RequirementsMode.Mode.NONE) {
            dB.echoError("Non-valid requirements structure at:");
            dB.echoDebug(ChatColor.GRAY + scriptName + ":");
            dB.echoDebug(ChatColor.GRAY + "  Requirements:");
            dB.echoDebug(ChatColor.GRAY + "    Mode: ???");
            dB.echoDebug(ChatColor.GRAY + "    List:");
            dB.echoDebug(ChatColor.GRAY + "    - ???");
            dB.echoDebug("* Check spacing, validate structure and spelling.");
            return null;
        }

        return new RequirementsContext(reqMode, reqList, scriptName).attachNPC(npc).attachPlayer(player);
    }


	public String getCurrentStep(Player player, String scriptName) {
		return getCurrentStep(player, scriptName, true);
	}

    /**
     * Returns the current step for a Player and specified script. If no current step is found, the default
     * step is used, 'Default', unless another default (used by ending the step-name with a '*') is specified
     * in the script.
     *
     * @param player the Player to check
     * @param scriptName the name of the script to check
     * @param verbose whether debugging information should be shown
     *
     * @return the current, or default, step name
     *
     */
	public String getCurrentStep(Player player, String scriptName, Boolean verbose) {
		String current = "DEFAULT";

        if (scriptName == null) return "";

		if (denizen.getSaves().getString("Players." + player.getName() + ".Scripts." + scriptName.toUpperCase() + "." + "Current Step") != null) {
			current =  denizen.getSaves().getString("Players." + player.getName() + ".Scripts." + scriptName.toUpperCase() + "." + "Current Step");
			if (verbose) dB.echoDebug("Getting current step... found '" + current + "'");
			return current;
		}

        // No saved step found, let's look for defaults (dScript default steps end in *)
        if (denizen.getScripts().contains(scriptName.toUpperCase())) {
        Set<String> steps = denizen.getScripts().getConfigurationSection(scriptName.toUpperCase() + ".STEPS").getKeys(false);
            // For backwards compatibility (dScript steps used to be numbered, default being '1')
            if (steps.contains("1")) current = "1";
            for (String step : steps)
                if (step.endsWith("*"))
                    current = step;
        }

        if (verbose) dB.echoDebug("Getting current step... not found, assuming '" + current + "'");
		return current;
	}

	/**
	 * Methods to help get String script entries from a YAML script.
	 */

	public static String scriptKey = ".SCRIPT";

	public String getTriggerScriptPath(String scriptName, String step, String triggerName) {
		return scriptName.toUpperCase() + ".STEPS." + step + "." +  triggerName.toUpperCase() + " TRIGGER.";
	}

	public List<String> getScriptContents(String path) {
		List<String> contents = new ArrayList<String>();
		path = path.toUpperCase().replace("..", ".");

        if (denizen.getScripts().contains(path.toUpperCase()))
			contents = denizen.getScripts().getStringList(path.toUpperCase());
		if (contents.isEmpty()) {

            // Less severe error for missing trigger script within an interact script-type
            if (path.toUpperCase().contains("TRIGGER")) {
                String[] context = path.split("\\.");
                if (context.length >= 4)
                dB.echoDebug(ChatColor.YELLOW + "INFO +> " + ChatColor.WHITE + "No " + context[3] + " for '"
                        + context[0].toUpperCase() + "/" + context[2].toUpperCase() + "'");
                return contents;
            }

			dB.echoDebug(ChatColor.YELLOW + "WARNING: " + ChatColor.WHITE + "Unable to locate script at:");
			String spacing = "";
			for (String node : path.split("\\.")) {
				dB.echoDebug(spacing + node + ":");
				spacing = spacing + "  ";
			}
			dB.echoDebug(spacing.substring(0, spacing.length() - 1) + "- ???");
			dB.echoDebug("This script may not exist, or structure may be incorrect. Validate structure and spelling.");
		}

		return contents;
	}

	/*
	 * ConcatenateScripts reads script files in the /Denizen/scripts/ folder, parses them
	 * for YAML errors and adds them to Memory for usage.
	 */

	public String concatenateScripts() {
		try {
			File file = new File(denizen.getDataFolder() + File.separator + "scripts");
			
			if(!file.exists()) throw new Exception("No script folder found, please create one first!");
			
			File[] files = file.listFiles();

			if (files.length > 0) {
				StringBuilder sb = new StringBuilder();  
				for (File f : files){
					String fileName = f.getName();
					if (fileName.substring(fileName.lastIndexOf('.') + 1).equalsIgnoreCase("YML")
							|| fileName.substring(fileName.lastIndexOf('.') + 1).equalsIgnoreCase("DSCRIPT")
                            || fileName.substring(fileName.lastIndexOf('.') + 1).equalsIgnoreCase("YAML")
							&& !fileName.startsWith(".")) {
						dB.echoDebug("Processing '" + fileName + "'... ");
						try {
							YamlConfiguration yaml = YamlConfiguration.loadConfiguration(f);
							if (yaml != null)
                            sb.append(yaml.saveToString() + "\r\n");
                            else dB.echoError(ChatColor.RED + "Woah! Error parsing " + fileName + "! This script has been skipped. See console for YAML errors.");
						} catch (RuntimeException e) {
							dB.echoError(ChatColor.RED + "Woah! Error parsing " + fileName + "!");
                            if (dB.showStackTraces) {
                                dB.echoDebug("STACKTRACE follows:");
                                e.printStackTrace();
                            }
                            else dB.echoDebug("Use '/denizen debug -s' for the nitty-gritty.");						}
					}
				}
				dB.echoApproval("All scripts loaded!");
				return yamlKeysToUpperCase(sb.toString());
			} else dB.echoError(ChatColor.RED + "Woah! No scripts in /plugins/Denizen/scripts/ to load!");  
		} catch (Exception error) {
		    dB.echoError(ChatColor.RED + "Woah! No script folder found in /plugins/Denizen/scripts/");
			if (dB.showStackTraces) error.printStackTrace();
		}

		return "";
	}

	/**
	 * Gets an object from a script key and accommodates for the case-insensitivity.
	 */

	public int getIntIgnoreCase(String path) {
		return denizen.getScripts().getInt(path.toUpperCase());
	}

	public String getStringIgnoreCase(String path) {
		return denizen.getScripts().getString(path.toUpperCase());
	}

	public List<String> getStringListIgnoreCase(String path) {
		return denizen.getScripts().getStringList(path.toUpperCase());
	}

	public int getIntIgnoreCase(String path, int def) {
		return denizen.getScripts().getInt(path.toUpperCase(), def);
	}

	public String getStringIgnoreCase(String path, String def) {
		return denizen.getScripts().getString(path.toUpperCase(), def);
	}

	
	/**
     * Changes YAML 'keys' to all Upper Case to de-sensitize case sensitivity when
     * reading and parsing scripts.
     */
    
    private String yamlKeysToUpperCase(String string) {
    	StringBuffer sb = new StringBuffer();
    	Pattern pattern = Pattern.compile("(^[^:-]*?[^\\s]:)", Pattern.MULTILINE);
    	Matcher matcher = pattern.matcher(string);
    
    	while (matcher.find())
    		matcher.appendReplacement(sb, matcher.group().toUpperCase());
    
    	matcher.appendTail(sb);
    	return sb.toString();
    }


    /**
	 * Used internally when comparing interact script assignment priorities to
	 * help out with sorting.
	 *
	 */
	private class PriorityPair implements Comparable<PriorityPair> {
		int priority;
		private String name;

		public PriorityPair(int priority, String scriptName) {
			this.priority = priority;
			this.name = scriptName.toUpperCase();
		}

		@Override
		public int compareTo(PriorityPair pair) {
			return priority < pair.priority ? -1 : priority > pair.priority ? 1 : 0;
		}

		public String getName() {
			return name;
		}

	}
}