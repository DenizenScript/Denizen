package net.aufdemrand.denizen.events.entity;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleDestroyEvent;

public class VehicleDestroyedScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // vehicle destroyed
    // <vehicle> destroyed
    // entity destroys vehicle
    // <entity> destroys vehicle
    // entity destroys <vehicle>
    // <entity> destroys <vehicle>
    //
    // @Regex ^on [^\s]+ destroys [^\s]+$
    // @Switch in <area>
    //
    // @Cancellable true
    //
    // @Triggers when a vehicle is destroyed.
    //
    // @Context
    // <context.vehicle> returns the dEntity of the vehicle.
    // <context.entity> returns the dEntity of the attacking entity.
    //
    // @NPC when the entity that destroyed the vehicle is a player..
    //
    // @NPC when the entity that destroyed the vehicle is an NPC.
    //
    // -->

    public VehicleDestroyedScriptEvent() {
        instance = this;
    }

    public static VehicleDestroyedScriptEvent instance;
    public dEntity vehicle;
    public dEntity entity;
    public VehicleDestroyEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String cmd = CoreUtilities.getXthArg(1, lower);
        return cmd.equals("destroyed") || cmd.equals("destroys");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String lower = path.eventLower;
        String cmd = path.eventArgLowerAt(1);
        String veh = cmd.equals("destroyed") ? path.eventArgLowerAt(0) : path.eventArgLowerAt(2);
        String ent = cmd.equals("destroys") ? path.eventArgLowerAt(0) : "";

        if (!tryEntity(vehicle, veh)) {
            return false;
        }

        if (ent.length() > 0 && (entity == null || !tryEntity(entity, ent))) {
            return false;
        }

        if (!runInCheck(path, vehicle.getLocation())) {
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "VehicleDestroyed";
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        if (entity != null) {
            return new BukkitScriptEntryData(entity.isPlayer() ? entity.getDenizenPlayer() : null,
                    entity.isCitizensNPC() ? entity.getDenizenNPC() : null);
        }
        return new BukkitScriptEntryData(null, null);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("vehicle")) {
            return vehicle;
        }
        else if (name.equals("entity") && entity != null) {
            return entity;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onVehicleDestroyed(VehicleDestroyEvent event) {
        vehicle = new dEntity(event.getVehicle());
        entity = event.getAttacker() != null ? new dEntity(event.getAttacker()) : null;
        this.event = event;
        fire(event);
    }
}
