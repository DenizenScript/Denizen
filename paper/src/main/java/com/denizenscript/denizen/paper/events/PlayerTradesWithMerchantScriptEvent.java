package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.TradeTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import io.papermc.paper.event.player.PlayerPurchaseEvent;
import io.papermc.paper.event.player.PlayerTradeEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerTradesWithMerchantScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player trades with merchant
    //
    // @Switch result:<result> to only process the event if the player received a specific result item.
    //
    // @Location true
    //
    // @Plugin Paper
    //
    // @Group Paper
    //
    // @Cancellable true
    //
    // @Triggers when a player trades with a merchant (villager).
    //
    // @Context
    // <context.merchant> returns the villager that was traded with, if any (may be null for example with 'opentrades' command usage).
    // <context.trade> returns a TradeTag of the trade that was done.
    //
    // @Determine
    // TradeTag to change the trade that should be processed.
    //
    //
    // @Player Always.
    //
    // -->

    public PlayerTradesWithMerchantScriptEvent() {
        registerCouldMatcher("player trades with merchant");
        registerSwitches("result");
        this.<PlayerTradesWithMerchantScriptEvent, TradeTag>registerDetermination(null, TradeTag.class, (evt, context, trade) -> {
            evt.event.setTrade(trade.getRecipe());
        });
    }

    public PlayerPurchaseEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, event.getPlayer().getLocation())) {
            return false;
        }
        if (!path.tryObjectSwitch("result", new ItemTag(event.getTrade().getResult()))) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(event.getPlayer());
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "merchant" -> event instanceof PlayerTradeEvent tradeEvent ? new EntityTag(tradeEvent.getVillager()) : null;
            case "trade" -> new TradeTag(event.getTrade()).duplicate();
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void playerTradeEvent(PlayerPurchaseEvent event) {
        this.event = event;
        fire(event);
    }
}
