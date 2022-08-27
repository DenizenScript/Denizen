package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleFlightEvent;

public class PlayerFlyingScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player toggles flying
    // player starts flying
    // player stops flying
    //
    // @Regex ^on player (toggles|starts|stops) flying$
    //
    // @Group Player
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when a player starts or stops flying.
    //
    // @Context
    // <context.state> returns an ElementTag(Boolean) with a value of "true" if the player is now flying and "false" otherwise.
    //
    // @Player Always.
    //
    // -->

    public PlayerFlyingScriptEvent() {
    }

    public boolean state;
    public PlayerToggleFlightEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!path.eventArgLowerAt(2).equals("flying") && !path.eventArgLowerAt(2).equals("flight")) {
            return false;
        }
        if (!path.eventArgLowerAt(0).equals("player")) {
            return false;
        }
        if (!path.eventArgLowerAt(1).equals("starts") && !path.eventArgLowerAt(1).equals("stops") && !path.eventArgLowerAt(1).equals("toggles")) {
            return false;
        }
        return true;
    }

    @Override
    public boolean matches(ScriptPath path) {
        String cmd = path.eventArgLowerAt(1);
        if (cmd.equals("starts") && !state) {
            return false;
        }
        if (cmd.equals("stops") && state) {
            return false;
        }
        if (!runInCheck(path, event.getPlayer().getLocation())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(event.getPlayer());
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("state")) {
            return new ElementTag(state);
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerFlying(PlayerToggleFlightEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        state = event.isFlying();
        this.event = event;
        fire(event);
    }
}
