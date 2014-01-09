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

    // <--[example]
    // @Title Using Local Scripts tutorial
    // @Description
    // Use local scripts as a way to avoid making unnecessary script containers
    // or to group together utility task scripts.
    //
    // @Code
    // # +--------------------
    // # | Using Local Scripts tutorial
    // # |
    // # | Since Script Containers are stored inside Denizen on a global level,
    // # | the problem of duplicate container names can become a problem.
    // # |
    // # | Using local scripts can be a good way to avoid situations by cutting
    // # | down on the amount of total script containers needed by allowing task utility
    // # | scripts to be included in other containers, or grouped together in a single
    // # | container.
    //
    // Local Script Tutorial:
    //   type: task
    //
    //   # As you probably already know, to run a 'base script' inside a task script
    //   # the 'run' or 'inject' commands can be used. This requires the name of the
    //   # script container as an argument in the commands. For example, type
    //   # /ex run 's@Local Script Tutorial' .. to run the script
    //   # below.
    //
    //   script:
    //   - narrate "This is the 'base script' of this task script container."
    //   - narrate "The current time is <util.date.time>!"
    //
    //
    //   # Local Script support by Denizen allows you to stash more scripts. Just specify
    //   # a new node. To run this script from other containers, specify the script as well as
    //   # the local script name node with a 'p' or 'path:' prefix. For example, to run the
    //   # script below, type /ex run 's@Local Script Tutorial' 'p:subscript_1'
    //
    //   subscript_1:
    //   - narrate "This is a 'local script' in the task script container 'LocalScript Tutorial'."
    //
    //   # But wait, there's more! If wanting to run a local script that is within the
    //   # same container, the run command can be even simpler by specifying 'local'
    //   # in place of the script name. Take a look at the next two local scripts. Type
    //   # /ex run 's@Local Script Tutorial' 'p:subscript_2' .. to run the script below
    //   # which will in turn run 'subscript_3' locally. Notice if you specify locally,
    //   # the script used is
    //
    //   subscript_2:
    //   - narrate "This is the second 'local script' in this task script container."
    //   - narrate "This script will now run 'subscript_3'."
    //   - run locally 'subscript_3'
    //
    //   subscript_3:
    //   - narrate "Done. This has been a message from subscript_3!"
    //
    //
    //   # There you have it! Three separate scripts inside a single task script container!
    //   # Both the 'run' command and 'inject' command support local scripts.
    //
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (arg.matchesPrefix("i, id"))
                scriptEntry.addObject("id", arg.asElement());

            else if (arg.matchesPrefix("a, as")
                    && arg.matchesArgumentType(dPlayer.class))
                scriptEntry.setPlayer((dPlayer) arg.asType(dPlayer.class));

            else if (arg.matchesPrefix("a, as")
                    && arg.matchesArgumentType(dNPC.class))
                scriptEntry.setNPC((dNPC) arg.asType(dNPC.class));

                // Catch invalid entry for 'as' argument
            else if (arg.matchesPrefix("a, as"))
                dB.echoDebug(scriptEntry, "Specified target was not attached. Value must contain a valid PLAYER or NPC object.");

            else if (arg.matchesPrefix("d, def, define, c, context"))
                scriptEntry.addObject("definitions", arg.asType(dList.class));

            else if (arg.matches("instant, instantly"))
                scriptEntry.addObject("instant", new Element(true));

            else if (arg.matchesPrefix("delay")
                    && arg.matchesArgumentType(Duration.class))
                scriptEntry.addObject("delay", arg.asType(Duration.class));

            else if (arg.matches("local, locally"))
                scriptEntry.addObject("local", new Element(true));

            else if (!scriptEntry.hasObject("script")
                    && arg.matchesArgumentType(dScript.class)
                    && !arg.matchesPrefix("p, path"))
                scriptEntry.addObject("script", arg.asType(dScript.class));

            else if (!scriptEntry.hasObject("path"))
                scriptEntry.addObject("path", arg.asElement());

            else arg.reportUnhandled();

        }

        if (!scriptEntry.hasObject("script") && !scriptEntry.hasObject("local"))
            throw new InvalidArgumentsException("Must define a SCRIPT to be run.");

        if (!scriptEntry.hasObject("path") && scriptEntry.hasObject("local"))
            throw new InvalidArgumentsException("Must specify a PATH.");

    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        dB.report(scriptEntry, getName(),
                (scriptEntry.hasObject("script") ? scriptEntry.getdObject("script").debug() : scriptEntry.getScript().debug())
                        + (scriptEntry.hasObject("instant") ? scriptEntry.getdObject("instant").debug() : "")
                        + (scriptEntry.hasObject("path") ? scriptEntry.getElement("path").debug() : "")
                        + (scriptEntry.hasObject("local") ? scriptEntry.getElement("local").debug() : "")
                        + (scriptEntry.hasObject("delay") ? scriptEntry.getdObject("delay").debug() : ""));

        // Get the script
        dScript script = (dScript) scriptEntry.getObject("script");

        // Get the entries
        List<ScriptEntry> entries;
        // If it's local
        if (scriptEntry.hasObject("local"))
            entries = scriptEntry.getScript().getContainer().getEntries(
                    scriptEntry.getPlayer(),
                    scriptEntry.getNPC(),
                    scriptEntry.getElement("path").asString());

            // If it has a path
        else if (scriptEntry.hasObject("path") && scriptEntry.getObject("path") != null)
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
        else {
            queue = TimedQueue.getQueue(id).addEntries(entries);

            // Check speed of the script if a TimedQueue -- if identified, use the speed from the script.
            if (script.getContainer() != null && script.getContainer().contains("speed"))
                ((TimedQueue) queue).setSpeed(Duration.valueOf(script.getContainer().getString("speed")).getTicks());

        }
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
                String name = definition_names != null && definition_names.length >= x ?
                        definition_names[x - 1].trim() : String.valueOf(x);
                queue.addDefinition(name, definition);
                dB.echoDebug(scriptEntry, "Adding definition %" + name + "% as " + definition);
                x++;
            }
        }

        // OK, GO!
        queue.start();
    }

}
