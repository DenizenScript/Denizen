package com.denizenscript.denizen.events.block;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BrewingStartEvent;

public class BrewingStartScriptEvent extends BukkitScriptEvent implements Listener {

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
    // <context.location> returns the LocationTag of the brewing stand.
    // <context.brew_time> returns an ElementTag(Number) of the brewing time.
    //
    // @Determine
    // "BREW_TIME:<ElementTag(Number)>" to set the fuel power level to be added.
    //
    // -->

    public BrewingStartScriptEvent(){
        registerCouldMatcher("brewing stand starts brew");
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
        if (determinationObj instanceof ElementTag element) {
            String val = element.asString();
            if (val.startsWith("brew_time:")) {
                event.setTotalBrewTime(Integer.parseInt(val.substring("brew_time:".length())));
                return true;
            }
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "location" -> location;
            case "brew_time" -> new ElementTag(event.getTotalBrewTime());
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
