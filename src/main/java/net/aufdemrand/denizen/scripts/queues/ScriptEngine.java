package net.aufdemrand.denizen.scripts.queues;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.CommandExecuter;
import net.aufdemrand.denizen.scripts.queues.core.Delayable;
import net.aufdemrand.denizen.scripts.requirements.RequirementChecker;
import net.aufdemrand.denizen.utilities.debugging.dB;

public class ScriptEngine {


    final private Denizen denizen;
    final private RequirementChecker requirementChecker;
    final private CommandExecuter commandExecuter;


    public ScriptEngine(Denizen denizenPlugin) {
        denizen  = denizenPlugin;
        // Create Denizen CommandExecuter and RequirementChecker
        commandExecuter = new CommandExecuter(denizen);
        requirementChecker = new RequirementChecker(denizen);
    }


    public void revolve(ScriptQueue scriptQueue) {
        // Check last ScriptEntry to see if it should be waited for
        if (scriptQueue.getLastEntryExecuted() != null
                && scriptQueue.getLastEntryExecuted().shouldWaitFor()) {
            if (!(scriptQueue instanceof Delayable)) {
                dB.echoError("Cannot wait for an instant command!");
            }
            else {
                return;
            }
        }

        // Okay to run next scriptEntry
        ScriptEntry scriptEntry = scriptQueue.getNext();

        // Loop through the entries
        while (scriptEntry != null) {
            // Mark script entry with Queue that is sending it to the executor
            scriptEntry.setSendingQueue(scriptQueue);

            try {
                // Execute the scriptEntry
                getScriptExecuter().execute(scriptEntry);
            }
            // Absolutely NO errors beyond this point!
            catch (Throwable e) {
                dB.echoError("Woah! An exception has been called with this command!");
                dB.echoError(e);
            }
            // Set as last entry executed
            scriptQueue.setLastEntryExecuted(scriptEntry);

            // Check if the scriptQueue is delayed (EG, via wait)
            if (scriptQueue instanceof Delayable) {
                if (((Delayable) scriptQueue).isDelayed())
                    break;
            }

            // If the entry is instant, and not injected, get the next Entry
            if (scriptEntry.isInstant()) {
                // Remove from execution list
                scriptEntry = scriptQueue.getNext();
            }

            // If entry isn't instant, end the revolutions and wait
            else
                break;
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
