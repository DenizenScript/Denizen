package net.aufdemrand.denizen.scripts.commands.entity;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Location;

import java.util.List;

public class SpawnCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("entities")
                    && arg.matchesArgumentList(dEntity.class)) {

                scriptEntry.addObject("entities", arg.asType(dList.class).filter(dEntity.class, scriptEntry));
            }
            else if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentType(dLocation.class)) {

                scriptEntry.addObject("location", arg.asType(dLocation.class));
            }
            else if (!scriptEntry.hasObject("target")
                    && arg.matchesArgumentType(dEntity.class)
                    && arg.matchesPrefix("target")) {

                scriptEntry.addObject("target", arg.asType(dEntity.class));
            }
            else if (!scriptEntry.hasObject("spread")
                    && arg.matchesPrimitive(aH.PrimitiveType.Integer)) {
                scriptEntry.addObject("spread", arg.asElement());
            }
            else if (!scriptEntry.hasObject("persistent")
                    && arg.matches("persistent")) {

                scriptEntry.addObject("persistent", "");
            }
            else {
                arg.reportUnhandled();
            }
        }

        // Use the NPC or player's locations as the location if one is not specified
        scriptEntry.defaultObject("location",
                ((BukkitScriptEntryData) scriptEntry.entryData).hasNPC() ? ((BukkitScriptEntryData) scriptEntry.entryData).getNPC().getLocation() : null,
                ((BukkitScriptEntryData) scriptEntry.entryData).hasPlayer() ? ((BukkitScriptEntryData) scriptEntry.entryData).getPlayer().getLocation() : null);

        // Check to make sure required arguments have been filled
        if (!scriptEntry.hasObject("entities")) {
            throw new InvalidArgumentsException("Must specify entity/entities!");
        }

        if (!scriptEntry.hasObject("location")) {
            throw new InvalidArgumentsException("Must specify a location!");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(final ScriptEntry scriptEntry) {

        // Get objects
        List<dEntity> entities = (List<dEntity>) scriptEntry.getObject("entities");
        dLocation location = (dLocation) scriptEntry.getObject("location");
        dEntity target = (dEntity) scriptEntry.getObject("target");
        Element spread = scriptEntry.getElement("spread");
        boolean persistent = scriptEntry.hasObject("persistent");

        // Report to dB
        if (scriptEntry.dbCallShouldDebug()) {
            dB.report(scriptEntry, getName(), aH.debugObj("entities", entities.toString()) +
                    location.debug() +
                    (spread != null ? spread.debug() : "") +
                    (target != null ? target.debug() : "") +
                    (persistent ? aH.debugObj("persistent", "true") : ""));
        }

        // Keep a dList of entities that can be called using <entry[name].spawned_entities>
        // later in the script queue

        dList entityList = new dList();

        // Go through all the entities and spawn them or teleport them,
        // then set their targets if applicable

        for (dEntity entity : entities) {
            Location loc = location.clone();
            if (spread != null) {
                loc.add(CoreUtilities.getRandom().nextInt(spread.asInt() * 2) - spread.asInt(),
                        0,
                        CoreUtilities.getRandom().nextInt(spread.asInt() * 2) - spread.asInt());
            }

            entity.spawnAt(loc);

            // Only add to entityList after the entities have been
            // spawned, otherwise you'll get something like "e@skeleton"
            // instead of "e@57" on it

            entityList.add(entity.toString());

            if (persistent && entity.isLivingEntity()) {
                entity.getLivingEntity().setRemoveWhenFarAway(false);
            }

            if (target != null && target.isLivingEntity()) {
                entity.target(target.getLivingEntity());
            }
        }

        // Add entities to context so that the specific entities created/spawned
        // can be fetched.

        scriptEntry.addObject("spawned_entities", entityList);
        if (entityList.size() != 0) {
            scriptEntry.addObject("spawned_entity", entityList.get(0));
        }
    }
}
