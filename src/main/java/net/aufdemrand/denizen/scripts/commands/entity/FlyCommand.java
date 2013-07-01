package net.aufdemrand.denizen.scripts.commands.entity;

import java.util.ArrayList;
import java.util.List;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.aH.ArgumentType;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import net.aufdemrand.denizen.utilities.entity.Rotation;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 * Fly on top of an entity in the direction you are looking, unless
 * a list of locations is specified, in which case the entity flies
 * towards them.
 *
 * @author David Cernat
 */

public class FlyCommand extends AbstractCommand {
	
    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Initialize necessary fields
    	List<Entity> entities = new ArrayList<Entity> ();
    	List<dLocation> destinations = new ArrayList<dLocation> ();
    	dLocation origin = null;
    	Boolean dismount = false;

        for (String arg : scriptEntry.getArguments()) {
        	if (aH.matchesValueArg("origin", arg, ArgumentType.Location)) {
        		
        		origin = aH.getLocationFrom(arg);
                dB.echoDebug("...origin set to '%s'.", arg);
                
            }
        	else if (aH.matchesArg("cancel", arg)) {
        		dismount = true;
        		dB.echoDebug("...will dismount.");
        	}
        	else if (aH.matchesValueArg("destination, destinations", arg, ArgumentType.Custom)) {
        	
        		for (String destination : aH.getListFrom(arg)) {

        			if (aH.matchesLocation("location:" + destination)) {
        				        				
        				destinations.add(aH.getLocationFrom(destination));
                        dB.echoDebug("...added '%s' to destinations.", arg);
        			}
        		}
        	}
        	else if (aH.matchesValueArg("entities, entity", arg, ArgumentType.Custom)) {
            	
                Entity entity = null;

                for (String target : aH.getListFrom(arg)) {
                    // Get entity
                	if (aH.matchesEntityType(target)) {
                		
                		dLocation entityLocation = null;
                		
                		// Cannot spawn an entity without a location, so go through possible locations
                		if (origin != null)
                			entityLocation = origin;
                		else if (scriptEntry.getPlayer() != null)
                            entityLocation = new dLocation(scriptEntry.getPlayer().getLocation());
                		else if (scriptEntry.getNPC() != null)
                            entityLocation = new dLocation(scriptEntry.getNPC().getLocation());
                		
                		if (entityLocation != null) {
                			
                			EntityType entityType = aH.getEntityTypeFrom(target);
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
            			dB.echoError("Invalid entity '%s'!", target);
            		}
                }
            }
               
            else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
        }
        
        // If there are no targets, default to the player mounting this NPC
        
        if (entities.size() == 0) {
            entities.add(scriptEntry.getPlayer().getPlayerEntity());
            entities.add(scriptEntry.getNPC().getEntity());
        }
        
        // If there is only one target entity, there will be no one to mount
        // it, so make this player mount it by adding him/her to the start
        // of the list

        if (entities.size() == 1) {
        	
        	entities.add(0, scriptEntry.getPlayer().getPlayerEntity());
        }
        
        // Stash objects
        scriptEntry.addObject("entities", entities);
        scriptEntry.addObject("destinations", destinations);
        scriptEntry.addObject("origin", origin);
        scriptEntry.addObject("dismount", dismount);
    }
    
	@Override
    public void execute(final ScriptEntry scriptEntry) throws CommandExecutionException {
        // Get objects
    	
		dLocation origin = (dLocation) scriptEntry.getObject("origin");
		List<Entity> entities = (List<Entity>) scriptEntry.getObject("entities");
		final List<dLocation> destinations = (List<dLocation>) scriptEntry.getObject("destinations");
		Boolean dismount = (Boolean) scriptEntry.getObject("dismount");
		
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
        
        if (origin != null) {
    		lastEntity.teleport(origin);
    	}
        
        if (dismount) {
        	
        	return;
        }
        
        final Entity entity = lastEntity;
        final Player player = scriptEntry.getPlayer().getPlayerEntity();
        
		// Set freeflight to true only if there are no destinations
        final Boolean freeflight = destinations.size() > 0 ? false : true;
        
        BukkitRunnable task = new BukkitRunnable()
        {
        	Location location = null;
        	Boolean flying = true;

        	public void run() {
    	                		
        		if (freeflight == true) {
        				
        			location = player.getEyeLocation().add(
            			   		   		player.getEyeLocation().getDirection().
            			   		   		multiply(30));
        		}
        		else {
        				
        			// If freelight is not on, keep flying only as long
        			// as there are destinations left
        				
        			if (destinations.size() > 0) {
        				
        				location = destinations.get(0);
        			}
        			else {
        					
        				flying = false;
        			}
        		}
        			
            	if (flying == true &&
             		   entity.isValid() == true &&
             		   entity.isEmpty() == false) {
        			
            		// To avoid excessive turbulence, only have the entity rotate
            		// when it really needs to
            		if (Rotation.isFacingLocation(entity, location, 50) == false) {
        		        
            			Rotation.faceLocation(entity, location);
            		}
        			
            		Vector v1 = entity.getLocation().toVector();
            		Vector v2 = location.toVector();
            		Vector v3 = v2.clone().subtract(v1).normalize().multiply(1.5);
    				
            		entity.setVelocity(v3);
    				
            		// If freeflight is off, check if the entity has reached its
            		// destination, and remove the destination if that happens
            		// to be the case
    				
            		if (freeflight == false) {
        			
            			if (Math.abs(v2.getX() - v1.getX()) < 2 && Math.abs(v2.getY() - v1.getY()) < 2
        					&& Math.abs(v2.getZ() - v1.getZ()) < 2) {
        			
            				destinations.remove(0);
            			}
            		}
            	}
            	else {
        			
            		flying = false;
            		this.cancel();
            	}
        	}
        };
        
       	task.runTaskTimer(denizen, 0, 3);     
    }

}