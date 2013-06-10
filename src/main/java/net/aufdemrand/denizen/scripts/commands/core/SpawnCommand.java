package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.arguments.dLocation;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

/**
 * Spawn entities at a location.
 * If no location is chosen, the entities are spawned at the NPC's location.
 *
 * @author David Cernat
 */

public class SpawnCommand extends AbstractCommand {
	
    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Initialize necessary fields
        EntityType entityType = null;
        //Integer qty = null;
        dLocation location = null;

        for (String arg : scriptEntry.getArguments()) {
            if (aH.matchesEntityType(arg)) {
                entityType = aH.getEntityFrom(arg);
                dB.echoDebug("...entity set to '%s'.", arg);

            } else if (aH.matchesLocation(arg)) {
                location = aH.getLocationFrom(arg);
                dB.echoDebug("...location set to '%s'.", arg);

            } else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
        }
        
        if (entityType == null) throw new InvalidArgumentsException(Messages.ERROR_INVALID_ENTITY);

        // Stash objects
        scriptEntry.addObject("entityType", entityType);
        scriptEntry.addObject("location", location);
    }
    
	@Override
    public void execute(final ScriptEntry scriptEntry) throws CommandExecutionException {
        // Get objects
    	
        final dLocation location = scriptEntry.hasObject("location") ?
        		                   (dLocation) scriptEntry.getObject("location") :
        		                   (dLocation) scriptEntry.getNPC().getLocation();
        		                   
        EntityType entityType = (EntityType) scriptEntry.getObject("entityType");

        final Entity entity = location.getWorld().spawnEntity(
        					  location, entityType);
        
    }

}