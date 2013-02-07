package net.aufdemrand.denizen.scripts.containers.core;

import net.aufdemrand.denizen.npc.dNPC;
import net.aufdemrand.denizen.scripts.ScriptRegistry;
import net.aufdemrand.denizen.scripts.triggers.AbstractTrigger;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InteractScriptHelper {

    /*
     * Gets the InteractScript from a NPC Denizen for a Player and returns the appropriate ScriptContainer. Returns null if no script found.
     */
    public static InteractScriptContainer getInteractScript(dNPC npc, Player player, Class<? extends AbstractTrigger> trigger) {

        if (npc == null || player == null) return null;

        // Find the assignmentScriptContainer currently assigned to the NPC
        AssignmentScriptContainer assignmentScript = npc.getAssignmentTrait().getAssignment();

        // Get list of interact scripts from the assignment script.
        // Note: this list includes the # priorities that prefix the script names.
        List<String> assignedScripts = new ArrayList<String>();
        if (assignmentScript.contains("INTERACT SCRIPTS"))
            assignedScripts = assignmentScript.getStringList("INTERACT SCRIPTS");

        // No debug necessary if there are no Interact Scripts specified in this Assignment.
        if (assignedScripts.isEmpty()) return null;

        // Alert the dBugger -- trying to find a good interact script!
        dB.echoDebug(dB.DebugElement.Header, "Getting interact script: " + npc.getName() + "/" + player.getName());

        //
        // Get scripts that meet requirements and add them to interactableScripts.
        //

        // Initialize list of interactable scripts
        List<PriorityPair> interactableScripts = new ArrayList<PriorityPair>();

        // Iterate through all entries to check each
        for (String entry : assignedScripts) {
            // Entries should be uppercase for this process
            entry = entry.toUpperCase();

            // Initialize the fields that will make up the PriorityPair
            String name = null;
            Integer priority;

            // Make sure a priority exists, deal with it if it doesn't.
            if (Character.isDigit(entry.charAt(0))) {
                try {
                    priority = Integer.valueOf(entry.split(" ", 2)[0]);
                    name = entry.split(" ", 2)[1].replace("^", "");
                } catch (Exception e) {
                    dB.echoError("Invalid Interact assignment for '" + entry + "'. Is the script name missing?");
                    continue;
                }
            } else {
                dB.echoError("Script '" + name + "' has an invalid priority! Assuming '0'.");
                name = entry;
                entry = "0 " + entry;
                priority = 0;
            }

            //
            // Start requirement checking
            //
            try {
                InteractScriptContainer interactScript = ScriptRegistry.getScriptContainerAs(name, InteractScriptContainer.class);

                // Check requirements of the script
                if (interactScript.checkBaseRequirements(player, npc)) {
                    dB.echoApproval("'" + entry + "' meets requirements.");

                    // Meets requirements, but we need to check cool down, too.
                    if (interactScript.checkCooldown(player))
                        interactableScripts.add(new PriorityPair(priority, entry.split(" ", 2)[1]));
                    else
                        dB.echoDebug(ChatColor.GOLD + " ...but, isn't cooled down, yet! Skipping.");

                } else
                    // Does not meet requirements, alert the console!
                    dB.echoDebug("'" + entry + "' does not meet requirements.");

            } catch (Exception e) {
                // Had a problem checking requirements, most likely a Legacy Requirement with bad syntax. Alert the console!
                dB.echoError(ChatColor.RED + "'" + entry + "' has a bad requirement, skipping.");
                if (!dB.showStackTraces) dB.echoError("Enable '/denizen stacktrace' for the nitty-gritty.");
                else e.printStackTrace();
            }

            dB.echoDebug(dB.DebugElement.Spacer);
        }

        // If list has only one entry, this is it!
        if (interactableScripts.size() == 1) {
            String script = interactableScripts.get(0).getName();
            dB.echoApproval("Highest scoring script is " + script + ".");
            dB.echoDebug(dB.DebugElement.Footer);
            return ScriptRegistry.getScriptContainerAs(script.replace("^", ""), InteractScriptContainer.class);
        }

        // Or, if list is empty.. no scripts meet requirements!
        else if (interactableScripts.isEmpty()) {
            dB.echoDebug(ChatColor.YELLOW + "+> " + ChatColor.WHITE + "No scripts meet requirements!");
            dB.echoDebug(dB.DebugElement.Footer);
            return null;
        }

        // If we have more than 2 script, let's sort the list from lowest to highest scoring script.
        else Collections.sort(interactableScripts);

        // Let's find which script to return since there are multiple.
        for (int a = interactableScripts.size() - 1; a >= 0; a--) {

            InteractScriptContainer interactScript = ScriptRegistry
                    .getScriptContainerAs(interactableScripts.get(a).name.replace("^", ""), InteractScriptContainer.class);

            dB.echoDebug("Checking script '" + interactableScripts.get(a).getName() + "'.");

            // Check for 'Overlay' assignment mode.
            // If specified as an 'Overlay', the criteria for matching requires the
            // specified trigger to have a script in the step.
            if (interactableScripts.get(a).getName().startsWith("^")) {

                // This is an Overlay Assignment, check for the appropriate Trigger Script...
                // If Trigger exists, cool, this is our script.
                if (interactScript.containsTriggerInStep(getCurrentStep(player, interactScript.getName()), trigger)) {
                    dB.echoDebug("...found trigger!");
                    dB.echoApproval("Highest scoring script is " + interactScript.getName() + ".");
                    dB.echoDebug(dB.DebugElement.Footer);
                    return interactScript;
                }

                else dB.echoDebug("...no trigger on this overlay assignment. Skipping.");
            }

            // Not an Overlay Assignment, so return this script, which is the highest scoring.
            else {
                dB.echoDebug("...script is good!");
                dB.echoApproval("Highest scoring script is " + interactScript.getName() + ".");
                dB.echoDebug(dB.DebugElement.Footer);
                return interactScript;
            }
        }

        return null;
    }

    /**
     * Returns the current step for a Player and specified script. If no current step is found, the default
     * step is used, 'Default', unless another default (used by ending the step-name with a '*') is specified
     * in the script.
     *
     * @param player the Player to check
     * @param scriptName the name of the script to check
     *
     * @return the current, or default, step name
     *
     */
    public static String getCurrentStep(Player player, String scriptName) {
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
    public static String getCurrentStep(Player player, String scriptName, Boolean verbose) {
        String currentStep = "DEFAULT";

        if (scriptName == null) return "";

        if (DenizenAPI._saves()
                .getString("Players." + player.getName()
                        + "." + "Scripts." + scriptName.toUpperCase()
                        + "." + "Current Step") != null) {

            currentStep =  DenizenAPI._saves()
                    .getString("Players." + player.getName()
                            + "." + "Scripts." + scriptName.toUpperCase()
                            + "." + "Current Step");

            if (verbose) dB.echoDebug("Getting current step... found '" + currentStep + "'");

            return currentStep;
        }

        // No saved step found, let's look for defaults (dScript default steps end in *)
        currentStep = ScriptRegistry.getScriptContainerAs(scriptName, InteractScriptContainer.class).getDefaultStepName();

        if (verbose) dB.echoDebug("Getting current step... not found, assuming '" + currentStep + "'");
        return currentStep;
    }

    /**
     * Used internally when comparing interact script assignment priorities to
     * help out with sorting.
     *
     */
    private static class PriorityPair implements Comparable<PriorityPair> {
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

