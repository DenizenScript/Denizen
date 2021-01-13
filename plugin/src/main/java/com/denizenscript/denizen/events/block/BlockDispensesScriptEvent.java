package com.denizenscript.denizen.events.block;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.utilities.debugging.Debug;
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
    // block dispenses item
    // block dispenses <item>
    // <block> dispenses item
    // <block> dispenses <item>
    //
    // @Regex ^on [^\s]+ dispenses [^\s]+$
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
    }

    public static BlockDispensesScriptEvent instance;
    public LocationTag location;
    public ItemTag item;
    private MaterialTag material;
    public BlockDispenseEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!path.eventArgLowerAt(1).equals("dispenses")) {
            return false;
        }
        if (!couldMatchBlock(path.eventArgLowerAt(0))) {
            return false;
        }
        if (!couldMatchItem(path.eventArgLowerAt(2))) {
            return false;
        }
        return true;
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, location)) {
            return false;
        }

        String iTest = path.eventArgLowerAt(2);
        if  (!iTest.equals("item") && !tryItem(item, iTest)) {
            return false;
        }
        if (!tryMaterial(material, path.eventArgLowerAt(0))) {
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
        String determination = determinationObj.toString();
        if (ArgumentHelper.matchesDouble(determination)) {
            Deprecations.blockDispensesItemDetermination.warn();
            event.setVelocity(event.getVelocity().multiply(Double.parseDouble(determination)));
            return true;
        }
        else if (LocationTag.matches(determination)) {
            LocationTag vel = LocationTag.valueOf(determination, getTagContext(path));
            if (vel == null) {
                Debug.echoError("[" + getName() + "] Invalid velocity '" + determination + "'!");
            }
            else {
                event.setVelocity(vel.toVector());
            }
            return true;
        }
        else if (ItemTag.matches(determination)) {
            ItemTag it = ItemTag.valueOf(determination, path.container);
            if (it == null) {
                Debug.echoError("[" + getName() + "] Invalid item '" + determination + "'!");
            }
            else {
                item = it;
                event.setItem(item.getItemStack());
            }
            return true;
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("location")) {
            return location;
        }
        else if (name.equals("item")) {
            return item;
        }
        else if (name.equals("velocity")) {
            return new LocationTag(event.getVelocity());
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
