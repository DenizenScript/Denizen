package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.ScriptQueue;
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

    public static Map<Long, String> outcomes = new ConcurrentHashMap<Long, String>();

    public static long uniqueId = 0;

    public static long getNewId() {
        uniqueId++;
        return uniqueId;
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        String outcome = "false";
        Boolean passively = false;

        for (String arg : scriptEntry.getArguments())

        if (aH.matchesArg("PASSIVELY", arg))
            passively = true;
        else
            outcome = aH.getStringFrom(arg);
        scriptEntry.addObject("outcome", outcome)
            .addObject("passively", passively);
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        String outcome = (String) scriptEntry.getObject("outcome");
        Boolean passively = (Boolean) scriptEntry.getObject("passively");

        Long uniqueId = (Long) scriptEntry.getObject("reqId");
        if (uniqueId == null) return;

        outcomes.put(uniqueId, outcome);

        if (!passively)
            // Stop the queue by clearing the remainder of it.
            scriptEntry.getResidingQueue().clear();
    }

}
