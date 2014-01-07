package net.aufdemrand.denizen.scripts.commands.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.dObject;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.BracedCommand;
import net.aufdemrand.denizen.scripts.queues.ScriptQueue;
import net.aufdemrand.denizen.scripts.queues.core.InstantQueue;
import net.aufdemrand.denizen.utilities.debugging.dB;

public class RepeatCommand extends BracedCommand {


    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("qty")
                    && arg.matchesPrimitive(aH.PrimitiveType.Integer))
                scriptEntry.addObject("qty", arg.asElement());

            // Don't report unhandled argument since getBracedCommands will handle
            // the remainder of the commands.

        }

        if (!scriptEntry.hasObject("qty"))
            throw new InvalidArgumentsException("Must specify a quantity!");

        scriptEntry.addObject("braces", getBracedCommands(scriptEntry, 1));

    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        // Get objects
        Element qty = scriptEntry.getElement("qty");
        ArrayList<ScriptEntry> bracedCommandsList = ((LinkedHashMap<String, ArrayList<ScriptEntry>>) scriptEntry.getObject("braces")).get("REPEAT");
        if (bracedCommandsList == null || bracedCommandsList.isEmpty()) {
            dB.echoError("Empty braces!");
            return;
        }
        ScriptEntry[] bracedCommands = bracedCommandsList.toArray(new ScriptEntry[bracedCommandsList.size()]);

        // Report to dB
        dB.report(scriptEntry, getName(), qty.debug());

        String queueId = UUID.randomUUID().toString();
        for (int incr = 0; incr < qty.asInt(); incr++) {
            if (scriptEntry.getResidingQueue().getWasCleared())
                return;
            ArrayList<ScriptEntry> newEntries = new ArrayList<ScriptEntry>();
            for (ScriptEntry entry: bracedCommands) {
                try {
                    ScriptEntry toAdd = entry.clone();
                    toAdd.getObjects().clear();
                    newEntries.add(toAdd);
                }
                catch (Throwable e) {
                    dB.echoError(e);
                }
            }
            ScriptQueue queue = new InstantQueue(queueId);
            for (Map.Entry<String, dObject> entry : scriptEntry.getResidingQueue().getAllContext().entrySet()) {
                queue.addContext(entry.getKey(), entry.getValue());
            }
            queue.addDefinition("parent_queue", scriptEntry.getResidingQueue().id);
            scriptEntry.getResidingQueue().addDefinition("value", String.valueOf(incr + 1));
            queue.addDefinition("value", String.valueOf(incr + 1));
            queue.getAllDefinitions().putAll(scriptEntry.getResidingQueue().getAllDefinitions());
            queue.addEntries(newEntries);
            queue.start();
        }

    }
}
