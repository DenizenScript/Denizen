package com.denizenscript.denizen.scripts.commands.entity;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsRuntimeException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;

import java.util.List;

public class BurnCommand extends AbstractCommand {

    public BurnCommand() {
        setName("burn");
        setSyntax("burn [<entity>|...] (duration:<value>)");
        setRequiredArguments(1, 2);
        isProcedural = false;
    }

    // <--[command]
    // @Name Burn
    // @Syntax burn [<entity>|...] (duration:<value>)
    // @Required 1
    // @Maximum 2
    // @Short Sets a list of entities on fire.
    // @Synonyms Ignite,Fire,Torch
    // @Group entity
    //
    // @Description
    // Burn will set a list of entities on fire.
    // Just specify a list of entities (or a single entity) and optionally, a duration.
    // Normal mobs and players will see damage afflicted, but NPCs will block damage from a burn unless 'vulnerable'.
    // Since this command sets the total time of fire, it can also be used to cancel fire on a burning entity by specifying a duration of 0.
    // Specifying no duration will result in a 5 second burn.
    //
    // @Tags
    // <EntityTag.fire_time>
    // <EntityTag.on_fire>
    //
    // @Usage
    // Use to set an entity on fire.
    // - burn <player> duration:10s
    //
    // @Usage
    // Use to cancel fire on entities.
    // - burn <player.location.find.living_entities.within[10]> duration:0
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("entities")
                    && arg.matchesArgumentList(EntityTag.class)) {
                scriptEntry.addObject("entities", arg.asType(ListTag.class).filter(EntityTag.class, scriptEntry));
            }
            else if (!scriptEntry.hasObject("duration")
                    && arg.matchesArgumentType(DurationTag.class)) {
                scriptEntry.addObject("duration", arg.asType(DurationTag.class));
            }
            else {
                arg.reportUnhandled();
            }
        }
        scriptEntry.defaultObject("entities", Utilities.entryDefaultEntityList(scriptEntry, true));
        scriptEntry.defaultObject("duration", new DurationTag(5));
    }

    @Override
    public void execute(final ScriptEntry scriptEntry) {
        List<EntityTag> entities = (List<EntityTag>) scriptEntry.getObject("entities");
        if (entities == null) {
            throw new InvalidArgumentsRuntimeException("Missing entity target input");
        }
        DurationTag duration = scriptEntry.getObjectTag("duration");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), duration, db("entities", entities));
        }
        for (EntityTag entity : entities) {
            if (entity.isSpawned()) {
                entity.getBukkitEntity().setFireTicks(duration.getTicksAsInt());
            }
        }
    }
}
