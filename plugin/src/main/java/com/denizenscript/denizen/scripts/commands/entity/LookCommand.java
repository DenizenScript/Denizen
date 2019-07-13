package com.denizenscript.denizen.scripts.commands.entity;

import com.denizenscript.denizen.utilities.DenizenAPI;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizen.objects.dLocation;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.DurationTag;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.List;

public class LookCommand extends AbstractCommand {

    // <--[command]
    // @Name Look
    // @Syntax look (<entity>|...) [<location>] (duration:<duration>)
    // @Required 1
    // @Short Causes the NPC or other entity to look at a target location.
    // @Group entity
    //
    // @Description
    // Makes the entity look towards the location, can be used on players. If a duration is set, the entity cannot
    // look away from the location until the duration has expired unless they are forces to look at a different
    // location.
    //
    // @Tags
    // <l@location.yaw>
    // <l@location.pitch>
    //
    // @Usage
    // Use to point an npc towards a spot.
    // - look <npc> <player.location>
    //
    // @Usage
    // Use to force a player to stare at a spot for some time.
    // - look <player> <npc.location> duration:10s
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (Argument arg : ArgumentHelper.interpretArguments(scriptEntry.aHArgs)) {

            if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentType(dLocation.class)) {
                scriptEntry.addObject("location", arg.asType(dLocation.class));
            }
            else if (!scriptEntry.hasObject("duration")
                    && arg.matchesArgumentType(DurationTag.class)
                    && arg.matchesPrefix("duration", "d")) {
                scriptEntry.addObject("duration", arg.asType(DurationTag.class));
            }
            else if (!scriptEntry.hasObject("entities")
                    && arg.matchesArgumentList(dEntity.class)) {
                // Entity arg
                scriptEntry.addObject("entities", arg.asType(ListTag.class).filter(dEntity.class, scriptEntry));
            }
            else {
                arg.reportUnhandled();
            }
        }

        // Use the NPC or player as the entity if no entities are specified

        scriptEntry.defaultObject("entities",
                Utilities.entryHasNPC(scriptEntry) ? Arrays.asList(Utilities.getEntryNPC(scriptEntry).getDenizenEntity()) : null,
                Utilities.entryHasPlayer(scriptEntry) ? Arrays.asList(Utilities.getEntryPlayer(scriptEntry).getDenizenEntity()) : null);

        if (!scriptEntry.hasObject("location") || !scriptEntry.hasObject("entities")) {
            throw new InvalidArgumentsException("Must specify a location and entity!");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(ScriptEntry scriptEntry) {

        final dLocation loc = (dLocation) scriptEntry.getObject("location");
        final List<dEntity> entities = (List<dEntity>) scriptEntry.getObject("entities");
        final DurationTag duration = (DurationTag) scriptEntry.getObject("duration");

        if (scriptEntry.dbCallShouldDebug()) {

            Debug.report(scriptEntry, getName(), loc.debug() +
                    ArgumentHelper.debugObj("entities", entities.toString()));

        }

        for (dEntity entity : entities) {
            if (entity.isSpawned()) {
                NMSHandler.getInstance().getEntityHelper().faceLocation(entity.getBukkitEntity(), loc);
            }
        }
        if (duration != null && duration.getTicks() > 2) {
            BukkitRunnable task = new BukkitRunnable() {
                long bounces = 0;

                public void run() {
                    bounces += 2;
                    if (bounces > duration.getTicks()) {
                        this.cancel();
                        return;
                    }
                    for (dEntity entity : entities) {
                        if (entity.isSpawned()) {
                            NMSHandler.getInstance().getEntityHelper().faceLocation(entity.getBukkitEntity(), loc);
                        }
                    }
                }
            };
            task.runTaskTimer(DenizenAPI.getCurrentInstance(), 0, 2);
        }
    }
}

