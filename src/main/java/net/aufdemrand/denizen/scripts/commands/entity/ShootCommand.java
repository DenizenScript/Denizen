package net.aufdemrand.denizen.scripts.commands.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.Duration;
import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dList;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dScript;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.scripts.containers.core.TaskScriptContainer;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import net.aufdemrand.denizen.utilities.Conversion;
import net.aufdemrand.denizen.utilities.entity.Position;
import net.aufdemrand.denizen.utilities.entity.Rotation;

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
                && arg.matchesPrefix("origin, o, source, shooter, s")) {
                // Entity arg
                scriptEntry.addObject("origin", arg.asType(dEntity.class).setPrefix("entity"));
            }
            
            else if (!scriptEntry.hasObject("entities")
                	 && arg.matchesArgumentList(dEntity.class)) {
                // Entity arg
                scriptEntry.addObject("entities", ((dList) arg.asType(dList.class)).filter(dEntity.class));
            }
            
            else if (!scriptEntry.hasObject("destination")
                     && arg.matchesArgumentType(dLocation.class)) {
                // Location arg
                scriptEntry.addObject("destination", arg.asType(dLocation.class));
            }
        	
            else if (!scriptEntry.hasObject("duration")
                     && arg.matchesArgumentType(Duration.class)
                     && arg.matchesPrefix("duration, d")) {
                // Add value
                scriptEntry.addObject("duration", arg.asType(Duration.class));
            }
        	
            else if (!scriptEntry.hasObject("speed")
                    && arg.matchesPrimitive(aH.PrimitiveType.Double)
                    && arg.matchesPrefix("speed, s")) {
               // Add value
               scriptEntry.addObject("speed", arg.asElement());
           }
        	
            else if (!scriptEntry.hasObject("height")
                    && arg.matchesPrimitive(aH.PrimitiveType.Double)
                    && arg.matchesPrefix("height, h")) {
               // Add value
               scriptEntry.addObject("height", arg.asElement());
           }
        	
            else if (!scriptEntry.hasObject("script")
                     && arg.matchesArgumentType(dScript.class)) {
                // add value
                scriptEntry.addObject("script", arg.asType(dScript.class));
            }
        }

        // Use the NPC or player's locations as the origin if one is not specified
        
        scriptEntry.defaultObject("origin",
				scriptEntry.hasNPC() ? scriptEntry.getNPC().getDenizenEntity() : null,
				scriptEntry.hasPlayer() ? scriptEntry.getPlayer().getDenizenEntity() : null);
        
        // Use a default speed of 1.5 if one is not specified
        
        scriptEntry.defaultObject("speed", new Element(1.5));
        scriptEntry.defaultObject("height", new Element(0));
        scriptEntry.defaultObject("duration", Duration.valueOf("80t"));
        
        // Check to make sure required arguments have been filled
        
        if ((!scriptEntry.hasObject("entities")))
            throw new InvalidArgumentsException(Messages.ERROR_MISSING_OTHER, "entities");

        if ((!scriptEntry.hasObject("origin")))
        	throw new InvalidArgumentsException(Messages.ERROR_MISSING_OTHER, "ORIGIN");
    }
    
	@SuppressWarnings("unchecked")
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

		List<dEntity> entities = (List<dEntity>) scriptEntry.getObject("entities");
		final Element speed = (Element) scriptEntry.getObject("speed");
        final dScript script = (dScript) scriptEntry.getObject("script");
        final double height = ((Element) scriptEntry.getObject("height")).asDouble();
        final int maxTicks = ((Duration) scriptEntry.getObject("duration")).getTicksAsInt() / 2;
        
        // Report to dB
        dB.report(getName(), aH.debugObj("origin", shooter) +
        					 aH.debugObj("entities", entities.toString()) +
        					 aH.debugObj("destination", destination) +
        					 aH.debugObj("speed", speed) +
        					 (script != null ? aH.debugObj("script", script) : ""));
        
        // If the shooter is an NPC, always rotate it to face the destination
        // of the projectile, but if the shooter is a player, only rotate him/her
        // if he/she is not looking in the correct general direction
        
        if (shooter.identify().startsWith("n@") ||
        	Rotation.isFacingLocation(shooterEntity, destination, 45) == false) {
        	
        	Rotation.faceLocation(shooterEntity, destination);
        }
        
        Location origin = shooterEntity.getEyeLocation().add(
				  				 shooterEntity.getEyeLocation().getDirection())
				  				 .subtract(0, 0.4, 0);
        
        // Go through all the entities, spawning/teleporting and rotating them
        for (dEntity entity : entities) {
        	
        	if (entity.isSpawned() == false) {
        		entity.spawnAt(origin);
        	}
        	else {
        		entity.teleport(origin);
        	}
        	
            Rotation.faceLocation(entity.getBukkitEntity(), destination);
            
            if (entity.getBukkitEntity() instanceof Projectile) {
    			((Projectile) entity.getBukkitEntity()).setShooter(shooter.getLivingEntity());
    		}
        }
        
        Position.mount(Conversion.convert(entities));
        
        // Only use the last projectile in the task below
        final Entity lastEntity = entities.get(entities.size() - 1).getBukkitEntity();
        
        BukkitRunnable task = new BukkitRunnable() {

        	int runs = 0;

        	public void run() {

        		if (runs < maxTicks && lastEntity.isValid())
        		{
        			Vector v1 = lastEntity.getLocation().toVector();
        			Vector v2 = destination.toVector();
        			Vector v3 = v2.clone().subtract(v1).normalize().multiply(speed.asDouble());
        							
        			lastEntity.setVelocity(v3);
        			runs++;
        				
        			// Check if the entity is close to its destination
        				
        			if (Math.abs(v2.getX() - v1.getX()) < 2 && Math.abs(v2.getY() - v1.getY()) < 2
        				&& Math.abs(v2.getZ() - v1.getZ()) < 2) {
        			
        					runs = maxTicks;
        			}
        				
        			// Check if the entity has collided with something
        			// using the most basic possible calculation
        				
        			if (lastEntity.getLocation().add(v3).getBlock().getType().toString().equals("AIR") == false) {

        				runs = maxTicks;
        			}
        		}
        		else {

        			this.cancel();
        			runs = 0;
        				
        			if (script != null)
        			{
        				Map<String, String> context = new HashMap<String, String>();
            			context.put("1", lastEntity.getLocation().getX() + "," + lastEntity.getLocation().getY() + "," + lastEntity.getLocation().getZ() + "," + lastEntity.getLocation().getWorld().getName());
            			context.put("2", "e@" + lastEntity.getEntityId());
            			
                        ((TaskScriptContainer) script.getContainer()).setSpeed(new Duration(0))
                           			.runTaskScript(scriptEntry.getPlayer(), scriptEntry.getNPC(), context);
        			}
        				
        		}
        	}
       	};
        
       	task.runTaskTimer(denizen, 0, 2);     
    }

}