package net.aufdemrand.denizen.scripts.commands.core;

import java.util.ArrayList;
import java.util.UUID;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.dList;
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

        scriptEntry.addObject("entries", getBracedCommands(scriptEntry, 1));

    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        // Get objects
        dList list = (dList) scriptEntry.getObject("list");
        ArrayList<ScriptEntry> entries = (ArrayList<ScriptEntry>) scriptEntry.getObject("entries");

        // Report to dB
        dB.report(getName(), list.debug() );

        String queueId = UUID.randomUUID().toString();
        for (String value : list) {
            if (scriptEntry.getResidingQueue().getWasCleared())
                return;
            ArrayList<ScriptEntry> newEntries = (ArrayList<ScriptEntry>) new ArrayList<ScriptEntry>();
            for (ScriptEntry entr: entries) {
                try {
                    ScriptEntry toadd = entr.clone();
                    toadd.getObjects().clear();
                    newEntries.add(toadd);
                }
                catch (Throwable e) {
                    e.printStackTrace();
                }
            }
            ScriptQueue queue = new InstantQueue(queueId);
            queue.addDefinition("parent_queue", scriptEntry.getResidingQueue().id);
            scriptEntry.getResidingQueue().addDefinition("value", value);
            queue.addDefinition("value", value);
            queue.addEntries(newEntries);
            queue.start();
        }

    }

}
