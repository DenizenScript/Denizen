package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.destroystokyo.paper.event.player.PlayerStopSpectatingEntityEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerStopsSpectatingScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player stops spectating (<entity>)
    //
    // @Location true
    //
    // @Plugin Paper
    //
    // @Group Paper
    //
    // @Cancellable true
    //
    // @Triggers when a player stops spectating an entity.
    //
    // @Context
    // <context.entity> returns the entity that was being spectated.
    //
    // @Player Always.
    //
    // -->

    public PlayerStopsSpectatingScriptEvent() {
        registerCouldMatcher("player stops spectating (<entity>)");
    }

    public PlayerStopSpectatingEntityEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (path.eventArgLowerAt(3).length() > 0 && !path.tryArgObject(3, new EntityTag(event.getSpectatorTarget()))) {
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
        if (name.equals("entity")) {
            return new EntityTag(event.getSpectatorTarget()).getDenizenObject();
        }
        return super.getContext(name);
    }

    @EventHandler
    public void playerSpectateEvent(PlayerStopSpectatingEntityEvent event) {
        this.event = event;
        fire(event);
    }
}
