package com.denizenscript.denizen.events.world;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.InventoryTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryPickupItemEvent;

public class InventoryPicksUpItemScriptEvent extends BukkitScriptEvent implements Listener {

    // TODO: Add in <area>
    // <--[event]
    // @Events
    // inventory picks up item
    // inventory picks up <item>
    // <inventory type> picks up item
    // <inventory type> picks up <item>
    //
    // @Regex ^on [^\s]+ picks up [^\s]+$
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
    public EntityTag entity;
    public InventoryPickupItemEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return lower.contains("picks up") && !lower.startsWith("player");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String inv = path.eventArgLowerAt(0);
        String itemName = path.eventArgLowerAt(3);
        if (!inv.equals("inventory")) {
            if (!inv.equals(CoreUtilities.toLowerCase(inventory.getInventoryType().toString()))) {
                return false;
            }
        }
        if (!itemName.equals("item")) {
            if (!tryItem(item, itemName)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String getName() {
        return "InventoryPicksUpItem";
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
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
            return entity;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onInvPicksUpItem(InventoryPickupItemEvent event) {
        inventory = InventoryTag.mirrorBukkitInventory(event.getInventory());
        item = new ItemTag(event.getItem());
        entity = new EntityTag(event.getItem());
        fire(event);
    }
}
