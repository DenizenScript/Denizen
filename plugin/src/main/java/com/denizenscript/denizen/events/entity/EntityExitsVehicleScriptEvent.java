package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.spigotmc.event.entity.EntityDismountEvent;

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
        instance = this;
        registerCouldMatcher("<entity> exits <entity>");
    }

    public static EntityExitsVehicleScriptEvent instance;
    public EntityTag vehicle;
    public EntityTag entity;
    public EntityDismountEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!tryEntity(entity, path.eventArgLowerAt(0))) {
            return false;
        }
        String vehicleLabel = path.eventArgLowerAt(2);
        if (!vehicleLabel.equals("vehicle") && !tryEntity(vehicle, vehicleLabel)) {
            return false;
        }
        if (!runInCheck(path, vehicle.getLocation())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public String getName() {
        return "EntityExitsVehicle";
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(entity);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("vehicle")) {
            return vehicle;
        }
        else if (name.equals("entity")) {
            return entity;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onEntityExitsVehicle(EntityDismountEvent event) {
        vehicle = new EntityTag(event.getDismounted());
        entity = new EntityTag(event.getEntity());
        this.event = event;
        fire(event);
    }
}
