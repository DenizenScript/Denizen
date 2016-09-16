package net.aufdemrand.denizen.events.entity;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleDamageEvent;

public class VehicleDamagedScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // vehicle damaged (in <area>)
    // <vehicle> damaged (in <area>)
    // entity damages vehicle (in <area>)
    // <entity> damages vehicle (in <area>)
    // entity damages <vehicle> (in <area>)
    // <entity> damages <vehicle> (in <area>)
    //
    // @Regex ^on [^\s]+ damages [^\s]+( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
    //
    // @Cancellable true
    //
    // @Triggers when a vehicle is damaged.
    //
    // @Context
    // <context.vehicle> returns the dEntity of the vehicle.
    // <context.entity> returns the dEntity of the attacking entity.
    //
    // @Determine
    // Element(Decimal) to set the value of the damage received by the vehicle.
    //
    // @NPC when the entity that damaged the vehicle is a player.
    //
    // @NPC when the entity that damaged the vehicle is an NPC.
    //
    // -->

    public VehicleDamagedScriptEvent() {
        instance = this;
    }

    public static VehicleDamagedScriptEvent instance;
    public dEntity vehicle;
    public dEntity entity;
    private double damage;
    public VehicleDamageEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String cmd = CoreUtilities.getXthArg(1, lower);
        return cmd.equals("damaged") || cmd.equals("damages");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String cmd = CoreUtilities.getXthArg(1, lower);
        String veh = cmd.equals("damaged") ? CoreUtilities.getXthArg(0, lower) : CoreUtilities.getXthArg(2, lower);
        String ent = cmd.equals("damages") ? CoreUtilities.getXthArg(0, lower) : "";

        if (!tryEntity(vehicle, veh)) {
            return false;
        }
        if (!ent.isEmpty() && entity != null && !tryEntity(entity, ent)) {
            return false;
        }

        if (!runInCheck(scriptContainer, s, lower, vehicle.getLocation())) {
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "VehicleDamaged";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        VehicleDamageEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        if (aH.matchesDouble(determination)) {
            damage = aH.getDoubleFrom(determination);
            return true;
        }
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
    public void onVehicleDestroyed(VehicleDamageEvent event) {
        vehicle = new dEntity(event.getVehicle());
        entity = event.getAttacker() != null ? new dEntity(event.getAttacker()) : null;
        damage = event.getDamage();
        this.event = event;
        cancelled = event.isCancelled();
        fire();
        event.setDamage(damage);
        event.setCancelled(cancelled);
    }
}
