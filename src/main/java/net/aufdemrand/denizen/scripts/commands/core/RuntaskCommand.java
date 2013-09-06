package net.aufdemrand.denizen.scripts.commands.core;

import java.util.HashMap;
import java.util.Map;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.dScript;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.queues.ScriptQueue;
import net.aufdemrand.denizen.scripts.ScriptRegistry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.scripts.containers.core.TaskScriptContainer;
import net.aufdemrand.denizen.objects.Duration;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.scripts.queues.core.InstantQueue;
import net.aufdemrand.denizen.scripts.queues.core.TimedQueue;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;

/**
 * Runs a task script.
 *
 * <b>dScript Usage:</b><br>
 * <pre>RUNTASK (ID:id_name{script_name}) [SCRIPT:script_name] (INSTANT|QUEUE:QueueType{PLAYER_TASK})
 *     (DELAY:#{0}) </pre>
 *
 * <ol><tt>Arguments: [] - Required, () - Optional, {} - Default</ol></tt>
 *
 * <ol><tt>(ID:id_name{script_name})</tt><br>
 *         The unique ID of this task, useful if the possibility of a CANCELTASK command may be used.
 *         If not specified, the name of the script specified is used.</ol>
 *
 * <ol><tt>[SCRIPT:script_name]</tt><br>
 *         The name of the script that should be run.</ol>
 *
 * <ol><tt>(INSTANT|QUEUE:QueueType{PLAYER_TASK})</tt><br>
 *         Specifies how the script should be run. If using INSTANT, all commands in the script are
 *         run without being queued, resulting in an 'instant' execution of all commands (the initial DELAY
 *         will still be honored). If using a QUEUE, choose whether to use PLAYER_TASK or NPC queue-type.
 *         Default is PLAYER_TASK queue if not specified otherwise.</ol>
 *
 * <ol><tt>(DELAY:#{0})</tt><br>
 *         Specifying a delay will set the script to be run in the future. Uses the dScript time format,
 *         (ie. 30, 6m, 1h, etc). Delayed RUNTASKs can be cancelled with the CANCELTASK command when given
 *         the specified ID. Also note: Delayed Tasks WILL BE LOST on a server reboot, so delaying scripts
 *         for long periods of time may not be honored if your server reboots during the wait period.</ol>
 *
 * <br><b>Example Usage:</b><br>
 * <ol><tt>
 *  - RUNTASK ID:&#60;player.name>_spiders SCRIPT:Spawn_spiders INSTANT DELAY:60
 *  - RUNTASK SCRIPT:Drop_reward QUEUE:NPC
 *  - RUNTASK 'SCRIPT:Welcome to Chakkor'
 * </ol></tt>
 *
 *
 * @author Jeremy Schroeder
 *
 */
@Deprecated
public class RuntaskCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Initialize necessary fields
        Map<String, String> context = null;
        Boolean instant = false;
        dScript script = null;
        Duration delay = null;
        String queue = scriptEntry.getResidingQueue().id;

        // Iterate through Arguments to extract needed information
        for (String arg : scriptEntry.getArguments()) {

            // Specify scriptContainer to use
            if (aH.matchesScript(arg)) {
                script = aH.getScriptFrom(arg);

            }   // Delay the start of the queue
            else if (aH.matchesValueArg("DELAY", arg, aH.ArgumentType.Duration)) {
                delay = aH.getDurationFrom(arg);

            }   // Use a specific queue
            else if (aH.matchesQueue(arg)) {
                queue = aH.getStringFrom(arg);

            }   // TODO: Remove this argument for version 1.0
            else if (aH.matchesValueArg("SPEED", arg, aH.ArgumentType.Duration)) {
                dB.log("SPEED argument has been removed from RUNTASK! Instead, specify " +
                        "a speed on the task script itself, or use the 'QUEUE SET_SPEED:#' command " +
                        "inside the task script. This warning will be removed in version 1.0 " +
                        "and this argument deprecated.");

            }   // Gets a new, randomly named queue
            else if (aH.matchesArg("QUEUE", arg)) {
                queue = ScriptQueue._getNextId();
                instant = false;

            }   // Run the script instantly.
            else if (aH.matchesArg("INSTANT, INSTANTLY", arg)) {
                queue = ScriptQueue._getNextId();
                instant = true;

            }   // Build context map if specified
            else if (aH.matchesContext(arg)) {
                context = aH.getContextFrom(arg);

            }   // Specify a script name without the 'script:' prefix
            else if (ScriptRegistry.containsScript(aH.getStringFrom(arg))) {
                script = aH.getScriptFrom(arg);
                if (!script.getType().equalsIgnoreCase("TASK"))
                    script = null;

            } else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
        }

        // Must specify at least a valid script to run...
        if (script == null)
            throw new InvalidArgumentsException("Must define a SCRIPT to be run.");
        // If not queue, and delayed, throw an exception... this cannot happen.
        if (queue.equals(scriptEntry.getResidingQueue().id) && delay != null)
            throw new InvalidArgumentsException("Cannot delay an INJECTED task script! Use 'QUEUE'.");

        // Put important objects inside the scriptEntry to be sent to execute()
        scriptEntry.addObject("instant", instant)
                .addObject("queue", queue)
                .addObject("delay", (delay != null ? delay.setPrefix("Delay") : null))
                .addObject("script", script)
                .addObject("context", context);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        Boolean instant = (Boolean) scriptEntry.getObject("instant");

        ScriptQueue queue;
        String id = (String) scriptEntry.getObject("queue");

        if (ScriptQueue._queueExists(id))
            queue = ScriptQueue._getExistingQueue(id);
        else if (instant)
            queue = InstantQueue.getQueue(id);
        else queue = TimedQueue.getQueue(id);

        Map<String, String> context = (HashMap<String, String>) scriptEntry.getObject("context");
        dScript script = (dScript) scriptEntry.getObject("script");
        Duration delay = (Duration) scriptEntry.getObject("delay");

        // Debug output
        dB.report(this.getName(),
                script.debug()
                        + (delay != null ? delay.debug() : "")
                        + aH.debugObj("Instant", instant.toString())
                        + aH.debugObj("Queue", id)
                        + (context != null ? aH.debugObj("Context", context.toString()) : "")
                        + (scriptEntry.getPlayer() != null
                        ? aH.debugObj("Player", scriptEntry.getPlayer().getName()) : "")
                        + (scriptEntry.getNPC() != null
                        ? aH.debugObj("NPC", scriptEntry.getNPC().toString()) : ""));

        if (instant) {
            // Instant, but no delay
            if (delay == null) {

                if (scriptEntry.getResidingQueue() != queue) {
                    // Instant, no delay, new queue
                    ((TaskScriptContainer) script.getContainer()).setSpeed(Duration.valueOf("0"))
                            .runTaskScript(queue.id,
                                    scriptEntry.getPlayer(),
                                    scriptEntry.getNPC(),
                                    context);
                }

                else {
                    // Instant, no delay, injection into current queue
                    ((TaskScriptContainer) script.getContainer()).setSpeed(Duration.valueOf("0"))
                            .injectTaskScript(queue.id,
                                    scriptEntry.getPlayer(),
                                    scriptEntry.getNPC(),
                                    context);
                }
            }

            else {
                // Instant, has delay, new queue
                ((TaskScriptContainer) script.getContainer()).setSpeed(Duration.valueOf("0"))
                        .runTaskScriptWithDelay(queue.id,
                                scriptEntry.getPlayer(),
                                scriptEntry.getNPC(),
                                context,
                                delay);
            }
        }
        else {

            if (delay == null) {

                // Not instant, no delay, new queue
                if (scriptEntry.getResidingQueue() != queue) {
                    ((TaskScriptContainer) script.getContainer())
                            .runTaskScript(queue.id,
                                    scriptEntry.getPlayer(),
                                    scriptEntry.getNPC(),
                                    context);
                }

                else {
                    // Not instant, no delay, injection into current queue
                    ((TaskScriptContainer) script.getContainer())
                            .injectTaskScript(queue.id,
                                    scriptEntry.getPlayer(),
                                    scriptEntry.getNPC(),
                                    context);
                }
            }

            else {
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
}
