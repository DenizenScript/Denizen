package net.aufdemrand.denizen.events.world;

import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dInventory;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryPickupItemEvent;

import java.util.HashMap;

public class InvPicksUpItemScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // inventory picks up item
    // inventory picks up <item>
    // <inventory type> picks up item
    // <inventory type> picks up <item>
    //
    // @Cancellable true
    //
    // @Triggers when a hopper or hopper minecart picks up an item.
    //
    // @Context
    // <context.inventory> returns the dInventory that picked up the item.
    // <context.item> returns the dItem.
    //
    // -->

    public InvPicksUpItemScriptEvent() {
        instance = this;
    }

    public static InvPicksUpItemScriptEvent instance;
    public dInventory inventory;
    public dItem item;
    public InventoryPickupItemEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return lower.contains("picks up") && !lower.startsWith("player");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String inv = CoreUtilities.getXthArg(0, lower);
        String itemName = CoreUtilities.getXthArg(3, lower);
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
        return "InvPicksUpItem";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        InventoryPickupItemEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("item")) {
            return item;
        }
        else if (name.equals("inventory")) {
            return inventory;
        }
        return super.getContext(name);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInvPicksUpItem(InventoryPickupItemEvent event) {
        inventory = dInventory.mirrorBukkitInventory(event.getInventory());
        item = new dItem(event.getItem());
        cancelled = event.isCancelled();
        fire();
        event.setCancelled(cancelled);
    }
}
