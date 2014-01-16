package net.aufdemrand.denizen.scripts.commands.entity;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
import net.aufdemrand.denizen.utilities.entity.Rotation;

import org.bukkit.scheduler.BukkitRunnable;

/**
 * Makes entities rotate clockwise or counterclockwise
 * for a certain duration.
 *
 * @author David Cernat
 */

public class RotateCommand extends AbstractCommand {

    public static Set<UUID> rotatingEntities = new HashSet<UUID>();

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("cancel")
                && (arg.matches("cancel") || arg.matches("stop"))) {

                    scriptEntry.addObject("cancel", "");
            }

            else if (!scriptEntry.hasObject("infinite")
                     && arg.matches("infinite")) {

                    scriptEntry.addObject("infinite", "");
            }

            else if (!scriptEntry.hasObject("duration")
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

                scriptEntry.addObject("entities", arg.asType(dList.class).filter(dEntity.class));
            }

            else arg.reportUnhandled();
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
            throw new InvalidArgumentsException("Must specify entity/entities!");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(final ScriptEntry scriptEntry) throws CommandExecutionException {

        final List<dEntity> entities = (List<dEntity>) scriptEntry.getObject("entities");
        final Duration duration = (Duration) scriptEntry.getObject("duration");
        final Duration frequency = (Duration) scriptEntry.getObject("frequency");
        final Element yaw = (Element) scriptEntry.getObject("yaw");
        final Element pitch = (Element) scriptEntry.getObject("pitch");
        boolean cancel = scriptEntry.hasObject("cancel");
        final boolean infinite = scriptEntry.hasObject("infinite");

        // Report to dB
        dB.report(scriptEntry, getName(), (cancel ? aH.debugObj("cancel", cancel) : "") +
                             aH.debugObj("entities", entities.toString()) +
                             (infinite ? aH.debugObj("duration", "infinite") : duration.debug()) +
                             frequency.debug() +
                             yaw.debug() +
                             pitch.debug());

        // Add entities to the rotatingEntities list or remove
        // them from it
        for (dEntity entity : entities)
            if (cancel) rotatingEntities.remove(entity.getUUID());
            else        rotatingEntities.add(entity.getUUID());

        // Go no further if we are canceling a rotation
        if (cancel) return;

        // Run a task that will keep rotating the entities
        BukkitRunnable task = new BukkitRunnable() {
            int ticks = 0;
            int maxTicks = duration.getTicksAsInt();

            // Track entities that are no longer used, to remove them from
            // the regular list
            Collection<dEntity> unusedEntities = new LinkedList<dEntity>();

            @Override
            public void run() {

                if (entities.isEmpty()) {
                    this.cancel();
                }

                else if (infinite || ticks < maxTicks) {
                    for (dEntity entity : entities) {
                        if (entity.isSpawned() && rotatingEntities.contains(entity.getUUID())) {
                            Rotation.rotate(entity.getBukkitEntity(),
                                    Rotation.normalizeYaw(entity.getLocation().getYaw() + yaw.asFloat()),
                                    entity.getLocation().getPitch() + pitch.asFloat());
                        }
                        else {
                            rotatingEntities.remove(entity.getUUID());
                            unusedEntities.add(entity);
                        }
                    }

                    // Remove any entities that are no longer spawned
                    if (!unusedEntities.isEmpty()) {
                        for (dEntity unusedEntity : unusedEntities) {
                            entities.remove(unusedEntity);
                        }
                        unusedEntities.clear();
                    }

                    ticks = (int) (ticks + frequency.getTicks());
                }
                else this.cancel();
            }
        };
        task.runTaskTimer(denizen, 0, frequency.getTicks());
    }
}
