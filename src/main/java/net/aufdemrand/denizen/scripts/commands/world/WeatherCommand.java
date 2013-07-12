package net.aufdemrand.denizen.scripts.commands.world;

import org.bukkit.WeatherType;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.dWorld;
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
            	scriptEntry.addObject("type", Type.valueOf(arg.getValue().toUpperCase()));

            else if (!scriptEntry.hasObject("world")
                    && arg.matchesArgumentType(dWorld.class))
                // add value
                scriptEntry.addObject("world", arg.asType(dWorld.class));

            else if (!scriptEntry.hasObject("value")
            		&& arg.matchesEnum(Value.values()))
                // add value
                scriptEntry.addObject("value", arg.asElement());
        }

        // Check to make sure required arguments have been filled

        if ((!scriptEntry.hasObject("value")))
            throw new InvalidArgumentsException(Messages.ERROR_MISSING_OTHER, "VALUE");
        
        // If the world has not been specified, try to use the NPC's or player's
        // world, or default to "world" if necessary
        
        if (!scriptEntry.hasObject("world")) {
            
            if ((scriptEntry.hasNPC())) {
            	scriptEntry.addObject("world", new dWorld(scriptEntry.getNPC().getWorld()));
            }
            else if ((scriptEntry.hasPlayer())) {
            	scriptEntry.addObject("world", new dWorld(scriptEntry.getPlayer().getWorld()));
            }
            else {
            	scriptEntry.addObject("world", dWorld.valueOf("world"));
            }
        }
    }
    
    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        // Fetch objects
        Value value = Value.valueOf(((Element) scriptEntry.getObject("value"))
        			 .asString().toUpperCase());
        dWorld world = (dWorld) scriptEntry.getObject("world");
        Type type = scriptEntry.hasObject("type") ? 
        		(Type) scriptEntry.getObject("type") : Type.GLOBAL;

        // Report to dB
        dB.report(getName(), aH.debugObj("type", type.name()) +
        		(type.name().equalsIgnoreCase("player") ?
        				aH.debugObj("player", scriptEntry.getPlayer()) : "") +
                (type.name().equalsIgnoreCase("global") ?
                        		aH.debugObj("world", world) : "") +
                aH.debugObj("value", value));

        switch(value) {
        	case SUNNY:
        		if (type.equals(Type.GLOBAL)) {
        			world.getWorld().setStorm(false);
        			world.getWorld().setThundering(false);
        		}
        		else {
        			scriptEntry.getPlayer().getPlayerEntity().setPlayerWeather(WeatherType.CLEAR);
        		}
        		
        		break;
            
        	case STORM:
        		if (type.equals(Type.GLOBAL)) {
        			world.getWorld().setStorm(true);
        		}
        		else {
        			scriptEntry.getPlayer().getPlayerEntity().setPlayerWeather(WeatherType.DOWNFALL);
        		}
        		
        		break;
            
        	case THUNDER:
        		// Note: setThundering always creates a storm
        		if (type.equals(Type.GLOBAL)) {
        			world.getWorld().setThundering(true);
        		}
        		else {
        			scriptEntry.getPlayer().getPlayerEntity().setPlayerWeather(WeatherType.DOWNFALL);
        		}
        		
        		break;
        }
    }
    
}
