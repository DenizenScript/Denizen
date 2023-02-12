package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.TradeTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerReplenishTradeEvent;

public class VillagerReplenishesTradeScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // villager replenishes trade
    //
    // @Group Entity
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when a villager replenishes a trade. A trade being "replenished" means its "uses" value is reset to "0".
    //
    // @Context
    // <context.entity> returns the EntityTag of the villager.
    // <context.trade> returns the TradeTag for the trade being replenished.
    // <context.bonus> returns the number of bonus uses added.
    //
    // @Determine
    // TradeTag to change the trade being replenished.
    // ElementTag(Number) to change the number of bonus uses added.
    // -->

    public VillagerReplenishesTradeScriptEvent() {
        registerCouldMatcher("villager replenishes trade");
    }

    public EntityTag entity;
    public VillagerReplenishTradeEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, entity.getLocation())) {
            return false;
        }
        return super.matches(path);
    }


    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        if (TradeTag.matches(determinationObj.toString())) {
            event.setRecipe(determinationObj.asType(TradeTag.class, getTagContext(path)).getRecipe());
            return true;
        }
        else if (determinationObj instanceof ElementTag element && element.isInt()) {
            event.setBonus(element.asInt());
            return true;
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "entity": return entity;
            case "trade": return new TradeTag(event.getRecipe()).duplicate();
            case "bonus": return new ElementTag(event.getBonus());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onVillagerReplenishesTrade(VillagerReplenishTradeEvent event) {
        this.event = event;
        entity = new EntityTag(event.getEntity());
        fire(event);
    }
}
