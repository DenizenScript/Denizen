package net.aufdemrand.denizen.scripts.commands.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.Duration;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dList;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dScript;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.scripts.containers.core.TaskScriptContainer;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 * Makes the NPC shoot entities towards a location.
 * If no location is chosen, the NPC shoots entities in the direction it is facing.
 *
 * @author David Cernat
 */

public class ShootCommand extends AbstractCommand {
	
    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
    	
        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

        	if (!scriptEntry.hasObject("origin")
                    && arg.matchesArgumentType(dEntity.class)
                    && arg.matchesPrefix("origin, o, source, s")) {
                // Entity arg
                scriptEntry.addObject("origin", arg.asType(dEntity.class).setPrefix("entity"));
            }
            
            else if (!scriptEntry.hasObject("projectiles")
                    && arg.matchesArgumentType(dEntity.class)
                	&& arg.matchesPrefix("projectile, projectiles, p, entity, entities, e")) {
                // Entity arg
                scriptEntry.addObject("projectiles", arg.asType(dList.class));
            }
            
            else if (!scriptEntry.hasObject("destination")
                    && arg.matchesArgumentType(dLocation.class)) {
                // Location arg
                scriptEntry.addObject("destination", arg.asType(dLocation.class).setPrefix("location"));
            }
        	
            else if (!scriptEntry.hasObject("duration")
                    && arg.matchesArgumentType(Duration.class)) {
                // add value
                scriptEntry.addObject("duration", arg.asType(Duration.class));
            }
        	
            else if (!scriptEntry.hasObject("script")
                    && arg.matchesArgumentType(dScript.class)) {
                // add value
                scriptEntry.addObject("script", arg.asType(dScript.class));
            }
        }

        // Check to make sure required arguments have been filled
        
        if ((!scriptEntry.hasObject("projectiles")))
            throw new InvalidArgumentsException(Messages.ERROR_MISSING_OTHER, "PROJECTILE");
    }
    
	@Override
    public void execute(final ScriptEntry scriptEntry) throws CommandExecutionException {
        // Get objects
    	
		dEntity shooter = (dEntity) scriptEntry.getObject("origin");
		LivingEntity shooterEntity = shooter.getLivingEntity();
		final dLocation destination = scriptEntry.hasObject("location") ?
									  (dLocation) scriptEntry.getObject("location") :
									  new dLocation(shooterEntity.getEyeLocation().add(
											  		shooterEntity.getEyeLocation().getDirection()
											  		.multiply(40)));

        dEntity projectiles = (dEntity) scriptEntry.getObject("projectiles");
        final dScript script = (dScript) scriptEntry.getObject("script");
        
        // If the shooter is an NPC, always rotate it to face the destination
        // of the projectile, but if the shooter is a player, only rotate him/her
        // if he/she is not looking in the correct general direction
        
        if (shooter.identify().startsWith("n@") ||
        	Utilities.isFacingLocation(shooterEntity, destination, 45) == false) {
        	
        	Utilities.faceLocation(shooterEntity, destination);
        }
        
        Location origin = shooterEntity.getEyeLocation().add(
				  				 shooterEntity.getEyeLocation().getDirection())
				  				 .subtract(0, 0.4, 0);
        
        if (projectiles.isSpawned() == false) {
        	projectiles.spawnAt(origin);
        }

        final Entity projectile = projectiles.getBukkitEntity();
        
        Utilities.faceLocation(projectile, destination);
        
        if (projectile instanceof Projectile) {
			((Projectile) projectile).setShooter(shooter.getLivingEntity());
		}
        
        BukkitRunnable task = new BukkitRunnable() {

        	int runs = 0;

        	public void run() {
    	        				
        		if (runs < 40 && projectile.isValid())
        		{
        			Vector v1 = projectile.getLocation().toVector();
        			Vector v2 = destination.toVector();
        			Vector v3 = v2.clone().subtract(v1).normalize().multiply(1.5);
        							
        			projectile.setVelocity(v3);
        			runs++;
        				
        			// Check if the entity is close to its destination
        				
        			if (Math.abs(v2.getX() - v1.getX()) < 2 && Math.abs(v2.getY() - v1.getY()) < 2
        				&& Math.abs(v2.getZ() - v1.getZ()) < 2) {
        			
        					runs = 40;
        			}
        				
        			// Check if the entity has collided with something
        			// using the most basic possible calculation
        				
        			if (projectile.getLocation().add(v3).getBlock().getType().toString().equals("AIR") == false) {

        				runs = 40;
        			}
        		}
        		else {

        			this.cancel();
        			runs = 0;
        				
        			if (script != null)
        			{
        				Map<String, String> context = new HashMap<String, String>();
            			context.put("1", projectile.getLocation().getX() + "," + projectile.getLocation().getY() + "," + projectile.getLocation().getZ() + "," + projectile.getLocation().getWorld().getName());
        					
                        ((TaskScriptContainer) script.getContainer()).setSpeed(new Duration(0))
                           		.runTaskScript(scriptEntry.getPlayer(), scriptEntry.getNPC(), context);
        			}
        				
        		}
        	}
       	};
        
       	task.runTaskTimer(denizen, 0, 2);     
    }

}