package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class EntityFoodLevelChangeScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // entity changes food level
    // <entity> changes food level
    //
    // @Regex ^on [^\s]+ changes food level$
    // @Switch in <area>
    //
    // @Cancellable true
    //
    // @Triggers when an entity's food level changes.
    //
    // @Context
    // <context.entity> returns the EntityTag.
    // <context.food> returns an Element(Number) of the entity's new food level.
    //
    // @Determine
    // Element(Decimal) to set the entity's new food level.
    //
    // @Player when the entity that's food level has changed is a player.
    //
    // @NPC when the entity that's food level has changed is an NPC.
    //
    // -->

    public EntityFoodLevelChangeScriptEvent() {
        instance = this;
    }

    public static EntityFoodLevelChangeScriptEvent instance;
    public EntityTag entity;
    public Integer food;
    public FoodLevelChangeEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return (CoreUtilities.toLowerCase(s).contains("changes food level"));
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

        return true;
    }

    @Override
    public String getName() {
        return "FoodLevelChanged";
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        if (ArgumentHelper.matchesInteger(determination)) {
            food = ArgumentHelper.getIntegerFrom(determination);
            return true;
        }
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(entity.isPlayer() ? EntityTag.getPlayerFrom(event.getEntity()) : null,
                entity.isCitizensNPC() ? EntityTag.getNPCFrom(event.getEntity()) : null);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("entity")) {
            return entity.getDenizenObject();
        }
        else if (name.equals("food")) {
            return new ElementTag(food);
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onEntityFoodLevelChanged(FoodLevelChangeEvent event) {
        entity = new EntityTag(event.getEntity());
        food = event.getFoodLevel();
        this.event = event;
        fire(event);
        event.setFoodLevel(food);
    }
}
