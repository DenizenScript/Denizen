package com.denizenscript.denizen.events.block;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.TNTPrimeEvent;

public class TNTPrimesScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // tnt primes
    //
    // @Location true
    //
    // @Group Block
    //
    // @Cancellable true
    //
    // @Triggers when TNT is activated and will soon explode.
    //
    // @Context
    // <context.entity> returns the entity that primed the TNT, if any.
    // <context.location> returns the location of the TNT block being primed.
    // <context.reason> returns the reason the TNT was primed. Refer to <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/block/TNTPrimeEvent.PrimeCause.html>
    //
    // @Player when the priming entity is a player.
    //
    // -->

    public TNTPrimesScriptEvent() {
        registerCouldMatcher("tnt primes");
    }

    public TNTPrimeEvent event;
    public LocationTag location;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, location)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(event.getPrimingEntity());
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("entity") && event.getPrimingEntity() != null) {
            return new EntityTag(event.getPrimingEntity()).getDenizenObject();
        }
        else if (name.equals("location")) {
            return location;
        }
        else if (name.equals("reason")) {
            return new ElementTag(event.getCause());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onTntPrimesEvent(TNTPrimeEvent event) {
        this.event = event;
        location = new LocationTag(event.getBlock().getLocation());
        fire(event);
    }
}
