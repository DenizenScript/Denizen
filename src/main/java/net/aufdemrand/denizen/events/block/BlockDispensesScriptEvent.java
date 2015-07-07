package net.aufdemrand.denizen.events.block;

import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dMaterial;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;

import java.util.HashMap;


public class BlockDispensesScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // block dispenses item (in <area>)
    // block dispenses <item> (in <area>)
    // <block> dispenses item (in <area>)
    // <block> dispenses <item> (in <area>)
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
    private Double power;
    private dMaterial material;
    public BlockDispenseEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String cmd = CoreUtilities.getXthArg(1, lower);
        return cmd.equals("dispenses") && lower.length() >= 3;
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        if (!runInCheck(scriptContainer, s, lower, location)) {
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
        material = dMaterial.getMaterialFrom(event.getBlock().getType(), event.getBlock().getData());
        item = new dItem(event.getItem());
        cancelled = event.isCancelled();
        power = null;
        this.event = event;
        fire();
        if (power != null) {
            event.setVelocity(event.getVelocity().multiply(power));
        }
        event.setCancelled(cancelled);
    }
}
