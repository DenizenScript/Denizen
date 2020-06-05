package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.PlayerTag;
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
    // @Regex ^on player spectates [^\s]+$
    //
    // @Switch in:<area> to only process the event if it occurred within a specified area.
    //
    // @Plugin Paper
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
        instance = this;
    }

    public static PlayerSpectatesEntityScriptEvent instance;
    public PlayerStartSpectatingEntityEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!path.eventLower.startsWith("player spectates ")) {
            return false;
        }
        if (!couldMatchEntity(path.eventArgLowerAt(2))) {
            return false;
        }
        return true;
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!tryEntity(new EntityTag(event.getNewSpectatorTarget()), path.eventArgLowerAt(2))) {
            return false;
        }
        if (!runInCheck(path, event.getPlayer().getLocation())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public String getName() {
        return "PlayerSpectates";
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(new PlayerTag(event.getPlayer()), null);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("entity")) {
            return new EntityTag(event.getNewSpectatorTarget());
        }
        else if (name.equals("old_entity")) {
            return new EntityTag(event.getCurrentSpectatorTarget());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void playerSpectateEvent(PlayerStartSpectatingEntityEvent event) {
        this.event = event;
        fire(event);
    }
}
