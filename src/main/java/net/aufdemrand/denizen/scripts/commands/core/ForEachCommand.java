package net.aufdemrand.denizen.scripts.commands.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.dList;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.BracedCommand;
import net.aufdemrand.denizen.utilities.debugging.dB;


public class ForEachCommand extends BracedCommand {

    // - foreach li@p@Vegeta|p@MuhammedAli|n@123 {
    //   - announce "Hello, <%value%.name>!"
    //   }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("stop")
                    && arg.matches("stop")) {
                scriptEntry.addObject("stop", Element.TRUE);
                break;
            }

            else if (!scriptEntry.hasObject("next")
                    && arg.matches("next")) {
                scriptEntry.addObject("next", Element.TRUE);
                break;
            }

            else if (!scriptEntry.hasObject("list")
                    && arg.matchesArgumentType(dList.class)) {
                scriptEntry.addObject("list", arg.asType(dList.class));
                scriptEntry.addObject("braces", getBracedCommands(scriptEntry, 1));
                break;
            }

            else {
                arg.reportUnhandled();
                break;
            }

        }

        if (!scriptEntry.hasObject("list")
                && !scriptEntry.hasObject("stop")
                && !scriptEntry.hasObject("next"))
            throw new InvalidArgumentsException("Must specify a valid list or 'stop' or 'next'!");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        Element stop = scriptEntry.getElement("stop");
        Element next = scriptEntry.getElement("next");

        if (stop != null && stop.asBoolean()) {
            dB.report(scriptEntry, getName(), stop.debug());
            scriptEntry.getResidingQueue().BreakLoop("FOREACH");
            return;
        }
        else if (next != null && next.asBoolean()) {
            dB.report(scriptEntry, getName(), next.debug());
            scriptEntry.getResidingQueue().BreakLoop("FOREACH/NEXT");
            return;
        }

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

        int index = 0;

        // Start iteration
        for (String value : list) {
            index++;
            // Check if Queue was cleared (end the foreach if so!)
            if (scriptEntry.getResidingQueue().getWasCleared())
                return;

            // Build cloned script entries for this iteration
            ArrayList<ScriptEntry> newEntries = new ArrayList<ScriptEntry>();
            for (ScriptEntry entry : bracedCommands) {
                try {
                    ScriptEntry toAdd = entry.clone();
                    toAdd.getObjects().clear();
                    toAdd.addObject("reqId", scriptEntry.getObject("reqId"));
                    toAdd.setFinished(true);
                    newEntries.add(toAdd);
                } catch (Throwable e) {
                    dB.echoError(e);
                }
            }

            // Set the %value% and inject entries
            scriptEntry.getResidingQueue().addDefinition("value", value);
            scriptEntry.getResidingQueue().addDefinition("loop_index", String.valueOf(index));

            // Run everything instantly
            String result = scriptEntry.getResidingQueue().runNow(newEntries, "FOREACH");
            if (result != null && !result.endsWith("/NEXT"))
                return;
        }

    }

}
