package net.aufdemrand.denizen.scripts.commands.entity;

import java.util.List;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import net.citizensnpcs.util.Util;
import org.bukkit.Location;

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

            else if (!scriptEntry.hasObject("spread")
                    && arg.matchesPrimitive(aH.PrimitiveType.Integer)) {
                scriptEntry.addObject("spread", arg.asElement());
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
        Element spread = scriptEntry.getElement("spread");

        // Report to dB
        dB.report(getName(), aH.debugObj("entities", entities.toString()) +
                              location.debug() +
                             (spread != null?spread.debug():"") +
                             (target != null ? target.debug() : ""));

        // Keep a dList of entities that can be called using %spawned_entities%
        // later in the script queue

        dList entityList = new dList();

        // Go through all the entities and spawn them or teleport them,
        // then set their targets if applicable

        for (dEntity entity : entities) {
            Location loc = location.clone();
            if (spread != null) {
                loc.add(Utilities.getRandom().nextInt(spread.asInt() * 2) - spread.asInt(),
                        0,
                        Utilities.getRandom().nextInt(spread.asInt() * 2) - spread.asInt());
            }

            if (!entity.isSpawned()) {
                entity.spawnAt(loc);
            }
            else {
                entity.teleport(loc);
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
