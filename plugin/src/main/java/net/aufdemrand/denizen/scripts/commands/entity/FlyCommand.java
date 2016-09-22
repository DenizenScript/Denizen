package net.aufdemrand.denizen.scripts.commands.entity;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.utilities.Conversion;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.entity.Position;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class FlyCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Initialize necessary fields

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("cancel")
                    && arg.matches("cancel")) {

                scriptEntry.addObject("cancel", "");
            }

            else if (!scriptEntry.hasObject("destinations")
                    && arg.matchesPrefix("destination", "destinations", "d")) {

                scriptEntry.addObject("destinations", arg.asType(dList.class).filter(dLocation.class));
            }

            else if (!scriptEntry.hasObject("controller")
                    && arg.matchesArgumentType(dPlayer.class)
                    && arg.matchesPrefix("controller", "c")) {

                // Check if it matches a dPlayer, but save it as a dEntity
                scriptEntry.addObject("controller", (arg.asType(dEntity.class)));
            }

            else if (!scriptEntry.hasObject("origin")
                    && arg.matchesArgumentType(dLocation.class)) {

                scriptEntry.addObject("origin", arg.asType(dLocation.class));
            }

            else if (!scriptEntry.hasObject("entities")
                    && arg.matchesArgumentList(dEntity.class)) {

                scriptEntry.addObject("entities", arg.asType(dList.class).filter(dEntity.class));
            }

            else if (!scriptEntry.hasObject("rotationThreshold")
                    && arg.matchesPrefix("rotationthreshold", "rotation", "r")
                    && arg.matchesPrimitive(aH.PrimitiveType.Float)) {

                scriptEntry.addObject("rotationThreshold", arg.asElement());
            }

            else if (!scriptEntry.hasObject("speed")
                    && arg.matchesPrimitive(aH.PrimitiveType.Double)) {

                scriptEntry.addObject("speed", arg.asElement());
            }

            else {
                arg.reportUnhandled();
            }
        }

        // Use the NPC or player's locations as the location if one is not specified
        scriptEntry.defaultObject("origin",
                ((BukkitScriptEntryData) scriptEntry.entryData).hasPlayer() ? ((BukkitScriptEntryData) scriptEntry.entryData).getPlayer().getLocation() : null,
                ((BukkitScriptEntryData) scriptEntry.entryData).hasNPC() ? ((BukkitScriptEntryData) scriptEntry.entryData).getNPC().getLocation() : null);

        // Use a default speed and rotation threshold if they are not specified
        scriptEntry.defaultObject("speed", new Element(1.2));
        scriptEntry.defaultObject("rotationThreshold", new Element(15));

        // Check to make sure required arguments have been filled
        if (!scriptEntry.hasObject("entities")) {
            throw new InvalidArgumentsException("Must specify entity/entities!");
        }
        if (!scriptEntry.hasObject("origin")) {
            throw new InvalidArgumentsException("Must specify an origin!");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(final ScriptEntry scriptEntry) throws CommandExecutionException {
        // Get objects

        dLocation origin = (dLocation) scriptEntry.getObject("origin");
        List<dEntity> entities = (List<dEntity>) scriptEntry.getObject("entities");
        final List<dLocation> destinations = scriptEntry.hasObject("destinations") ?
                (List<dLocation>) scriptEntry.getObject("destinations") :
                new ArrayList<dLocation>();

        // Set freeflight to true only if there are no destinations
        final boolean freeflight = destinations.size() < 1;

        dEntity controller = (dEntity) scriptEntry.getObject("controller");

        // If freeflight is on, we need to do some checks
        if (freeflight) {

            // If no controller was set, we need someone to control the
            // flying entities, so try to find a player in the entity list
            if (controller == null) {
                for (dEntity entity : entities) {
                    if (entity.isPlayer()) {
                        // If this player will be a rider on something, and will not
                        // be at the bottom ridden by the other entities, set it as
                        // the controller
                        if (entities.get(entities.size() - 1) != entity) {
                            controller = entity;
                            dB.report(scriptEntry, getName(), "Flight control defaulting to " + controller);
                            break;
                        }
                    }
                }

                // If the controller is still null, we cannot continue
                if (controller == null) {
                    dB.report(scriptEntry, getName(), "There is no one to control the flight's path!");
                    return;
                }
            }

            // Else, if the controller was set, we need to make sure
            // it is among the flying entities, and add it if it is not
            else {
                boolean found = false;

                for (dEntity entity : entities) {
                    if (entity.identify().equals(controller.identify())) {
                        found = true;
                        break;
                    }
                }

                // Add the controller to the entity list
                if (!found) {
                    dB.report(scriptEntry, getName(), "Adding controller " + controller + " to flying entities.");
                    entities.add(0, controller);
                }
            }
        }

        final double speed = ((Element) scriptEntry.getObject("speed")).asDouble();
        final float rotationThreshold = ((Element) scriptEntry.getObject("rotationThreshold")).asFloat();
        boolean cancel = scriptEntry.hasObject("cancel");

        // Report to dB
        dB.report(scriptEntry, getName(), (cancel ? aH.debugObj("cancel", cancel) : "") +
                aH.debugObj("origin", origin) +
                aH.debugObj("entities", entities.toString()) +
                aH.debugObj("speed", speed) +
                aH.debugObj("rotation threshold degrees", rotationThreshold) +
                (freeflight ? aH.debugObj("controller", controller)
                        : aH.debugObj("destinations", destinations.toString())));

        // Mount or dismount all of the entities
        if (!cancel) {

            // Go through all the entities, spawning/teleporting them
            for (dEntity entity : entities) {
                entity.spawnAt(origin);
            }

            Position.mount(Conversion.convertEntities(entities));
        }
        else {
            Position.dismount(Conversion.convertEntities(entities));

            // Go no further if we are dismounting entities
            return;
        }

        // Get the last entity on the list
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
                        location = finalController.getEyeLocation()
                                .add(finalController.getEyeLocation().getDirection()
                                        .multiply(30));
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
                    if (!NMSHandler.getInstance().getEntityHelper().isFacingLocation(entity, location, rotationThreshold)) {

                        NMSHandler.getInstance().getEntityHelper().faceLocation(entity, location);
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

        task.runTaskTimer(DenizenAPI.getCurrentInstance(), 0, 3);
    }
}
