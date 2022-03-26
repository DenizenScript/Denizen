package com.denizenscript.denizen.scripts.commands.entity;

import com.denizenscript.denizen.scripts.containers.core.EntityScriptHelper;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import java.util.List;

public class SpawnCommand extends AbstractCommand {

    public SpawnCommand() {
        setName("spawn");
        setSyntax("spawn [<entity>|...] (<location>) (target:<entity>) (persistent)");
        setRequiredArguments(1, 4);
        isProcedural = false;
    }

    // <--[command]
    // @Name Spawn
    // @Syntax spawn [<entity>|...] (<location>) (target:<entity>) (persistent)
    // @Required 1
    // @Maximum 4
    // @Short Spawns a list of entities at a certain location.
    // @Group entity
    //
    // @Synonyms summon
    //
    // @Description
    // Spawn an entity or list of entities at the specified location.
    //
    // Accepts the 'target:<entity>' argument which will cause all spawned entities to follow and attack the targeted entity.
    //
    // If the persistent argument is present, the entity will not despawn when no players are within range, causing the entity to remain until killed.
    //
    // @Tags
    // <EntityTag.is_spawned>
    // <server.entity_types>
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
    public void addCustomTabCompletions(TabCompletionsBuilder tab) {
        tab.add(EntityType.values());
        tab.add(EntityScriptHelper.scripts.keySet());
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("entities")
                    && arg.matchesArgumentList(EntityTag.class)) {
                EntityTag.allowDespawnedNpcs = true;
                scriptEntry.addObject("entities", arg.asType(ListTag.class).filter(EntityTag.class, scriptEntry));
                EntityTag.allowDespawnedNpcs = false;
            }
            else if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentType(LocationTag.class)) {
                scriptEntry.addObject("location", arg.asType(LocationTag.class));
            }
            else if (!scriptEntry.hasObject("target")
                    && arg.matchesArgumentType(EntityTag.class)
                    && arg.matchesPrefix("target")) {
                scriptEntry.addObject("target", arg.asType(EntityTag.class));
            }
            else if (!scriptEntry.hasObject("spread")
                    && arg.matchesInteger()) {
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
        scriptEntry.defaultObject("location", Utilities.entryDefaultLocation(scriptEntry, false));
        if (!scriptEntry.hasObject("entities")) {
            throw new InvalidArgumentsException("Must specify entity/entities!");
        }
        if (!scriptEntry.hasObject("location")) {
            throw new InvalidArgumentsException("Must specify a location!");
        }
    }

    @Override
    public void execute(final ScriptEntry scriptEntry) {
        List<EntityTag> entities = (List<EntityTag>) scriptEntry.getObject("entities");
        LocationTag location = scriptEntry.getObjectTag("location");
        EntityTag target = scriptEntry.getObjectTag("target");
        ElementTag spread = scriptEntry.getElement("spread");
        boolean persistent = scriptEntry.hasObject("persistent");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), db("entities", entities), location, spread, target, (persistent ? db("persistent", "true") : ""));
        }
        // Keep a ListTag of entities that can be called using <entry[name].spawned_entities> later in the script queue
        ListTag entityList = new ListTag();
        // Go through all the entities and spawn them or teleport them, then set their targets if applicable
        for (EntityTag entity : entities) {
            Location loc = location.clone();
            if (spread != null) {
                loc.add(CoreUtilities.getRandom().nextInt(spread.asInt() * 2) - spread.asInt(),
                        0,
                        CoreUtilities.getRandom().nextInt(spread.asInt() * 2) - spread.asInt());
            }
            entity = entity.duplicate();
            entity.spawnAt(loc);
            entityList.addObject(entity);
            if (!entity.isSpawned()) {
                Debug.echoDebug(scriptEntry, "Failed to spawn " + entity + " (blocked by other plugin, script, or gamerule?).");
            }
            else {
                if (persistent && entity.isLivingEntity()) {
                    entity.getLivingEntity().setRemoveWhenFarAway(false);
                }
                if (target != null) {
                    entity.target(target.getLivingEntity());
                }
            }
        }
        // Add entities to context so that the specific entities created/spawned can be fetched.
        scriptEntry.addObject("spawned_entities", entityList);
        if (entities.size() != 0) {
            scriptEntry.addObject("spawned_entity", entityList.getObject(0));
        }
    }
}
