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
        return outcomes.containsKey(id);
    }

    public static String getOutcome(long id) {
        String outcome = outcomes.get(id);
        outcomes.remove(id);
        return outcome;
    }

    public static String readOutcome(long id) {
        return outcomes.get(id);
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (arg.matches("passive, passively"))
                scriptEntry.addObject("passively", new Element(true));

            else if (!scriptEntry.hasObject("outcome"))
                scriptEntry.addObject("outcome", new Element(arg.raw_value));

            else arg.reportUnhandled();
        }

        // Set defaults
        scriptEntry.defaultObject("passively", new Element(false));
        scriptEntry.defaultObject("outcome", new Element(false));
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        // Report!
        dB.report(scriptEntry, getName(), scriptEntry.getElement("outcome").debug()
                + scriptEntry.getElement("passively").debug());

        // Fetch
        String outcome = scriptEntry.getElement("outcome").asString();
        Boolean passively = scriptEntry.getElement("passively").asBoolean();

        Long uniqueId = (Long) scriptEntry.getObject("reqId");
        if (uniqueId == null) {
            dB.echoError("Cannot use determine in this queue!");
            return;
        }

        if (outcomes.containsKey(uniqueId)) {
            dB.echoError("This queue already has a determination!");
            return;
        }

        outcomes.put(uniqueId, outcome);

        if (!passively)
            // Stop the queue by clearing the remainder of it.
            scriptEntry.getResidingQueue().clear();
    }

}
