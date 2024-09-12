package com.denizenscript.denizen.events.block;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BellRingEvent;

public class BellRingScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // bell rings
    //
    // @Location true
    //
    // @Group Block
    //
    // @Cancellable true
    //
    // @Triggers when a bell block rings. (Requires Paper on versions lower than 1.19)
    //
    // @Context
    // <context.entity> returns the EntityTag that rung the bell, if any.
    // <context.location> returns the LocationTag of the bell being rung.
    // <context.direction> returns the ElementTag of the direction the bell was rung. Available only on MC 1.19+.
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
        return switch (name) {
            case "entity" -> event.getEntity() == null ? null : new EntityTag(event.getEntity());
            case "location" -> location;
            case "direction" -> new ElementTag(event.getDirection());
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void bellRingEvent(BellRingEvent event) {
        this.event = event;
        location = new LocationTag(event.getBlock().getLocation());
        fire(event);
    }
}
