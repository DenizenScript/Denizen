package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.npc.dNPC;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.utilities.arguments.Location;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.citizensnpcs.trait.Anchors;
import net.citizensnpcs.trait.Poses;
import net.citizensnpcs.util.Util;

/**
 *
 * TODO: Document usage
 *
 * Controls a NPC's 'Poses' trait.
 *
 * @author aufdemrand
 *
 */
public class PoseCommand extends AbstractCommand {

    private enum Action { ADD, REMOVE, ASSUME}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        Action action = Action.ASSUME;
        String id = null;

        // Parse Arguments
        for (String arg : scriptEntry.getArguments()) {
            if (aH.matchesArg("ADD, ASSUME, REMOVE", arg)) {
                action = Action.valueOf(aH.getStringFrom(arg).toUpperCase());

            } else if (aH.matchesValueArg("ID", arg, aH.ArgumentType.String)) {
                id = aH.getStringFrom(arg);

            } else throw new InvalidArgumentsException(dB.Messages.ERROR_UNKNOWN_ARGUMENT);

        }

        scriptEntry.addObject("action", action)
                .addObject("id", id);
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        // Get objects
        Action action = (Action) scriptEntry.getObject("action");
        String id = (String) scriptEntry.getObject("id");

        // Report to dB
        dB.report(getName(),
                aH.debugObj("NPC", scriptEntry.getNPC().toString())
                        + aH.debugObj("Action", action.toString())
                        + aH.debugObj("Id", id));

        dNPC npc = scriptEntry.getNPC();

        switch (action) {

            case ASSUME:
                npc.getCitizen().getTrait(Poses.class).assumePose(id);
                return;
        }

        // TODO: ADD ADD/REMOVE

    }
}