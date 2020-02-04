package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleDamageEvent;

public class VehicleDamagedScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // vehicle damaged
    // <vehicle> damaged
    // entity damages vehicle
    // <entity> damages vehicle
    // entity damages <vehicle>
    // <entity> damages <vehicle>
    //
    // @Regex ^on [^\s]+ damages [^\s]+$
    //
    // @Switch in:<area> to only process the event if it occurred within a specified area.
    //
    // @Cancellable true
    //
    // @Triggers when a vehicle is damaged.
    //
    // @Context
    // <context.vehicle> returns the EntityTag of the vehicle.
    // <context.entity> returns the EntityTag of the attacking entity.
    //
    // @Determine
    // ElementTag(Decimal) to set the value of the damage received by the vehicle.
    //
    // @NPC when the entity that damaged the vehicle is a player.
    //
    // @NPC when the entity that damaged the vehicle is an NPC.
    //
    // -->

    public VehicleDamagedScriptEvent() {
        instance = this;
    }

    public static VehicleDamagedScriptEvent instance;
    public EntityTag vehicle;
    public EntityTag entity;
    private double damage;
    public VehicleDamageEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (path.eventArgLowerAt(3).equals("by")) {
            return false;
        }
        String tid = path.eventArgLowerAt(0);
        String cmd = path.eventArgAt(1);
        return !tid.equals("entity") && (cmd.equals("damaged")
                || (cmd.equals("damages") && !path.eventArgLowerAt(2).equals("entity")));
    }

    @Override
    public boolean matches(ScriptPath path) {
        String cmd = path.eventArgLowerAt(1);
        String veh = cmd.equals("damaged") ? path.eventArgLowerAt(0) : path.eventArgLowerAt(2);
        String ent = cmd.equals("damages") ? path.eventArgLowerAt(0) : "";

        if (!tryEntity(vehicle, veh)) {
            return false;
        }
        if (!ent.isEmpty() && entity != null && !tryEntity(entity, ent)) {
            return false;
        }

        if (!runInCheck(path, vehicle.getLocation())) {
            return false;
        }

        return super.matches(path);
    }

    @Override
    public String getName() {
        return "VehicleDamaged";
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        if (determinationObj instanceof ElementTag && ((ElementTag) determinationObj).isDouble()) {
            damage = ((ElementTag) determinationObj).asDouble();
            return true;
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        if (entity != null) {
            return new BukkitScriptEntryData(entity);
        }
        return new BukkitScriptEntryData(null, null);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("vehicle")) {
            return vehicle.getDenizenObject();
        }
        else if (name.equals("entity") && entity != null) {
            return entity.getDenizenObject();
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onVehicleDestroyed(VehicleDamageEvent event) {
        vehicle = new EntityTag(event.getVehicle());
        entity = event.getAttacker() != null ? new EntityTag(event.getAttacker()) : null;
        damage = event.getDamage();
        this.event = event;
        fire(event);
        event.setDamage(damage);
    }
}
