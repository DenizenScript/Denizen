package net.aufdemrand.denizen.events.world;


import net.aufdemrand.denizen.objects.dWorld;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.events.ScriptEvent;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldSaveEvent;

import java.util.HashMap;

public class WorldSavesScriptEvent extends ScriptEvent implements Listener {

    // <--[event]
    // @Events
    // world saves
    // <world> saves
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
        dB.log("world: " + CoreUtilities.toLowerCase(world.getName()));
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
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        WorldSaveEvent.getHandlerList().unregister(this);
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
    public void onWorldSaves(WorldSaveEvent event) {
        world = new dWorld(event.getWorld());
        this.event = event;
        fire();
    }
}
