package net.aufdemrand.denizen.scripts.commands.entity;

import java.util.ArrayList;
import java.util.List;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dList;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.Conversion;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import net.aufdemrand.denizen.utilities.entity.Position;
import net.aufdemrand.denizen.utilities.entity.Rotation;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
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

    	for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {
        	
    		if (!scriptEntry.hasObject("cancel")
    				&& arg.matches("cancel")) {
    			
    			scriptEntry.addObject("cancel", "");
    		}
    		
            if (!scriptEntry.hasObject("origin")
                    && arg.matchesArgumentType(dLocation.class)) {
                // Location arg
                scriptEntry.addObject("origin", arg.asType(dLocation.class).setPrefix("origin"));
            }
        	
            else if (!scriptEntry.hasObject("destinations")
                	&& arg.matchesPrefix("destination, destinations, d")) {
                // Entity arg
                scriptEntry.addObject("destinations", ((dList) arg.asType(dList.class)).filter(dLocation.class));
            }
            
        	else if (!scriptEntry.hasObject("entities")
                	&& arg.matchesPrefix("entity, entities, e")) {
                // Entity arg
                scriptEntry.addObject("entities", ((dList) arg.asType(dList.class)).filter(dEntity.class));
            }
        }
    	
    	// Check to make sure required arguments have been filled
        
        if ((!scriptEntry.hasObject("entities")))
            throw new InvalidArgumentsException(Messages.ERROR_MISSING_OTHER, "ENTITIES");
        
        // Use the NPC or player's locations as the origin if one is not specified
        
        if ((!scriptEntry.hasObject("origin"))) {
        	
        	if (scriptEntry.hasNPC())
        		scriptEntry.addObject("origin", scriptEntry.getNPC().getLocation());
        	else if (scriptEntry.hasPlayer())
        		scriptEntry.addObject("origin", scriptEntry.getPlayer().getLocation());
        	else
        		throw new InvalidArgumentsException(Messages.ERROR_MISSING_OTHER, "ORIGIN");
        }
    }
    
	@SuppressWarnings("unchecked")
	@Override
    public void execute(final ScriptEntry scriptEntry) throws CommandExecutionException {
        // Get objects
    	
		dLocation origin = (dLocation) scriptEntry.getObject("origin");
		List<dEntity> entities = (List<dEntity>) scriptEntry.getObject("entities");
		final List<dLocation> destinations = scriptEntry.hasObject("destinations") ?
								(List<dLocation>) scriptEntry.getObject("destinations") :
								new ArrayList<dLocation>();			
		Boolean cancel = scriptEntry.hasObject("cancel") ?
							true :
							false;
		
        // Report to dB
        dB.report(getName(), (cancel == true ? "cancel, " : "") +
        					 aH.debugObj("origin", origin) +
        					 aH.debugObj("entities", entities.toString()) +
        					 (destinations.size() > 0 ? aH.debugObj("destinations", destinations.toString()) : ""));
		        
		// Mount or dismount all of the entities
		if (cancel == false) {
			
			// Go through all the entities, spawning/teleporting them
	        for (dEntity entity : entities) {
	        	
	        	if (entity.isSpawned() == false) {
	        		entity.spawnAt(origin);
	        	}
	        	else {
	        		entity.teleport(origin);
	        	}
	        }
			
			Position.mount(Conversion.convert(entities));
		}
		else {
			Position.dismount(Conversion.convert(entities));
			
			// Go no further if we are dismounting entities
			return;
			
		}
        
        // Get the last entity on the list
        final Entity entity = entities.get(entities.size() - 1).getBukkitEntity();
        
        // Get the attached player
        final Player player = scriptEntry.getPlayer().getPlayerEntity();
        
		// Set freeflight to true only if there are no destinations
        final Boolean freeflight = destinations.size() > 0 ? false : true;
        
        BukkitRunnable task = new BukkitRunnable() {

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