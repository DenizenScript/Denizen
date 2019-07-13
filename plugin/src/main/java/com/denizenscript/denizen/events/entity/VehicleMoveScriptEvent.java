package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizen.objects.dLocation;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.dObject;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;

public class VehicleMoveScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // vehicle moves
    // <vehicle> moves
    //
    // @Regex ^on [^\s]+ moves$
    // @Switch in <area>
    //
    // @Warning This event fires very very rapidly!
    //
    // @Triggers when a vehicle moves in the slightest.
    //
    // @Context
    // <context.vehicle> returns the dEntity of the vehicle.
    // <context.from> returns the location of where the vehicle was.
    // <context.to> returns the location of where the vehicle is.
    //
    // -->

    public VehicleMoveScriptEvent() {
        instance = this;
    }

    public static VehicleMoveScriptEvent instance;
    public dEntity vehicle;
    public dLocation from;
    public dLocation to;
    public VehicleMoveEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return CoreUtilities.xthArgEquals(1, lower, "moves");
    }

    @Override
    public boolean matches(ScriptPath path) {

        if (!tryEntity(vehicle, path.eventArgLowerAt(0))) {
            return false;
        }

        if (!runInCheck(path, vehicle.getLocation())) {
            return false;
        }

        return true;
    }

    // TODO: Can the vehicle be an NPC?

    @Override
    public String getName() {
        return "VehicleMoves";
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("from")) {
            return from;
        }
        else if (name.equals("to")) {
            return to;
        }
        else if (name.equals("vehicle")) {
            return vehicle;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onVehicleMove(VehicleMoveEvent event) {
        to = new dLocation(event.getTo());
        from = new dLocation(event.getFrom());
        vehicle = new dEntity(event.getVehicle());
        this.event = event;
        fire(event);
    }
}
