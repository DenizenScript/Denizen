package net.aufdemrand.denizen.scripts.commands.world;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;

/**
 * Create an explosion at a location.
 * If no location is specified, create the explosion where the NPC is.
 *
 * @author Alain Blanquet
 */

public class ExplodeCommand extends AbstractCommand {
    
    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Iterate through arguments
        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {
        
            if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentType(dLocation.class)) {
                // Location arg
                scriptEntry.addObject("location", arg.asType(dLocation.class));
            }
            
            else if (!scriptEntry.hasObject("power")
                    && arg.matchesPrimitive(aH.PrimitiveType.Float)
                    && arg.matchesPrefix("power, p")) {
                // Add value
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
        }
        
        // Use default values if necessary
        
        scriptEntry.defaultObject("power", new Element(1.0));
        scriptEntry.defaultObject("location",
                scriptEntry.hasNPC() ? scriptEntry.getNPC().getLocation() : null,
                scriptEntry.hasPlayer() ? scriptEntry.getPlayer().getLocation() : null);
        
        if (!scriptEntry.hasObject("location")) {
            throw new InvalidArgumentsException(Messages.ERROR_MISSING_OTHER, "LOCATION");
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
        dB.report(getName(),
                (aH.debugObj("location", location.toString()) +
                 aH.debugObj("power", power) +
                 aH.debugObj("breakblocks", breakblocks) +
                 aH.debugObj("fire", fire)));
        
        location.getWorld().createExplosion
                    (location.getX(), location.getY(), location.getZ(),
                     power.asFloat(), fire, breakblocks);
    }
}