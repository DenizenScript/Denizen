package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.InventoryTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
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
    // @Regex ^on player drags( [^\s]+)?( in [^\s]+)?$
    //
    // @Group Player
    //
    // @Switch in_area:<area> replaces the default 'in:<area>' for this event.
    // @Switch drag_type:<type> to only run the event if the given drag type (SINGLE or EVEN) was used.
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when a player drags in an inventory (that is, clicks and then holds the mouse button down while moving the mouse across multiple slots).
    //
    // @Context
    // <context.item> returns the ItemTag the player has dragged.
    // <context.inventory> returns the InventoryTag (the 'top' inventory, regardless of which slot was clicked).
    // <context.clicked_inventory> returns the InventoryTag that was clicked in.
    // <context.slots> returns a ListTag of the slot numbers dragged through.
    // <context.raw_slots> returns a ListTag of the raw slot numbers dragged through.
    // <context.drag_type> returns either SINGLE or EVEN depending on whether the player used their left or right mouse button.
    //
    // @Player Always.
    //
    // -->

    public PlayerDragsInInvScriptEvent() {
    }


    public Inventory inventory;
    public ItemTag item;
    private PlayerTag entity;
    private InventoryTag dInv;
    public InventoryDragEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        return path.eventLower.startsWith("player drags");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String arg2 = path.eventArgLowerAt(2);
        String arg3 = path.eventArgLowerAt(3);
        String arg4 = path.eventArgLowerAt(4);
        String inv = arg2.equals("in") ? arg3 : arg3.equals("in") ? arg4 : "";
        if (!inv.equals("") && !dInv.tryAdvancedMatcher(inv)) {
            return false;
        }
        if (!arg2.equals("in") && !item.tryAdvancedMatcher(arg2)) {
            return false;
        }
        if (!runInCheck(path, entity.getLocation(), "in_area")) {
            return false;
        }
        if (!runGenericSwitchCheck(path, "drag_type", event.getType().name())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(entity, null);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "inventory":
                return dInv;
            case "slots":
                ListTag slots = new ListTag();
                for (Integer slot : event.getInventorySlots()) {
                    slots.add(String.valueOf(slot + 1));
                }
                return slots;
            case "raw_slots":
                ListTag raw_slots = new ListTag();
                for (Integer raw_slot : event.getRawSlots()) {
                    raw_slots.add(String.valueOf(raw_slot + 1));
                }
                return raw_slots;
            case "item":
                return item;
            case "clicked_inventory":
                return InventoryTag.mirrorBukkitInventory(event.getView()
                        .getInventory(event.getRawSlots().stream().findFirst().orElse(0)));
            case "drag_type":
                return new ElementTag(event.getType());
        }
        return super.getContext(name);
    }

    @Override
    public void cancellationChanged() {
        if (cancelled) {
            final InventoryHolder holder = inventory.getHolder();
            new BukkitRunnable() {
                @Override
                public void run() {
                    entity.getPlayerEntity().updateInventory();
                    if (holder instanceof Player) {
                        ((Player) holder).updateInventory();
                    }
                }
            }.runTaskLater(Denizen.getInstance(), 1);
        }
        super.cancellationChanged();
    }

    @EventHandler
    public void onPlayerDragsInInv(InventoryDragEvent event) {
        if (EntityTag.isCitizensNPC(event.getWhoClicked())) {
            return;
        }
        entity = EntityTag.getPlayerFrom(event.getWhoClicked());
        inventory = event.getInventory();
        dInv = InventoryTag.mirrorBukkitInventory(inventory);
        item = new ItemTag(event.getOldCursor());
        this.event = event;
        fire(event);
    }
}
