package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.ScriptQueue;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;

import java.util.ArrayList;
import java.util.List;

/**
 * Clears queue(s).
 *
 * @author aufdemrand
 */

public class ClearCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        List<ScriptQueue> queues = new ArrayList<ScriptQueue>();

        // Use current queue if none specified.
        queues.add(ScriptQueue._getQueue(scriptEntry.getResidingQueue()));

        for (String arg : scriptEntry.getArguments()) {
            if (aH.matchesQueue(arg))
                for (String queueName : aH.getListFrom(arg)) {
                    queues.clear();
                    try {
                        queues.add(aH.getQueueFrom(queueName));
                    } catch (Exception e) {
                    // must be null, don't add
                    }
                }

            else throw new InvalidArgumentsException(dB.Messages.ERROR_UNKNOWN_ARGUMENT, arg);
        }

        if (queues.isEmpty()) throw new InvalidArgumentsException("Must specify at least one ScriptQueue!");

        scriptEntry.addObject("queues", queues);
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        // Fetch queues from the scriptEntry
        List<ScriptQueue> queues = (List<ScriptQueue>) scriptEntry.getObject("queues");

        // Report to dBugger
        dB.report(getName(),
                aH.debugObj("Queues", queues.toString()));

        // clear() each queue
        for (ScriptQueue queue : queues)
            queue.clear();
    }

}
