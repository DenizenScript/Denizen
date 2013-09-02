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

        scriptEntry.addObject("entries", getBracedCommands(scriptEntry, 1));

    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        // Get objects
        Element qty = scriptEntry.getElement("qty");
        ArrayList<ScriptEntry> entries = (ArrayList<ScriptEntry>) scriptEntry.getObject("entries");

        // Report to dB
        dB.report(getName(), qty.debug());

        for (int incr = 0; incr < qty.asInt(); incr++) {
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
            ScriptQueue queue = new InstantQueue(UUID.randomUUID().toString());
            scriptEntry.getResidingQueue().addDefinition("value", String.valueOf(incr + 1));
            queue.addDefinition("value", String.valueOf(incr + 1));
            queue.addEntries(newEntries);
            queue.start();
        }

    }
}
