package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.DenizenCore;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.*;
import net.aufdemrand.denizencore.scripts.ScriptBuilder;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import net.aufdemrand.denizencore.scripts.commands.Holdable;
import net.aufdemrand.denizencore.scripts.commands.core.DetermineCommand;
import net.aufdemrand.denizencore.scripts.queues.ScriptQueue;
import net.aufdemrand.denizencore.scripts.queues.core.InstantQueue;
import net.aufdemrand.denizencore.scripts.queues.core.TimedQueue;

import java.util.List;

/**
 * Runs a task script in a new ScriptQueue.
 * This replaces the now-deprecated runtask command with queue argument.
 *
 * @author Jeremy Schroeder
 */

public class RunCommand extends AbstractCommand implements Holdable {

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

            if (arg.matchesPrefix("i", "id")) {
                scriptEntry.addObject("id", arg.asElement());
            }
            else if (arg.matchesPrefix("a", "as")
                    && arg.matchesArgumentType(dPlayer.class)) {
                ((BukkitScriptEntryData) scriptEntry.entryData).setPlayer(arg.asType(dPlayer.class));
                dB.echoError(scriptEntry.getResidingQueue(), "Run as:<player> is outdated, use player:<player>");
            }
            else if (arg.matchesPrefix("a", "as")
                    && arg.matchesArgumentType(dNPC.class)) {
                ((BukkitScriptEntryData) scriptEntry.entryData).setNPC(arg.asType(dNPC.class));
                dB.echoError(scriptEntry.getResidingQueue(), "Run as:<npc> is outdated, use npc:<npc>");
            }

            // Catch invalid entry for 'as' argument
            else if (arg.matchesPrefix("a", "as")) {
                dB.echoError(scriptEntry.getResidingQueue(), "Specified target was not attached. Value must contain a valid PLAYER or NPC object.");
            }
            else if (arg.matchesPrefix("d", "def", "define", "c", "context")) {
                scriptEntry.addObject("definitions", arg.asElement());
            }
            else if (arg.matches("instant", "instantly")) {
                scriptEntry.addObject("instant", new Element(true));
            }
            else if (arg.matchesPrefix("delay")
                    && arg.matchesArgumentType(Duration.class)) {
                scriptEntry.addObject("delay", arg.asType(Duration.class));
            }
            else if (arg.matches("local", "locally")) {
                scriptEntry.addObject("local", new Element("true"));
                scriptEntry.addObject("script", scriptEntry.getScript());
            }
            else if (!scriptEntry.hasObject("script")
                    && arg.matchesArgumentType(dScript.class)
                    && !arg.matchesPrefix("p", "path")) {
                scriptEntry.addObject("script", arg.asType(dScript.class));
            }
            else if (!scriptEntry.hasObject("speed") && arg.matchesPrefix("speed")
                    && arg.matchesArgumentType(Duration.class)) {
                scriptEntry.addObject("speed", arg.asType(Duration.class));
            }
            else if (!scriptEntry.hasObject("path")) {
                scriptEntry.addObject("path", arg.asElement());
            }
            else {
                arg.reportUnhandled();
            }

        }

        if (!scriptEntry.hasObject("script") && (!scriptEntry.hasObject("local") || scriptEntry.getScript() == null)) {
            throw new InvalidArgumentsException("Must define a SCRIPT to be run.");
        }

        if (!scriptEntry.hasObject("path") && scriptEntry.hasObject("local")) {
            throw new InvalidArgumentsException("Must specify a PATH.");
        }

    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        if (scriptEntry.dbCallShouldDebug()) {
            dB.report(scriptEntry, getName(),
                    (scriptEntry.hasObject("script") ? scriptEntry.getdObject("script").debug() : scriptEntry.getScript().debug())
                            + (scriptEntry.hasObject("instant") ? scriptEntry.getdObject("instant").debug() : "")
                            + (scriptEntry.hasObject("path") ? scriptEntry.getElement("path").debug() : "")
                            + (scriptEntry.hasObject("local") ? scriptEntry.getElement("local").debug() : "")
                            + (scriptEntry.hasObject("delay") ? scriptEntry.getdObject("delay").debug() : "")
                            + (scriptEntry.hasObject("id") ? scriptEntry.getdObject("id").debug() : "")
                            + (scriptEntry.hasObject("definitions") ? scriptEntry.getdObject("definitions").debug() : "")
                            + (scriptEntry.hasObject("speed") ? scriptEntry.getdObject("speed").debug() : ""));
        }

        // Get the script
        dScript script = scriptEntry.getdObject("script");

        // Get the entries
        List<ScriptEntry> entries;
        // If it's local
        if (scriptEntry.hasObject("local")) {
            entries = scriptEntry.getScript().getContainer().getEntries(scriptEntry.entryData.clone(),
                    scriptEntry.getElement("path").asString());
            script = scriptEntry.getScript();
        }

        // If it has a path
        else if (scriptEntry.hasObject("path") && scriptEntry.getObject("path") != null) {
            entries = script.getContainer().getEntries(scriptEntry.entryData.clone(),
                    scriptEntry.getElement("path").asString());
        }

        // Else, assume standard path
        else {
            entries = script.getContainer().getBaseEntries(scriptEntry.entryData.clone());
        }

        if (entries == null) {
            dB.echoError(scriptEntry.getResidingQueue(), "Script run failed (invalid path or script name)!");
            return;
        }

        // Get the 'id' if specified
        String id = (scriptEntry.hasObject("id") ?
                (scriptEntry.getElement("id")).asString() : ScriptQueue.getNextId(script.getContainer().getName()));

        // Build the queue
        ScriptQueue queue;
        if (scriptEntry.hasObject("instant")) {
            queue = InstantQueue.getQueue(id).addEntries(entries);
        }
        else {

            if (scriptEntry.hasObject("speed")) {
                Duration speed = scriptEntry.getdObject("speed");
                queue = ((TimedQueue) TimedQueue.getQueue(id).addEntries(entries)).setSpeed(speed.getTicks());
            }
            else {
                // Check speed of the script if a TimedQueue -- if identified, use the speed from the script.
                if (script != null && script.getContainer().contains("SPEED")) {
                    long ticks = Duration.valueOf(script.getContainer().getString("SPEED", "0")).getTicks();
                    if (ticks > 0) {
                        queue = ((TimedQueue) TimedQueue.getQueue(id).addEntries(entries)).setSpeed(ticks);
                    }
                    else {
                        queue = InstantQueue.getQueue(id).addEntries(entries);
                    }
                }
                else {
                    queue = TimedQueue.getQueue(id).addEntries(entries);
                }
            }

        }

        // Set any delay
        if (scriptEntry.hasObject("delay")) {
            queue.delayUntil(DenizenCore.serverTimeMillis + ((Duration) scriptEntry.getObject("delay")).getMillis());
        }

        // Set any definitions
        if (scriptEntry.hasObject("definitions")) {
            int x = 1;
            Element raw_defintions = scriptEntry.getElement("definitions");
            dList definitions = dList.valueOf(raw_defintions.asString());
            String[] definition_names = null;
            try {
                if (script != null && script.getContainer() != null) {
                    String str = script.getContainer().getString("definitions");
                    if (str != null) {
                        definition_names = str.split("\\|");
                    }
                }
            }
            catch (Exception e) {
                // TODO: less lazy handling
            }
            for (String definition : definitions) {
                String name = definition_names != null && definition_names.length >= x ?
                        definition_names[x - 1].trim() : String.valueOf(x);
                queue.addDefinition(name, definition);
                dB.echoDebug(scriptEntry, "Adding definition %" + name + "% as " + definition);
                x++;
            }
            queue.addDefinition("raw_context", raw_defintions.asString());
        }


        // Setup a callback if the queue is being waited on
        if (scriptEntry.shouldWaitFor()) {
            // Record the ScriptEntry
            final ScriptEntry se = scriptEntry;
            queue.callBack(new Runnable() {
                @Override
                public void run() {
                    se.setFinished(true);
                }
            });
        }

        // Allow determinations because why not
        long reqId = DetermineCommand.getNewId();
        queue.setReqId(reqId);

        // Also add the reqId to each of the entries for reasons
        ScriptBuilder.addObjectToEntries(entries, "reqid", reqId);

        // Save the queue for script referencing
        scriptEntry.addObject("created_queue", queue);

        // OK, GO!
        queue.start();
    }
}
