package com.denizenscript.denizen.events.world;

import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.WorldTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.SpawnChangeEvent;

public class SpawnChangeScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // spawn changes
    //
    // @Regex ^on spawn changes$
    //
    // @Switch for:<world> to only process the event when a specified world's spawn changes.
    //
    // @Group World
    //
    // @Triggers when the world's spawn point changes.
    //
    // @Context
    // <context.world> returns the WorldTag that the spawn point changed in.
    // <context.old_location> returns the LocationTag of the old spawn point.
    // <context.new_location> returns the LocationTag of the new spawn point.
    //
    // -->

    public SpawnChangeScriptEvent() {
        instance = this;
    }

    public static SpawnChangeScriptEvent instance;
    public SpawnChangeEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        return path.eventLower.startsWith("spawn changes");
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!runGenericSwitchCheck(path, "world", event.getWorld().getName())) {
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return "SpawnChange";
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("world")) {
            return new WorldTag(event.getWorld());
        }
        else if (name.equals("old_location")) {
            return new LocationTag(event.getPreviousLocation());
        }
        else if (name.equals("new_location")) {
            return new LocationTag(event.getWorld().getSpawnLocation());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onSpawnChange(SpawnChangeEvent event) {
        this.event = event;
        fire(event);
    }
}
