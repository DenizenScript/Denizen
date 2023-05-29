package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.destroystokyo.paper.event.player.PlayerReadyArrowEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerReadiesArrowScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player readies arrow
    //
    // @Plugin Paper
    //
    // @Group Paper
    //
    // @Cancellable true
    //
    // @Triggers when a player is firing a bow and the server is choosing an arrow to use.
    // Upon cancellation of this event, the server will try the next available arrow and trigger another event.
    // The arrow will temporarily be removed from the player's inventory, a client-side, and will come back upon being updated.
    //
    // @Context
    // <context.bow> returns an ItemTag of the bow that is about to be used.
    // <context.arrow> returns an ItemTag of the arrow that is about to be used.
    //
    // -->

    public PlayerReadiesArrowScriptEvent() {
        registerCouldMatcher("player readies arrow");
    }

    public PlayerReadyArrowEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        return super.matches(path);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(event.getPlayer());
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "arrow" -> new ItemTag(event.getArrow());
            case "bow" -> new ItemTag(event.getBow());
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void onPlayerReadiesArrow(PlayerReadyArrowEvent event) {
        this.event = event;
        fire(event);
    }
}
