package net.aufdemrand.denizen.scripts.commands.entity;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dList;
import net.aufdemrand.denizen.objects.dWorld;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import net.aufdemrand.denizen.utilities.depends.WorldGuardUtilities;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

import java.util.List;

/**
 * Delete certain entities or all entities of a type.
 * Can permanently remove NPCs if used on them.
 *
 * @author David Cernat
 */

public class RemoveCommand extends AbstractCommand {
	
    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
    	
        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

        	if (!scriptEntry.hasObject("entities")
                	&& arg.matchesArgumentList(dEntity.class)) {
                // Entity arg
                scriptEntry.addObject("entities", ((dList) arg.asType(dList.class)).filter(dEntity.class));
            }
            
        	else if (!scriptEntry.hasObject("region")
        			&& arg.matchesPrefix("region, r")) {
                // Location arg
                scriptEntry.addObject("region", arg.asElement());
            }
        	
        	else if (!scriptEntry.hasObject("world")
                    && arg.matchesArgumentType(dWorld.class)) {
                // add world
                scriptEntry.addObject("world", arg.asType(dWorld.class));
        	}
        }

        // Check to make sure required arguments have been filled
        
        if ((!scriptEntry.hasObject("entities")))
            throw new InvalidArgumentsException(Messages.ERROR_MISSING_OTHER, "ENTITIES");
        
        // If the world has not been specified, try to use the NPC's or player's
        // world, or default to the specified world in the server properties if necessary
 
        scriptEntry.defaultObject("world",
        		scriptEntry.hasNPC() ? new dWorld(scriptEntry.getNPC().getWorld()) : null,
        		scriptEntry.hasPlayer() ? new dWorld(scriptEntry.getPlayer().getWorld()) : null,
        		new dWorld(Bukkit.getWorlds().get(0)));
    }
    
	@SuppressWarnings("unchecked")
	@Override
    public void execute(final ScriptEntry scriptEntry) throws CommandExecutionException {

		// Get objects
		List<dEntity> entities = (List<dEntity>) scriptEntry.getObject("entities");
		dWorld world = (dWorld) scriptEntry.getObject("world");
		Element region = (Element) scriptEntry.getObject("region");
		
        // Report to dB
        dB.report(getName(), aH.debugObj("entities", entities.toString()) +
        					 (region != null ? aH.debugObj("region", region) : ""));
		
        boolean conditionsMet;
        
        // Go through all of our entities and remove them
        
        for (dEntity entity : entities) {
			// NP check to prevent errors from happening
			if(entity == null) {
				continue;
			}
        	
        	conditionsMet = true;
    		
        	// If this is a specific spawned entity, and all
        	// other applicable conditions are met, remove it
        	
        	if (entity.isGeneric()) {
        		
        		if (region != null) {
        			conditionsMet = WorldGuardUtilities.inRegion
        							(entity.getBukkitEntity().getLocation(),
        							region.asString());
        		}
        		
        		if (conditionsMet) {
        		
        			if (entity.isNPC()) {
        				entity.getNPC().destroy();
        			}
        			else {
        				entity.remove();
        			}
        		}
        	}
        	
        	// If this is a generic unspawned entity, remove
        	// all entities of this type from the world
        	
        	else {
        		
                // Note: getting the entities from each loaded chunk
                // in the world (like in Essentials' /killall) has the
        		// exact same effect as the below
        		
        		for (Entity worldEntity : world.getEntities()) {
        			
        			// If this entity from the world is of the same type
        			// as our current dEntity, and all other applicable
        			// conditions are met, remove it
        			
        			if (entity.getEntityType().equals(worldEntity.getType())) {
        				
        				if (region != null) {
        					conditionsMet = WorldGuardUtilities.inRegion
        									(worldEntity.getLocation(),
        									region.asString());
        				}
        				
        				if (conditionsMet == true) {
        					worldEntity.remove();
        				}
        			}
        		}
        	}
    	}
	}
}