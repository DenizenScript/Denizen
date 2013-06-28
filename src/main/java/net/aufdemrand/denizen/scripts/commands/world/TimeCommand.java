package net.aufdemrand.denizen.scripts.commands.world;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.objects.Duration;
import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;

/**
 *
 * Set the time in the world to a number of ticks.
 *
 */
public class TimeCommand extends AbstractCommand {

    private enum Type { GLOBAL, PLAYER }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("type")
                    && arg.matchesEnum(Type.values()))
                // add type
                scriptEntry.addObject("type", arg.asElement());


            else if (!scriptEntry.hasObject("value")
                    && arg.matchesArgumentType(Duration.class))
                // add value
                scriptEntry.addObject("value", arg.asType(Duration.class));
        }

        // Check to make sure required arguments have been filled

        if ((!scriptEntry.hasObject("value")))
            throw new InvalidArgumentsException(Messages.ERROR_MISSING_OTHER, "VALUE");
    }
    
    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        // Fetch objects
        Duration value = (Duration) scriptEntry.getObject("value");
        Element type = (scriptEntry.hasObject("type") ?
                (Element) scriptEntry.getObject("type") : new Element("player"));

        // Report to dB
        dB.report(getName(), type.debug()
                + (type.toString().equalsIgnoreCase("player") ? scriptEntry.getPlayer().debug() : "")
                + value.debug());

        scriptEntry.getPlayer().getPlayerEntity().getWorld().setTime(value.getTicks());
    }
    
}
