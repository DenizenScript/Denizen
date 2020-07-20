package com.denizenscript.denizen.events.block;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceBurnEvent;

public class FurnaceBurnsItemScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // furnace burns item
    // furnace burns <item>
    //
    // @Regex ^on furnace burns [^\s]+$
    //
    // @Group Block
    //
    // @Switch in:<area> to only process the event if it occurred within a specified area.
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
        instance = this;
    }

    public static FurnaceBurnsItemScriptEvent instance;
    public ItemTag item;
    public LocationTag location;
    public FurnaceBurnEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!path.eventLower.startsWith("furnace burns")) {
            return false;
        }
        if (!couldMatchItem(path.eventArgLowerAt(2))) {
            return false;
        }
        return true;
    }

    @Override
    public boolean matches(ScriptPath path) {
        String iTest = path.eventArgLowerAt(2);
        if (!tryItem(item, iTest)) {
            return false;
        }
        if (!runInCheck(path, location)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public String getName() {
        return "FurnaceBurns";
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        if (determinationObj instanceof ElementTag && ((ElementTag) determinationObj).isInt()) {
            event.setBurnTime(((ElementTag) determinationObj).asInt());
            return true;
        }
        else if (DurationTag.matches(determinationObj.toString())) {
            event.setBurnTime(DurationTag.valueOf(determinationObj.toString(), getTagContext(path)).getTicksAsInt());
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("location")) {
            return location;
        }
        else if (name.equals("item")) {
            return item;
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
