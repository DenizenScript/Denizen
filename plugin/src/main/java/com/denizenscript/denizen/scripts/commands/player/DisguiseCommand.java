package com.denizenscript.denizen.scripts.commands.player;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;

import java.util.Arrays;
import java.util.List;

public class DisguiseCommand extends AbstractCommand {

    public DisguiseCommand() {
        setName("disguise");
        setSyntax("disguise [<entity>] [cancel/as:<type>] (players:<player>|...)");
        setRequiredArguments(2, 3);
        isProcedural = false;
    }

    // <--[command]
    // @Name disguise
    // @Syntax disguise [<entity>] [cancel/as:<type>] (players:<player>|...)
    // @Required 2
    // @Maximum 3
    // @Short Makes the player see an entity as though it were a different type of entity.
    // @Group player
    //
    // @Description
    // Makes the player see an entity as though it were a different type of entity.
    //
    // The entity won't actually change on the server.
    // The entity will still visibly behave the same as the real entity type does.
    //
    // Be warned that the replacement is imperfect, and visual or internal-client errors may arise from using this command.
    // This command should not be used to disguise players in their own view.
    //
    // The disguise is purely temporary, and lasts only as long as the player is able to render the entity.
    //
    // Optionally, specify a list of players to show the entity to.
    // If unspecified, will default to the linked player.
    //
    // To remove a disguise, use the 'cancel' argument.
    //
    // @Tags
    // None.
    //
    // @Usage
    // Use to show a turn the NPC into a creeper for the linked player.
    // - disguise <npc> as:creeper
    //
    // @Usage
    // Use to show a turn the NPC into a red sheep for the linked player.
    // - disguise <npc> as:sheep[color=red]
    //
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry.getProcessedArgs()) {
            if (!scriptEntry.hasObject("players")
                    && arg.matchesPrefix("to", "players")) {
                scriptEntry.addObject("players", arg.asType(ListTag.class).filter(PlayerTag.class, scriptEntry));
            }
            else if (arg.matchesPrefix("as")
                    && arg.matchesArgumentType(EntityTag.class)) {
                scriptEntry.addObject("as", arg.asType(EntityTag.class));
            }
            else if (!scriptEntry.hasObject("entity")
                    && arg.matchesArgumentType(EntityTag.class)) {
                scriptEntry.addObject("entity", arg.asType(EntityTag.class));
            }
            else if (!scriptEntry.hasObject("cancel")
                    && arg.matches("cancel")) {
                scriptEntry.addObject("cancel", new ElementTag(true));
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("players") && Utilities.entryHasPlayer(scriptEntry)) {
            scriptEntry.defaultObject("players", Arrays.asList(Utilities.getEntryPlayer(scriptEntry)));
        }
        if (!scriptEntry.hasObject("as") && !scriptEntry.hasObject("cancel")) {
            throw new InvalidArgumentsException("Must specify a valid type to disguise as!");
        }
        if (!scriptEntry.hasObject("players")) {
            throw new InvalidArgumentsException("Must have a valid, online player attached!");
        }
        if (!scriptEntry.hasObject("entity")) {
            throw new InvalidArgumentsException("Must specify a valid entity!");
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        EntityTag entity = scriptEntry.getObjectTag("entity");
        EntityTag as = scriptEntry.getObjectTag("as");
        ElementTag cancel = scriptEntry.getElement("cancel");
        List<PlayerTag> players = (List<PlayerTag>) scriptEntry.getObject("players");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), entity.debug()
                    + (cancel != null ? cancel.debug() : as.debug())
                    + ArgumentHelper.debugList("players", players));
        }
        if (cancel != null && cancel.asBoolean()) {
            for (PlayerTag player : players) {
                NMSHandler.getPlayerHelper().deTrackEntity(player.getPlayerEntity(), entity.getBukkitEntity());
            }
        }
        else {
            for (PlayerTag player : players) {
                NMSHandler.getPlayerHelper().sendEntityDestroy(player.getPlayerEntity(), entity.getBukkitEntity());
            }
            NMSHandler.getPlayerHelper().sendEntitySpawn(players, as.getBukkitEntityType(), entity.getLocation(), as.getWaitingMechanisms(), entity.getBukkitEntity().getEntityId(), entity.getUUID(), false);
        }
    }
}
