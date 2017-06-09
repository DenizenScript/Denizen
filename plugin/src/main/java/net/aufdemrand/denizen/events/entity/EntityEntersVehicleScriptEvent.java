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
import org.bukkit.event.vehicle.VehicleEnterEvent;

public class EntityEntersVehicleScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // entity enters vehicle (in <area>)
    // entity enters <vehicle> (in <area>)
    // <entity> enters vehicle (in <area>)
    // <entity> enters <vehicle> (in <area>)
    //
    // @Regex ^on [^\s]+ enters [^\s]+( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
    //
    // @Cancellable true
    //
    // @Triggers when an entity enters a vehicle.
    //
    // @Context
    // <context.vehicle> returns the dEntity of the vehicle.
    // <context.entity> returns the dEntity of the exiting entity.
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
    public dEntity vehicle;
    public dEntity entity;
    public VehicleEnterEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return CoreUtilities.getXthArg(1, lower).equals("enters")
                // TODO: Ideally, match valid entity types
                && !CoreUtilities.getXthArg(2, lower).equals("biome");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);

        if (!tryEntity(entity, CoreUtilities.getXthArg(0, lower))
                || !tryEntity(vehicle, CoreUtilities.getXthArg(2, lower))) {
            return false;
        }

        if (!runInCheck(scriptContainer, s, lower, vehicle.getLocation())) {
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "EntityEntersVehicle";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        VehicleEnterEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
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
        return super.getContext(name);
    }

    @EventHandler
    public void onEntityEntersVehicle(VehicleEnterEvent event) {
        vehicle = new dEntity(event.getVehicle());
        entity = new dEntity(event.getEntered());
        this.event = event;
        cancelled = event.isCancelled();
        fire();
        event.setCancelled(cancelled);
    }
}
