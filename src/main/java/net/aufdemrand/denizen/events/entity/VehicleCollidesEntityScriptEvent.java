package net.aufdemrand.denizen.events.entity;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;

public class VehicleCollidesEntityScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // vehicle collides with entity (in <area>)
    // vehicle collides with <entity> (in <area>)
    // <vehicle> collides with entity (in <area>)
    // <vehicle> collides with <entity> (in <area>)
    //
    // @Regex ^on [^\s]+ collides with [^\s]+( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
    //
    // @Cancellable true
    //
    // @Triggers when a vehicle collides with an entity.
    //
    // @Context
    // <context.vehicle> returns the dEntity of the vehicle.
    // <context.entity> returns the dEntity of the entity the vehicle has collided with.
    // <context.pickup> returns whether the vehicle can pick up the entity.
    //
    // @Determine
    // "PICKUP:TRUE" to allow the vehicle to pick up the entity.
    // "PICKUP:FALSE" to stop the vehicle from picking up the entity.
    //
    // @Player when a vehicle collides with a player.
    //
    // @NPC when a vehicle collides with an NPC.
    //
    // -->

    public VehicleCollidesEntityScriptEvent() {
        instance = this;
    }

    public static VehicleCollidesEntityScriptEvent instance;

    public dEntity vehicle;
    public dEntity entity;
    private Boolean pickup_cancel;
    public VehicleEntityCollisionEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return lower.contains("collides with");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String veh = CoreUtilities.getXthArg(0, lower);
        if (!vehicle.matchesEntity(veh)) {
            return false;
        }

        String ent = CoreUtilities.getXthArg(3, lower);
        if (!entity.matchesEntity(ent)) {
            return false;
        }

        return runInCheck(scriptContainer, s, lower, vehicle.getLocation());
    }

    @Override
    public String getName() {
        return "VehicleCollidesEntity";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        VehicleEntityCollisionEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        aH.Argument arg = aH.Argument.valueOf(determination);
        if (arg.matchesPrefix("pickup")) {
            pickup_cancel = !arg.asElement().asBoolean();
            return true;
        }
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(entity.isPlayer() ? entity.getDenizenPlayer() : null,
                entity.isCitizensNPC() ? entity.getDenizenNPC() : null);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("vehicle")) {
            return vehicle;
        }
        else if (name.equals("entity")) {
            return entity;
        }
        else if (name.equals("pickup")) {
            return new Element(!pickup_cancel);
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onVehicleCollidesEntity(VehicleEntityCollisionEvent event) {
        entity = new dEntity(event.getEntity());
        vehicle = new dEntity(event.getVehicle());
        pickup_cancel = event.isPickupCancelled();
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
        event.setPickupCancelled(pickup_cancel);
    }
}
