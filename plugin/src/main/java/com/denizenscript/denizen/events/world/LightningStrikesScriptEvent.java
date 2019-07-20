package com.denizenscript.denizen.events.world;


import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.WorldTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.LightningStrikeEvent;

public class LightningStrikesScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // lightning strikes
    //
    // @Regex ^on lightning strikes$
    //
    // @Group World
    //
    // @Switch in <area>
    //
    // @Cancellable true
    //
    // @Triggers when lightning strikes in a world.
    //
    // @Context
    // <context.world> DEPRECATED
    // <context.lightning> returns the EntityTag of the lightning.
    // <context.location> returns the LocationTag where the lightning struck.
    //
    // -->

    public LightningStrikesScriptEvent() {
        instance = this;
    }

    public static LightningStrikesScriptEvent instance;
    public EntityTag lightning;
    public LocationTag location;
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
    public ObjectTag getContext(String name) {
        if (name.equals("lightning")) {
            return lightning;
        }
        else if (name.equals("location")) {
            return location;
        }
        else if (name.equals("world")) { // NOTE: Deprecated in favor of context.location.world
            return new WorldTag(location.getWorld());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onLightningStrikes(LightningStrikeEvent event) {
        lightning = new EntityTag(event.getLightning());
        location = new LocationTag(event.getLightning().getLocation());
        this.event = event;
        fire(event);
    }
}
