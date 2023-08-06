package com.denizenscript.denizen.events.block;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SpongeAbsorbEvent;

public class SpongeAbsorbsScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // sponge absorbs water
    //
    // @Location true
    //
    // @Group Block
    //
    // @Warning this event may in some cases double-fire, requiring usage of the 'ratelimit' command (like 'ratelimit <context.location> 1t') to prevent doubling actions.
    //
    // @Cancellable true
    //
    // @Triggers when a sponge block absorbs water.
    //
    // @Context
    // <context.location> returns the location of the sponge.
    // <context.blocks> returns a ListTag(LocationTag) of blocks (of water) that are being removed.
    //
    // -->

    public SpongeAbsorbsScriptEvent() {
        registerCouldMatcher("sponge absorbs water");
    }

    public SpongeAbsorbEvent event;

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "location" -> new LocationTag(event.getBlock().getLocation());
            case "blocks" -> new ListTag(event.getBlocks(), block -> new LocationTag(block.getLocation()));
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void onSpongeAbsorbEvent(SpongeAbsorbEvent event) {
        this.event = event;
        fire(event);
    }
}
