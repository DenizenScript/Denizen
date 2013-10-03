package net.aufdemrand.denizen.scripts.commands.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.dList;
import net.aufdemrand.denizen.objects.dObject;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.BracedCommand;
import net.aufdemrand.denizen.scripts.queues.ScriptQueue;
import net.aufdemrand.denizen.scripts.queues.core.InstantQueue;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;

public class ForEachCommand extends BracedCommand {

    // - foreach li@p@Vegeta|p@MuhammedAli|n@123 {
    //   - inventory move origin:<%value%.inventory> destination:in@location[123,70,321]
    //   }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("list")
                    && arg.matchesArgumentType(dList.class))
                scriptEntry.addObject("list", arg.asType(dList.class));

        }

        if (!scriptEntry.hasObject("list"))
            throw new InvalidArgumentsException(Messages.ERROR_MISSING_OTHER, "LIST");

        scriptEntry.addObject("braces", getBracedCommands(scriptEntry, 1));

    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        // Get objects
        dList list = (dList) scriptEntry.getObject("list");
        ArrayList<ScriptEntry> bracedCommands = ((LinkedHashMap<String, ArrayList<ScriptEntry>>) scriptEntry.getObject("braces")).get("FOREACH");
        if (bracedCommands == null || bracedCommands.isEmpty()) {
            dB.echoError("Empty braces!");
            return;
        }


        // Report to dB
        dB.report(getName(), list.debug() );

        String queueId = UUID.randomUUID().toString();
        for (String value : list) {
            if (scriptEntry.getResidingQueue().getWasCleared())
                return;
            ArrayList<ScriptEntry> newEntries = new ArrayList<ScriptEntry>();
            for (ScriptEntry entry : bracedCommands) {
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
            for (Map.Entry<String, dObject> entry : scriptEntry.getResidingQueue().getAllContext().entrySet()) {
                queue.addContext(entry.getKey(), entry.getValue());
            }
            queue.addDefinition("parent_queue", scriptEntry.getResidingQueue().id);
            scriptEntry.getResidingQueue().addDefinition("value", value);
            queue.addDefinition("value", value);
            queue.getAllDefinitions().putAll(scriptEntry.getResidingQueue().getAllDefinitions());
            queue.addEntries(newEntries);
            queue.start();
        }

    }

}
