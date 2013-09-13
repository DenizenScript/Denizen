package net.aufdemrand.denizen.scripts.commands.entity;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.scripts.containers.core.TaskScriptContainer;
import net.aufdemrand.denizen.utilities.Conversion;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import net.aufdemrand.denizen.utilities.entity.Position;
import net.aufdemrand.denizen.utilities.entity.Rotation;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Moves entities through the air from an origin to a destination.
 * The origin can optionally be an entity that will look at the
 * object it is moving.
 *
 * @author David Cernat, mcmonkey
 */

public class PushCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {
            if (!scriptEntry.hasObject("origin")
                && arg.matchesPrefix("origin, o, source, shooter, s")) {
                if (arg.matchesArgumentType(dEntity.class))
                    scriptEntry.addObject("origin", arg.asType(dEntity.class));
                else if (arg.matchesArgumentType(dLocation.class))
                    scriptEntry.addObject("originlocation", arg.asType(dLocation.class));
                else
                    dB.echoError("Ignoring unrecognized argument: " + arg.raw_value);
            }

            else if (!scriptEntry.hasObject("destination")
                    && arg.matchesPrefix("destination, dest")
                    && arg.matchesArgumentType(dLocation.class))
                scriptEntry.addObject("destination", arg.asType(dLocation.class));

            else if (!scriptEntry.hasObject("duration")
                    && arg.matchesPrefix("duration, d")
                    && arg.matchesArgumentType(Duration.class))
                scriptEntry.addObject("duration", arg.asType(Duration.class));

            else if (!scriptEntry.hasObject("speed")
                    && arg.matchesPrefix("speed, s")
                    && arg.matchesPrimitive(aH.PrimitiveType.Double))
                scriptEntry.addObject("speed", arg.asElement());

            else if (!scriptEntry.hasObject("script")
                    && arg.matchesPrefix("script")
                    && arg.matchesArgumentType(dScript.class))
                scriptEntry.addObject("script", arg.asType(dScript.class));

            else if (!scriptEntry.hasObject("entities")
                    && arg.matchesArgumentList(dEntity.class))
                scriptEntry.addObject("entities", ((dList) arg.asType(dList.class)).filter(dEntity.class));

            else
                dB.echoError("Ignoring unrecognized argument: " + arg.raw_value);
        }

        if (!scriptEntry.hasObject("origin") && !scriptEntry.hasObject("originlocation")) {
            dEntity origin = (scriptEntry.hasNPC() ? scriptEntry.getNPC().getDenizenEntity() :
                            (scriptEntry.hasPlayer() ? scriptEntry.getPlayer().getDenizenEntity() : null));
            if (origin == null)
                throw new InvalidArgumentsException(Messages.ERROR_INVALID_ENTITY, "origin");
            scriptEntry.addObject("origin", origin);
        }

        if (!scriptEntry.hasObject("speed"))
            scriptEntry.addObject("speed", new Element("1.5"));
        if (!scriptEntry.hasObject("duration"))
            scriptEntry.addObject("duration", new Duration((long)80));

        if ((!scriptEntry.hasObject("entities")))
            throw new InvalidArgumentsException(Messages.ERROR_MISSING_OTHER, "entities");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(final ScriptEntry scriptEntry) throws CommandExecutionException {

        dLocation origin = null;
        if (scriptEntry.hasObject("originlocation"))
            origin = (dLocation)scriptEntry.getObject("location");

        dEntity shooter = null;
        LivingEntity shooterEntity = null;
        if (scriptEntry.hasObject("origin")) {
            shooter = (dEntity)scriptEntry.getObject("origin");
            shooterEntity = shooter.getLivingEntity();
            origin = new dLocation(shooterEntity.getEyeLocation().subtract(0, 0.4, 0));
        }

        if (shooter == null && origin == null) {
            throw new CommandExecutionException(Messages.ERROR_INVALID_ENTITY, "origin");
        }
            dLocation destination;
            if (scriptEntry.hasObject("destination"))
                destination = (dLocation)scriptEntry.getObject("destination");
            else {
                if (shooterEntity != null)
                    destination = new dLocation(shooterEntity.getEyeLocation().add(shooterEntity.getEyeLocation().getDirection().multiply(40)));
                else
                    destination = new dLocation(origin.add(40, 0, 0));
                dB.echoDebug("Defaulting destination.");
            }

            List<dEntity> entities = (List<dEntity>) scriptEntry.getObject("entities");
            Element speed = scriptEntry.getElement("speed");
            final dScript script;
            if (scriptEntry.hasObject("script"))
                script = (dScript)scriptEntry.getObject("script");
            else
                script = null;
            final int maxTicks = ((Duration) scriptEntry.getObject("duration")).getTicksAsInt() / 2;

            // Report to dB
            dB.report(getName(), aH.debugObj("origin", shooter) +
                    aH.debugObj("entities", entities.toString()) +
                    aH.debugObj("destination", destination) +
                    aH.debugObj("speed", speed) +
                    (script != null ? aH.debugObj("script", script) : ""));

            // If the shooter is an NPC, always rotate it to face the destination
            // of the projectile, but if the shooter is a player, only rotate him/her
            // if he/she is not looking in the correct general direction

            if (shooter != null && (shooter.isNPC() || !Rotation.isFacingLocation(shooterEntity, destination, 45)))
                Rotation.faceLocation(shooterEntity, destination);


            // Go through all the entities, spawning/teleporting and rotating them
            for (dEntity entity : entities) {
                if (!entity.isSpawned())
                    entity.spawnAt(origin);
                else
                    entity.teleport(origin);
                Rotation.faceLocation(entity.getBukkitEntity(), destination);
                if (entity.getBukkitEntity() instanceof Projectile && shooter != null)
                    ((Projectile) entity.getBukkitEntity()).setShooter(shooter.getLivingEntity());
            }

            Position.mount(Conversion.convert(entities));

            // Only use the last projectile in the task below
            final Entity lastEntity = entities.get(entities.size() - 1).getBukkitEntity();
            final Vector v2 = destination.toVector();
            final double fireSpeed = speed.asDouble();

            BukkitRunnable task = new BukkitRunnable() {
                int runs = 0;
                public void run() {
                    if (runs < maxTicks && lastEntity.isValid()) {
                        Vector v1 = lastEntity.getLocation().toVector();
                        Vector v3 = v2.clone().subtract(v1).normalize().multiply(fireSpeed);

                        lastEntity.setVelocity(v3);
                        runs++;

                        if (Math.abs(v2.getX() - v1.getX()) < 2 && Math.abs(v2.getY() - v1.getY()) < 2
                                && Math.abs(v2.getZ() - v1.getZ()) < 2) {
                            runs = maxTicks;
                        }

                        if (lastEntity.getLocation().add(v3).getBlock().getType() != Material.AIR) {
                            runs = maxTicks;
                        }
                    }
                    else {
                        this.cancel();
                        runs = 0;

                        if (script != null) {
                            Map<String, String> context = new HashMap<String, String>();
                            context.put("1", new dLocation(lastEntity.getLocation()).identify());
                            context.put("2", new dEntity(lastEntity).identify());

                            ((TaskScriptContainer) script.getContainer()).setSpeed(new Duration(0))
                                    .runTaskScript(scriptEntry.getPlayer(), scriptEntry.getNPC(), context);
                        }

                    }
                }
            };
        task.runTaskTimer(denizen, 0, 2);
    }
}
