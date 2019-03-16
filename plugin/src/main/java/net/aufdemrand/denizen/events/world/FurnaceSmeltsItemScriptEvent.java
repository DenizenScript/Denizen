package net.aufdemrand.denizen.events.world;

import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceSmeltEvent;

public class FurnaceSmeltsItemScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // furnace smelts item (into <item>)
    // furnace smelts <item> (into <item>)
    //
    // @Cancellable true
    //
    // @Regex ^on furnace smelts [^\s]+( into [^\s]+)?$
    // @Switch in <area>
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
    public boolean matches(ScriptPath path) {
        String srcItem = path.eventArgLowerAt(2);
        if (!tryItem(source_item, srcItem)) {
            return false;
        }

        if (path.eventArgLowerAt(3).equals("into")) {
            String resItem = path.eventArgLowerAt(4);
            if (!tryItem(result_item, resItem)) {
                return false;
            }
        }
        return runInCheck(path, location);
    }

    @Override
    public String getName() {
        return "FurnaceSmelts";
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        if (dItem.matches(determination)) {
            result_item = dItem.valueOf(determination, container);
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

    @EventHandler
    public void onFurnaceSmelts(FurnaceSmeltEvent event) {
        location = new dLocation(event.getBlock().getLocation());
        source_item = new dItem(event.getSource());
        result_item = new dItem(event.getResult());
        this.event = event;
        fire(event);
        event.setResult(result_item.getItemStack());
    }
}
