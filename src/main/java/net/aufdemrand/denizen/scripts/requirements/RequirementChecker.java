package net.aufdemrand.denizen.scripts.requirements;

import java.util.List;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.utilities.debugging.Debugger;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.command.exception.RequirementMissingException;

import org.bukkit.entity.Player;

@SuppressWarnings("deprecation")
public class RequirementChecker {

    private Denizen plugin;
    private Debugger dB;

    public RequirementChecker(Denizen denizen) {
        plugin = denizen;
        dB = plugin.getDebugger();
    }

    public boolean check(String scriptName, NPC npc, Player player) throws RequirementMissingException {

        String reqMode = plugin.getScripts().getString(scriptName + ".Requirements.Mode", null);
        List<String> reqList = plugin.getScripts().getStringList(scriptName + ".Requirements.List");

        // No requirements met yet, we just started!
        int numberMet = 0; 
        boolean negativeRequirement;

        // Requirements list null? This script is probably named wrong, or doesn't exist!
        if (reqList.isEmpty()) {
            dB.echoError("Non-valid requirements structure at:");
            dB.echoDebug(scriptName + ":");
            dB.echoDebug("  Requirements:");
            dB.echoDebug("    Mode: ???");
            dB.echoDebug("    List:");
            dB.echoDebug("    - ???");
            dB.echoError("Check spacing, validate structure and spelling.");
            return false;
        }

        // Requirement node "NONE"? No requirements in the LIST? No need to continue, return TRUE
        if (reqMode.equals("NONE") || reqList.isEmpty()) return true;

        dB.echoDebug("Requirement mode: '%s'", reqMode.toUpperCase());

        boolean firstReqMet = false;
        boolean firstReqChecked = false;
        
        for (String reqEntry : reqList) {
            
            // Check if this is a Negative Requirement
            if (reqEntry.startsWith("-")) { 
                negativeRequirement = true; 
                reqEntry = reqEntry.substring(1);
            } else negativeRequirement = false;

            String requirement = reqEntry.split(" ")[0];

            // Check requirement with RequirementRegistry
            if (plugin.getRequirementRegistry().list().containsKey(requirement)) {
                String[] arguments = null;
                if (reqEntry.split(" ").length > 1)	arguments = plugin.getScriptEngine().getScriptBuilder().buildArgs(reqEntry.split(" ", 2)[1]);

                // Get requirement class and check
                try {
                    boolean requirementmet = plugin.getRequirementRegistry().get(requirement).check(player, plugin.getNPCRegistry().getDenizen(npc), scriptName, arguments);
                    if (requirementmet != negativeRequirement) {
                        // Check first requirement for mode 'FIRST AND ANY #'
                        if (!firstReqChecked) {
                            firstReqMet = true;
                            firstReqChecked = true;
                        }
                        numberMet++;
                        dB.echoApproval("Checking Requirement '" + reqEntry.split(" ")[0].toUpperCase() + "'" + " ...requirement met!");
                    } else {
                        if (!firstReqChecked) {
                            firstReqMet = false;
                            firstReqChecked = true;
                        }
                        dB.echoApproval("Checking Requirement '" + reqEntry.split(" ")[0].toUpperCase() + "'" + " ...requirement not met!");
                    }
                } catch (Throwable e) {
                    dB.echoError("Woah! An exception has been called for Requirement '" + reqEntry.split(" ")[0].toUpperCase() + "'!");
                    if (!dB.showStackTraces)
                        dB.echoError("Enable '/denizen stacktrace' for the nitty-gritty.");
                    else e.printStackTrace(); }
            }
            else dB.echoError("Requirement not found! Check that the requirement is installed!");
        }

        // Check numberMet	
        if (reqMode.equalsIgnoreCase("ALL") && numberMet == reqList.size()) return true;

        String[] ModeArgs = reqMode.split(" ");
        if (ModeArgs[0].equalsIgnoreCase("ANY")) {
            if (ModeArgs.length == 1) {
                if (numberMet >= 1) return true;
            } else {
                if (numberMet >= Integer.parseInt(ModeArgs[1])) return true;
            }
        }
        else if (ModeArgs[0].equalsIgnoreCase("FIRST") && ModeArgs[3].matches("\\d+")) {
            if (firstReqMet) {
                if (numberMet > Integer.parseInt(ModeArgs[3])) return true;
            }
        }
        else dB.echoError("Invalid Requirement Mode!");

        // Nothing met, return FALSE	
        return false;
    }

}
