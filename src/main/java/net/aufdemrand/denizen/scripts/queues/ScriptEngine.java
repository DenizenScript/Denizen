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

        // Mark script entry with Queue that is sending it to the executer
        scriptEntry.setSendingQueue(scriptQueue);

        // Execute the scriptEntry
        try {
            getScriptExecuter().execute(scriptEntry);
        }
        catch (Throwable e) {
            dB.echoError("Woah! An exception has been called with this command!");
            dB.echoError(e);
        }
        // Set as last entry executed
        scriptQueue.setLastEntryExecuted(scriptEntry);
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
