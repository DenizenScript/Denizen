package net.aufdemrand.denizen.events.core;

import net.aufdemrand.denizen.scripts.containers.core.BukkitWorldScriptHelper;
import net.aufdemrand.denizencore.events.OldSmartEvent;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizencore.objects.*;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleBlockCollisionEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class VehicleCollisionSmartEvent implements OldSmartEvent, Listener {


    ///////////////////
    // SMARTEVENT METHODS
    ///////////////


    @Override
    public boolean shouldInitialize(Set<String> events) {

        // Loop through event names from loaded world script events
        for (String event : events) {

            // TODO: More precise regex
            Matcher m = Pattern.compile("on (.+) collides with (.+)", Pattern.CASE_INSENSITIVE)
                    .matcher(event);

            if (m.matches()) {
                // TODO: Check input data
                return true;
            }
        }
        // No matches at all, so return false.
        return false;
    }


    @Override
    public void _initialize() {
        // Yay! Your event is in use! Register it here.
        DenizenAPI.getCurrentInstance().getServer().getPluginManager()
                .registerEvents(this, DenizenAPI.getCurrentInstance());
        // Record that you loaded in the debug.
        dB.log("Loaded Vehicle Collision SmartEvent.");
    }


    @Override
    public void breakDown() {
        // Unregister events or any other temporary links your event created in _intialize()
        VehicleBlockCollisionEvent.getHandlerList().unregister(this);
        VehicleEntityCollisionEvent.getHandlerList().unregister(this);
    }

    //////////////
    //  MECHANICS
    ///////////

    // <--[event]
    // @Events
    // vehicle collides with block
    // vehicle collides with <material>
    // <vehicle> collides with block
    // <vehicle> collides with <material>
    //
    // @Triggers when a vehicle collides with a block.
    // @Context
    // <context.vehicle> returns the dEntity of the vehicle.
    // <context.location> returns the dLocation of the block.
    //
    // -->
    @EventHandler
    public void vehicleBlockCollision(VehicleBlockCollisionEvent event) {

        // Bukkit seems to be triggering collision on air.. let's filter that out.
        if (event.getBlock().getType() == Material.AIR) return;

        dPlayer player = null;
        dNPC npc = null; // TODO: These are always null!

        dEntity vehicle = new dEntity(event.getVehicle());
        dMaterial material = dMaterial.getMaterialFrom(event.getBlock().getType(), event.getBlock().getData());

        Map<String, dObject> context = new HashMap<String, dObject>();
        context.put("vehicle", vehicle);
        context.put("location", new dLocation(event.getBlock().getLocation()));

        List<String> events = new ArrayList<String>();
        events.add("vehicle collides with block");
        events.add("vehicle collides with " + material.identifySimple());
        events.add(vehicle.identifyType() + " collides with block");
        events.add(vehicle.identifyType() + " collides with " + material.identifySimple());

        BukkitWorldScriptHelper.doEvents(events, npc, player, context, true);
    }

    // <--[event]
    // @Events
    // vehicle collides with entity
    // vehicle collides with <entity>
    // <vehicle> collides with entity
    // <vehicle> collides with <entity>
    //
    // @Triggers when a vehicle collides with an entity.
    // @Context
    // <context.vehicle> returns the dEntity of the vehicle.
    // <context.entity> returns the dEntity of the entity the vehicle has collided with.
    //
    // @Determine
    // "CANCELLED" to stop the collision from happening.
    // "NOPICKUP" to stop the vehicle from picking up the entity.
    //
    // -->
    @EventHandler
    public void vehicleEntityCollision(VehicleEntityCollisionEvent event) {

        dPlayer player = null;
        dNPC npc = null;

        dEntity vehicle = new dEntity(event.getVehicle());
        dEntity entity = new dEntity(event.getEntity());

        Map<String, dObject> context = new HashMap<String, dObject>();
        context.put("vehicle", vehicle);
        context.put("entity", entity.getDenizenObject());

        if (entity.isCitizensNPC()) npc = entity.getDenizenNPC();
        else if (entity.isPlayer()) player = entity.getDenizenPlayer();

        List<String> events = new ArrayList<String>();
        events.add("vehicle collides with entity");
        events.add("vehicle collides with " + entity.identifyType());
        events.add(vehicle.identifyType() + " collides with entity");
        events.add(vehicle.identifyType() + " collides with " + entity.identifyType());

        String determination = BukkitWorldScriptHelper.doEvents(events, npc, player, context, true);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
        if (determination.toUpperCase().startsWith("NOPICKUP"))
            event.setPickupCancelled(true);
    }
}
