package net.aufdemrand.denizen.scripts.commands.entity;

import java.util.Arrays;
import java.util.List;

import net.aufdemrand.denizen.objects.*;

import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.entity.Rotation;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Controls entity heads.
 *
 * @author Jeremy Schroeder, mcmonkey
 *
 */

public class LookCommand extends AbstractCommand {

    // look (<entity>) [<location>]
    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentType(dLocation.class)) {
                scriptEntry.addObject("location", arg.asType(dLocation.class));
            }

            else if (!scriptEntry.hasObject("duration")
                    && arg.matchesArgumentType(Duration.class)
                    && arg.matchesPrefix("duration", "d"))
                scriptEntry.addObject("duration", arg.asType(Duration.class));

            else if (!scriptEntry.hasObject("entities")
                     && arg.matchesArgumentList(dEntity.class)) {
                // Entity arg
                scriptEntry.addObject("entities", arg.asType(dList.class).filter(dEntity.class));
            }

            else arg.reportUnhandled();
        }

        // Use the NPC or player as the entity if no entities are specified

        scriptEntry.defaultObject("entities",
                scriptEntry.hasNPC() ? Arrays.asList(scriptEntry.getNPC().getDenizenEntity()) : null,
                scriptEntry.hasPlayer() ? Arrays.asList(scriptEntry.getPlayer().getDenizenEntity()) : null);

        if (!scriptEntry.hasObject("location") || !scriptEntry.hasObject("entities"))
            throw new InvalidArgumentsException("Must specify a location and entity!");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        final dLocation loc = (dLocation) scriptEntry.getObject("location");
        final List<dEntity> entities = (List<dEntity>) scriptEntry.getObject("entities");
        final Duration duration = (Duration) scriptEntry.getObject("duration");

        dB.report(scriptEntry, getName(), loc.debug() +
                aH.debugObj("entities", entities.toString()));

        for (dEntity entity : entities) {
            if (entity.isSpawned()) {
                Rotation.faceLocation(entity.getBukkitEntity(), loc);
            }
        }
        if (duration != null && duration.getTicks() > 2) {
            BukkitRunnable task = new BukkitRunnable() {
                long bounces = 0;
                public void run() {
                    bounces += 2;
                    if (bounces > duration.getTicks()) {
                        this.cancel();
                        return;
                    }
                    for (dEntity entity : entities) {
                        if (entity.isSpawned()) {
                            Rotation.faceLocation(entity.getBukkitEntity(), loc);
                        }
                    }
                }
            };
            task.runTaskTimer(DenizenAPI.getCurrentInstance(), 0, 2);
        }
    }
}

