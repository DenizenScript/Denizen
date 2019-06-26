package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Duration;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import net.aufdemrand.denizencore.scripts.queues.ScriptQueue;
import net.aufdemrand.denizencore.scripts.queues.core.Delayable;

public class WaitCommand extends AbstractCommand {

    // <--[command]
    // @Name Wait
    // @Syntax wait (<duration>) (queue:<name>)
    // @Required 0
    // @Short Delays a script for a specified amount of time.
    // @Group core
    //
    // @Description
    // Pauses the script queue for the duration specified. If no duration is specified it defaults to 3 seconds.
    // Accepts the 'queue:<name>' argument which allows the delay of a different queue.
    //
    // @Tags
    // <q@queue.speed>
    //
    // @Usage
    // Use to delay the current queue for 1 minute.
    // - wait 1m
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Initialize required fields
        ScriptQueue queue = scriptEntry.getResidingQueue();
        Duration delay = new Duration(3);

        // Iterate through arguments
        for (String arg : scriptEntry.getArguments()) {

            // Set duration
            if (aH.matchesDuration(arg)) {
                delay = Duration.valueOf(arg);
            }

            // Specify queue
            if (aH.matchesQueue(arg)) {
                queue = ScriptQueue._getExistingQueue(arg);
            }
        }

        scriptEntry.addObject("queue", queue);
        scriptEntry.addObject("delay", delay);
    }


    @Override
    public void execute(ScriptEntry scriptEntry) {

        ScriptQueue queue = (ScriptQueue) scriptEntry.getObject("queue");
        Duration delay = (Duration) scriptEntry.getObject("delay");

        if (scriptEntry.dbCallShouldDebug()) {

            dB.report(scriptEntry, getName(),
                    aH.debugObj("queue", queue.id) + delay.debug());

        }

        // Tell the queue to delay
        if (queue instanceof Delayable) {
            ((Delayable) queue).delayFor(delay);
        }
        else {
            scriptEntry.setInstant(false);
            dB.echoDebug(scriptEntry, "Forcing queue " + queue.id + " into a timed queue...");
            queue.forceToTimed(delay);
        }
    }
}
