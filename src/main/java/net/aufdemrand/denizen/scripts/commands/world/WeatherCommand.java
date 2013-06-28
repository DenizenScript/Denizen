package net.aufdemrand.denizen.scripts.commands.world;

import org.bukkit.Bukkit;
import org.bukkit.World;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;

/**
 *
 * Set the weather in the world.
 *
 */
public class WeatherCommand extends AbstractCommand {

    private enum Type { GLOBAL, PLAYER }
    private enum Value { SUNNY, STORM, THUNDER }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("type")
                    && arg.matchesEnum(Type.values()))
                // add type
                scriptEntry.addObject("type", arg.asElement());


            else if (!scriptEntry.hasObject("value")
            		&& arg.matchesEnum(Value.values()))
                // add value
                scriptEntry.addObject("value", arg.asElement());
        }

        // Check to make sure required arguments have been filled

        if ((!scriptEntry.hasObject("value")))
            throw new InvalidArgumentsException(Messages.ERROR_MISSING_OTHER, "VALUE");
    }
    
    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        // Fetch objects
        Value value = Value.valueOf(((Element) scriptEntry.getObject("value"))
        			 .asString().toUpperCase());
        Element type = (scriptEntry.hasObject("type") ?
                	   (Element) scriptEntry.getObject("type") : new Element("player"));
        World world = scriptEntry.getPlayer().getPlayerEntity().getWorld();

        // Report to dB
        dB.report(getName(), type.debug()
                + (type.toString().equalsIgnoreCase("player") ? scriptEntry.getPlayer().debug() : "")
                + value.toString());

        switch(value) {
        	case SUNNY:
            	world.setStorm(false);
            	world.setThundering(false);
            	break;
            
        	case STORM:
        		world.setStorm(true);
        		break;
            
        	case THUNDER:
        		// Note: setThundering always creates a storm
        		world.setThundering(true);
        		break;
        }
    }
    
}
