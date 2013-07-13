package net.aufdemrand.denizen.scripts.commands.npc;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.dB;

/**
 * Handles NPC walking with the Citizens API.
 *
 * @author Jeremy Schroeder
 */
public class WalkCommand extends AbstractCommand {

    //                        percentage
    // walk [location] (speed:#.#) (auto_range)

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

        }


        // Check for required information

        if (!scriptEntry.hasObject("location"))
            throw new InvalidArgumentsException("Must specify a location!");
        if (scriptEntry.getNPC() == null
                || !scriptEntry.getNPC().isValid()
                || !scriptEntry.getNPC().isSpawned())
            throw new InvalidArgumentsException("Must have a valid spawned NPC attached.");

    }


    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        // Fetch required objects

        dLocation loc = (dLocation) scriptEntry.getObject("location");
        Element speed = (Element) scriptEntry.getObject("speed");
        Element auto_range = (Element) scriptEntry.getObject("auto_range");


        // Debug the execution

        dB.report(getName(), loc.debug()
                + (speed != null ? speed.debug() : "")
                + (auto_range != null ? auto_range.debug() : ""));

        // Do the execution

        if (auto_range != null
                && auto_range == Element.TRUE) {
            double distance = scriptEntry.getNPC().getLocation().distance(loc);
            if (scriptEntry.getNPC().getNavigator().getLocalParameters().range() < distance)
                scriptEntry.getNPC().getNavigator().getDefaultParameters().range((float) distance + 10);
        }

        scriptEntry.getNPC().getNavigator().setTarget(loc);

        if (speed != null)
            scriptEntry.getNPC().getNavigator().getLocalParameters().speedModifier(speed.asFloat());

    }


}