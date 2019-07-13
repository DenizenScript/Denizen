package com.denizenscript.denizen.events.world;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
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
    // @Switch in <area>
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
    // Element(Number) to set the burn time for this fuel.
    //
    // -->

    public FurnaceBurnsItemScriptEvent() {
        instance = this;
    }

    public static FurnaceBurnsItemScriptEvent instance;
    public ItemTag item;
    public LocationTag location;
    private Integer burntime;
    public FurnaceBurnEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).startsWith("furnace burns");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String iTest = path.eventArgLowerAt(2);
        return tryItem(item, iTest)
                && runInCheck(path, location);
    }

    @Override
    public String getName() {
        return "FurnaceBurns";
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        if (ArgumentHelper.matchesInteger(determination)) {
            burntime = ArgumentHelper.getIntegerFrom(determination);
            return true;
        }
        return super.applyDetermination(container, determination);
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
        burntime = event.getBurnTime();
        this.event = event;
        fire(event);
        event.setBurnTime(burntime);
    }
}
