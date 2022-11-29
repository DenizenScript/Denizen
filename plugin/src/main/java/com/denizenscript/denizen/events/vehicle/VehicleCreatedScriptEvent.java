package com.denizenscript.denizen.events.vehicle;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleCreateEvent;

public class VehicleCreatedScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // vehicle created
    // <vehicle> created
    //
    // @Group Vehicle
    //
    // @Regex ^on [^\s]+ created$
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when a vehicle is created.
    //
    // @Context
    // <context.vehicle> returns the EntityTag of the vehicle.
    //
    // -->

    public VehicleCreatedScriptEvent() {
    }

    public EntityTag vehicle;
    public VehicleCreateEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!path.eventArgLowerAt(1).equals("created")) {
            return false;
        }
        if (!exactMatchesVehicle(path.eventArgLowerAt(0))) {
            return false;
        }
        return true;
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!path.tryArgObject(0, vehicle)) {
            return false;
        }
        if (!runInCheck(path, vehicle.getLocation())) {
            return false;
        }
        return super.matches(path);
    }

    // TODO: Can the vehicle be an NPC?

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("vehicle")) {
            return vehicle;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onVehicleCreated(VehicleCreateEvent event) {
        Entity entity = event.getVehicle();
        EntityTag.rememberEntity(entity);
        vehicle = new EntityTag(entity);
        this.event = event;
        fire(event);
        EntityTag.forgetEntity(entity);
    }
}
