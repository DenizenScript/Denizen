package com.denizenscript.denizen.scripts.commands.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Collections;
import java.util.List;

public class TeleportCommand extends AbstractCommand {

    public TeleportCommand() {
        setName("teleport");
        setSyntax("teleport (<entity>|...) [<location>] (cause:<cause>)");
        setRequiredArguments(1, 3);
        isProcedural = false;
    }

    // <--[command]
    // @Name Teleport
    // @Syntax teleport (<entity>|...) [<location>] (cause:<cause>)
    // @Required 1
    // @Maximum 3
    // @Short Teleports the entity(s) to a new location.
    // @Synonyms tp
    // @Group entity
    //
    // @Description
    // Teleports the entity or entities to the new location.
    // Entities can be teleported between worlds using this command.
    // You may optionally specify a teleport cause for player entities, allowing proper teleport event handling. When not specified, this is "PLUGIN". See <@link language teleport cause> for causes.
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

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentType(LocationTag.class)) {
                scriptEntry.addObject("location", arg.asType(LocationTag.class));
            }
            else if (!scriptEntry.hasObject("entities")
                    && arg.matchesArgumentList(EntityTag.class)) {
                scriptEntry.addObject("entities", arg.asType(ListTag.class).filter(EntityTag.class, scriptEntry));
            }
            // NPC arg for compatibility with old scripts
            else if (arg.matches("npc") && Utilities.entryHasNPC(scriptEntry)) {
                scriptEntry.addObject("entities", Collections.singletonList(Utilities.getEntryNPC(scriptEntry).getDenizenEntity()));
            }
            else if (!scriptEntry.hasObject("cause")
                    && arg.matchesEnum(PlayerTeleportEvent.TeleportCause.class)) {
                scriptEntry.addObject("cause", arg.asElement());
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("location")) {
            throw new InvalidArgumentsException("Must specify a location!");
        }
        if (!scriptEntry.hasObject("entities")) {
            scriptEntry.defaultObject("entities", Utilities.entryDefaultEntityList(scriptEntry, true));
        }
    }

    @Override
    public void execute(final ScriptEntry scriptEntry) {
        LocationTag location = scriptEntry.getObjectTag("location");
        List<EntityTag> entities = (List<EntityTag>) scriptEntry.getObject("entities");
        ElementTag cause = scriptEntry.getElement("cause");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), location, db("entities", entities), cause);
        }
        PlayerTeleportEvent.TeleportCause causeEnum = cause == null ? PlayerTeleportEvent.TeleportCause.PLUGIN : PlayerTeleportEvent.TeleportCause.valueOf(cause.asString().toUpperCase());
        for (EntityTag entity : entities) {
            if (entity.isFake && entity.getWorld().equals(location.getWorld())) {
                NMSHandler.getEntityHelper().snapPositionTo(entity.getBukkitEntity(), location.toVector());
                NMSHandler.getEntityHelper().look(entity.getBukkitEntity(), location.getYaw(), location.getPitch());
                return;
            }
            entity.teleport(location, causeEnum);
        }
    }
}
