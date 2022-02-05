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
    // thunder changes|begins|clears (in <world>)
    //
    // @Group World
    //
    // @Cancellable true
    //
    // @Triggers when thunder changes in a world.
    //
    // @Context
    // <context.world> returns the WorldTag the thunder changed in.
    // <context.thunder> returns an ElementTag(Boolean) with the new state of thunder.
    //
    // -->

    public ThunderChangesScriptEvent() {
        instance = this;
        registerCouldMatcher("thunder changes|begins|clears (in <world>)");
    }

    public static ThunderChangesScriptEvent instance;
    public WorldTag world;
    public ElementTag thunder;
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
        return super.matches(path);
    }

    @Override
    public String getName() {
        return "ThunderChanges";
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("world")) {
            return world;
        }
        else if (name.equals("thunder")) {
            return thunder;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onThunderChanges(ThunderChangeEvent event) {
        world = new WorldTag(event.getWorld());
        thunder = new ElementTag(event.toThunderState());
        this.event = event;
        fire(event);
    }
}
