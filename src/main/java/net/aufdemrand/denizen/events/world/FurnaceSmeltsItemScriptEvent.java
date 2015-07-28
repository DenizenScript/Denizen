package net.aufdemrand.denizen.events.world;

import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceSmeltEvent;

public class FurnaceSmeltsItemScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // furnace smelts item (into <item>) (in <area>)
    // furnace smelts <item> (into <item>) (in <area>)
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
        String srcItem = CoreUtilities.getXthArg(2, lower);
        if (!tryItem(source_item, srcItem)) {
            return false;
        }

        if (CoreUtilities.getXthArg(3, lower).equals("into")) {
            String resItem = CoreUtilities.getXthArg(4, lower);
            if (!tryItem(result_item, resItem)) {
                return false;
            }
        }
        return runInCheck(scriptContainer, s, lower, location);
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
    public dObject getContext(String name) {
        if (name.equals("location")) {
            return location;
        }
        else if (name.equals("source_item")) {
            return source_item;
        }
        else if (name.equals("result_item")) {
            return result_item;
        }
        return super.getContext(name);
    }

    @EventHandler(ignoreCancelled = true)
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
