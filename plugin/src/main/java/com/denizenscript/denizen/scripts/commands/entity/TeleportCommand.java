package com.denizenscript.denizen.scripts.commands.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.PaperAPITools;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.commands.generator.*;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsRuntimeException;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import net.citizensnpcs.trait.CurrentLocation;
import org.bukkit.event.player.PlayerTeleportEvent;

public class TeleportCommand extends AbstractCommand {

    public TeleportCommand() {
        setName("teleport");
        setSyntax("teleport (<entity>|...) [<location>] (cause:<cause>) (relative)");
        setRequiredArguments(1, 4);
        isProcedural = false;
        autoCompile();
    }

    // <--[command]
    // @Name Teleport
    // @Syntax teleport (<entity>|...) [<location>] (cause:<cause>) (relative)
    // @Required 1
    // @Maximum 4
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
    // Optionally specify "relative" when teleporting a player to use relative teleporation (Paper only).
    // Relative teleports are smoother for the client when teleporting over short distances.
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
    // - teleport <player> <player.location.add[0,200,0]>
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
    // Use to teleport the NPC to a location that was noted wih the <@link command note> command.
    // - teleport <npc> my_prenoted_location
    //
    // @Usage
    // Use to teleport a player to some location, and inform events that it was caused by a nether portal.
    // - teleport <player> <server.flag[nether_hub_location]> cause:nether_portal
    // -->

    @Override
    public void addCustomTabCompletions(TabCompletionsBuilder tab) {
        tab.addNotesOfType(LocationTag.class);
    }

    public static void autoExecute(ScriptEntry scriptEntry,
                                   @ArgLinear @ArgName("entities") ObjectTag entityList,
                                   @ArgLinear @ArgName("location") @ArgDefaultNull ObjectTag locationRaw,
                                   @ArgPrefixed @ArgName("cause") @ArgDefaultText("PLUGIN") PlayerTeleportEvent.TeleportCause cause,
                                   @ArgName("relative") boolean relative) {
        if (locationRaw == null) { // Compensate for legacy "- teleport <loc>" default fill
            locationRaw = entityList;
            entityList = Utilities.entryDefaultEntity(scriptEntry, true);
        }
        else if (entityList.identify().startsWith("l@")) { // Compensate for legacy entity/location out-of-order support
            ObjectTag swap = locationRaw;
            locationRaw = entityList;
            entityList = swap;
        }
        LocationTag location = locationRaw.asType(LocationTag.class, scriptEntry.context);
        ListTag entities = entityList.asType(ListTag.class, scriptEntry.context);
        if (location == null || entities == null) {
            throw new InvalidArgumentsRuntimeException("Location or entity list missing or invalid for Teleport command");
        }
        for (ObjectTag entityObj : entities.objectForms) {
            if (entityObj.shouldBeType(PlayerTag.class)) {
                PlayerTag player = entityObj.asType(PlayerTag.class, scriptEntry.context);
                if (player != null) {
                    if (!player.isOnline()) {
                        player.setLocation(location);
                        continue;
                    }
                    if (relative) {
                        PaperAPITools.instance.teleportPlayerRelative(player.getPlayerEntity(), location);
                        continue;
                    }
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
            entity.teleport(location, cause);
        }
    }
}
