package com.denizenscript.denizen.scripts.commands.entity;
import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class LookCommand extends AbstractCommand {

    public LookCommand() {
        setName("look");
        setSyntax("look (<entity>|...) [<location>/cancel] (duration:<duration>)");
        setRequiredArguments(1, 3);
        isProcedural = false;
    }

    // <--[command]
    // @Name Look
    // @Syntax look (<entity>|...) [<location>/cancel] (duration:<duration>)
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
    // If a duration is set, the entity cannot look away from the location until the duration has expired.
    // Use the cancel argument to end the duration earlier.
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
                    && !scriptEntry.hasObject("cancel")
                    && arg.matchesArgumentType(LocationTag.class)) {
                scriptEntry.addObject("location", arg.asType(LocationTag.class));
            }
            else if (!scriptEntry.hasObject("cancel")
                    && !scriptEntry.hasObject("location")
                    && arg.matches("cancel")) {
                scriptEntry.addObject("cancel", new ElementTag("true"));
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
            scriptEntry.defaultObject("entities", Utilities.entryDefaultEntityList(scriptEntry, false));
        }
        if (!scriptEntry.hasObject("location") && !scriptEntry.hasObject("cancel")) {
            throw new InvalidArgumentsException("Must specify a location or 'cancel'!");
        }
        if (!scriptEntry.hasObject("entities")) {
            throw new InvalidArgumentsException("Must specify an entity!");
        }
    }

    public static HashMap<UUID, BukkitTask> lookTasks = new HashMap<>();

    @SuppressWarnings("unchecked")
    @Override
    public void execute(ScriptEntry scriptEntry) {
        final LocationTag loc = scriptEntry.getObjectTag("location");
        List<EntityTag> entities = (List<EntityTag>) scriptEntry.getObject("entities");
        final DurationTag duration = scriptEntry.getObjectTag("duration");
        ElementTag cancel = scriptEntry.getElement("cancel");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), (cancel != null ? cancel.debug() : loc.debug()) +
                    ArgumentHelper.debugObj("entities", entities.toString()));
        }
        for (EntityTag entity : entities) {
            if (entity.isSpawned()) {
                BukkitTask task = lookTasks.remove(entity.getUUID());
                if (task != null) {
                    task.cancel();
                }
            }
        }
        if (cancel != null && cancel.asBoolean()) {
            return;
        }
        for (EntityTag entity : entities) {
            if (entity.isSpawned()) {
                NMSHandler.getEntityHelper().faceLocation(entity.getBukkitEntity(), loc);
            }
        }
        if (duration != null && duration.getTicks() > 2) {
            for (EntityTag entity : entities) {
                BukkitRunnable task = new BukkitRunnable() {
                    long bounces = 0;
                    public void run() {
                        bounces += 2;
                        if (bounces > duration.getTicks()) {
                            this.cancel();
                            lookTasks.remove(entity.getUUID());
                            return;
                        }
                        if (entity.isSpawned()) {
                            NMSHandler.getEntityHelper().faceLocation(entity.getBukkitEntity(), loc);
                        }
                    }
                };
                BukkitTask newTask = task.runTaskTimer(Denizen.getInstance(), 0, 2);
                lookTasks.put(entity.getUUID(), newTask);
            }
        }
    }
}

