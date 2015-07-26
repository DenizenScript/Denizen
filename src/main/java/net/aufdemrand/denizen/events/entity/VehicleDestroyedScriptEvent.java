package net.aufdemrand.denizen.events.entity;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleDestroyEvent;

import java.util.HashMap;

public class VehicleDestroyedScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // vehicle destroyed (in <area>)
    // <vehicle> destroyed (in <area>)
    // entity destroys vehicle (in <area>)
    // <entity> destroys vehicle (in <area>)
    // entity destroys <vehicle> (in <area>)
    // <entity> destroys <vehicle> (in <area>)
    //
    // @Regex ^on [^\s]+ destroys [^\s]+( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
    //
    // @Cancellable true
    //
    // @Triggers when an entity enters a vehicle.
    //
    // @Context
    // <context.vehicle> returns the dEntity of the vehicle.
    // <context.entity> returns the dEntity of the attacking entity.
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
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String cmd = CoreUtilities.getXthArg(1, lower);
        String veh = cmd.equals("destroyed") ? CoreUtilities.getXthArg(0, lower) : CoreUtilities.getXthArg(2, lower);
        String ent = cmd.equals("destroys") ? CoreUtilities.getXthArg(0, lower) : "";
        if (!vehicle.matchesEntity(veh)) {
            return false;
        }
        if (ent.length() > 0) {
            if (entity == null || !entity.matchesEntity(ent)) {
                return false;
            }
        }
        return runInCheck(scriptContainer, s, lower, vehicle.getLocation());
    }

    @Override
    public String getName() {
        return "VehicleDestroyed";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        VehicleDestroyEvent.getHandlerList().unregister(this);
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
    public HashMap<String, dObject> getContext() {
        HashMap<String, dObject> context = super.getContext();
        context.put("vehicle", vehicle);
        if (entity != null) {
            context.put("entity", entity);
        }
        return context;
    }

    @EventHandler
    public void onVehicleDestroyed(VehicleDestroyEvent event) {
        vehicle = new dEntity(event.getVehicle());
        entity = event.getAttacker() != null ? new dEntity(event.getAttacker()) : null;
        this.event = event;
        cancelled = event.isCancelled();
        fire();
        event.setCancelled(cancelled);
    }
}
