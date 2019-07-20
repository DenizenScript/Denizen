package com.denizenscript.denizen.events.world;

import com.denizenscript.denizen.objects.WorldTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldUnloadEvent;

public class WorldUnloadsScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // world unloads
    // <world> unloads
    //
    // @Regex ^on [^\s]+ unloads$
    //
    // @Group World
    //
    // @Cancellable true
    //
    // @Triggers when a world is unloaded.
    //
    // @Context
    // <context.world> returns the WorldTag that was unloaded.
    //
    // -->

    public WorldUnloadsScriptEvent() {
        instance = this;
    }

    public static WorldUnloadsScriptEvent instance;
    public WorldTag world;
    public WorldUnloadEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return CoreUtilities.getXthArg(1, lower).equals("unloads")
                && !CoreUtilities.getXthArg(0, lower).equals("chunk");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String wCheck = CoreUtilities.getXthArg(0, CoreUtilities.toLowerCase(s));
        if (!wCheck.equals("world") && !wCheck.equals(CoreUtilities.toLowerCase(world.getName()))) {
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return "WorldUnloads";
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
    public void onWorldUnloads(WorldUnloadEvent event) {
        world = new WorldTag(event.getWorld());
        this.event = event;
        fire(event);
    }
}
