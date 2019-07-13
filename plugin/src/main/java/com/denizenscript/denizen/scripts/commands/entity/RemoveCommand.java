package com.denizenscript.denizen.scripts.commands.entity;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.entity.DenizenEntityType;
import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizen.objects.dWorld;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
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
    //
    // @Tags
    // <e@entity.is_spawned>
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

        for (Argument arg : ArgumentHelper.interpretArguments(scriptEntry.aHArgs)) {

            if (!scriptEntry.hasObject("entities")
                    && arg.matchesArgumentList(dEntity.class)) {
                scriptEntry.addObject("entities", arg.asType(ListTag.class).filter(dEntity.class, scriptEntry));
            }
            else if (!scriptEntry.hasObject("region")
                    && arg.matchesPrefix("region", "r")) {
                scriptEntry.addObject("region", arg.asElement());
            }
            else if (!scriptEntry.hasObject("world")
                    && arg.matchesArgumentType(dWorld.class)) {
                scriptEntry.addObject("world", arg.asType(dWorld.class));
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
                (Utilities.entryHasNPC(scriptEntry) && Utilities.getEntryNPC(scriptEntry).isSpawned()) ? new dWorld(Utilities.getEntryNPC(scriptEntry).getWorld()) : null,
                (Utilities.entryHasPlayer(scriptEntry) && Utilities.getEntryPlayer(scriptEntry).isOnline()) ? new dWorld(Utilities.getEntryPlayer(scriptEntry).getWorld()) : null,
                new dWorld(Bukkit.getWorlds().get(0)));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(final ScriptEntry scriptEntry) {

        // Get objects
        List<dEntity> entities = (List<dEntity>) scriptEntry.getObject("entities");
        dWorld world = (dWorld) scriptEntry.getObject("world");
        ElementTag region = (ElementTag) scriptEntry.getObject("region");

        // Report to dB
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), ArgumentHelper.debugList("entities", entities) +
                    (region != null ? ArgumentHelper.debugObj("region", region) : ""));
        }

        boolean conditionsMet;

        // Go through all of our entities and remove them

        for (dEntity entity : entities) {

            conditionsMet = true;

            // If this is a specific spawned entity, and all
            // other applicable conditions are met, remove it

            if (!entity.isGeneric()) {

                if (region != null) {
                    Debug.echoError(scriptEntry.getResidingQueue(), "Region support is deprecated!");
                    /*conditionsMet = WorldGuardUtilities.inRegion
                                    (entity.getBukkitEntity().getLocation(),
                                    region.asString());*/
                }

                if (conditionsMet) {

                    if (entity.isCitizensNPC()) {
                        entity.getDenizenNPC().getCitizen().destroy();
                    }
                    else {
                        entity.remove();
                    }
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
                    // as our current dEntity, and all other applicable
                    // conditions are met, remove it

                    if (entity.getEntityType().equals(DenizenEntityType.getByEntity(worldEntity))) {

                        if (region != null) {
                            Debug.echoError(scriptEntry.getResidingQueue(), "Region support is deprecated!");
                            /*conditionsMet = WorldGuardUtilities.inRegion
                                            (worldEntity.getLocation(),
                                            region.asString());*/
                        }

                        if (conditionsMet) {
                            worldEntity.remove();
                        }
                    }
                }
            }
        }
    }
}
