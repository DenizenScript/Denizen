package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleSwimEvent;

public class EntitySwimScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // <entity> toggles|starts|stops swimming
    //
    // @Group Entity
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when an entity starts or stops swimming.
    //
    // @Context
    // <context.entity> returns the EntityTag of this event.
    // <context.state> returns an ElementTag(Boolean) with a value of "true" if the entity is now swimming and "false" otherwise.
    //
    // @Player when the entity is a player.
    //
    // @NPC when the entity is an NPC.
    //
    // -->

    public EntitySwimScriptEvent() {
        instance = this;
        registerCouldMatcher("<entity> toggles|starts|stops swimming");
    }

    public static EntitySwimScriptEvent instance;
    public EntityTag entity;
    public boolean state;

    @Override
    public boolean matches(ScriptPath path) {
        if (!entity.tryAdvancedMatcher(path.eventArgLowerAt(0))) {
            return false;
        }
        String cmd = path.eventArgLowerAt(1);
        if (cmd.equals("starts") && !state) {
            return false;
        }
        if (cmd.equals("stops") && state) {
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
        else if (name.equals("state")) {
            return new ElementTag(state);
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onEntityToggleSwim(EntityToggleSwimEvent event) {
        entity = new EntityTag(event.getEntity());
        state = event.isSwimming();
        fire(event);
    }
}
