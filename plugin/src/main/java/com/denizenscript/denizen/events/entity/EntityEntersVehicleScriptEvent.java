package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.spigotmc.event.entity.EntityMountEvent;

import java.util.Arrays;
import java.util.HashSet;

public class EntityEntersVehicleScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // entity enters vehicle
    // <entity> enters <entity>
    //
    // @Group Entity
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when an entity mounts another entity.
    //
    // @Context
    // <context.vehicle> returns the EntityTag of the mounted vehicle.
    // <context.entity> returns the EntityTag of the entering entity.
    //
    // @Player when the entity that mounted the vehicle is a player.
    //
    // @NPC when the entity that mounted the vehicle is an NPC.
    //
    // -->

    public EntityEntersVehicleScriptEvent() {
        registerCouldMatcher("<entity> enters <entity>");
    }

    public EntityTag vehicle;
    public EntityTag entity;
    // TODO: 1.20.6: EntityMountEvent changed packages, might need to register in version-specific modules? or reflection?
    public EntityMountEvent event;

    public static HashSet<String> notRelevantEnterables = new HashSet<>(Arrays.asList("notable", "cuboid", "biome", "bed", "portal"));

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!super.couldMatch(path)) {
            return false;
        }
        if (notRelevantEnterables.contains(path.eventArgLowerAt(2))) {
            return false;
        }
        return true;
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!path.tryArgObject(0, entity)) {
            return false;
        }
        String vehicleLabel = path.eventArgLowerAt(2);
        if (!vehicleLabel.equals("vehicle") && !vehicle.tryAdvancedMatcher(vehicleLabel)) {
            return false;
        }
        if (!runInCheck(path, vehicle.getLocation())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(entity);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("vehicle")) {
            return vehicle.getDenizenObject();
        }
        else if (name.equals("entity")) {
            return entity.getDenizenObject();
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onEntityEntersVehicle(EntityMountEvent event) {
        vehicle = new EntityTag(event.getMount());
        entity = new EntityTag(event.getEntity());
        this.event = event;
        fire(event);
    }
}
