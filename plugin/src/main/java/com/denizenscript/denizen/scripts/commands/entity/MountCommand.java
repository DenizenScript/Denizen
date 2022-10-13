package com.denizenscript.denizen.scripts.commands.entity;

import com.denizenscript.denizen.utilities.Conversion;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.entity.Position;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;

import java.util.List;

public class MountCommand extends AbstractCommand {

    public MountCommand() {
        setName("mount");
        setSyntax("mount (cancel) [<entity>|...] (<location>)");
        setRequiredArguments(0, 3);
        isProcedural = false;
    }

    // <--[command]
    // @Name Mount
    // @Syntax mount (cancel) [<entity>|...] (<location>)
    // @Required 0
    // @Maximum 3
    // @Short Mounts one entity onto another.
    // @Group entity
    //
    // @Description
    // Mounts an entity onto another as though in a vehicle.
    // Can be used to force a player into a vehicle or to mount an entity onto another entity, for example a player onto an NPC.
    // If the entity(s) don't exist they will be spawned.
    // Accepts a location, which the entities will be teleported to on mounting.
    //
    // @Tags
    // <EntityTag.vehicle>
    // <EntityTag.is_inside_vehicle>
    // <entry[saveName].mounted_entities> returns a list of entities that were mounted.
    //
    // @Usage
    // Use to mount an NPC on top of a player.
    // - mount <npc>|<player>
    //
    // @Usage
    // Use to spawn a mutant pile of mobs.
    // - mount cow|pig|sheep|chicken
    //
    // @Usage
    // Use to place a diamond block above a player's head.
    // - mount falling_block,diamond_block|<player>
    //
    // @Usage
    // Use to force an entity in a vehicle.
    // - mount <player>|boat
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        List<EntityTag> entities = null;
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("cancel")
                    && arg.matches("cancel")) {
                scriptEntry.addObject("cancel", "");
            }
            else if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentType(LocationTag.class)) {
                scriptEntry.addObject("location", arg.asType(LocationTag.class));
                scriptEntry.addObject("custom_location", new ElementTag(true));
            }
            else if (!scriptEntry.hasObject("entities")
                    && arg.matchesArgumentList(EntityTag.class)) {
                entities = arg.asType(ListTag.class).filter(EntityTag.class, scriptEntry);
                scriptEntry.addObject("entities", entities);
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("entities")) {
            throw new InvalidArgumentsException("Must specify entity/entities!");
        }
        if (!scriptEntry.hasObject("location")) {
            if (entities != null) {
                for (int i = entities.size() - 1; i >= 0; i--) {
                    if (entities.get(i).isSpawned()) {
                        scriptEntry.defaultObject("location", entities.get(i).getLocation());
                        break;
                    }
                }
            }
            scriptEntry.defaultObject("location", Utilities.entryDefaultLocation(scriptEntry, true));
        }
        if (!scriptEntry.hasObject("location")) {
            throw new InvalidArgumentsException("Must specify a location!");
        }
    }

    @Override
    public void execute(final ScriptEntry scriptEntry) {
        LocationTag location = scriptEntry.getObjectTag("location");
        boolean hasCustomLocation = scriptEntry.hasObject("custom_location");
        List<EntityTag> entities = (List<EntityTag>) scriptEntry.getObject("entities");
        boolean cancel = scriptEntry.hasObject("cancel");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), (cancel ? db("cancel", true) : ""), location, db("entities", entities));
        }
        if (!cancel) {
            for (EntityTag entity : entities) {
                if (!entity.isSpawned() || hasCustomLocation) {
                    entity.spawnAt(location);
                }
            }
            Position.mount(Conversion.convertEntities(entities));
        }
        else {
            Position.dismount(Conversion.convertEntities(entities));
        }
        ListTag entityList = new ListTag();
        entityList.addObjects((List) entities);
        scriptEntry.addObject("mounted_entities", entityList);
    }
}
