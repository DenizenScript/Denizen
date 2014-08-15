package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.events.bukkit.ReplaceableTagEvent;
import net.aufdemrand.denizen.objects.ObjectFetcher;
import net.aufdemrand.denizen.objects.dList;
import net.aufdemrand.denizen.objects.dScript;
import net.aufdemrand.denizen.scripts.ScriptBuilder;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.queues.ScriptQueue;
import net.aufdemrand.denizen.scripts.commands.core.DetermineCommand;
import net.aufdemrand.denizen.scripts.queues.core.InstantQueue;
import net.aufdemrand.denizen.tags.Attribute;
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
    //   # You can define variables to insert into the procedure, named here and separated by the |pipe| symbol.
    //   # You can have as many as you want - anywhere from none to several thousand.
    //   # Note: If you don't name the definitions but they are given anyway, they will be added as
    //   # %1%, %2%, and so on.
    //   definitions: price|moneysign
    //   # (For the purposes of this example, assume price = 3 dollars, and moneysign holds the '&' symbol)
    //
    //   script:
    //   # The procedure script is much like a task script
    //   # Except it must contain a Determine command
    //
    //   # In this example, we're checking if the player can buy a heal
    //   # So first check if the player has at least $3
    //   # If the player has less than 3 dollars, determine false
    //   - if <player.money> < %price% determine false
    //   # The determine above will immediately end the procedure and return false
    //
    //   # If the player has more than 80% health, he doesn't need a heal so determine false
    //   - if <player.health.percentage> > 80 determine false
    //
    //   # This example shouldn't get too complex, so
    //   # If it passed the money and health requirements:
    //   # First, take the $3
    //   - take money qty:%price%
    //
    //   # Tell the player they paid, to be nice
    //   - narrate "You lost %moneysign%%price%!"
    //   # Remember that due to the definitions this script requres, %moneysign% becomes '&' and price becomes '3'
    //
    //   # And, finally, determine true, to inform the calling script that it passed
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
    //     # If it returned true. You could also compare any other value, EG <proc[whatever]> == 3
    //     # Depending on the determine command. Remember not to return different types of things in one procedure script
    //     # If one determine returns "true", the other shouldn't return "3"
    //
    //     # the .context[3|$] adds the two definitions: 'price' as '3', and 'moneysign' as '&'
    //     # Remember that if you don't need context, you can just entirely remove the '.context[]' portion
    //     - if <proc[ProcedureTutorial].context[3|&]> {
    //       - heal
    //       - chat "All patched up!"
    //       }
    //       else {
    //       - chat "You need $3 for a heal!"
    //       }
    //     # The 'else' braced section will be run if the procedure returned 'false'.
    //
    // -->

    @EventHandler
    public void procedureTag(ReplaceableTagEvent event) {

        // <--[tag]
        // @attribute <proc[ProcedureScript].Context[<element>|...]>
        // @returns dObject
        // @description
        // Returns the 'determine' result of a procedure script with the given context.
        // See <@link example Using Procedure Scripts>.
        // -->

        // <--[tag]
        // @attribute <proc[ProcedureScript]>
        // @returns dObject
        // @description
        // Returns the 'determine' result of a procedure script.
        // See <@link example Using Procedure Scripts>.
        // -->
        if (!event.matches("proc, pr")) return;

        Attribute attr = event.getAttributes();
        int attribs = 1;

        dScript script = null;
        String path = null;

        if (event.hasNameContext()) {
            if (event.getNameContext().indexOf('.') > 0) {
                String[] split = event.getNameContext().split("\\.", 2);
                path = split[1];
                script = dScript.valueOf(split[0]);

            } else script = dScript.valueOf(event.getNameContext());

        } else if (event.getValue() != null) {
            script = dScript.valueOf(event.getValue());

        } else {
            dB.echoError("Invalid procedure script tag '" + event.getValue() + "'!");
            return;
        }

        if (script == null) {
            dB.echoError("Missing script for procedure script tag '" + event.getValue() + "'!");
            return;
        }

        // Build script entries
        List<ScriptEntry> entries;
        if (path != null)
            entries = script.getContainer().getEntries(event.getPlayer(), event.getNPC(), path);
        else
            entries = script.getContainer().getBaseEntries(event.getPlayer(), event.getNPC());

        // Return if no entries built
        if (entries.isEmpty()) return;

        // Create new ID -- this is what we will look for when determining an outcome
        long id = DetermineCommand.getNewId();

        // Add the reqId to each of the entries for referencing
        ScriptBuilder.addObjectToEntries(entries, "ReqId", id);

        InstantQueue queue = InstantQueue.getQueue(ScriptQueue._getNextId());
        queue.addEntries(entries);
        queue.setReqId(id);
        if (event.hasType() &&
                event.getType().equalsIgnoreCase("context") &&
                event.hasTypeContext()) {
            attribs = 2;
            int x = 1;
            dList definitions = new dList(event.getTypeContext());
            String[] definition_names = null;

            try { definition_names = script.getContainer().getString("definitions").split("\\|");
            } catch (Exception e) { }

            for (String definition : definitions) {
                String name = definition_names != null && definition_names.length >= x ?
                        definition_names[x - 1].trim() : String.valueOf(x);
                queue.addDefinition(name, definition);
                dB.echoDebug(event.getScriptEntry(), "Adding definition %" + name + "% as " + definition);
                x++;
            }
        }

        queue.start();

        if (DetermineCommand.hasOutcome(id)) {
            event.setReplaced(ObjectFetcher.pickObjectFor(DetermineCommand.getOutcome(id))
                    .getAttribute(attr.fulfill(attribs)));
        }
    }
}
