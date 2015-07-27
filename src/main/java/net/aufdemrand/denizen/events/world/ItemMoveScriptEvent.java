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
import org.bukkit.event.inventory.InventoryMoveItemEvent;

import java.util.HashMap;

public class ItemMoveScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // item moves from inventory (to <inventory type>)
    // item moves from <inventory type> (to <inventory type>)
    // <item> moves from inventory (to <inventory type>)
    // <item> moves from <inventory type> (to <inventory type>)
    //
    // @Cancellable true
    //
    // @Triggers when an entity or block moves an item from one inventory to another. (Hopper-style movement, not player-induced movement).
    //
    // @Context
    // <context.origin> returns the origin dInventory.
    // <context.destination> returns the destination dInventory.
    // <context.initiator> returns the dInventory that initiatied the item's transfer.
    // <context.item> returns the dItem that was moved.
    //
    // @Determine
    // dItem to set a different item to be moved. NOTE: The original item will not be moved!
    //
    // -->

    public ItemMoveScriptEvent() {
        instance = this;
    }

    public static ItemMoveScriptEvent instance;

    public dInventory origin;
    public dInventory destination;
    public dInventory initiator;
    public dItem item;
    public boolean itemSet;
    public InventoryMoveItemEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return lower.contains("moves from");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String iCheck = CoreUtilities.getXthArg(0, lower);
        String oCheck = CoreUtilities.getXthArg(3, lower);
        String dCheck = CoreUtilities.getXthArg(5, lower);
        String originType = CoreUtilities.toLowerCase(origin.getInventoryType().name());
        String destinationType = CoreUtilities.toLowerCase(destination.getInventoryType().name());

        if (!tryItem(item, iCheck)) {
            return false;
        }
        if (!oCheck.equals(originType)) {
            return false;
        }
        if (dCheck.length() > 0) {
            if (!dCheck.equals(destinationType)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String getName() {
        return "ItemMoves";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        InventoryMoveItemEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        if (dItem.matches(determination)) {
            item = dItem.valueOf(determination);
            itemSet = true;
            return true;
        }
        return super.applyDetermination(container, determination);
    }

    @Override
    public HashMap<String, dObject> getContext() {
        HashMap<String, dObject> context = super.getContext();
        context.put("origin", origin);
        context.put("destination", destination);
        context.put("initiator", initiator);
        context.put("item", item);
        return context;
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryMoveItemEvent(InventoryMoveItemEvent event) {
        origin = dInventory.mirrorBukkitInventory(event.getSource());
        destination = dInventory.mirrorBukkitInventory(event.getDestination());
        initiator = dInventory.mirrorBukkitInventory(event.getInitiator());
        item = new dItem(event.getItem());
        itemSet = false;
        cancelled = event.isCancelled();
        fire();
        event.setCancelled(cancelled);
        if (itemSet) {
            event.setItem(item.getItemStack());
        }
    }
}
