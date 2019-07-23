package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
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
    // @Switch in <area>
    //
    // @Cancellable true
    //
    // @Triggers when a player leashes an entity.
    //
    // @Context
    // <context.entity> returns the EntityTag of the leashed entity.
    // <context.holder> returns the EntityTag that is holding the leash.
    //
    // -->

    public PlayerLeashesEntityScriptEvent() {
        instance = this;
    }

    public static PlayerLeashesEntityScriptEvent instance;
    public EntityTag entity;
    public PlayerTag holder;
    public PlayerLeashEntityEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).startsWith("player leashes");
    }

    @Override
    public boolean matches(ScriptPath path) {

        if (!tryEntity(entity, path.eventArgLowerAt(2))) {
            return false;
        }

        if (!runInCheck(path, entity.getLocation())) {
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "PlayerLeashesEntity";
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
