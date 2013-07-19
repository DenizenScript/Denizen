package net.aufdemrand.denizen.scripts;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.scripts.commands.CommandExecuter;
import net.aufdemrand.denizen.scripts.requirements.RequirementChecker;
import net.aufdemrand.denizen.utilities.debugging.dB;

public class ScriptEngine {

    final private Denizen denizen;

    final private RequirementChecker requirementChecker;
    final private CommandExecuter commandExecuter;

    public ScriptEngine(Denizen denizenPlugin) {
        denizen  = denizenPlugin;
        // Create Denizen Executer and RequirementChecker
        commandExecuter = new CommandExecuter(denizen);
        requirementChecker = new RequirementChecker(denizen);
    }

    public void revolve(ScriptQueue scriptQueue) {
        // Check last ScriptEntry to see if it should be waited for
        if (scriptQueue.getLastEntryExecuted() != null
                && scriptQueue.getLastEntryExecuted().shouldWaitFor())
            if (!scriptQueue.getLastEntryExecuted().isDone()) return;

        ScriptEntry scriptEntry = scriptQueue.getNext();
        while (scriptEntry != null) {
            // Find next ScriptEntry
            scriptEntry.setSendingQueue(scriptQueue);
            // Check if last entry is still holding up the queue
            if (scriptQueue.getLastEntryExecuted() != null
                    && scriptQueue.getLastEntryExecuted().getHoldTime() > System.currentTimeMillis()) {
                break;
            }
            // Check allowed run-time of next ScriptEntry
            if (scriptEntry.getRunTime() < System.currentTimeMillis()) {
                // Mark script entry with Queue that is sending it to the executer
                scriptEntry.setSendingQueue(scriptQueue);
                // Execute the scriptEntry
                try {
                    getScriptExecuter().execute(scriptEntry);
                } catch (Throwable e) {
                    dB.echoError("Woah! An exception has been called with this command!");
                    if (!dB.showStackTraces)
                        dB.echoError("Enable '/denizen stacktrace' for the nitty-gritty.");
                    else e.printStackTrace();
                }
                // Set as last entry executed
                scriptQueue.setLastEntryExecuted(scriptEntry);

                if (scriptEntry.isInstant() || scriptQueue.ticks == 0 && !scriptQueue.hasInjectedItems) {
                    // Remove from execution list
                    scriptEntry = scriptQueue.getNext();
                }
                else if (scriptQueue.hasInjectedItems) {
                    scriptQueue.hasInjectedItems = false;
                    break;
                }
                // If entry isn't instant, end the revolution and wait for another
                else
                    break;
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
