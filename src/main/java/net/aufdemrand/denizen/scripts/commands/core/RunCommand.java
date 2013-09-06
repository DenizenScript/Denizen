package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.queues.ScriptQueue;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.scripts.queues.core.InstantQueue;
import net.aufdemrand.denizen.scripts.queues.core.TimedQueue;
import net.aufdemrand.denizen.utilities.debugging.dB;

import java.util.List;

/**
 * Runs a task script in a new ScriptQueue.
 * This replaces the now-deprecated runtask command with queue argument.
 *
 * @author Jeremy Schroeder
 *
 */
public class RunCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("script")
                    && arg.matchesArgumentType(dScript.class))
                scriptEntry.addObject("script", arg.asType(dScript.class));

            else if (arg.matchesPrefix("i, id"))
                scriptEntry.addObject("id", arg.asElement());

            else if (arg.matchesPrefix("p, path"))
                scriptEntry.addObject("path", arg.asElement());

            else if (arg.matches("instant, instantly"))
                scriptEntry.addObject("instant", new Element(true));

            else if (arg.matchesPrefix("delay")
                    && arg.matchesArgumentType(Duration.class))
                scriptEntry.addObject("delay", arg.asType(Duration.class));

            else if (arg.matches("loop"))
                scriptEntry.addObject("loop", new Element(true));

            else if (arg.matchesPrefix("q, quantity")
                    && arg.matchesPrimitive(aH.PrimitiveType.Integer))
                scriptEntry.addObject("quantity", arg.asElement());

            else if (arg.matchesPrefix("a, as")
                    && arg.matchesArgumentType(dPlayer.class))
                scriptEntry.setPlayer((dPlayer) arg.asType(dPlayer.class));

            else if (arg.matchesPrefix("a, as")
                    && arg.matchesArgumentType(dNPC.class))
                scriptEntry.setNPC((dNPC) arg.asType(dNPC.class));

            else if (arg.matchesPrefix("d, def, define, c, context"))
                scriptEntry.addObject("definitions", arg.asType(dList.class));
        }

        if (!scriptEntry.hasObject("script"))
            throw new InvalidArgumentsException("Must define a SCRIPT to be run.");
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        dB.report(getName(), scriptEntry.getdObject("script").debug()
        + (scriptEntry.hasObject("instant") ? scriptEntry.getdObject("instant").debug() : ""));

        // definitions
        // loop
        // quantity
        // delay
        // instant

        // Get the script
        dScript script = (dScript) scriptEntry.getObject("script");

        // Get the entries
        List<ScriptEntry> entries;
        // If a path is specified
        if (scriptEntry.hasObject("path"))
            entries = script.getContainer().getEntries(
                    scriptEntry.getPlayer(),
                    scriptEntry.getNPC(),
                    scriptEntry.getElement("path").asString());

        // Else, assume standard path
        else entries = script.getContainer().getBaseEntries(
                scriptEntry.getPlayer(),
                scriptEntry.getNPC());

        // Get the 'id' if specified
        String id = (scriptEntry.hasObject("id") ?
                (scriptEntry.getElement("id")).asString() : ScriptQueue._getNextId());

        // Build the queue
        ScriptQueue queue;
        if (scriptEntry.hasObject("instant"))
            queue = InstantQueue.getQueue(id).addEntries(entries);
        else queue = TimedQueue.getQueue(id).addEntries(entries);

        // Set any delay
        if (scriptEntry.hasObject("delay"))
            queue.delayUntil(System.currentTimeMillis() + ((Duration) scriptEntry.getObject("delay")).getMillis());

        // Set any definitions
        if (scriptEntry.hasObject("definitions")) {
            int x = 1;
            dList definitions = (dList) scriptEntry.getObject("definitions");
            String[] definition_names = null;
            try { definition_names = script.getContainer().getString("definitions").split("\\|"); }
                catch (Exception e) { }
            for (String definition : definitions) {
                queue.addDefinition(definition_names != null && definition_names.length >= x ?
                definition_names[x - 1].trim() : String.valueOf(x), definition);
                x++;
            }
        }

        // OK, GO!
        queue.start();
    }

}