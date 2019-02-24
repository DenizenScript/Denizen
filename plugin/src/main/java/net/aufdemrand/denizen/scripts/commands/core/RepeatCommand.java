package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.BracedCommand;
import net.aufdemrand.denizencore.utilities.debugging.dB.DebugElement;

import java.util.List;

public class RepeatCommand extends BracedCommand {

    private class RepeatData {
        public int index;
        public int target;
    }

    @Override
    public void onEnable() {
        setBraced();
    }


    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        boolean handled = false;

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!handled
                    && arg.matchesPrimitive(aH.PrimitiveType.Integer)) {
                scriptEntry.addObject("qty", arg.asElement());
                scriptEntry.addObject("braces", getBracedCommands(scriptEntry));
                handled = true;
            }
            else if (!handled
                    && arg.matches("stop")) {
                scriptEntry.addObject("stop", new Element(true));
                handled = true;
            }
            else if (!handled
                    && arg.matches("next")) {
                scriptEntry.addObject("next", new Element(true));
                handled = true;
            }
            else if (!handled
                    && arg.matches("\0CALLBACK")) {
                scriptEntry.addObject("callback", new Element(true));
                handled = true;
            }
            else if (!scriptEntry.hasObject("as_name")
                    && arg.matchesOnePrefix("as")) {
                scriptEntry.addObject("as_name", arg.asElement());
            }
            else if (arg.matchesOne("{")) {
                break;
            }
            else {
                arg.reportUnhandled();
            }
        }

        if (!handled) {
            throw new InvalidArgumentsException("Must specify a quantity or 'stop' or 'next'!");
        }

        scriptEntry.defaultObject("as_name", new Element("value"));

    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(ScriptEntry scriptEntry) {

        Element stop = scriptEntry.getElement("stop");
        Element next = scriptEntry.getElement("next");
        Element callback = scriptEntry.getElement("callback");
        Element quantity = scriptEntry.getElement("qty");
        Element as_name = scriptEntry.getElement("as_name");

        if (stop != null && stop.asBoolean()) {
            // Report to dB
            if (scriptEntry.dbCallShouldDebug()) {
                dB.report(scriptEntry, getName(), stop.debug());
            }
            boolean hasnext = false;
            for (int i = 0; i < scriptEntry.getResidingQueue().getQueueSize(); i++) {
                ScriptEntry entry = scriptEntry.getResidingQueue().getEntry(i);
                List<String> args = entry.getOriginalArguments();
                if (entry.getCommandName().equalsIgnoreCase("repeat") && args.size() > 0 && args.get(0).equalsIgnoreCase("\0CALLBACK")) {
                    hasnext = true;
                    break;
                }
            }
            if (hasnext) {
                while (scriptEntry.getResidingQueue().getQueueSize() > 0) {
                    ScriptEntry entry = scriptEntry.getResidingQueue().getEntry(0);
                    List<String> args = entry.getOriginalArguments();
                    if (entry.getCommandName().equalsIgnoreCase("repeat") && args.size() > 0 && args.get(0).equalsIgnoreCase("\0CALLBACK")) {
                        scriptEntry.getResidingQueue().removeEntry(0);
                        break;
                    }
                    scriptEntry.getResidingQueue().removeEntry(0);
                }
            }
            else {
                dB.echoError("Cannot stop repeat: not in one!");
            }
            return;
        }
        else if (next != null && next.asBoolean()) {
            // Report to dB
            if (scriptEntry.dbCallShouldDebug()) {
                dB.report(scriptEntry, getName(), next.debug());
            }
            boolean hasnext = false;
            for (int i = 0; i < scriptEntry.getResidingQueue().getQueueSize(); i++) {
                ScriptEntry entry = scriptEntry.getResidingQueue().getEntry(i);
                List<String> args = entry.getOriginalArguments();
                if (entry.getCommandName().equalsIgnoreCase("repeat") && args.size() > 0 && args.get(0).equalsIgnoreCase("\0CALLBACK")) {
                    hasnext = true;
                    break;
                }
            }
            if (hasnext) {
                while (scriptEntry.getResidingQueue().getQueueSize() > 0) {
                    ScriptEntry entry = scriptEntry.getResidingQueue().getEntry(0);
                    List<String> args = entry.getOriginalArguments();
                    if (entry.getCommandName().equalsIgnoreCase("repeat") && args.size() > 0 && args.get(0).equalsIgnoreCase("\0CALLBACK")) {
                        break;
                    }
                    scriptEntry.getResidingQueue().removeEntry(0);
                }
            }
            else {
                dB.echoError("Cannot stop repeat: not in one!");
            }
            return;
        }
        else if (callback != null && callback.asBoolean()) {
            if (scriptEntry.getOwner() != null && (scriptEntry.getOwner().getCommandName().equalsIgnoreCase("repeat") ||
                    scriptEntry.getOwner().getBracedSet() == null || scriptEntry.getOwner().getBracedSet().size() == 0 ||
                    scriptEntry.getBracedSet().get(0).value.get(scriptEntry.getBracedSet().get(0).value.size() - 1) != scriptEntry)) {
                RepeatData data = (RepeatData) scriptEntry.getOwner().getData();
                data.index++;
                if (data.index <= data.target) {
                    dB.echoDebug(scriptEntry, DebugElement.Header, "Repeat loop " + data.index);
                    scriptEntry.getResidingQueue().addDefinition(as_name.asString(), String.valueOf(data.index));
                    List<ScriptEntry> bracedCommands = BracedCommand.getBracedCommands(scriptEntry.getOwner()).get(0).value;
                    ScriptEntry callbackEntry = new ScriptEntry("REPEAT", new String[] {"\0CALLBACK", "as:" + as_name.asString()},
                            (scriptEntry.getScript() != null ? scriptEntry.getScript().getContainer() : null));
                    callbackEntry.copyFrom(scriptEntry);
                    callbackEntry.setOwner(scriptEntry.getOwner());
                    bracedCommands.add(callbackEntry);
                    for (int i = 0; i < bracedCommands.size(); i++) {
                        bracedCommands.get(i).setInstant(true);
                        bracedCommands.get(i).addObject("reqid", scriptEntry.getObject("reqid"));
                    }
                    scriptEntry.getResidingQueue().injectEntries(bracedCommands, 0);
                }
                else {
                    dB.echoDebug(scriptEntry, DebugElement.Header, "Repeat loop complete");
                }
            }
            else {
                dB.echoError("Repeat CALLBACK invalid: not a real callback!");
            }
        }
        else {
            List<BracedData> data = ((List<BracedData>) scriptEntry.getObject("braces"));
            if (data == null || data.isEmpty()) {
                dB.echoError(scriptEntry.getResidingQueue(), "Empty braces (internal)!");
                dB.echoError(scriptEntry.getResidingQueue(), "Empty braces!");
                return;
            }
            List<ScriptEntry> bracedCommandsList = data.get(0).value;

            if (bracedCommandsList == null || bracedCommandsList.isEmpty()) {
                dB.echoError("Empty braces!");
                return;
            }

            // Report to dB
            if (scriptEntry.dbCallShouldDebug()) {
                dB.report(scriptEntry, getName(), quantity.debug() + as_name.debug());
            }

            int target = quantity.asInt();
            if (target <= 0) {
                dB.echoDebug(scriptEntry, "Zero count, not looping...");
                return;
            }
            RepeatData datum = new RepeatData();
            datum.target = target;
            datum.index = 1;
            scriptEntry.setData(datum);
            ScriptEntry callbackEntry = new ScriptEntry("REPEAT", new String[] {"\0CALLBACK", "as:" + as_name.asString()},
                    (scriptEntry.getScript() != null ? scriptEntry.getScript().getContainer() : null));
            callbackEntry.copyFrom(scriptEntry);
            callbackEntry.setOwner(scriptEntry);
            bracedCommandsList.add(callbackEntry);
            scriptEntry.getResidingQueue().addDefinition(as_name.asString(), "1");
            for (int i = 0; i < bracedCommandsList.size(); i++) {
                bracedCommandsList.get(i).setInstant(true);
                bracedCommandsList.get(i).addObject("reqid", scriptEntry.getObject("reqid"));
            }
            scriptEntry.setInstant(true);
            scriptEntry.getResidingQueue().injectEntries(bracedCommandsList, 0);
        }
    }
}
