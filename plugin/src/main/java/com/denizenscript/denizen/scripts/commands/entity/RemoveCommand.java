package com.denizenscript.denizen.scripts.commands.entity;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.entity.DenizenEntityType;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.WorldTag;
import com.denizenscript.denizen.utilities.entity.FakeEntity;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.List;

public class RemoveCommand extends AbstractCommand {

    public RemoveCommand() {
        setName("remove");
        setSyntax("remove [<entity>|...] (world:<world>)");
        setRequiredArguments(1, 2);
        isProcedural = false;
    }

    // <--[command]
    // @Name Remove
    // @Syntax remove [<entity>|...] (world:<world>)
    // @Required 1
    // @Maximum 2
    // @Short Despawns an entity or list of entities, permanently removing any NPCs.
    // @Group entity
    // @Description
    // Removes the selected entity. May also take a list of entities to remove.
    //
    // Any NPC removed this way is completely removed, as if by '/npc remove'.
    // For temporary NPC removal, see <@link command despawn>.
    //
    // If a generic entity name is given (like 'zombie'), this will remove all entities of that type from the given world.
    // Optionally, you may specify a world to target.
    // (Defaults to the world of the player running the command)
    //
    // @Tags
    // <EntityTag.is_spawned>
    //
    // @Usage
    // Use to remove the entity the player is looking at.
    // - remove <player.target>
    //
    // @Usage
    // Use to remove all nearby entities around the player, excluding the player itself.
    // - remove <player.location.find_entities.within[10].exclude[<player>]>
    //
    // @Usage
    // Use to remove all dropped items in the world called cookies.
    // - remove dropped_item world:cookies
    // -->

    @Override
    public void addCustomTabCompletions(TabCompletionsBuilder tab) {
        tab.add(EntityType.values());
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
            else if (!scriptEntry.hasObject("world")
                    && arg.matchesArgumentType(WorldTag.class)) {
                scriptEntry.addObject("world", arg.asType(WorldTag.class));
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("entities")) {
            throw new InvalidArgumentsException("Must specify entity/entities!");
        }
        scriptEntry.defaultObject("world", Utilities.entryDefaultWorld(scriptEntry, false));
    }

    public static boolean alwaysWarnOnMassRemove = false;

    @Override
    public void execute(final ScriptEntry scriptEntry) {
        List<EntityTag> entities = (List<EntityTag>) scriptEntry.getObject("entities");
        WorldTag world = scriptEntry.getObjectTag("world");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), world, db("entities", entities));
        }
        for (EntityTag entity : entities) {
            if (entity.isUnique()) {
                if (entity.isFake) {
                    FakeEntity fakeEnt = FakeEntity.idsToEntities.get(entity.getUUID());
                    if (fakeEnt != null) {
                        fakeEnt.cancelEntity();
                    }
                }
                else if (entity.isCitizensNPC()) {
                    entity.getDenizenNPC().getCitizen().destroy();
                }
                else {
                    if (!entity.isSpawned()) {
                        Debug.echoError("Tried to remove already-removed entity.");
                        // Still remove() anyway to compensate for Spigot/NMS bugs
                    }
                    entity.remove();
                }
            }
            else {
                int removed = 0;
                for (Entity worldEntity : world.getEntities()) {
                    if (entity.getEntityType().equals(DenizenEntityType.getByEntity(worldEntity))) {
                        worldEntity.remove();
                        removed++;
                    }
                }
                Debug.echoDebug(scriptEntry, "Removed " + removed + " entities from the world.");
                if (alwaysWarnOnMassRemove) {
                    Debug.echoError("Remove command 'Always warn on mass delete' in Denizen config is enabled - mass removal of '" + entity.getEntityType() + "' performed, removing " + removed + " entities.");
                }
            }
        }
    }
}
