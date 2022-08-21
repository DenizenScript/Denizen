package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;

public class EntityResurrectScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // <entity> resurrected
    //
    // @Group Entity
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when an entity dies and is resurrected by a totem.
    //
    // @Context
    // <context.entity> returns the EntityTag being resurrected.
    //
    // @Player when the entity being resurrected is a player.
    //
    // -->

    public EntityResurrectScriptEvent() {
        instance = this;
        registerCouldMatcher("<entity> resurrected");
    }

    public static EntityResurrectScriptEvent instance;
    public EntityTag entity;
    public EntityResurrectEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!entity.tryAdvancedMatcher(path.eventArgLowerAt(0))) {
            return false;
        }
        if (!runInCheck(path, entity.getLocation())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(entity);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("entity")) {
            return entity;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onEntityResurrect(EntityResurrectEvent event) {
        EntityTag.rememberEntity(event.getEntity());
        entity = new EntityTag(event.getEntity());
        this.event = event;
        fire(event);
        EntityTag.forgetEntity(event.getEntity());
    }
}
