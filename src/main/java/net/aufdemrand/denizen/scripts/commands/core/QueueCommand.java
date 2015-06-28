package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Duration;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import net.aufdemrand.denizencore.scripts.queues.ScriptQueue;
import net.aufdemrand.denizencore.scripts.queues.core.Delayable;


public class QueueCommand extends AbstractCommand {

    private enum Action {CLEAR, DELAY, PAUSE, RESUME, STOP}

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
            else if ((arg.matchesArgumentType(ScriptQueue.class)
                    || arg.matchesPrefix("queue"))
                    && !scriptEntry.hasObject("queue")) {
                scriptEntry.addObject("queue", arg.asType(ScriptQueue.class));
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

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        ScriptQueue queue = (ScriptQueue) scriptEntry.getObject("queue");
        Action action = (Action) scriptEntry.getObject("action");
        Duration delay = (Duration) scriptEntry.getObject("delay");

        // Debugger
        dB.report(scriptEntry, getName(), queue.debug()
                + aH.debugObj("Action", action.toString())
                + (action == Action.DELAY ? delay.debug() : ""));

        switch (action) {

            case CLEAR:
                queue.clear();
                return;

            case STOP:
                queue.clear();
                queue.stop();
                return;

            case PAUSE:
                if (queue instanceof Delayable) {
                    ((Delayable) queue).setPaused(true);
                }
                else {
                    queue.forceToTimed(new Duration(1L)).setPaused(true);
                }
                return;

            case RESUME:
                if (queue instanceof Delayable)
                    ((Delayable) queue).setPaused(false);
                return;

            case DELAY:
                if (queue instanceof Delayable)
                    ((Delayable) queue).delayFor(delay);
                else
                    queue.forceToTimed(delay);
                return;

        }

    }
}

