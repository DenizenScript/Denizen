package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import io.papermc.paper.event.block.BlockPreDispenseEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class BlockPreDispenseScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // <block> tries to dispense <item>
    //
    // @Group Block
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers before a block dispenses an item.
    // This event fires before the dispenser fully processes a drop, allowing access to the dispensing slot and cancellation of sound effects.
    //
    // @Context
    // <context.location> returns the LocationTag of the dispenser.
    // <context.item> returns the ItemTag of the item about to be dispensed.
    // <context.slot> returns a ElementTag(Number) of the slot that will be dispensed from.
    //
    // -->

    public BlockPreDispenseScriptEvent() {
        registerCouldMatcher("<block> tries to dispense <item>");
    }

    public BlockPreDispenseEvent event;
    public ItemTag item;
    public LocationTag location;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, location)) {
            return false;
        }
        if (!path.tryArgObject(0, location)) {
            return false;
        }
        if (!path.tryArgObject(4, item)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "item" -> item;
            case "location" -> location;
            case "slot" -> new ElementTag(event.getSlot() + 1);
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void onBlockPreDispense(BlockPreDispenseEvent event) {
        this.event = event;
        item = new ItemTag(event.getItemStack());
        location = new LocationTag(event.getBlock().getLocation());
        fire(event);
    }
}
