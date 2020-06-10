package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityAirChangeEvent;

public class EntityAirLevelChangeScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // entity changes air level
    // <entity> changes air level
    //
    // @Regex ^on [^\s]+ changes air level$
    //
    // @Group Entity
    //
    // @Switch in:<area> to only process the event if it occurred within a specified area.
    //
    // @Cancellable true
    //
    // @Triggers when an entity's air level changes.
    //
    // @Context
    // <context.entity> returns the EntityTag.
    // <context.air> returns an ElementTag(Number) of the entity's new air level (measured in ticks).
    //
    // @Determine
    // ElementTag(Decimal) to set the entity's new air level.
    //
    // @Player when the entity that's air level has changed is a player.
    //
    // @NPC when the entity that's air level has changed is an NPC.
    //
    // -->

    public EntityAirLevelChangeScriptEvent() {
        instance = this;
    }

    public static EntityAirLevelChangeScriptEvent instance;
    public EntityTag entity;
    public EntityAirChangeEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!path.eventLower.contains("changes air level")) {
            return false;
        }
        if (!couldMatchEntity(path.eventArgLowerAt(0))) {
            return false;
        }
        return true;
    }

    @Override
    public boolean matches(ScriptPath path) {
        String target = path.eventArgLowerAt(0);

        if (!tryEntity(entity, target)) {
            return false;
        }

        if (!runInCheck(path, entity.getLocation())) {
            return false;
        }

        return super.matches(path);
    }

    @Override
    public String getName() {
        return "AirLevelChanged";
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        if (determinationObj instanceof ElementTag && ((ElementTag) determinationObj).isInt()) {
            event.setAmount(((ElementTag) determinationObj).asInt());
            return true;
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(entity);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("entity")) {
            return entity.getDenizenObject();
        }
        else if (name.equals("air")) {
            return new ElementTag(event.getAmount());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onEntityAirLevelChanged(EntityAirChangeEvent event) {
        entity = new EntityTag(event.getEntity());
        this.event = event;
        fire(event);
    }
}
