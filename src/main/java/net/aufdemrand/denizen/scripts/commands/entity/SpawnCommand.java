package net.aufdemrand.denizen.scripts.commands.entity;

import java.util.List;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dList;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;

/**
 * Spawn entities at a location. If no location is chosen,
 * the entities are spawned at the NPC or player's location.
 *
 * @author David Cernat
 */

public class SpawnCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {



        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("entities")
                && arg.matchesArgumentList(dEntity.class)) {
                // Entity arg
                scriptEntry.addObject("entities", ((dList) arg.asType(dList.class)).filter(dEntity.class));
            }

            else if (!scriptEntry.hasObject("location")
                     && arg.matchesArgumentType(dLocation.class)) {
                // Location arg
                scriptEntry.addObject("location", arg.asType(dLocation.class));
            }

            else if (!scriptEntry.hasObject("target")
                     && arg.matchesArgumentType(dEntity.class)
                     && arg.matchesPrefix("target")) {
                // Entity arg
                scriptEntry.addObject("target", arg.asType(dEntity.class));
            }
        }

        // Use the NPC or player's locations as the location if one is not specified

        scriptEntry.defaultObject("location",
                scriptEntry.hasNPC() ? scriptEntry.getNPC().getLocation() : null,
                scriptEntry.hasPlayer() ? scriptEntry.getPlayer().getLocation() : null);

        // Check to make sure required arguments have been filled

        if (!scriptEntry.hasObject("entities"))
            throw new InvalidArgumentsException(Messages.ERROR_MISSING_OTHER, "ENTITIES");

        if (!scriptEntry.hasObject("location"))
            throw new InvalidArgumentsException(Messages.ERROR_MISSING_OTHER, "LOCATION");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(final ScriptEntry scriptEntry) throws CommandExecutionException {

        // Get objects
        List<dEntity> entities = (List<dEntity>) scriptEntry.getObject("entities");
        dLocation location = (dLocation) scriptEntry.getObject("location");
        dEntity target = (dEntity) scriptEntry.getObject("target");

        // Report to dB
        dB.report(getName(), aH.debugObj("entities", entities.toString()) +
                             aH.debugObj("location", location) +
                             (target != null ? aH.debugObj("target", target) : ""));

        // Keep a dList of entities that can be called using %spawned_entities%
        // later in the script queue

        dList entityList = new dList();

        // Go through all the entities and spawn them or teleport them,
        // then set their targets if applicable

        for (dEntity entity : entities) {

            if (!entity.isSpawned()) {
                entity.spawnAt(location);
            }
            else {
                entity.teleport(location);
            }

            // Only add to entityList after the entities have been
            // spawned, otherwise you'll get something like "e@skeleton"
            // instead of "e@57" on it

            entityList.add(entity.toString());

            if (target != null && target.isLivingEntity()) {
                entity.target(target.getLivingEntity());
            }
        }

        // Add entities to context so that the specific entities created/spawned
        // can be fetched.

        scriptEntry.addObject("spawned_entities", entityList);
    }
}
