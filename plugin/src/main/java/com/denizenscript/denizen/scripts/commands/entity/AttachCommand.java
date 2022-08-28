package com.denizenscript.denizen.scripts.commands.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.entity.EntityAttachmentHelper;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;

import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

public class AttachCommand extends AbstractCommand {

    public AttachCommand() {
        setName("attach");
        setSyntax("attach [<entity>|...] [to:<entity>/cancel] (offset:<offset>) (relative) (yaw_offset:<#.#>) (pitch_offset:<#.#>) (sync_server) (no_rotate/no_pitch) (for:<player>|...)");
        setRequiredArguments(2, 9);
        isProcedural = false;
    }

    // <--[command]
    // @Name attach
    // @Syntax attach [<entity>|...] [to:<entity>/cancel] (offset:<offset>) (relative) (yaw_offset:<#.#>) (pitch_offset:<#.#>) (sync_server) (no_rotate/no_pitch) (for:<player>|...)
    // @Required 2
    // @Maximum 9
    // @Short Attaches a list of entities to another entity, for client-visible motion sync.
    // @Group entity
    //
    // @Description
    // Attaches a list of entities to another entity, for client-visible motion sync.
    //
    // You must specify the entity or list of entities to be attached.
    // You must specify the entity that they will be attached to, or 'cancel' to end attachment.
    //
    // Optionally, specify an offset location vector to be a positional offset. This can include a yaw/pitch to offset those as well.
    // Note that setting an offset of 0,0,0 will produce slightly different visual results from not setting any offset.
    //
    // Optionally, specify 'relative' to indicate that the offset vector should rotate with the target entity.
    // If relative is used, optionally specify yaw_offset and/or pitch_offset to add an offset to rotation of the target entity when calculating the attachment offset.
    //
    // Optionally, specify 'for' with a player or list of players to only sync motion for those players.
    // If unspecified, will sync for everyone.
    //
    // Optionally, specify 'sync_server' to keep the serverside position of the attached entities near the target entity.
    // This can reduce some visual artifacts (such as entity unloading at distance), but may produce unintended functional artifacts.
    // Note that you should generally only use 'sync_server' when you exclude the 'for' argument.
    //
    // Optionally specify 'no_rotate' to retain the attached entity's own rotation and ignore the target rotation.
    // Optionally instead specify 'no_pitch' to retain the attached entity's own pitch, but use the target yaw.
    //
    // Note that attaches involving a player will not be properly visible to that player, but will still be visible to *other* players.
    //
    // It may be ideal to change setting "Packets.Auto init" in the Denizen config to "true" to guarantee this command functions as expected.
    //
    // @Tags
    // <EntityTag.attached_entities[(<player>)]>
    // <EntityTag.attached_to[(<player>)]>
    // <EntityTag.attached_offset[(<player>)]>
    //
    // @Usage
    // Use to attach random NPC to the air 3 blocks above a linked NPC.
    // - attach <server.list_npcs.random> to:<npc> offset:0,3,0
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("to")
                    && !scriptEntry.hasObject("cancel")
                    && arg.matchesPrefix("to")
                    && arg.matchesArgumentType(EntityTag.class)) {
                scriptEntry.addObject("to", arg.asType(EntityTag.class));
            }
            else if (!scriptEntry.hasObject("cancel")
                    && !scriptEntry.hasObject("to")
                    && arg.matches("cancel")) {
                scriptEntry.addObject("cancel", new ElementTag(true));
            }
            else if (!scriptEntry.hasObject("relative")
                    && arg.matches("relative")) {
                scriptEntry.addObject("relative", new ElementTag(true));
            }
            else if (!scriptEntry.hasObject("sync_server")
                    && arg.matches("sync_server")) {
                scriptEntry.addObject("sync_server", new ElementTag(true));
            }
            else if (!scriptEntry.hasObject("no_rotate")
                    && arg.matches("no_rotate")) {
                scriptEntry.addObject("no_rotate", new ElementTag(true));
            }
            else if (!scriptEntry.hasObject("no_pitch")
                    && arg.matches("no_pitch")) {
                scriptEntry.addObject("no_pitch", new ElementTag(true));
            }
            else if (!scriptEntry.hasObject("yaw_offset")
                    && arg.matchesPrefix("yaw_offset")
                    && arg.matchesFloat()) {
                scriptEntry.addObject("yaw_offset", arg.asElement());
            }
            else if (!scriptEntry.hasObject("pitch_offset")
                    && arg.matchesPrefix("pitch_offset")
                    && arg.matchesFloat()) {
                scriptEntry.addObject("pitch_offset", arg.asElement());
            }
            else if (!scriptEntry.hasObject("offset")
                    && arg.matchesPrefix("offset")
                    && arg.matchesArgumentType(LocationTag.class)) {
                scriptEntry.addObject("offset", arg.asType(LocationTag.class));
            }
            else if (!scriptEntry.hasObject("for")
                    && arg.matchesPrefix("for")
                    && arg.matchesArgumentList(PlayerTag.class)) {
                scriptEntry.addObject("for", arg.asType(ListTag.class).filter(PlayerTag.class, scriptEntry));
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
            throw new InvalidArgumentsException("Must specify attaching entities!");
        }
        if (!scriptEntry.hasObject("to") && !scriptEntry.hasObject("cancel")) {
            throw new InvalidArgumentsException("Must specify a target entity, or 'cancel'!");
        }
        scriptEntry.defaultObject("cancel", new ElementTag(false));
        scriptEntry.defaultObject("relative", new ElementTag(false));
        scriptEntry.defaultObject("sync_server", new ElementTag(false));
        scriptEntry.defaultObject("no_rotate", new ElementTag(false));
        scriptEntry.defaultObject("no_pitch", new ElementTag(false));
        scriptEntry.defaultObject("yaw_offset", new ElementTag(0f));
        scriptEntry.defaultObject("pitch_offset", new ElementTag(0f));
    }

    @Override
    public void execute(final ScriptEntry scriptEntry) {
        LocationTag offset = scriptEntry.getObjectTag("offset");
        List<EntityTag> entities = (List<EntityTag>) scriptEntry.getObject("entities");
        EntityTag target = scriptEntry.getObjectTag("to");
        List<PlayerTag> forPlayers = (List<PlayerTag>) scriptEntry.getObject("for");
        ElementTag cancel = scriptEntry.getElement("cancel");
        ElementTag relative = scriptEntry.getElement("relative");
        ElementTag sync_server = scriptEntry.getElement("sync_server");
        ElementTag no_rotate = scriptEntry.getElement("no_rotate");
        ElementTag no_pitch = scriptEntry.getElement("no_pitch");
        ElementTag yaw_offset = scriptEntry.getElement("yaw_offset");
        ElementTag pitch_offset = scriptEntry.getElement("pitch_offset");
        boolean shouldCancel = cancel.asBoolean();
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), db("entities", entities), shouldCancel ? cancel : target, relative, offset,
                    yaw_offset, pitch_offset, sync_server, no_rotate, no_pitch, db("for", forPlayers));
        }
        BiConsumer<EntityTag, UUID> procPlayer = (entity, player) -> {
            if (shouldCancel) {
                EntityAttachmentHelper.removeAttachment(entity.getUUID(), player);
            }
            else {
                EntityAttachmentHelper.AttachmentData attachment = new EntityAttachmentHelper.AttachmentData();
                attachment.attached = entity;
                attachment.to = target;
                attachment.positionalOffset = offset == null ? null : offset.clone();
                attachment.offsetRelative = relative.asBoolean();
                attachment.yawAngleOffset = yaw_offset.asFloat();
                attachment.pitchAngleOffset = pitch_offset.asFloat();
                attachment.syncServer = sync_server.asBoolean();
                attachment.forPlayer = player;
                attachment.noRotate = no_rotate.asBoolean();
                attachment.noPitch = no_pitch.asBoolean();
                EntityAttachmentHelper.registerAttachment(attachment);
            }
        };
        for (EntityTag entity : entities) {
            if (!entity.isSpawned() && !entity.isFake && !shouldCancel) {
                Debug.echoError("Cannot attach entity '" + entity + "': entity is not spawned.");
                continue;
            }
            if (forPlayers == null || (forPlayers.isEmpty() && sync_server.asBoolean())) {
                procPlayer.accept(entity, null);
            }
            else {
                for (PlayerTag player : forPlayers) {
                    procPlayer.accept(entity, player.getUUID());
                }
            }
        }
    }
}
