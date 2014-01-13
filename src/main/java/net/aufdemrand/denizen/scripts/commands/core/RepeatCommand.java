package net.aufdemrand.denizen.scripts.commands.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.BracedCommand;
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
        ArrayList<ScriptEntry> bracedCommandsList =
                ((LinkedHashMap<String, ArrayList<ScriptEntry>>) scriptEntry.getObject("braces")).get("REPEAT");

        if (bracedCommandsList == null || bracedCommandsList.isEmpty()) {
            dB.echoError("Empty braces!");
            return;
        }

        ScriptEntry[] bracedCommands = bracedCommandsList.toArray(new ScriptEntry[bracedCommandsList.size()]);

        // Report to dB
        dB.report(scriptEntry, getName(), qty.debug());

        int loops = qty.asInt();

        for (int incr = 0; incr < loops; incr++) {
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

            // Set the %value% and inject entries
            scriptEntry.getResidingQueue().addDefinition("value", String.valueOf(incr + 1));
            scriptEntry.getResidingQueue().injectEntries(newEntries, 0);
            int entries = newEntries.size();
            int entrycount = scriptEntry.getResidingQueue().getQueueSize();

            // Run the entries immediately
            for (int i = 0; i < entries; i++) {
                denizen.getScriptEngine().revolve(scriptEntry.getResidingQueue());
                entrycount--;
                if (scriptEntry.getResidingQueue().getQueueSize() > entrycount) {
                    entries += scriptEntry.getResidingQueue().getQueueSize() - entrycount;
                    entrycount += scriptEntry.getResidingQueue().getQueueSize() - entrycount;
                }
            }
        }

    }
}
