package com.denizenscript.denizen.events.world;

import com.denizenscript.denizen.objects.dWorld;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldSaveEvent;

public class WorldSavesScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // world saves
    // <world> saves
    //
    // @Regex ^on [^\s]+ saves$
    //
    // @Triggers when a world is saved.
    //
    // @Context
    // <context.world> returns the dWorld that was saved.
    //
    // -->

    public WorldSavesScriptEvent() {
        instance = this;
    }

    public static WorldSavesScriptEvent instance;
    public dWorld world;
    public WorldSaveEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.getXthArg(1, CoreUtilities.toLowerCase(s)).equals("saves");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String wCheck = CoreUtilities.getXthArg(0, CoreUtilities.toLowerCase(s));
        Debug.log("world: " + CoreUtilities.toLowerCase(world.getName()));
        if (!wCheck.equals("world") && !wCheck.equals(CoreUtilities.toLowerCase(world.getName()))) {
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return "WorldSaves";
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("world")) {
            return world;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onWorldSaves(WorldSaveEvent event) {
        world = new dWorld(event.getWorld());
        this.event = event;
        fire(event);
    }
}
