package com.denizenscript.denizen.events.vehicle;

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
    // @Group Vehicle
    //
    // @Regex ^on [^\s]+ damages [^\s]+$
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when a vehicle is damaged.
    //
    // @Context
    // <context.vehicle> returns the EntityTag of the vehicle.
    // <context.entity> returns the EntityTag of the attacking entity.
    // <context.damage> returns the amount of damage to be received.
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
    public VehicleDamageEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        String cmd = path.eventArgAt(1);
        if (!cmd.equals("damaged") && !cmd.equals("damages")) {
            return false;
        }
        if (path.eventArgLowerAt(2).equals("by")) {
            return false;
        }
        if (!exactMatchesVehicle(path.eventArgLowerAt(0)) && !exactMatchesVehicle(path.eventArgLowerAt(2))) {
            return false;
        }
        if (!couldMatchEntity(path.eventArgLowerAt(0))) {
            return false;
        }
        return true;
    }

    @Override
    public boolean matches(ScriptPath path) {
        String cmd = path.eventArgLowerAt(1);
        String veh = cmd.equals("damaged") ? path.eventArgLowerAt(0) : path.eventArgLowerAt(2);
        String ent = cmd.equals("damages") ? path.eventArgLowerAt(0) : "";
        if (!vehicle.tryAdvancedMatcher(veh)) {
            return false;
        }
        if (!ent.isEmpty() && (entity == null || !entity.tryAdvancedMatcher(ent))) {
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
            event.setDamage(((ElementTag) determinationObj).asDouble());
            return true;
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(entity);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("vehicle")) {
            return vehicle.getDenizenObject();
        }
        else if (name.equals("entity") && entity != null) {
            return entity.getDenizenObject();
        }
        else if (name.equals("damage")) {
            return new ElementTag(event.getDamage());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onVehicleDestroyed(VehicleDamageEvent event) {
        vehicle = new EntityTag(event.getVehicle());
        entity = event.getAttacker() != null ? new EntityTag(event.getAttacker()) : null;
        this.event = event;
        fire(event);
    }
}
