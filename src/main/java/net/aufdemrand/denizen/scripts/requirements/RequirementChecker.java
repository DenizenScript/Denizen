package net.aufdemrand.denizen.scripts.requirements;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.exceptions.RequirementCheckException;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.tags.TagManager;
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
     * @param context The requirement context
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

        // Index number for the current 'reqMet' index
        int index = -1;

        // Counters for keeping track of checked and met requirements
        int numberChecked = 0;
        int[] numberMet = new int[context.mode.modeInt.length];

        // Check all requirements
        for (String reqEntry : context.list) {
            boolean negativeRequirement = false;
            boolean reqMet = false;

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
            List<String> argumentList = TagManager.fillArguments(aH.buildArgs(reqEntry), context.player, context.npc);
            String reqString = argumentList.get(0).toUpperCase();
            
            //
            // Evaluate the requirement
            //
            if (reqString.equals("VALUEOF")) {
                String arg = argumentList.get(1);
                String debug = "Checking '" + (negativeRequirement ? "-" : "") + "VALUEOF' " + arg + "... requirement ";

                if (arg.equalsIgnoreCase("true") != negativeRequirement) {
                    debug += "met!";
                    reqMet = true;

                } else {
                    debug += "not met!";
                }

                dB.echoApproval(debug);

            } else if (plugin.getRequirementRegistry().list().containsKey(reqString)) {
                // Check requirement with RequirementRegistry
                AbstractRequirement requirement = plugin.getRequirementRegistry().get(reqString);

                // Remove command name from arguments
                argumentList.remove(0);

                try {
                    // Check if # of required args are met
                    int	numArguments = argumentList.isEmpty() ? 0 : argumentList.size();
                    int neededArguments = requirement.requirementOptions.REQUIRED_ARGS;

                    if ((numArguments == 0 && neededArguments > 0) || numArguments < neededArguments) {
                        throw new RequirementCheckException("Not enough arguments (" + numArguments + " / " + neededArguments + ")");
                    }

                    // Check the Requirement
                    reqMet = requirement.check(context, argumentList) != negativeRequirement;

                    dB.echoApproval("Checking Requirement '" + requirement.getName() + "'" + " ...requirement " + (reqMet ? "" : "not") + " met!");

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
            } else {
                // If the requirement is not registered with the Requirement Registry
                dB.echoError("Requirement '" + reqEntry.split(" ")[0] + "' not found! Check that the requirement is installed!");
            }

            // Prepare the variables
            if(index < 0 || context.mode.modeInt[index] == numberChecked) {
                index++;

                numberChecked = 0;
                numberMet[index] = 0;
            }

            // Increase the counters
            numberChecked++;

            // If we met the requirements, log it into the 'numberMet' array
            if(reqMet) {
                numberMet[index]++;
            }
        }

        // Check against the Mode type
        switch(context.mode.getMode()) {
            case ALL:
                // Return true if the amount of met requirements are the same as the list size
                return numberMet[0] == context.list.size();

            case ANY_NUM:
                // Return true if the amount of met requirements are at least as defined
                return numberMet[0] == context.mode.modeInt[0];

            case FIRST_NUM_AND_ANY_NUM:
                // Loop through the 'modeInt' array and check against the 'numberMet' array
                for(int n = 0; n < context.mode.modeInt.length; n++) {
                    if(context.mode.modeInt[n] != numberMet[n]) {
                        return false;
                    }
                }

                return true;

            default:
                // Nothing met, return FALSE
                return false;
        }
    }

}
