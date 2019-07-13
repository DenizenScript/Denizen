package com.denizenscript.denizen.events.block;

import com.denizenscript.denizen.objects.dItem;
import com.denizenscript.denizen.objects.dLocation;
import com.denizenscript.denizen.objects.dMaterial;
import com.denizenscript.denizen.utilities.debugging.dB;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.dObject;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
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
    // @Regex ^on [^\s]+ dispense [^\s]+ $
    // @Switch in <area>
    //
    // @Cancellable true
    //
    // @Triggers when a block dispenses an item.
    //
    // @Context
    // <context.location> returns the dLocation of the dispenser.
    // <context.item> returns the dItem of the item being dispensed.
    // <context.velocity> returns a dLocation vector of the velocity the item will be shot at.
    //
    // @Determine
    // Element(Decimal) (DEPRECATED) to multiply the velocity by the given amount.
    // dLocation to set the velocity the item will be shot at.
    // dItem to set the item being shot.
    //
    // -->

    public BlockDispensesScriptEvent() {
        instance = this;
    }

    public static BlockDispensesScriptEvent instance;
    public dLocation location;
    public dItem item;
    private dLocation velocity;
    private dMaterial material;
    public BlockDispenseEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String cmd = CoreUtilities.getXthArg(1, lower);
        return cmd.equals("dispenses") && lower.length() >= 3;
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
            velocity = new dLocation(velocity.multiply(ArgumentHelper.getDoubleFrom(determination)));
            return true;
        }
        else if (dLocation.matches(determination)) {
            dLocation vel = dLocation.valueOf(determination);
            if (vel == null) {
                dB.echoError("[" + getName() + "] Invalid velocity!");
            }
            else {
                velocity = vel;
            }
        }
        else if (dItem.matches(determination)) {
            dItem it = dItem.valueOf(determination, container);
            if (it == null) {
                dB.echoError("[" + getName() + "] Invalid item!");
            }
            else {
                item = it;
            }
        }
        return super.applyDetermination(container, determination);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("location")) {
            return location;
        }
        else if (name.equals("item")) {
            return item;
        }
        else if (name.equals("velocity")) {
            return velocity;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onBlockDispenses(BlockDispenseEvent event) {
        location = new dLocation(event.getBlock().getLocation());
        material = new dMaterial(event.getBlock());
        item = new dItem(event.getItem());
        velocity = new dLocation(null, event.getVelocity().getX(), event.getVelocity().getY(), event.getVelocity().getZ());
        this.event = event;
        fire(event);
        event.setVelocity(velocity.toVector());
        event.setItem(item.getItemStack());
    }
}
