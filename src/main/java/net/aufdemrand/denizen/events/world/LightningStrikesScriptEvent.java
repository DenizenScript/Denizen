package net.aufdemrand.denizen.events.world;


import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dWorld;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.LightningStrikeEvent;

import java.util.HashMap;

public class LightningStrikesScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // lightning strikes (in <area>)
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
    public boolean matches(ScriptContainer scriptContainer, String s) {
        return runInCheck(scriptContainer, s, CoreUtilities.toLowerCase(s), location);
    }

    @Override
    public String getName() {
        return "LightningStrikes";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        LightningStrikeEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public HashMap<String, dObject> getContext() {
        HashMap<String, dObject> context = super.getContext();
        context.put("lightning", lightning);
        context.put("world", new dWorld(location.getWorld()));
        context.put("location", location);
        return context;
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
