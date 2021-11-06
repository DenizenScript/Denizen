package com.denizenscript.denizen.npc.actions;

import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.scripts.containers.core.AssignmentScriptContainer;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.queues.ContextSource;
import com.denizenscript.denizencore.scripts.queues.ScriptQueue;
import com.denizenscript.denizencore.scripts.queues.core.InstantQueue;
import com.denizenscript.denizencore.utilities.debugging.Debug.DebugElement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActionHandler {

    public ActionHandler() {
    }

    public String doAction(String actionName, NPCTag npc, PlayerTag player, AssignmentScriptContainer assignment, Map<String, ObjectTag> context) {
        if (context == null) {
            context = new HashMap<>();
        }
        String determination = "none";
        if (assignment == null) {
            return determination;
        }
        if (!assignment.containsScriptSection("actions.on " + actionName)) {
            return determination;
        }
        Debug.report(assignment, "Action", ArgumentHelper.debugObj("Type", "On " + actionName), npc, assignment.getAsScriptArg(), player);
        // Fetch script from Actions
        List<ScriptEntry> script = assignment.getEntries(new BukkitScriptEntryData(player, npc), "actions.on " + actionName);
        if (script.isEmpty()) {
            return determination;
        }
        Debug.echoDebug(assignment, DebugElement.Header, "Building action 'On " + actionName.toUpperCase() + "' for " + npc.toString());
        // Add entries and context to the queue
        ScriptQueue queue = new InstantQueue(assignment.getName());
        queue.addEntries(script);
        ContextSource.SimpleMap src = new ContextSource.SimpleMap();
        src.contexts = context;
        src.contexts.put("event_header", new ElementTag(actionName));
        queue.setContextSource(src);
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
