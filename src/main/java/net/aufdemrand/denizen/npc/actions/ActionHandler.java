package net.aufdemrand.denizen.npc.actions;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.scripts.ScriptBuilder;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.queues.ScriptQueue;
import net.aufdemrand.denizen.scripts.containers.core.AssignmentScriptContainer;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.scripts.queues.core.InstantQueue;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.DebugElement;

import java.util.List;
import java.util.Map;

public class ActionHandler {

    final Denizen denizen;

    public ActionHandler(Denizen denizen) {
        this.denizen = denizen;
    }

    public boolean doAction(String actionName, dNPC npc, dPlayer player, AssignmentScriptContainer assignment) {
        return doAction(actionName, npc, player, assignment, null);
    }

    public boolean doAction(String actionName, dNPC npc, dPlayer player, AssignmentScriptContainer assignment, Map<String, Object> context) {
        if (assignment == null) {
            // dB.echoDebug("Tried to do 'on " + actionName + ":' but couldn't find a matching script.");
            return false;
        }

        if (!assignment.contains("actions.on " + actionName)) return false;

        dB.report("Action",
                aH.debugObj("Type", "On " + actionName)
                        + aH.debugObj("NPC", npc.toString())
                        + assignment.getAsScriptArg().debug()
                        + (player != null ? aH.debugObj("Player", player.getName()) : ""));

        // Fetch script from Actions
        List<ScriptEntry> script = assignment.getEntries(player, npc, "actions.on " + actionName);
        if (script.isEmpty()) return false;

        dB.echoDebug(DebugElement.Header, "Building action 'On " + actionName.toUpperCase() + "' for " + npc.toString());

        if (context != null) {
            for (Map.Entry<String, Object> entry : context.entrySet()) {
                ScriptBuilder.addObjectToEntries(script, entry.getKey(), entry.getValue());
            }
        }

        ScriptQueue queue = InstantQueue.getQueue(null).addEntries(script);
        queue.start();

        // TODO: Read queue context to see if the event behind action should be cancelled.
        // if (queue.getContext() != null
        //    && queue.getContext().equalsIgnoreCase("cancelled")) return true;

        return false;
    }

}
