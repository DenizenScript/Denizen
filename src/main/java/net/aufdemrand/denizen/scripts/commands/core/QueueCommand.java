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


public class QueueCommand extends AbstractCommand {

    private enum Action { CLEAR, SET, DELAY, PAUSE, RESUME }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        ScriptQueue queue = scriptEntry.getResidingQueue();
        Action action = null;
        Duration delay = null;

        // Iterate through arguments
        for (String arg : scriptEntry.getArguments()){
            if (aH.matchesQueue(arg))
                queue = aH.getQueueFrom(arg);

            else if (aH.matchesArg("CLEAR, SET, PAUSE, RESUME", arg))
                action = Action.valueOf(aH.getStringFrom(arg).toUpperCase());

            else if (aH.matchesValueArg("DELAY", arg, aH.ArgumentType.Duration)) {
                action = Action.DELAY;
                delay = aH.getDurationFrom(arg);
            }

            else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
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

        ScriptQueue queue = (ScriptQueue) scriptEntry.getObject("queue");
        Action action = (Action) scriptEntry.getObject("action");
        Duration delay = (Duration) scriptEntry.getObject("duration");

        // Debugger
        dB.report(getName(), queue.toString()
                + aH.debugObj("Action", action.toString())
                + (action != null && action == Action.DELAY ? delay.debug() : ""));

        switch (action) {

            case CLEAR:
                queue.clear();
                return;

            case PAUSE:
                queue.setPaused(true);
                return;

            case RESUME:
                queue.setPaused(false);
                return;

            case DELAY:
                queue.delayFor(delay.getTicks());
                return;

        }

    }

}

