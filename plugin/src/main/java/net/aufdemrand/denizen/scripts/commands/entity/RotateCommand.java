package net.aufdemrand.denizen.scripts.commands.entity;

import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Duration;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import net.aufdemrand.denizencore.scripts.commands.Holdable;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class RotateCommand extends AbstractCommand implements Holdable {

    // <--[command]
    // @Name Rotate
    // @Syntax rotate (cancel) (<entity>|...) (yaw:<#.#>) (pitch:<#.#>) (infinite/duration:<duration>) (frequency:<duration>)
    // @Required 1
    // @Short Rotates a list of entities.
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
    // @Tags
    // <e@entity.location.yaw>
    // <e@entity.location.pitch>
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

        for (aH.Argument arg : aH.interpretArguments(scriptEntry.aHArgs)) {

            if (!scriptEntry.hasObject("cancel")
                    && (arg.matches("cancel") || arg.matches("stop"))) {

                scriptEntry.addObject("cancel", new Element("true"));
            }
            else if (!scriptEntry.hasObject("infinite")
                    && arg.matches("infinite")) {

                scriptEntry.addObject("infinite", new Element("true"));
            }
            else if (!scriptEntry.hasObject("duration")
                    && arg.matchesArgumentType(Duration.class)
                    && arg.matchesPrefix("duration", "d")) {

                scriptEntry.addObject("duration", arg.asType(Duration.class));
            }
            else if (!scriptEntry.hasObject("frequency")
                    && arg.matchesArgumentType(Duration.class)
                    && arg.matchesPrefix("frequency", "f")) {

                scriptEntry.addObject("frequency", arg.asType(Duration.class));
            }
            else if (!scriptEntry.hasObject("yaw")
                    && arg.matchesPrefix("yaw", "y", "rotation", "r")
                    && arg.matchesPrimitive(aH.PrimitiveType.Float)) {

                scriptEntry.addObject("yaw", arg.asElement());
            }
            else if (!scriptEntry.hasObject("pitch")
                    && arg.matchesPrefix("pitch", "p", "tilt", "t")
                    && arg.matchesPrimitive(aH.PrimitiveType.Float)) {

                scriptEntry.addObject("pitch", arg.asElement());
            }
            else if (!scriptEntry.hasObject("entities")
                    && arg.matchesArgumentList(dEntity.class)) {

                scriptEntry.addObject("entities", arg.asType(dList.class).filter(dEntity.class, scriptEntry));
            }
            else {
                arg.reportUnhandled();
            }
        }

        // Use the NPC or the Player as the default entity
        scriptEntry.defaultObject("entities",
                (Utilities.entryHasPlayer(scriptEntry) ? Arrays.asList(Utilities.getEntryPlayer(scriptEntry).getDenizenEntity()) : null),
                (Utilities.entryHasNPC(scriptEntry) ? Arrays.asList(Utilities.getEntryNPC(scriptEntry).getDenizenEntity()) : null));

        scriptEntry.defaultObject("yaw", new Element(10));
        scriptEntry.defaultObject("pitch", new Element(0));
        scriptEntry.defaultObject("duration", new Duration(20));
        scriptEntry.defaultObject("frequency", Duration.valueOf("1t"));

        // Check to make sure required arguments have been filled
        if (!scriptEntry.hasObject("entities")) {
            throw new InvalidArgumentsException("Must specify entity/entities!");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(final ScriptEntry scriptEntry) {

        final List<dEntity> entities = new ArrayList<>((List<dEntity>) scriptEntry.getObject("entities"));
        final Duration duration = (Duration) scriptEntry.getObject("duration");
        final Duration frequency = (Duration) scriptEntry.getObject("frequency");
        final Element yaw = (Element) scriptEntry.getObject("yaw");
        final Element pitch = (Element) scriptEntry.getObject("pitch");
        boolean cancel = scriptEntry.hasObject("cancel");
        final boolean infinite = scriptEntry.hasObject("infinite");

        // Report to dB
        if (scriptEntry.dbCallShouldDebug()) {
            dB.report(scriptEntry, getName(), (cancel ? aH.debugObj("cancel", cancel) : "") +
                    aH.debugObj("entities", entities.toString()) +
                    (infinite ? aH.debugObj("duration", "infinite") : duration.debug()) +
                    frequency.debug() +
                    yaw.debug() +
                    pitch.debug());
        }

        // Add entities to the rotatingEntities list or remove
        // them from it
        for (dEntity entity : entities) {
            if (cancel) {
                rotatingEntities.remove(entity.getUUID());
            }
            else {
                rotatingEntities.add(entity.getUUID());
            }
        }

        // Go no further if we are canceling a rotation
        if (cancel) {
            return;
        }

        // Run a task that will keep rotating the entities
        BukkitRunnable task = new BukkitRunnable() {
            int ticks = 0;
            int maxTicks = duration.getTicksAsInt();

            // Track entities that are no longer used, to remove them from
            // the regular list
            Collection<dEntity> unusedEntities = new LinkedList<>();

            @Override
            public void run() {

                if (entities.isEmpty()) {
                    scriptEntry.setFinished(true);
                    this.cancel();
                }
                else if (infinite || ticks < maxTicks) {
                    for (dEntity entity : entities) {
                        if (entity.isSpawned() && rotatingEntities.contains(entity.getUUID())) {
                            NMSHandler.getInstance().getEntityHelper().rotate(entity.getBukkitEntity(),
                                    NMSHandler.getInstance().getEntityHelper().normalizeYaw(entity.getLocation().getYaw() + yaw.asFloat()),
                                    entity.getLocation().getPitch() + pitch.asFloat());
                        }
                        else {
                            rotatingEntities.remove(entity.getUUID());
                            unusedEntities.add(entity);
                        }
                    }

                    // Remove any entities that are no longer spawned
                    if (!unusedEntities.isEmpty()) {
                        for (dEntity unusedEntity : unusedEntities) {
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
        task.runTaskTimer(DenizenAPI.getCurrentInstance(), 0, frequency.getTicks());
    }
}
