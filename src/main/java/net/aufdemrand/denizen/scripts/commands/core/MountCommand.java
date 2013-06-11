package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.arguments.dEntity;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.arguments.dLocation;
import net.aufdemrand.denizen.utilities.arguments.aH.ArgumentType;
import net.aufdemrand.denizen.utilities.debugging.dB;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

/**
 * Mounts a player on the NPC if no targets are specified.
 * If targets are specified, mount them on each other in order.
 *
 * @author David Cernat
 */

public class MountCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry)
            throws InvalidArgumentsException {

        //
        // List of entities to be mounted.
        //
        List<Entity> entities = new ArrayList<Entity> ();

        //
        // Process the arguments.
        //
        Boolean dismount = false;
        dLocation location = null;
        
        for (String arg : scriptEntry.getArguments()) {

        	if (aH.matchesLocation(arg)) {

                location = aH.getLocationFrom(arg);
                dB.echoDebug("...location set to '%s'.", arg);
        	}
        	else if (aH.matchesArg("cancel", arg)) {
        		dismount = true;
        		dB.echoDebug("...will dismount.");
        	}
        	else if (aH.matchesValueArg("TARGETS, TARGET", arg, ArgumentType.Custom)) {
            	
                Entity entity = null;

                for (String target : aH.getListFrom(arg)) {
                    // Get entity
                	if (aH.matchesEntityType(target)) {
                		
                		dLocation entityLocation = null;
                		
                		// Cannot spawn an entity without a location, so go through possible locations
                		if (location != null)
                			entityLocation = location;
                		else if (scriptEntry.getPlayer() != null)
                            entityLocation = new dLocation(scriptEntry.getPlayer().getLocation());
                		else if (scriptEntry.getNPC() != null)
                            entityLocation = new dLocation(scriptEntry.getNPC().getLocation());
                		
                		if (entityLocation != null) {
                			
                			EntityType entityType = aH.getEntityFrom(target);
                			entity = entityLocation.getWorld().spawnEntity(entityLocation, entityType);
                		}
                	}
                	else {
                		entity = dEntity.valueOf(target).getBukkitEntity();
                	}
                	
                	if (entity != null) {
            			entities.add(entity);
            		}
            		else {
            			dB.echoError("Invalid target '%s'!", target);
            		}
                }
            }

            else throw new InvalidArgumentsException(dB.Messages.ERROR_UNKNOWN_ARGUMENT, arg);
        }

        // If there are no targets, default to the player mounting this NPC
        
        if (entities.size() == 0) {
            entities.add(scriptEntry.getPlayer());
            entities.add(scriptEntry.getNPC().getEntity());
        }
        
        // If there is only one target entity, there will be no one to mount
        // it, so make this player mount it by adding him/her to the start
        // of the list

        if (entities.size() == 1) {
        	
        	entities.add(0, scriptEntry.getPlayer());
        }

        // Store objects in ScriptEntry for use in execute()
        scriptEntry.addObject("entities", entities);
        scriptEntry.addObject("location", location);
        scriptEntry.addObject("dismount", dismount);
    }

    @SuppressWarnings("unchecked")
	@Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        List<Entity> entities = (List<Entity>) scriptEntry.getObject("entities");
        final dLocation location = (dLocation) scriptEntry.getObject("location");
        Boolean dismount = (Boolean) scriptEntry.getObject("dismount");

        // Debug output
        dB.echoApproval("<G>Executing '<Y>" + getName() + "<G>': "
                + "Targets=<Y>'" + entities.toString() + "<G>'");

        Entity lastEntity = null;
        
        for (Entity entity : entities) {
        	
        	if (dismount) {
        		
        		entity.leaveVehicle();
        	}
        	else {
        	
        		if (lastEntity != null) {
        			// Because setPassenger() is a toggle, only use it if the new passenger
        			// is not already the current passenger
        		
        			if (entity.getPassenger() != lastEntity) {
        				lastEntity.teleport(entity.getLocation());
        				entity.setPassenger(lastEntity);
        			}
        		}
        	
        		lastEntity = entity;
        	}
        }
        
        if (location != null) {
    		lastEntity.teleport(location);
    	}
    }
}
