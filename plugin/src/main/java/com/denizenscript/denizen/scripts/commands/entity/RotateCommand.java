package com.denizenscript.denizen.scripts.commands.entity;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.nms.interfaces.EntityHelper;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.Holdable;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class RotateCommand extends AbstractCommand implements Holdable {

    public RotateCommand() {
        setName("rotate");
        setSyntax("rotate (cancel) (<entity>|...) (yaw:<#.#>) (pitch:<#.#>) (infinite/duration:<duration>) (frequency:<duration>)");
        setRequiredArguments(1, 6);
        isProcedural = false;
    }

    // <--[command]
    // @Name Rotate
    // @Syntax rotate (cancel) (<entity>|...) (yaw:<#.#>) (pitch:<#.#>) (infinite/duration:<duration>) (frequency:<duration>)
    // @Required 1
    // @Maximum 6
    // @Short Rotates a list of entities.
    // @Synonyms Spin
    // @Group entity
    //
    // @Description
    // Induces incremental rotation on a list of entities over a period of time.
    //
    // The yaw and pitch arguments specify how much the entity will rotate each step. Default to 10 and 0 respectively.
    //
    // The frequency argument specifies how long it takes between each rotation step. Defaults to 1t.
    //
    // The duration argument specifies how long the whole rotation process will last. Defaults to 1s.
    // Alternatively, use "infinite" if you want the entity to spin forever.
    //
    // You can use "cancel" to prematurely stop the ongoing rotation (useful when set to infinite)
    //
    // The rotate command is ~waitable. Refer to <@link language ~waitable>.
    //
    // @Tags
    // <EntityTag.location>
    //
    // @Usage
    // Use to rotate the player's yaw by 10 every tick for 3 seconds total
    // - rotate <player> duration:3s
    //
    // @Usage
    // Use to rotate the player's pitch by 20 every 5 ticks for a second total
    // - rotate <player> yaw:0.0 pitch:20.0 frequency:5t
    //
    // @Usage
    // Use to prematurely stop the player's rotation
    // - rotate cancel <player>
    // -->

    public static Set<UUID> rotatingEntities = new HashSet<>();

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("cancel")
                    && (arg.matches("cancel") || arg.matches("stop"))) {
                scriptEntry.addObject("cancel", new ElementTag("true"));
            }
            else if (!scriptEntry.hasObject("infinite")
                    && arg.matches("infinite")) {
                scriptEntry.addObject("infinite", new ElementTag("true"));
            }
            else if (!scriptEntry.hasObject("duration")
                    && arg.matchesArgumentType(DurationTag.class)
                    && arg.matchesPrefix("duration", "d")) {
                scriptEntry.addObject("duration", arg.asType(DurationTag.class));
            }
            else if (!scriptEntry.hasObject("frequency")
                    && arg.matchesArgumentType(DurationTag.class)
                    && arg.matchesPrefix("frequency", "f")) {
                scriptEntry.addObject("frequency", arg.asType(DurationTag.class));
            }
            else if (!scriptEntry.hasObject("yaw")
                    && arg.matchesPrefix("yaw", "y", "rotation", "r")
                    && arg.matchesFloat()) {
                scriptEntry.addObject("yaw", arg.asElement());
            }
            else if (!scriptEntry.hasObject("pitch")
                    && arg.matchesPrefix("pitch", "p", "tilt", "t")
                    && arg.matchesFloat()) {
                scriptEntry.addObject("pitch", arg.asElement());
            }
            else if (!scriptEntry.hasObject("entities")
                    && arg.matchesArgumentList(EntityTag.class)) {
                scriptEntry.addObject("entities", arg.asType(ListTag.class).filter(EntityTag.class, scriptEntry));
            }
            else {
                arg.reportUnhandled();
            }
        }
        scriptEntry.defaultObject("entities", Utilities.entryDefaultEntityList(scriptEntry, true));
        scriptEntry.defaultObject("yaw", new ElementTag(10));
        scriptEntry.defaultObject("pitch", new ElementTag(0));
        scriptEntry.defaultObject("duration", new DurationTag(20));
        scriptEntry.defaultObject("frequency", new DurationTag(1L));
        if (!scriptEntry.hasObject("entities")) {
            throw new InvalidArgumentsException("Must specify entity/entities!");
        }
    }

    @Override
    public void execute(final ScriptEntry scriptEntry) {
        final List<EntityTag> entities = new ArrayList<>((List<EntityTag>) scriptEntry.getObject("entities"));
        final DurationTag duration = scriptEntry.getObjectTag("duration");
        final DurationTag frequency = scriptEntry.getObjectTag("frequency");
        final ElementTag yaw = scriptEntry.getElement("yaw");
        final ElementTag pitch = scriptEntry.getElement("pitch");
        boolean cancel = scriptEntry.hasObject("cancel");
        final boolean infinite = scriptEntry.hasObject("infinite");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), (cancel ? db("cancel", true) : ""), db("entities", entities),
                    (infinite ? db("duration", "infinite") : duration), frequency, yaw, pitch);
        }
        for (EntityTag entity : entities) {
            if (cancel) {
                rotatingEntities.remove(entity.getUUID());
            }
            else {
                rotatingEntities.add(entity.getUUID());
            }
        }
        if (cancel) {
            return;
        }
        BukkitRunnable task = new BukkitRunnable() {
            int ticks = 0;
            int maxTicks = duration.getTicksAsInt();
            ArrayList<EntityTag> unusedEntities = new ArrayList<>();
            @Override
            public void run() {
                if (entities.isEmpty()) {
                    scriptEntry.setFinished(true);
                    this.cancel();
                }
                else if (infinite || ticks < maxTicks) {
                    for (EntityTag entity : entities) {
                        if (entity.isSpawned() && rotatingEntities.contains(entity.getUUID())) {
                            NMSHandler.entityHelper.rotate(entity.getBukkitEntity(),
                                    EntityHelper.normalizeYaw(entity.getLocation().getYaw() + yaw.asFloat()),
                                    entity.getLocation().getPitch() + pitch.asFloat());
                        }
                        else {
                            rotatingEntities.remove(entity.getUUID());
                            unusedEntities.add(entity);
                        }
                    }
                    if (!unusedEntities.isEmpty()) {
                        for (EntityTag unusedEntity : unusedEntities) {
                            entities.remove(unusedEntity);
                        }
                        unusedEntities.clear();
                    }
                    ticks = (int) (ticks + frequency.getTicks());
                }
                else {
                    scriptEntry.setFinished(true);
                    this.cancel();
                }
            }
        };
        task.runTaskTimer(Denizen.getInstance(), 0, frequency.getTicks());
    }
}
