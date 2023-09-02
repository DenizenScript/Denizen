package com.denizenscript.denizen.events.block;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.DurationTag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BrewingStartEvent;

public class BrewingStartsScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // brewing stand starts brew
    //
    // @Group Block
    //
    // @Location true
    //
    // @Triggers when a brewing stand starts brew a potion.
    //
    // @Context
    // <context.location> returns a LocationTag of the brewing stand's location.
    // <context.brew_time> returns a DurationTag of the total time it will take to brew the potion.
    //
    // @Determine to set the total time for the potion being brewed.
    //
    // -->

    public BrewingStartsScriptEvent(){
        registerCouldMatcher("brewing starts");
    }

    public LocationTag location;
    public BrewingStartEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, location)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        if (determinationObj instanceof DurationTag duration) {
            event.setTotalBrewTime(duration.getTicksAsInt());
            return true;
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "location" -> location;
            case "brew_time" -> new DurationTag((long) event.getTotalBrewTime());
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void onBrewingStart(BrewingStartEvent event) {
        location = new LocationTag(event.getBlock().getLocation());
        this.event = event;
        fire(event);
    }
}
