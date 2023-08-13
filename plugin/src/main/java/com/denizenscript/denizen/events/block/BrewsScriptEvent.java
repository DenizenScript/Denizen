package com.denizenscript.denizen.events.block;

import com.denizenscript.denizen.objects.InventoryTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.inventory.ItemStack;

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
    // <context.fuel_level> returns an ElementTag(Number) of the brewing stand's fuel level.
    // <context.result> returns a ListTag(ItemTag) of the brewing stand's result.
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
        return switch (name) {
            case "location" -> location;
            case "inventory" -> InventoryTag.mirrorBukkitInventory(event.getContents());
            case "fuel_level" -> new ElementTag(event.getFuelLevel());
            case "result" -> new ListTag(event.getResults(), ItemTag::new);
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void onBrews(BrewEvent event) {
        location = new LocationTag(event.getBlock().getLocation());
        this.event = event;
        fire(event);
    }
}
