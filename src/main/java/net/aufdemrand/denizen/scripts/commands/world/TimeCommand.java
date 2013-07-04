package net.aufdemrand.denizen.scripts.commands.world;

import org.bukkit.Bukkit;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.objects.Duration;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.dWorld;
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
                    && arg.matchesEnum(Type.values())) {
                // add type
            	scriptEntry.addObject("type", Type.valueOf(arg.getValue().toUpperCase()));
            }

            else if (!scriptEntry.hasObject("value")
                    && arg.matchesArgumentType(Duration.class)) {
                // add value
                scriptEntry.addObject("value", arg.asType(Duration.class));
            }
            
            else if (!scriptEntry.hasObject("world")
                    && arg.matchesArgumentType(dWorld.class)) {
                // add world
                scriptEntry.addObject("world", arg.asType(dWorld.class));
        	}
        }
        
        // Check to make sure required arguments have been filled

        if ((!scriptEntry.hasObject("value")))
            throw new InvalidArgumentsException(Messages.ERROR_MISSING_OTHER, "VALUE");
        
        // Use default world if none has been specified
        
        if (!scriptEntry.hasObject("world")) {
            scriptEntry.addObject("world", dWorld.valueOf("world"));
        }
    }
    
    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        // Fetch objects
        Duration value = (Duration) scriptEntry.getObject("value");
        dWorld world = (dWorld) scriptEntry.getObject("world");
        Type type = scriptEntry.hasObject("type") ? 
        		(Type) scriptEntry.getObject("type") : Type.GLOBAL;

        // Report to dB
        dB.report(getName(), type.name() + ", "
                + (type.name().equalsIgnoreCase("player") ? scriptEntry.getPlayer().debug() : "")
                + (type.name().equalsIgnoreCase("global") ? world.debug() : "")
                + value.debug());

        if (type.equals(Type.GLOBAL)) {
        	world.getWorld().setTime(value.getTicks());
        }
        else {
        	scriptEntry.getPlayer().getPlayerEntity().setPlayerTime(value.getTicks(), true);
        }
    }
    
}
