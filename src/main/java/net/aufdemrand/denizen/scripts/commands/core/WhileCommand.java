package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.Settings;
import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.BracedCommand;
import net.aufdemrand.denizen.tags.TagManager;
import net.aufdemrand.denizen.utilities.debugging.dB;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class WhileCommand extends BracedCommand {


    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        List<aH.Argument> original_args = aH.interpret(scriptEntry.getOriginalArguments());
        List<aH.Argument> parsed_args = aH.interpret(scriptEntry.getArguments());
        for (int i = 0; i < parsed_args.size(); i++) {

            aH.Argument arg = parsed_args.get(i);
            aH.Argument original = original_args.get(i);

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

            else if (!scriptEntry.hasObject("value")) {
                scriptEntry.addObject("value", new Element(original.raw_value).setPrefix("comparison_value"));
                break;
            }

            else {
                arg.reportUnhandled();
                break;
            }
        }

        if (!scriptEntry.hasObject("value") && !scriptEntry.hasObject("stop") && !scriptEntry.hasObject("next"))
            throw new InvalidArgumentsException("Must specify a comparison value or 'stop' or 'next'!");

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
            scriptEntry.getResidingQueue().BreakLoop("WHILE");
            return;
        }
        else if (next != null && next.asBoolean()) {
            // Report to dB
            dB.report(scriptEntry, getName(), next.debug());
            scriptEntry.getResidingQueue().BreakLoop("WHILE/NEXT");
            return;
        }

        // Get objects
        Element value = scriptEntry.getElement("value");
        ArrayList<ScriptEntry> bracedCommandsList =
                ((LinkedHashMap<String, ArrayList<ScriptEntry>>) scriptEntry.getObject("braces")).get("WHILE");

        if (bracedCommandsList == null || bracedCommandsList.isEmpty()) {
            dB.echoError("Empty braces!");
            return;
        }

        ScriptEntry[] bracedCommands = bracedCommandsList.toArray(new ScriptEntry[bracedCommandsList.size()]);

        // Report to dB
        dB.report(scriptEntry, getName(), value.debug());

        int loops = Settings.WhileMaxLoops();

        for (int incr = 0; incr < loops; incr++) {
            if (scriptEntry.getResidingQueue().getWasCleared())
                return;

            if (!TagManager.tag(scriptEntry.getPlayer(), scriptEntry.getNPC(),
                    value.asString(), false, scriptEntry).equalsIgnoreCase("true"))
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
            scriptEntry.getResidingQueue().addDefinition("loop_index", String.valueOf(incr + 1));

            // Run everything instantly
            String result = scriptEntry.getResidingQueue().runNow(newEntries, "WHILE");
            if (result != null && !result.endsWith("/NEXT"))
                return;
        }
    }
}
