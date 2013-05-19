package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.Settings;
import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.scripts.containers.core.TaskScriptContainer;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.utilities.arguments.Duration;
import net.aufdemrand.denizen.utilities.arguments.dLocation;
import net.aufdemrand.denizen.utilities.arguments.Script;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.arguments.aH.ArgumentType;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.meta.FireworkMeta;
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

        // Initialize necessary fields
        EntityType entityType = null;
        //Integer qty = null;
        dLocation location = null;
        Script newScript = null;
        Boolean ride = false;
        Boolean burn = false;
        double explosion = -1;
        Boolean fireworks = false;

        // Set some defaults
        if (scriptEntry.getPlayer() != null)
            location = new dLocation(scriptEntry.getPlayer().getLocation());
        if (location == null && scriptEntry.getNPC() != null)
            location = new dLocation(scriptEntry.getNPC().getLocation());

        for (String arg : scriptEntry.getArguments()) {
            if (aH.matchesEntityType(arg)) {
                entityType = aH.getEntityFrom(arg);
                dB.echoDebug("...entity set to '%s'.", arg);

            } else if (aH.matchesLocation(arg)) {
                location = aH.getLocationFrom(arg);
                dB.echoDebug("...location set to '%s'.", arg);
                
            } else if (aH.matchesScript(arg)) {
				newScript = aH.getScriptFrom(arg);
				dB.echoDebug(Messages.DEBUG_SET_SCRIPT, arg);

            } else if (aH.matchesArg("ride, mount", arg)) {
                ride = true;
                dB.echoDebug("...will be mounted.");
                
            } else if (aH.matchesArg("burn, burning", arg)) {
                burn = true;
                dB.echoDebug("...will burn.");
               
            } else if (aH.matchesValueArg("explosion", arg, ArgumentType.Double)) {
            	explosion = aH.getDoubleFrom(arg);
                dB.echoDebug("...will have an explosion radius of " + explosion);
                
            } else if (aH.matchesArg("fireworks", arg)) {
                fireworks = true;
                dB.echoDebug("...will launch fireworks.");

            } else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
        }
        
        if (entityType == null) throw new InvalidArgumentsException(Messages.ERROR_INVALID_ENTITY);

        // Stash objects
        scriptEntry.addObject("location", location);
        scriptEntry.addObject("script", newScript);
        scriptEntry.addObject("entityType", entityType);
        scriptEntry.addObject("ride", ride);
        scriptEntry.addObject("burn", burn);
        scriptEntry.addObject("explosion", explosion);
        scriptEntry.addObject("fireworks", fireworks);
    }
    
	@Override
    public void execute(final ScriptEntry scriptEntry) throws CommandExecutionException {
        // Get objects
    	
        final dLocation location = scriptEntry.hasObject("location") ? (dLocation) scriptEntry.getObject("location") : (dLocation) scriptEntry.getNPC().getEyeLocation().getDirection().
                multiply(4).toLocation(scriptEntry.getNPC().getWorld());
        EntityType entityType = (EntityType) scriptEntry.getObject("entityType");
        Boolean ride = (Boolean) scriptEntry.getObject("ride");
        Boolean burn = (Boolean) scriptEntry.getObject("burn");
        
        if (location == null)
        {
        	
        }
        else
        {
        	Utilities.faceLocation(scriptEntry.getNPC().getCitizen().getBukkitEntity(), location);
        }

        final Entity entity = scriptEntry.getNPC().getWorld().spawnEntity(
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
        
        BukkitRunnable task = new BukkitRunnable()
        	{
                int runs = 0;
        		@Override
        		public void run() {
    	        				
        			if (runs < 40 && entity.isValid())
        			{
        				//dB.echoDebug(entity.getType().name() + " flying time " + getRuns() + " in task " + getId());

        				Vector v1 = entity.getLocation().toVector().clone();
        				Vector v2 = location.toVector().clone();
        				Vector v3 = v2.clone().subtract(v1).normalize().multiply(1.5);
        							
        				entity.setVelocity(v3);
        				runs++;
        						
        				if (Math.abs(v2.getX() - v1.getX()) < 2 && Math.abs(v2.getY() - v1.getY()) < 2
        				&& Math.abs(v2.getZ() - v1.getZ()) < 2)
        				{
        					runs = 40;
        				}
        			}
        			else
        			{
        				this.cancel();
        				runs = 0;
        				
        				if (scriptEntry.getObject("script") != null)
        				{
                            ((TaskScriptContainer) ((Script) scriptEntry.getObject("script")).
                            		getContainer()).setSpeed(new Duration(
                            		Duration.valueOf(Settings.ScriptQueueSpeed()).getSeconds()))
                            		.runTaskScript(scriptEntry.getPlayer(), scriptEntry.getNPC(), null);
        				}
        				if ((Boolean) scriptEntry.getObject("fireworks"))
        				{
        					Firework firework = entity.getWorld().spawn(entity.getLocation(), Firework.class);
        			        FireworkMeta fireworkMeta = (FireworkMeta) firework.getFireworkMeta();
        			        fireworkMeta.addEffects(FireworkEffect.builder().withColor(Color.YELLOW).with(Type.STAR).build());
        			        fireworkMeta.setPower(2);
        			        firework.setFireworkMeta(fireworkMeta);
        				}
        				if ((Double) scriptEntry.getObject("explosion") > 0)
        				{
        					entity.getWorld().createExplosion(entity.getLocation(),
        							(Float) scriptEntry.getObject("explosion"));
        				}
        				
        			}
        		}
       		};
        
       	task.runTaskTimer(denizen, 0, 2);     
    }

}