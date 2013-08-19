package net.aufdemrand.denizen.scripts.commands.entity;

import java.util.Arrays;
import java.util.List;

import net.aufdemrand.denizen.objects.*;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.entity.Rotation;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Controls entity heads.
 * 
 * @author Jeremy Schroeder
 *
 */

public class LookCommand extends AbstractCommand {

    // look (<entity>) [<location>]
    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentType(dLocation.class)) {
                scriptEntry.addObject("location", arg.asType(dLocation.class));
            }

            else if (!scriptEntry.hasObject("duration")
                    && arg.matchesArgumentType(Duration.class)
                    && arg.matchesPrefix("duration, d"))
                scriptEntry.addObject("duration", arg.asType(Duration.class));

            else if (!scriptEntry.hasObject("entities")
                     && arg.matchesArgumentList(dEntity.class)) {
                // Entity arg
                scriptEntry.addObject("entities", ((dList) arg.asType(dList.class)).filter(dEntity.class));
            }
        }

        // Use the NPC or player as the entity if no entities are specified
        
        scriptEntry.defaultObject("entities",
                scriptEntry.hasNPC() ? Arrays.asList(scriptEntry.getNPC().getDenizenEntity()) : null,
                scriptEntry.hasPlayer() ? Arrays.asList(scriptEntry.getPlayer().getDenizenEntity()) : null);
        
        if (!scriptEntry.hasObject("location") || !scriptEntry.hasObject("entities"))
            throw new InvalidArgumentsException("Must specify a location and entity!");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        
        final dLocation loc = (dLocation) scriptEntry.getObject("location");
        final List<dEntity> entities = (List<dEntity>) scriptEntry.getObject("entities");
        final Duration duration = (Duration) scriptEntry.getObject("duration");

        dB.report(getName(), loc.debug() +
                aH.debugObj("entities", entities.toString()));

        for (dEntity entity : entities) {
            if (entity.isSpawned()) {
                Rotation.faceLocation(entity.getBukkitEntity(), loc);
            }
        }
        if (duration != null && duration.getTicks() > 2) {
            BukkitRunnable task = new BukkitRunnable() {
                long bounces = 0;
                public void run() {
                    bounces += 2;
                    if (bounces > duration.getTicks()) {
                        this.cancel();
                        return;
                    }
                    for (dEntity entity : entities) {
                        if (entity.isSpawned()) {
                            Rotation.faceLocation(entity.getBukkitEntity(), loc);
                        }
                    }
                }
            };
            task.runTaskTimer(denizen, 0, 2);
        }
    }


    
    
    /*

        else if (lookWhere.equals("LEFT")) {
            theDenizen.lookClose(false);                        
            theDenizen.getHandle().yaw = theDenizen.getLocation().getYaw() - (float) 80;
            theDenizen.getHandle().az = theDenizen.getHandle().yaw;
        }

        else if (lookWhere.equals("RIGHT")) {
            theDenizen.lookClose(false);
            theDenizen.getHandle().yaw = theDenizen.getLocation().getYaw() + (float) 80;
            theDenizen.getHandle().az = theDenizen.getHandle().yaw;
        }

        else if (lookWhere.equals("UP")) {
            theDenizen.lookClose(false);
            theDenizen.getHandle().pitch = theDenizen.getHandle().pitch - (float) 60;
            theDenizen.getHandle().az = theDenizen.getHandle().yaw;
        }

        else if (lookWhere.equals("DOWN")) {
            theDenizen.lookClose(false);
            theDenizen.getHandle().pitch = theDenizen.getHandle().pitch + (float) 40;
            theDenizen.getHandle().az = theDenizen.getHandle().yaw;
        }

        else if (lookWhere.equals("BACK")) {
            theDenizen.lookClose(false);
            theDenizen.getHandle().yaw = theDenizen.getLocation().getYaw() - 180;
            theDenizen.getHandle().az = theDenizen.getHandle().yaw;            
        }

        else if (lookWhere.equals("SOUTH")) {
            theDenizen.lookClose(false);
            theDenizen.getHandle().yaw = 0;
            theDenizen.getHandle().az = theDenizen.getHandle().yaw;            
        }

        else if (lookWhere.equals("WEST")) {
            theDenizen.lookClose(false);
            theDenizen.getHandle().yaw = 90;
            theDenizen.getHandle().az = theDenizen.getHandle().yaw;            
        }

        else if (lookWhere.equals("NORTH")) {
            theDenizen.lookClose(false);
            theDenizen.getHandle().yaw = 180;
            theDenizen.getHandle().az = theDenizen.getHandle().yaw;            
        }

        else if (lookWhere.equals("EAST")) {
            theDenizen.lookClose(false);
            theDenizen.getHandle().yaw = 270;
            theDenizen.getHandle().az = theDenizen.getHandle().yaw;            
        }

*/
}

