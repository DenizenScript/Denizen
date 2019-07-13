package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizen.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.dPlayer;
import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.dObject;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinsScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player joins
    // player join
    //
    // @Regex ^on player (joins|join)$
    //
    // @Triggers when a player joins the server.
    //
    // @Context
    // <context.message> returns an Element of the join message.
    //
    // @Determine
    // Element to change the join message.
    // "NONE" to cancel the join message.
    //
    // -->

    public PlayerJoinsScriptEvent() {
        instance = this;
    }

    public static PlayerJoinsScriptEvent instance;
    public String message;
    public PlayerJoinEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).startsWith("player join");
    }

    @Override
    public boolean matches(ScriptPath path) {
        return true;
    }

    @Override
    public String getName() {
        return "PlayerJoins";
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        if (!CoreUtilities.toLowerCase(determination).equals("none")) {
            message = determination;
            return true;
        }
        else {
            message = null;
            return true;
        }
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(new dPlayer(event.getPlayer()), null);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("message")) {
            return new Element(message);
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerJoins(PlayerJoinEvent event) {
        if (dEntity.isNPC(event.getPlayer())) {
            return;
        }
        message = event.getJoinMessage();
        this.event = event;
        fire(event);
        event.setJoinMessage(message);
    }
}
