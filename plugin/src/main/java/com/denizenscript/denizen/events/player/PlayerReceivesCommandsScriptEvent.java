package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandSendEvent;

public class PlayerReceivesCommandsScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player receives commands
    //
    // @Regex ^on player receives commands$
    //
    // @Group Player
    //
    // @Triggers when the list of available server commands is sent to the player for tab completion.
    //
    // @Context
    // <context.commands> returns a ListTag of received commands.
    //
    // @Determine
    // ListTag to set the player's available commands. NOTE: It is not possible to add entries to the command list, only remove them.
    //
    // @Player Always.
    //
    // -->

    public PlayerReceivesCommandsScriptEvent() {
    }

    public PlayerCommandSendEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        return path.eventLower.startsWith("player receives commands");
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        String determination = determinationObj.toString();
        if (determination.length() > 0) {
            event.getCommands().clear();
            event.getCommands().addAll(ListTag.getListFor(determinationObj, getTagContext(path)));
            return true;
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(event.getPlayer());
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("commands")) {
            ListTag list = new ListTag();
            list.addAll(event.getCommands());
            return list;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerCommandSend(PlayerCommandSendEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        this.event = event;
        fire(event);
    }
}
