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
import net.aufdemrand.denizen.utilities.Conversion;
import net.aufdemrand.denizen.utilities.Velocity;
import net.aufdemrand.denizen.utilities.entity.Gravity;
import net.aufdemrand.denizen.utilities.entity.Position;
import net.aufdemrand.denizen.utilities.entity.Rotation;

import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 * Shoots an entity through the air up to a certain height, optionally using a custom gravity value and triggering a script on impact with a surface.
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
                else
                    dB.echoError("Ignoring unrecognized argument: " + arg.raw_value);
            }

            else if (!scriptEntry.hasObject("destination")
                     && arg.matchesArgumentType(dLocation.class)
                     && arg.matchesPrefix("destination, d")) {

                scriptEntry.addObject("destination", arg.asType(dLocation.class));
            }

            else if (!scriptEntry.hasObject("height")
                     && arg.matchesPrimitive(aH.PrimitiveType.Double)
                     && arg.matchesPrefix("height, h")) {

                scriptEntry.addObject("height", arg.asElement());
            }

            else if (!scriptEntry.hasObject("speed")
                    && arg.matchesPrimitive(aH.PrimitiveType.Double)
                    && arg.matchesPrefix("speed")) {

                scriptEntry.addObject("speed", arg.asElement());
            }

            else if (!scriptEntry.hasObject("script")
                     && arg.matchesArgumentType(dScript.class)) {

                scriptEntry.addObject("script", arg.asType(dScript.class));
            }

            else if (!scriptEntry.hasObject("shooter")
                    && arg.matchesArgumentType(dEntity.class)) {
                scriptEntry.addObject("shooter", arg.asType(dEntity.class));
            }

            else if (!scriptEntry.hasObject("entities")
                     && arg.matchesArgumentList(dEntity.class)) {

                scriptEntry.addObject("entities", arg.asType(dList.class).filter(dEntity.class));
            }

            // Don't document this argument; it is for debug purposes only
            else if (!scriptEntry.hasObject("gravity")
                     && arg.matchesPrimitive(aH.PrimitiveType.Double)
                     && arg.matchesPrefix("gravity, g")) {

                scriptEntry.addObject("gravity", arg.asElement());
            }

            else arg.reportUnhandled();
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
            throw new InvalidArgumentsException("Must specify entity/entities!");

        if (!scriptEntry.hasObject("originEntity") && !scriptEntry.hasObject("originLocation"))
            throw new InvalidArgumentsException("Must specify an origin location!");
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

        // If there is no destination set, but there is a shooter, get a point
        // in front of the shooter and set it as the destination
        final dLocation destination = scriptEntry.hasObject("destination") ?
                                      (dLocation) scriptEntry.getObject("destination") :
                                      (originEntity != null ? new dLocation(originEntity.getEyeLocation()
                                                               .add(originEntity.getEyeLocation().getDirection()
                                                               .multiply(30)))
                                                            : null);

        // TODO: Same as PUSH -- is this the place to do this?
        if (destination == null) {
            dB.report(scriptEntry, getName(), "No destination specified!");
            return;
        }

        List<dEntity> entities = (List<dEntity>) scriptEntry.getObject("entities");
        final dScript script = (dScript) scriptEntry.getObject("script");
        dEntity shooter = (dEntity) scriptEntry.getObject("shooter");

        Element height = scriptEntry.getElement("height");
        Element gravity = scriptEntry.getElement("gravity");
        Element speed = scriptEntry.getElement("speed");

        // Report to dB
        dB.report(scriptEntry, getName(), aH.debugObj("origin", originEntity != null ? originEntity : originLocation) +
                             aH.debugObj("entities", entities.toString()) +
                             destination.debug() +
                             height.debug() +
                             (gravity != null ? gravity.debug(): "") +
                             (speed != null ? speed.debug(): "") +
                             (script != null ? script.debug() : "") +
                             (shooter != null ? shooter.debug() : ""));

        // Keep a dList of entities that can be called using <entry[name].shot_entities>
        // later in the script queue

        final dList entityList = new dList();

        // Go through all the entities, spawning/teleporting and rotating them
        for (dEntity entity : entities) {
            entity.spawnAt(originLocation);

            // Only add to entityList after the entities have been
            // spawned, otherwise you'll get something like "e@skeleton"
            // instead of "e@57" on it
            entityList.add(entity.toString());

            Rotation.faceLocation(entity.getBukkitEntity(), destination);

            // If the current entity is a projectile, set its shooter
            // when applicable
            if (entity.isProjectile() && (shooter != null || originEntity != null)) {
                entity.setShooter(shooter != null ? shooter : originEntity);
            }
        }

        // Add entities to context so that the specific entities created/spawned
        // can be fetched.
        scriptEntry.addObject("shot_entities", entityList);

        Position.mount(Conversion.convertEntities(entities));

        // Get the entity at the bottom of the entity list, because
        // only its gravity should be affected and tracked considering
        // that the other entities will be mounted on it
        final dEntity lastEntity = entities.get(entities.size() - 1);

        if (gravity == null) {

            String entityType = lastEntity.getEntityType().name();

            for (Gravity defaultGravity : Gravity.values()) {

                if (defaultGravity.name().equals(entityType)) {

                    gravity = new Element(defaultGravity.getGravity());
                }
            }

            // If the gravity is still null, use a default value
            if (gravity == null) {
                gravity = new Element(0.115);
            }
        }

        if (speed == null) {
            Vector v1 = lastEntity.getLocation().toVector();
            Vector v2 = destination.toVector();
            Vector v3 = Velocity.calculate(v1, v2, gravity.asDouble(), height.asDouble());
            lastEntity.setVelocity(v3);
        }
        else {
            lastEntity.setVelocity(destination.subtract(originLocation).toVector()
                    .normalize().multiply(speed.asDouble()));
        }

        // A task used to trigger a script if the entity is no longer
        // being shot, when the script argument is used
        BukkitRunnable task = new BukkitRunnable() {

            boolean flying = true;
            Vector lastVelocity = null;

            public void run() {

                // If the entity is no longer spawned, stop the task
                if (!lastEntity.isSpawned()) {
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

                    List<ScriptEntry> entries = script.getContainer().getBaseEntries
                            (scriptEntry.getPlayer(),
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
