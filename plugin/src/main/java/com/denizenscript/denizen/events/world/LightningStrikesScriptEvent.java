package com.denizenscript.denizen.events.world;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.LightningStrikeEvent;

public class LightningStrikesScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // lightning strikes
    //
    // @Group World
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when lightning strikes in a world.
    //
    // @Context
    // <context.lightning> returns the EntityTag of the lightning.
    // <context.location> returns the LocationTag where the lightning struck.
    //
    // -->

    public LightningStrikesScriptEvent() {
        registerCouldMatcher("lightning strikes");
    }

    public LocationTag location;
    public LightningStrikeEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, location)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "lightning":
                return new EntityTag(event.getLightning());
            case "location":
                return location;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onLightningStrikes(LightningStrikeEvent event) {
        location = new LocationTag(event.getLightning().getLocation());
        this.event = event;
        fire(event);
    }
}
