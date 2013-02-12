package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.npc.traits.AssignmentTrait;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.arguments.Script;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;

/**
 *
 * @author Jeremy Schroeder
 *
 */
public class AssignmentCommand extends AbstractCommand {

    private enum Action {SET, REMOVE}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        // Initialize fields
        Script script = null;
        Action action = Action.SET;

        // Parse arguments
        for (String arg : scriptEntry.getArguments()) {

            // If script argument, check the type -- must be 'assignment'
            if (aH.matchesScript(arg)) {
                script = aH.getScriptFrom(arg);
                if (script != null && !script.getType().equalsIgnoreCase("assignment")) {
                    dB.echoError("Script type must be 'ASSIGNMENT'. Script specified is '%s'.", script.getType());
                    script = null;
                }
            }

            // Get desired action
            else if (aH.matchesArg("SET, REMOVE", arg))
                action = Action.valueOf(arg.toUpperCase());

            else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
        }

        // If 'SET'ting and no 'script', throw an error.
        if (action == Action.SET && script == null)
            throw new InvalidArgumentsException(Messages.ERROR_MISSING_OTHER, "SCRIPT");
        // If no NPC attached, throw an error
        if (scriptEntry.getNPC() == null)
            throw new InvalidArgumentsException(Messages.ERROR_NO_NPCID);

        // Add objects that need to be passed to execute() to the scriptEntry
        scriptEntry.addObject("script", script);
        scriptEntry.addObject("action", action);
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        // Fetch objects
        Action action = (Action) scriptEntry.getObject("action");
        Script script = (Script) scriptEntry.getObject("script");

        // Report to dB
        dB.report(getName(),
                aH.debugObj("Action", action.toString())
                        + (script != null ? script.debug() : "")
                        + aH.debugObj("NPC", scriptEntry.getNPC().getName() + "/" + scriptEntry.getNPC().getId()));

        // Perform desired action
        if (action == Action.SET)
            scriptEntry.getNPC().getCitizen().getTrait(AssignmentTrait.class)
                    .setAssignment(script.getName(), scriptEntry.getPlayer());

        else if (action == Action.REMOVE)
            scriptEntry.getNPC().getCitizen().getTrait(AssignmentTrait.class)
                    .removeAssignment(scriptEntry.getPlayer());
    }

}