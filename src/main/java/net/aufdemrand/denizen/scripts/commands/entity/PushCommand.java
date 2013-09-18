package net.aufdemrand.denizen.scripts.commands.entity;

import java.util.List;

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
import net.aufdemrand.denizen.scripts.queues.ScriptQueue;
import net.aufdemrand.denizen.scripts.queues.core.InstantQueue;
import net.aufdemrand.denizen.utilities.Conversion;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import net.aufdemrand.denizen.utilities.entity.Position;
import net.aufdemrand.denizen.utilities.entity.Rotation;

import org.bukkit.Material;
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

public class PushCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("origin")
                && arg.matchesPrefix("origin, o, source, shooter, s")) {

                if (arg.matchesArgumentType(dEntity.class))
                    scriptEntry.addObject("originEntity", arg.asType(dEntity.class));
                else if (arg.matchesArgumentType(dLocation.class))
                    scriptEntry.addObject("originLocation", arg.asType(dLocation.class));
                else
                    dB.echoError("Ignoring unrecognized argument: " + arg.raw_value);
            }

            else if (!scriptEntry.hasObject("destination")
                     && arg.matchesArgumentType(dLocation.class)
                     && arg.matchesPrefix("destination, d")) {

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

            else if (!scriptEntry.hasObject("script")
                     && arg.matchesArgumentType(dScript.class)) {

                scriptEntry.addObject("script", arg.asType(dScript.class));
            }

            else if (!scriptEntry.hasObject("entities")
                     && arg.matchesArgumentList(dEntity.class)) {

                scriptEntry.addObject("entities", ((dList) arg.asType(dList.class)).filter(dEntity.class));
            }

            else
                dB.echoError("Ignoring unrecognized argument: " + arg.raw_value);
        }

        // Use the NPC or player's locations as the origin if one is not specified

        if (!scriptEntry.hasObject("originLocation")) {

            scriptEntry.defaultObject("originEntity",
                    scriptEntry.hasNPC() ? scriptEntry.getNPC().getDenizenEntity() : null,
                    scriptEntry.hasPlayer() ? scriptEntry.getPlayer().getDenizenEntity() : null);
        }

        scriptEntry.defaultObject("speed", new Element(1.5));
        scriptEntry.defaultObject("duration", new Duration(20));

        // Check to make sure required arguments have been filled

        if (!scriptEntry.hasObject("entities"))
            throw new InvalidArgumentsException(Messages.ERROR_MISSING_OTHER, "entities");

        if (!scriptEntry.hasObject("originEntity") && !scriptEntry.hasObject("originLocation"))
            throw new InvalidArgumentsException(Messages.ERROR_MISSING_OTHER, "origin");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(final ScriptEntry scriptEntry) throws CommandExecutionException {

        dEntity originEntity = (dEntity) scriptEntry.getObject("originEntity");
        dLocation originLocation = scriptEntry.hasObject("originLocation") ?
                                   (dLocation) scriptEntry.getObject("originLocation") :
                                   new dLocation(originEntity.getEyeLocation()
                                               .add(originEntity.getEyeLocation().getDirection())
                                               .subtract(0, 0.4, 0));

        // If a living entity is doing the shooting, get its LivingEntity
        LivingEntity shooter = (originEntity != null && originEntity.isLivingEntity()) ? originEntity.getLivingEntity() : null;

        // If there is no destination set, but there is a shooter, get a point
        // in front of the shooter and set it as the destination

        final dLocation destination = scriptEntry.hasObject("destination") ?
                                      (dLocation) scriptEntry.getObject("destination") :
                                      (shooter != null ? new dLocation(shooter.getEyeLocation()
                                                               .add(shooter.getEyeLocation().getDirection()
                                                               .multiply(30)))
                                                       : null);

        if (destination == null) {
            dB.report(getName(), "No destination specified!");
            return;
        }

        List<dEntity> entities = (List<dEntity>) scriptEntry.getObject("entities");
        final dScript script = (dScript) scriptEntry.getObject("script");

        final double speed = scriptEntry.getElement("speed").asDouble();
        final int maxTicks = ((Duration) scriptEntry.getObject("duration")).getTicksAsInt() / 2;

        // Report to dB
        dB.report(getName(), aH.debugObj("origin", shooter) +
                             aH.debugObj("entities", entities.toString()) +
                             aH.debugObj("destination", destination) +
                             aH.debugObj("speed", speed) +
                             aH.debugObj("max ticks", maxTicks) +
                             (script != null ? aH.debugObj("script", script) : ""));

        // If the shooter is an NPC, always rotate it to face the destination
        // of the projectile, but if the shooter is a player, only rotate him/her
        // if he/she is not looking in the correct general direction

        if (shooter != null && (originEntity.isNPC() || !Rotation.isFacingLocation(shooter, destination, 45)))
                Rotation.faceLocation(shooter, destination);

        // Keep a dList of entities that can be called using <entry[name].pushed_entities>
        // later in the script queue

        final dList entityList = new dList();

        // Go through all the entities, spawning/teleporting and rotating them
        for (dEntity entity : entities) {

            if (!entity.isSpawned()) entity.spawnAt(originLocation);
            else                     entity.teleport(originLocation);

            // Only add to entityList after the entities have been
            // spawned, otherwise you'll get something like "e@skeleton"
            // instead of "e@57" on it

            entityList.add(entity.toString());

            Rotation.faceLocation(entity.getBukkitEntity(), destination);

            // If the current entity is a projectile, set its shooter
            // when applicable

            if (entity.getBukkitEntity() instanceof Projectile && shooter != null) {
                ((Projectile) entity.getBukkitEntity()).setShooter(shooter);
            }
        }

        // Add entities to context so that the specific entities created/spawned
        // can be fetched.
        scriptEntry.addObject("pushed_entities", entityList);

        Position.mount(Conversion.convert(entities));

        // Get the entity at the bottom of the entity list, because
        // only its gravity should be affected and tracked considering
        // that the other entities will be mounted on it

        final dEntity lastEntity = entities.get(entities.size() - 1);

        final Vector v2 = destination.toVector();

        BukkitRunnable task = new BukkitRunnable() {
            int runs = 0;
            @Override
            public void run() {

                if (runs < maxTicks && lastEntity.isValid()) {

                    Vector v1 = lastEntity.getLocation().toVector();
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
                    if (lastEntity.getLocation().add(v3).getBlock().getType() != Material.AIR) {
                        runs = maxTicks;
                    }
                }
                else {
                    this.cancel();

                    if (script != null) {

                        List<ScriptEntry> entries = script.getContainer().getBaseEntries(
                                scriptEntry.getPlayer(),
                                scriptEntry.getNPC());
                        ScriptQueue queue = InstantQueue.getQueue(ScriptQueue._getNextId()).addEntries(entries);
                        queue.addDefinition("location", lastEntity.getLocation().identify());
                        queue.addDefinition("pushed_entities", entityList.toString());
                        queue.addDefinition("last_entity", lastEntity.identify());
                        queue.start();
                    }
                }
            }
        };
        task.runTaskTimer(denizen, 0, 2);
    }
}
