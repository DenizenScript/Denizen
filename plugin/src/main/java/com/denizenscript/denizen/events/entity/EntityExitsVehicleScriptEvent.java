package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleExitEvent;

public class EntityExitsVehicleScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // entity exits vehicle
    // entity exits <vehicle>
    // <entity> exits vehicle
    // <entity> exits <vehicle>
    //
    // @Regex ^on [^\s]+ exits [^\s]+$
    // @Switch in <area>
    //
    // @Cancellable true
    //
    // @Triggers when an entity exits a vehicle.
    //
    // @Context
    // <context.vehicle> returns the EntityTag of the vehicle.
    // <context.entity> returns the EntityTag of the exiting entity.
    //
    // @Player when the entity that exits the vehicle is a player.
    //
    // @NPC when the entity that exists the vehicle is an NPC.
    //
    // -->

    public EntityExitsVehicleScriptEvent() {
        instance = this;
    }

    public static EntityExitsVehicleScriptEvent instance;
    public EntityTag vehicle;
    public EntityTag entity;
    public VehicleExitEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.getXthArg(1, CoreUtilities.toLowerCase(s)).equals("exits");
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

        return true;
    }

    @Override
    public String getName() {
        return "EntityExitsVehicle";
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(entity.isPlayer() ? entity.getDenizenPlayer() : null,
                entity.isCitizensNPC() ? entity.getDenizenNPC() : null);
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
    public void onEntityExitsVehicle(VehicleExitEvent event) {
        vehicle = new EntityTag(event.getVehicle());
        entity = new EntityTag(event.getExited());
        this.event = event;
        fire(event);
    }
}
