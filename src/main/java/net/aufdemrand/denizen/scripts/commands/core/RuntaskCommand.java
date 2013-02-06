package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.Settings;
import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.ScriptQueue;
import net.aufdemrand.denizen.scripts.ScriptRegistry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.arguments.Duration;
import net.aufdemrand.denizen.utilities.arguments.Script;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import net.aufdemrand.denizen.utilities.runnables.Runnable2;
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
        String id = null;
        Script script = null;
        Map<String, String> context = null;
        Duration delay = new Duration(0);
        int speed = Settings.InteractDelayInTicks();
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

            } else if (aH.matchesArg("QUEUE", arg)) {
                // Deprecated, no longer needed. All tasks are now queued, even if they are 'instant'.

            } else if (aH.matchesArg("INSTANT, INSTANTLY", arg)) {
                // Deprecated, no longer needed. Instead, use SPEED, or specify speed in the task script.

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

        // Check for necessary information post-argument validation
        if ((queue == ScriptEngine.QueueType.PLAYER || queue == ScriptEngine.QueueType.PLAYER_TASK)
                && scriptEntry.getPlayer() == null)
            throw new InvalidArgumentsException("Player cannot be null when using a 'Player' Queue.");
        if (queue == ScriptEngine.QueueType.NPC && scriptEntry.getNPC() == null)
            throw new InvalidArgumentsException("NPC cannot be null when using a 'NPC' Queue.");
        if (script == null) throw new InvalidArgumentsException("Must define a script to be run!");

        // Set an ID if not specified (we'll use the name of the script)
        if (id == null) id = script.getName();

        // Put important objects inside the scriptEntry to be sent to execute()
        scriptEntry.addObject("id", id);
        scriptEntry.addObject("queue", queue);
        scriptEntry.addObject("delay", delay);
        scriptEntry.addObject("script", script);
        scriptEntry.addObject("instant", instant);
        scriptEntry.addObject("context", context);
    }

    // For keeping track of delays
    private static Map<String, Integer> delays = new ConcurrentHashMap<String, Integer>();

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        Map<String, String> context = (HashMap<String, String>) scriptEntry.getObject("context");
        Script script = (Script) scriptEntry.getObject("script");
        ScriptEngine.QueueType queue = (ScriptEngine.QueueType) scriptEntry.getObject("queue");
        Boolean instant = (Boolean) scriptEntry.getObject("instant");
        Duration delay = (Duration) scriptEntry.getObject("delay");

        // Debug output
        dB.echoApproval("Executing '" + getName() + "': "
                + script.debug()
                + delay.debug()
                + "Player='" + (scriptEntry.getPlayer() != null ? scriptEntry.getPlayer().getName() + "', " : "NULL', ")
                + "NPC='" + (scriptEntry.getNPC() != null ? scriptEntry.getNPC() + "', " : "NULL', ")
                + "Queue='" + (instant == false ? queue.toString() + "'" : "INSTANT'"));


        // Run right now if no delay
        if (delay.getSeconds() <= 0) {

            if (instant)
                denizen.getScriptEngine().getScriptBuilder()
                        .runTaskScriptInstantly(scriptEntry.getPlayer(), scriptEntry.getNPC(), script.getName(), context);

            else switch (queue) {

                case PLAYER:
                case PLAYER_TASK:
                    denizen.getScriptEngine().getScriptBuilder()
                            .runTaskScript(scriptEntry.getPlayer(), scriptEntry.getNPC(), script.getName());
                    break;

                case NPC:
                    denizen.getScriptEngine().getScriptBuilder()
                            .runTaskScript(scriptEntry.getNPC(), scriptEntry.getPlayer(), script.getName());
                    break;
            }

            // Remove from delays if this was initially delayed
            if (delays.containsKey(((String) scriptEntry.getObject("id")).toUpperCase()))
                delays.remove(((String) scriptEntry.getObject("id")).toUpperCase());

        } else { // Delay this command
            String id = ((String) scriptEntry.getObject("id")).toUpperCase();
            long ldelay = (long) (((Duration) scriptEntry.getObject("delay")).getSeconds() * 20);

            // Reset delay in scriptEntry so next time it's executed it's not delayed again.
            scriptEntry.addObject("delay", new Duration(0));

            // Set delayed task and put id in a map (for cancellations with CANCELTASK [id])
            dB.echoDebug(Messages.DEBUG_SETTING_DELAYED_TASK, "Run TASK SCRIPT '" + script + "'");
            delays.put(id, denizen.getServer().getScheduler().scheduleSyncDelayedTask(denizen,
                            new Runnable2<String, ScriptEntry>(script.getName(), scriptEntry) {
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
                            }, ldelay));
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