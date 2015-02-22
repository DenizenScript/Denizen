package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.Settings;
import net.aufdemrand.denizen.tags.BukkitTagContext;
import net.aufdemrand.denizencore.DenizenCore;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.exceptions.ScriptEntryCreationException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.BracedCommand;
import net.aufdemrand.denizencore.tags.TagManager;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.utilities.debugging.dB.DebugElement;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class WhileCommand extends BracedCommand {

    private class WhileData {
        public int index;
        public String value;
        public long LastChecked;
        int instaTicks;
    }

    @Override
    public void onEnable() {
        setBraced();
    }


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

            else if (!scriptEntry.hasObject("callback")
                    && arg.matches("\0CALLBACK")) {
                scriptEntry.addObject("callback", Element.TRUE);
                break;
            }

            else if (!scriptEntry.hasObject("value")) {
                scriptEntry.addObject("value", new Element(original.raw_value));
                scriptEntry.addObject("parsed_value", new Element(arg.raw_value));
                break;
            }

            else {
                arg.reportUnhandled();
                break;
            }
        }

        if (!scriptEntry.hasObject("value") && !scriptEntry.hasObject("stop") && !scriptEntry.hasObject("next") && !scriptEntry.hasObject("callback"))
            throw new InvalidArgumentsException("Must specify a comparison value or 'stop' or 'next'!");

        scriptEntry.addObject("braces", getBracedCommands(scriptEntry));

    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        Element stop = scriptEntry.getElement("stop");
        Element next = scriptEntry.getElement("next");
        Element callback = scriptEntry.getElement("callback");

        if (stop != null && stop.asBoolean()) {
            // Report to dB
            dB.report(scriptEntry, getName(), stop.debug());
            boolean hasnext = false;
            for (int i = 0; i < scriptEntry.getResidingQueue().getQueueSize(); i++) {
                ScriptEntry entry = scriptEntry.getResidingQueue().getEntry(i);
                List<String> args = entry.getOriginalArguments();
                if (entry.getCommandName().equalsIgnoreCase("while") && args.size() > 0 && args.get(0).equalsIgnoreCase("\0CALLBACK")) {
                    hasnext = true;
                    break;
                }
            }
            if (hasnext) {
                while (scriptEntry.getResidingQueue().getQueueSize() > 0) {
                    ScriptEntry entry = scriptEntry.getResidingQueue().getEntry(0);
                    List<String> args = entry.getOriginalArguments();
                    if (entry.getCommandName().equalsIgnoreCase("while") && args.size() > 0 && args.get(0).equalsIgnoreCase("\0CALLBACK")) {
                        scriptEntry.getResidingQueue().removeEntry(0);
                        break;
                    }
                    scriptEntry.getResidingQueue().removeEntry(0);
                }
            }
            else {
                dB.echoError(scriptEntry.getResidingQueue(), "Cannot stop while: not in one!");
            }
            return;
        }
        else if (next != null && next.asBoolean()) {
            // Report to dB
            dB.report(scriptEntry, getName(), next.debug());
            boolean hasnext = false;
            for (int i = 0; i < scriptEntry.getResidingQueue().getQueueSize(); i++) {
                ScriptEntry entry = scriptEntry.getResidingQueue().getEntry(i);
                List<String> args = entry.getOriginalArguments();
                if (entry.getCommandName().equalsIgnoreCase("while") && args.size() > 0 && args.get(0).equalsIgnoreCase("\0CALLBACK")) {
                    hasnext = true;
                    break;
                }
            }
            if (hasnext) {
                while (scriptEntry.getResidingQueue().getQueueSize() > 0) {
                    ScriptEntry entry = scriptEntry.getResidingQueue().getEntry(0);
                    List<String> args = entry.getOriginalArguments();
                    if (entry.getCommandName().equalsIgnoreCase("while") && args.size() > 0 && args.get(0).equalsIgnoreCase("\0CALLBACK")) {
                        break;
                    }
                    scriptEntry.getResidingQueue().removeEntry(0);
                }
            }
            else {
                dB.echoError(scriptEntry.getResidingQueue(), "Cannot stop while: not in one!");
            }
            return;
        }
        else if (callback != null && callback.asBoolean()) {
            if (scriptEntry.getOwner() != null && (scriptEntry.getOwner().getCommandName().equalsIgnoreCase("while") ||
                    scriptEntry.getOwner().getBracedSet() == null || scriptEntry.getOwner().getBracedSet().size() == 0 ||
                    scriptEntry.getBracedSet().get(0).value.get(scriptEntry.getBracedSet().get(0).value.size() - 1) != scriptEntry)) {
                WhileData data = (WhileData)scriptEntry.getOwner().getData();
                data.index++;
                if (System.currentTimeMillis() - data.LastChecked < 50) {
                    data.instaTicks++;
                    int max = Settings.whileMaxLoops();
                    if (data.instaTicks > max && max != 0)
                        return;
                }
                else {
                    data.instaTicks = 0;
                }
                data.LastChecked = System.currentTimeMillis();
                if (TagManager.tag(data.value, DenizenCore.getImplementation().getTagContextFor(scriptEntry, false)).equalsIgnoreCase("true")) {
                    dB.echoDebug(scriptEntry, DebugElement.Header, "While loop " + data.index);
                    scriptEntry.getResidingQueue().addDefinition("loop_index", String.valueOf(data.index));
                    List<ScriptEntry> bracedCommands = BracedCommand.getBracedCommands(scriptEntry.getOwner()).get(0).value;
                    ScriptEntry callbackEntry = null;
                    try {
                        callbackEntry = new ScriptEntry("WHILE", new String[] { "\0CALLBACK" },
                                (scriptEntry.getScript() != null ? scriptEntry.getScript().getContainer(): null));
                        callbackEntry.copyFrom(scriptEntry);
                    }
                    catch (ScriptEntryCreationException e) {
                        dB.echoError(scriptEntry.getResidingQueue(), e);
                    }
                    callbackEntry.setOwner(scriptEntry.getOwner());
                    bracedCommands.add(callbackEntry);
                    for (int i = 0; i < bracedCommands.size(); i++) {
                        bracedCommands.get(i).setInstant(true);
                        bracedCommands.get(i).addObject("reqId", scriptEntry.getObject("reqId"));
                    }
                    scriptEntry.getResidingQueue().injectEntries(bracedCommands, 0);
                }
            }
            else {
                dB.echoError(scriptEntry.getResidingQueue(), "Wnew BukkitTagContext(scriptEntry, false)hile CALLBACK invalid: not a real callback!");
            }
        }

        else {

            // Get objects
            Element value = scriptEntry.getElement("value");
            Element parsed_value = scriptEntry.getElement("parsed_value");
            List<ScriptEntry> bracedCommandsList =
                    ((List<BracedData>) scriptEntry.getObject("braces")).get(0).value;

            if (bracedCommandsList == null || bracedCommandsList.isEmpty()) {
                dB.echoError(scriptEntry.getResidingQueue(), "Empty braces!");
                return;
            }

            // Report to dB
            dB.report(scriptEntry, getName(), value.debug());

            if (!parsed_value.asString().equalsIgnoreCase("true")) {
                return;
            }

            WhileData datum = new WhileData();
            datum.index = 1;
            datum.value = value.asString();
            datum.LastChecked = System.currentTimeMillis();
            datum.instaTicks = 1;
            scriptEntry.setData(datum);
            ScriptEntry callbackEntry = null;
            try {
                callbackEntry = new ScriptEntry("WHILE", new String[] { "\0CALLBACK" },
                        (scriptEntry.getScript() != null ? scriptEntry.getScript().getContainer(): null));
                callbackEntry.copyFrom(scriptEntry);
            }
            catch (ScriptEntryCreationException e) {
                dB.echoError(scriptEntry.getResidingQueue(), e);
            }
            callbackEntry.setOwner(scriptEntry);
            bracedCommandsList.add(callbackEntry);
            scriptEntry.getResidingQueue().addDefinition("loop_index", "1");
            for (int i = 0; i < bracedCommandsList.size(); i++) {
                bracedCommandsList.get(i).setInstant(true);
                bracedCommandsList.get(i).addObject("reqId", scriptEntry.getObject("reqId"));
            }
            scriptEntry.getResidingQueue().injectEntries(bracedCommandsList, 0);
        }
    }
}
