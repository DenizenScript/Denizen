package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizen.objects.dInventory;
import com.denizenscript.denizen.objects.dItem;
import com.denizenscript.denizen.objects.dPlayer;
import com.denizenscript.denizen.utilities.DenizenAPI;
import com.denizenscript.denizen.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerDragsInInvScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player drags in inventory
    // player drags (<item>) (in <inventory>)
    //
    // @Regex ^on player drags( ^[\s]+)?(in [^\s]+)?( in_area ((notable (cuboid|ellipsoid))|([^\s]+)))?$
    // @Switch in_area <area>
    //
    // @Cancellable true
    //
    // @Triggers when a player drags in an inventory.
    //
    // @Context
    // <context.item> returns the dItem the player has dragged.
    // <context.inventory> returns the dInventory (the 'top' inventory, regardless of which slot was clicked).
    // <context.clicked_inventory> returns the dInventory that was clicked in.
    // <context.slots> returns a ListTag of the slot numbers dragged through.
    // <context.raw_slots> returns a ListTag of the raw slot numbers dragged through.
    //
    // -->

    public PlayerDragsInInvScriptEvent() {
        instance = this;
    }

    public static PlayerDragsInInvScriptEvent instance;

    public Inventory inventory;
    public dItem item;
    private dPlayer entity;
    private dInventory dInv;
    public InventoryDragEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return lower.startsWith("player drags");
    }

    @Override
    public boolean matches(ScriptPath path) {

        String arg2 = path.eventArgLowerAt(2);
        String arg3 = path.eventArgLowerAt(3);
        String arg4 = path.eventArgLowerAt(4);
        String inv = arg2.equals("in") ? arg3 : arg3.equals("in") ? arg4 : "";
        if (!inv.equals("") && !tryInventory(dInv, inv)) {
            return false;
        }
        if (!arg2.equals("in") && !tryItem(item, arg2)) {
            return false;
        }
        return runInCheck(path, entity.getLocation(), "in_area");
    }

    @Override
    public String getName() {
        return "PlayerDragsInInventory";
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(entity, null);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("inventory")) {
            return dInv;
        }
        else if (name.equals("slots")) {
            ListTag slots = new ListTag();
            for (Integer slot : event.getInventorySlots()) {
                slots.add(String.valueOf(slot + 1));
            }
            return slots;
        }
        else if (name.equals("raw_slots")) {
            ListTag raw_slots = new ListTag();
            for (Integer raw_slot : event.getRawSlots()) {
                raw_slots.add(String.valueOf(raw_slot + 1));
            }
            return raw_slots;
        }
        else if (name.equals("item")) {
            return item;
        }
        else if (name.equals("clicked_inventory")) {
            return dInventory.mirrorBukkitInventory(event.getView()
                    .getInventory(event.getRawSlots().stream().findFirst().orElse(0)));
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerDragsInInv(InventoryDragEvent event) {
        if (dEntity.isCitizensNPC(event.getWhoClicked())) {
            return;
        }
        entity = dEntity.getPlayerFrom(event.getWhoClicked());
        inventory = event.getInventory();
        dInv = dInventory.mirrorBukkitInventory(inventory);
        item = new dItem(event.getOldCursor());
        boolean wasCancelled = event.isCancelled();
        this.event = event;
        fire(event);
        if (cancelled && !wasCancelled) {
            final InventoryHolder holder = inventory.getHolder();
            new BukkitRunnable() {
                @Override
                public void run() {
                    entity.getPlayerEntity().updateInventory();
                    if (holder != null && holder instanceof Player) {
                        ((Player) holder).updateInventory();
                    }
                }
            }.runTaskLater(DenizenAPI.getCurrentInstance(), 1);
        }
    }
}
