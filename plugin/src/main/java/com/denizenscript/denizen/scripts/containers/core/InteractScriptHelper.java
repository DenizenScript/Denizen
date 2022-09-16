package com.denizenscript.denizen.scripts.containers.core;

import com.denizenscript.denizen.npc.traits.AssignmentTrait;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.scripts.commands.core.CooldownCommand;
import com.denizenscript.denizen.scripts.triggers.AbstractTrigger;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.TimeTag;
import com.denizenscript.denizencore.scripts.ScriptRegistry;
import com.denizenscript.denizencore.utilities.debugging.Debug.DebugElement;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class InteractScriptHelper {

    public static List<InteractScriptContainer> getInteractScripts(NPCTag npc) {
        if (npc == null) {
            return null;
        }
        AssignmentTrait trait = npc.getCitizen().getTraitNullable(AssignmentTrait.class);
        if (trait == null) {
            return null;
        }
        ArrayList<InteractScriptContainer> result = new ArrayList<>();
        for (AssignmentScriptContainer container : trait.containerCache) {
            if (container != null) {
                InteractScriptContainer interact = container.getInteract();
                if (interact != null && interact.shouldEnable()) {
                    result.add(interact);
                }
            }
        }
        return result.isEmpty() ? null : result;
    }

    public static List<InteractScriptContainer> getInteractScripts(NPCTag npc, PlayerTag player, boolean showDebug, Class<? extends AbstractTrigger> trigger) {
        if (player == null || trigger == null) {
            return null;
        }
        List<InteractScriptContainer> interactScripts = getInteractScripts(npc);
        if (interactScripts == null) {
            return null;
        }
        for (int i = 0; i < interactScripts.size(); i++) {
            InteractScriptContainer interactScript = interactScripts.get(i);
            if (Debug.shouldDebug(interactScript) && showDebug) {
                Debug.log(DebugElement.Header, "Getting interact script: n@" + npc.getName() + "/p@" + player.getName());
            }
            if (!CooldownCommand.checkCooldown(player, interactScript.getName())) {
                if (Debug.shouldDebug(interactScript) && showDebug) {
                    Debug.log(ChatColor.GOLD + interactScript.getName() + " isn't cooled down yet! Skipping.");
                    interactScripts.remove(i--);
                    continue;
                }
            }
            if (Debug.shouldDebug(interactScript) && showDebug) {
                Debug.log("Interact script is " + interactScript.getName() + ". Current step for this script is: " + getCurrentStep(player, interactScript.getName()));
                Debug.log(DebugElement.Footer, "");
            }
        }
        return interactScripts.isEmpty() ? null : interactScripts;
    }

    public static String getCurrentStep(PlayerTag player, String scriptName) {
        if (scriptName == null) {
            return null;
        }
        ObjectTag step = player.getFlagTracker().getFlagValue("__interact_step." + scriptName);
        if (step != null) {
            return step.toString().toUpperCase();
        }
        return ScriptRegistry.getScriptContainerAs(scriptName, InteractScriptContainer.class).getDefaultStepName().toUpperCase();
    }

    public static TimeTag getStepExpiration(PlayerTag player, String scriptName) {
        if (scriptName == null) {
            return null;
        }
        return player.getFlagTracker().getFlagExpirationTime("__interact_step." + scriptName);
    }
}
