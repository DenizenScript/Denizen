package net.aufdemrand.denizen.scripts.commands.npc;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.citizensnpcs.api.ai.flocking.*;

import java.util.Arrays;
import java.util.List;

/**
 * Handles NPC walking with the Citizens API.
 *
 * @author Jeremy Schroeder
 */
public class WalkCommand extends AbstractCommand {

    //                        percentage
    // walk [location] (speed:#.#) (auto_range)
    //

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Interpret arguments

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentType(dLocation.class))
                scriptEntry.addObject("location", arg.asType(dLocation.class));

            else if (!scriptEntry.hasObject("speed")
                    && arg.matchesPrimitive(aH.PrimitiveType.Percentage)
                    && arg.matchesPrefix("s, speed"))
                scriptEntry.addObject("speed", arg.asElement());

            else if (!scriptEntry.hasObject("auto_range")
                    && arg.matches("auto_range"))
                scriptEntry.addObject("auto_range", Element.TRUE);

            else if (!scriptEntry.hasObject("radius")
                    && arg.matchesPrimitive(aH.PrimitiveType.Double)
                    && arg.matchesPrefix("radius"))
                scriptEntry.addObject("radius", arg.asElement());

            else if (!scriptEntry.hasObject("npcs")
                    && arg.matchesArgumentList(dNPC.class))
                scriptEntry.addObject("npcs", arg.asType(dList.class).filter(dNPC.class));

            else
                arg.reportUnhandled();
        }


        // Check for required information

        if (!scriptEntry.hasObject("location"))
            throw new InvalidArgumentsException("Must specify a location!");

        if (!scriptEntry.hasObject("npcs")) {
            if (scriptEntry.getNPC() == null
                    || !scriptEntry.getNPC().isValid()
                    || !scriptEntry.getNPC().isSpawned())
                throw new InvalidArgumentsException("Must have a valid spawned NPC attached.");
            else
                scriptEntry.addObject("npcs", Arrays.asList(scriptEntry.getNPC()));
        }


    }


    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        // Fetch required objects

        dLocation loc = (dLocation) scriptEntry.getObject("location");
        Element speed = scriptEntry.getElement("speed");
        Element auto_range = scriptEntry.getElement("auto_range");
        Element radius = scriptEntry.getElement("radius");
        List<dNPC> npcs = (List<dNPC>) scriptEntry.getObject("npcs");


        // Debug the execution

        dB.report(scriptEntry, getName(), loc.debug()
                + (speed != null ? speed.debug() : "")
                + (auto_range != null ? auto_range.debug() : "")
                + (radius != null ? radius.debug(): "")
                + (aH.debugObj("npcs", npcs)));

        // Do the execution

        for (dNPC npc: npcs) {
            if (auto_range != null
                    && auto_range == Element.TRUE) {
                double distance = npc.getLocation().distance(loc);
                if (npc.getNavigator().getLocalParameters().range() < distance)
                    npc.getNavigator().getDefaultParameters().range((float) distance + 10);
            }

            npc.getNavigator().setTarget(loc);

            if (speed != null)
                npc.getNavigator().getLocalParameters().speedModifier(speed.asFloat());

            if (radius != null) {
                NPCFlock flock = new RadiusNPCFlock(radius.asDouble());
                Flocker flocker = new Flocker(npc.getCitizen(), flock, new SeparationBehavior(Flocker.LOW_INFLUENCE),
                        new CohesionBehavior(Flocker.LOW_INFLUENCE), new AlignmentBehavior(Flocker.HIGH_INFLUENCE));
                npc.getNavigator().getLocalParameters().addRunCallback(flocker);
            }
        }

    }


}
