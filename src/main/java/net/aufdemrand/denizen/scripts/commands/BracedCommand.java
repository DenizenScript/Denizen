package net.aufdemrand.denizen.scripts.commands;

import java.util.ArrayList;
import java.util.TreeMap;

import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.exceptions.ScriptEntryCreationException;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.utilities.debugging.dB;

public abstract class BracedCommand extends AbstractCommand {
    
    public final boolean hyperdebug = false;

    public ArrayList<ScriptEntry> getBracedCommands(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        return getBracedCommands(scriptEntry, 0);
    }

    /**
     * Gets the commands inside the braces of this ScriptEntry.
     * 
     * @param scriptEntry
     *          The ScriptEntry to get the braced commands from.
     *          
     * @param extraEntries
     *          (Currently serves no purpose.) The main intention is to allow commands to
     *          listen for braced commands in ScriptEntries after this one.
     *          
     * @return
     *          The list of ScriptEntries to be executed in the command.
     *          
     * @throws InvalidArgumentsException
     */
    public ArrayList<ScriptEntry> getBracedCommands(ScriptEntry scriptEntry, int extraEntries) throws InvalidArgumentsException {
        
        // We need a place to store the commands being built at...
        TreeMap<Integer, ArrayList<String>> commandList = new TreeMap<Integer, ArrayList<String>>();
        // And a place to store finished commands...
        ArrayList<ScriptEntry> commands = new ArrayList<ScriptEntry>();
        
        int bracesEntered = 0;
        boolean newCommand = false;

        // Inject the scriptEntry into the front of the queue, otherwise it doesn't exist
        scriptEntry.getResidingQueue().injectEntry(scriptEntry, 0);
        // Send info to debug
        if (hyperdebug) dB.echoDebug("Starting getBracedCommands...");
        
        // If the specified amount of possible entries is less than the queue size, print that instead
        if (hyperdebug) dB.echoDebug("...with queue size: " + 
        (extraEntries < scriptEntry.getResidingQueue().getQueueSize() 
                ? extraEntries + 1 
                : scriptEntry.getResidingQueue().getQueueSize()));
        
        if (hyperdebug) dB.echoDebug("...with first command name: " + scriptEntry.getCommandName());
        if (hyperdebug) dB.echoDebug("...with first command arguments: " + scriptEntry.getArguments());
        
        // Now, loop through the entries
        for (int x = 0; x < (extraEntries > 0 ? extraEntries + 1 : 1); x++) {
            ScriptEntry entry = scriptEntry.getResidingQueue().getEntry(0);
            if (hyperdebug) dB.echoDebug("Entry found: " + entry.getCommandName());
            
            // Loop through the arguments of each entry
            for (aH.Argument arg : aH.interpret(entry.getArguments())) {
                if (hyperdebug) dB.echoDebug("Arg found: " + arg.raw_value);
                
                // Listen for opened braces
                if (arg.matches("{")) {
                    bracesEntered++;
                    if (hyperdebug) dB.echoDebug("Opened brace; " + bracesEntered + " now");
                    if (bracesEntered > 1) {
                        commandList.get(commandList.lastKey()).add(arg.raw_value);
                    }
                }
                
                // Listen for closed braces
                else if (arg.matches("}")) {
                    bracesEntered--;
                    if (hyperdebug) dB.echoDebug("Closed brace; " + bracesEntered + " now");
                    if (bracesEntered > 0) {
                        commandList.get(commandList.lastKey()).add(arg.raw_value);
                    }
                }
                
                // Finish building a command
                else if (newCommand && bracesEntered == 1) {
                    commandList.put(commandList.size(), new ArrayList<String>());
                    commandList.get(commandList.lastKey()).add(arg.raw_value);
                    newCommand = false;
                    if (hyperdebug) dB.echoDebug("Treating as new command");
                }
                
                // Nothing to do here...
                else if (bracesEntered == 0) {
                    if (hyperdebug) dB.echoDebug("Ignoring");
                }
                
                // Start building a command
                else if (arg.matches("-") && bracesEntered == 1) {
                    newCommand = true;
                    if (hyperdebug) dB.echoDebug("Assuming following is a new command");
                }
                
                // Continue building the current command
                else {
                    commandList.get(commandList.lastKey()).add(arg.raw_value);
                    if (hyperdebug) dB.echoDebug("Adding to the command");
                }
            }
            
            // Add all the new commands to the final list
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
            
            // Remove the commands from the current queue, so there's no YAML errors
            scriptEntry.getResidingQueue().removeEntry(0);
        }
        
        // Return the list of commands in the braces
        return commands;
    }

}