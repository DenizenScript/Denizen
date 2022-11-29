package com.denizenscript.denizen.events.world;

import com.denizenscript.denizen.objects.WorldTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

public class WorldLoadsScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // <world> loads
    //
    // @Group World
    //
    // @Triggers when a world is loaded.
    //
    // @Context
    // <context.world> returns the WorldTag that was loaded.
    //
    // -->

    public WorldLoadsScriptEvent() {
        registerCouldMatcher("<world> loads");
    }

    public WorldTag world;
    public WorldLoadEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!super.couldMatch(path)) {
            return false;
        }
        if (path.eventArgLowerAt(0).equals("chunk")) {
            return false;
        }
        return true;
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!path.tryArgObject(0, world)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("world")) {
            return world;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onWorldLoads(WorldLoadEvent event) {
        world = new WorldTag(event.getWorld());
        this.event = event;
        fire(event);
    }
}
