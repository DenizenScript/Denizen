package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.TradeTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;

public class VillagerAcquiresTradeScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // villager acquires trade
    //
    // @Regex ^on villager acquires trade$
    //
    // @Group Entity
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when a villager acquires a new trade.
    //
    // @Context
    // <context.entity> returns the EntityTag of the villager.
    // <context.trade> returns the TradeTag for the new trade.
    //
    // @Determine
    // TradeTag to change the new trade.
    // -->

    public VillagerAcquiresTradeScriptEvent() {
    }

    public EntityTag entity;
    public VillagerAcquireTradeEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        return path.eventLower.startsWith("villager acquires trade");
    }

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
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("entity")) {
            return entity;
        }
        else if (name.equals("trade")) {
            return new TradeTag(event.getRecipe()).duplicate();
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onVillagerAcquiresTrade(VillagerAcquireTradeEvent event) {
        this.event = event;
        entity = new EntityTag(event.getEntity());
        fire(event);
    }
}
