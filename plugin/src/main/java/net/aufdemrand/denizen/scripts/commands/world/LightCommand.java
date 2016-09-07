package net.aufdemrand.denizen.scripts.commands.world;

import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.utilities.blocks.BlockLight;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Duration;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;

public class LightCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentType(dLocation.class)) {
                scriptEntry.addObject("location", arg.asType(dLocation.class));
            }

            else if (!scriptEntry.hasObject("light")
                    && arg.matchesPrimitive(aH.PrimitiveType.Integer)) {
                scriptEntry.addObject("light", arg.asElement());
            }

            else if (!scriptEntry.hasObject("reset")
                    && arg.matches("reset")) {
                scriptEntry.addObject("reset", new Element(true));
            }

            else if (!scriptEntry.hasObject("duration")
                    && arg.matchesPrefix("d", "duration")
                    && arg.matchesArgumentType(Duration.class)) {
                scriptEntry.addObject("duration", arg.asType(Duration.class));
            }

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
        Duration duration = scriptEntry.getdObject("duration");

        dB.report(scriptEntry, getName(), location.debug() + reset.debug()
                + (light != null ? light.debug() : "") + (duration != null ? duration.debug() : ""));

        if (location.getY() < 0 || location.getY() > 255) {
            dB.echoError(scriptEntry.getResidingQueue(), "Invalid light location!");
            return;
        }
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                location.clone().add(x * 16, 0, z * 16).getChunk().load();
            }
        }
        if (!reset.asBoolean()) {
            int brightness = light.asInt();
            if (brightness < 0 || brightness > 15) {
                throw new CommandExecutionException("Light brightness must be between 0 and 15, inclusive!");
            }
            BlockLight.createLight(location, brightness, duration);
        }
        else {
            BlockLight.removeLight(location);
        }
    }
}
