package net.aufdemrand.denizen.events.world;

import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dWorld;
import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.dObject;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.WeatherChangeEvent;

public class WeatherChangesScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // weather changes/rains/clears (in <world>)
    //
    // @Regex ^on weather (changes|rains|clears)( in [^\s]+)?$
    //
    // @Cancellable true
    //
    // @Triggers when weather changes in a world.
    //
    // @Context
    // <context.world> returns the dWorld the weather changed in.
    // <context.weather> returns an Element with the name of the new weather. (rain or clear).
    //
    // -->

    public WeatherChangesScriptEvent() {
        instance = this;
    }

    public static WeatherChangesScriptEvent instance;
    public dWorld world;
    public Element weather;
    public WeatherChangeEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.getXthArg(0, CoreUtilities.toLowerCase(s)).equals("weather");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String cmd = path.eventArgLowerAt(1);
        if (!cmd.equals("changes") && !cmd.equals(weather.identifySimple())) {
            return false;
        }
        String wCheck = path.eventArgLowerAt(3);
        if (wCheck.length() > 0 && !wCheck.equals(CoreUtilities.toLowerCase(world.getName()))) {
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return "WeatherChanges";
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public dObject getContext(String name) {
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
        world = new dWorld(event.getWorld());
        weather = new Element(event.toWeatherState() ? "rains" : "clears");
        this.event = event;
        fire(event);
    }
}
