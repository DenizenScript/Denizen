package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.events.ReplaceableTagEvent;
import net.aufdemrand.denizen.objects.dScript;
import net.aufdemrand.denizen.scripts.ScriptBuilder;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.queues.ScriptQueue;
import net.aufdemrand.denizen.scripts.commands.core.DetermineCommand;
import net.aufdemrand.denizen.scripts.queues.core.InstantQueue;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;

public class ProcedureScriptTag implements Listener {

    public ProcedureScriptTag(Denizen denizen) {
        denizen.getServer().getPluginManager().registerEvents(this, denizen);
    }

    // <--[example]
    // @Title Using Procedure Scripts
    // @Description
    // Use procedure scripts to quickly place advanced calculations
    // in multiple scripts.
    //
    // @Code
    // # +--------------------
    // # | Using Procedure Scripts
    // # |
    // # | Sometimes you need a huge chunk of scriptwork just to decide
    // # | what to do with something.
    // # | EG, is your NPC a member of an army? Well, you have 20 different armies to check!
    // # |
    // # | Procedure scripts allow you to run an entire script and get the result from
    // # | it, without even taking a single extra line in the script it's being used in.
    //
    // # +-- The Procedure Script --+
    // ProcedureTutorial:
    //   type: procedure
    //
    //   # The procedure script is much like a task script
    //   # Except it must end with a Determine command
    //
    //   # In this example, we're checking if the player can buy a heal
    //   # So first check if the player has at least $3
    //   # If the player has less than 3 dollars, determine false
    //   - if <player.money> < 3 determine false
    //   # The determine above will immediately end the procedure and return false
    //
    //   # If the player has more than 75% health, he doesn't need a heal so determine false
    //   - if <player.health.percentage> > 75 determine false
    //
    //   # This example shouldn't get too complex, so
    //   # If it passed the money and health requirements:
    //   # First, take the $3
    //   - take money qty:3
    //
    //   # Tell the player they paid, to be nice
    //   - narrate "You lost $3!"
    //
    //   # And, finally, determine true
    //   - determine true
    //
    // # +-- The NPC Assignment --+
    // ProcedureNPC:
    //   type: assignment
    //   actions:
    //     # Using an assignment 'on click' action only because an interact would take too much space
    //     on click:
    //
    //     # This will run the procedure script, and run the commands inside the { braces }
    //     # If it returned true. You could also compare any other value, EG <proc:whatever> == 3
    //     # Depending on the determine command. Remember not to return different types of things in one procedure script
    //     # If one determine returns "true", the other shouldn't return "3"
    //     - if <proc:ProcedureTutorial> {
    //       - heal
    //       - chat "All patched up!"
    //       }
    //
    // -->

   @EventHandler
    public void procedureTag(ReplaceableTagEvent event) {
       // <--[tag]
       // @attribute <proc:<ProcedureScript>>
       // @returns Element
       // @description
       // Returns the 'determine' result of a procedure script
       // See !tutorial procedure
       // -->
    if (!event.matches("proc, pr")) return;
       if (event.getValue() == null) return;

       // Get the script's name from the tag's value
       dScript script = dScript.valueOf(event.getValue());

       if (script == null) {
           dB.echoError("Tried to execute '" + event.getValue() + "', but it couldn't be found!");
           return;
       }

       // Build script entries
       List<ScriptEntry> entries = script.getContainer().getBaseEntries(event.getPlayer(), event.getNPC());

       // Return if no entries built
       if (entries.isEmpty()) return;

       // Create new ID -- this is what we will look for when determining an outcome
       long id = DetermineCommand.getNewId();

       // Add the reqId to each of the entries for referencing
       ScriptBuilder.addObjectToEntries(entries, "ReqId", id);

       InstantQueue.getQueue(ScriptQueue._getNextId()).addEntries(entries).start();

       if (DetermineCommand.hasOutcome(id)) {
           event.setReplaced(DetermineCommand.getOutcome(id));
       }


   }

}
