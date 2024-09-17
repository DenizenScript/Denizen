package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.entity.Entity;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityEvent;

public class EntityExitsVehicleScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // entity exits vehicle
    // <entity> exits <entity>
    //
    // @Group Entity
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when an entity dismounts from another entity.
    //
    // @Context
    // <context.vehicle> returns the EntityTag of the mount vehicle.
    // <context.entity> returns the EntityTag of the exiting entity.
    //
    // @Player when the entity that dismounts the vehicle is a player.
    //
    // @NPC when the entity that dismounts the vehicle is an NPC.
    //
    // -->

    public EntityExitsVehicleScriptEvent() {
        registerCouldMatcher("<entity> exits <entity>");
    }

    public EntityTag vehicle;
    public EntityTag entity;

    @Override
    public boolean matches(ScriptPath path) {
        if (!path.tryArgObject(0, entity)) {
            return false;
        }
        String vehicleLabel = path.eventArgLowerAt(2);
        if (!vehicleLabel.equals("vehicle") && !vehicle.tryAdvancedMatcher(vehicleLabel, path.context)) {
            return false;
        }
        if (!runInCheck(path, vehicle.getLocation())) {
            return false;
        }
        return super.matches(path);
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
        else if (name.equals("entity")) {
            return entity.getDenizenObject();
        }
        return super.getContext(name);
    }

    @Override
    public String getName() { // TODO: once 1.20 is the minimum supported version, remove
        return "EntityExitsVehicle";
    }

    public void fire(EntityEvent event, Entity vehicle) {
        this.entity = new EntityTag(event.getEntity());
        this.vehicle = new EntityTag(vehicle);
        fire(event);
    }
}
