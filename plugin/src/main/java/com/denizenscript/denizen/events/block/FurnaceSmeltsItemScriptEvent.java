package com.denizenscript.denizen.events.block;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
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
    //
    // @Group Block
    //
    // @Switch in <area>
    //
    // @Triggers when a furnace smelts an item.
    //
    // @Context
    // <context.location> returns the LocationTag of the furnace.
    // <context.source_item> returns the ItemTag that is being smelted.
    // <context.result_item> returns the ItemTag that is the result of the smelting.
    //
    // @Determine
    // ItemTag to set the item that is the result of the smelting.
    //
    // -->

    public FurnaceSmeltsItemScriptEvent() {
        instance = this;
    }

    public static FurnaceSmeltsItemScriptEvent instance;
    public ItemTag source_item;
    public ItemTag result_item;
    public LocationTag location;
    public FurnaceSmeltEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        return path.eventLower.startsWith("furnace smelts");
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!tryItem(source_item, path.eventArgLowerAt(2))) {
            return false;
        }

        if (path.eventArgLowerAt(3).equals("into")) {
            if (!tryItem(result_item, path.eventArgLowerAt(4))) {
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
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        String determination = determinationObj.toString();
        if (ItemTag.matches(determination)) {
            result_item = ItemTag.valueOf(determination, path.container);
            event.setResult(result_item.getItemStack());
            return true;
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ObjectTag getContext(String name) {
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
        location = new LocationTag(event.getBlock().getLocation());
        source_item = new ItemTag(event.getSource());
        result_item = new ItemTag(event.getResult());
        this.event = event;
        fire(event);
    }
}
