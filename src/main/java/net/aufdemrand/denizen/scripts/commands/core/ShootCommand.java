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

            } else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
        }

        if (entityType == null) throw new InvalidArgumentsException(Messages.ERROR_INVALID_ENTITY);

        // Stash objects
        scriptEntry.addObject("location", location);
        scriptEntry.addObject("entityType", entityType);
        scriptEntry.addObject("qty", qty);
    }
    
    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        // Get objects
        Location location = (Location) scriptEntry.getObject("location");
        Integer qty = (Integer) scriptEntry.getObject("qty");
        EntityType entityType = (EntityType) scriptEntry.getObject("entityType");
        
        //Vector direction = scriptEntry.getNPC().getEyeLocation().getDirection().multiply(2.5);

        // Set quantity if not specified
        if (qty != null && entityType != null)
            qty = 1;
        else qty = 1;
        
        Entity entity = scriptEntry.getNPC().getWorld().spawnEntity(
    			scriptEntry.getNPC().getEyeLocation(), entityType);
        long ldelay = (long) 20;
        
        Runnable3 task = new Runnable3<ScriptEntry, Entity, Location>
        				(scriptEntry, entity, location)
        				{
        					@Override
        					public void run(ScriptEntry scriptEntry, Entity entity, Location location) {
    	        
        						Vector v1 = entity.getLocation().toVector().clone();
        						Vector v2 = location.toVector().clone();
        						Vector v3 = v2.clone().subtract(v1).normalize().multiply(3);
        						entity.setVelocity(v3);
                
        						dB.echoApproval("Running task for time " + getRuns() + " of " + getId());
        						addRuns();
        						
        						if (getRuns() > 10)
        							Bukkit.getScheduler().cancelTask(getId());
        					}
        				};
        				
        task.setId(Bukkit.getScheduler().scheduleSyncRepeatingTask(denizen, task, 10, 10));
        	
        }
    

}