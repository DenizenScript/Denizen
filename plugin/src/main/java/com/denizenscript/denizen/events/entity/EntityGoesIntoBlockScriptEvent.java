package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityEnterBlockEvent;

public class EntityGoesIntoBlockScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // <entity> goes into <block>
    //
    // @Group Entity
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when an entity enters, and is stored in a block (eg a bee enters a bee nest).
    // Does not fire when a silverfish "enters" a stone block. Prefer <@link event entity changes block> for that.
    //
    // @Context
    // <context.entity> returns the EntityTag.
    // <context.location> returns the LocationTag of the block entered by the entity.
    // <context.material> returns the MaterialTag of the block entered by the entity.
    //
    // -->

    public EntityGoesIntoBlockScriptEvent() {
        registerCouldMatcher("<entity> goes into <block>");
    }

    public EntityTag entity;
    public MaterialTag material;
    public LocationTag location;
    public EntityEnterBlockEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!path.tryArgObject(0, entity)) {
            return false;
        }
        if (!path.tryArgObject(3, material)) {
            return false;
        }
        if (!runInCheck(path, location)) {
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
        switch (name) {
            case "entity": return entity.getDenizenObject();
            case "location": return location;
            case "material": return material;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onEntityEntersBlock(EntityEnterBlockEvent event) {
        entity = new EntityTag(event.getEntity());
        location = new LocationTag(event.getBlock().getLocation());
        material = new MaterialTag(event.getBlock());
        this.event = event;
        fire(event);
    }
}
