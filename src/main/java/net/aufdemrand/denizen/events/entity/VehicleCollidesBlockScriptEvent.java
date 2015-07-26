package net.aufdemrand.denizen.events.entity;

import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dMaterial;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleBlockCollisionEvent;

import java.util.HashMap;

public class VehicleCollidesBlockScriptEvent extends BukkitScriptEvent implements Listener {

    // TODO: de-collide with 'collides with entity'
    // <--[event]
    // @Events
    // vehicle collides with block (in <area>)
    // vehicle collides with <material> (in <area>)
    // <vehicle> collides with block (in <area>)
    // <vehicle> collides with <material> (in <area>)
    //
    // @Regex ^on [^\s]+ collides with [^\s]+( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
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
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String ent = CoreUtilities.getXthArg(0, lower);
        if (!vehicle.matchesEntity(ent)) {
            return false;
        }

        String mat = CoreUtilities.getXthArg(3, lower);
        if (tryMaterial(material, mat)) {
            return false;
        }

        if (!runInCheck(scriptContainer, s, lower, location)) {
            return false;
        }

        return true;
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
        material = dMaterial.getMaterialFrom(event.getBlock().getType(), event.getBlock().getData());
        this.event = event;
        fire();
    }
}
