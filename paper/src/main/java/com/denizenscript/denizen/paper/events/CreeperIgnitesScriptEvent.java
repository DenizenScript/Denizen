package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import com.destroystokyo.paper.event.entity.CreeperIgniteEvent;

public class CreeperIgnitesScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // creeper ignites
    //
    // @Plugin Paper
    //
    // @Group Paper
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when a creeper is ignited by flint and steel, or by certain plugin-based activations.
    //
    // @Context
    // <context.entity> returns the EntityTag of the creeper.
    // <context.ignited> returns true if the creeper is ignited, or false if not. NOTE: In most cases, this will return true.
    //
    // -->

    public CreeperIgnitesScriptEvent() {
        registerCouldMatcher("creeper ignites");
    }

    public EntityTag entity;
    public CreeperIgniteEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, entity.getLocation())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "entity":
                return entity;
            case "ignited":
                return new ElementTag(event.isIgnited());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onCreeperIgnites(CreeperIgniteEvent event) {
        entity = new EntityTag(event.getEntity());
        this.event = event;
        fire(event);
    }
}
