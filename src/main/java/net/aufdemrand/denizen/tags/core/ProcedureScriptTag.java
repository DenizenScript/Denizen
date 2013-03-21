package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.events.ReplaceableTagEvent;
import net.aufdemrand.denizen.scripts.ScriptBuilder;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.ScriptQueue;
import net.aufdemrand.denizen.scripts.commands.core.DetermineCommand;
import net.aufdemrand.denizen.utilities.arguments.Script;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;

public class ProcedureScriptTag implements Listener {

    public ProcedureScriptTag(Denizen denizen) {
        denizen.getServer().getPluginManager().registerEvents(this, denizen);
    }

   @EventHandler
    public void utilTags(ReplaceableTagEvent event) {
    if (!event.matches("PROC")) return;
       if (event.getValue() == null) return;

       // Get the script's name from the tag's value
       Script script = aH.getScriptFrom(event.getValue());

       if (script == null) {
           dB.echoError("Tried to call a procedure script, but couldn't find a match!");
           return;
       }

       // Build script entries
       List<ScriptEntry> entries = script.getContainer().getBaseEntries(event.getPlayer(), event.getNPC());

       // Return if no entries built
       if (entries.isEmpty()) return;

       // Create new ID -- this is what we will look for when determining an outcome
       long id = DetermineCommand.getNewId();

       // Add the reqId to each of the entries
       ScriptBuilder.addObjectToEntries(entries, "ReqId", id);

       ScriptQueue._getInstantQueue(ScriptQueue._getNextId()).addEntries(entries).start();

       if (DetermineCommand.hasOutcome(id)) {
           event.setReplaced(DetermineCommand.getOutcome(id));
       }


   }
    
}