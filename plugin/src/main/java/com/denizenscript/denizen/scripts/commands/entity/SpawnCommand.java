package com.denizenscript.denizen.scripts.commands.entity;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.command.TabCompleteHelper;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsRuntimeException;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.commands.generator.*;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.Location;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.List;

public class SpawnCommand extends AbstractCommand {

    public SpawnCommand() {
        setName("spawn");
        setSyntax("spawn [<entity>|...] (<location>) (target:<entity>) (persistent) (reason:<reason>)");
        setRequiredArguments(1, 5);
        isProcedural = false;
        autoCompile();
    }

    // <--[command]
    // @Name Spawn
    // @Syntax spawn [<entity>|...] (<location>) (target:<entity>) (persistent) (reason:<reason>)
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
    // Optionally specify 'reason:<reason>' (Paper only) to specify the reason an entity is spawning for the 'entity spawns' event,
    // using any reason from <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/entity/CreatureSpawnEvent.SpawnReason.html>
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
        TabCompleteHelper.tabCompleteEntityTypes(tab);
        tab.addWithPrefix("reason", CreatureSpawnEvent.SpawnReason.values());
    }

    public static void autoExecute(final ScriptEntry scriptEntry,
                                   @ArgLinear @ArgName("entities") ObjectTag entityListInput,
                                   @ArgDefaultNull @ArgLinear @ArgName("location") ObjectTag locationInput,
                                   @ArgDefaultNull @ArgPrefixed @ArgName("target") EntityTag target,
                                   @ArgDefaultNull @ArgPrefixed @ArgName("spread") ElementTag spread, // TODO: proper native optional int support somehow?
                                   @ArgName("persistent") boolean persistent,
                                   @ArgDefaultText("custom") @ArgPrefixed @ArgName("reason") CreatureSpawnEvent.SpawnReason reason) {
        if (locationInput != null && entityListInput.shouldBeType(LocationTag.class)) {
            ObjectTag swap = locationInput;
            locationInput = entityListInput;
            entityListInput = swap;
        }
        LocationTag location = locationInput == null ? Utilities.entryDefaultLocation(scriptEntry, false) : locationInput.asType(LocationTag.class, scriptEntry.context);
        if (location == null) {
            throw new InvalidArgumentsRuntimeException("Must specify a location!");
        }
        EntityTag.allowDespawnedNpcs = true;
        List<EntityTag> entities = entityListInput.asType(ListTag.class, scriptEntry.context).filter(EntityTag.class, scriptEntry.context);
        EntityTag.allowDespawnedNpcs = false;
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
            entity.spawnAt(loc, PlayerTeleportEvent.TeleportCause.PLUGIN, reason);
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
