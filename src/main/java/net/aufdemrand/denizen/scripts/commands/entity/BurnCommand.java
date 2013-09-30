package net.aufdemrand.denizen.scripts.commands.entity;

import java.util.Arrays;
import java.util.List;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.Duration;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dList;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;

/**
 * Sets a list of entities on fire.
 *
 * @author David Cernat
 */

public class BurnCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("entities")
                && arg.matchesArgumentList(dEntity.class)) {

                scriptEntry.addObject("entities", ((dList) arg.asType(dList.class)).filter(dEntity.class));
            }

            else if (!scriptEntry.hasObject("duration")
                     && arg.matchesArgumentType(Duration.class)) {

                scriptEntry.addObject("duration", arg.asType(Duration.class));
            }

            else dB.echoError(dB.Messages.ERROR_UNKNOWN_ARGUMENT, arg.raw_value);
        }

        // Use the NPC or the Player as the default entity
        scriptEntry.defaultObject("entities",
                (scriptEntry.hasPlayer() ? Arrays.asList(scriptEntry.getPlayer().getDenizenEntity()) : null),
                (scriptEntry.hasNPC() ? Arrays.asList(scriptEntry.getNPC().getDenizenEntity()) : null));

        // Use default duration if one is not specified
        scriptEntry.defaultObject("duration", Duration.valueOf("5s"));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(final ScriptEntry scriptEntry) throws CommandExecutionException {

        // Get objects
        List<dEntity> entities = (List<dEntity>) scriptEntry.getObject("entities");
        Duration duration = (Duration) scriptEntry.getObject("duration");

        // Report to dB
        dB.report(getName(), duration.debug() +
                aH.debugObj("entities", entities.toString()));

        // Go through all the entities and set them on fire
        for (dEntity entity : entities) {
            if (entity.isSpawned()) {
                entity.getBukkitEntity().setFireTicks(duration.getTicksAsInt());
            }
        }
    }
}
