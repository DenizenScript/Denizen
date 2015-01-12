package net.aufdemrand.denizen.scripts.commands.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.exceptions.ScriptEntryCreationException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.BracedCommand;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.utilities.debugging.dB.DebugElement;


public class ForeachCommand extends BracedCommand {

    private class ForeachData {
        public int index;
        public dList list;
    }

    @Override
    public void onEnable() {
        setBraced();
    }

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

            else if (!scriptEntry.hasObject("callback")
                    && arg.matches("\0CALLBACK")) {
                scriptEntry.addObject("callback", Element.TRUE);
                break;
            }

            else if (!scriptEntry.hasObject("list")
                    && arg.matchesArgumentType(dList.class)) {
                scriptEntry.addObject("list", dList.valueOf(arg.raw_value));
                scriptEntry.addObject("braces", getBracedCommands(scriptEntry));
                break;
            }

            else {
                arg.reportUnhandled();
                break;
            }

        }

        if (!scriptEntry.hasObject("list")
                && !scriptEntry.hasObject("stop")
                && !scriptEntry.hasObject("next")
                && !scriptEntry.hasObject("callback"))
            throw new InvalidArgumentsException("Must specify a valid list or 'stop' or 'next'!");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        Element stop = scriptEntry.getElement("stop");
        Element next = scriptEntry.getElement("next");
        Element callback = scriptEntry.getElement("callback");
        dList list = (dList) scriptEntry.getObject("list");

        if (stop != null && stop.asBoolean()) {
            // Report to dB
            dB.report(scriptEntry, getName(), stop.debug());
            boolean hasnext = false;
            for (int i = 0; i < scriptEntry.getResidingQueue().getQueueSize(); i++) {
                ScriptEntry entry = scriptEntry.getResidingQueue().getEntry(i);
                List<String> args = entry.getOriginalArguments();
                if (entry.getCommandName().equalsIgnoreCase("foreach") && args.size() > 0 && args.get(0).equalsIgnoreCase("\0CALLBACK")) {
                    hasnext = true;
                    break;
                }
            }
            if (hasnext) {
                while (scriptEntry.getResidingQueue().getQueueSize() > 0) {
                    ScriptEntry entry = scriptEntry.getResidingQueue().getEntry(0);
                    List<String> args = entry.getOriginalArguments();
                    if (entry.getCommandName().equalsIgnoreCase("foreach") && args.size() > 0 && args.get(0).equalsIgnoreCase("\0CALLBACK")) {
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
                if (entry.getCommandName().equalsIgnoreCase("foreach") && args.size() > 0 && args.get(0).equalsIgnoreCase("\0CALLBACK")) {
                    hasnext = true;
                    break;
                }
            }
            if (hasnext) {
                while (scriptEntry.getResidingQueue().getQueueSize() > 0) {
                    ScriptEntry entry = scriptEntry.getResidingQueue().getEntry(0);
                    List<String> args = entry.getOriginalArguments();
                    if (entry.getCommandName().equalsIgnoreCase("foreach") && args.size() > 0 && args.get(0).equalsIgnoreCase("\0CALLBACK")) {
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
            if (scriptEntry.getOwner() != null && (scriptEntry.getOwner().getCommandName().equalsIgnoreCase("foreach") ||
                    scriptEntry.getOwner().getBracedSet() == null || scriptEntry.getOwner().getBracedSet().size() == 0 ||
                    scriptEntry.getBracedSet().get("REPEAT").get(scriptEntry.getBracedSet().get("FOREACH").size() - 1) != scriptEntry)) {
                ForeachData data = (ForeachData)scriptEntry.getOwner().getData();
                data.index++;
                if (data.index <= data.list.size()) {
                    dB.echoDebug(scriptEntry, DebugElement.Header, "Foreach loop " + data.index);
                    scriptEntry.getResidingQueue().addDefinition("loop_index", String.valueOf(data.index));
                    scriptEntry.getResidingQueue().addDefinition("value", String.valueOf(data.list.get(data.index - 1)));
                    ArrayList<ScriptEntry> bracedCommands = BracedCommand.getBracedCommands(scriptEntry.getOwner()).get("FOREACH");
                    ScriptEntry callbackEntry = null;
                    try {
                        callbackEntry = new ScriptEntry("FOREACH", new String[] { "\0CALLBACK" },
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
                dB.echoError(scriptEntry.getResidingQueue(), "Foreach CALLBACK invalid: not a real callback!");
            }
        }

        else {

            // Get objects
            ArrayList<ScriptEntry> bracedCommandsList =
                    ((LinkedHashMap<String, ArrayList<ScriptEntry>>) scriptEntry.getObject("braces")).get("FOREACH");

            if (bracedCommandsList == null || bracedCommandsList.isEmpty()) {
                dB.echoError(scriptEntry.getResidingQueue(), "Empty braces!");
                return;
            }

            // Report to dB
            dB.report(scriptEntry, getName(), list.debug());

            int target = list.size();
            if (target <= 0) {
                dB.echoDebug(scriptEntry, "Empty list, not looping...");
                return;
            }
            ForeachData datum = new ForeachData();
            datum.list = list;
            datum.index = 1;
            scriptEntry.setData(datum);
            ScriptEntry callbackEntry = null;
            try {
                callbackEntry = new ScriptEntry("FOREACH", new String[] { "\0CALLBACK" },
                        (scriptEntry.getScript() != null ? scriptEntry.getScript().getContainer(): null));
                callbackEntry.copyFrom(scriptEntry);
            }
            catch (ScriptEntryCreationException e) {
                dB.echoError(scriptEntry.getResidingQueue(), e);
            }
            callbackEntry.setOwner(scriptEntry);
            bracedCommandsList.add(callbackEntry);
            scriptEntry.getResidingQueue().addDefinition("value", list.get(0));
            scriptEntry.getResidingQueue().addDefinition("loop_index", "1");
            for (int i = 0; i < bracedCommandsList.size(); i++) {
                bracedCommandsList.get(i).setInstant(true);
                bracedCommandsList.get(i).addObject("reqId", scriptEntry.getObject("reqId"));
            }
            scriptEntry.getResidingQueue().injectEntries(bracedCommandsList, 0);
        }
    }
}
