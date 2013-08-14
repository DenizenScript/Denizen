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
 * Moves entities through the air from an origin to a destination.
 * The origin can optionally be an entity that will look at the
 * object it is moving.
 *
 * @author David Cernat
 */

public class ShootCommand extends AbstractCommand {
    
    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        
        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("origin")
                && arg.matchesPrefix("origin, o, source, shooter, s")) {
                
                if (arg.matchesArgumentType(dEntity.class))
                    scriptEntry.addObject("originEntity", arg.asType(dEntity.class));
                else if (arg.matchesArgumentType(dLocation.class))
                    scriptEntry.addObject("originLocation", arg.asType(dLocation.class));
            }
            
            else if (!scriptEntry.hasObject("entities")
                     && arg.matchesArgumentList(dEntity.class)) {

                scriptEntry.addObject("entities", ((dList) arg.asType(dList.class)).filter(dEntity.class));
            }
            
            else if (!scriptEntry.hasObject("destination")
                     && arg.matchesArgumentType(dLocation.class)) {

                scriptEntry.addObject("destination", arg.asType(dLocation.class));
            }
            
            else if (!scriptEntry.hasObject("duration")
                     && arg.matchesArgumentType(Duration.class)
                     && arg.matchesPrefix("duration, d")) {

                scriptEntry.addObject("duration", arg.asType(Duration.class));
            }
            
            else if (!scriptEntry.hasObject("speed")
                     && arg.matchesPrimitive(aH.PrimitiveType.Double)
                     && arg.matchesPrefix("speed, s")) {

               scriptEntry.addObject("speed", arg.asElement());
           }
            
            else if (!scriptEntry.hasObject("parabola")
                     && arg.matchesPrimitive(aH.PrimitiveType.Double)
                     && arg.matchesPrefix("parabola, p")) {

               scriptEntry.addObject("parabola", arg.asElement());
           }
            
            else if (!scriptEntry.hasObject("script")
                     && arg.matchesArgumentType(dScript.class)) {

                scriptEntry.addObject("script", arg.asType(dScript.class));
            }
        }

        // Use the NPC or player's locations as the origin if one is not specified
        
        if (!scriptEntry.hasObject("originLocation")) {
        
            scriptEntry.defaultObject("originEntity",
                    scriptEntry.hasNPC() ? scriptEntry.getNPC().getDenizenEntity() : null,
                    scriptEntry.hasPlayer() ? scriptEntry.getPlayer().getDenizenEntity() : null);
        }
        
        // Use a default speed of 1.5 if one is not specified
        
        scriptEntry.defaultObject("speed", new Element(1.5));
        scriptEntry.defaultObject("parabola", new Element(0));
        scriptEntry.defaultObject("duration", Duration.valueOf("80t"));
        
        // Check to make sure required arguments have been filled
        
        if (!scriptEntry.hasObject("entities"))
            throw new InvalidArgumentsException(Messages.ERROR_MISSING_OTHER, "entities");

        if (!scriptEntry.hasObject("originEntity") && !scriptEntry.hasObject("originLocation"))
            throw new InvalidArgumentsException(Messages.ERROR_MISSING_OTHER, "origin");
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void execute(final ScriptEntry scriptEntry) throws CommandExecutionException {
        
        // Get objects
        
        dEntity originEntity = (dEntity) scriptEntry.getObject("originEntity");
        dLocation originLocation = scriptEntry.hasObject("originLocation") ?
                                   (dLocation) scriptEntry.getObject("originLocation") :
                                   new dLocation(originEntity.getEyeLocation()
                                               .add(originEntity.getEyeLocation().getDirection())
                                               .subtract(0, 0.4, 0));

        // If a living entity is doing the shooting, get its LivingEntity
                                   
        LivingEntity shooter = (originEntity != null && originEntity.isLivingEntity()) ? originEntity.getLivingEntity() : null;
        
        final dLocation destination = scriptEntry.hasObject("destination") ?
                                      (dLocation) scriptEntry.getObject("destination") :
                                      new dLocation(shooter.getEyeLocation()
                                                   .add(shooter.getEyeLocation().getDirection()
                                                   .multiply(30)));

        List<dEntity> entities = (List<dEntity>) scriptEntry.getObject("entities");
        final dScript script = (dScript) scriptEntry.getObject("script");
        final double speed = ((Element) scriptEntry.getObject("speed")).asDouble();
        final double parabola = ((Element) scriptEntry.getObject("parabola")).asDouble();
        final int maxTicks = ((Duration) scriptEntry.getObject("duration")).getTicksAsInt() / 2;
        
        // Report to dB
        
        dB.report(getName(), aH.debugObj("origin", originEntity != null ? originEntity : originLocation) +
                             aH.debugObj("entities", entities.toString()) +
                             aH.debugObj("destination", destination) +
                             aH.debugObj("speed", speed) +
                             (script != null ? aH.debugObj("script", script) : ""));
        
        // If the shooter is not a player, always rotate it to face the destination
        // of the projectile, but if the shooter is a player, only rotate him/her
        // if he/she is not looking in the correct general direction
        
        if (shooter != null) {

            if (!originEntity.isPlayer() ||
                Rotation.isFacingLocation(shooter, destination, 45) == false) {

                Rotation.faceLocation(shooter, destination);
            }
        }
        
        // Go through all the entities, spawning/teleporting and rotating them
        for (dEntity entity : entities) {
            
            if (entity.isSpawned() == false) {
                entity.spawnAt(originLocation);
            }
            else {
                entity.teleport(originLocation);
            }
            
            Rotation.faceLocation(entity.getBukkitEntity(), destination);
            
            if (entity.getBukkitEntity() instanceof Projectile && shooter != null) {
                ((Projectile) entity.getBukkitEntity()).setShooter(shooter);
            }
        }
        
        Position.mount(Conversion.convert(entities));
        
        // Only use the last projectile in the task below
        
        final Entity lastEntity = entities.get(entities.size() - 1).getBukkitEntity();
        
        BukkitRunnable task = new BukkitRunnable() {

            int runs = 0;

            public void run() {

                if (runs < maxTicks && lastEntity.isValid()) {
                    
                    Vector v1 = lastEntity.getLocation().toVector();
                    Vector v2 = destination.toVector();
                    Vector v3 = v2.clone().subtract(v1).normalize().multiply(speed);
                                    
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
                        
                    if (script != null) {

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
