package net.aufdemrand.denizen.scripts.commands;

import java.util.ArrayList;
import java.util.TreeMap;

import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.exceptions.ScriptEntryCreationException;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.utilities.debugging.dB;

public abstract class BracedCommand extends AbstractCommand {



    public ArrayList<ScriptEntry> getBracedCommands(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        return getBracedCommands(scriptEntry, 0);
    }
    public final boolean hyperdebug = false;

    public ArrayList<ScriptEntry> getBracedCommands(ScriptEntry scriptEntry, int extraEntries) throws InvalidArgumentsException {
        TreeMap<Integer, ArrayList<String>> commandList = new TreeMap<Integer, ArrayList<String>>();
        ArrayList<ScriptEntry> commands = new ArrayList<ScriptEntry>();
        int bracesEntered = 0;
        boolean newCommand = false;
        if (hyperdebug) dB.echoDebug("Starting getBracedCommands...");
        scriptEntry.getResidingQueue().injectEntry(scriptEntry, 0);
        if (hyperdebug) dB.echoDebug("...with queue size: " + (extraEntries > 0 ? extraEntries + 1 : 1));
        if (hyperdebug) dB.echoDebug("...with first command name: " + scriptEntry.getCommandName());
        if (hyperdebug) dB.echoDebug("...with first command arguments: " + scriptEntry.getArguments());
        for (int x = 0; x < (extraEntries > 0 ? extraEntries + 1 : 1); x++) {
            ScriptEntry entry = scriptEntry.getResidingQueue().getEntry(0);
            if (hyperdebug) dB.echoDebug("Entry found: " + entry.getCommandName());
            for (aH.Argument arg : aH.interpret(entry.getArguments())) {
                if (hyperdebug) dB.echoDebug("Arg found: " + arg.raw_value);
                if (arg.matches("{")) {
                    bracesEntered++;
                    if (hyperdebug) dB.echoDebug("Opened brace; " + bracesEntered + " now");
                    if (bracesEntered > 1) {
                        commandList.get(commandList.lastKey()).add(arg.raw_value);
                    }
                }
                else if (arg.matches("}")) {
                    bracesEntered--;
                    if (hyperdebug) dB.echoDebug("Closed brace; " + bracesEntered + " now");
                    if (bracesEntered > 0) {
                        commandList.get(commandList.lastKey()).add(arg.raw_value);
                    }
                }
                else if (newCommand && bracesEntered == 1) {
                    commandList.put(commandList.size(), new ArrayList<String>());
                    commandList.get(commandList.lastKey()).add(arg.raw_value);
                    newCommand = false;
                    if (hyperdebug) dB.echoDebug("Treating as new command");
                }
                else if (bracesEntered == 0) {
                    if (hyperdebug) dB.echoDebug("Ignoring");
                    // Do nothing ... for now...
                }
                else if (arg.matches("-") && bracesEntered == 1) {
                    newCommand = true;
                    if (hyperdebug) dB.echoDebug("Assuming following is a new command");
                }
                else {
                    commandList.get(commandList.lastKey()).add(arg.raw_value);
                    if (hyperdebug) dB.echoDebug("Adding to previous command");
                }
            }
            for (ArrayList<String> command : commandList.values()) {
                try {
                    if (command.isEmpty()) {
                        if (hyperdebug) dB.echoError("Empty command?");
                        continue;
                    }
                    String cmd = command.get(0);
                    if (hyperdebug) dB.echoDebug("Calculating " + cmd);
                    command.remove(0);
                    String[] args = new String[command.size()];
                    args = command.toArray(args);
                    commands.add(new ScriptEntry(cmd,
                            args,
                            scriptEntry.getScript().getContainer()));
                    commands.get(commands.size() - 1).setPlayer(scriptEntry.getPlayer());
                    commands.get(commands.size() - 1).setNPC(scriptEntry.getNPC());
                    if (hyperdebug) dB.echoDebug("Command added: " + cmd + ", with " + String.valueOf(args.length) + " arguments");
                } catch (ScriptEntryCreationException e) {
                    if (hyperdebug) dB.echoError(e.getMessage());
                }
            }
            scriptEntry.getResidingQueue().removeEntry(0);
        }
        return commands;
    }

}