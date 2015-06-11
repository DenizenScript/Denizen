package net.aufdemrand.denizen.events.scriptevents;

import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.events.ScriptEvent;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;

import java.util.HashMap;

public class VehicleCollidesEntityScriptEvent extends ScriptEvent implements Listener {

    // <--[event]
    // @Events
    // vehicle collides with entity
    // vehicle collides with <entity>
    // <vehicle> collides with entity
    // <vehicle> collides with <entity>
    //
    // @Cancellable true
    //
    // @Triggers when a vehicle collides with an entity.
    //
    // @Context
    // <context.vehicle> returns the dEntity of the vehicle.
    // <context.entity> returns the dEntity of the entity the vehicle has collided with.
    //
    // @Determine
    // "NOPICKUP" to stop the vehicle from picking up the entity.
    //
    // -->

    public VehicleCollidesEntityScriptEvent() {
        instance = this;
    }

    public static VehicleCollidesEntityScriptEvent instance;
    public dEntity vehicle;
    public dEntity entity;
    public Boolean pickup_cancel;
    public VehicleEntityCollisionEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String arg = lower.substring(lower.lastIndexOf("with ") + 5);
        return lower.contains(" collides with ")
                && (arg.equals("entity")
                || dEntity.matches(arg));
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);

        String v_test = lower.substring(0,lower.indexOf(" "));
        if (!v_test.equals("vehicle")
                && !v_test.equals(vehicle.identifySimpleType())) {
            return false;
        }

        String e_test= lower.substring(lower.lastIndexOf(" ") + 1);
        if (!e_test.equals("entity")
                && !e_test.equals(entity.identifySimpleType())) {
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "VehicleCollidesEntity";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        VehicleEntityCollisionEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        if (determination.toLowerCase().equals("nopickup")) {
            pickup_cancel = true;
            return true;
        }
        return super.applyDetermination(container, determination);
    }

    @Override
    public HashMap<String, dObject> getContext() {
        HashMap<String, dObject> context = super.getContext();
        context.put("vehicle", vehicle);
        context.put("entity", entity);
        return context;
    }

    @EventHandler
    public void onVehicleCollidesEntity(VehicleEntityCollisionEvent event) {
        entity = new dEntity(event.getEntity());
        vehicle = new dEntity(event.getVehicle());
        pickup_cancel = event.isPickupCancelled();
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
        event.setPickupCancelled(pickup_cancel);
    }
}
