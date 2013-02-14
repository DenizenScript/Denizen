package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.arguments.aH;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author aufdemrand
 *
 */
public class DetermineCommand extends AbstractCommand {

    public static Map<Long, Boolean> outcomes = new ConcurrentHashMap<Long, Boolean>();

    public static long uniqueId = 0;

    public static long getNewId() {
        uniqueId++;
        return uniqueId;
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        Boolean outcome = false;
        for (String arg : scriptEntry.getArguments())
            outcome = aH.getBooleanFrom(arg);
        scriptEntry.addObject("outcome", outcome);
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        Boolean outcome = (Boolean) scriptEntry.getObject("outcome");
        Long uniqueId = (Long) scriptEntry.getObject("reqId");
        if (uniqueId == null) return;
        outcomes.put(uniqueId, outcome);


        scriptEntry.getResidingQueue();
    }

}
