package com.denizenscript.denizen.scripts.commands.entity;

import com.denizenscript.denizen.utilities.DenizenAPI;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.List;

public class LookCommand extends AbstractCommand {

    public LookCommand() {
        setName("look");
        setSyntax("look (<entity>|...) [<location>] (duration:<duration>)");
        setRequiredArguments(1, 3);
        isProcedural = false;
    }

    // <--[command]
    // @Name Look
    // @Syntax look (<entity>|...) [<location>] (duration:<duration>)
    // @Required 1
    // @Maximum 3
    // @Short Causes the NPC or other entity to look at a target location.
    // @Group entity
    //
    // @Description
    // Makes the entity look towards the location.
    //
    // Can be used on players.
    //
    // If a duration is set, the entity cannot look away from the location until the duration has expired
    // (unless they are forced to look at a different location).
    //
    // @Tags
    // <LocationTag.yaw>
    // <LocationTag.pitch>
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

        for (Argument arg : scriptEntry.getProcessedArgs()) {

            if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentType(LocationTag.class)) {
                scriptEntry.addObject("location", arg.asType(LocationTag.class));
            }
            else if (!scriptEntry.hasObject("duration")
                    && arg.matchesArgumentType(DurationTag.class)
                    && arg.matchesPrefix("duration", "d")) {
                scriptEntry.addObject("duration", arg.asType(DurationTag.class));
            }
            else if (!scriptEntry.hasObject("entities")
                    && arg.matchesArgumentList(EntityTag.class)) {
                scriptEntry.addObject("entities", arg.asType(ListTag.class).filter(EntityTag.class, scriptEntry));
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("entities")) {
            scriptEntry.defaultObject("entities",
                    Utilities.entryHasNPC(scriptEntry) && Utilities.getEntryNPC(scriptEntry).isSpawned() ? Arrays.asList(Utilities.getEntryNPC(scriptEntry).getDenizenEntity()) : null,
                    Utilities.entryHasPlayer(scriptEntry) && Utilities.getEntryPlayer(scriptEntry).isOnline() ? Arrays.asList(Utilities.getEntryPlayer(scriptEntry).getDenizenEntity()) : null);
        }
        if (!scriptEntry.hasObject("location") || !scriptEntry.hasObject("entities")) {
            throw new InvalidArgumentsException("Must specify a location and entity!");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(ScriptEntry scriptEntry) {
        final LocationTag loc = scriptEntry.getObjectTag("location");
        final List<EntityTag> entities = (List<EntityTag>) scriptEntry.getObject("entities");
        final DurationTag duration = scriptEntry.getObjectTag("duration");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), loc.debug() +
                    ArgumentHelper.debugObj("entities", entities.toString()));
        }
        for (EntityTag entity : entities) {
            if (entity.isSpawned()) {
                NMSHandler.getEntityHelper().faceLocation(entity.getBukkitEntity(), loc);
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
                    for (EntityTag entity : entities) {
                        if (entity.isSpawned()) {
                            NMSHandler.getEntityHelper().faceLocation(entity.getBukkitEntity(), loc);
                        }
                    }
                }
            };
            task.runTaskTimer(DenizenAPI.getCurrentInstance(), 0, 2);
        }
    }
}

