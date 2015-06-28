package net.aufdemrand.denizen.scripts.commands.entity;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.utilities.Conversion;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.blocks.SafeBlock;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.entity.Position;
import net.aufdemrand.denizen.utilities.entity.Rotation;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.*;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import net.aufdemrand.denizencore.scripts.commands.Holdable;
import net.aufdemrand.denizencore.scripts.queues.ScriptQueue;
import net.aufdemrand.denizencore.scripts.queues.core.InstantQueue;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;

public class PushCommand extends AbstractCommand implements Holdable {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("origin")
                    && arg.matchesPrefix("origin", "o", "source", "shooter", "s")) {

                if (arg.matchesArgumentType(dEntity.class))
                    scriptEntry.addObject("originEntity", arg.asType(dEntity.class));
                else if (arg.matchesArgumentType(dLocation.class))
                    scriptEntry.addObject("originLocation", arg.asType(dLocation.class));
                else
                    dB.echoError("Ignoring unrecognized argument: " + arg.raw_value);
            }

            else if (!scriptEntry.hasObject("destination")
                    && arg.matchesArgumentType(dLocation.class)
                    && arg.matchesPrefix("destination", "d")) {

                scriptEntry.addObject("destination", arg.asType(dLocation.class));
            }

            else if (!scriptEntry.hasObject("duration")
                    && arg.matchesArgumentType(Duration.class)
                    && arg.matchesPrefix("duration", "d")) {

                scriptEntry.addObject("duration", arg.asType(Duration.class));
            }

            else if (!scriptEntry.hasObject("speed")
                    && arg.matchesPrimitive(aH.PrimitiveType.Double)
                    && arg.matchesPrefix("speed", "s")) {

                scriptEntry.addObject("speed", arg.asElement());
            }

            else if (!scriptEntry.hasObject("script")
                    && arg.matchesArgumentType(dScript.class)) {

                scriptEntry.addObject("script", arg.asType(dScript.class));
            }

            else if (!scriptEntry.hasObject("entities")
                    && arg.matchesArgumentList(dEntity.class)) {

                scriptEntry.addObject("entities", arg.asType(dList.class).filter(dEntity.class));
            }

            else if (!scriptEntry.hasObject("force_along")
                    && arg.matches("force_along")) {
                scriptEntry.addObject("force_along", new Element(true));
            }

            else if (!scriptEntry.hasObject("no_rotate")
                    && arg.matches("no_rotate")) {
                scriptEntry.addObject("no_rotate", new Element(true));
            }

            else if (!scriptEntry.hasObject("precision")
                    && arg.matchesPrefix("precision")) {
                scriptEntry.addObject("precision", arg.asElement());
            }

            else if (!scriptEntry.hasObject("no_damage")
                    && arg.matches("no_damage")) {
                scriptEntry.addObject("no_damage", new Element(true));
            }

            else arg.reportUnhandled();
        }

        // Use the NPC or player's locations as the origin if one is not specified

        if (!scriptEntry.hasObject("originLocation")) {

            scriptEntry.defaultObject("originEntity",
                    ((BukkitScriptEntryData) scriptEntry.entryData).hasNPC() ? ((BukkitScriptEntryData) scriptEntry.entryData).getNPC().getDenizenEntity() : null,
                    ((BukkitScriptEntryData) scriptEntry.entryData).hasPlayer() ? ((BukkitScriptEntryData) scriptEntry.entryData).getPlayer().getDenizenEntity() : null);
        }

        scriptEntry.defaultObject("speed", new Element(1.5));
        scriptEntry.defaultObject("duration", new Duration(20));
        scriptEntry.defaultObject("force_along", new Element(false));
        scriptEntry.defaultObject("precision", new Element(2));

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
        boolean no_rotate = scriptEntry.hasObject("no_rotate") && scriptEntry.getElement("no_rotate").asBoolean();
        final boolean no_damage = scriptEntry.hasObject("no_damage") && scriptEntry.getElement("no_damage").asBoolean();

        // If there is no destination set, but there is a shooter, get a point
        // in front of the shooter and set it as the destination
        final dLocation destination = scriptEntry.hasObject("destination") ?
                (dLocation) scriptEntry.getObject("destination") :
                (originEntity != null ? new dLocation(originEntity.getEyeLocation()
                        .add(originEntity.getEyeLocation().getDirection()
                                .multiply(30)))
                        : null);

        // TODO: Should this be checked in argument parsing?
        if (destination == null) {
            dB.report(scriptEntry, getName(), "No destination specified!");
            scriptEntry.setFinished(true);
            return;
        }

        List<dEntity> entities = (List<dEntity>) scriptEntry.getObject("entities");
        final dScript script = (dScript) scriptEntry.getObject("script");

        final double speed = scriptEntry.getElement("speed").asDouble();
        final int maxTicks = ((Duration) scriptEntry.getObject("duration")).getTicksAsInt();

        Element force_along = scriptEntry.getElement("force_along");

        Element precision = scriptEntry.getElement("precision");

        // Report to dB
        dB.report(scriptEntry, getName(), aH.debugObj("origin", originEntity != null ? originEntity : originLocation) +
                aH.debugObj("entities", entities.toString()) +
                aH.debugObj("destination", destination) +
                aH.debugObj("speed", speed) +
                aH.debugObj("max ticks", maxTicks) +
                (script != null ? script.debug() : "") +
                force_along.debug() +
                precision.debug() +
                (no_rotate ? aH.debugObj("no_rotate", "true") : "") +
                (no_damage ? aH.debugObj("no_damage", "true") : ""));

        final boolean forceAlong = force_along.asBoolean();

        // Keep a dList of entities that can be called using <entry[name].pushed_entities>
        // later in the script queue
        final dList entityList = new dList();

        // Go through all the entities, spawning/teleporting and rotating them
        for (dEntity entity : entities) {
            entity.spawnAt(originLocation);

            // Only add to entityList after the entities have been
            // spawned, otherwise you'll get something like "e@skeleton"
            // instead of "e@57" on it
            entityList.add(entity.toString());

            if (!no_rotate)
                Rotation.faceLocation(entity.getBukkitEntity(), destination);

            // If the current entity is a projectile, set its shooter
            // when applicable
            if (entity.isProjectile() && originEntity != null) {
                entity.setShooter(originEntity);
            }
        }

        // Add entities to context so that the specific entities created/spawned
        // can be fetched.
        scriptEntry.addObject("pushed_entities", entityList);

        Position.mount(Conversion.convertEntities(entities));

        // Get the entity at the bottom of the entity list, because
        // only its gravity should be affected and tracked considering
        // that the other entities will be mounted on it
        final dEntity lastEntity = entities.get(entities.size() - 1);

        final Vector v2 = destination.toVector();
        final Vector Origin = originLocation.toVector();

        final int prec = precision.asInt();

        BukkitRunnable task = new BukkitRunnable() {
            int runs = 0;
            dLocation lastLocation;

            @Override
            public void run() {

                if (runs < maxTicks && lastEntity.isValid()) {

                    Vector v1 = lastEntity.getLocation().toVector();
                    Vector v3 = v2.clone().subtract(v1).normalize();
                    Vector newVel = v3.multiply(speed);

                    lastEntity.setVelocity(newVel);

                    if (forceAlong) {
                        Vector newDest = v2.clone().subtract(Origin).normalize().multiply(runs / 20).add(Origin);
                        lastEntity.teleport(new Location(lastEntity.getLocation().getWorld(),
                                newDest.getX(), newDest.getY(), newDest.getZ(),
                                lastEntity.getLocation().getYaw(), lastEntity.getLocation().getPitch()));
                    }

                    runs += prec;

                    // Check if the entity is close to its destination
                    if (Math.abs(v2.getX() - v1.getX()) < 1.5f && Math.abs(v2.getY() - v1.getY()) < 1.5f
                            && Math.abs(v2.getZ() - v1.getZ()) < 1.5f) {
                        runs = maxTicks;
                    }

                    // Check if the entity has collided with something
                    // using the most basic possible calculation
                    if (!SafeBlock.blockIsSafe(lastEntity.getLocation().add(v3).getBlock().getType())
                            || !SafeBlock.blockIsSafe(lastEntity.getLocation().add(newVel).getBlock().getType())) {
                        runs = maxTicks;
                    }

                    if (no_damage && lastEntity.isLivingEntity()) {
                        lastEntity.getLivingEntity().setFallDistance(0);
                    }

                    // Record the location in case the entity gets lost (EG, if a pushed arrow hits a mob)
                    lastLocation = lastEntity.getLocation();
                }
                else {
                    this.cancel();

                    if (script != null) {

                        List<ScriptEntry> entries = script.getContainer().getBaseEntries(scriptEntry.entryData.clone());
                        ScriptQueue queue = InstantQueue.getQueue(ScriptQueue.getNextId(script.getContainer().getName()))
                                .addEntries(entries);
                        if (lastEntity.getLocation() != null)
                            queue.addDefinition("location", lastEntity.getLocation().identify());
                        else
                            queue.addDefinition("location", lastLocation.identify());
                        queue.addDefinition("pushed_entities", entityList.toString());
                        queue.addDefinition("last_entity", lastEntity.identify());
                        queue.start();
                    }
                    scriptEntry.setFinished(true);
                }
            }
        };
        task.runTaskTimer(DenizenAPI.getCurrentInstance(), 0, prec);
    }
}
