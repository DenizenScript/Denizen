package net.aufdemrand.denizen.scripts.commands.world;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.objects.Duration;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.dWorld;
import net.aufdemrand.denizen.utilities.debugging.dB;

/**
 *
 * Set the time in the world to a number of ticks.
 *
 * @author David Cernat
 */
public class TimeCommand extends AbstractCommand {

    private enum Type { GLOBAL, PLAYER }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("type")
                && arg.matchesEnum(Type.values())) {

                scriptEntry.addObject("type", Type.valueOf(arg.getValue().toUpperCase()));
            }

            else if (!scriptEntry.hasObject("value")
                     && arg.matchesArgumentType(Duration.class)) {

                scriptEntry.addObject("value", arg.asType(Duration.class));
            }

            else if (!scriptEntry.hasObject("world")
                     && arg.matchesArgumentType(dWorld.class)) {

                scriptEntry.addObject("world", arg.asType(dWorld.class));
            }

            else arg.reportUnhandled();
        }

        // Check to make sure required arguments have been filled

        if ((!scriptEntry.hasObject("value")))
            throw new InvalidArgumentsException("Must specify a value!");

        // If the world has not been specified, try to use the NPC's or player's
        // world, or default to "world" if necessary

        scriptEntry.defaultObject("world",
                scriptEntry.hasNPC() ? new dWorld(scriptEntry.getNPC().getWorld()) : null,
                scriptEntry.hasPlayer() ? new dWorld(scriptEntry.getPlayer().getWorld()) : null,
                dWorld.valueOf("world"));
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        // Fetch objects
        Duration value = (Duration) scriptEntry.getObject("value");
        dWorld world = (dWorld) scriptEntry.getObject("world");
        Type type = scriptEntry.hasObject("type") ?
                (Type) scriptEntry.getObject("type") : Type.GLOBAL;

        // Report to dB
        dB.report(scriptEntry, getName(), aH.debugObj("type", type.name()) +
                (type.name().equalsIgnoreCase("player") ?
                        aH.debugObj("player", scriptEntry.getPlayer()) : "") +
                (type.name().equalsIgnoreCase("global") ?
                        aH.debugObj("world", world) : "") +
                aH.debugObj("value", value));

        if (type.equals(Type.GLOBAL)) {
            world.getWorld().setTime(value.getTicks());
        }
        else {
            scriptEntry.getPlayer().getPlayerEntity().setPlayerTime(value.getTicks(), true);
        }
    }

}
