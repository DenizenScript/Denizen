package com.denizenscript.denizen.scripts.containers.core;

import com.denizenscript.denizen.utilities.DenizenAPI;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.scripts.commands.core.CooldownCommand;
import com.denizenscript.denizen.scripts.triggers.AbstractTrigger;
import com.denizenscript.denizencore.scripts.ScriptRegistry;
import com.denizenscript.denizencore.utilities.debugging.Debug.DebugElement;
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
    public static InteractScriptContainer getInteractScript(NPCTag npc, PlayerTag player,
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
        if (Debug.shouldDebug(assignmentScript)) {
            Debug.log(DebugElement.Header, "Getting interact script: n@" + npc.getName() + "/p@" + player.getName());
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
                    Debug.echoError("Invalid Interact assignment for '" + entry + "'. Is the script name missing?");
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
                        if (Debug.shouldDebug(interactScript)) {
                            Debug.log(ChatColor.GOLD + " ...but, isn't cooled down, yet! Skipping.");
                        }
                    }
                }
                else {
                    // Alert the console
                    Debug.echoError("'" + entry + "' is not a valid Interact Script. Is there a duplicate script by this name?");
                }
            }
            catch (Exception e) {
                // Had a problem checking requirements, most likely a Legacy Requirement with bad syntax. Alert the console!
                Debug.echoError(ChatColor.RED + "'" + entry + "' has a bad requirement, skipping.");
                Debug.echoError(e);
            }

            if (Debug.shouldDebug(assignmentScript)) {
                Debug.log(DebugElement.Spacer, null);
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
            if (Debug.shouldDebug(interactScript)) {
                Debug.echoApproval("Highest scoring script is " + script + ".");
            }
            if (Debug.shouldDebug(assignmentScript)) {
                Debug.log("Current step for this script is: " + getCurrentStep(player, script));
            }
            if (Debug.shouldDebug(interactScript)) {
                Debug.log(DebugElement.Footer, "");
            }
            return interactScript;
        }

        // Or, if list is empty.. no scripts meet requirements!
        else if (interactableScripts.isEmpty()) {
            if (Debug.shouldDebug(assignmentScript)) {
                Debug.log(ChatColor.YELLOW + "+> " + ChatColor.WHITE + "No scripts meet requirements!");
                Debug.log(DebugElement.Footer, "");
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

            if (Debug.shouldDebug(interactScript)) {
                Debug.log("Checking script '" + interactableScripts.get(a).getName() + "'.");
            }

            // Check for 'Overlay' assignment mode.
            // If specified as an 'Overlay', the criteria for matching requires the
            // specified trigger to have a script in the step.
            if (interactableScripts.get(a).getName().startsWith("^")) {

                // This is an Overlay Assignment, check for the appropriate Trigger Script...
                // If Trigger exists, cool, this is our script.
                if (interactScript.containsTriggerInStep(getCurrentStep(player, interactScript.getName()), trigger)) {
                    if (Debug.shouldDebug(interactScript)) {
                        Debug.log("...found trigger!");
                        Debug.echoApproval("Highest scoring script is " + interactScript.getName() + ".");
                        Debug.log("Current step for this script is: " + getCurrentStep(player, interactScript.getName()));
                        Debug.log(DebugElement.Footer, "");
                    }
                    return interactScript;
                }
                else {
                    if (Debug.shouldDebug(interactScript)) {
                        Debug.log("...no trigger on this overlay assignment. Skipping.");
                    }
                }
            }

            // Not an Overlay Assignment, so return this script, which is the highest scoring.
            else {
                if (Debug.shouldDebug(interactScript)) {
                    Debug.log("...script is good!");
                    Debug.echoApproval("Highest scoring script is " + interactScript.getName() + ".");
                    Debug.log("Current step for this script is: " + getCurrentStep(player, interactScript.getName()));
                    Debug.log(DebugElement.Footer, "");
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
    public static String getCurrentStep(PlayerTag player, String scriptName) {
        if (scriptName == null) {
            return null;
        }
        // Probe 'saves.yml' for the current step
        if (DenizenAPI.getSaves().contains("Players." + player.getSaveName()
                + "." + "Scripts." + scriptName.toUpperCase()
                + "." + "Current Step")) {
            return DenizenAPI.getSaves().getString("Players." + player.getSaveName()
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

