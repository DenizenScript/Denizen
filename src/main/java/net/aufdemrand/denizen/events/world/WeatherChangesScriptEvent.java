package net.aufdemrand.denizen.events.world;


import net.aufdemrand.denizen.objects.dWorld;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.events.ScriptEvent;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.WeatherChangeEvent;

public class WeatherChangesScriptEvent extends ScriptEvent implements Listener {

    // <--[event]
    // @Events
    // weather changes/rains/clears (in <world>)
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
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String cmd = CoreUtilities.getXthArg(1, lower);
        if (!cmd.equals("changes") && !cmd.equals(weather.identifySimple())) {
            return false;
        }
        String wCheck = CoreUtilities.getXthArg(3, lower);
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
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        WeatherChangeEvent.getHandlerList().unregister(this);
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

    @EventHandler(ignoreCancelled = true)
    public void onWeatherChanges(WeatherChangeEvent event) {
        world = new dWorld(event.getWorld());
        weather = new Element(event.toWeatherState() ? "rains" : "clears");
        this.event = event;
        cancelled = event.isCancelled();
        fire();
        event.setCancelled(cancelled);
    }
}
