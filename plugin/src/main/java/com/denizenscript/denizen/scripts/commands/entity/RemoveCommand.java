package com.denizenscript.denizen.scripts.commands.entity;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.entity.DenizenEntityType;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.WorldTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

import java.util.List;

public class RemoveCommand extends AbstractCommand {

    // <--[command]
    // @Name Remove
    // @Syntax remove [<entity>|...] (world:<world>)
    // @Required 1
    // @Short Despawns an entity or list of entities, permanently removing any NPCs.
    // @Group entity
    // @Description
    // Removes the selected entity. May also take a list of entities to remove.
    // Any NPC removed this way is completely removed, as if by '/npc remove'.
    // If a generic entity name is given (see: <@link language entities>)
    // it will remove all entities of that type from the given world.
    // Optionally, you may specifiy a world to target.
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
    // - remove <player.location.find.entities.within[10].exclude[<player>]>
    //
    // @Usage
    // Use to remove all dropped items in the world called cookies.
    // - remove dropped_item world:cookies
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (Argument arg : scriptEntry.getProcessedArgs()) {

            if (!scriptEntry.hasObject("entities")
                    && arg.matchesArgumentList(EntityTag.class)) {
                scriptEntry.addObject("entities", arg.asType(ListTag.class).filter(EntityTag.class, scriptEntry));
            }
            else if (!scriptEntry.hasObject("world")
                    && arg.matchesArgumentType(WorldTag.class)) {
                scriptEntry.addObject("world", arg.asType(WorldTag.class));
            }
            else {
                arg.reportUnhandled();
            }
        }

        // Check to make sure required arguments have been filled

        if (!scriptEntry.hasObject("entities")) {
            throw new InvalidArgumentsException("Must specify entity/entities!");
        }

        // If the world has not been specified, try to use the NPC's or player's
        // world, or default to the specified world in the server properties if necessary

        scriptEntry.defaultObject("world",
                (Utilities.entryHasNPC(scriptEntry) && Utilities.getEntryNPC(scriptEntry).isSpawned()) ? new WorldTag(Utilities.getEntryNPC(scriptEntry).getWorld()) : null,
                (Utilities.entryHasPlayer(scriptEntry) && Utilities.getEntryPlayer(scriptEntry).isOnline()) ? new WorldTag(Utilities.getEntryPlayer(scriptEntry).getWorld()) : null,
                new WorldTag(Bukkit.getWorlds().get(0)));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(final ScriptEntry scriptEntry) {

        // Get objects
        List<EntityTag> entities = (List<EntityTag>) scriptEntry.getObject("entities");
        WorldTag world = (WorldTag) scriptEntry.getObject("world");

        // Report to dB
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), ArgumentHelper.debugList("entities", entities));
        }

        // Go through all of our entities and remove them

        for (EntityTag entity : entities) {

            // If this is a specific spawned entity, and all
            // other applicable conditions are met, remove it

            if (!entity.isGeneric()) {

                if (entity.isCitizensNPC()) {
                    entity.getDenizenNPC().getCitizen().destroy();
                }
                else {
                    entity.remove();
                }
            }

            // If this is a generic unspawned entity, remove
            // all entities of this type from the world (as
            // long as they meet all other conditions)

            else {

                // Note: getting the entities from each loaded chunk
                // in the world (like in Essentials' /killall) has the
                // exact same effect as the below

                for (Entity worldEntity : world.getEntities()) {

                    // If this entity from the world is of the same type
                    // as our current EntityTag, and all other applicable
                    // conditions are met, remove it

                    if (entity.getEntityType().equals(DenizenEntityType.getByEntity(worldEntity))) {

                        worldEntity.remove();
                    }
                }
            }
        }
    }
}
