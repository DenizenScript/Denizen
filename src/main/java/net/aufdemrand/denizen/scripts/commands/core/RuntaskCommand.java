package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEngine;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import net.aufdemrand.denizen.utilities.runnables.Runnable2;
import org.bukkit.Bukkit;

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
public class RuntaskCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Initialize necessary fields
        String id = null;
        String script = null;
        double delay = -1.0d;
        ScriptEngine.QueueType queue;
        boolean instant = false;

        // Set some defaults
        if (scriptEntry.getPlayer() != null)
            queue = ScriptEngine.QueueType.PLAYER_TASK;
        else
            queue = ScriptEngine.QueueType.NPC;

        // Iterate through Arguments to extract needed information
        for (String arg : scriptEntry.getArguments()) {
            if (aH.matchesScript(arg)) {
                script = aH.getStringFrom(arg);
                dB.echoDebug(Messages.DEBUG_SET_SCRIPT, script);

            } else if (aH.matchesValueArg("DELAY", arg, aH.ArgumentType.Duration)) {
                delay = aH.getSecondsFrom(arg);
                dB.echoDebug("...set DELAY: '%s'", String.valueOf(delay));

            } else if (aH.matchesArg("INSTANT", arg)) {
                instant = true;
                dB.echoDebug("...script will not be queued.");

            } else if (aH.matchesQueueType(arg)) {
                queue = aH.getQueueFrom(arg);
                dB.echoDebug("...set QUEUE: '%s'", queue.toString());

            } else if (aH.matchesValueArg("ID", arg, aH.ArgumentType.Word)) {
                id = aH.getStringFrom(arg);
                dB.echoDebug("...set ID: '%s'", id);

            } else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
        }

        // Check for necessary information post-argument validation
        if ((queue == ScriptEngine.QueueType.PLAYER || queue == ScriptEngine.QueueType.PLAYER_TASK)
                && scriptEntry.getPlayer() == null)
            throw new InvalidArgumentsException("Player cannot be null when using a 'Player' Queue.");
        if (queue == ScriptEngine.QueueType.NPC && scriptEntry.getNPC() == null)
            throw new InvalidArgumentsException("NPC cannot be null when using a 'NPC' Queue.");
        if (script == null) throw new InvalidArgumentsException("Must define a script to be run!");

        // Set an ID if not specified (we'll use the name of the script)
        if (id == null) id = script;

        // Put important objects inside the scriptEntry to be sent to execute()
        scriptEntry.addObject("id", id);
        scriptEntry.addObject("queue", queue);
        scriptEntry.addObject("delay", delay);
        scriptEntry.addObject("script", script);
        scriptEntry.addObject("instant", instant);
    }

    // For keeping track of delays
    private static Map<String, Integer> delays = new ConcurrentHashMap<String, Integer>();

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        String script = (String) scriptEntry.getObject("script");

        // Run right now if no delay
        if ((Double) scriptEntry.getObject("delay") <= 0) {

            if ((Boolean) scriptEntry.getObject("instant"))
                denizen.getScriptEngine().getScriptBuilder()
                        .runTaskScriptInstantly(scriptEntry.getPlayer(), scriptEntry.getNPC(), script);

            else switch ((ScriptEngine.QueueType) scriptEntry.getObject("queue")) {

                case PLAYER:
                case PLAYER_TASK:
                    denizen.getScriptEngine().getScriptBuilder()
                            .runTaskScript(scriptEntry.getPlayer(), scriptEntry.getNPC(), script);
                    break;

                case NPC:
                    denizen.getScriptEngine().getScriptBuilder()
                            .runTaskScript(scriptEntry.getNPC(), scriptEntry.getPlayer(), script);
                    break;
            }

            // Remove from delays if this was initially delayed
            if (delays.containsKey(((String) scriptEntry.getObject("id")).toUpperCase()))
                delays.remove(((String) scriptEntry.getObject("id")).toUpperCase());

        } else { // Delay this command
            String id = ((String) scriptEntry.getObject("id")).toUpperCase();
            long delay = (long) ((Double) scriptEntry.getObject("delay") * 20);

            // Reset delay in scriptEntry so next time it's executed it's not delayed again.
            scriptEntry.addObject("delay", 0d);

            // Set delayed task and put id in a map (for cancellations with CANCELTASK [id])
            dB.echoDebug(Messages.DEBUG_SETTING_DELAYED_TASK, "Run TASK SCRIPT '" + script + "'");
            delays.put(id, denizen.getServer().getScheduler().scheduleSyncDelayedTask(denizen,
                            new Runnable2<String, ScriptEntry>(script, scriptEntry) {
                                @Override
                                public void run(String script, ScriptEntry scriptEntry) {
                                        dB.log(Messages.DEBUG_RUNNING_DELAYED_TASK, "Run TASK SCRIPT '" + script + "'");
                                    try {
                                        execute(scriptEntry);
                                    } catch (CommandExecutionException e) {
                                        dB.echoError("Could not run delayed task!");
                                        if (dB.showStackTraces) e.printStackTrace();
                                    }
                                }
                            }, delay));
        }
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