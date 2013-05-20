package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.arguments.Element;
import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.npc.dNPC;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.arguments.dLocation;
import net.aufdemrand.denizen.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.citizensnpcs.trait.Anchors;

import static net.aufdemrand.denizen.arguments.aH.Argument;

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

        // Parse Arguments
        for (Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("action")
                    && arg.matchesEnum(Action.values()))
                // add Action
                scriptEntry.addObject("action", arg.asElement().setPrefix("action"));


            else if (!scriptEntry.hasObject("range")
                    && arg.matchesPrimitive(aH.PrimitiveType.Double)
                    && arg.matchesPrefix("range, r"))
                // add range (for WALKNEAR)
                scriptEntry.addObject("range", arg.asElement().setPrefix("range"));


            else if (!scriptEntry.hasObject("id")
                    && arg.matchesPrefix("id, i"))
                // add anchor ID
                scriptEntry.addObject("id", arg.asElement().setPrefix("id"));


            else if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentType(dLocation.class))
                // add location (for ADD)
                scriptEntry.addObject("location", arg.asType(dLocation.class).setPrefix("location"));


            else throw new InvalidArgumentsException(dB.Messages.ERROR_UNKNOWN_ARGUMENT);
        }


        if (!scriptEntry.hasObject("action"))
            throw new InvalidArgumentsException("Must specify an 'Anchor Action'.");

    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        // Get objects
        dLocation location = (dLocation) scriptEntry.getObject("location");
        Element action = (Element) scriptEntry.getObject("action");
        Element range = (Element) scriptEntry.getObject("range");
        Element id = (Element) scriptEntry.getObject("id");

        // Report to dB
        dB.report(getName(),
                aH.debugObj("NPC", scriptEntry.getNPC().toString())
                        + action.debug() + id.debug()
                        + (location != null ? location.debug() : "")
                        + (range != null ? range.debug() : "" ));

        dNPC npc = scriptEntry.getNPC();
        Action action_ = Action.valueOf(action.toString().replace("_", "").toUpperCase());

        switch (action_) {

            case ADD:
                npc.getCitizen().getTrait(Anchors.class).addAnchor(id.asString(), location);
                return;

            case ASSUME:
                npc.getEntity().teleport(npc.getCitizen().getTrait(Anchors.class)
                        .getAnchor(id.asString()).getLocation());
                return;

            case WALKNEAR:
                npc.getNavigator().setTarget(Utilities
                        .getWalkableLocationNear(npc.getCitizen().getTrait(Anchors.class)
                                .getAnchor(id.asString()).getLocation(), range.asInt()));
                return;

            case WALKTO:
                npc.getNavigator().setTarget(npc.getCitizen().getTrait(Anchors.class)
                        .getAnchor(id.asString()).getLocation());
                return;

            case REMOVE:
                npc.getCitizen().getTrait(Anchors.class)
                        .removeAnchor(npc.getCitizen().getTrait(Anchors.class)
                                .getAnchor(id.asString()));
                return;
        }

    }
}