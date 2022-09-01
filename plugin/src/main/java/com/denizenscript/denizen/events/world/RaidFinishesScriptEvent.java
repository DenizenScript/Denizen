package com.denizenscript.denizen.events.world;

import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.raid.RaidFinishEvent;

public class RaidFinishesScriptEvent extends RaidScriptEvent<RaidFinishEvent> implements Listener {

    // <--[event]
    // @Events
    // raid finishes
    //
    // @Group World
    //
    // @Location true
    //
    // @Triggers when a village raid finishes normally.
    //
    // @Context
    // <context.raid> returns the raid data. See <@link language Raid Event Data>.
    // <context.winners> returns the ListTag of players who completed the raid. This is separate from the raid's heroes in that the winners are guaranteed to be online.
    //
    // -->

    public RaidFinishesScriptEvent() {
        super(true);
        registerCouldMatcher("raid finishes");
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "winners":
                ListTag list = new ListTag();
                for (Player player : event.getWinners()) {
                    list.addObject(new PlayerTag(player));
                }
                return list;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onRaidFinishes(RaidFinishEvent event) {
        this.event = event;
        fire(event);
    }
}
