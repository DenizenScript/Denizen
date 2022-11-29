package com.denizenscript.denizen.events.item;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.InventoryTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryPickupItemEvent;

public class InventoryPicksUpItemScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // <inventory> picks up <item>
    //
    // @Group Item
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when a hopper or hopper minecart picks up an item.
    //
    // @Context
    // <context.inventory> returns the InventoryTag that picked up the item.
    // <context.item> returns the ItemTag.
    // <context.entity> returns a EntityTag of the item entity.
    //
    // -->

    public InventoryPicksUpItemScriptEvent() {
        registerCouldMatcher("<inventory> picks up <item>");
    }

    public InventoryTag inventory;
    public ItemTag item;
    public InventoryPickupItemEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!super.couldMatch(path)) {
            return false;
        }
        if (couldMatchEntity(path.eventArgLowerAt(0))) {
            return false;
        }
        return true;
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!path.tryArgObject(0, inventory)) {
            return false;
        }
        if (!path.tryArgObject(3, item)) {
            return false;
        }
        if (!runInCheck(path, event.getItem().getLocation())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "item": return item;
            case "inventory": return inventory;
            case "entity": return new EntityTag(event.getItem());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onInvPicksUpItem(InventoryPickupItemEvent event) {
        this.event = event;
        inventory = InventoryTag.mirrorBukkitInventory(event.getInventory());
        item = new ItemTag(event.getItem().getItemStack());
        fire(event);
    }
}
