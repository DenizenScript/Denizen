package net.aufdemrand.denizen.scripts.commands.npc;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.npc.traits.AssignmentTrait;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dScript;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;

public class AssignmentCommand extends AbstractCommand {

    private enum Action {SET, REMOVE}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Parse Arguments
        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (arg.matchesEnum(Action.values())
                    && !scriptEntry.hasObject("action")) {
                scriptEntry.addObject("action", Action.valueOf(arg.getValue().toUpperCase()));
            }


            else if (arg.matchesArgumentType(dScript.class)
                    && !scriptEntry.hasObject("script")) {
                // Check the type of script.. it must be an assignment-type container
                if (arg.asType(dScript.class) != null
                        && arg.asType(dScript.class).getType().equalsIgnoreCase("assignment")) {
                    scriptEntry.addObject("script", arg.asType(dScript.class));
                }
                else {
                    throw new InvalidArgumentsException("Script specified is not an 'assignment-type' container.");
                }
            }


            else {
                arg.reportUnhandled();
            }
        }

        // Check required arguments
        if (!((BukkitScriptEntryData) scriptEntry.entryData).hasNPC()) {
            throw new InvalidArgumentsException("NPC linked was missing or invalid.");
        }

        if (!scriptEntry.hasObject("action")) {
            throw new InvalidArgumentsException("Must specify an action!");
        }

        if (scriptEntry.getObject("action").equals(Action.SET) && !scriptEntry.hasObject("script")) {
            throw new InvalidArgumentsException("Script specified was missing or invalid.");
        }

    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        dScript script = scriptEntry.getdObject("script");

        // Report to dB
        dB.report(scriptEntry, getName(), aH.debugObj("action", scriptEntry.getObject("action")) + (script != null ? script.debug() : ""));

        // Perform desired action
        if (scriptEntry.getObject("action").equals(Action.SET)) {
            ((BukkitScriptEntryData) scriptEntry.entryData).getNPC().getCitizen().getTrait(AssignmentTrait.class)
                    .setAssignment(script.getName(), ((BukkitScriptEntryData) scriptEntry.entryData).getPlayer());
        }

        else if (scriptEntry.getObject("action").equals(Action.REMOVE)) {
            ((BukkitScriptEntryData) scriptEntry.entryData).getNPC().getCitizen().getTrait(AssignmentTrait.class)
                    .removeAssignment(((BukkitScriptEntryData) scriptEntry.entryData).getPlayer());
        }
    }
}
