package com.denizenscript.denizen.events.item;

import com.denizenscript.denizen.objects.InventoryTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;

public class ItemMoveScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // <item> moves from <inventory> (to <inventory>)
    //
    // @Group Item
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when an entity or block moves an item from one inventory to another. (Hopper-style movement, not player-induced movement).
    //
    // @Context
    // <context.origin> returns the origin InventoryTag.
    // <context.destination> returns the destination InventoryTag.
    // <context.initiator> returns the InventoryTag that initiated the item's transfer.
    // <context.item> returns the ItemTag that was moved.
    //
    // @Determine
    // ItemTag to set a different item to be moved.
    //
    // -->

    public ItemMoveScriptEvent() {
        registerCouldMatcher("<item> moves from <inventory> (to <inventory>)");
    }


    public InventoryTag origin;
    public InventoryTag destination;
    public ItemTag item;
    public InventoryMoveItemEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!path.tryArgObject(0, item)) {
            return false;
        }
        if (!path.tryArgObject(3, origin)) {
            return false;
        }
        if (path.eventArgLowerAt(4).equals("to") && !path.tryArgObject(5, destination)) {
            return false;
        }
        if (!runInCheck(path, origin.getLocation())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        if (determinationObj.canBeType(ItemTag.class)) {
            item = determinationObj.asType(ItemTag.class, getTagContext(path));
            event.setItem(item.getItemStack());
            return true;
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "origin": return origin;
            case "destination": return destination;
            case "initiator": return InventoryTag.mirrorBukkitInventory(event.getInitiator());
            case "item": return item;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onInventoryMoveItemEvent(InventoryMoveItemEvent event) {
        this.event = event;
        origin = InventoryTag.mirrorBukkitInventory(event.getSource());
        destination = InventoryTag.mirrorBukkitInventory(event.getDestination());
        item = new ItemTag(event.getItem());
        fire(event);
    }
}
