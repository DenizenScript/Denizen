package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class EntityFoodLevelChangeScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // <entity> changes food level
    //
    // @Synonyms player hunger depletes
    //
    // @Group Entity
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Switch item:<item> to only process the event if it was triggered by an item that matches the specified item.
    //
    // @Triggers when an entity's food level changes.
    //
    // @Context
    // <context.entity> returns the EntityTag.
    // <context.food> returns an ElementTag(Number) of the entity's new food level.
    // <context.item> returns an ItemTag of the item that triggered the event, if any.
    //
    // @Determine
    // ElementTag(Number) to set the entity's new food level.
    //
    // @Player when the entity that's food level has changed is a player.
    //
    // @NPC when the entity that's food level has changed is an NPC.
    //
    // -->

    public EntityFoodLevelChangeScriptEvent() {
        registerCouldMatcher("<entity> changes food level");
        registerSwitches("item");
    }

    public EntityTag entity;
    public ItemTag item;
    public FoodLevelChangeEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        String target = path.eventArgLowerAt(0);
        if (!entity.tryAdvancedMatcher(target)) {
            return false;
        }
        if (!path.tryObjectSwitch("item", item)) {
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
            event.setFoodLevel(element.asInt());
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
            case "entity": return entity.getDenizenObject();
            case "food": return new ElementTag(event.getFoodLevel());
            case "item": return item;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onEntityFoodLevelChanged(FoodLevelChangeEvent event) {
        entity = new EntityTag(event.getEntity());
        item = event.getItem() != null ? new ItemTag(event.getItem()) : null;
        this.event = event;
        fire(event);
    }
}
