package net.aufdemrand.denizen.scripts.requirements;

import net.aufdemrand.denizen.exceptions.RequirementCheckException;
import net.aufdemrand.denizen.tags.BukkitTagContext;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dScript;
import net.aufdemrand.denizencore.tags.TagManager;
import org.bukkit.ChatColor;

import java.util.List;

/**
 * This class implements requirement checking for scripts.
 *
 * @author aufdemrand
 */
public class RequirementChecker {

    public RequirementChecker() {
    }

    /**
     * Checks a RequirementsContext with Requirements from the RequirementsRegistry.
     *
     * @param context the context to check against
     * @return true if the list meets the requirements and context set
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
        dB.echoDebug(context.getScriptContainer(), ChatColor.YELLOW + "CHECK! Now checking '" + context.container.getName() + "'");
        dB.echoDebug(context.getScriptContainer(), "Requirement mode: '" + context.mode.getMode().toString() + "'");

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
            List<String> argumentList = TagManager.fillArguments(aH.buildArgs(reqEntry),
                    new BukkitTagContext(context.player, context.npc, false, null, dB.shouldDebug(context.getScriptContainer()), new dScript(context.getScriptContainer())));
            String reqString = argumentList.get(0).toUpperCase();

            //
            // Evaluate the requirement
            //

            // <--[requirement]
            // @Name ValueOf
            // @Syntax valueof [<tag>]
            // @Required 1
            // @Stable stable
            // @Short Checks if the tag is true.
            //
            // @Description
            // Checks if a specified tag or value returns 'true'.
            //
            // @Usage
            // Check if a simple tag is true.
            // - valueof <player.is_player>
            //
            // @Usage
            // Check a comparable (See <@link language comparable> for more information.)
            // - valueof <player.health.is[LESS].than[10]>
            // -->
            if (reqString.equalsIgnoreCase("valueof")) {
                String arg = argumentList.get(1);

                if (arg.equalsIgnoreCase("true")) {
                    if (!negativeRequirement) {
                        dB.echoApproval("Checking 'VALUEOF " + arg + "... requirement met!");
                        numberMet++;
                    }
                    else {
                        dB.echoApproval("Checking '-VALUEOF " + arg + "...requirement not met!");
                    }

                }
                else {
                    if (!negativeRequirement) {
                        dB.echoApproval("Checking 'VALUEOF " + arg + "...requirement not met!");
                    }
                    else {
                        dB.echoApproval("Checking '-VALUEOF " + arg + "...requirement met!");// Not met!
                        numberMet++;
                    }
                }

            }
            else
                // Check requirement with RequirementRegistry
                if (DenizenAPI.getCurrentInstance().getRequirementRegistry().list().containsKey(reqString)) {
                    AbstractRequirement requirement = DenizenAPI.getCurrentInstance().getRequirementRegistry().get(reqString);

                    // Remove command name from arguments
                    argumentList.remove(0);

                    try {
                        // Check if # of required args are met
                        int numArguments = argumentList.isEmpty() ? 0 : argumentList.size();
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
                            dB.echoApproval("Checked '" + requirement.getName() + "'" + " ...requirement met!");
                        }
                        else {
                            if (!firstReqChecked) {
                                firstReqMet = false;
                                firstReqChecked = true;
                            }
                            dB.echoApproval("Checked '" + requirement.getName() + "'" + " ...requirement not met!");
                        }

                    }
                    catch (Throwable e) {
                        if (e instanceof RequirementCheckException) {
                            String msg = e.getMessage().isEmpty() || e == null ? "No Error message defined!" : e.getMessage();
                            dB.echoError("Woah! Invalid arguments were specified: " + msg);
                            dB.echoError("Usage: " + requirement.getUsageHint());
                        }
                        else {
                            dB.echoError("Woah! An exception has been called " + (requirement != null ? "for Requirement '" + requirement.getName() + "'" : "") + "!");
                            dB.echoError(e);
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
        if (context.mode.getMode() == RequirementsMode.Mode.ALL && numberMet == context.list.size()) {
            return true;
        }

        // ANY # mode
        else if (context.mode.getMode() == RequirementsMode.Mode.ANY_NUM) {
            return (numberMet >= context.mode.modeInt);
        }

        // FIRST AND ANY # mode
        else if (context.mode.getMode() == RequirementsMode.Mode.FIRST_AND_ANY_NUM) {
            return firstReqMet && (numberMet <= context.mode.modeInt);
        }

        // Nothing met, return FALSE
        return false;
    }
}
