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

/**
 *
 * TODO: Document usage
 *
 * Controls a NPC's 'Anchors' trait.
 *
 * @author aufdemrand
 *
 */
public class AnchorCommand extends AbstractCommand {

    private enum Action { ADD, REMOVE, ASSUME, WALKTO, WALKNEAR }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        Action action = Action.ADD;
        Location location = null;
        String id = null;
        Integer range = null;

        // Parse Arguments
        for (String arg : scriptEntry.getArguments()) {
            if (aH.matchesArg("ADD, WALKTO, WALKNEAR, ASSUME, REMOVE", arg)) {
                action = Action.valueOf(aH.getStringFrom(arg).toUpperCase());

            } else if (aH.matchesValueArg("ID", arg, aH.ArgumentType.String)) {
                id = aH.getStringFrom(arg);

            } else if (aH.matchesLocation(arg)) {
                location = aH.getLocationFrom(arg);

            } else if (aH.matchesValueArg("RANGE", arg, aH.ArgumentType.Integer)) {
                range = aH.getIntegerFrom(arg);

            } else throw new InvalidArgumentsException(dB.Messages.ERROR_UNKNOWN_ARGUMENT);

        }

        scriptEntry.addObject("action", action)
                .addObject("id", id)
                .addObject("range", range)
                .addObject("location", location);

    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        // Get objects
        Action action = (Action) scriptEntry.getObject("action");
        Location location = (Location) scriptEntry.getObject("location");
        Integer range = (Integer) scriptEntry.getObject("range");
        String id = (String) scriptEntry.getObject("id");

        // Report to dB
        dB.report(getName(),
                aH.debugObj("NPC", scriptEntry.getNPC().toString())
                        + aH.debugObj("Action", action.toString())
                        + aH.debugObj("Id", id)
                        + (location != null ? location.debug() : "")
                        + (range != null ? aH.debugObj("Range", range.toString()) : "" ));

        dNPC npc = scriptEntry.getNPC();

        switch (action) {

            case ADD:
                npc.getCitizen().getTrait(Anchors.class).addAnchor(id, location);
                return;

            case ASSUME:
                npc.getEntity().teleport(npc.getCitizen().getTrait(Anchors.class)
                        .getAnchor(id).getLocation());
                return;

            case WALKNEAR:
                npc.getNavigator().setTarget(Utilities
                        .getWalkableLocationNear(npc.getCitizen().getTrait(Anchors.class)
                                .getAnchor(id).getLocation(), range));
                return;

            case WALKTO:
                npc.getNavigator().setTarget(npc.getCitizen().getTrait(Anchors.class)
                                .getAnchor(id).getLocation());
                return;

            case REMOVE:
                npc.getCitizen().getTrait(Anchors.class)
                        .removeAnchor(npc.getCitizen().getTrait(Anchors.class)
                                .getAnchor(id));
                return;
        }

    }
}