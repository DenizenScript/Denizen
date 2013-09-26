package net.aufdemrand.denizen.scripts.commands.entity;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.Duration;
import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dList;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import net.aufdemrand.denizen.utilities.entity.Rotation;

import org.bukkit.scheduler.BukkitRunnable;

/**
 * Makes entities rotate clockwise or counterclockwise
 * for a certain duration.
 *
 * @author David Cernat
 */

public class RotateCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("duration")
                    && arg.matchesArgumentType(Duration.class)
                    && arg.matchesPrefix("duration, d")) {

               scriptEntry.addObject("duration", arg.asType(Duration.class));
            }

            else if (!scriptEntry.hasObject("frequency")
                     && arg.matchesArgumentType(Duration.class)
                     && arg.matchesPrefix("frequency, f")) {

                scriptEntry.addObject("frequency", arg.asType(Duration.class));
            }

            else if (!scriptEntry.hasObject("yaw")
                    && arg.matchesPrefix("yaw, y, rotation, r")
                    && arg.matchesPrimitive(aH.PrimitiveType.Float)) {

               scriptEntry.addObject("yaw", arg.asElement());
            }

            else if (!scriptEntry.hasObject("pitch")
                    && arg.matchesPrefix("pitch, p, tilt, t")
                    && arg.matchesPrimitive(aH.PrimitiveType.Float)) {

               scriptEntry.addObject("pitch", arg.asElement());
            }

            else if (!scriptEntry.hasObject("entities")
                     && arg.matchesArgumentList(dEntity.class)) {

                scriptEntry.addObject("entities", ((dList) arg.asType(dList.class)).filter(dEntity.class));
            }

            else dB.echoError(dB.Messages.ERROR_UNKNOWN_ARGUMENT, arg.raw_value);
        }

        // Use the NPC or the Player as the default entity
        scriptEntry.defaultObject("entities",
                (scriptEntry.hasPlayer() ? Arrays.asList(scriptEntry.getPlayer().getDenizenEntity()) : null),
                (scriptEntry.hasNPC() ? Arrays.asList(scriptEntry.getNPC().getDenizenEntity()) : null));

        scriptEntry.defaultObject("yaw", new Element(10));
        scriptEntry.defaultObject("pitch", new Element(0));
        scriptEntry.defaultObject("duration", new Duration(20));
        scriptEntry.defaultObject("frequency", Duration.valueOf("1t"));

        // Check to make sure required arguments have been filled
        if (!scriptEntry.hasObject("entities"))
            throw new InvalidArgumentsException(Messages.ERROR_MISSING_OTHER, "entities");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(final ScriptEntry scriptEntry) throws CommandExecutionException {

        final List<dEntity> entities = (List<dEntity>) scriptEntry.getObject("entities");
        final Duration duration = (Duration) scriptEntry.getObject("duration");
        final Duration frequency = (Duration) scriptEntry.getObject("frequency");
        final float yaw = ((Element) scriptEntry.getObject("yaw")).asFloat();
        final float pitch = ((Element) scriptEntry.getObject("pitch")).asFloat();

        // Report to dB
        dB.report(getName(), aH.debugObj("entities", entities.toString()) +
                             duration.debug() +
                             frequency.debug());

        // Go through all the entities, removing those that
        // are not spawned
        Collection<dEntity> unspawnedEntities = new LinkedList<dEntity>();
        for (dEntity entity : entities)
            if (!entity.isSpawned())
                unspawnedEntities.add(entity);

        for (dEntity unspawnedEntity : unspawnedEntities)
            entities.remove(unspawnedEntity);

        // Run a task that will keep rotating the entities
        BukkitRunnable task = new BukkitRunnable() {
            int ticks = 0;
            int maxTicks = duration.getTicksAsInt();
            @Override
            public void run() {

                if (ticks < maxTicks) {
                    for (dEntity entity : entities) {

                        Rotation.rotate(entity.getBukkitEntity(),
                                        Rotation.normalizeYaw(entity.getLocation().getYaw() + yaw),
                                        entity.getLocation().getPitch() + pitch);
                    }
                    ticks = (int) (ticks + frequency.getTicks());
                }
                else this.cancel();
            }
        };
        task.runTaskTimer(denizen, 0, frequency.getTicks());
    }
}
