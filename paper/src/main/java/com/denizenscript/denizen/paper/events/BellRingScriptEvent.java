package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import io.papermc.paper.event.block.BellRingEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class BellRingScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // bell rings
    //
    // @Location true
    //
    // @Plugin Paper
    //
    // @Group Paper
    //
    // @Cancellable true
    //
    // @Triggers when a bell block rings.
    //
    // @Context
    // <context.entity> returns the entity that rung the bell, if any.
    // <context.location> returns the location of the bell being rung.
    //
    // @Player when the ringing entity is a player.
    //
    // -->

    public BellRingScriptEvent() {
        registerCouldMatcher("bell rings");
    }

    public BellRingEvent event;
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
        return new BukkitScriptEntryData(event.getEntity());
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "entity":
                return event.getEntity() == null ? null : new EntityTag(event.getEntity());
            case "location": return location;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void bellRingEvent(BellRingEvent event) {
        this.event = event;
        location = new LocationTag(event.getBlock().getLocation());
        fire(event);
    }
}
