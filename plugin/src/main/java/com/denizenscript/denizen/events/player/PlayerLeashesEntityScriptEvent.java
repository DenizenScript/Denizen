package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerLeashEntityEvent;

public class PlayerLeashesEntityScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player leashes entity
    // player leashes <entity>
    //
    // @Regex ^on player leashes [^\s]+$
    //
    // @Group Player
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when a player leashes an entity.
    //
    // @Context
    // <context.entity> returns the EntityTag of the leashed entity.
    // <context.holder> returns the EntityTag that is holding the leash.
    //
    // @Player Always.
    //
    // -->

    public PlayerLeashesEntityScriptEvent() {
    }

    public EntityTag entity;
    public PlayerTag holder;
    public PlayerLeashEntityEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!path.eventLower.startsWith("player leashes")) {
            return false;
        }
        if (!couldMatchEntity(path.eventArgLowerAt(2))) {
            return false;
        }
        return true;
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!path.tryArgObject(2, entity)) {
            return false;
        }
        if (!runInCheck(path, entity.getLocation())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(holder, null);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("holder")) {
            return holder;
        }
        else if (name.equals("entity")) {
            return entity;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerLeashes(PlayerLeashEntityEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        holder = PlayerTag.mirrorBukkitPlayer(event.getPlayer());
        entity = new EntityTag(event.getEntity());
        this.event = event;
        fire(event);
    }
}
