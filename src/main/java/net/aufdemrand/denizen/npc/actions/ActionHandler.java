package net.aufdemrand.denizen.npc.actions;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.objects.dObject;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.scripts.ScriptBuilder;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.core.DetermineCommand;
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

    public String doAction(String actionName, dNPC npc, dPlayer player, AssignmentScriptContainer assignment) {
        return doAction(actionName, npc, player, assignment, null);
    }


    public String doAction(String actionName, dNPC npc, dPlayer player, AssignmentScriptContainer assignment, Map<String, dObject> context) {

        String determination = "none";

        if (assignment == null) {
            // dB.echoDebug("Tried to do 'on " + actionName + ":' but couldn't find a matching script.");
            return determination;
        }

        if (!assignment.contains("actions.on " + actionName)) return determination;

        dB.report(assignment, "Action",
                aH.debugObj("Type", "On " + actionName)
                        + aH.debugObj("NPC", npc.toString())
                        + assignment.getAsScriptArg().debug()
                        + (player != null ? aH.debugObj("Player", player.getName()) : ""));

        // Fetch script from Actions
        List<ScriptEntry> script = assignment.getEntries(new BukkitScriptEntryData(player, npc), "actions.on " + actionName);
        if (script.isEmpty()) return determination;

        // Create new ID -- this is what we will look for when determining an outcome
        long id = DetermineCommand.getNewId();

        // Add the reqId to each of the entries for the determine command
        ScriptBuilder.addObjectToEntries(script, "ReqId", id);

        dB.echoDebug(assignment, DebugElement.Header,
                "Building action 'On " + actionName.toUpperCase() + "' for " + npc.toString());

        // Add entries and context to the queue
        ScriptQueue queue = InstantQueue.getQueue(ScriptQueue.getNextId(assignment.getName())).addEntries(script);

        if (context != null) {
            for (Map.Entry<String, dObject> entry : context.entrySet()) {
                queue.addContext(entry.getKey(), entry.getValue());
            }
        }

        // Start the queue!
        queue.start();

        // Check the determination by asking the DetermineCommand
        if (DetermineCommand.hasOutcome(id))
            determination =  DetermineCommand.getOutcome(id).get(0);
        // TODO: Multiple determination system
        return determination;
    }
}
