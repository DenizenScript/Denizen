package net.aufdemrand.denizen.events.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandSendEvent;

import java.util.Collection;

public class PlayerReceivesCommandsScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player receives commands
    //
    // @Regex ^on player receives commands$
    //
    // @Triggers when the list of available server commands is sent to the player for tab completion.
    //
    // @Context
    // <context.commands> returns a dList of received commands.
    //
    // @Determine
    // dList to set the player's available commands. NOTE: It is not possible to add entries to the command list, only remove them.
    //
    // -->

    public PlayerReceivesCommandsScriptEvent() {
        instance = this;
    }

    public static PlayerReceivesCommandsScriptEvent instance;
    private Collection<String> commands;
    public PlayerCommandSendEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).startsWith("player receives commands");
    }

    @Override
    public boolean matches(ScriptPath path) {
        return true;
    }

    @Override
    public String getName() {
        return "PlayerReceivesCommands";
    }

    @Override
    public void destroy() {
        PlayerCommandSendEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        String lower = CoreUtilities.toLowerCase(determination);
        if (determination.length() > 0 && !isDefaultDetermination(determination)) {
            commands.clear();
            commands.addAll(dList.valueOf(determination));
            return true;
        }
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(new dPlayer(event.getPlayer()), null);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("commands")) {
            dList list = new dList();
            list.addAll(commands);
            return list;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerCommandSend(PlayerCommandSendEvent event) {
        if (dEntity.isNPC(event.getPlayer())) {
            return;
        }
        commands = event.getCommands();
        this.event = event;
        fire();
    }
}
