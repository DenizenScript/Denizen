package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.Duration;
import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.dScript;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.ScriptQueue;
import net.aufdemrand.denizen.scripts.ScriptRegistry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.scripts.containers.core.TaskScriptContainer;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;

import java.util.HashMap;
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
                "timer. You can even specify the amount of loops.\n" +
                "- run server_announcement loop 1h";
                //"Attach some definitions to "
    }

    public String getUsage() {
        return "run [script] (as:p@player|n@npc) (id:id_name) (delay:duration) (loop) (q:#)";
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Initialize necessary fields
        Map<String, String> context = null;
        Boolean instant = false;
        dScript script = null;
        Duration delay = null;
        ScriptQueue queue = scriptEntry.getResidingQueue();

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("script")
                    && arg.matchesArgumentType(dScript.class))
                scriptEntry.addObject("script", arg.asType(dScript.class));

            else if (arg.matchesPrefix("i, id"))
                scriptEntry.addObject("id", arg.asElement());

            else if (arg.matches("instant, instantly"))
                scriptEntry.addObject("instant", Element.TRUE);

            else if (arg.matchesPrefix("d, delay")
                    && arg.matchesArgumentType(Duration.class))
                scriptEntry.addObject("delay", arg.asType(Duration.class));

            else if (arg.matches("loop"))
                scriptEntry.addObject("loop", Element.TRUE);

            else if (arg.matchesPrefix("q, quantity")
                && arg.matchesPrimitive(aH.PrimitiveType.Integer))
                scriptEntry.addObject("quantity", arg.asElement());
        }

        if (!scriptEntry.hasObject("script"))
            throw new InvalidArgumentsException("Must define a SCRIPT to be run.");
    }

    @SuppressWarnings("unchecked")
	@Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        Map<String, String> context = (HashMap<String, String>) scriptEntry.getObject("context");
        dScript script = (dScript) scriptEntry.getObject("script");
        ScriptQueue queue = (ScriptQueue) scriptEntry.getObject("queue");
        Boolean instant = (Boolean) scriptEntry.getObject("instant");
        Duration delay = (Duration) scriptEntry.getObject("delay");

        // Debug output
        dB.report(this.getName(),
                script.debug()
                        + (delay != null ? delay.debug() : "")
                        + aH.debugObj("Instant", instant.toString())
                        + aH.debugObj("Queue", queue.id)
                        + (instant == false ? aH.debugObj("Speed", queue.getSpeed()) : "" )
                        + (context != null ? aH.debugObj("Context", context.toString()) : "")
                        + (scriptEntry.getPlayer() != null
                        ? aH.debugObj("Player", scriptEntry.getPlayer().getName()) : "")
                        + (scriptEntry.getNPC() != null
                        ? aH.debugObj("NPC", scriptEntry.getNPC().toString()) : ""));

        if (instant) {
            // Instant, but no delay
            if (delay == null)

                if (scriptEntry.getResidingQueue() != queue)
                    // Instant, no delay, new queue
                    ((TaskScriptContainer) script.getContainer()).setSpeed(Duration.valueOf("0"))
                            .runTaskScript(queue.id,
                                    scriptEntry.getPlayer(),
                                    scriptEntry.getNPC(),
                                    context);

                else
                    // Instant, no delay, injection into current queue
                    ((TaskScriptContainer) script.getContainer()).setSpeed(Duration.valueOf("0"))
                            .injectTaskScript(queue.id,
                                    scriptEntry.getPlayer(),
                                    scriptEntry.getNPC(),
                                    context);


            else
                // Instant, has delay, new queue
                ((TaskScriptContainer) script.getContainer()).setSpeed(Duration.valueOf("0"))
                        .runTaskScriptWithDelay(queue.id,
                                scriptEntry.getPlayer(),
                                scriptEntry.getNPC(),
                                context,
                                delay);

        } else {

            if (delay == null)

                // Not instant, no delay, new queue
                if (scriptEntry.getResidingQueue() != queue)
                    ((TaskScriptContainer) script.getContainer())
                            .runTaskScript(queue.id,
                                    scriptEntry.getPlayer(),
                                    scriptEntry.getNPC(),
                                    context);

                else
                    // Not instant, no delay, injection into current queue
                    ((TaskScriptContainer) script.getContainer())
                            .injectTaskScript(queue.id,
                                    scriptEntry.getPlayer(),
                                    scriptEntry.getNPC(),
                                    context);


            else
                // Not instant, delayed, new queue
                ((TaskScriptContainer) script.getContainer())
                        .runTaskScriptWithDelay(queue.id,
                                scriptEntry.getPlayer(),
                                scriptEntry.getNPC(),
                                context,
                                delay);
        }
    }

}