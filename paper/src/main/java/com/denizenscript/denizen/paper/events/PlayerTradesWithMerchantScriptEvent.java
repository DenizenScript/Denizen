package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.TradeTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import io.papermc.paper.event.player.PlayerTradeEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerTradesWithMerchantScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player trades with merchant
    //
    // @Regex ^on player trades with merchant$
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
    // <context.merchant> returns the villager that was traded with.
    // <context.trade> returns a TradeTag of the trade that was done.
    //
    // @Player Always.
    //
    // -->

    public PlayerTradesWithMerchantScriptEvent() {
        instance = this;
    }

    public static PlayerTradesWithMerchantScriptEvent instance;
    public PlayerTradeEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!path.eventLower.startsWith("player trades with merchant")) {
            return false;
        }
        return true;
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, event.getPlayer().getLocation())) {
            return false;
        }
        if (path.switches.containsKey("result") && !tryItem(new ItemTag(event.getTrade().getResult()), path.switches.get("result"))) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public String getName() {
        return "PlayerTradesWithMerchant";
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(event.getPlayer());
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("merchant")) {
            return new EntityTag(event.getVillager());
        }
        else if (name.equals("trade")) {
            return new TradeTag(event.getTrade()).duplicate();
        }
        return super.getContext(name);
    }

    @EventHandler
    public void playerTradeEvent(PlayerTradeEvent event) {
        this.event = event;
        fire(event);
    }
}
