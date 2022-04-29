package com.denizenscript.denizen.events.block;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.utilities.Deprecations;
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
    // ItemTag to set the item being shot.
    //
    // -->

    public BlockDispensesScriptEvent() {
        instance = this;
        registerCouldMatcher("<block> dispenses <item>");
    }

    public static BlockDispensesScriptEvent instance;
    public LocationTag location;
    public ItemTag item;
    private MaterialTag material;
    public BlockDispenseEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, location)) {
            return false;
        }
        if  (!item.tryAdvancedMatcher(path.eventArgLowerAt(2))) {
            return false;
        }
        if (!material.tryAdvancedMatcher(path.eventArgLowerAt(0))) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public String getName() {
        return "BlockDispenses";
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        if (determinationObj.canBeType(LocationTag.class)) {
            LocationTag vel = determinationObj.asType(LocationTag.class, getTagContext(path));
            if (vel != null) {
                event.setVelocity(vel.toVector());
                return true;
            }
        }
        if (determinationObj.canBeType(ItemTag.class)) {
            ItemTag it = determinationObj.asType(ItemTag.class, getTagContext(path));
            if (it != null) {
                item = it;
                event.setItem(item.getItemStack());
                return true;
            }
        }
        if (ArgumentHelper.matchesDouble(determinationObj.toString())) {
            Deprecations.blockDispensesItemDetermination.warn();
            event.setVelocity(event.getVelocity().multiply(Double.parseDouble(determinationObj.toString())));
            return true;
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "location": return location;
            case "item": return item;
            case "velocity": return new LocationTag(event.getVelocity());
        }
        return super.getContext(name);
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
