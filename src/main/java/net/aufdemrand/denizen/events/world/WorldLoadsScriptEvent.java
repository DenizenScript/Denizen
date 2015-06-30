package net.aufdemrand.denizen.events.world;


import net.aufdemrand.denizen.objects.dWorld;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.events.ScriptEvent;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

import java.util.HashMap;

public class WorldLoadsScriptEvent extends ScriptEvent implements Listener {

    // <--[event]
    // @Events
    // world loads
    // <world> loads
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
        return CoreUtilities.getXthArg(1,CoreUtilities.toLowerCase(s)).equals("loads");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String wCheck = CoreUtilities.getXthArg(0,CoreUtilities.toLowerCase(s));
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
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        WorldLoadEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public HashMap<String, dObject> getContext() {
        HashMap<String, dObject> context = super.getContext();
        context.put("world", world);
        return context;
    }

    @EventHandler
    public void onWorldLoads(WorldLoadEvent event) {
        world = new dWorld(event.getWorld());
        this.event = event;
        fire();
    }
}
