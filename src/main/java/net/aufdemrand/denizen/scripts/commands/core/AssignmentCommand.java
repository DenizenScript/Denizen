package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.npc.traits.AssignmentTrait;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;

/**
 * <p>Announces a message to the server.</p>
 * 
 * <b>dScript Usage:</b><br>
 * <pre>ANNOUNCE ['message to announce']</pre>
 * 
 * <ol><tt>Arguments: [] - Required</ol></tt>
 * 
 * <ol><tt>['message to announce']</tt><br> 
 *         The message to send to the server. This will be seen by all Players.</ol>
 * 
 * 
 * <br><b>Example Usage:</b><br>
 * <ol><tt>
 *  - ANNOUNCE 'Today is Christmas!' <br>
 *  - ANNOUNCE "&#60;PLAYER.NAME> has completed '&#60;FLAG.P:currentQuest>'!" <br>
 *  - ANNOUNCE "&#60;GOLD>$$$ &#60;WHITE>- Make some quick cash at our &#60;RED>MINEA-SINO&#60;WHITE>!" 
 * </ol></tt>
 *
 * @author Jeremy Schroeder
 * 
 */
public class AssignmentCommand extends AbstractCommand {

    private enum Action { SET, REMOVE }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

    	// Initialize fields
    	String script = null;
        Action action = Action.SET;

        // Parse arguments
        for (String arg : scriptEntry.getArguments()) {
            if (aH.matchesScript(arg))
               script = aH.getStringFrom(arg);
           else if (aH.matchesArg("SET, REMOVE", arg))
                action = Action.valueOf(arg.toUpperCase());
            else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
        }

        // If 'SET'ting and no 'script', throw an error.
        if (script == null && action == Action.SET)
            throw new InvalidArgumentsException(Messages.ERROR_MISSING_OTHER, "SCRIPT");
        if (scriptEntry.getNPC() == null)
            throw new InvalidArgumentsException(Messages.ERROR_NO_NPCID);

        // Add objects that need to be passed to execute() to the scriptEntry
        scriptEntry.addObject("script", script);
        scriptEntry.addObject("action", action);
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        Action action = (Action) scriptEntry.getObject("action");
        String script = (String) scriptEntry.getObject("script");

        dB.echoDebug("<G>Executing '<Y>" + getName() + "<G>': "
                + "Action='<Y>" + action.toString() + "<G>', "
                + (script != null ? "Script='<Y>" + script + "<G>', " : "")
                + "NPC='<Y>" + scriptEntry.getNPC() + "<G>'");

        if (action == Action.SET)
            scriptEntry.getNPC().getCitizen().getTrait(AssignmentTrait.class)
                .setAssignment(script, scriptEntry.getPlayer());

        else if (action == Action.REMOVE)
            scriptEntry.getNPC().getCitizen().getTrait(AssignmentTrait.class)
                    .removeAssignment(scriptEntry.getPlayer());
        }
}