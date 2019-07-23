package com.denizenscript.denizen.events.world;

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
    // inventory picks up item
    // inventory picks up <item>
    // <inventory type> picks up item
    // <inventory type> picks up <item>
    //
    // @Regex ^on [^\s]+ picks up [^\s]+$
    //
    // @Group World
    //
    // @Switch in <area>
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
        instance = this;
    }

    public static InventoryPicksUpItemScriptEvent instance;
    public InventoryTag inventory;
    public ItemTag item;
    public InventoryPickupItemEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (path.eventArgLowerAt(0).equals("player")) {
            return false;
        }
        return path.eventArgLowerAt(1).equals("picks") && path.eventArgLowerAt(2).equals("up");
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!tryInventory(inventory, path.eventArgLowerAt(0))) {
            return false;
        }
        if (!tryItem(item, path.eventArgLowerAt(3))) {
            return false;
        }
        if (!runInCheck(path, event.getItem().getLocation())) {
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return "InventoryPicksUpItem";
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("item")) {
            return item;
        }
        else if (name.equals("inventory")) {
            return inventory;
        }
        else if (name.equals("entity")) {
            return new EntityTag(event.getItem());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onInvPicksUpItem(InventoryPickupItemEvent event) {
        inventory = InventoryTag.mirrorBukkitInventory(event.getInventory());
        item = new ItemTag(event.getItem());
        fire(event);
    }
}
