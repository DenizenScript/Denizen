package com.denizenscript.denizen.scripts.commands.entity;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.utilities.Conversion;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.entity.Position;
import com.denizenscript.denizen.nms.NMSHandler;
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
import com.denizenscript.denizencore.scripts.containers.core.TaskScriptContainer;
import com.denizenscript.denizencore.scripts.queues.ScriptQueue;
import com.denizenscript.denizencore.utilities.ScriptUtilities;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.function.Consumer;

public class PushCommand extends AbstractCommand implements Holdable {

    public PushCommand() {
        setName("push");
        setSyntax("push [<entity>|...] (origin:<entity>/<location>) (destination:<location>) (speed:<#.#>) (duration:<duration>) (script:<name>) (def:<element>|...) (force_along) (precision:<#>) (no_rotate) (no_damage) (ignore_collision)");
        setRequiredArguments(1, 12);
        isProcedural = false;
    }

    // <--[command]
    // @Name Push
    // @Syntax push [<entity>|...] (origin:<entity>/<location>) (destination:<location>) (speed:<#.#>) (duration:<duration>) (script:<name>) (def:<element>|...) (force_along) (precision:<#>) (no_rotate) (no_damage) (ignore_collision)
    // @Required 1
    // @Maximum 12
    // @Short Pushes entities through the air in a straight line.
    // @Group entity
    //
    // @Description
    // Pushes entities through the air in a straight line at a certain speed and for a certain duration,
    // triggering a script when they hit an obstacle or stop flying.
    //
    // You must specify an entity to be pushed.
    //
    // Usually, you should specify the origin and the destination. If unspecified, they will be assumed from contextual data.
    //
    // You can specify the script to be run with the (script:<name>) argument,
    // and optionally specify definitions to be available in this script with the (def:<element>|...) argument.
    //
    // Using the 'no_damage' argument causes the entity to receive no damage when they stop moving.
    //
    // Optionally use the "ignore_collision" argument to ignore block collisions.
    //
    // Optionally use "speed:#" to set how fast it should be pushed.
    //
    // Optionally use "force_along" to cause the entity to teleport through any blockage.
    //
    // Optionally use "no_rotate" to prevent entities being rotated at the start of the push.
    //
    // Optionally use "duration:#" to set the max length of time to continue pushing.
    //
    // The push command is ~waitable. Refer to <@link language ~waitable>.
    //
    // @Tags
    // <EntityTag.velocity>
    // <entry[saveName].pushed_entities> returns the list of pushed entities.
    //
    // @Usage
    // Use to launch an arrow straight towards a target.
    // - push arrow destination:<player.location>
    //
    // @Usage
    // Use to launch an entity into the air.
    // - push cow
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("origin")
                    && arg.matchesPrefix("origin", "o", "source", "shooter", "s")) {
                if (arg.matchesArgumentType(EntityTag.class)) {
                    scriptEntry.addObject("origin_entity", arg.asType(EntityTag.class));
                }
                else if (arg.matchesArgumentType(LocationTag.class)) {
                    scriptEntry.addObject("origin_location", arg.asType(LocationTag.class));
                }
                else {
                    Debug.echoError("Ignoring unrecognized argument: " + arg.getRawValue());
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
                    && arg.matchesFloat()
                    && arg.matchesPrefix("speed", "s")) {
                scriptEntry.addObject("speed", arg.asElement());
            }
            else if (!scriptEntry.hasObject("script")
                    && ((arg.matchesArgumentType(ScriptTag.class) && arg.asType(ScriptTag.class).getContainer() instanceof TaskScriptContainer)
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
        if (!scriptEntry.hasObject("origin_location")) {
            scriptEntry.defaultObject("origin_entity", Utilities.entryDefaultEntity(scriptEntry, false));
        }
        scriptEntry.defaultObject("speed", new ElementTag(1.5));
        scriptEntry.defaultObject("duration", new DurationTag(20));
        scriptEntry.defaultObject("force_along", new ElementTag(false));
        scriptEntry.defaultObject("precision", new ElementTag(2));
        if (!scriptEntry.hasObject("entities")) {
            throw new InvalidArgumentsException("Must specify entity/entities!");
        }
        if (!scriptEntry.hasObject("origin_entity") && !scriptEntry.hasObject("origin_location")) {
            throw new InvalidArgumentsException("Must specify an origin location!");
        }
    }

    @Override
    public void execute(final ScriptEntry scriptEntry) {
        EntityTag originEntity = scriptEntry.getObjectTag("origin_entity");
        LocationTag originLocation = scriptEntry.hasObject("origin_location") ?
                (LocationTag) scriptEntry.getObject("origin_location") :
                new LocationTag(originEntity.getEyeLocation()
                        .add(originEntity.getEyeLocation().getDirection())
                        .subtract(0, 0.4, 0));
        boolean no_rotate = scriptEntry.hasObject("no_rotate") && scriptEntry.getElement("no_rotate").asBoolean();
        final boolean no_damage = scriptEntry.hasObject("no_damage") && scriptEntry.getElement("no_damage").asBoolean();
        // If there is no destination set, but there is a shooter, get a point in front of the shooter and set it as the destination
        final LocationTag destination = scriptEntry.hasObject("destination") ?
                (LocationTag) scriptEntry.getObject("destination") :
                (originEntity != null ? new LocationTag(originEntity.getEyeLocation()
                        .add(originEntity.getEyeLocation().getDirection()
                                .multiply(30)))
                        : null);
        // TODO: Should this be checked in argument parsing?
        if (destination == null) {
            Debug.echoError("No destination specified!");
            scriptEntry.setFinished(true);
            return;
        }
        List<EntityTag> entities = (List<EntityTag>) scriptEntry.getObject("entities");
        final ScriptTag script = scriptEntry.getObjectTag("script");
        final ListTag definitions = scriptEntry.getObjectTag("definitions");
        ElementTag speedElement = scriptEntry.getElement("speed");
        DurationTag duration = (DurationTag) scriptEntry.getObject("duration");
        ElementTag force_along = scriptEntry.getElement("force_along");
        ElementTag precision = scriptEntry.getElement("precision");
        ElementTag ignore_collision = scriptEntry.getElement("ignore_collision");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), db("origin", originEntity != null ? originEntity : originLocation),  db("entities", entities),
                    destination, speedElement, duration, script, force_along, precision, (no_rotate ? db("no_rotate", "true") : ""), (no_damage ? db("no_damage", "true") : ""),
                    ignore_collision, definitions);
        }
        final boolean ignoreCollision = ignore_collision != null && ignore_collision.asBoolean();
        final double speed = speedElement.asDouble();
        final int maxTicks = duration.getTicksAsInt();
        final boolean forceAlong = force_along.asBoolean();
        // Keep a ListTag of entities that can be called using <entry[name].pushed_entities> later in the script queue
        final ListTag entityList = new ListTag();
        for (EntityTag entity : entities) {
            entity.spawnAt(originLocation);
            entityList.addObject(entity);
            if (!no_rotate) {
                NMSHandler.entityHelper.faceLocation(entity.getBukkitEntity(), destination);
            }
            if (entity.isProjectile() && originEntity != null) {
                entity.setShooter(originEntity);
            }
        }
        scriptEntry.saveObject("pushed_entities", entityList);
        Position.mount(Conversion.convertEntities(entities));
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
                    if (forceAlong) {
                        Vector newDest = v2.clone().subtract(Origin).normalize().multiply(runs * speed).add(Origin);
                        lastEntity.teleport(new Location(lastEntity.getLocation().getWorld(),
                                newDest.getX(), newDest.getY(), newDest.getZ(),
                                lastEntity.getLocation().getYaw(), lastEntity.getLocation().getPitch()));
                    }
                    runs += prec;
                    // Check if the entity is close to its destination
                    if (Math.abs(v2.getX() - v1.getX()) < 1.5f && Math.abs(v2.getY() - v1.getY()) < 1.5f && Math.abs(v2.getZ() - v1.getZ()) < 1.5f) {
                        runs = maxTicks;
                        return;
                    }
                    Vector newVel = v3.multiply(speed);
                    lastEntity.setVelocity(newVel);
                    if (!ignoreCollision && lastEntity.isValid()) {
                        BoundingBox box = lastEntity.getBukkitEntity().getBoundingBox().expand(newVel);
                        Location ref = lastEntity.getLocation().clone();
                        for (int x = (int) Math.floor(box.getMinX()); x < Math.ceil(box.getMaxX()); x++) {
                            ref.setX(x);
                            for (int y = (int) Math.floor(box.getMinY()); y < Math.ceil(box.getMaxY()); y++) {
                                ref.setY(y);
                                for (int z = (int) Math.floor(box.getMinZ()); z < Math.ceil(box.getMaxZ()); z++) {
                                    ref.setZ(z);
                                    if (!isSafeBlock(ref)) {
                                        runs = maxTicks;
                                    }
                                }
                            }
                        }
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
                        Consumer<ScriptQueue> configure = (queue) -> {
                            if (lastEntity.getLocation() != null) {
                                queue.addDefinition("location", lastEntity.getLocation());
                            }
                            else {
                                queue.addDefinition("location", lastLocation);
                            }
                            queue.addDefinition("pushed_entities", entityList);
                            queue.addDefinition("last_entity", lastEntity);
                        };
                        ScriptUtilities.createAndStartQueue(script.getContainer(), null, scriptEntry.entryData, null, configure, null, null, definitions, scriptEntry);
                    }
                    scriptEntry.setFinished(true);
                }
            }
        };
        task.runTaskTimer(Denizen.getInstance(), 0, prec);
    }

    public static boolean isSafeBlock(Location loc) {
        return !Utilities.isLocationYSafe(loc) || !loc.getBlock().getType().isSolid();
    }
}
