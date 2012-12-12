package net.aufdemrand.denizen.scripts.requirements;

import java.util.ArrayList;
import java.util.List;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.exceptions.RequirementCheckException;
import net.aufdemrand.denizen.utilities.debugging.Debugger;
import net.aufdemrand.denizen.utilities.debugging.Debugger.DebugElement;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.command.exception.RequirementMissingException;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class RequirementChecker {

	private Denizen plugin;
	private Debugger dB;

	public RequirementChecker(Denizen denizen) {
		plugin = denizen;
		dB = plugin.getDebugger();
	}

	public boolean check(String scriptName, NPC npc, Player player) throws RequirementMissingException {

		String reqMode = plugin.getScripts().getString(scriptName + ".REQUIREMENTS.MODE", "NONE");
		List<String> reqList = plugin.getScripts().getStringList(scriptName + ".REQUIREMENTS.LIST");

		dB.echoDebug(ChatColor.YELLOW + "CHECK! Now checking '%s'", scriptName);
		
		// No requirements met yet, we just started!
		int numberMet = 0; 
		boolean negativeRequirement;

		// Requirements list null? This script is probably named wrong, or doesn't exist!
		if (reqList.isEmpty() && !reqMode.equals("NONE")) {
			dB.echoError("Non-valid requirements structure at:");
			dB.echoDebug(ChatColor.GRAY + scriptName + ":");
			dB.echoDebug(ChatColor.GRAY + "  Requirements:");
			dB.echoDebug(ChatColor.GRAY + "    Mode: ???");
			dB.echoDebug(ChatColor.GRAY + "    List:");
			dB.echoDebug(ChatColor.GRAY + "    - ???");
			dB.echoDebug("* Check spacing, validate structure and spelling.");
			return false;
		}

		// Requirement node "NONE"? No requirements in the LIST? No need to continue, return TRUE
		if (reqMode.equals("NONE") || reqList.isEmpty()) return true;

		dB.echoDebug("Requirement mode: '%s'", reqMode.toUpperCase());

		// Set up checks for requirement mode 'FIRST AND ANY #'
		boolean firstReqMet = false;
		boolean firstReqChecked = false;

		// Check all requirements
		for (String reqEntry : reqList) {

			// Check if this is a Negative Requirement
			if (reqEntry.startsWith("-")) { 
				negativeRequirement = true; 
				reqEntry = reqEntry.substring(1);
			}   else negativeRequirement = false;

			// Check requirement with RequirementRegistry
			if (plugin.getRequirementRegistry().list().containsKey(reqEntry.split(" ")[0])) {

				AbstractRequirement requirement = plugin.getRequirementRegistry().get(reqEntry.split(" ")[0]);
				String[] arguments = null;
				if (reqEntry.split(" ").length > 1)	arguments = plugin.getScriptEngine().getScriptBuilder().buildArgs(reqEntry.split(" ", 2)[1]);

				// Replace tags
				List<String> argumentList = new ArrayList<String>();
				if (arguments != null) argumentList = plugin.tagManager().fillArguments(arguments, player, plugin.getNPCRegistry().getDenizen(npc)); 

				try {
					// Check if # of required args are met
					if ((arguments == null && requirement.requirementOptions.REQUIRED_ARGS > 0) ||
							arguments.length < requirement.requirementOptions.REQUIRED_ARGS) throw new RequirementCheckException("");

					// Check the Requirement
					if (requirement.check(player, plugin.getNPCRegistry().getDenizen(npc), scriptName, argumentList) != negativeRequirement) {
						// Check first requirement for mode 'FIRST AND ANY #'
						if (!firstReqChecked) {
							firstReqMet = true;
							firstReqChecked = true;
						}
						numberMet++;
						dB.echoApproval("Checking Requirement '" + requirement.getName() + "'" + " ...requirement met!");
					} else {
						if (!firstReqChecked) {
							firstReqMet = false;
							firstReqChecked = true;
						}
						dB.echoApproval("Checking Requirement '" + requirement.getName() + "'" + " ...requirement not met!");
					}

				} catch (Throwable e) {
					if (e instanceof RequirementCheckException) {
						dB.echoError("Woah! Invalid arguments were specified!");
						dB.echoError(requirement.getUsageHint());
					} else {
						dB.echoError("Woah! An exception has been called for Requirement '" + requirement.getName() + "'!");
						if (!dB.showStackTraces) dB.echoError("Enable '/denizen stacktrace' for the nitty-gritty.");
						else e.printStackTrace(); 
					}
				}
			}

			// If the requirement is not registered with the Requirement Registry
			else {
				dB.echoError("Requirement '" + reqEntry.split(" ")[0] + "' not found! Check that the requirement is installed!");
			}
		}

		// Check numberMet with mode...
		String[] ModeArgs = reqMode.split(" ");
		// ALL mode
		if (reqMode.equalsIgnoreCase("ALL") && numberMet == reqList.size()) return true;

		// ANY # mode
		else if (ModeArgs[0].equalsIgnoreCase("ANY")) {
			if (ModeArgs.length == 1) {
				if (numberMet >= 1) return true;
				else return false;
			} else {
				if (numberMet >= Integer.parseInt(ModeArgs[1])) return true;
				else return false;
			}
		}

		// FIRST AND ANY # mode
		else if (ModeArgs[0].equalsIgnoreCase("FIRST") && ModeArgs[3].matches("\\d+")) {
			if (firstReqMet) {
				if (numberMet > Integer.parseInt(ModeArgs[3])) return true;
				else return false;
			} else return false;
		}

		else if (!ModeArgs[0].equalsIgnoreCase("ALL")) dB.echoError("Invalid Requirement Mode!");

		// Nothing met, return FALSE	
		return false;
	}

}
