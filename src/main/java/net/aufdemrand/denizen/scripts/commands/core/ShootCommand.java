package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.npc.dNPC;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.arguments.Duration;
import net.aufdemrand.denizen.utilities.arguments.Location;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import net.aufdemrand.denizen.utilities.runnables.Runnable3;
import net.aufdemrand.denizen.utilities.Utilities;

import org.bukkit.Bukkit;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
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

        // Set some defaults
        if (scriptEntry.getPlayer() != null)
            location = new Location(scriptEntry.getPlayer().getLocation());
        if (location == null && scriptEntry.getNPC() != null)
            location = new Location(scriptEntry.getNPC().getLocation());

        for (String arg : scriptEntry.getArguments()) {
            if (aH.matchesEntityType(arg)) {
                entityType = aH.getEntityFrom(arg);

            } else if (aH.matchesQuantity(arg)) {
                qty = aH.getIntegerFrom(arg);

            } else if (aH.matchesLocation(arg)) {
                location = aH.getLocationFrom(arg);
               
            } else if (aH.matchesArg("RIDE, MOUNT", arg)) {
                ride = true;

            } else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
        }
        

    	dB.echoApproval("Finished argument parsing!");

        if (entityType == null) throw new InvalidArgumentsException(Messages.ERROR_INVALID_ENTITY);

        // Stash objects
        scriptEntry.addObject("location", location);
        scriptEntry.addObject("entityType", entityType);
        scriptEntry.addObject("qty", qty);
        scriptEntry.addObject("ride", ride);
    }
    
    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        // Get objects
    	dB.echoApproval("Reached execute part!");
    	
        Location location = (Location) scriptEntry.getObject("location");
        Integer qty = (Integer) scriptEntry.getObject("qty");
        EntityType entityType = (EntityType) scriptEntry.getObject("entityType");
        Boolean ride = (Boolean) scriptEntry.getObject("ride");
        
        //Vector direction = scriptEntry.getNPC().getEyeLocation().getDirection().multiply(2.5);

        // Set quantity if not specified
        if (qty != null && entityType != null)
            qty = 1;
        else qty = 1;
        
        Entity entity = scriptEntry.getNPC().getWorld().spawnEntity(
    			scriptEntry.getNPC().getEyeLocation(), entityType);
        
        entity.teleport(Utilities.lookAt(entity.getLocation(), location));
        
        if (ride == true)
        {
        	entity.setPassenger(scriptEntry.getPlayer());
        }
        
        Runnable3 task = new Runnable3<ScriptEntry, Entity, Location>
        				(scriptEntry, entity, location)
        				{
        					@Override
        					public void run(ScriptEntry scriptEntry, Entity entity, Location location) {
    	        
        						if (getRuns() < 40)
        						{
        							dB.echoDebug(entity.getType().name() + " is flying in task " + getId());
        						
        							Vector v1 = entity.getLocation().toVector().clone();
        							Vector v2 = location.toVector().clone();
        							Vector v3 = v2.clone().subtract(v1).normalize().multiply(2);
        							
        							entity.setVelocity(v3);
                
        							//dB.echoError("Current run: " + getRuns() + " of " + getId());
        							//dB.echoApproval("Vector 1: " + v1.toString());
        							//dB.echoApproval("Vector 2: " + v2.toString());
        							//dB.echoApproval("Vector 1 floored: " + v1.getBlockX() + " " + v1.getBlockY() + " " + v1.getBlockZ());
        							//dB.echoApproval("Vector 2 floored: " + v2.getBlockX() + " " + v2.getBlockY() + " " + v2.getBlockZ());
        							//dB.echoApproval("Vector 3: " + v3.toString());
        							//dB.echoApproval("Vector 3 floored: " + v3.getBlockX() + " " + v3.getBlockY() + " " + v3.getBlockZ());
        							//dB.echoApproval("Location 1: " + entity.getLocation().toString());
        							addRuns();
        						
        							if (Math.abs(v2.getBlockX() - v1.getBlockX()) < 2 && Math.abs(v2.getBlockY() - v1.getBlockY()) < 2
        									&& Math.abs(v2.getBlockZ() - v1.getBlockZ()) < 2)
        							{
        								Bukkit.getScheduler().cancelTask(getId());
        								clearRuns();
            							dB.echoApproval("Finished task for " + entity.getType().name());
        							}
        						}
        						else
        						{
        							Bukkit.getScheduler().cancelTask(getId());
        							clearRuns();
        							dB.echoApproval("Finished task for " + entity.getType().name());
        						}
        					}
        				};
        				
        task.setId(Bukkit.getScheduler().scheduleSyncRepeatingTask(denizen, task, 2, 2));
        	
        }
    

}