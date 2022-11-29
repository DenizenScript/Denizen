package com.denizenscript.denizen.events.world;

import com.denizenscript.denizen.objects.WorldTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.WeatherChangeEvent;

public class WeatherChangesScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // weather changes|rains|clears (in <world>)
    //
    // @Group World
    //
    // @Cancellable true
    //
    // @Triggers when weather changes in a world.
    //
    // @Context
    // <context.world> returns the WorldTag the weather changed in.
    // <context.weather> returns an ElementTag with the name of the new weather (rains or clears).
    //
    // -->

    public WeatherChangesScriptEvent() {
        registerCouldMatcher("weather changes|rains|clears (in <world>)");
    }

    public WorldTag world;
    public ElementTag weather;
    public WeatherChangeEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        String cmd = path.eventArgLowerAt(1);
        if (!cmd.equals("changes") && !cmd.equals(weather.asString())) {
            return false;
        }
        if (path.eventArgLowerAt(2).equals("in") && !path.tryArgObject(3, world)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("world")) {
            return world;
        }
        else if (name.equals("weather")) {
            return weather;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onWeatherChanges(WeatherChangeEvent event) {
        world = new WorldTag(event.getWorld());
        weather = new ElementTag(event.toWeatherState() ? "rains" : "clears");
        this.event = event;
        fire(event);
    }
}
