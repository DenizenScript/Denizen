package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleEnterEvent;

import java.util.Arrays;
import java.util.HashSet;

public class EntityEntersVehicleScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // entity enters vehicle
    // entity enters <vehicle>
    // <entity> enters vehicle
    // <entity> enters <vehicle>
    //
    // @Regex ^on [^\s]+ enters [^\s]+$
    //
    // @Switch in:<area> to only process the event if it occurred within a specified area.
    //
    // @Cancellable true
    //
    // @Triggers when an entity enters a vehicle.
    //
    // @Context
    // <context.vehicle> returns the EntityTag of the vehicle.
    // <context.entity> returns the EntityTag of the entering entity.
    //
    // @Player when the entity that entered the vehicle is a player.
    //
    // @NPC when the entity that entered the vehicle is an NPC.
    //
    // -->

    public EntityEntersVehicleScriptEvent() {
        instance = this;
    }

    public static EntityEntersVehicleScriptEvent instance;
    public EntityTag vehicle;
    public EntityTag entity;
    public VehicleEnterEvent event;

    public static HashSet<String> notRelevantEnterables = new HashSet<>(Arrays.asList("notable", "cuboid", "biome", "bed", "portal"));

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!path.eventArgLowerAt(1).equals("enters")) {
            return false;
        }
        if (notRelevantEnterables.contains(path.eventArgLowerAt(2))) {
            return false;
        }
        if (!couldMatchEntity(path.eventArgLowerAt(0))) {
            return false;
        }
        if (!couldMatchVehicle(path.eventArgLowerAt(2))) {
            return false;
        }
        return true;
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!tryEntity(entity, path.eventArgLowerAt(0))
                || !tryEntity(vehicle, path.eventArgLowerAt(2))) {
            return false;
        }
        if (!runInCheck(path, vehicle.getLocation())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public String getName() {
        return "EntityEntersVehicle";
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
    public void onEntityEntersVehicle(VehicleEnterEvent event) {
        vehicle = new EntityTag(event.getVehicle());
        entity = new EntityTag(event.getEntered());
        this.event = event;
        fire(event);
    }
}
