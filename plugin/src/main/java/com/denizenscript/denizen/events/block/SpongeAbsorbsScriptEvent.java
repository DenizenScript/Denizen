package com.denizenscript.denizen.events.block;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SpongeAbsorbEvent;
import org.bukkit.event.block.TNTPrimeEvent;

public class SpongeAbsorbsScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // sponge absorbs
    //
    // @Location true
    //
    // @Group Block
    //
    // @Warning this event may in some cases double-fire, requiring usage of the 'ratelimit' command (like 'ratelimit <script> 1t') to prevent doubling actions.
    //
    // @Cancellable true
    //
    // @Triggers when Sponge absorbs water.
    //
    // @Context
    // <context.location> returns the location of the Sponge.
    // <context.blocks> returns a ListTag of blocks that are being removed.
    //
    // -->

    public SpongeAbsorbsScriptEvent() {
        registerCouldMatcher("sponge absorbs");
    }

    public SpongeAbsorbEvent event;
    public LocationTag location;
    public ListTag blocks;

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "location" -> location;
            case "blocks" -> blocks;
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void onSpongeAbsorbEvent(SpongeAbsorbEvent event) {
        this.event = event;
        location = new LocationTag(event.getBlock().getLocation());
        ListTag blocks = new ListTag();
        for (BlockState blockState : event.getBlocks()) {
            blocks.addObject(new LocationTag(blockState.getLocation()));
        }
        this.blocks = blocks;
        fire(event);
    }
}