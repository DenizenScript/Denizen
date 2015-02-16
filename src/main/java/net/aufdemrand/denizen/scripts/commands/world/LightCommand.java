package net.aufdemrand.denizen.scripts.commands.world;

import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.utilities.blocks.BlockLight;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;

public class LightCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentType(dLocation.class))
                scriptEntry.addObject("location", arg.asType(dLocation.class));

            else if (!scriptEntry.hasObject("light")
                    && arg.matchesPrimitive(aH.PrimitiveType.Integer))
                scriptEntry.addObject("light", arg.asElement());

            else if (!scriptEntry.hasObject("reset")
                    && arg.matches("reset"))
                scriptEntry.addObject("reset", new Element(true));

        }

        if (!scriptEntry.hasObject("location") ||
                (!scriptEntry.hasObject("light") && !scriptEntry.hasObject("reset"))) {
            throw new InvalidArgumentsException("Must specify a valid location and light level.");
        }

        scriptEntry.defaultObject("reset", new Element(false));
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        dLocation location = scriptEntry.getdObject("location");
        Element light = scriptEntry.getElement("light");
        Element reset = scriptEntry.getElement("reset");

        dB.report(scriptEntry, getName(), location.debug() + (light != null ? light.debug() : "") + reset.debug());

        if (!reset.asBoolean())
            BlockLight.createLight(location, light.asInt());
        else
            BlockLight.removeLight(location);
    }
}
