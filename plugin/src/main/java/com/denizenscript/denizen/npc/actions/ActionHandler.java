package com.denizenscript.denizen.npc.actions;

import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.scripts.containers.core.AssignmentScriptContainer;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizencore.events.OldEventManager;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.queues.ScriptQueue;
import com.denizenscript.denizencore.scripts.queues.core.InstantQueue;
import com.denizenscript.denizencore.utilities.debugging.Debug.DebugElement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActionHandler {

    final Denizen denizen;

    public ActionHandler(Denizen denizen) {
        this.denizen = denizen;
    }

    public String doAction(String actionName, NPCTag npc, PlayerTag player, AssignmentScriptContainer assignment) {
        return doAction(actionName, npc, player, assignment, null);
    }


    public String doAction(String actionName, NPCTag npc, PlayerTag player, AssignmentScriptContainer assignment, Map<String, ObjectTag> context) {

        if (context == null) {
            context = new HashMap<>();
        }

        String determination = "none";

        if (assignment == null) {
            // dB.echoDebug("Tried to do 'on " + actionName + ":' but couldn't find a matching script.");
            return determination;
        }

        if (!assignment.contains("actions.on " + actionName)) {
            return determination;
        }

        Debug.report(assignment, "Action",
                ArgumentHelper.debugObj("Type", "On " + actionName)
                        + ArgumentHelper.debugObj("NPC", npc.toString())
                        + assignment.getAsScriptArg().debug()
                        + (player != null ? ArgumentHelper.debugObj("Player", player.getName()) : ""));

        // Fetch script from Actions
        List<ScriptEntry> script = assignment.getEntries(new BukkitScriptEntryData(player, npc), "actions.on " + actionName);
        if (script.isEmpty()) {
            return determination;
        }

        Debug.echoDebug(assignment, DebugElement.Header,
                "Building action 'On " + actionName.toUpperCase() + "' for " + npc.toString());

        // Add entries and context to the queue
        ScriptQueue queue = new InstantQueue(assignment.getName()).addEntries(script);

        OldEventManager.OldEventContextSource oecs = new OldEventManager.OldEventContextSource();
        oecs.contexts = context;
        oecs.contexts.put("event_header", new ElementTag(actionName));
        queue.setContextSource(oecs);

        // Start the queue!
        queue.start();

        // Check the determination by asking the DetermineCommand
        if (queue.determinations != null && queue.determinations.size() > 0) {
            determination = queue.determinations.get(0);
        }
        // TODO: Multiple determination system
        return determination;
    }
}
