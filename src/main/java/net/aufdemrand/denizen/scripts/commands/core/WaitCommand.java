package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.queues.ScriptQueue;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.objects.Duration;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.scripts.queues.core.Delayable;
import net.aufdemrand.denizen.utilities.debugging.dB;

/**
 *
 * @author aufdemrand
 *
 */
public class WaitCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Initialize required fields
        ScriptQueue queue = scriptEntry.getResidingQueue();
        Duration delay = new Duration(3);

        // Iterate through arguments
        for (String arg : scriptEntry.getArguments()) {

            // Set duration
            if (aH.matchesDuration(arg))
                delay = Duration.valueOf(arg);

            // Specify queue
            if (aH.matchesQueue(arg))
                queue = ScriptQueue._getExistingQueue(arg);
        }

        scriptEntry.addObject("queue", queue);
        scriptEntry.addObject("delay", delay);
    }


    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        ScriptQueue queue = (ScriptQueue) scriptEntry.getObject("queue");
        Duration delay = (Duration) scriptEntry.getObject("delay");

        dB.report(scriptEntry, getName(),
                aH.debugObj("queue", queue.id) + delay.debug());

        // Tell the queue to delay
        if (queue instanceof Delayable)
               ((Delayable) queue).delayFor(delay);
        else {
            scriptEntry.setInstant(false);
            dB.echoDebug(scriptEntry, "Forcing queue " + queue.id + " into a timed queue...");
            queue.forceToTimed(delay);
        }
    }
}
