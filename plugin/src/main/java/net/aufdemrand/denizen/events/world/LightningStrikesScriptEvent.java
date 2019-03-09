package net.aufdemrand.denizen.events.world;


import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dWorld;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.LightningStrikeEvent;

public class LightningStrikesScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // lightning strikes
    //
    // @Regex ^on lightning strikes$
    // @Switch in <area>
    //
    // @Cancellable true
    //
    // @Triggers when lightning strikes in a world.
    //
    // @Context
    // <context.world> DEPRECATED
    // <context.lightning> returns the dEntity of the lightning.
    // <context.location> returns the dLocation where the lightning struck.
    //
    // -->

    public LightningStrikesScriptEvent() {
        instance = this;
    }

    public static LightningStrikesScriptEvent instance;
    public dEntity lightning;
    public dLocation location;
    public LightningStrikeEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).startsWith("lightning strikes");
    }

    @Override
    public boolean matches(ScriptPath path) {
        return runInCheck(path, location);
    }

    @Override
    public String getName() {
        return "LightningStrikes";
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("lightning")) {
            return lightning;
        }
        else if (name.equals("location")) {
            return location;
        }
        else if (name.equals("world")) { // NOTE: Deprecated in favor of context.location.world
            return new dWorld(location.getWorld());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onLightningStrikes(LightningStrikeEvent event) {
        lightning = new dEntity(event.getLightning());
        location = new dLocation(event.getLightning().getLocation());
        this.event = event;
        cancelled = event.isCancelled();
        fire();
        event.setCancelled(cancelled);
    }
}
