package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.DenizenCore;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.*;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import net.aufdemrand.denizencore.scripts.commands.Holdable;
import net.aufdemrand.denizencore.scripts.queues.ScriptQueue;
import net.aufdemrand.denizencore.scripts.queues.core.InstantQueue;
import net.aufdemrand.denizencore.scripts.queues.core.TimedQueue;

import java.util.List;

public class RunCommand extends AbstractCommand implements Holdable {

    // <--[command]
    // @Name Run
    // @Syntax run (locally) [<script>] (path:<name>) (def:<element>|...) (id:<name>) (instantly) (speed:<value>) (delay:<value>)
    // @Required 1
    // @Short Runs a script in a new ScriptQueue.
    // @Video /denizen/vids/Run%20And%20Inject
    // @Group core
    //
    // @Description
    // Runs a new script queue, either in the local script or in a different task script.
    //
    // You can set the queue speed using the speed argument
    // this makes the queue run each script command with a delay.
    // Specifying the "instantly" argument will run the queue instantly
    // (speed at 0 ticks; queue running in total of 1 tick, just like an event script)
    // If no speed or "instantly" argument are applied,
    // it assumes the default script speed that are configured.
    //
    // Specifying definitions as argument
    // allows the transfer of definitions to the new queue.
    // Definitions are not carried over if not specified.
    // (See <@link command define>)
    //
    // Specifying a player argument will run the queue with a player attached
    // to that queue. The same can be done to attach an npc.
    // Player and npc are not carried over to the new queue if not specified.
    //
    // @Tags
    // <entry[saveName].created_queue> returns the queue that was started by the run command.
    //
    // @Usage
    // Use to run a new queue instant
    // - run MyNewTask instantly
    //
    // @Usage
    // Use to run a new queue instant
    // - run MyNewTask instantly def:4|20|true
    //
    // @Usage
    // Use to run a new queue with an attached player and npc with a definition
    // - run MyNewTask def:friends player:p@bob npc:<player.selected_npc>
    //
    // @Usage
    // Use to run a new queue instant with the same attached player
    // - run MyNewTask instantly player:<player>
    //
    // @Usage
    // Use to run a new queue from a local script
    // - run locally MyNewTask
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpretArguments(scriptEntry.aHArgs)) {

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
                String path = arg.asElement().asString();
                if (!scriptEntry.hasObject("script")) {
                    int dotIndex = path.indexOf('.');
                    if (dotIndex > 0) {
                        dScript script = new dScript(path.substring(0, dotIndex));
                        if (script.isValid()) {
                            scriptEntry.addObject("script", script);
                            path = path.substring(dotIndex + 1);
                        }
                    }
                }
                scriptEntry.addObject("path", new Element(path));
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
                "FORCE:" + (scriptEntry.getElement("id")).asString() : script.getContainer().getName());

        // Build the queue
        ScriptQueue queue;
        if (scriptEntry.hasObject("instant")) {
            queue = new InstantQueue(id).addEntries(entries);
        }
        else {

            if (scriptEntry.hasObject("speed")) {
                Duration speed = scriptEntry.getdObject("speed");
                queue = ((TimedQueue) new TimedQueue(id).addEntries(entries)).setSpeed(speed.getTicks());
            }
            else {
                // Check speed of the script if a TimedQueue -- if identified, use the speed from the script.
                if (script != null && script.getContainer().contains("SPEED")) {
                    long ticks = Duration.valueOf(script.getContainer().getString("SPEED", "0")).getTicks();
                    if (ticks > 0) {
                        queue = ((TimedQueue) new TimedQueue(id).addEntries(entries)).setSpeed(ticks);
                    }
                    else {
                        queue = new InstantQueue(id).addEntries(entries);
                    }
                }
                else {
                    queue = new TimedQueue(id).addEntries(entries);
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
                dB.echoDebug(scriptEntry, "Adding definition '" + name + "' as " + definition);
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

        // Save the queue for script referencing
        scriptEntry.addObject("created_queue", queue);

        // OK, GO!
        queue.start();
    }
}
