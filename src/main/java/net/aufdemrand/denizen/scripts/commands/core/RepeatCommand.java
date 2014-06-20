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
                    && arg.matchesPrimitive(aH.PrimitiveType.Integer)) {
                scriptEntry.addObject("qty", arg.asElement());
                break;
            }

            else if (!scriptEntry.hasObject("stop")
                    && arg.matches("stop")) {
                scriptEntry.addObject("stop", Element.TRUE);
                break;
            }

            else if (!scriptEntry.hasObject("next")
                    && arg.matches("next")) {
                scriptEntry.addObject("next", Element.TRUE);
                break;
            }

            else {
                arg.reportUnhandled();
                break;
            }

        }

        if (!scriptEntry.hasObject("qty") && !scriptEntry.hasObject("stop") && !scriptEntry.hasObject("next"))
            throw new InvalidArgumentsException("Must specify a quantity or 'stop' or 'next'!");

        scriptEntry.addObject("braces", getBracedCommands(scriptEntry, 1));

    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        Element stop = scriptEntry.getElement("stop");
        Element next = scriptEntry.getElement("next");

        if (stop != null && stop.asBoolean()) {
            // Report to dB
            dB.report(scriptEntry, getName(), stop.debug());
            scriptEntry.getResidingQueue().BreakLoop("REPEAT");
            return;
        }
        else if (next != null && next.asBoolean()) {
            // Report to dB
            dB.report(scriptEntry, getName(), next.debug());
            scriptEntry.getResidingQueue().BreakLoop("REPEAT/NEXT");
            return;
        }

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
                    toAdd.addObject("reqId", scriptEntry.getObject("reqId"));
                    toAdd.setFinished(true);
                    newEntries.add(toAdd);
                }
                catch (Throwable e) {
                    dB.echoError(e);
                }
            }

            // Set the %value% and inject entries
            scriptEntry.getResidingQueue().addDefinition("value", String.valueOf(incr + 1));

            // Run everything instantly
            String result = scriptEntry.getResidingQueue().runNow(newEntries, "REPEAT");
            if (result != null && !result.endsWith("/NEXT"))
                return;
        }
    }
}
