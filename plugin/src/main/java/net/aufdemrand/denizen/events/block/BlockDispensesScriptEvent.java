package net.aufdemrand.denizen.events.block;

import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dMaterial;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;


public class BlockDispensesScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // block dispenses item (in <area>)
    // block dispenses <item> (in <area>)
    // <block> dispenses item (in <area>)
    // <block> dispenses <item> (in <area>)
    //
    // @Regex ^on [^\s]+ dispense [^\s]+ ( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
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
        String lower = path.eventLower;
        if (!runInCheck(path, location)) {
            return false;
        }

        String dispenser = CoreUtilities.getXthArg(0, lower);
        String iTest = CoreUtilities.getXthArg(2, lower);
        return tryMaterial(material, dispenser) && (iTest.equals("item") || tryItem(item, iTest));
    }

    @Override
    public String getName() {
        return "BlockDispenses";
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        if (aH.matchesDouble(determination)) {
            velocity = new dLocation(velocity.multiply(aH.getDoubleFrom(determination)));
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
        cancelled = event.isCancelled();
        velocity = new dLocation(null, event.getVelocity().getX(), event.getVelocity().getY(), event.getVelocity().getZ());
        this.event = event;
        fire();
        event.setVelocity(velocity.toVector());
        event.setItem(item.getItemStack());
        event.setCancelled(cancelled);
    }
}
