package net.aufdemrand.denizen.events.world;

import net.aufdemrand.denizen.objects.dInventory;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.events.ScriptEvent;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryPickupItemEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class InvPicksUpItemScriptEvent extends ScriptEvent implements Listener {

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
        String inv = CoreUtilities.getXthArg(0, lower);
        List<String> types = Arrays.asList("inventory", "hopper", "hopper_minecart");
        return lower.contains("picks up")
                && types.contains(inv);
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String inv = CoreUtilities.getXthArg(0, lower);
        String itemName = CoreUtilities.getXthArg(3, lower);
        if (!inv.equals("inventory")) {
            if (!inventory.getInventoryType().toString().toLowerCase().equals(inv)) {
                return false;
            }
        }
        if (!itemName.equals("item")) {
            if (!itemName.equals(item.identifySimpleNoIdentifier()) && !itemName.equals(inventory.identifySimple())) {
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
    public HashMap<String, dObject> getContext() {
        HashMap<String, dObject> context = super.getContext();
        context.put("inventory", inventory);
        context.put("item", item);
        return context;
    }

    @EventHandler
    public void onInvPicksUpItem(InventoryPickupItemEvent event) {
        inventory = dInventory.mirrorBukkitInventory(event.getInventory());
        item = new dItem(event.getItem());
        cancelled = event.isCancelled();
        fire();
        event.setCancelled(cancelled);
    }
}
