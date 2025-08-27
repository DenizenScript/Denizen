package com.denizenscript.denizen.events.block;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import com.denizenscript.denizencore.objects.ObjectTag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;

public class BlockDispensesScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // <block> dispenses <item>
    //
    // @Group Block
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when a block dispenses an item.
    //
    // @Context
    // <context.location> returns the LocationTag of the dispenser.
    // <context.item> returns the ItemTag of the item being dispensed.
    // <context.velocity> returns a LocationTag vector of the velocity the item will be shot at.
    //
    // @Determine
    // LocationTag to set the velocity the item will be shot at.
    // "ITEM:<ItemTag>" to set the item being shot.
    //
    // -->

    public BlockDispensesScriptEvent() {
        registerCouldMatcher("<block> dispenses <item>");
        this.<BlockDispensesScriptEvent, ObjectTag>registerOptionalDetermination(null, ObjectTag.class, (evt, context, value) -> {
            if (value.canBeType(LocationTag.class)) {
                LocationTag location = value.asType(LocationTag.class, context);
                if (location != null) {
                    evt.event.setVelocity(location.toVector());
                    return true;
                }
            }
            else if (value.canBeType(ItemTag.class)) {
                BukkitImplDeprecations.blockDispensesItemDetermination.warn();
                ItemTag item = value.asType(ItemTag.class, context);
                if (item != null) {
                    evt.item = item;
                    evt.event.setItem(item.getItemStack());
                    return true;
                }
            }
            return false;
        });
        this.<BlockDispensesScriptEvent, ItemTag>registerDetermination("item", ItemTag.class, (evt, context, value) -> {
            evt.item = value;
            evt.event.setItem(value.getItemStack());
        });
    }

    public LocationTag location;
    public ItemTag item;
    private MaterialTag material;
    public BlockDispenseEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, location)) {
            return false;
        }
        if  (!path.tryArgObject(2, item)) {
            return false;
        }
        if (!path.tryArgObject(0, material)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "location" -> location;
            case "item" -> new ItemTag(event.getItem());
            case "velocity" -> new LocationTag(event.getVelocity());
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void onBlockDispenses(BlockDispenseEvent event) {
        location = new LocationTag(event.getBlock().getLocation());
        material = new MaterialTag(event.getBlock());
        item = new ItemTag(event.getItem());
        this.event = event;
        fire(event);
    }
}
