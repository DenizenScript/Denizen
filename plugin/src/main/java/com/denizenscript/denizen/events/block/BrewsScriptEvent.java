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
    // @Group Block
    //
    // @Location true
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
        registerCouldMatcher("brewing stand brews");
    }

    public LocationTag location;
    public BrewEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, location)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "location": return location;
            case "inventory": return InventoryTag.mirrorBukkitInventory(event.getContents());
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
