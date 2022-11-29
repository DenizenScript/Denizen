package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.destroystokyo.paper.event.player.PlayerStartSpectatingEntityEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerSpectatesEntityScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player spectates <entity>
    //
    // @Location true
    //
    // @Plugin Paper
    //
    // @Group Paper
    //
    // @Cancellable true
    //
    // @Triggers when a player starts spectating an entity.
    //
    // @Context
    // <context.entity> returns the entity that is being spectated.
    // <context.old_entity> returns the entity that was previously being spectated (or the player themself if they weren't spectating anything).
    //
    // @Player Always.
    //
    // -->

    public PlayerSpectatesEntityScriptEvent() {
        registerCouldMatcher("player spectates <entity>");
    }

    public PlayerStartSpectatingEntityEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!path.tryArgObject(2, new EntityTag(event.getNewSpectatorTarget()))) {
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
            return new EntityTag(event.getNewSpectatorTarget()).getDenizenObject();
        }
        else if (name.equals("old_entity")) {
            return new EntityTag(event.getCurrentSpectatorTarget()).getDenizenObject();
        }
        return super.getContext(name);
    }

    @EventHandler
    public void playerSpectateEvent(PlayerStartSpectatingEntityEvent event) {
        this.event = event;
        fire(event);
    }
}
