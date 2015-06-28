package net.aufdemrand.denizen.scripts.commands.entity;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.utilities.Conversion;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.entity.Position;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;

import java.util.List;

public class MountCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Initialize necessary fields

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("cancel")
                    && arg.matches("cancel")) {

                scriptEntry.addObject("cancel", "");
            }

            else if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentType(dLocation.class)) {
                // Location arg
                scriptEntry.addObject("location", arg.asType(dLocation.class));
            }

            else if (!scriptEntry.hasObject("entities")
                    && arg.matchesArgumentList(dEntity.class)) {
                // Entity arg
                scriptEntry.addObject("entities", arg.asType(dList.class).filter(dEntity.class));
            }

            else arg.reportUnhandled();
        }

        // Use the NPC or player's locations as the location if one is not specified

        scriptEntry.defaultObject("location",
                ((BukkitScriptEntryData) scriptEntry.entryData).hasPlayer() ? ((BukkitScriptEntryData) scriptEntry.entryData).getPlayer().getLocation() : null,
                ((BukkitScriptEntryData) scriptEntry.entryData).hasNPC() ? ((BukkitScriptEntryData) scriptEntry.entryData).getNPC().getLocation() : null);

        // Check to make sure required arguments have been filled

        if (!scriptEntry.hasObject("entities"))
            throw new InvalidArgumentsException("Must specify entity/entities!");

        if (!scriptEntry.hasObject("location"))
            throw new InvalidArgumentsException("Must specify a location!");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(final ScriptEntry scriptEntry) throws CommandExecutionException {
        // Get objects

        dLocation location = (dLocation) scriptEntry.getObject("location");
        List<dEntity> entities = (List<dEntity>) scriptEntry.getObject("entities");
        boolean cancel = scriptEntry.hasObject("cancel");

        // Report to dB
        dB.report(scriptEntry, getName(), (cancel ? aH.debugObj("cancel", cancel) : "") +
                aH.debugObj("location", location) +
                aH.debugObj("entities", entities.toString()));

        // Mount or dismount all of the entities
        if (!cancel) {

            // Go through all the entities, spawning/teleporting them
            for (dEntity entity : entities) {
                entity.spawnAt(location);
            }

            Position.mount(Conversion.convertEntities(entities));
        }
        else {
            Position.dismount(Conversion.convertEntities(entities));
        }
    }
}
