package net.aufdemrand.denizen.events.block;

import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.events.ScriptEvent;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;

import java.util.HashMap;


public class BlockDispensesScriptEvent extends ScriptEvent implements Listener {

    // <--[event]
    // @Events
    // block dispenses item
    // block dispenses <item>
    // <block> dispenses item
    // <block> dispenses <item>
    //
    // @Cancellable true
    //
    // @Triggers when a block dispenses an item.
    //
    // @Context
    // <context.location> returns the dLocation of the dispenser.
    // <context.item> returns the dItem of the item being dispensed.
    //
    // @Determine
    // Element(Decimal) to set the power with which the item is shot.
    //
    // -->

    public BlockDispensesScriptEvent() {
        instance = this;
    }
    public static BlockDispensesScriptEvent instance;
    public dLocation location;
    public dItem item;
    public Vector velocity;
    public Double power;
    public BlockDispenseEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return lower.contains(" dispenses ");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String arg1 = CoreUtilities.getXthArg(0, lower);
        String arg2 = CoreUtilities.getXthArg(2, lower);
        return (arg1.equals("block") || arg1.equals(item.identifyNoIdentifier()))
                && (arg2.equals("item") || arg2.equals(item.identifyNoIdentifier()));
    }

    @Override
    public String getName() {
        return "BlockDispenses";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        BlockDispenseEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        if (aH.Argument.valueOf(determination).matchesPrimitive(aH.PrimitiveType.Double)) {
            power = aH.getDoubleFrom(determination);
            return true;
        }
        return super.applyDetermination(container, determination);
    }

    @Override
    public HashMap<String, dObject> getContext() {
        HashMap<String, dObject> context = super.getContext();
        context.put("location", location);
        context.put("item", item);
        return context;
    }

    @EventHandler
    public void onBlockDispenses(BlockDispenseEvent event) {
        location = new dLocation(event.getBlock().getLocation());
        item = new dItem(event.getItem());
        velocity = event.getVelocity();
        cancelled = event.isCancelled();
        power = null;
        this.event = event;
        fire();
        if (power != null) {
            event.setVelocity(velocity.multiply(power));
        }
        event.setCancelled(cancelled);
    }
}
