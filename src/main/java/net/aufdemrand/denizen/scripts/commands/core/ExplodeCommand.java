package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.arguments.aH.ArgumentType;
import net.aufdemrand.denizen.utilities.arguments.dLocation;
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

        dLocation location = null;
        Float power = 1F;
        boolean breakblocks = false;
        boolean setFire = false;

        for (String arg : scriptEntry.getArguments()) {
            if (aH.matchesLocation(arg)) {
                location = aH.getLocationFrom(arg);
                dB.echoDebug("...location set to '%s'.", arg);

            } else if (aH.matchesValueArg("power", arg, ArgumentType.Float)) {
                power = aH.getFloatFrom(arg);
                dB.echoDebug("...will have a power of " + power);
                
            } else if (aH.matchesArg("breakblocks", arg)) {
                    breakblocks = true;
                    dB.echoDebug("...will break blocks.");        
                    
            } else if (aH.matchesArg("setFire", arg)) {
                setFire = true;
                dB.echoDebug("...will set Fire on blocks.");
                    
            } else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
        }            

         // Stash objects
         scriptEntry.addObject("location", location);
         scriptEntry.addObject("power", power);
         scriptEntry.addObject("breakblocks", breakblocks);
         scriptEntry.addObject("setFire", setFire);
    }
    
    @Override
    public void execute(final ScriptEntry scriptEntry) throws CommandExecutionException {
        // Get objects
        
        final dLocation location = scriptEntry.hasObject("location") ?
                (dLocation) scriptEntry.getObject("location") :
                (dLocation) scriptEntry.getNPC().getLocation();
        Float power = (Float) scriptEntry.getObject("power");
        boolean breakblocks = (Boolean) scriptEntry.getObject("breakblocks");
        boolean setFire = (Boolean) scriptEntry.getObject("setFire");
        
        location.getWorld().createExplosion(location.getX(),location.getY(),location.getZ(), (Float) power,breakblocks,setFire);
 
    }

}