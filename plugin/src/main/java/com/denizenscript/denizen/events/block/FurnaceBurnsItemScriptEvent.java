package com.denizenscript.denizen.events.block;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceBurnEvent;

public class FurnaceBurnsItemScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // furnace burns <item>
    //
    // @Group Block
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when a furnace burns an item used as fuel.
    //
    // @Context
    // <context.location> returns the LocationTag of the furnace.
    // <context.item> returns the ItemTag burnt.
    //
    // @Determine
    // DurationTag to set the burn time for this fuel.
    //
    // -->

    public FurnaceBurnsItemScriptEvent() {
        registerCouldMatcher("furnace burns <item>");
        this.<FurnaceBurnsItemScriptEvent, ObjectTag>registerOptionalDetermination(null, ObjectTag.class, (evt, context, time) -> {
            if (time instanceof ElementTag elementTag && elementTag.isInt()) { // Backwards compatibility for non-duration tick input
                evt.event.setBurnTime(elementTag.asInt());
                return true;
            }
            else if (time.canBeType(DurationTag.class)) {
                evt.event.setBurnTime(time.asType(DurationTag.class, context).getTicksAsInt());
                return true;
            }
            return false;
        });
    }

    public ItemTag item;
    public LocationTag location;
    public FurnaceBurnEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!path.tryArgObject(2, item)) {
            return false;
        }
        if (!runInCheck(path, location)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "location" -> location;
            case "item" -> item;
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void onFurnaceBurns(FurnaceBurnEvent event) {
        location = new LocationTag(event.getBlock().getLocation());
        item = new ItemTag(event.getFuel());
        this.event = event;
        fire(event);
    }
}
