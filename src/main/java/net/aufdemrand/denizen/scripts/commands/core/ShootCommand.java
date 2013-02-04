package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.arguments.Location;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import net.aufdemrand.denizen.utilities.runnables.Runnable3;
import net.aufdemrand.denizen.utilities.Utilities;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Projectile;
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

        // Initialize necessary fields
        EntityType entityType = null;
        Integer qty = null;
        Location location = null;
        Boolean ride = false;
        Boolean burn = false;

        // Set some defaults
        if (scriptEntry.getPlayer() != null)
            location = new Location(scriptEntry.getPlayer().getLocation());
        if (location == null && scriptEntry.getNPC() != null)
            location = new Location(scriptEntry.getNPC().getLocation());

        for (String arg : scriptEntry.getArguments()) {
            if (aH.matchesEntityType(arg)) {
                entityType = aH.getEntityFrom(arg);
                dB.echoDebug("...entity set to '%s'.", arg);

            } else if (aH.matchesLocation(arg)) {
                location = aH.getLocationFrom(arg);
                dB.echoDebug("...location set to '%s'.", arg);

            } else if (aH.matchesArg("RIDE, MOUNT", arg)) {
                ride = true;
                dB.echoDebug("...will be mounted.");
                
            } else if (aH.matchesArg("BURN, BURNING", arg)) {
                burn = true;
                dB.echoDebug("...will burn.");

            } else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
        }
        
        if (entityType == null) throw new InvalidArgumentsException(Messages.ERROR_INVALID_ENTITY);

        // Stash objects
        scriptEntry.addObject("location", location);
        scriptEntry.addObject("entityType", entityType);
        scriptEntry.addObject("ride", ride);
        scriptEntry.addObject("burn", burn);
    }
    
    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        // Get objects
    	
        Location location = (Location) scriptEntry.getObject("location");
        EntityType entityType = (EntityType) scriptEntry.getObject("entityType");
        Boolean ride = (Boolean) scriptEntry.getObject("ride");
        Boolean burn = (Boolean) scriptEntry.getObject("burn");
        
        if (location == null)
        {
        	location = (Location) scriptEntry.getNPC().getEyeLocation().getDirection().
        				multiply(4).toLocation(scriptEntry.getNPC().getWorld());
        }
        else
        {
        	Utilities.faceLocation(scriptEntry.getNPC().getCitizen().getBukkitEntity(), location);
        }

        Entity entity = scriptEntry.getNPC().getWorld().spawnEntity(
        				scriptEntry.getNPC().getEyeLocation().add(
        				scriptEntry.getNPC().getEyeLocation().getDirection())
        				.subtract(0, 0.4, 0),
        				entityType);
        
        Utilities.faceLocation(entity, location);
        
        if (ride == true)
        {
        	entity.setPassenger(scriptEntry.getPlayer());
        }
        
        if (burn == true)
        {
        	entity.setFireTicks(500);
        }
        
        if (entity instanceof Projectile)
		{
			((Projectile) entity).setShooter(scriptEntry.getNPC().getCitizen().getBukkitEntity());
		}
        
        Runnable3 task = new Runnable3<ScriptEntry, Entity, Location>
        				(scriptEntry, entity, location)
        	{
        		@Override
        		public void run(ScriptEntry scriptEntry, Entity entity, Location location) {
    	        				
        			if (getRuns() < 40 && entity.isValid())
        			{
        				//dB.echoDebug(entity.getType().name() + " flying time " + getRuns() + " in task " + getId());
        							
        				Vector v1 = entity.getLocation().toVector().clone();
        				Vector v2 = location.toVector().clone();
        				Vector v3 = v2.clone().subtract(v1).normalize().multiply(1.5);
        							
        				entity.setVelocity(v3);
        				addRuns();
        						
        				if (Math.abs(v2.getBlockX() - v1.getBlockX()) < 2 && Math.abs(v2.getBlockY() - v1.getBlockY()) < 2
        				&& Math.abs(v2.getBlockZ() - v1.getBlockZ()) < 2)
        				{
        					this.cancel();
        					clearRuns();
        				}
        			}
        			else
        			{
        				this.cancel();
        				clearRuns();
        			}
        		}
       		};
        
        task.setId(Bukkit.getScheduler().scheduleSyncRepeatingTask(denizen, task, 0, 2));        
    }
    

}