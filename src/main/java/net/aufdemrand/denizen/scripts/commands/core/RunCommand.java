package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.ScriptQueue;
import net.aufdemrand.denizen.scripts.ScriptRegistry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.scripts.containers.core.TaskScriptContainer;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Runs a task script in a new ScriptQueue.
 * This replaces the now-deprecated runtask command with queue argument.
 *
 * @author Jeremy Schroeder
 *
 */
public class RunCommand extends AbstractCommand {

    public String getHelp() {
        return  "Runs a script in a new ScriptQueue. By using a new and separate" +
                "queue, scripts can be delayed, run instantly, and even used to" +
                "create loops. If wanting to run a series of commands in the same" +
                "queue, use the 'inject' command. \n" +
                " \n" +
                "Use to start an 'event' that is independent of the current script. \n" +
                "- run giant_door_open_script \n" +
                "Use the 'as' argument to attach a player or npc. \n" +
                "- run start_walking as:n@4 \n" +
                "Name the queue with an 'id' argument. \n" +
                "- run goal_tracker id:<p.name>'s_goal_tracker \n" +
                "Put a script's execution off by specifying a delay. \n" +
                "- run gate_closer delay:10s \n" +
                "Use run with a delay and 'loop' argument to create a script on a " +
                "timer. You can even specify the amount of loops. \n" +
                "- run server_announcement loop 1h \n" +
                "You can also attach some definitions to the task script being run. \n" +
                "- run get_color_of def:i@epic_leather_armor";
    }

    public String getUsage() {
        return "run [script] (path:...) (as:p@player|n@npc) (def:...|...) (id:id_name) (delay:duration) (loop) (q:#)";
    }

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
                scriptEntry.addObject("instant", Element.TRUE);

            else if (arg.matchesPrefix("delay")
                    && arg.matchesArgumentType(Duration.class))
                scriptEntry.addObject("delay", arg.asType(Duration.class));

            else if (arg.matches("loop"))
                scriptEntry.addObject("loop", Element.TRUE);

            else if (arg.matchesPrefix("q, quantity")
                    && arg.matchesPrimitive(aH.PrimitiveType.Integer))
                scriptEntry.addObject("quantity", arg.asElement());

            else if (arg.matchesPrefix("a, as")
                    && arg.matchesArgumentType(dPlayer.class))
                scriptEntry.setPlayer((dPlayer) arg.asType(dPlayer.class));

            else if (arg.matchesPrefix("a, as")
                    && arg.matchesArgumentType(dNPC.class))
                scriptEntry.setNPC((dNPC) arg.asType(dNPC.class));

            else if (arg.matchesPrefix("d, def"))
                scriptEntry.addObject("definitions", arg.asType(dList.class));
        }

        if (!scriptEntry.hasObject("script"))
            throw new InvalidArgumentsException("Must define a SCRIPT to be run.");
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

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
                    (String) scriptEntry.getObject("path"));
        // Else, assume standard path
        else entries = script.getContainer().getBaseEntries(
                scriptEntry.getPlayer(),
                scriptEntry.getNPC());

        // Get the 'id' if specified
        String id = (scriptEntry.hasObject("id") ?
                (String) scriptEntry.getObject("id") : ScriptQueue._getNextId());

        // Build the queue
        ScriptQueue queue;
        if (scriptEntry.hasObject("instant"))
            queue = ScriptQueue._getInstantQueue(id).addEntries(entries);
        else queue = ScriptQueue._getQueue(id).addEntries(entries);

        // Set any delay
        if (scriptEntry.hasObject("delay"))
            queue.delayFor(((Duration) scriptEntry.getObject("delay")).getTicks());

        // Set any definitions
        if (scriptEntry.hasObject("definitions")) {
            int x = 1;
            dList definitions = (dList) scriptEntry.getObject("definitions");
            String[] definition_names = null;
            try { definition_names = script.getContainer().getString("definitions").split(","); }
                catch (Exception e) { }
            for (String definition : definitions) {
                queue.context.put(definition_names != null && definition_names.length >= x ?
                definition_names[x - 1] : String.valueOf(x), definition);
                x++;
            }
        }

        dB.log(queue.getQueueSize() + " " + entries.size());

        // OK, GO!
        queue.start();
    }

}