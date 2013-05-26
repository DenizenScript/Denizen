package net.aufdemrand.denizen.npc.actions;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.npc.dNPC;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.ScriptQueue;
import net.aufdemrand.denizen.scripts.containers.core.AssignmentScriptContainer;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.DebugElement;
import org.bukkit.entity.Player;

import java.util.List;

public class ActionHandler {

    final Denizen denizen;

    public ActionHandler(Denizen denizen) {
        this.denizen = denizen;
    }

    public boolean doAction(String actionName, dNPC npc, Player player, AssignmentScriptContainer assignment) {

        if (assignment == null) return false;

        if (!assignment.contains("actions.on " + actionName)) {
            // dB.echoDebug("Tried to do 'on " + actionName + ":' but couldn't find a matching script.");
            return false;
        }

        dB.report("Action",
                aH.debugObj("Type", "On " + actionName)
                        + aH.debugObj("NPC", npc.toString())
                        + assignment.getAsScriptArg().debug()
                        + (player != null ? aH.debugObj("Player", player.getName()) : ""));

        // Fetch script from Actions
        List<ScriptEntry> script = assignment.getBaseEntries(player, npc, "actions.on " + actionName);
        if (script.isEmpty()) return false;

        dB.echoDebug(DebugElement.Header, "Building action 'On " + actionName.toUpperCase() + "' for " + npc.toString());

        ScriptQueue queue = ScriptQueue._getInstantQueue(ScriptQueue._getNextId()).addEntries(script);
        queue.start();

        // TODO: Read queue context to see if the event behind action should be cancelled.
        // if (queue.getContext() != null
        //    && queue.getContext().equalsIgnoreCase("cancelled")) return true;

        return false;
    }

}
