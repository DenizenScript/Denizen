package net.aufdemrand.denizen.scripts.containers.core;

import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.scripts.commands.core.CooldownCommand;
import net.aufdemrand.denizen.scripts.triggers.AbstractTrigger;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import com.denizenscript.denizencore.scripts.ScriptRegistry;
import com.denizenscript.denizencore.utilities.debugging.dB.DebugElement;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InteractScriptHelper {

    /**
     * Gets the InteractScript from a NPC Denizen for a Player and returns the appropriate ScriptContainer.
     * Returns null if no script found.
     *
     * @param npc     the NPC involved
     * @param player  the Player involved
     * @param trigger the class of the trigger being used
     * @return the highest priority InteractScriptContainer that meets requirements, if any.
     */
    public static InteractScriptContainer getInteractScript(dNPC npc, dPlayer player,
                                                            Class<? extends AbstractTrigger> trigger) {
        // If no trigger, npc or player specified, return null.
        // These objects are required to progress any further.
        if (npc == null || player == null || trigger == null) {
            return null;
        }

        // Find the assignmentScriptContainer currently assigned to the NPC
        AssignmentScriptContainer assignmentScript = npc.getAssignmentTrait().getAssignment();

        if (assignmentScript == null) {
            return null;
        }

        // Get list of interact scripts from the assignment script.
        // Note: this list includes the # priorities that prefix the script names.
        List<String> assignedScripts = new ArrayList<>();
        if (assignmentScript.contains("INTERACT SCRIPTS")) {
            assignedScripts = assignmentScript.getStringList("INTERACT SCRIPTS");
        }

        // No debug necessary if there are no Interact Scripts specified in this Assignment.
        if (assignedScripts.isEmpty()) {
            return null;
        }

        // Alert the dBugger -- trying to find a good interact script!
        if (dB.shouldDebug(assignmentScript)) {
            dB.log(DebugElement.Header, "Getting interact script: n@" + npc.getName() + "/p@" + player.getName());
        }

        //
        // Get scripts that meet requirements and add them to interactableScripts.
        //

        // Initialize list of interactable scripts as PriorityPair objects which help with sorting
        List<PriorityPair> interactableScripts = new ArrayList<>();

        // Iterate through all entries to check requirements for each
        for (String entry : assignedScripts) {
            // Entries should be uppercase for this process
            entry = entry.toUpperCase();

            // Initialize the fields that will make up the PriorityPair
            String name = null;
            int priority;

            // Make sure a priority exists, deal with it if it doesn't.
            if (Character.isDigit(entry.charAt(0))) {
                try {
                    priority = Integer.parseInt(entry.split(" ", 2)[0]);
                    name = entry.split(" ", 2)[1].replace("^", "");
                }
                catch (Exception e) {
                    dB.echoError("Invalid Interact assignment for '" + entry + "'. Is the script name missing?");
                    continue;
                }
            }
            else {
                //dB.echoError("Script '" + name + "' has an invalid priority! Assuming '0'.");
                name = entry;
                entry = "0 " + entry;
                priority = 0;
            }

            //
            // Start requirement checking
            //
            try {
                InteractScriptContainer interactScript = ScriptRegistry.getScriptContainer(name);

                if (interactScript != null) {
                    // Check script cooldown
                    if (CooldownCommand.checkCooldown(player, interactScript.getName())) {
                        interactableScripts.add(new PriorityPair(priority, entry.split(" ", 2)[1]));
                    }
                    else {
                        if (dB.shouldDebug(interactScript)) {
                            dB.log(ChatColor.GOLD + " ...but, isn't cooled down, yet! Skipping.");
                        }
                    }
                }
                else {
                    // Alert the console
                    dB.echoError("'" + entry + "' is not a valid Interact Script. Is there a duplicate script by this name?");
                }
            }
            catch (Exception e) {
                // Had a problem checking requirements, most likely a Legacy Requirement with bad syntax. Alert the console!
                dB.echoError(ChatColor.RED + "'" + entry + "' has a bad requirement, skipping.");
                dB.echoError(e);
            }

            if (dB.shouldDebug(assignmentScript)) {
                dB.log(DebugElement.Spacer, null);
            }
            // Next entry!
        }

        //
        // Sort scripts that met requirements and check which one has the highest priority
        //

        // If list has only one entry, this is it!
        if (interactableScripts.size() == 1) {
            String script = interactableScripts.get(0).getName();
            InteractScriptContainer interactScript = ScriptRegistry.getScriptContainer(script.replace("^", ""));
            if (dB.shouldDebug(interactScript)) {
                dB.echoApproval("Highest scoring script is " + script + ".");
            }
            if (dB.shouldDebug(assignmentScript)) {
                dB.log("Current step for this script is: " + getCurrentStep(player, script));
            }
            if (dB.shouldDebug(interactScript)) {
                dB.log(DebugElement.Footer, "");
            }
            return interactScript;
        }

        // Or, if list is empty.. no scripts meet requirements!
        else if (interactableScripts.isEmpty()) {
            if (dB.shouldDebug(assignmentScript)) {
                dB.log(ChatColor.YELLOW + "+> " + ChatColor.WHITE + "No scripts meet requirements!");
                dB.log(DebugElement.Footer, "");
            }
            return null;
        }

        // If we have more than 2 matches, let's sort the list from lowest to highest scoring script.
        else {
            Collections.sort(interactableScripts);
        }

        // Let's find which script to return since there are multiple.
        for (int a = interactableScripts.size() - 1; a >= 0; a--) {

            InteractScriptContainer interactScript = ScriptRegistry
                    .getScriptContainer(interactableScripts.get(a).name.replace("^", ""));

            if (dB.shouldDebug(interactScript)) {
                dB.log("Checking script '" + interactableScripts.get(a).getName() + "'.");
            }

            // Check for 'Overlay' assignment mode.
            // If specified as an 'Overlay', the criteria for matching requires the
            // specified trigger to have a script in the step.
            if (interactableScripts.get(a).getName().startsWith("^")) {

                // This is an Overlay Assignment, check for the appropriate Trigger Script...
                // If Trigger exists, cool, this is our script.
                if (interactScript.containsTriggerInStep(getCurrentStep(player, interactScript.getName()), trigger)) {
                    if (dB.shouldDebug(interactScript)) {
                        dB.log("...found trigger!");
                        dB.echoApproval("Highest scoring script is " + interactScript.getName() + ".");
                        dB.log("Current step for this script is: " + getCurrentStep(player, interactScript.getName()));
                        dB.log(DebugElement.Footer, "");
                    }
                    return interactScript;
                }
                else {
                    if (dB.shouldDebug(interactScript)) {
                        dB.log("...no trigger on this overlay assignment. Skipping.");
                    }
                }
            }

            // Not an Overlay Assignment, so return this script, which is the highest scoring.
            else {
                if (dB.shouldDebug(interactScript)) {
                    dB.log("...script is good!");
                    dB.echoApproval("Highest scoring script is " + interactScript.getName() + ".");
                    dB.log("Current step for this script is: " + getCurrentStep(player, interactScript.getName()));
                    dB.log(DebugElement.Footer, "");
                }
                return interactScript;
            }
        }

        return null;
    }


    /**
     * Returns the current step for a Player and specified script. If no current step is found, the default
     * step is used, 'Default', unless another default (used by ending the step-name with a '*') is specified
     * in the script. For the sake of compatibility from v0.76, '1' can also be used.
     *
     * @param player     the Player to check
     * @param scriptName the name of the interact script container to check
     * @return the current, or default, step name
     */
    public static String getCurrentStep(dPlayer player, String scriptName) {
        if (scriptName == null) {
            return null;
        }
        // Probe 'saves.yml' for the current step
        if (DenizenAPI._saves().contains("Players." + player.getSaveName()
                + "." + "Scripts." + scriptName.toUpperCase()
                + "." + "Current Step")) {
            return DenizenAPI._saves().getString("Players." + player.getSaveName()
                    + "." + "Scripts." + scriptName.toUpperCase()
                    + "." + "Current Step").toUpperCase();
        }
        // No saved step found, so we'll just use the default
        return ScriptRegistry.getScriptContainerAs(scriptName, InteractScriptContainer.class).getDefaultStepName().toUpperCase();
    }


    /**
     * Used with the getInteractScript method. Overrides Java's compareTo to allow comparisons of
     * possible interact scripts' priorities.
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

