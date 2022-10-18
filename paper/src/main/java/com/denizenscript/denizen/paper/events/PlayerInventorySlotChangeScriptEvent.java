package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerInventorySlotChangeScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player inventory slot changes
    //
    // @Group Paper
    //
    // @Location true
    //
    // @Plugin Paper
    //
    // @Switch from:<item> to only process the event if the previous item in the slot matches the specified item.
    // @Switch to:<item> to only process the event if the new item in the slot matches the specified item.
    // @Switch slot:<slot> to only process the event if a specific slot was clicked. For slot input options, see <@link language Slot Inputs>.
    //
    // @Triggers when the item in a slot of a player's inventory changes.
    // Note that this fires for every item in the player's inventory when they join.
    //
    // @Context
    // <context.new_item> returns an ItemTag of the new item in the slot.
    // <context.old_item> returns an ItemTag of the previous item in the slot.
    // <context.slot> returns an ElementTag(Number) of the slot that was changed.
    // <context.raw_slot> returns an ElementTag(Number) of the raw number of the slot that was changed.
    //
    // @Player Always.
    //
    // -->

    public PlayerInventorySlotChangeScriptEvent() {
        registerCouldMatcher("player inventory slot changes");
        registerSwitches("from", "to", "slot");
    }

    public PlayerInventorySlotChangeEvent event;
    public ItemTag oldItem;
    public ItemTag newItem;

    @Override
    public boolean matches(ScriptPath path) {
        if (!trySlot(path, "slot", event.getPlayer(), event.getSlot())) {
            return false;
        }
        if (!runWithCheck(path, oldItem, "from")) {
            return false;
        }
        if (!runWithCheck(path, newItem, "to")) {
            return false;
        }
        if (!runInCheck(path, event.getPlayer().getLocation())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "new_item": return newItem;
            case "old_item": return oldItem;
            case "slot": return new ElementTag(event.getSlot() + 1);
            case "raw_slot": return new ElementTag(event.getRawSlot());
        }
        return super.getContext(name);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(event.getPlayer());
    }

    @EventHandler
    public void onPlayerInventorySlotChange(PlayerInventorySlotChangeEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        oldItem = new ItemTag(event.getOldItemStack());
        newItem = new ItemTag(event.getNewItemStack());
        this.event = event;
        fire(event);
    }
}
