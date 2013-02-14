package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.Settings;
import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.ScriptQueue;
import net.aufdemrand.denizen.scripts.ScriptRegistry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.scripts.containers.core.TaskScriptContainer;
import net.aufdemrand.denizen.utilities.arguments.Duration;
import net.aufdemrand.denizen.utilities.arguments.Script;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
public class RuntaskCommand extends AbstractCommand implements Listener {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Initialize necessary fields
        Script script = null;
        Map<String, String> context = null;
        Duration delay = new Duration(0);
        Duration speed = new Duration((long) Settings.InteractDelayInTicks());
        ScriptQueue queue = ScriptQueue._getQueue(ScriptQueue._getNextId());

        // Iterate through Arguments to extract needed information
        for (String arg : scriptEntry.getArguments()) {

            // Specify scriptContainer to use
            if (aH.matchesScript(arg)) {
                script = aH.getScriptFrom(arg);

                // Delay the start of the queue
            } else if (aH.matchesValueArg("DELAY", arg, aH.ArgumentType.Duration)) {
                delay = aH.getDurationFrom(arg);
                delay.setPrefix("Delay");

                // Use a specific queue
            } else if (aH.matchesQueue(arg)) {
                queue = aH.getQueueFrom(arg);

            } else if (aH.matchesValueArg("SPEED", arg, aH.ArgumentType.Duration)) {
                speed = aH.getDurationFrom(arg);

            } else if (aH.matchesArg("QUEUE", arg)) {
                // Deprecated, no longer needed. All tasks are now queued, even if they are 'instant'.

            } else if (aH.matchesArg("INSTANT, INSTANTLY", arg)) {
                speed = new Duration(0);

                // Specify context
            } else if (aH.matchesValueArg("CONTEXT", arg, aH.ArgumentType.Custom)) {
                context = new HashMap<String, String>();
                List<String> contexts = aH.getListFrom(arg);
                for (String ctxt : contexts) {
                    String[] sctxt = ctxt.split(",", 2);
                    if (sctxt.length > 1)
                        context.put(sctxt[0].trim().toUpperCase(), sctxt[1].trim());
                    else context.put(sctxt[0].trim().toUpperCase(), "true");
                }

            } else if (aH.matchesValueArg("ID", arg, aH.ArgumentType.Word)) {
                // Deprecated, no longer needed. You can name the queue instead.

                // Specify SCRIPT name without SCRIPT: prefix
            } else if (ScriptRegistry.containsScript(aH.getStringFrom(arg))) {
                script = aH.getScriptFrom(arg);
                if (!script.getType().equalsIgnoreCase("TASK"))
                    script = null;

            } else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
        }

        if (script == null) throw new InvalidArgumentsException("Must define a script to be run!");

        // Put important objects inside the scriptEntry to be sent to execute()
        scriptEntry.addObject("speed", speed);
        scriptEntry.addObject("queue", queue);
        scriptEntry.addObject("delay", delay.setPrefix("Delay"));
        scriptEntry.addObject("script", script);
        scriptEntry.addObject("context", context);
    }

    // For keeping track of delays
    private static Map<String, Integer> delays = new ConcurrentHashMap<String, Integer>();

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        Map<String, String> context = (HashMap<String, String>) scriptEntry.getObject("context");
        Script script = (Script) scriptEntry.getObject("script");
        ScriptQueue queue = (ScriptQueue) scriptEntry.getObject("queue");
        Duration speed = (Duration) scriptEntry.getObject("speed");
        Duration delay = (Duration) scriptEntry.getObject("delay");

        // Debug output
        dB.echoApproval("Executing '" + getName() + "': "
                + script.debug()
                + delay.debug()
                + "Queue='" + queue.toString());

        if (delay.getSeconds() <= 0)
            ((TaskScriptContainer) script.getContainer()).setSpeed(speed)
                    .runTaskScript(queue.id, scriptEntry.getPlayer(), scriptEntry.getNPC(), context);

        else
            ((TaskScriptContainer) script.getContainer()).setSpeed(speed)
                    .runTaskScriptWithDelay(queue.id, scriptEntry.getPlayer(), scriptEntry.getNPC(), context, delay);

    }

    /**
     * Cancels a delayed task script by 'id'. If no 'id' was specified when delaying, it's likely
     * the name of the script. 'id' is Case in-sensitive.
     *
     * @param id the 'id' of the task script to cancel
     *
     * @return true if the id was found and cancelled
     *
     */
    public static boolean cancelTask(String id) {
        if (delays.containsKey(id.toUpperCase())) {
            try {
                Bukkit.getServer().getScheduler().cancelTask(delays.get(id.toUpperCase()));
            } catch (Exception e) {
                dB.echoError("Could not cancel task!");
                if (dB.showStackTraces) e.printStackTrace();
            }
            // No need to track this any longer
            delays.remove(id.toUpperCase());

            return true;
        }

        // Hrmmm... no 'id' with that name!
        return false;
    }

}