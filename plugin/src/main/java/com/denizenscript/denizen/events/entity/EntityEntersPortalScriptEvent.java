package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEnterEvent;

public class EntityEntersPortalScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // <entity> enters portal
    //
    // @Group Entity
    //
    // @Location true
    //
    // @Triggers when an entity enters a portal. That is, when the entity touches a portal block.
    //
    // @Context
    // <context.entity> returns the EntityTag.
    // <context.location> returns the LocationTag of the portal block touched by the entity.
    //
    // @Player when the entity that entered the portal is a player
    //
    // @NPC when the entity that entered the portal is an NPC.
    //
    // -->

    public EntityEntersPortalScriptEvent() {
        registerCouldMatcher("<entity> enters portal");
    }

    public EntityTag entity;
    public LocationTag location;
    public EntityPortalEnterEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        String target = path.eventArgLowerAt(0);
        if (!entity.tryAdvancedMatcher(target)) {
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
        else if (name.equals("location")) {
            return location;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onEntityEntersPortal(EntityPortalEnterEvent event) {
        entity = new EntityTag(event.getEntity());
        location = new LocationTag(event.getLocation());
        this.event = event;
        fire(event);
    }
}
