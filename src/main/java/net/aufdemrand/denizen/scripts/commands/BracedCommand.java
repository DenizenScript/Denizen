package net.aufdemrand.denizen.scripts.commands;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;

import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.exceptions.ScriptEntryCreationException;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.utilities.debugging.dB;

public abstract class BracedCommand extends AbstractCommand {

    public final boolean hyperdebug = false;

    /**
     * Gets the commands inside the braces of this ScriptEntry.
     *
     * @param scriptEntry
     *          The ScriptEntry to get the braced commands from.
     *
     * @param startArg
     *          The argument to start at.
     * @return
     *          The list of ScriptEntries to be executed in the command.
     *
     * @throws InvalidArgumentsException
     */
    public LinkedHashMap<String, ArrayList<ScriptEntry>> getBracedCommands(ScriptEntry scriptEntry, int startArg) throws InvalidArgumentsException {

        // We need a place to store the commands being built at...
        TreeMap<Integer, ArrayList<String>> commandList = new TreeMap<Integer, ArrayList<String>>();
        // And a place to store all the final braces...
        LinkedHashMap<String, ArrayList<ScriptEntry>> bracedSections = new LinkedHashMap<String, ArrayList<ScriptEntry>>();

        int bracesEntered = 0;
        boolean newCommand = true;

        // Inject the scriptEntry into the front of the queue, otherwise it doesn't exist
        scriptEntry.getResidingQueue().injectEntry(scriptEntry, 0);
        // Send info to debug
        if (hyperdebug) dB.echoDebug("Starting getBracedCommands...");

        // If the specified amount of possible entries is less than the queue size, print that instead
        if (hyperdebug) dB.echoDebug("...with queue size: " + scriptEntry.getResidingQueue().getQueueSize());
        if (hyperdebug) dB.echoDebug("...with first command name: " + scriptEntry.getCommandName());
        if (hyperdebug) dB.echoDebug("...with first command arguments: " + scriptEntry.getArguments());

        ScriptEntry entry = scriptEntry.getResidingQueue().getEntry(0);
        if (hyperdebug) dB.echoDebug("Entry found: " + entry.getCommandName());

        // Loop through the arguments of each entry
        List<aH.Argument> argList = aH.interpret(entry.getArguments());
        
        // Set the variable to use for naming braced command lists; the first should be the command name
        String bracesName = scriptEntry.getCommandName();
        
        for (int i = startArg; i < argList.size(); i++) {
            aH.Argument arg = argList.get(i);
            if (hyperdebug) dB.echoDebug("Arg found: " + arg.raw_value);

            // Listen for opened braces
            if (arg.matches("{")) {
                bracesEntered++;
                newCommand = false;
                if (hyperdebug) dB.echoDebug("Opened brace; " + bracesEntered + " now");
                if (bracesEntered > 1) {
                    commandList.get(commandList.lastKey()).add(arg.raw_value);
                }
            }

            // Listen for closed braces
            else if (arg.matches("}")) {
                bracesEntered--;
                newCommand = false;
                if (hyperdebug) dB.echoDebug("Closed brace; " + bracesEntered + " now");
                if (bracesEntered > 0) {
                    commandList.get(commandList.lastKey()).add(arg.raw_value);
                }
                else {
                    if (bracedSections.containsKey(bracesName)) {
                        if (hyperdebug) dB.echoError("You may not have braced commands with the same arguments.");
                        continue;
                    }
                    ArrayList<ScriptEntry> bracesSection = new ArrayList<ScriptEntry>();
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
                            bracesSection.add(new ScriptEntry(cmd,
                                    args,
                                    scriptEntry.getScript() != null ? scriptEntry.getScript().getContainer() : null));
                            bracesSection.get(bracesSection.size() - 1).setPlayer(scriptEntry.getPlayer());
                            bracesSection.get(bracesSection.size() - 1).setNPC(scriptEntry.getNPC());
                            if (hyperdebug) dB.echoDebug("Command added: " + cmd + ", with " + String.valueOf(args.length) + " arguments");
                        } catch (ScriptEntryCreationException e) {
                            if (hyperdebug) dB.echoError(e.getMessage());
                        }
                    }
                    bracedSections.put(bracesName, bracesSection);
                    bracesName = "";
                }
            }

            // Finish building a command
            else if (newCommand && bracesEntered == 1) {
                commandList.put(commandList.size(), new ArrayList<String>());
                commandList.get(commandList.lastKey()).add(arg.raw_value);
                newCommand = false;
                if (hyperdebug) dB.echoDebug("Treating as new command");
            }

            // Start building a command
            else if (arg.matches("-") && bracesEntered == 1) {
                newCommand = true;
                if (hyperdebug) dB.echoDebug("Assuming following is a new command");
            }
            
            // Add to the name of the braced command list
            else if (bracesEntered == 0) {
                bracesName += arg.raw_value;
            }

            // Continue building the current command
            else {
                newCommand = false;
                commandList.get(commandList.lastKey()).add(arg.raw_value);
                if (hyperdebug) dB.echoDebug("Adding to the command");
            }
        }

        scriptEntry.getResidingQueue().removeEntry(0);
        return bracedSections;
        
    }

}
