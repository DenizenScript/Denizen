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
    // @Cancellable true
    //
    // @Triggers when a furnace starts smelting an item.
    //
    // @Context
    // <context.location> returns the LocationTag of the furnace.
    // <context.item> returns the ItemTag of the item being smelted.
    // <context.recipe_id> returns the recipe ID of the item being smelted.
    // <context.cook_duration> returns a DurationTag of the time it will take to smelt the item.
    // <context.experience> returns the experience that will be given when the item is smelted.
    //
    // @Determine
    // COOK_DURATION: + DurationTag to set the total time it will take to smelt the item. (Default: 200 ticks)
    // EXPERIENCE: + ElementTag to change the experience that will be given when the item is smelted.
    //
    //
    // @Example
    // # Sets the total cook time for the item being smelted to 30 seconds.
    // on furnace starts smelting item:
    // - determine COOK_DURATION:30s
    //
    // @Example
    // # Removes the experience awarded for smelting iron_ore.
    // on furnace starts smelting iron_ore:
    // - determine EXPERIENCE:0
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
        if (determinationObj.canBeType(ElementTag.class)) {
            String determination = determinationObj.toString();
            String lower = CoreUtilities.toLowerCase(determination);
            if (lower.startsWith("cook_duration:")) {
                String duration = determination.substring("cook_duration:".length());
                if (duration.canBeType(DurationTag.class)) {
                    event.setCookingTime(duration.asType(DurationTag.class, getTagContext(path)).getTicksAsInt());
                    return true;
                }
                else {
                    Debug.echoError("Invalid value '" + duration + "' provided for cook duration. Must be a valid DurationTag input. See <@link tag DurationTag>");
                }
            }
            else if (lower.startsWith("experience:")) {
                String experience = determination.substring("experience:".length());
                if (experience.isInt() || experience.isDouble()) {
                    event.getRecipe().setExperience(experience.asFloat());
                    return true;
                }
                else {
                    Debug.echoError("Invalid value '" + experience + "' provided for experience. Value must be an Integer or Double.");
                }
            }
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "location": return location;
            case "item": return item;
            case "cook_duration": return new Durationtag(event.getCookingTime());
            case "experience": return new ElementTag(event.getRecipe().getExperience());
            case "recipe_id": return new ElementTag(((Keyed) event.getRecipe()).getKey().toString());
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
