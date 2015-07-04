package net.aufdemrand.denizen.events.entity;

import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;

import java.util.HashMap;

public class VehicleMoveScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // vehicle moves
    // <vehicle> moves
    //
    // @Warning This event fires very very rapidly!
    //
    // @Triggers when a vehicle moves in the slightest.
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
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String veh = CoreUtilities.getXthArg(0, lower);
        if (!vehicle.matchesEntity(veh)) {
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
        return "VehicleMoves";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        VehicleMoveEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public HashMap<String, dObject> getContext() {
        HashMap<String, dObject> context = super.getContext();
        context.put("from", from);
        context.put("to", to);
        context.put("vehicle", vehicle);
        return context;
    }

    @EventHandler
    public void onVehicleMove(VehicleMoveEvent event) {
        to = new dLocation(event.getTo());
        from = new dLocation(event.getFrom());
        vehicle = new dEntity(event.getVehicle());
        this.event = event;
        fire();
    }
}
