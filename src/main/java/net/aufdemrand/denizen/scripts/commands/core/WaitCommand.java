package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.arguments.Duration;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;

import java.util.ArrayList;
import java.util.List;

/**
 * Instructs the NPC to follow a player.
 *
 * @author aufdemrand
 *
 */
public class WaitCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        if (scriptEntry.getSendingQueue() == null) throw new InvalidArgumentsException("WAIT can only be used with a queue!");

		/* The WAIT command ultimately sends itself back to the que with an appropriate delay.
		 * if the delay is more than the time initiated, we can assume it's the second time
		 * around, and therefore finish the command right now. */

        if (scriptEntry.getAllowedRunTime() > scriptEntry.getQueueTime()) {
            aH.debugObj("Held Queue", scriptEntry.getSendingQueue().toString()
                    + (scriptEntry.getPlayer() != null && (scriptEntry.getSendingQueue() == ScriptEngine.QueueType.PLAYER
                    || scriptEntry.getSendingQueue() == ScriptEngine.QueueType.PLAYER_TASK) ?
                    aH.debugObj("Player", scriptEntry.getPlayer().getName()) : "")
                    + (scriptEntry.getNPC() != null && scriptEntry.getSendingQueue() == ScriptEngine.QueueType.NPC ?
                    aH.debugObj("NPC", scriptEntry.getNPC().toString()) : ""));
            return;
        }

        // Initialize required fields
        ScriptEngine.QueueType queueToHold = scriptEntry.getSendingQueue();
        scriptEntry.setInstant(true);
        Duration delay = new Duration(5d);

        // Iterate through arguments
        for (String arg : scriptEntry.getArguments()) {

            if (aH.matchesInteger(arg) || aH.matchesDuration(arg))
                delay = Duration.valueOf(arg);

            if (aH.matchesQueueType(arg))
                queueToHold = aH.getQueueFrom(arg);

        }

        scriptEntry.addObject("queue", queueToHold);
        scriptEntry.addObject("delay", delay.setPrefix("Duration"));
    }


    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        if (scriptEntry.getAllowedRunTime() > scriptEntry.getQueueTime()) return;

        dB.echoDebug("Holding...");

        ScriptEngine.QueueType queue = (ScriptEngine.QueueType) scriptEntry.getObject("queue");
        Duration delay = (Duration) scriptEntry.getObject("delay");

        // Inject this scriptEntry back into the queue.

        List<ScriptEntry> list = new ArrayList<ScriptEntry>();

        try {
            scriptEntry.setAllowedRunTime(System.currentTimeMillis() + (long) (delay.getSeconds() * 1000));
            list.add(scriptEntry);

            if (queue == ScriptEngine.QueueType.PLAYER_TASK) {
                denizen.getScriptEngine().injectToQueue(scriptEntry.getPlayer(), list, ScriptEngine.QueueType.PLAYER_TASK, 0);
            }

            else if (queue == ScriptEngine.QueueType.PLAYER) {
                denizen.getScriptEngine().injectToQueue(scriptEntry.getPlayer(), list, ScriptEngine.QueueType.PLAYER, 0);
            }

            else if (queue == ScriptEngine.QueueType.NPC) {
                denizen.getScriptEngine().injectToQueue(scriptEntry.getNPC(), list, ScriptEngine.QueueType.NPC, 0);
            }
        } catch (Exception e) {

        }

    }

}