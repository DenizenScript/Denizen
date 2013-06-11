package net.aufdemrand.denizen.scripts.commands.core;

import java.util.HashMap;
import java.util.Map;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.scripts.containers.core.TaskScriptContainer;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.utilities.arguments.Duration;
import net.aufdemrand.denizen.utilities.arguments.Script;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
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
	
    private enum ShooterType { NPC, PLAYER }
	
    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Initialize necessary fields
	    ShooterType shooterType = ShooterType.NPC;
	    LivingEntity shooter = null;
        EntityType entityType = null;
        //Integer qty = null;
        Location location = null;
        Script newScript = null;
        Boolean ride = false;
        Boolean burn = false;

        for (String arg : scriptEntry.getArguments()) {
            if (aH.matchesEntityType(arg)) {
                entityType = aH.getEntityFrom(arg);
                dB.echoDebug("...projectile set to '%s'.", arg);

            } else if (aH.matchesLocation(arg)) {
                location = aH.getLocationFrom(arg);
                dB.echoDebug("...location set to '%s'.", arg);
                
            } else if (aH.matchesArg("PLAYER", arg)) {
        		shooterType = ShooterType.PLAYER;
                dB.echoDebug("... will be shot by the player!");
                
        	} else if (aH.matchesScript(arg)) {
				newScript = aH.getScriptFrom(arg);
				dB.echoDebug(Messages.DEBUG_SET_SCRIPT, arg);

            } else if (aH.matchesArg("ride, mount", arg)) {
                ride = true;
                dB.echoDebug("...will be mounted.");
                
            } else if (aH.matchesArg("burn, burning", arg)) {
                burn = true;
                dB.echoDebug("...will burn.");
               
            } else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
        }
        
        if (shooterType.name().equals("NPC")) {
        	
        	shooter = scriptEntry.getNPC().getEntity();
        }
        else if (shooterType.name().equals("PLAYER")) {
        	
        	shooter = scriptEntry.getPlayer();
        }
        
        if (location == null) {
        	
        	location = shooter.getEyeLocation().add(
        			   		shooter.getEyeLocation().getDirection().
                    		multiply(40));
        }
        
        if (entityType == null) throw new InvalidArgumentsException(Messages.ERROR_INVALID_ENTITY);
        
        // Stash objects
        scriptEntry.addObject("entityType", entityType);
        scriptEntry.addObject("location", location);
        scriptEntry.addObject("shooter", shooter);
        scriptEntry.addObject("shooterType", shooterType);
        scriptEntry.addObject("script", newScript);
        scriptEntry.addObject("ride", ride);
        scriptEntry.addObject("burn", burn);
    }
    
	@Override
    public void execute(final ScriptEntry scriptEntry) throws CommandExecutionException {
        // Get objects
    	
		final Location location = (Location) scriptEntry.getObject("location");
		LivingEntity shooter = (LivingEntity) scriptEntry.getObject("shooter");
		ShooterType shooterType = (ShooterType) scriptEntry.getObject("shooterType");
        EntityType entityType = (EntityType) scriptEntry.getObject("entityType");
        
        Boolean ride = (Boolean) scriptEntry.getObject("ride");
        Boolean burn = (Boolean) scriptEntry.getObject("burn");
        
        // If the shooter is an NPC, always rotate it to face the destination
        // of the projectile, but if the shooter is a player, only rotate him/her
        // if he/she is not looking in the correct general direction
        
        if (shooterType.name().equals("NPC") ||
        	Utilities.isFacingLocation(shooter, location, 45) == false) {
        
        	Utilities.faceLocation(shooter, location);
        }
        
        final Entity entity = shooter.getWorld().spawnEntity(
        				shooter.getEyeLocation().add(
        				shooter.getEyeLocation().getDirection())
        				.subtract(0, 0.4, 0),
        				entityType);
        
        Utilities.faceLocation(entity, location);
        
        if (ride == true) {
        	entity.setPassenger(scriptEntry.getPlayer());
        }
        
        if (burn == true) {
        	entity.setFireTicks(500);
        }
        
        if (entity instanceof Projectile) {
			((Projectile) entity).setShooter(shooter);
		}
        
        BukkitRunnable task = new BukkitRunnable()
        {
        	int runs = 0;

        	public void run() {
    	        				
        		if (runs < 40 && entity.isValid())
        		{
        			Vector v1 = entity.getLocation().toVector();
        			Vector v2 = location.toVector();
        			Vector v3 = v2.clone().subtract(v1).normalize().multiply(1.5);
        							
        			entity.setVelocity(v3);
        			runs++;
        				
        			// Check if the entity is close to its destination
        				
        			if (Math.abs(v2.getX() - v1.getX()) < 2 && Math.abs(v2.getY() - v1.getY()) < 2
        				&& Math.abs(v2.getZ() - v1.getZ()) < 2) {
        			
        					runs = 40;
        			}
        				
        			// Check if the entity has collided with something
        			// using the most basic possible calculation
        				
        			if (entity.getLocation().add(v3).getBlock().getType().toString().equals("AIR") == false) {

        				runs = 40;
        			}
        		}
        		else {

        			this.cancel();
        			runs = 0;
        				
        			if (scriptEntry.getObject("script") != null)
        			{
        				Map<String, String> context = new HashMap<String, String>();
            			context.put("1", entity.getLocation().getX() + "," + entity.getLocation().getY() + "," + entity.getLocation().getZ() + "," + entity.getLocation().getWorld().getName());
        					
                        ((TaskScriptContainer) ((Script) scriptEntry.getObject("script")).
                           		getContainer()).setSpeed(new Duration(0))
                           		.runTaskScript(scriptEntry.getPlayer(), scriptEntry.getNPC(), context);
        			}
        				
        		}
        	}
       	};
        
       	task.runTaskTimer(denizen, 0, 2);     
    }

}