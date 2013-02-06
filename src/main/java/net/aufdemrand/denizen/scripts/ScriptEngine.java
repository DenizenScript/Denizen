package net.aufdemrand.denizen.scripts;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.scripts.commands.CommandExecuter;
import net.aufdemrand.denizen.scripts.requirements.RequirementChecker;
import net.aufdemrand.denizen.utilities.debugging.dB;

import java.util.Iterator;

public class Engine {

    final private Denizen denizen;

    final private RequirementChecker requirementChecker;
    final private CommandExecuter commandExecuter;

    public Engine(Denizen denizenPlugin) {
        denizen  = denizenPlugin;
        // Create Denizen Executer and RequirementChecker
        commandExecuter = new CommandExecuter(denizen);
        requirementChecker = new RequirementChecker(denizen);
    }

    public void revolve(Queue queue) {
        Iterator<ScriptEntry> scriptEntries = queue.scriptEntries.iterator();
        while (scriptEntries.hasNext()) {
            // Find next ScriptEntry
            ScriptEntry scriptEntry = scriptEntries.next();
            // Check run time
            if (scriptEntry.getRunTime() < System.currentTimeMillis()) {
                // Mark script entry with Queue that is sending it to the executer
                scriptEntry.setSendingQueue(queue);
                // Execute the scriptEntry
                try {
                    getScriptExecuter().execute(scriptEntry);
                } catch (Throwable e) {
                    dB.echoError("Woah! An exception has been called with this command!");
                    if (!dB.showStackTraces)
                        dB.echoError("Enable '/denizen stacktrace' for the nitty-gritty.");
                    else e.printStackTrace();
                }
                // Set as last entry exectured
                queue.setLastEntryExecuted(scriptEntry);
                // Remove from execution list
                scriptEntries.remove();
                if (scriptEntry.isInstant() || queue.delay > 0) continue;
                // If entry isn't instant, end the revolution and wait for another
                else break;
            }
        }
    }

    /**
     * Gets the currently loaded instance of the RequirementChecker
     *
     * @return  ScriptHelper
     *
     */
    public RequirementChecker getRequirementChecker() {
        return requirementChecker;
    }

    /**
     * Gets the currently loaded instance of the Command Executer
     *
     * @return  CommandExecuter
     *
     */
    public CommandExecuter getScriptExecuter() {
        return commandExecuter;
    }





}
