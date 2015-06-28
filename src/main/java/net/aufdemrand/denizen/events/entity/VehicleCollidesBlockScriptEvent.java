package net.aufdemrand.denizen.events.entity;

import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dMaterial;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.events.ScriptEvent;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleBlockCollisionEvent;

import java.util.HashMap;

public class VehicleCollidesBlockScriptEvent extends ScriptEvent implements Listener {

    // <--[event]
    // @Events
    // vehicle collides with block
    // vehicle collides with <material>
    // <vehicle> collides with block
    // <vehicle> collides with <material>
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
    public VehicleBlockCollisionEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String arg = lower.substring(lower.lastIndexOf("with ") + 5);
        return lower.contains(" collides with ") && (arg.equals("block") || dMaterial.matches(arg));
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);

        String block = lower.substring(lower.lastIndexOf(" ") + 1);
        String mat = dMaterial.getMaterialFrom(event.getBlock().getType(), event.getBlock().getData()).identifyNoIdentifier();

        if (mat.equals("m@air") || (mat.equals("air"))) {
            return false;
        }
        if (!block.equals("block")
                && !block.equals(mat)) {
            return false;
        }

        String ent = CoreUtilities.getXthArg(0, s);
        return vehicle.matchesEntity(ent);
    }

    @Override
    public String getName() {
        return "VehicleCollidesBlock";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        VehicleBlockCollisionEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public HashMap<String, dObject> getContext() {
        HashMap<String, dObject> context = super.getContext();
        context.put("vehicle", vehicle);
        context.put("location", location);
        return context;
    }

    @EventHandler
    public void onVehicleCollidesBlock(VehicleBlockCollisionEvent event) {
        vehicle = new dEntity(event.getVehicle());
        location = new dLocation(event.getBlock().getLocation());
        this.event = event;
        fire();
    }
}
