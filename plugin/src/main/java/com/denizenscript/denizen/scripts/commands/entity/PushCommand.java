package com.denizenscript.denizen.scripts.commands.entity;

import com.denizenscript.denizen.utilities.Conversion;
import com.denizenscript.denizen.utilities.DenizenAPI;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.entity.Position;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.interfaces.BlockHelper;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.ScriptTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.Holdable;
import com.denizenscript.denizencore.scripts.queues.ScriptQueue;
import com.denizenscript.denizencore.scripts.queues.core.InstantQueue;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;

public class PushCommand extends AbstractCommand implements Holdable {

    // <--[command]
    // @Name Push
    // @Syntax push [<entity>|...] (origin:<entity>/<location>) (destination:<location>) (speed:<#.#>) (duration:<duration>) (script:<name>) (def:<element>|...) (force_along) (precision:<#>) (no_rotate) (no_damage) (ignore_collision)
    // @Required 1
    // @Short Pushes entities through the air in a straight line.
    // @Group entity
    //
    // @Description
    // Pushes entities through the air in a straight line at a certain speed and for a certain duration,
    // triggering a script when they hit an obstacle or stop flying. You can specify the script to be run
    // with the (script:<name>) argument, and optionally specify definitions to be available in this script
    // with the (def:<element>|...) argument. Using the 'no_damage' argument causes the entity to receive no damage
    // when they stop moving.
    //
    // Optionally use the "ignore_collision" argument to ignore block collisions.
    //
    // @Tags
    // <EntityTag.velocity>
    //
    // @Usage
    // Use to launch an arrow straight towards a target
    // - push arrow destination:<player.location>
    //
    // @Usage
    // Use to launch an entity into the air
    // - push cow
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (Argument arg : scriptEntry.getProcessedArgs()) {

            if (!scriptEntry.hasObject("origin")
                    && arg.matchesPrefix("origin", "o", "source", "shooter", "s")) {

                if (arg.matchesArgumentType(EntityTag.class)) {
                    scriptEntry.addObject("originEntity", arg.asType(EntityTag.class));
                }
                else if (arg.matchesArgumentType(LocationTag.class)) {
                    scriptEntry.addObject("originLocation", arg.asType(LocationTag.class));
                }
                else {
                    Debug.echoError("Ignoring unrecognized argument: " + arg.raw_value);
                }
            }
            else if (!scriptEntry.hasObject("destination")
                    && arg.matchesArgumentType(LocationTag.class)
                    && arg.matchesPrefix("destination", "d")) {

                scriptEntry.addObject("destination", arg.asType(LocationTag.class));
            }
            else if (!scriptEntry.hasObject("duration")
                    && arg.matchesArgumentType(DurationTag.class)
                    && arg.matchesPrefix("duration", "d")) {

                scriptEntry.addObject("duration", arg.asType(DurationTag.class));
            }
            else if (!scriptEntry.hasObject("speed")
                    && arg.matchesPrimitive(ArgumentHelper.PrimitiveType.Double)
                    && arg.matchesPrefix("speed", "s")) {

                scriptEntry.addObject("speed", arg.asElement());
            }
            else if (!scriptEntry.hasObject("script")
                    && (arg.matchesArgumentType(ScriptTag.class)
                    || arg.matchesPrefix("script"))) {
                scriptEntry.addObject("script", arg.asType(ScriptTag.class));
            }
            else if (!scriptEntry.hasObject("entities")
                    && arg.matchesArgumentList(EntityTag.class)) {

                scriptEntry.addObject("entities", arg.asType(ListTag.class).filter(EntityTag.class, scriptEntry));
            }
            else if (!scriptEntry.hasObject("force_along")
                    && arg.matches("force_along")) {
                scriptEntry.addObject("force_along", new ElementTag(true));
            }
            else if (!scriptEntry.hasObject("no_rotate")
                    && arg.matches("no_rotate")) {
                scriptEntry.addObject("no_rotate", new ElementTag(true));
            }
            else if (!scriptEntry.hasObject("precision")
                    && arg.matchesPrefix("precision")) {
                scriptEntry.addObject("precision", arg.asElement());
            }
            else if (!scriptEntry.hasObject("no_damage")
                    && arg.matches("no_damage")) {
                scriptEntry.addObject("no_damage", new ElementTag(true));
            }
            else if (!scriptEntry.hasObject("ignore_collision")
                    && arg.matches("ignore_collision")) {
                scriptEntry.addObject("ignore_collision", new ElementTag(true));
            }
            else if (arg.matchesPrefix("def", "define", "context")) {
                scriptEntry.addObject("definitions", arg.asType(ListTag.class));
            }
            else {
                arg.reportUnhandled();
            }
        }

        // Use the NPC or player's locations as the origin if one is not specified

        if (!scriptEntry.hasObject("originlocation")) {

            scriptEntry.defaultObject("originentity",
                    Utilities.entryHasNPC(scriptEntry) ? Utilities.getEntryNPC(scriptEntry).getDenizenEntity() : null,
                    Utilities.entryHasPlayer(scriptEntry) ? Utilities.getEntryPlayer(scriptEntry).getDenizenEntity() : null);
        }

        scriptEntry.defaultObject("speed", new ElementTag(1.5));
        scriptEntry.defaultObject("duration", new DurationTag(20));
        scriptEntry.defaultObject("force_along", new ElementTag(false));
        scriptEntry.defaultObject("precision", new ElementTag(2));

        // Check to make sure required arguments have been filled

        if (!scriptEntry.hasObject("entities")) {
            throw new InvalidArgumentsException("Must specify entity/entities!");
        }

        if (!scriptEntry.hasObject("originentity") && !scriptEntry.hasObject("originlocation")) {
            throw new InvalidArgumentsException("Must specify an origin location!");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(final ScriptEntry scriptEntry) {

        EntityTag originEntity = (EntityTag) scriptEntry.getObject("originentity");
        LocationTag originLocation = scriptEntry.hasObject("originlocation") ?
                (LocationTag) scriptEntry.getObject("originlocation") :
                new LocationTag(originEntity.getEyeLocation()
                        .add(originEntity.getEyeLocation().getDirection())
                        .subtract(0, 0.4, 0));
        boolean no_rotate = scriptEntry.hasObject("no_rotate") && scriptEntry.getElement("no_rotate").asBoolean();
        final boolean no_damage = scriptEntry.hasObject("no_damage") && scriptEntry.getElement("no_damage").asBoolean();

        // If there is no destination set, but there is a shooter, get a point
        // in front of the shooter and set it as the destination
        final LocationTag destination = scriptEntry.hasObject("destination") ?
                (LocationTag) scriptEntry.getObject("destination") :
                (originEntity != null ? new LocationTag(originEntity.getEyeLocation()
                        .add(originEntity.getEyeLocation().getDirection()
                                .multiply(30)))
                        : null);

        // TODO: Should this be checked in argument parsing?
        if (destination == null) {
            if (scriptEntry.dbCallShouldDebug()) {
                Debug.report(scriptEntry, getName(), "No destination specified!");
            }
            scriptEntry.setFinished(true);
            return;
        }

        List<EntityTag> entities = (List<EntityTag>) scriptEntry.getObject("entities");
        final ScriptTag script = (ScriptTag) scriptEntry.getObject("script");
        final ListTag definitions = (ListTag) scriptEntry.getObject("definitions");

        final double speed = scriptEntry.getElement("speed").asDouble();
        final int maxTicks = ((DurationTag) scriptEntry.getObject("duration")).getTicksAsInt();

        ElementTag force_along = scriptEntry.getElement("force_along");
        ElementTag precision = scriptEntry.getElement("precision");
        ElementTag ignore_collision = scriptEntry.getElement("ignore_collision");
        final boolean ignoreCollision = ignore_collision != null && ignore_collision.asBoolean();

        // Report to dB
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), ArgumentHelper.debugObj("origin", originEntity != null ? originEntity : originLocation) +
                    ArgumentHelper.debugObj("entities", entities.toString()) +
                    ArgumentHelper.debugObj("destination", destination) +
                    ArgumentHelper.debugObj("speed", speed) +
                    ArgumentHelper.debugObj("max ticks", maxTicks) +
                    (script != null ? script.debug() : "") +
                    force_along.debug() +
                    precision.debug() +
                    (no_rotate ? ArgumentHelper.debugObj("no_rotate", "true") : "") +
                    (no_damage ? ArgumentHelper.debugObj("no_damage", "true") : "") +
                    (ignore_collision != null ? ignore_collision.debug() : "") +
                    (definitions != null ? definitions.debug() : ""));
        }

        final boolean forceAlong = force_along.asBoolean();

        // Keep a ListTag of entities that can be called using <entry[name].pushed_entities>
        // later in the script queue
        final ListTag entityList = new ListTag();

        // Go through all the entities, spawning/teleporting and rotating them
        for (EntityTag entity : entities) {
            entity.spawnAt(originLocation);

            // Only add to entityList after the entities have been
            // spawned, otherwise you'll get something like "e@skeleton"
            // instead of "e@57" on it
            entityList.add(entity.toString());

            if (!no_rotate) {
                NMSHandler.getEntityHelper().faceLocation(entity.getBukkitEntity(), destination);
            }

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
        final EntityTag lastEntity = entities.get(entities.size() - 1);

        final Vector v2 = destination.toVector();
        final Vector Origin = originLocation.toVector();

        final int prec = precision.asInt();

        BukkitRunnable task = new BukkitRunnable() {
            int runs = 0;
            LocationTag lastLocation;

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
                    BlockHelper blockHelper = NMSHandler.getBlockHelper();
                    if (!ignoreCollision && (!blockHelper.isSafeBlock(lastEntity.getLocation().add(v3).getBlock().getType())
                            || !blockHelper.isSafeBlock(lastEntity.getLocation().add(newVel).getBlock().getType()))) {
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
                        ScriptQueue queue = new InstantQueue(script.getContainer().getName())
                                .addEntries(entries);
                        if (lastEntity.getLocation() != null) {
                            queue.addDefinition("location", lastEntity.getLocation().identify());
                        }
                        else {
                            queue.addDefinition("location", lastLocation.identify());
                        }
                        queue.addDefinition("pushed_entities", entityList.toString());
                        queue.addDefinition("last_entity", lastEntity.identify());
                        if (definitions != null) {
                            int x = 1;
                            String[] definition_names = null;
                            try {
                                definition_names = script.getContainer().getString("definitions").split("\\|");
                            }
                            catch (Exception e) {
                                // TODO: less lazy handling
                            }
                            for (String definition : definitions) {
                                String name = definition_names != null && definition_names.length >= x ?
                                        definition_names[x - 1].trim() : String.valueOf(x);
                                queue.addDefinition(name, definition);
                                Debug.echoDebug(scriptEntry, "Adding definition %" + name + "% as " + definition);
                                x++;
                            }
                        }
                        queue.start();
                    }
                    scriptEntry.setFinished(true);
                }
            }
        };
        task.runTaskTimer(DenizenAPI.getCurrentInstance(), 0, prec);
    }
}
