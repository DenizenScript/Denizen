package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.ScriptQueue;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;

import java.util.ArrayList;
import java.util.List;

/**
 * Clears queue(s).
 *
 * @author aufdemrand
 */

@Deprecated
public class ClearCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // TODO: Remove this command in 1.0
        dB.log("The CLEAR command has been deprecated. While it will still function normally "
                + "for the time being, it is likely that it will be removed in the 1.0 release. It has "
                + "instead been replaced by a wider scope command named QUEUE. To replicate "
                + "the functionality of this command with QUEUE, use: queue clear queue_name(|addl_queue)");

        List<ScriptQueue> queues = new ArrayList<ScriptQueue>();

        // Use current queue if none specified.
        queues.add(scriptEntry.getResidingQueue());

        for (String arg : scriptEntry.getArguments()) {
            if (aH.matchesQueue(arg)) {
                queues.clear();
                for (String queueName : aH.getListFrom(arg)) {
                    try {
                        queues.add(aH.getQueueFrom(queueName));
                    } catch (Exception e) {
                        // must be null, don't add
                    }
                }
            }

            else throw new InvalidArgumentsException(dB.Messages.ERROR_UNKNOWN_ARGUMENT, arg);
        }

        if (queues.isEmpty()) throw new InvalidArgumentsException("Must specify at least one ScriptQueue!");

        scriptEntry.addObject("queues", queues);
    }

    @SuppressWarnings("unchecked")
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
