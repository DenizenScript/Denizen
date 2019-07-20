package com.denizenscript.denizen.events.block;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
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
    // @Regex ^on [^\s]+ dispense [^\s]+$
    //
    // @Switch in <area>
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
    // Element(Decimal) (DEPRECATED) to multiply the velocity by the given amount.
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
        return path.eventArgLowerAt(1).equals("dispenses");
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, location)) {
            return false;
        }

        String dispenser = path.eventArgLowerAt(0);
        String iTest = path.eventArgLowerAt(2);
        return tryMaterial(material, dispenser) && (iTest.equals("item") || tryItem(item, iTest));
    }

    @Override
    public String getName() {
        return "BlockDispenses";
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        if (ArgumentHelper.matchesDouble(determination)) {
            event.setVelocity(event.getVelocity().multiply(ArgumentHelper.getDoubleFrom(determination)));
            return true;
        }
        else if (LocationTag.matches(determination)) {
            LocationTag vel = LocationTag.valueOf(determination);
            if (vel == null) {
                Debug.echoError("[" + getName() + "] Invalid velocity '" + determination + "'!");
            }
            else {
                event.setVelocity(vel.toVector());
            }
        }
        else if (ItemTag.matches(determination)) {
            ItemTag it = ItemTag.valueOf(determination, container);
            if (it == null) {
                Debug.echoError("[" + getName() + "] Invalid item '" + determination + "'!");
            }
            else {
                item = it;
                event.setItem(item.getItemStack());
            }
        }
        return super.applyDetermination(container, determination);
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
