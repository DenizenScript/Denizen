package com.denizenscript.denizen.events.block;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.DurationTag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BrewingStartEvent;

public class BrewingStartsScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // brewing starts
    //
    // @Group Block
    //
    // @Location true
    //
    // @Triggers when a brewing stand starts brewing a potion.
    //
    // @Context
    // <context.item> returns an ItemTag of the used ingredient to brew potions.
    // <context.location> returns a LocationTag of the brewing stand's location.
    // <context.brew_time> returns a DurationTag of the total time it will take to brew the potion.
    //
    // @Determine
    // "BREW_TIME:DurationTag" to set the total time for the potion being brewed.
    //
    // -->

    public BrewingStartsScriptEvent() {
        registerCouldMatcher("brewing starts");
        this.<BrewingStartsScriptEvent, DurationTag>registerDetermination("brew_time", DurationTag.class, (evt, context, time) -> {
            evt.event.setTotalBrewTime(time.getTicksAsInt());
        });
    }

    public BrewingStartEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, event.getBlock().getLocation())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "item" -> new ItemTag(event.getSource());
            case "location" -> new LocationTag(event.getBlock().getLocation());
            case "brew_time" -> new DurationTag((long) event.getTotalBrewTime());
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void onBrewingStart(BrewingStartEvent event) {
        this.event = event;
        fire(event);
    }
}
