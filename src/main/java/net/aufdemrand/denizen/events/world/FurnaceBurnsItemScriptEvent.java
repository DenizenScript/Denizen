package net.aufdemrand.denizen.events.world;

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
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceBurnEvent;

import java.util.HashMap;

public class FurnaceBurnsItemScriptEvent extends ScriptEvent implements Listener {

    // <--[event]
    // @Events
    // furnace burns item
    // furnace burns <item>
    //
    // @Cancellable true
    //
    // @Triggers when a furnace burns an item used as fuel.
    //
    // @Context
    // <context.location> returns the dLocation of the furnace.
    // <context.item> returns the dItem burnt.
    //
    // @Determine
    // Element(Number) to set the burn time for this fuel.
    //
    // -->

    public FurnaceBurnsItemScriptEvent() {
        instance = this;
    }
    public static FurnaceBurnsItemScriptEvent instance;
    public dItem item;
    public dLocation location;
    private Integer burntime;
    public FurnaceBurnEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).startsWith("furnace burns");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String itemName = CoreUtilities.getXthArg(2,s.toLowerCase());
        return itemName.equals("item") || itemName.equals(item.identifyNoIdentifier());
    }

    @Override
    public String getName() {
        return "FurnaceBurns";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        FurnaceBurnEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        if (aH.Argument.valueOf(determination)
                .matchesPrimitive(aH.PrimitiveType.Integer)) {
            burntime = aH.getIntegerFrom(determination);
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
    public void onBrews(FurnaceBurnEvent event) {
        location = new dLocation(event.getBlock().getLocation());
        item = new dItem(event.getFuel());
        burntime = event.getBurnTime();
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setBurnTime(burntime);
        event.setCancelled(cancelled);
    }
}
