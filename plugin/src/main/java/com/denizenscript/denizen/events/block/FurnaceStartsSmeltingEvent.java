package com.denizenscript.denizen.events.block;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.Keyed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceStartSmeltEvent;

public class FurnaceStartsSmeltingEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // furnace starts smelting <item>
    //
    // @Group Block
    //
    // @Location true
    //
    // @Triggers when a furnace starts smelting an item.
    //
    // @Context
    // <context.location> returns the LocationTag of the furnace.
    // <context.item> returns the ItemTag of the item being smelted.
    // <context.recipe_id> returns the ElementTag of the recipe ID being used.
    // <context.total_cook_time> returns the DurationTag of the total time it will take to smelt the item.
    //
    // @Determine
    // DurationTag to set the total cook time for the item being smelted.
    //
    // @Example
    // # Sets the total cook time of the item to be 2 seconds.
    // on furnace starts smelting item:
    // - determine 2s
    //
    // @Example
    // # Sets the total cook time of iron ore to be 2 seconds.
    // on furnace starts smelting iron_ore:
    // - determine 2s
    // -->

    public FurnaceStartsSmeltingEvent() {
        registerCouldMatcher("furnace starts smelting <item>");
    }

    public ItemTag item;
    public LocationTag location;
    public FurnaceStartSmeltEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, location)) {
            return false;
        }
        if (!item.tryAdvancedMatcher(path.eventArgLowerAt(3))) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        if (determinationObj.canBeType(DurationTag.class)) {
            event.setTotalCookTime(determinationObj.asType(DurationTag.class, getTagContext(path)).getTicksAsInt());
            return true;
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "location": return location;
            case "item": return item;
            case "recipe_id": return new ElementTag(((Keyed) event.getRecipe()).getKey().toString());
            case "total_cook_time": return new DurationTag(event.getTotalCookTime());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onFurnaceStartsSmelting(FurnaceStartSmeltEvent event) {
        location = new LocationTag(event.getBlock().getLocation());
        item = new ItemTag(event.getSource());
        this.event = event;
        fire(event);
    }
}
