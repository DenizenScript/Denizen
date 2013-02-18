package net.aufdemrand.denizen.scripts.requirements;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.exceptions.RequirementCheckException;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.ChatColor;

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
        if (context.mode.getMode() == RequirementsMode.Mode.NONE || context.list.isEmpty())
            return true;

        //
        // Actual requirements that need checking, alert the debugger
        //
        dB.echoDebug(ChatColor.YELLOW + "CHECK! Now checking '%s'", context.container.getName());
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

            //
            // Replace tags and build arguments
            //
            List<String> argumentList
                    = plugin.tagManager().fillArguments(aH.buildArgs(reqEntry), context.player, context.npc);

            //
            // Evaluate the requirement
            //
            if (argumentList.get(0).equalsIgnoreCase("valueof")) {
                if (argumentList.get(1).equalsIgnoreCase("true")) {
                    if (!negativeRequirement) {
                        dB.echoApproval("Checking 'VALUEOF " + argumentList.get(1) + "... requirement met!");
                        numberMet++;
                    } else
                        dB.echoApproval("Checking '-VALUEOF " + argumentList.get(1) + "...requirement not met!");

                } else {
                    if (!negativeRequirement)
                        dB.echoApproval("Checking 'VALUEOF " + argumentList.get(1) + "...requirement not met!");

                    else {
                        dB.echoApproval("Checking '-VALUEOF " + argumentList.get(1) + "...requirement met!");// Not met!
                        numberMet++;
                    }
                }
            } else
                // Check requirement with RequirementRegistry
                if (plugin.getRequirementRegistry().list().containsKey(argumentList.get(0).toUpperCase())) {

                    AbstractRequirement requirement = plugin.getRequirementRegistry().get(argumentList.get(0));

                    // Remove command name from arguments
                    argumentList.remove(0);

                    try {
                        // Check if # of required args are met
                        int	numArguments = argumentList.isEmpty() ? 0 : argumentList.size();
                        int neededArguments = requirement.requirementOptions.REQUIRED_ARGS;
                        if ((numArguments == 0 && neededArguments > 0) ||
                                numArguments < neededArguments) {
                            throw new RequirementCheckException("Not enough arguments (" + numArguments + " / " + neededArguments + ")");
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
                            String msg = e.getMessage().isEmpty() || e == null ? "No Error message defined!" : e.getMessage();
                            dB.echoError("Woah! Invalid arguments were specified: " + msg);
                            dB.echoError("Usage: " + requirement.getUsageHint());
                        } else {
                            dB.echoError("Woah! An exception has been called " + (requirement != null ? "for Requirement '" + requirement.getName() + "'" : "") + "!");
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
