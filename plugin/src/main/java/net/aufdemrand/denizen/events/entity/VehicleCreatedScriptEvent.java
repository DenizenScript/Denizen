package net.aufdemrand.denizen.events.entity;

import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleCreateEvent;

public class VehicleCreatedScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // vehicle created (in <area>)
    // <vehicle> created (in <area>)
    //
    // @Regex ^on [^\s]+ created( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
    //
    // @Cancellable true
    //
    // @Triggers when a vehicle is created.
    //
    // @Context
    // <context.vehicle> returns the dEntity of the vehicle.
    //
    // -->

    public VehicleCreatedScriptEvent() {
        instance = this;
    }

    public static VehicleCreatedScriptEvent instance;
    public dEntity vehicle;
    public VehicleCreateEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return CoreUtilities.xthArgEquals(1, lower, "created");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, ScriptPath path) {
        String s = path.event;
        String lower = path.eventLower;

        if (!tryEntity(vehicle, CoreUtilities.getXthArg(0, lower))) {
            return false;
        }

        if (!runInCheck(scriptContainer, s, lower, vehicle.getLocation())) {
            return false;
        }

        return true;
    }

    // TODO: Can the vehicle be an NPC?

    @Override
    public String getName() {
        return "VehicleCreated";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        VehicleCreateEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("vehicle")) {
            return vehicle;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onVehicleCreated(VehicleCreateEvent event) {
        Entity entity = event.getVehicle();
        cancelled = event.isCancelled();
        dEntity.rememberEntity(entity);
        vehicle = new dEntity(entity);
        this.event = event;
        fire();
        dEntity.forgetEntity(entity);
        event.setCancelled(cancelled);
    }
}
