package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.queues.ScriptQueue;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.objects.Duration;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.scripts.queues.core.TimedQueue;
import net.aufdemrand.denizen.utilities.debugging.dB;


public class QueueCommand extends AbstractCommand {

    private enum Action { CLEAR, DELAY, PAUSE, RESUME }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("action")
                    && arg.matchesEnum(Action.values())) {
                scriptEntry.addObject("action", Action.valueOf(arg.getValue().toUpperCase()));
                if (scriptEntry.getObject("action") == Action.DELAY
                        && arg.matchesArgumentType(Duration.class))
                    scriptEntry.addObject("delay", arg.asType(Duration.class));
            }

            // No prefix required to specify the queue
            else if (ScriptQueue._getExistingQueue(arg.getValue()) != null) {
                scriptEntry.addObject("queue", ScriptQueue._getExistingQueue(arg.getValue()));
            }

            // ...but we also need to error out this command if the queue was not found.
            else throw new InvalidArgumentsException("The specified queue could not be found: " + arg.raw_value);

        }

        // If no queues have been added, assume 'residing queue'
        scriptEntry.defaultObject("queue", scriptEntry.getResidingQueue());

        // Check required args
        if (!scriptEntry.hasObject("action"))
            throw new InvalidArgumentsException("Must specify an action. Valid: CLEAR, DELAY, PAUSE, RESUME");

        if (scriptEntry.getObject("action") == Action.DELAY && !scriptEntry.hasObject("delay"))
            throw new InvalidArgumentsException("Must specify a delay.");

    }

    @SuppressWarnings("incomplete-switch")
    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        ScriptQueue queue = (ScriptQueue) scriptEntry.getObject("queue");
        Action action = (Action) scriptEntry.getObject("action");
        Duration delay = (Duration) scriptEntry.getObject("delay");

        // Debugger
        dB.report(getName(), aH.debugObj("Queue", queue.id)
                + aH.debugObj("Action", action.toString())
                + (action != null && action == Action.DELAY ? delay.debug() : ""));

        switch (action) {

            case CLEAR:
                queue.clear();
                return;

            case PAUSE:
                if (queue instanceof TimedQueue)
                    ((TimedQueue) queue).setPaused(true);
                return;

            case RESUME:
                if (queue instanceof TimedQueue)
                    ((TimedQueue) queue).setPaused(false);
                return;

            case DELAY:
                if (queue instanceof TimedQueue)
                    ((TimedQueue) queue).delayFor(delay);
                return;

        }

    }

}

