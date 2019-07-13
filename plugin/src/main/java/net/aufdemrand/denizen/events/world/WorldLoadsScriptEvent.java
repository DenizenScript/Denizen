package net.aufdemrand.denizen.events.world;


import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dWorld;
import com.denizenscript.denizencore.objects.dObject;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

public class WorldLoadsScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // world loads
    // <world> loads
    //
    // @Regex ^on [^\s]+ loads$
    //
    // @Triggers when a world is loaded.
    //
    // @Context
    // <context.world> returns the dWorld that was loaded.
    //
    // -->

    public WorldLoadsScriptEvent() {
        instance = this;
    }

    public static WorldLoadsScriptEvent instance;
    public dWorld world;
    public WorldLoadEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.getXthArg(1, CoreUtilities.toLowerCase(s)).equals("loads");
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
        return "WorldLoads";
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
        return super.getContext(name);
    }

    @EventHandler
    public void onWorldLoads(WorldLoadEvent event) {
        world = new dWorld(event.getWorld());
        this.event = event;
        fire(event);
    }
}
