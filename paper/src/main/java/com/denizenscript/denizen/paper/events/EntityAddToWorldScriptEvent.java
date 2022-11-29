package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class EntityAddToWorldScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // <entity> added to world
    //
    // @Plugin Paper
    //
    // @Group Paper
    //
    // @Location true
    //
    // @Triggers any time an entity is added to the world for any reason, including chunks loading pre-existing entities.
    //
    // @Context
    // <context.entity> returns the EntityTag that will be added. Note that this entity will not necessarily be fully spawned yet, so usage will be limited.
    // <context.location> returns the LocationTag that the entity will be added at.
    //
    // -->

    public EntityAddToWorldScriptEvent() {
        registerCouldMatcher("<entity> added to world");
    }

    public EntityTag entity;
    public LocationTag location;
    public EntityAddToWorldEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!path.tryArgObject(0, entity)) {
            return false;
        }
        if (!runInCheck(path, location)) {
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
        switch (name) {
            case "entity": return entity;
            case "location": return location;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPreCreatureSpawn(EntityAddToWorldEvent event) {
        this.entity = new EntityTag(event.getEntity());
        this.location = new LocationTag(event.getEntity().getLocation());
        this.event = event;
        fire(event);
    }
}
