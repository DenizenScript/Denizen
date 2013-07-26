package net.aufdemrand.denizen.scripts.commands.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;

/**
 *
 * @author aufdemrand
 *
 */
public class DetermineCommand extends AbstractCommand {

    private static Map<Long, String> outcomes = new ConcurrentHashMap<Long, String>(8, 0.9f, 1);

    public static long uniqueId = 0;

    public static long getNewId() {
        uniqueId++;
        return uniqueId;
    }

    public static boolean hasOutcome(long id) {
        if (outcomes.containsKey(id)) return true;
        return false;
    }

    public static String getOutcome(long id) {
        String outcome = outcomes.get(id);
        outcomes.remove(id);
        return outcome;
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        String outcome = "false";
        Boolean passively = false;

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments()))

            if (arg.matches("passive, passively"))
                scriptEntry.addObject("passively", new Element(true));
            else
                scriptEntry.addObject("outcome", arg.asElement());

        // Set defaults
        scriptEntry.defaultObject("passively", new Element(false));
        scriptEntry.defaultObject("outcome", new Element(false));
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        // Report!
        dB.report(getName(), scriptEntry.getElement("outcome").debug()
                + scriptEntry.getElement("passively").debug());

        // Fetch
        String outcome = scriptEntry.getElement("outcome").asString();
        Boolean passively = scriptEntry.getElement("passively").asBoolean();

        Long uniqueId = (Long) scriptEntry.getObject("reqId");
        if (uniqueId == null) return;

        outcomes.put(uniqueId, outcome);

        if (!passively)
            // Stop the queue by clearing the remainder of it.
            scriptEntry.getResidingQueue().clear();
    }

}
