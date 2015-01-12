package net.aufdemrand.denizen.scripts.commands.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
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

    //
    // Static helpers
    //


    // Default 'DETERMINE_NONE' value.
    public static String DETERMINE_NONE = "none";

    // Map for keeping track of cache
    // Key: ID, Value: outcome
    private static Map<Long, List<String>> cache = new ConcurrentHashMap<Long, List<String>>(8, 0.9f, 1);

    // Start at 0
    public static long uniqueId = 0;


    /**
     * Increment the counter and return it, thus returning
     * a unique id. Determinations are very short lived.
     *
     * @return long ID
     */
    public static long getNewId() {
        // Just in case? Start over if already max_value.
        if (uniqueId == Long.MAX_VALUE)
            uniqueId = 0;
        // Increment the counter
        return uniqueId++;
    }


    /**
     * Checks the cache for existence of an outcome.
     *
     * @param id the outcome id to check
     * @return if the cache has the outcome
     */
    public static boolean hasOutcome(long id) {
        return cache.containsKey(id) && !cache.get(id).isEmpty();
    }


    /**
     * Gets the outcome, and removes it from the cache.
     *
     * @param id the outcome id to check
     * @return the outcome
     */
    public static List<String> getOutcome(long id) {
        List<String> outcome = cache.get(id);
        cache.remove(id);
        return outcome;
    }


    /**
     * Gets the current value of the outcome.
     * Note: The value of the outcome may change.
     *
     * @param id the outcome id to check
     * @return the current value of the outcome
     */
    public static String readOutcome(long id) {
        return cache.get(id).isEmpty() ? DETERMINE_NONE: cache.get(id).get(0);
    }


    //
    // Command Singleton
    //

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        //
        // Parse the arguments
        //

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (arg.matches("passive", "passively"))
                scriptEntry.addObject("passively", new Element(true));

            else if (!scriptEntry.hasObject("outcome"))
                scriptEntry.addObject("outcome", new Element(arg.raw_value));

            else arg.reportUnhandled();
        }

        //
        // Set defaults
        //

        scriptEntry.defaultObject("passively", new Element(false));
        scriptEntry.defaultObject("outcome", new Element(DETERMINE_NONE));
    }


    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        // Report!
        dB.report(scriptEntry, getName(), scriptEntry.getElement("outcome").debug()
                + scriptEntry.getElement("passively").debug());

        // Fetch the ScriptEntry elements
        String outcome = scriptEntry.getElement("outcome").asString();
        Boolean passively = scriptEntry.getElement("passively").asBoolean();

        Long uniqueId = (Long) scriptEntry.getObject("reqId");

        // Useful for debug
        if (uniqueId == null) {
            dB.echoError(scriptEntry.getResidingQueue(), "Cannot use determine in this queue!");
            return;
        }

        // Store the outcome in the cache
        List<String> strs;
        if (cache.containsKey(uniqueId)) {
            strs = cache.get(uniqueId);
        }
        else {
            strs = new ArrayList<String>();
            cache.put(uniqueId, strs);
        }
        strs.add(outcome);

        if (!passively)
            // Stop the queue by clearing the remainder of it.
            scriptEntry.getResidingQueue().clear();
    }
}
