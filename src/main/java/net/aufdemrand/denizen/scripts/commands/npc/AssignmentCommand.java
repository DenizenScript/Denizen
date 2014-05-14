package net.aufdemrand.denizen.scripts.commands.npc;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.npc.traits.AssignmentTrait;
import net.aufdemrand.denizen.objects.dScript;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;

/**
 * Controls a NPC's 'Assignment' trait.
 *
 * @author Jeremy Schroeder
 *
 */

// TODO: Fully update command to 0.9+ style
public class AssignmentCommand extends AbstractCommand {

    private enum Action { SET, REMOVE }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Parse Arguments
        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (arg.matchesEnum(Action.values())
                    && !scriptEntry.hasObject("action"))
                scriptEntry.addObject("action", Action.valueOf(arg.getValue().toUpperCase()));


            else if (arg.matchesArgumentType(dScript.class)
                    && !scriptEntry.hasObject("script")) {
                // Check the type of script.. it must be an assignment-type container
                if (arg.asType(dScript.class) != null
                        && arg.asType(dScript.class).getType().equalsIgnoreCase("assignment"))
                    scriptEntry.addObject("script", arg.asType(dScript.class));
                else
                    throw new InvalidArgumentsException("Script specified is not an 'assignment-type' container.");
            }


            else arg.reportUnhandled();
        }

        // Check required arguments
        if (!scriptEntry.hasNPC())
            throw new InvalidArgumentsException("NPC linked was missing or invalid.");

        if (!scriptEntry.hasObject("action"))
            throw new InvalidArgumentsException("Must specify an action!");

        if (scriptEntry.getObject("action").equals(Action.SET) && !scriptEntry.hasObject("script"))
            throw new InvalidArgumentsException("Script specified was missing or invalid.");

    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        // Report to dB
        dB.report(scriptEntry, getName(), scriptEntry.getNPC().debug()
                + scriptEntry.reportObject("action")
                + scriptEntry.reportObject("script"));

        // Perform desired action
        if (scriptEntry.getObject("action").equals(Action.SET))
            scriptEntry.getNPC().getCitizen().getTrait(AssignmentTrait.class)
                    .setAssignment(scriptEntry.getdObjectAs("script", dScript.class).getName(),
                            scriptEntry.getPlayer());

        else if (scriptEntry.getObject("action").equals(Action.REMOVE))
            scriptEntry.getNPC().getCitizen().getTrait(AssignmentTrait.class)
                    .removeAssignment(scriptEntry.getPlayer());
    }

}
