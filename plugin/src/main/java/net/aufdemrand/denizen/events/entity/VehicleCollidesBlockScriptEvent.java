package net.aufdemrand.denizen.events.entity;

import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dMaterial;
import com.denizenscript.denizencore.objects.dObject;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleBlockCollisionEvent;

public class VehicleCollidesBlockScriptEvent extends BukkitScriptEvent implements Listener {

    // TODO: de-collide with 'collides with entity'
    // <--[event]
    // @Events
    // vehicle collides with block
    // vehicle collides with <material>
    // <vehicle> collides with block
    // <vehicle> collides with <material>
    //
    // @Regex ^on [^\s]+ collides with [^\s]+$
    // @Switch in <area>
    //
    // @Triggers when a vehicle collides with a block.
    //
    // @Context
    // <context.vehicle> returns the dEntity of the vehicle.
    // <context.location> returns the dLocation of the block.
    //
    // -->

    public VehicleCollidesBlockScriptEvent() {
        instance = this;
    }

    public static VehicleCollidesBlockScriptEvent instance;

    public dEntity vehicle;
    public dLocation location;
    private dMaterial material;
    public VehicleBlockCollisionEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return lower.contains("collides with");
    }

    @Override
    public boolean matches(ScriptPath path) {

        if (!tryEntity(vehicle, path.eventArgLowerAt(0))) {
            return false;
        }

        if (!tryMaterial(material, path.eventArgLowerAt(3))) {
            return false;
        }

        if (!runInCheck(path, location)) {
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "VehicleCollidesBlock";
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("vehicle")) {
            return vehicle;
        }
        else if (name.equals("location")) {
            return location;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onVehicleCollidesBlock(VehicleBlockCollisionEvent event) {
        vehicle = new dEntity(event.getVehicle());
        location = new dLocation(event.getBlock().getLocation());
        material = new dMaterial(event.getBlock());
        this.event = event;
        fire(event);
    }
}
