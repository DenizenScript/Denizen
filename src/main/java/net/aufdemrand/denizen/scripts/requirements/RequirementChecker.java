package net.aufdemrand.denizen.scripts.requirements;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.exceptions.RequirementCheckException;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

/**
 * This class implements requirement checking for scripts.
 * 
 * @author aufdemrand
 */
public class RequirementChecker {

	private Denizen plugin;

	public RequirementChecker(Denizen denizen) {
		plugin = denizen;
	}

    /**
     * Checks a RequirementsContext with Requirements from the RequirementsRegistry.
     *
     * @param context
     * @return true if the list meets the requirements and context set
     *
     */
    public boolean check(RequirementsContext context) {

        //
        // Requirement node "NONE"? No requirements in the LIST? No need to
        // continue, return true.
        //
        if (context.mode.getMode() == RequirementsMode.Mode.NONE || context.list.isEmpty()) {
            return true;
        }

        //
        // Actual requirements that need checking, alert the debugger
        //
        dB.echoDebug(ChatColor.YELLOW + "CHECK! Now checking '%s'", context.scriptName);
        dB.echoDebug("Requirement mode: '%s'", context.mode.getMode().toString());

        // Set up checks for requirement mode 'FIRST AND ANY #'
		boolean firstReqMet = false;
		boolean firstReqChecked = false;

        // Counter for keeping track of met requirements
        int numberMet = 0;

		// Check all requirements
		for (String reqEntry : context.list) {
			boolean negativeRequirement = false;

			//
			// Check if this is a Negative Requirement.  Negative requirements start
			// with a... negative sign :)
			//
			if (reqEntry.startsWith("-")) {
				negativeRequirement = true; 
				reqEntry = reqEntry.substring(1);
			}

			// Check requirement with RequirementRegistry
			if (plugin.getRequirementRegistry().list().containsKey(reqEntry.split(" ")[0].toUpperCase())) {

				AbstractRequirement requirement = plugin.getRequirementRegistry().get(reqEntry.split(" ")[0].toUpperCase());
				String[] arguments = null;
				if (reqEntry.split(" ").length > 1)	{
					arguments = plugin.getScriptEngine().getScriptBuilder().buildArgs(reqEntry.split(" ", 2)[1]);
				}

				// Replace tags
				List<String> argumentList = new ArrayList<String>();
				if (arguments != null) {
				    argumentList = plugin.tagManager().fillArguments(arguments, context.player, plugin.getNPCRegistry().getDenizen(context.npc));
				}

				try {
					// Check if # of required args are met
					int	numArguments = arguments == null ? 0 : arguments.length;
					if ((numArguments == 0 && requirement.requirementOptions.REQUIRED_ARGS > 0) ||
							 numArguments < requirement.requirementOptions.REQUIRED_ARGS) {
						throw new RequirementCheckException("");
					}

					// Check the Requirement
					if (requirement.check(context, argumentList) != negativeRequirement) {
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
						dB.echoError("Usage: " + requirement.getUsageHint());
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

        // Check numberMet with mode-type

		// ALL mode
		if (context.mode.getMode() == RequirementsMode.Mode.ALL && numberMet == context.list.size()) return true;

		// ANY # mode
		else if (context.mode.getMode() == RequirementsMode.Mode.ANY_NUM) {
				if (numberMet >= context.mode.modeInt) return true;
				else return false;
		}

		// FIRST AND ANY # mode
		else if (context.mode.getMode() == RequirementsMode.Mode.FIRST_AND_ANY_NUM) {
			if (firstReqMet) {
				if (numberMet > context.mode.modeInt) return true;
				else return false;
			} else return false;
		}

		// Nothing met, return FALSE
		return false;
	}

}
