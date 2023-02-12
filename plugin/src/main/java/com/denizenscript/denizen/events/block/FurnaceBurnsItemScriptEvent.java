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
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        if (determinationObj instanceof ElementTag element && element.isInt()) {
            event.setBurnTime(element.asInt());
            return true;
        }
        else if (determinationObj.canBeType(DurationTag.class)) {
            event.setBurnTime(determinationObj.asType(DurationTag.class, getTagContext(path)).getTicksAsInt());
            return true;
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "location": return location;
            case "item": return item;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onBrews(FurnaceBurnEvent event) {
        location = new LocationTag(event.getBlock().getLocation());
        item = new ItemTag(event.getFuel());
        this.event = event;
        fire(event);
    }
}
