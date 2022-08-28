package com.denizenscript.denizen.scripts.commands.entity;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.utilities.Conversion;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.entity.Position;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class FlyCommand extends AbstractCommand {

    public FlyCommand() {
        setName("fly");
        setSyntax("fly (cancel) [<entity>|...] (controller:<player>) (origin:<location>) (destinations:<location>|...) (speed:<#.#>) (rotationthreshold:<#.#>)");
        setRequiredArguments(1, 7);
        isProcedural = false;
    }

    // <--[command]
    // @Name Fly
    // @Syntax fly (cancel) [<entity>|...] (controller:<player>) (origin:<location>) (destinations:<location>|...) (speed:<#.#>) (rotationthreshold:<#.#>)
    // @Required 1
    // @Maximum 7
    // @Short Make an entity fly where its controller is looking or fly to waypoints.
    // @Group entity
    //
    // @Description
    // Make an entity fly where its controller is looking or fly to waypoints.
    // This is particularly useful as a way to make flying entities rideable (or make a non-flying entity start flying).
    //
    // Optionally specify a list of "destinations" to make it move through a series of checkpoints (disabling the "controller" functionality).
    //
    // Optionally specify an "origin" location where the entities should teleport to at the start.
    //
    // Optionally specify the "speed" argument to set how fast the entity should fly.
    //
    // Optionally specify the "rotationthreshold" to set the minimum threshold (in degrees) before the entity should forcibly rotate to the correct direction.
    //
    // Use the "cancel" argument to stop an existing flight.
    //
    // @Tags
    // <PlayerTag.can_fly>
    // <PlayerTag.fly_speed>
    // <PlayerTag.is_flying>
    //
    // @Usage
    // Use to make a player ride+fly a newly spawned dragon.
    // - fly <player>|ender_dragon
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("cancel")
                    && arg.matches("cancel")) {
                scriptEntry.addObject("cancel", "");
            }
            else if (!scriptEntry.hasObject("destinations")
                    && arg.matchesPrefix("destination", "destinations", "d")) {
                scriptEntry.addObject("destinations", arg.asType(ListTag.class).filter(LocationTag.class, scriptEntry));
            }
            else if (!scriptEntry.hasObject("controller")
                    && arg.matchesArgumentType(PlayerTag.class)
                    && arg.matchesPrefix("controller", "c")) {
                // Check if it matches a PlayerTag, but save it as a EntityTag
                scriptEntry.addObject("controller", arg.asType(EntityTag.class));
            }
            else if (!scriptEntry.hasObject("origin")
                    && arg.matchesArgumentType(LocationTag.class)) {
                scriptEntry.addObject("origin", arg.asType(LocationTag.class));
            }
            else if (!scriptEntry.hasObject("entities")
                    && arg.matchesArgumentList(EntityTag.class)) {
                scriptEntry.addObject("entities", arg.asType(ListTag.class).filter(EntityTag.class, scriptEntry));
            }
            else if (!scriptEntry.hasObject("rotation_threshold")
                    && arg.matchesPrefix("rotationthreshold", "rotation", "r")
                    && arg.matchesFloat()) {
                scriptEntry.addObject("rotation_threshold", arg.asElement());
            }
            else if (!scriptEntry.hasObject("speed")
                    && arg.matchesFloat()) {
                scriptEntry.addObject("speed", arg.asElement());
            }
            else {
                arg.reportUnhandled();
            }
        }
        scriptEntry.defaultObject("origin", Utilities.entryDefaultLocation(scriptEntry, true));
        scriptEntry.defaultObject("speed", new ElementTag(1.2));
        scriptEntry.defaultObject("rotation_threshold", new ElementTag(15));
        if (!scriptEntry.hasObject("entities")) {
            throw new InvalidArgumentsException("Must specify entity/entities!");
        }
        if (!scriptEntry.hasObject("origin")) {
            throw new InvalidArgumentsException("Must specify an origin!");
        }
    }

    @Override
    public void execute(final ScriptEntry scriptEntry) {
        LocationTag origin = scriptEntry.getObjectTag("origin");
        List<EntityTag> entities = (List<EntityTag>) scriptEntry.getObject("entities");
        final List<LocationTag> destinations = scriptEntry.hasObject("destinations") ? (List<LocationTag>) scriptEntry.getObject("destinations") : new ArrayList<>();
        // Set freeflight to true only if there are no destinations
        final boolean freeflight = destinations.size() < 1;
        EntityTag controller = scriptEntry.getObjectTag("controller");
        // If freeflight is on, we need to do some checks
        if (freeflight) {
            // If no controller was set, we need someone to control the
            // flying entities, so try to find a player in the entity list
            if (controller == null) {
                for (EntityTag entity : entities) {
                    if (entity.isPlayer()) {
                        // If this player will be a rider on something, and will not
                        // be at the bottom ridden by the other entities, set it as
                        // the controller
                        if (entities.get(entities.size() - 1) != entity) {
                            controller = entity;
                            if (scriptEntry.dbCallShouldDebug()) {
                                Debug.echoDebug(scriptEntry, "Flight control defaulting to " + controller);
                            }
                            break;
                        }
                    }
                }
                // If the controller is still null, we cannot continue
                if (controller == null) {
                    if (scriptEntry.dbCallShouldDebug()) {
                        Debug.echoDebug(scriptEntry, "There is no one to control the flight's path!");
                    }
                    return;
                }
            }
            // Else, if the controller was set, we need to make sure
            // it is among the flying entities, and add it if it is not
            else {
                boolean found = false;
                for (EntityTag entity : entities) {
                    if (entity.identify().equals(controller.identify())) {
                        found = true;
                        break;
                    }
                }
                // Add the controller to the entity list
                if (!found) {
                    if (scriptEntry.dbCallShouldDebug()) {
                        Debug.echoDebug(scriptEntry, "Adding controller " + controller + " to flying entities.");
                    }
                    entities.add(0, controller);
                }
            }
        }
        final double speed = ((ElementTag) scriptEntry.getObject("speed")).asDouble();
        final float rotationThreshold = ((ElementTag) scriptEntry.getObject("rotation_threshold")).asFloat();
        boolean cancel = scriptEntry.hasObject("cancel");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), (cancel ? db("cancel", true) : ""), origin, db("entities", entities),
                    db("speed", speed), db("rotation threshold degrees", rotationThreshold), (freeflight ? controller : db("destinations", destinations)));
        }
        if (!cancel) {
            for (EntityTag entity : entities) {
                entity.spawnAt(origin);
            }
            Position.mount(Conversion.convertEntities(entities));
        }
        else {
            Position.dismount(Conversion.convertEntities(entities));
            return;
        }
        final Entity entity = entities.get(entities.size() - 1).getBukkitEntity();
        final LivingEntity finalController = controller != null ? controller.getLivingEntity() : null;
        BukkitRunnable task = new BukkitRunnable() {
            Location location = null;
            Boolean flying = true;
            public void run() {
                if (freeflight) {
                    // If freeflight is on, and the flying entity
                    // is ridden by another entity, let it keep
                    // flying where the controller is looking
                    if (!entity.isEmpty() && finalController.isInsideVehicle()) {
                        location = finalController.getEyeLocation().add(finalController.getEyeLocation().getDirection().multiply(30));
                    }
                    else {
                        flying = false;
                    }
                }
                else {
                    // If freelight is not on, keep flying only as long
                    // as there are destinations left
                    if (destinations.size() > 0) {
                        location = destinations.get(0);
                    }
                    else {
                        flying = false;
                    }
                }
                if (flying && entity.isValid()) {
                    // To avoid excessive turbulence, only have the entity rotate
                    // when it really needs to
                    if (!NMSHandler.entityHelper.isFacingLocation(entity, location, rotationThreshold)) {
                        NMSHandler.entityHelper.faceLocation(entity, location);
                    }
                    Vector v1 = entity.getLocation().toVector();
                    Vector v2 = location.toVector();
                    Vector v3 = v2.clone().subtract(v1).normalize().multiply(speed);
                    entity.setVelocity(v3);
                    // If freeflight is off, check if the entity has reached its
                    // destination, and remove the destination if that happens
                    // to be the case
                    if (!freeflight) {
                        if (Math.abs(v2.getX() - v1.getX()) < 2 && Math.abs(v2.getY() - v1.getY()) < 2
                                && Math.abs(v2.getZ() - v1.getZ()) < 2) {
                            destinations.remove(0);
                        }
                    }
                }
                else {
                    flying = false;
                    this.cancel();
                }
            }
        };
        task.runTaskTimer(Denizen.getInstance(), 0, 3);
    }
}
