package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.BracedCommand;
import net.aufdemrand.denizen.scripts.queues.ScriptQueue;
import net.aufdemrand.denizen.scripts.queues.core.InstantQueue;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.UUID;

public class RepeatCommand extends BracedCommand {


    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("qty")
                    && arg.matchesPrimitive(aH.PrimitiveType.Integer))
                scriptEntry.addObject("qty", arg.asElement());

        }

        if (!scriptEntry.hasObject("qty"))
            throw new InvalidArgumentsException(Messages.ERROR_MISSING_OTHER, "QUANTITY");

        scriptEntry.addObject("braces", getBracedCommands(scriptEntry, 1));

    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        // Get objects
        Element qty = scriptEntry.getElement("qty");
        ArrayList<ScriptEntry> bracedCommands = ((LinkedHashMap<String, ArrayList<ScriptEntry>>) scriptEntry.getObject("braces")).get("REPEAT");
        if (bracedCommands == null || bracedCommands.isEmpty()) {
            dB.echoError("Empty braces!");
            return;
        }

        // Report to dB
        dB.report(getName(), qty.debug());

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
                    e.printStackTrace();
                }
            }
            ScriptQueue queue = new InstantQueue(queueId);
            queue.addDefinition("parent_queue", scriptEntry.getResidingQueue().id);
            scriptEntry.getResidingQueue().addDefinition("value", String.valueOf(incr + 1));
            queue.addDefinition("value", String.valueOf(incr + 1));
            queue.getAllDefinitions().putAll(scriptEntry.getResidingQueue().getAllDefinitions());
            queue.addEntries(newEntries);
            queue.start();
        }

    }
}
