package net.aufdemrand.denizen.events.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.events.ScriptEvent;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingPlaceEvent;

import java.util.HashMap;

public class PlayerPlacesHangingScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player places hanging (in <area>)
    // player places <hanging> (in <area>)
    //
    // @Cancellable true
    //
    // @Triggers when a hanging entity (painting or itemframe) is placed.
    //
    // @Context
    // <context.hanging> returns the dEntity of the hanging.
    // <context.location> DEPRECATED
    // <context.cuboids> DEPRECATED.
    //
    // -->

    public PlayerPlacesHangingScriptEvent() {
        instance = this;
    }

    public static PlayerPlacesHangingScriptEvent instance;
    public dEntity hanging;
    public dList cuboids;
    public dLocation location;
    public HangingPlaceEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return lower.startsWith("player places");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String hangCheck = CoreUtilities.getXthArg(2, lower);
        if (!hanging.matchesEntity(hangCheck)) {
            return false;
        }

        if (!runInCheck(scriptContainer, s, lower, location)) {
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "PlayerPlacesHanging";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        HangingPlaceEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(new dPlayer(event.getPlayer()), null);
    }

    @Override
    public HashMap<String, dObject> getContext() {
        HashMap<String, dObject> context = super.getContext();
        context.put("hanging", hanging);
        context.put("cuboids", cuboids);
        context.put("location", location);
        return context;
    }

    @EventHandler
    public void pnPlayerPlacesHanging(HangingPlaceEvent event) {
        hanging = new dEntity(event.getEntity());
        location = new dLocation(event.getBlock().getLocation());
        cuboids = new dList();
        for (dCuboid cuboid : dCuboid.getNotableCuboidsContaining(location)) {
            cuboids.add(cuboid.identifySimple());
        }
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
    }
}
