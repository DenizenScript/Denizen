package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class PlayerLoginScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player logs in (for the first time)
    // player (first) login
    //
    // @Regex ^on player( logs in( for the first time)?|( first)? login)$
    //
    // @Group Player
    //
    // @Triggers when a player logs in to the server. This is during the authentication process, and should NOT be confused with <@link event player joins>.
    //
    // @Warning Generally avoid this event. This is not a way to get a 'first join' event. This is an internal technical event, with specific uses (eg custom whitelisting).
    //
    // @Context
    // <context.hostname> returns an ElementTag of the player's IP address.
    // <context.server_hostname> returns an ElementTag of the server address that the player used to connect to the server.
    //
    // @Determine
    // "KICKED" to kick the player from the server.
    // "KICKED:<ElementTag>" to kick the player and specify a message to show.
    //
    // @Player Always.
    //
    // -->

    public PlayerLoginScriptEvent() {
    }

    public PlayerLoginEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        return path.eventLower.startsWith("player login") || path.eventLower.startsWith("player first login")
                || path.eventLower.startsWith("player logs in");
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (path.eventLower.contains("first") && PlayerTag.isNoted(event.getPlayer())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        if (determinationObj instanceof ElementTag) {
            String determination = determinationObj.toString();
            if (CoreUtilities.toLowerCase(determination).startsWith("kicked")) {
                String message = determination.length() > "KICKED:".length() ? determination.substring("KICKED:".length()) : determination;
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER, message);
                return true;
            }
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(event.getPlayer());
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "hostname" -> new ElementTag(event.getAddress().toString());
            case "server_hostname" -> new ElementTag(event.getHostname());
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        this.event = event;
        fire(event);
    }
}
