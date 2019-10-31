package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
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
    // <context.message> returns an ElementTag of the join message.
    //
    // @Determine
    // ElementTag to change the join message.
    // "NONE" to cancel the join message.
    //
    // @Player Always.
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
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        if (determinationObj instanceof ElementTag) {
            String determination = determinationObj.toString();
            if (CoreUtilities.toLowerCase(determination).equals("none")) {
                message = null;
                return true;
            }
            message = determination;
            return true;
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(new PlayerTag(event.getPlayer()), null);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("message")) {
            return new ElementTag(message);
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerJoins(PlayerJoinEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        message = event.getJoinMessage();
        this.event = event;
        fire(event);
        event.setJoinMessage(message);
    }
}
