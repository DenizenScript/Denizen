package com.denizenscript.denizen.scripts.commands.entity;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.dB;
import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizen.objects.dLocation;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.dList;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.Location;

import java.util.List;

public class SpawnCommand extends AbstractCommand {

    // <--[command]
    // @Name Spawn
    // @Syntax spawn [<entity>|...] [<location>] (target:<entity>) (persistent)
    // @Required 2
    // @Short Spawns a list of entities at a certain location.
    // @Group entity
    //
    // @Description
    // Spawn an entity or list of entities at the specified location. Accepts the 'target:<entity>' argument which
    // will cause all spawned entities to follow and attack the targetted entity.
    // If the persistent argument is present, the entity will not despawn when no players are within range, causing
    // the enity to remain until killed.
    //
    // @Tags
    // <e@entity.is_spawned>
    // <server.entity_is_spawned[<entity>]>
    // <server.list_entity_types>
    // <entry[saveName].spawned_entities> returns a list of entities that were spawned.
    // <entry[saveName].spawned_entity> returns the entity that was spawned (if you only spawned one).
    //
    // @Usage
    // Use to spawn a spider at the player's location.
    // - spawn spider <player.location>
    //
    // @Usage
    // Use to spawn a spider at the player's location which will automatically target the player.
    // - spawn spider <player.location> target:<player>
    //
    // @Usage
    // Use to spawn a swarm of creepers around the npc, which will not despawn until killed.
    // - spawn creeper|creeper|creeper|creeper|creeper <npc.location> persistent
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (Argument arg : ArgumentHelper.interpretArguments(scriptEntry.aHArgs)) {

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
                    && arg.matchesPrimitive(ArgumentHelper.PrimitiveType.Integer)) {
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
                Utilities.entryHasNPC(scriptEntry) ? Utilities.getEntryNPC(scriptEntry).getLocation() : null,
                Utilities.entryHasPlayer(scriptEntry) ? Utilities.getEntryPlayer(scriptEntry).getLocation() : null);

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
            dB.report(scriptEntry, getName(), ArgumentHelper.debugObj("entities", entities.toString()) +
                    location.debug() +
                    (spread != null ? spread.debug() : "") +
                    (target != null ? target.debug() : "") +
                    (persistent ? ArgumentHelper.debugObj("persistent", "true") : ""));
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

            entityList.addObject(entity);

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
        if (entities.size() != 0) {
            scriptEntry.addObject("spawned_entity", entities.get(0));
        }
    }
}
