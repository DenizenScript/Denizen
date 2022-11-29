package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityUnleashEvent;

public class EntityUnleashedScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // <entity> unleashed (because <'reason'>)
    //
    // @Group Entity
    //
    // @Location true
    //
    // @Triggers when an entity is unleashed.
    //
    // @Context
    // <context.entity> returns the EntityTag.
    // <context.reason> returns an ElementTag of the reason for the unleashing.
    // Reasons include DISTANCE, HOLDER_GONE, PLAYER_UNLEASH, and UNKNOWN
    //
    // @NPC when the entity being unleashed is an NPC.
    //
    // -->

    public EntityUnleashedScriptEvent() {
        registerCouldMatcher("<entity> unleashed (because <'reason'>)");
    }

    public EntityTag entity;
    public ElementTag reason;
    public EntityUnleashEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!path.tryArgObject(0, entity)) {
            return false;
        }
        if (path.eventArgAt(2).equals("because") && !path.eventArgLowerAt(3).equals(reason.asLowerString())) {
            return false;
        }
        if (!runInCheck(path, entity.getLocation())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("entity")) {
            return entity;
        }
        else if (name.equals("reason")) {
            return reason;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onEntityUnleashed(EntityUnleashEvent event) {
        entity = new EntityTag(event.getEntity());
        reason = new ElementTag(event.getReason().toString());
        this.event = event;
        fire(event);
    }
}
