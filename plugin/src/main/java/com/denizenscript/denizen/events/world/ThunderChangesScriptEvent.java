package com.denizenscript.denizen.events.world;

import com.denizenscript.denizen.objects.WorldTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.ThunderChangeEvent;

public class ThunderChangesScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // thunder changes|begins|clears
    //
    // @Switch in:<world> to only run the event if it applies to a specific world.
    //
    // @Group World
    //
    // @Cancellable true
    //
    // @Triggers when thunder starts or stops in a world.
    //
    // @Context
    // <context.world> returns the WorldTag the thunder changed in.
    // <context.thunder> returns true if thunder is starting, or false if thunder is stopping.
    //
    // -->

    public ThunderChangesScriptEvent() {
        registerCouldMatcher("thunder changes|begins|clears");
        registerSwitches("in");
    }

    public ThunderChangeEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        String changeType = path.eventArgLowerAt(1);
        if (changeType.equals("clears")) {
            if (event.toThunderState()) {
                return false;
            }
        }
        else if (changeType.equals("begins")) {
            if (!event.toThunderState()) {
                return false;
            }
        }
        else if (!changeType.equals("changes")) {
            return false;
        }
        if (!path.tryObjectSwitch("in", new WorldTag(event.getWorld()))) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch(name) {
            case "world":
                return new WorldTag(event.getWorld());
            case "thunder":
                return new ElementTag(event.toThunderState());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onThunderChanges(ThunderChangeEvent event) {
        this.event = event;
        fire(event);
    }
}
