package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.ScriptQueue;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.arguments.Duration;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;

import java.util.ArrayList;
import java.util.List;


public class QueueCommand extends AbstractCommand {

    private enum Action { CLEAR, SET, DELAY, PAUSE, RESUME }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        ScriptQueue queue = scriptEntry.getResidingQueue();
        Action action = null;
        Duration delay = null;

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

            else if (aH.matchesArg("CLEAR, SET, PAUSE, RESUME", arg))
                action = Action.valueOf(aH.getStringFrom(arg).toUpperCase());

            else if (aH.matchesValueArg("DELAY", arg, aH.ArgumentType.Duration)) {
                action = Action.DELAY;
                delay = aH.getDurationFrom(arg);
            }

            // queue: argument should be optional in this command
            else {
                queues.clear();
                for (String queueName : aH.getListFrom(arg)) {
                    try {
                        queues.add(aH.getQueueFrom(queueName));
                    } catch (Exception e) {
                        // must be null, don't add
                    }
                }

            }
        }

        // Check required args
        if (action == null)
            throw new InvalidArgumentsException("Must specify an action. Valid: CLEAR, SET, DELAY, PAUSE, RESUME");

        if (action == Action.DELAY && delay == null)
            throw new InvalidArgumentsException("Must specify a delay.");

        // Stash args in ScriptEntry for use in execute()
        scriptEntry.addObject("queue", queue)
                .addObject("action", action)
                .addObject("delay", delay);
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        List<ScriptQueue> queues = (List<ScriptQueue>) scriptEntry.getObject("queues");
        Action action = (Action) scriptEntry.getObject("action");
        Duration delay = (Duration) scriptEntry.getObject("duration");

        // Debugger
        dB.report(getName(), aH.debugObj("Queues", queues.toString())
                + aH.debugObj("Action", action.toString())
                + (action != null && action == Action.DELAY ? delay.debug() : ""));

        switch (action) {

            case CLEAR:
                for (ScriptQueue queue : queues)
                    queue.clear();
                return;

            case PAUSE:
                for (ScriptQueue queue : queues)
                    queue.setPaused(true);
                return;

            case RESUME:
                for (ScriptQueue queue : queues)
                    queue.setPaused(false);
                return;

            case DELAY:
                for (ScriptQueue queue : queues)
                    queue.delayFor(delay.getTicks());
                return;

        }

    }

}

