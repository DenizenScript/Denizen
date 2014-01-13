package net.aufdemrand.denizen.scripts.commands.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.dList;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.BracedCommand;
import net.aufdemrand.denizen.utilities.debugging.dB;


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

            // Don't report unhandled since getBracedCommands will handle the remainder

        }

        if (!scriptEntry.hasObject("list"))
            throw new InvalidArgumentsException("Must specify a valid list!");

        scriptEntry.addObject("braces", getBracedCommands(scriptEntry, 1));

    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        // Get objects
        dList list = (dList) scriptEntry.getObject("list");
        ArrayList<ScriptEntry> bracedCommandsList =
                ((LinkedHashMap<String, ArrayList<ScriptEntry>>) scriptEntry.getObject("braces")).get("FOREACH");

        if (bracedCommandsList == null || bracedCommandsList.isEmpty()) {
            dB.echoError("Empty braces!");
            return;
        }

        ScriptEntry[] bracedCommands = bracedCommandsList.toArray(new ScriptEntry[bracedCommandsList.size()]);

        // Report to dB
        dB.report(scriptEntry, getName(), list.debug());

        // Start iteration
        for (String value : list) {
            // Check if Queue was cleared (end the foreach if so!)
            if (scriptEntry.getResidingQueue().getWasCleared())
                return;

            // Build cloned script entries for this iteration
            ArrayList<ScriptEntry> newEntries = new ArrayList<ScriptEntry>();
            for (ScriptEntry entry : bracedCommands) {
                try {
                    ScriptEntry toAdd = entry.clone();
                    toAdd.getObjects().clear();
                    newEntries.add(toAdd);
                } catch (Throwable e) {
                    dB.echoError(e);
                }
            }

            // Set the %value% and inject entries
            scriptEntry.getResidingQueue().addDefinition("value", value);
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
