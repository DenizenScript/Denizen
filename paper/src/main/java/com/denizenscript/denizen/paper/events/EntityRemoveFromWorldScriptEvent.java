package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class EntityRemoveFromWorldScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // <entity> removed from world
    //
    // @Plugin Paper
    //
    // @Group Paper
    //
    // @Location true
    //
    // @Triggers any time an entity is removed from the world for any reason, including chunks unloading.
    //
    // @Context
    // <context.entity> returns the EntityTag that will be removed. Note that this entity will not necessarily be fully spawned at time of firing, so usage will be limited.
    //
    // -->

    public EntityRemoveFromWorldScriptEvent() {
        registerCouldMatcher("<entity> removed from world");
    }

    public EntityTag entity;
    public EntityRemoveFromWorldEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!path.tryArgObject(0, entity)) {
            return false;
        }
        if (!runInCheck(path, entity.getLocation())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(null, null);
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "entity" -> entity;
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void onPreCreatureSpawn(EntityRemoveFromWorldEvent event) {
        this.entity = new EntityTag(event.getEntity());
        this.event = event;
        fire(event);
    }
}
