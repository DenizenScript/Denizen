package com.denizenscript.denizen.scripts.commands.entity;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.utilities.PaperAPITools;
import com.denizenscript.denizen.utilities.packets.NetworkInterceptHelper;
import com.denizenscript.denizencore.DenizenCore;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsRuntimeException;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.generator.ArgDefaultNull;
import com.denizenscript.denizencore.scripts.commands.generator.ArgLinear;
import com.denizenscript.denizencore.scripts.commands.generator.ArgName;
import com.denizenscript.denizencore.scripts.commands.generator.ArgPrefixed;
import com.denizenscript.denizencore.utilities.Deprecations;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class LookCommand extends AbstractCommand {

    public LookCommand() {
        setName("look");
        setSyntax("look (<entity>|...) [<location>/cancel/yaw:<yaw> pitch:<pitch>] (duration:<duration>) (offthread_repeat:<#>)");
        setRequiredArguments(1, 5);
        isProcedural = false;
        addRemappedPrefixes("duration", "d");
        autoCompile();
    }

    // <--[command]
    // @Name Look
    // @Syntax look (<entity>|...) [<location>/cancel/yaw:<yaw> pitch:<pitch>] (duration:<duration>) (offthread_repeat:<#>)
    // @Required 1
    // @Maximum 5
    // @Short Causes the NPC or other entity to look at a target location.
    // @Synonyms Turn,Face
    // @Group entity
    //
    // @Description
    // Makes the entity look towards the location.
    //
    // You can specify either a target location, or a yaw and pitch.
    //
    // Can be used on players.
    //
    // If a duration is set, the entity cannot look away from the location until the duration has expired.
    // Use the cancel argument to end the duration earlier.
    //
    // Optionally, you can use the "offthread_repeat:" option alongside "yaw:" and "pitch:"
    // to cause a player's rotation to be smoothed out with a specified number of extra async rotation packets within a single tick.
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

    public static HashMap<UUID, BukkitTask> lookTasks = new HashMap<>();

    public static void autoExecute(ScriptEntry scriptEntry,
                                   @ArgName("entities") @ArgDefaultNull @ArgLinear ObjectTag entitiesObj,
                                   @ArgName("location") @ArgDefaultNull @ArgLinear ObjectTag locationObj,
                                   @ArgName("duration") @ArgDefaultNull @ArgPrefixed DurationTag duration,
                                   @ArgName("yaw") @ArgDefaultNull @ArgPrefixed ElementTag yaw,
                                   @ArgName("pitch") @ArgDefaultNull @ArgPrefixed ElementTag pitch,
                                   @ArgName("offthread_repeat") @ArgDefaultNull @ArgPrefixed ElementTag offthreadRepeats) {
        if (!(entitiesObj instanceof ListTag)) {
            String entStr = entitiesObj.asElement().asLowerString();
            if (entStr.equals("cancel") || entStr.startsWith("l@")) {
                Deprecations.outOfOrderArgs.warn(scriptEntry);
                ObjectTag swap = entitiesObj;
                entitiesObj = locationObj;
                locationObj = swap;
            }
        }
        List<EntityTag> entities = entitiesObj.asType(ListTag.class, scriptEntry.context).filter(EntityTag.class, scriptEntry);
        for (EntityTag entity : entities) {
            if (entity.isSpawned()) {
                BukkitTask task = lookTasks.remove(entity.getUUID());
                if (task != null) {
                    task.cancel();
                }
            }
        }
        if (locationObj != null && !(locationObj instanceof LocationTag) && locationObj.asElement().asLowerString().equals("cancel")) {
            return;
        }
        LocationTag loc = locationObj == null ? null : locationObj.asType(LocationTag.class, scriptEntry.context);
        if (loc == null && yaw == null && pitch == null) {
            throw new InvalidArgumentsRuntimeException("Missing or invalid Location input!");
        }
        final float yawRaw = yaw == null ? 0 : yaw.asFloat();
        final float pitchRaw = pitch == null ? 0 : pitch.asFloat();
        for (EntityTag entity : entities) {
            if (entity.isSpawned()) {
                if (loc != null) {
                    NMSHandler.entityHelper.faceLocation(entity.getBukkitEntity(), loc);
                }
                else {
                    if (entity.isPlayer()) {
                        Location playerTeleDest = entity.getLocation().clone();
                        float relYaw = (yawRaw - playerTeleDest.getYaw()) % 360;
                        if (relYaw > 180) {
                            relYaw -= 360;
                        }
                        final float actualRelYaw = relYaw;
                        float relPitch = pitchRaw - playerTeleDest.getPitch();
                        playerTeleDest.setYaw(yawRaw);
                        playerTeleDest.setPitch(pitchRaw);
                        Player player = entity.getPlayer();
                        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19)) {
                            NetworkInterceptHelper.enable();
                            NMSHandler.packetHelper.sendRelativeLookPacket(player, actualRelYaw, relPitch);
                        }
                        else {
                            PaperAPITools.instance.teleport(player, playerTeleDest, PlayerTeleportEvent.TeleportCause.PLUGIN, null, Arrays.asList(TeleportCommand.Relative.values()));
                        }
                        if (offthreadRepeats != null) {
                            NetworkInterceptHelper.enable();
                            int times = offthreadRepeats.asInt();
                            int ms = 50 / (times + 1);
                            DenizenCore.runAsync(() -> {
                                try {
                                    for (int i = 0; i < times; i++) {
                                        Thread.sleep(ms);
                                        NMSHandler.packetHelper.sendRelativeLookPacket(player, actualRelYaw, relPitch);
                                    }
                                }
                                catch (Throwable ex) {
                                    Debug.echoError(ex);
                                }
                            });
                        }
                    }
                    else {
                        NMSHandler.entityHelper.rotate(entity.getBukkitEntity(), yawRaw, pitchRaw);
                    }
                }
            }
        }
        if (duration != null && duration.getTicks() > 1) {
            for (EntityTag entity : entities) {
                BukkitRunnable task = new BukkitRunnable() {
                    long bounces = 0;
                    public void run() {
                        bounces++;
                        if (bounces > duration.getTicks()) {
                            this.cancel();
                            lookTasks.remove(entity.getUUID());
                            return;
                        }
                        if (entity.isSpawned()) {
                            if (loc != null) {
                                NMSHandler.entityHelper.faceLocation(entity.getBukkitEntity(), loc);
                            }
                            else {
                                NMSHandler.entityHelper.rotate(entity.getBukkitEntity(), yawRaw, pitchRaw);
                            }
                        }
                    }
                };
                BukkitTask newTask = task.runTaskTimer(Denizen.getInstance(), 0, 1);
                lookTasks.put(entity.getUUID(), newTask);
            }
        }
    }
}
