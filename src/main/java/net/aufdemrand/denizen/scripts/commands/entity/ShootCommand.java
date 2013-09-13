package net.aufdemrand.denizen.scripts.commands.entity;

import java.util.List;
import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
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
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import net.aufdemrand.denizen.utilities.Conversion;
import net.aufdemrand.denizen.utilities.Velocity;
import net.aufdemrand.denizen.utilities.entity.Gravity;
import net.aufdemrand.denizen.utilities.entity.Position;
import net.aufdemrand.denizen.utilities.entity.Rotation;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 * Shoots an entity like a bow.
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

            else if (!scriptEntry.hasObject("height")
                     && arg.matchesPrimitive(aH.PrimitiveType.Double)
                     && arg.matchesPrefix("height, h")) {

               scriptEntry.addObject("height", arg.asElement());
            }

            else if (!scriptEntry.hasObject("gravity")
                    && arg.matchesPrimitive(aH.PrimitiveType.Double)
                    && arg.matchesPrefix("gravity, g")) {

              scriptEntry.addObject("gravity", arg.asElement());
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

        scriptEntry.defaultObject("height", new Element(3));

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

        double height = ((Element) scriptEntry.getObject("height")).asDouble();
        Element gravity = (Element) scriptEntry.getObject("gravity");

        // Report to dB

        dB.report(getName(), aH.debugObj("origin", originEntity != null ? originEntity : originLocation) +
                             aH.debugObj("entities", entities.toString()) +
                             aH.debugObj("destination", destination) +
                             aH.debugObj("height", height) +
                             aH.debugObj("gravity", gravity) +
                             (script != null ? aH.debugObj("script", script.identify()) : ""));

        // Keep a dList of entities that can be called using %shot_entities%
        // later in the script queue

        final dList entityList = new dList();

        // Go through all the entities, spawning/teleporting and rotating them

        for (dEntity entity : entities) {

            if (!entity.isSpawned()) {
                entity.spawnAt(originLocation);
            }
            else {
                entity.teleport(originLocation);
            }

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

        scriptEntry.addObject("shot_entities", entityList);

        Position.mount(Conversion.convert(entities));

        // Get the entity at the bottom of the entity list, because
        // only its gravity should be affected and tracked considering
        // that the other entities will be mounted on it

        final dEntity lastEntity = entities.get(entities.size() - 1);

        if (gravity == null) {

            String entityType = lastEntity.getEntityType().name();

            for (Gravity defaultGravity : Gravity.values()) {

                if (defaultGravity.name().equals(entityType)) {

                    gravity = new Element(defaultGravity.getGravity());
                    dB.echoApproval("Gravity: " + gravity);
                }
            }

            // If the gravity is still null, use a default value
            if (gravity == null) {
                gravity = new Element(0.115);
            }
        }

        Vector v1 = lastEntity.getLocation().toVector();
        Vector v2 = destination.toVector();
        Vector v3 = Velocity.calculate(v1, v2, gravity.asDouble(), height);

        lastEntity.setVelocity(v3);

        // A task used to trigger a script if the entity is no longer
        // being shot, when the script argument is used

        BukkitRunnable task = new BukkitRunnable() {

            boolean flying = true;
            Vector lastVelocity = null;

            public void run() {

                // If the entity is no longer valid, stop the task

                if (!lastEntity.isValid()) {
                    flying = false;
                }

                // Else, if the entity is no longer traveling through
                // the air, stop the task

                else if (lastVelocity != null) {

                    if (lastVelocity.distance
                            (lastEntity.getBukkitEntity().getVelocity()) < 0.05) {
                        flying = false;
                    }
                }

                // Stop the task and run the script if conditions
                // are met

                if (!flying) {

                    this.cancel();

                    List<ScriptEntry> entries = script.getContainer().getBaseEntries(
                            scriptEntry.getPlayer(),
                            scriptEntry.getNPC());
                    ScriptQueue queue = InstantQueue.getQueue(ScriptQueue._getNextId()).addEntries(entries);
                    queue.addDefinition("location", lastEntity.getLocation().identify());
                    queue.addDefinition("shot_entities", entityList.toString());
                    queue.addDefinition("last_entity", lastEntity.identify());
                    queue.start();
                }
                else {
                    lastVelocity = lastEntity.getVelocity();
                }
            }
        };

        // Run the task above if a script argument was specified

        if (script != null) {

            task.runTaskTimer(denizen, 0, 2);
        }
    }
}
