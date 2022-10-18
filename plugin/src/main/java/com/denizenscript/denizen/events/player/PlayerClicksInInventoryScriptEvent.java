package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.*;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashSet;

public class PlayerClicksInInventoryScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[language]
    // @Name Inventory Actions
    // @Group Useful Lists
    // @Description
    // Used by some inventory world events to describe the action of the inventory event.
    //
    // Actions, as described by the bukkit javadocs:
    // CLONE_STACK
    // A max-size stack of the clicked item is put on the cursor.
    // COLLECT_TO_CURSOR
    // The inventory is searched for the same material, and they are put on the cursor up to
    //      MaterialTag.max_stack_size.
    // DROP_ALL_CURSOR
    // The entire cursor item is dropped.
    // DROP_ALL_SLOT
    // The entire clicked slot is dropped.
    // DROP_ONE_CURSOR
    // One item is dropped from the cursor.
    // DROP_ONE_SLOT
    // One item is dropped from the clicked slot.
    // HOTBAR_MOVE_AND_READD
    // The clicked item is moved to the hotbar, and the item currently there is re-added to the
    //      player's inventory.
    // HOTBAR_SWAP
    // The clicked slot and the picked hotbar slot are swapped.
    // MOVE_TO_OTHER_INVENTORY
    // The item is moved to the opposite inventory if a space is found.
    // NOTHING
    // Nothing will happen from the click.
    // PICKUP_ALL
    // All of the items on the clicked slot are moved to the cursor.
    // PICKUP_HALF
    // Half of the items on the clicked slot are moved to the cursor.
    // PICKUP_ONE
    // One of the items on the clicked slot are moved to the cursor.
    // PICKUP_SOME
    // Some of the items on the clicked slot are moved to the cursor.
    // PLACE_ALL
    // All of the items on the cursor are moved to the clicked slot.
    // PLACE_ONE
    // A single item from the cursor is moved to the clicked slot.
    // PLACE_SOME
    // Some of the items from the cursor are moved to the clicked slot (usually up to the max stack size).
    // SWAP_WITH_CURSOR
    // The clicked item and the cursor are exchanged.
    // UNKNOWN
    // An unrecognized ClickType.
    //
    // -->

    // <--[event]
    // @Events
    // player (<'click_type'>) clicks (<item>) in <inventory>
    //
    // @Group Player
    //
    // @Switch with:<item> to only process the event if a specified cursor item was used.
    // @Switch in_area:<area> replaces the default 'in:<area>' for this event.
    // @Switch action:<action> to only process the event if a specified action occurred.
    // @Switch slot:<slot> to only process the event if a specified slot or slot_type was clicked. For slot input options, see <@link language Slot Inputs>.
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when a player clicks in an inventory. Note that you likely will also want to listen to <@link event player drags in inventory>.
    //
    // @Context
    // <context.item> returns the ItemTag the player has clicked on.
    // <context.inventory> returns the InventoryTag (the 'top' inventory, regardless of which slot was clicked).
    // <context.clicked_inventory> returns the InventoryTag that was clicked in.
    // <context.cursor_item> returns the item the Player is clicking with.
    // <context.click> returns an ElementTag with the name of the click type. Click type list: <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/inventory/ClickType.html>
    // <context.slot_type> returns an ElementTag with the name of the slot type that was clicked. Slot type list: <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/inventory/InventoryType.SlotType.html>
    // <context.slot> returns an ElementTag with the number of the slot that was clicked.
    // <context.raw_slot> returns an ElementTag with the raw number of the slot that was clicked.
    // <context.is_shift_click> returns true if 'shift' was used while clicking.
    // <context.action> returns the inventory_action. See <@link language Inventory Actions>.
    // <context.hotbar_button> returns an ElementTag of the button pressed as a number, or 0 if no number button was pressed.
    //
    // @Determine
    // ItemTag to set the current item for the event.
    //
    // @Player Always.
    //
    // -->

    public PlayerClicksInInventoryScriptEvent() {
        registerCouldMatcher("player (<'click_type'>) clicks (<item>) in <inventory>");
        registerSwitches("with", "in_area", "action", "slot");
    }


    public InventoryTag inventory;
    public ItemTag item;
    public ItemTag cursor; // Needed due to internal oddity
    public InventoryClickEvent event;

    private static final HashSet<String> matchHelpList = new HashSet<>(Arrays.asList("at", "entity", "npc", "player", "vehicle", "projectile", "hanging", "fake"));

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!super.couldMatch(path)) {
            return false;
        }
        boolean clickFirst = path.eventArgLowerAt(1).equals("clicks");
        if (!clickFirst && !path.eventArgLowerAt(2).equals("clicks")) {
            return false;
        }
        String clickedOn = path.eventArgLowerAt(clickFirst ? 2 : 3);
        if (matchHelpList.contains(clickedOn)) {
            return false;
        }
        int inIndex = -1;
        for (int i = 0; i < path.eventArgsLower.length; i++) {
            if (path.eventArgLowerAt(i).equals("in")) {
                inIndex = i;
            }
        }
        if (inIndex == -1) {
            return false;
        }
        return true;
    }

    @Override
    public boolean matches(ScriptPath path) {
        boolean hasClickType = path.eventArgLowerAt(2).equals("clicks");
        if (hasClickType && !runGenericCheck(path.eventArgLowerAt(1), event.getClick().name())) {
            return false;
        }
        String clickedItemText = path.eventArgLowerAt(hasClickType ? 3 : 2);
        if (!clickedItemText.equals("in") && !item.tryAdvancedMatcher(clickedItemText)) {
            return false;
        }
        int inIndex = -1;
        for (int i = 0; i < path.eventArgsLower.length; i++) {
            if (path.eventArgLowerAt(i).equals("in")) {
                inIndex = i;
            }
        }
        if (!inventory.tryAdvancedMatcher(path.eventArgLowerAt(inIndex + 1))) {
            return false;
        }
        if (!runWithCheck(path, cursor)) {
            return false;
        }
        if (!nonSwitchWithCheck(path, cursor)) {
            return false;
        }
        if (!runInCheck(path, event.getWhoClicked().getLocation(), "in_area")) {
            return false;
        }
        if (!runGenericSwitchCheck(path, "action", event.getAction().name())) {
            return false;
        }
        if (!trySlot(path, "slot", event.getWhoClicked(), event.getSlot()) && !runGenericSwitchCheck(path, "slot", event.getSlotType().name())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        if (determinationObj.canBeType(ItemTag.class)) {
            event.setCurrentItem(determinationObj.asType(ItemTag.class, getTagContext(path)).getItemStack());
            return true;
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(event.getWhoClicked());
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("inventory")) {
            return inventory;
        }
        else if (name.equals("item")) {
            return item;
        }
        else if (name.equals("cursor_item")) {
            return cursor;
        }
        else if (name.equals("click")) {
            return new ElementTag(event.getClick());
        }
        else if (name.equals("action")) {
            return new ElementTag(event.getAction());
        }
        else if (name.equals("slot_type")) {
            return new ElementTag(event.getSlotType());
        }
        else if (name.equals("is_shift_click")) {
            return new ElementTag(event.isShiftClick());
        }
        else if (name.equals("clicked_inventory") && event.getClickedInventory() != null) {
            return InventoryTag.mirrorBukkitInventory(event.getClickedInventory());
        }
        else if (name.equals("slot")) {
            return new ElementTag(event.getSlot() + 1);
        }
        else if (name.equals("raw_slot")) {
            return new ElementTag(event.getRawSlot() + 1);
        }
        else if (name.equals("hotbar_button")) {
            return new ElementTag(event.getHotbarButton() + 1);
        }
        return super.getContext(name);
    }

    @EventHandler
    public void inventoryClickEvent(InventoryClickEvent event) {
        inventory = InventoryTag.mirrorBukkitInventory(event.getInventory());
        item = event.getCurrentItem() == null ? new ItemTag(Material.AIR) : new ItemTag(event.getCurrentItem().clone());
        cursor = new ItemTag(event.getCursor() == null ? new ItemStack(Material.AIR) : event.getCursor().clone());
        this.event = event;
        fire(event);
    }
}
