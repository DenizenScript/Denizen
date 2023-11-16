package com.denizenscript.denizen.scripts.commands.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.PaperAPITools;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.packets.NetworkInterceptHelper;
import com.denizenscript.denizencore.DenizenCore;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsRuntimeException;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.generator.*;
import com.denizenscript.denizencore.utilities.Deprecations;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.citizensnpcs.trait.CurrentLocation;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.List;

public class TeleportCommand extends AbstractCommand {

    public TeleportCommand() {
        setName("teleport");
        setSyntax("teleport (<entity>|...) [<location>] (cause:<cause>) (entity_options:<option>|...) (relative) (relative_axes:<axis>|...) (offthread_repeat:<#>) (offthread_yaw) (offthread_pitch)");
        setRequiredArguments(1, 9);
        isProcedural = false;
        autoCompile();
    }

    // <--[command]
    // @Name Teleport
    // @Syntax teleport (<entity>|...) [<location>] (cause:<cause>) (entity_options:<option>|...) (relative) (relative_axes:<axis>|...) (offthread_repeat:<#>) (offthread_yaw) (offthread_pitch)
    // @Required 1
    // @Maximum 9
    // @Short Teleports the entity(s) to a new location.
    // @Synonyms tp
    // @Group entity
    //
    // @Description
    // Teleports the entity or entities to the new location.
    // Entities can be teleported between worlds using this command.
    // You may optionally specify a teleport cause for player entities, allowing proper teleport event handling. When not specified, this is "PLUGIN". See <@link language teleport cause> for causes.
    //
    // Instead of a valid entity, an unspawned NPC or an offline player may also be used.
    //
    // Optionally specify "relative" to use relative teleportation (Paper only). This is primarily useful only for players, but available for all entities.
    // Relative teleports are smoother for the client when teleporting over short distances.
    // Optionally, you may use "relative_axes:" to specify a set of axes to move relative on (and other axes will be treated as absolute), as any of "X", "Y", "Z", "YAW", "PITCH".
    // Optionally, you may use "offthread_repeat:" with the relative arg when teleporting a player to smooth out the teleport with a specified number of extra async packets sent within a single tick.
    // Optionally, specify "offthread_yaw" or "offthread_pitch" while using offthread_repeat to smooth the player's yaw/pitch to the new location's yaw/pitch.
    //
    // Optionally, specify additional teleport options using the 'entity_options:' arguments (Paper only).
    // This allows things like retaining an open inventory when teleporting - see the links below for more information.
    // See <@link url https://jd.papermc.io/paper/1.19/io/papermc/paper/entity/TeleportFlag.EntityState.html> for all possible options.
    // Note that the API this is based on is marked as experimental in Paper, and so may change in the future.
    //
    // @Tags
    // <EntityTag.location>
    //
    // @Usage
    // Use to teleport a player to the location their cursor is pointing at.
    // - teleport <player> <player.cursor_on>
    //
    // @Usage
    // Use to teleport a player high above.
    // - teleport <player> <player.location.above[200]>
    //
    // @Usage
    // Use to teleport to a random online player.
    // - teleport <player> <server.online_players.random.location>
    //
    // @Usage
    // Use to teleport all players to your location.
    // - teleport <server.online_players> <player.location>
    //
    // @Usage
    // Use to teleport the NPC to a location that was noted with the <@link command note> command.
    // - teleport <npc> my_prenoted_location
    //
    // @Usage
    // Use to teleport a player to some location, and inform events that it was caused by a nether portal.
    // - teleport <player> <server.flag[nether_hub_location]> cause:nether_portal
    //
    // @Usage
    // Use to teleport the player without closing their currently open inventory.
    // - teleport <player> <player.location.below[5]> entity_options:retain_open_inventory
    // -->

    @Override
    public void addCustomTabCompletions(TabCompletionsBuilder tab) {
        tab.addNotesOfType(LocationTag.class);
        tab.addWithPrefix("entity_options:", EntityState.values());
        tab.addWithPrefix("relative_axes:", Relative.values());
    }

    public enum EntityState { RETAIN_PASSENGERS, RETAIN_VEHICLE, RETAIN_OPEN_INVENTORY }
    public enum Relative { X, Y, Z, YAW, PITCH }

    public static void autoExecute(ScriptEntry scriptEntry,
                                   @ArgLinear @ArgName("entities") ObjectTag entityList,
                                   @ArgLinear @ArgName("location") @ArgDefaultNull ObjectTag locationRaw,
                                   @ArgPrefixed @ArgName("cause") @ArgDefaultText("plugin") PlayerTeleportEvent.TeleportCause cause,
                                   @ArgName("entity_options") @ArgPrefixed @ArgDefaultNull @ArgSubType(EntityState.class) List<EntityState> entityOptions,
                                   @ArgName("relative_axes") @ArgPrefixed @ArgDefaultNull @ArgSubType(Relative.class) List<Relative> relativeAxes,
                                   @ArgName("relative") boolean relative,
                                   @ArgName("offthread_repeat") @ArgDefaultNull @ArgPrefixed ElementTag offthreadRepeats,
                                   @ArgName("offthread_yaw") boolean offthreadYaw,
                                   @ArgName("offthread_pitch") boolean offthreadPitch) {
        if (locationRaw == null) { // Compensate for legacy "- teleport <loc>" default fill
            locationRaw = entityList;
            entityList = Utilities.entryDefaultEntity(scriptEntry, true);
        }
        else if (entityList.identify().startsWith("l@")) { // Compensate for legacy entity/location out-of-order support
            ObjectTag swap = locationRaw;
            locationRaw = entityList;
            entityList = swap;
            Deprecations.outOfOrderArgs.warn(scriptEntry);
        }
        if (relative && relativeAxes == null) {
            relativeAxes = Arrays.asList(Relative.values());
        }
        LocationTag location = locationRaw.asType(LocationTag.class, scriptEntry.context);
        ListTag entities = entityList.asType(ListTag.class, scriptEntry.context);
        if (location == null || entities == null) {
            throw new InvalidArgumentsRuntimeException("Location or entity list missing or invalid for Teleport command");
        }
        for (ObjectTag entityObj : entities.objectForms) {
            if (entityObj.shouldBeType(PlayerTag.class)) {
                PlayerTag player = entityObj.asType(PlayerTag.class, scriptEntry.context);
                if (player != null && !player.isOnline()) {
                    player.setLocation(location);
                    continue;
                }
            }
            if (entityObj.shouldBeType(NPCTag.class)) {
                NPCTag npc = entityObj.asType(NPCTag.class, scriptEntry.context);
                if (npc != null && !npc.isSpawned()) {
                    npc.getCitizen().getOrAddTrait(CurrentLocation.class).setLocation(location.clone());
                    continue;
                }
            }
            EntityTag entity = entityObj.asType(EntityTag.class, scriptEntry.context);
            if (entity == null) {
                Debug.echoError("Cannot interpret object '" + entityObj + "' as an EntityTag.");
                continue;
            }
            if (entity.isFake && entity.getWorld().equals(location.getWorld())) {
                NMSHandler.entityHelper.snapPositionTo(entity.getBukkitEntity(), location.toVector());
                NMSHandler.entityHelper.look(entity.getBukkitEntity(), location.getYaw(), location.getPitch());
                return;
            }
            if (offthreadRepeats != null && relativeAxes != null && entity.isPlayer()) {
                NetworkInterceptHelper.enable();
                int times = offthreadRepeats.asInt() + 1;
                int ms = 50 / times;
                Player player = entity.getPlayer();
                Vector increment = location.clone().subtract(player.getLocation()).toVector().multiply(1.0 / times);
                double x = relativeAxes.contains(Relative.X) ? increment.getX() : location.getX();
                double y = relativeAxes.contains(Relative.Y) ? increment.getY() : location.getY();
                double z = relativeAxes.contains(Relative.Z) ? increment.getZ() : location.getZ();
                float yaw;
                if (relativeAxes.contains(Relative.YAW)) {
                    float relYaw = (location.getYaw() - player.getLocation().getYaw()) % 360;
                    if (relYaw > 180) {
                        relYaw -= 360;
                    }
                    yaw = offthreadYaw ? relYaw / times : 0;
                }
                else {
                    yaw = location.getYaw();
                }
                float pitch;
                if (relativeAxes.contains(Relative.PITCH)) {
                    pitch = offthreadPitch ? (location.getPitch() - player.getLocation().getPitch()) / times : 0;
                }
                else {
                    pitch = location.getPitch();
                }
                List<Relative> finalRelativeAxes = relativeAxes;
                NMSHandler.packetHelper.sendRelativePositionPacket(player, x, y, z, yaw, pitch, finalRelativeAxes);
                DenizenCore.runAsync(() -> {
                    try {
                        for (int i = 0; i < times - 1; i++) {
                            Thread.sleep(ms);
                            NMSHandler.packetHelper.sendRelativePositionPacket(player, x, y, z, yaw, pitch, finalRelativeAxes);
                        }
                    }
                    catch (Throwable ex) {
                        Debug.echoError(ex);
                    }
                });
                continue;
            }
            if (entityOptions != null || relativeAxes != null) {
                PaperAPITools.instance.teleport(entity.getBukkitEntity(), location, cause, entityOptions, relativeAxes);
                continue;
            }
            entity.teleport(location, cause);
        }
    }
}
