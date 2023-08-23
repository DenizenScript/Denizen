package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import io.papermc.paper.event.player.PlayerOpenSignEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerOpenSignScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player opens sign
    //
    // @Plugin Paper
    //
    // @Group Paper
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers When player open sign.
    //
    // @Context
    // <context.side> returns an ElementTag of side the sign was clicked.
    // <context.cause> returns an ElementTag of reason the sign was opened.
    // <context.location> returns a LocationTag of the sign's location.
    //
    // @Player Always.
    //
    // -->

    public PlayerOpenSignScriptEvent() {
        registerCouldMatcher("player opens sign");
    }

    public PlayerOpenSignEvent event;
    public LocationTag location;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, location)) {
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
            case "side" -> new ElementTag(event.getSide());
            case "cause" -> new ElementTag(event.getCause());
            case "location" -> location;
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void playerOpenSignEvent(PlayerOpenSignEvent event) {
        location = new LocationTag(event.getSign().getLocation());
        this.event = event;
        fire(event);
    }
}