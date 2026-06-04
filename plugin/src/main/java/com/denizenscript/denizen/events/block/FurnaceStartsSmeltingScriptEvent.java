package com.denizenscript.denizen.events.block;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceStartSmeltEvent;

public class FurnaceStartsSmeltingScriptEvent extends BukkitScriptEvent implements Listener {

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
    // <context.location> returns a LocationTag of the furnace's location.
    // <context.item> returns an ItemTag of the item being smelted.
    // <context.recipe_id> returns an ElementTag of the recipe ID being used.
    // <context.total_cook_time> returns a DurationTag of the total time it will take to smelt the item.
    //
    // @Determine
    // DurationTag to set the total cook time for the item being smelted.
    //
    // -->

    public FurnaceStartsSmeltingScriptEvent() {
        registerCouldMatcher("furnace starts smelting <item>");
        this.<FurnaceStartsSmeltingScriptEvent, DurationTag>registerDetermination(null, DurationTag.class, (evt, context, time) -> {
            evt.event.setTotalCookTime(time.getTicksAsInt());
        });
    }

    public ItemTag item;
    public LocationTag location;
    public FurnaceStartSmeltEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, location)) {
            return false;
        }
        if (!path.tryArgObject(3, item)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "location" -> location;
            case "item" -> item;
            case "recipe_id" -> new ElementTag(event.getRecipe().getKey().toString(), true);
            case "total_cook_time" -> new DurationTag((long) event.getTotalCookTime());
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void onFurnaceStartsSmelting(FurnaceStartSmeltEvent event) {
        location = new LocationTag(event.getBlock().getLocation());
        item = new ItemTag(event.getSource());
        this.event = event;
        fire(event);
    }
}
