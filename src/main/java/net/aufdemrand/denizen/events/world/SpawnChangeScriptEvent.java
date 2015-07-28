package net.aufdemrand.denizen.events.world;


import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dWorld;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.events.ScriptEvent;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.SpawnChangeEvent;

public class SpawnChangeScriptEvent extends ScriptEvent implements Listener {

    // <--[event]
    // @Events
    // spawn changes
    //
    // @Triggers when the world's spawn point changes.
    //
    // @Context
    // <context.world> returns the dWorld that the spawn point changed in.
    // <context.old_location> returns the dLocation of the old spawn point.
    // <context.new_location> returns the dLocation of the new spawn point.
    //
    // -->

    public SpawnChangeScriptEvent() {
        instance = this;
    }

    public static SpawnChangeScriptEvent instance;
    public dWorld world;
    public dLocation old_location;
    public dLocation new_location;
    public SpawnChangeEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).startsWith("spawn changes");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        return true;
    }

    @Override
    public String getName() {
        return "SpawnChange";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        SpawnChangeEvent.getHandlerList().unregister(this);
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
        else if (name.equals("old_location")) {
            return old_location;
        }
        else if (name.equals("new_location")) {
            return new_location;
        }
        return super.getContext(name);
    }

    @EventHandler(ignoreCancelled = true)
    public void onSpawnChange(SpawnChangeEvent event) {
        world = new dWorld(event.getWorld());
        old_location = new dLocation(event.getPreviousLocation());
        new_location = new dLocation(event.getWorld().getSpawnLocation());
        this.event = event;
        fire();
    }
}
