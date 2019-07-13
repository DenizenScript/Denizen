package com.denizenscript.denizen.scripts.commands.entity;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.dB;
import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.Duration;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.dList;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;

import java.util.Arrays;
import java.util.List;

public class BurnCommand extends AbstractCommand {

    // <--[command]
    // @Name Burn
    // @Syntax burn [<entity>|...] (duration:<value>)
    // @Required 1
    // @Short Sets a list of entities on fire.
    // @Group entity
    //
    // @Description
    // Burn will set a list of entities on fire. Just specify a list of entities (or a single entity) and
    // optionally, a duration. Normal mobs and players will see damage afflicted, but NPCs will block damage
    // from a burn unless 'vulnerable'. Since this command sets the total time of fire, it can also be used
    // to cancel fire on a burning entity by specifying a duration of 0. Specifying no duration will result
    // in a 5 second burn.
    //
    // @Tags
    // <e@entity.fire_time>
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

        for (Argument arg : ArgumentHelper.interpretArguments(scriptEntry.aHArgs)) {

            if (!scriptEntry.hasObject("entities")
                    && arg.matchesArgumentList(dEntity.class)) {

                scriptEntry.addObject("entities", arg.asType(dList.class).filter(dEntity.class, scriptEntry));
            }
            else if (!scriptEntry.hasObject("duration")
                    && arg.matchesArgumentType(Duration.class)) {

                scriptEntry.addObject("duration", arg.asType(Duration.class));
            }
            else {
                arg.reportUnhandled();
            }
        }

        // Use the NPC or the Player as the default entity
        scriptEntry.defaultObject("entities",
                (Utilities.entryHasPlayer(scriptEntry) ? Arrays.asList(Utilities.getEntryPlayer(scriptEntry).getDenizenEntity()) : null),
                (Utilities.entryHasNPC(scriptEntry) ? Arrays.asList(Utilities.getEntryNPC(scriptEntry).getDenizenEntity()) : null));

        // Use default duration if one is not specified
        scriptEntry.defaultObject("duration", Duration.valueOf("5s"));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(final ScriptEntry scriptEntry) {

        // Get objects
        List<dEntity> entities = (List<dEntity>) scriptEntry.getObject("entities");
        Duration duration = (Duration) scriptEntry.getObject("duration");

        // Report to dB
        if (scriptEntry.dbCallShouldDebug()) {
            dB.report(scriptEntry, getName(), duration.debug() +
                    ArgumentHelper.debugObj("entities", entities.toString()));
        }

        // Go through all the entities and set them on fire
        for (dEntity entity : entities) {
            if (entity.isSpawned()) {
                entity.getBukkitEntity().setFireTicks(duration.getTicksAsInt());
            }
        }
    }
}
