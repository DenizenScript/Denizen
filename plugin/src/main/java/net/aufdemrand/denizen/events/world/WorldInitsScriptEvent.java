package net.aufdemrand.denizen.events.world;

import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dWorld;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;

public class WorldInitsScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // world initializes
    // <world> initializes
    //
    // @Regex ^on [^\s]+ initializes$
    //
    // @Triggers when a world is initialized.
    //
    // @Context
    // <context.world> returns the dWorld that was initialized.
    //
    // -->

    public WorldInitsScriptEvent() {
        instance = this;
    }

    public static WorldInitsScriptEvent instance;
    public dWorld world;
    public WorldInitEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.getXthArg(1, CoreUtilities.toLowerCase(s)).equals("initializes");
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
        return "WorldInits";
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
    public void onWorldInits(WorldInitEvent event) {
        world = new dWorld(event.getWorld());
        this.event = event;
        fire();
    }
}
