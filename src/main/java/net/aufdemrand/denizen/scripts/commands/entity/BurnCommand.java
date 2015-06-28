package net.aufdemrand.denizen.scripts.commands.entity;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Duration;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;

import java.util.Arrays;
import java.util.List;

public class BurnCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("entities")
                    && arg.matchesArgumentList(dEntity.class)) {

                scriptEntry.addObject("entities", (arg.asType(dList.class)).filter(dEntity.class));
            }

            else if (!scriptEntry.hasObject("duration")
                    && arg.matchesArgumentType(Duration.class)) {

                scriptEntry.addObject("duration", arg.asType(Duration.class));
            }

            else arg.reportUnhandled();
        }

        // Use the NPC or the Player as the default entity
        scriptEntry.defaultObject("entities",
                (((BukkitScriptEntryData) scriptEntry.entryData).hasPlayer() ? Arrays.asList(((BukkitScriptEntryData) scriptEntry.entryData).getPlayer().getDenizenEntity()) : null),
                (((BukkitScriptEntryData) scriptEntry.entryData).hasNPC() ? Arrays.asList(((BukkitScriptEntryData) scriptEntry.entryData).getNPC().getDenizenEntity()) : null));

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
        dB.report(scriptEntry, getName(), duration.debug() +
                aH.debugObj("entities", entities.toString()));

        // Go through all the entities and set them on fire
        for (dEntity entity : entities) {
            if (entity.isSpawned()) {
                entity.getBukkitEntity().setFireTicks(duration.getTicksAsInt());
            }
        }
    }
}
