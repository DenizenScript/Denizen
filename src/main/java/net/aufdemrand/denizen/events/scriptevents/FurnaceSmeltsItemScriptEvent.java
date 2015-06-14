package net.aufdemrand.denizen.events.scriptevents;

import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.events.ScriptEvent;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceSmeltEvent;

import java.util.HashMap;

public class FurnaceSmeltsItemScriptEvent extends ScriptEvent implements Listener {

    // <--[event]
    // @Events
    // furnace smelts item (into <item>)
    // furnace smelts <item> (into <item>)
    //
    // @Cancellable true
    //
    // @Triggers when a furnace smelts an item.
    //
    // @Context
    // <context.location> returns the dLocation of the furnace.
    // <context.source_item> returns the dItem that is being smelted.
    // <context.result_item> returns the dItem that is the result of the smelting.
    //
    // @Determine
    // dItem to set the item that is the result of the smelting.
    //
    // -->

    public FurnaceSmeltsItemScriptEvent() {
        instance = this;
    }
    public static FurnaceSmeltsItemScriptEvent instance;
    public dItem source_item;
    public dItem result_item;
    public dLocation location;
    public FurnaceSmeltEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).startsWith("furnace smelts");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String srcItem = CoreUtilities.getXthArg(2,lower);
        if (!srcItem.equals("item") && (!dItem.matches(srcItem) && !srcItem.equals(source_item.identifyNoIdentifier()))) {
                return false;
        }

        String resItem = CoreUtilities.getXthArg(4,lower);
        if (!resItem.equals("item") && (!dItem.matches(resItem)) && !resItem.equals(result_item.identifyNoIdentifier())) {
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return "FurnaceSmelts";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        FurnaceSmeltEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        if (dItem.matches(determination)) {
            result_item = dItem.valueOf(determination);
            return true;
        }
        return super.applyDetermination(container, determination);
    }

    @Override
    public HashMap<String, dObject> getContext() {
        HashMap<String, dObject> context = super.getContext();
        context.put("location", location);
        context.put("source_item", source_item);
        context.put("result_item", result_item);
        return context;
    }

    @EventHandler
    public void onFurnaceSmelts(FurnaceSmeltEvent event) {
        location = new dLocation(event.getBlock().getLocation());
        source_item = new dItem(event.getSource());
        result_item = new dItem(event.getResult());
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setResult(result_item.getItemStack());
        event.setCancelled(cancelled);
    }
}
