package com.denizenscript.denizen.events.block;

import com.denizenscript.denizen.objects.InventoryTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.BrewEvent;

public class BrewsScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // brewing stand brews
    //
    // @Regex ^on brewing stand brews$
    //
    // @Group Block
    //
    // @Switch in:<area> to only process the event if it occurred within a specified area.
    //
    // @Cancellable true
    //
    // @Triggers when a brewing stand brews a potion.
    //
    // @Context
    // <context.location> returns the LocationTag of the brewing stand.
    // <context.inventory> returns the InventoryTag of the brewing stand's contents.
    //
    // -->

    public BrewsScriptEvent() {
        instance = this;
    }

    public static BrewsScriptEvent instance;
    public LocationTag location;
    public BrewEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        return path.eventLower.startsWith("brewing stand brews");
    }

    @Override
    public boolean matches(ScriptPath path) {
        return runInCheck(path, location);
    }

    @Override
    public String getName() {
        return "Brews";
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("location")) {
            return location;
        }
        else if (name.equals("inventory")) {
            return InventoryTag.mirrorBukkitInventory(event.getContents());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onBrews(BrewEvent event) {
        location = new LocationTag(event.getBlock().getLocation());
        this.event = event;
        fire(event);
    }
}
