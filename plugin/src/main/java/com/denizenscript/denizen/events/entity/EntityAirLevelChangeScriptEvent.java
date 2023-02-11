package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityAirChangeEvent;

public class EntityAirLevelChangeScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // <entity> changes air level
    //
    // @Synonyms player loses oxygen,player drowns,player is drowning,oxygen depletion
    //
    // @Group Entity
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when an entity's air level changes.
    //
    // @Context
    // <context.entity> returns the EntityTag.
    // <context.air_duration> returns a DurationTag of the entity's new air level.
    //
    // @Determine
    // DurationTag to set the entity's new air level.
    //
    // @Player when the entity that's air level has changed is a player.
    //
    // @NPC when the entity that's air level has changed is an NPC.
    //
    // -->

    public EntityAirLevelChangeScriptEvent() {
        registerCouldMatcher("<entity> changes air level");
    }

    public EntityTag entity;
    public EntityAirChangeEvent event;

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
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        if (determinationObj instanceof ElementTag element && element.isInt()) {
            event.setAmount(element.asInt());
            return true;
        }
        else if (DurationTag.matches(determinationObj.toString())) {
            event.setAmount(DurationTag.valueOf(determinationObj.toString(), getTagContext(path)).getTicksAsInt());
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
        switch (name) {
            case "entity":
                return entity.getDenizenObject();
            case "air":
                BukkitImplDeprecations.airLevelEventDuration.warn();
                return new ElementTag(event.getAmount());
            case "air_duration":
                return new DurationTag((long) event.getAmount());
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
