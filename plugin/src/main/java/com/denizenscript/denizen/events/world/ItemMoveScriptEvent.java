package com.denizenscript.denizen.events.world;

import com.denizenscript.denizen.objects.InventoryTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;

public class ItemMoveScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // item moves from inventory (to <inventory type>)
    // item moves from <inventory type> (to <inventory type>)
    // <item> moves from inventory (to <inventory type>)
    // <item> moves from <inventory type> (to <inventory type>)
    //
    // @Group World
    //
    // @Regex ^on [^\s]+ moves from [^\s]+( to [^\s]+)?$
    //
    // @Switch in <area>
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
    // ItemTag to set a different item to be moved. NOTE: The original item will not be moved!
    //
    // -->

    public ItemMoveScriptEvent() {
        instance = this;
    }

    public static ItemMoveScriptEvent instance;

    public InventoryTag origin;
    public InventoryTag destination;
    public ItemTag item;
    public InventoryMoveItemEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        return path.eventArgLowerAt(1).equals("moves") && path.eventArgLowerAt(2).equals("from");
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!tryItem(item, CoreUtilities.getXthArg(0, path.eventLower))) {
            return false;
        }
        if (!tryInventory(origin, CoreUtilities.getXthArg(3, path.eventLower))) {
            return false;
        }
        if (CoreUtilities.xthArgEquals(4, path.eventLower, "to")) {
            if (!tryInventory(destination, CoreUtilities.getXthArg(5, path.eventLower))) {
                return false;
            }
        }
        if (!runInCheck(path, origin.getLocation())) {
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return "ItemMoves";
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        if (ItemTag.matches(determination)) {
            item = ItemTag.valueOf(determination, container);
            event.setItem(item.getItemStack());
            return true;
        }
        return super.applyDetermination(container, determination);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("origin")) {
            return origin;
        }
        else if (name.equals("destination")) {
            return destination;
        }
        else if (name.equals("initiator")) {
            return InventoryTag.mirrorBukkitInventory(event.getInitiator());
        }
        else if (name.equals("item")) {
            return item;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onInventoryMoveItemEvent(InventoryMoveItemEvent event) {
        origin = InventoryTag.mirrorBukkitInventory(event.getSource());
        destination = InventoryTag.mirrorBukkitInventory(event.getDestination());
        item = new ItemTag(event.getItem());
        fire(event);
    }
}
