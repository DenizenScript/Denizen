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
    // @Regex ^on villager replenishes trade$
    //
    // @Group Entity
    //
    // @Switch in:<area> to only process the event if it occurred within a specified area.
    //
    // @Cancellable true
    //
    // @Triggers when a villager replenishes a trade.
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
        instance = this;
    }

    public static VillagerReplenishesTradeScriptEvent instance;
    public EntityTag entity;
    public VillagerReplenishTradeEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        return path.eventLower.startsWith("villager replenishes trade");
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, entity.getLocation())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public String getName() {
        return "VillagerReplenishesTrade";
    }


    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        if (TradeTag.matches(determinationObj.toString())) {
            event.setRecipe(((TradeTag) determinationObj).getRecipe());
            return true;
        }
        else if (((ElementTag) determinationObj).isInt()) {
            event.setBonus(((ElementTag) determinationObj).asInt());
            return true;
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("entity")) {
            return entity;
        }
        else if (name.equals("trade")) {
            return new TradeTag(event.getRecipe());
        }
        else if (name.equals("bonus")) {
            return new ElementTag(event.getBonus());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onVillagerReplenishesTrade(VillagerReplenishTradeEvent event) {
        this.event = event;
        fire(event);
    }
}
