package net.aufdemrand.denizen.scripts.commands.world;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;

public class ExplodeCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Iterate through arguments
        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentType(dLocation.class)) {

                scriptEntry.addObject("location", arg.asType(dLocation.class));
            }

            else if (!scriptEntry.hasObject("power")
                    && arg.matchesPrimitive(aH.PrimitiveType.Float)
                    && arg.matchesPrefix("power", "p")) {

                scriptEntry.addObject("power", arg.asElement());
            }

            else if (!scriptEntry.hasObject("breakblocks")
                    && arg.matches("breakblocks")) {

                scriptEntry.addObject("breakblocks", "");
            }

            else if (!scriptEntry.hasObject("fire")
                    && arg.matches("fire")) {

                scriptEntry.addObject("fire", "");
            }

            else
                arg.reportUnhandled();
        }

        // Use default values if necessary
        scriptEntry.defaultObject("power", new Element(1.0));
        scriptEntry.defaultObject("location",
                ((BukkitScriptEntryData) scriptEntry.entryData).hasNPC() ? ((BukkitScriptEntryData) scriptEntry.entryData).getNPC().getLocation() : null,
                ((BukkitScriptEntryData) scriptEntry.entryData).hasPlayer() ? ((BukkitScriptEntryData) scriptEntry.entryData).getPlayer().getLocation() : null);

        if (!scriptEntry.hasObject("location")) {
            throw new InvalidArgumentsException("Missing location argument!");
        }
    }

    @Override
    public void execute(final ScriptEntry scriptEntry) throws CommandExecutionException {
        // Get objects

        final dLocation location = (dLocation) scriptEntry.getObject("location");
        Element power = (Element) scriptEntry.getObject("power");
        Boolean breakblocks = scriptEntry.hasObject("breakblocks");
        Boolean fire = scriptEntry.hasObject("fire");

        // Report to dB
        dB.report(scriptEntry, getName(),
                (aH.debugObj("location", location.toString()) +
                        aH.debugObj("power", power) +
                        aH.debugObj("breakblocks", breakblocks) +
                        aH.debugObj("fire", fire)));

        location.getWorld().createExplosion
                (location.getX(), location.getY(), location.getZ(),
                        power.asFloat(), fire, breakblocks);
    }
}
