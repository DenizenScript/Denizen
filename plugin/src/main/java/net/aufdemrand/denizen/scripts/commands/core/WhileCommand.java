package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.Settings;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.BracedCommand;
import net.aufdemrand.denizencore.scripts.commands.core.IfCommand;
import net.aufdemrand.denizencore.utilities.debugging.dB.DebugElement;

import java.util.ArrayList;
import java.util.List;

public class WhileCommand extends BracedCommand {

    private class WhileData {
        public int index;
        public List<String> value;
        public long LastChecked;
        int instaTicks;
    }

    @Override
    public void onEnable() {
        setBraced();
        setParseArgs(false);
    }


    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        List<String> comparisons = new ArrayList<>();

        if (scriptEntry.getArguments().size() == 1) {
            String arg = scriptEntry.getArguments().get(0);
            if (arg.equalsIgnoreCase("stop")) {
                scriptEntry.addObject("stop", new Element(true));
            }
            else if (arg.equalsIgnoreCase("next")) {
                scriptEntry.addObject("next", new Element(true));
            }
            else if (arg.equals("\0CALLBACK")) {
                scriptEntry.addObject("callback", new Element(true));
            }
        }
        for (String arg : scriptEntry.getArguments()) {
            if (arg.equals("{")) {
                break;
            }
            comparisons.add(arg);
        }
        if (comparisons.isEmpty() && !scriptEntry.hasObject("stop") && !scriptEntry.hasObject("next") && !scriptEntry.hasObject("callback")) {
            throw new InvalidArgumentsException("Must specify a comparison value or 'stop' or 'next'!");
        }
        scriptEntry.addObject("braces", getBracedCommands(scriptEntry));
        scriptEntry.addObject("comparisons", comparisons);

    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(ScriptEntry scriptEntry) {

        Element stop = scriptEntry.getElement("stop");
        Element next = scriptEntry.getElement("next");
        Element callback = scriptEntry.getElement("callback");

        if (stop != null && stop.asBoolean()) {
            // Report to dB
            if (scriptEntry.dbCallShouldDebug()) {
                dB.report(scriptEntry, getName(), stop.debug());
            }
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
            if (scriptEntry.dbCallShouldDebug()) {
                dB.report(scriptEntry, getName(), next.debug());
            }
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
                WhileData data = (WhileData) scriptEntry.getOwner().getData();
                data.index++;
                if (System.currentTimeMillis() - data.LastChecked < 50) {
                    data.instaTicks++;
                    int max = Settings.whileMaxLoops();
                    if (data.instaTicks > max && max != 0) {
                        return;
                    }
                }
                else {
                    data.instaTicks = 0;
                }
                data.LastChecked = System.currentTimeMillis();
                boolean run = new IfCommand.ArgComparer().compare(new ArrayList(data.value), scriptEntry);
                if (run) {
                    dB.echoDebug(scriptEntry, DebugElement.Header, "While loop " + data.index);
                    scriptEntry.getResidingQueue().addDefinition("loop_index", String.valueOf(data.index));
                    List<ScriptEntry> bracedCommands = BracedCommand.getBracedCommands(scriptEntry.getOwner()).get(0).value;
                    ScriptEntry callbackEntry = new ScriptEntry("WHILE", new String[] {"\0CALLBACK"},
                            (scriptEntry.getScript() != null ? scriptEntry.getScript().getContainer() : null));
                    callbackEntry.copyFrom(scriptEntry);
                    callbackEntry.setOwner(scriptEntry.getOwner());
                    bracedCommands.add(callbackEntry);
                    for (int i = 0; i < bracedCommands.size(); i++) {
                        bracedCommands.get(i).setInstant(true);
                    }
                    scriptEntry.getResidingQueue().injectEntries(bracedCommands, 0);
                }
                else {
                    dB.echoDebug(scriptEntry, DebugElement.Header, "While loop complete");
                }
            }
            else {
                dB.echoError(scriptEntry.getResidingQueue(), "While CALLBACK invalid: not a real callback!");
            }
        }
        else {
            List<String> comparisons = (List<String>) scriptEntry.getObject("comparisons");
            List<BracedData> data = ((List<BracedData>) scriptEntry.getObject("braces"));
            if (data == null || data.isEmpty()) {
                dB.echoError(scriptEntry.getResidingQueue(), "Empty braces (internal)!");
                return;
            }
            List<ScriptEntry> bracedCommandsList = data.get(0).value;

            if (bracedCommandsList == null || bracedCommandsList.isEmpty()) {
                dB.echoError(scriptEntry.getResidingQueue(), "Empty braces!");
                return;
            }
            boolean run = new IfCommand.ArgComparer().compare(comparisons, scriptEntry);

            // Report to dB
            if (scriptEntry.dbCallShouldDebug()) {
                dB.report(scriptEntry, getName(), aH.debugObj("run_first_loop", run));
            }

            if (!run) {
                return;
            }

            WhileData datum = new WhileData();
            datum.index = 1;
            datum.value = comparisons;
            datum.LastChecked = System.currentTimeMillis();
            datum.instaTicks = 1;
            scriptEntry.setData(datum);
            ScriptEntry callbackEntry = new ScriptEntry("WHILE", new String[] {"\0CALLBACK"},
                    (scriptEntry.getScript() != null ? scriptEntry.getScript().getContainer() : null));
            callbackEntry.copyFrom(scriptEntry);
            callbackEntry.setOwner(scriptEntry);
            bracedCommandsList.add(callbackEntry);
            scriptEntry.getResidingQueue().addDefinition("loop_index", "1");
            for (int i = 0; i < bracedCommandsList.size(); i++) {
                bracedCommandsList.get(i).setInstant(true);
            }
            scriptEntry.setInstant(true);
            scriptEntry.getResidingQueue().injectEntries(bracedCommandsList, 0);
        }
    }
}
