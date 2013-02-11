package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.ScriptQueue;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.arguments.Duration;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;

/**
 * Instructs the NPC to follow a player.
 *
 * @author aufdemrand
 *
 */
public class WaitCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Initialize required fields
        ScriptQueue queue = ScriptQueue._getQueue(scriptEntry.getResidingQueue());
        Duration delay = new Duration(3);

        // Iterate through arguments
        for (String arg : scriptEntry.getArguments()) {

            // Set duration
            if (aH.matchesInteger(arg) || aH.matchesDuration(arg))
                delay = Duration.valueOf(arg);

            // Specify queue
            if (aH.matchesQueue(arg))
                queue = aH.getQueueFrom(arg);
        }

        scriptEntry.addObject("queue", queue);
        scriptEntry.addObject("delay", delay.setPrefix("Duration"));
    }


    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        ScriptQueue queue = (ScriptQueue) scriptEntry.getObject("queue");
        Duration delay = (Duration) scriptEntry.getObject("delay");

        // TODO: dBugger output

        // Tell the queue to delay
        dB.echoDebug("Delaying " + (long) (delay.getSeconds() * 1000) + "ms");
        queue.delayUntil(System.currentTimeMillis() + (long) (delay.getSeconds() * 1000));
    }

}