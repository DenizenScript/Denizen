package com.denizenscript.denizen.events.vehicle;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;

public class VehicleCollidesEntityScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // vehicle collides with entity
    // vehicle collides with <entity>
    // <vehicle> collides with entity
    // <vehicle> collides with <entity>
    //
    // @Group Vehicle
    //
    // @Location true
    //
    // @Switch type:<entity> to only process the event if the colliding entity matches the EntityTag matcher input.
    //
    // @Cancellable true
    //
    // @Triggers when a vehicle collides with an entity.
    //
    // @Context
    // <context.vehicle> returns the EntityTag of the vehicle.
    // <context.entity> returns the EntityTag of the entity the vehicle has collided with.
    // <context.pickup> returns whether the vehicle can pick up the entity.
    //
    // @Determine
    // "PICKUP:<ElementTag(Boolean)>" to set whether the vehicle is allowed to pick up the entity or not.
    //
    // @Player when a vehicle collides with a player.
    //
    // @NPC when a vehicle collides with an NPC.
    //
    // @Example
    // on vehicle collides with entity:
    //
    // @Example
    // on minecart collides with sheep:
    //
    // @Example
    // # This example disambiguates this event from the "projectile collides with entity" event for specific entity types.
    // on vehicle collides with entity type:creeper:
    // - announce "A <context.vehicle.entity_type> collided with a creeper!"
    //
    // -->

    public VehicleCollidesEntityScriptEvent() {
        registerCouldMatcher("<vehicle> collides with <entity>");
        registerSwitches("type");
    }

    public VehicleEntityCollisionEvent event;
    public EntityTag vehicle;
    public EntityTag entity;

    @Override
    public boolean matches(ScriptPath path) {
        if (!path.tryArgObject(0, vehicle)) {
            return false;
        }
        if (!path.tryArgObject(3, entity)) {
            return false;
        }
        if (!runInCheck(path, vehicle.getLocation())) {
            return false;
        }
        if (!path.tryObjectSwitch("type", entity)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        if (determinationObj instanceof ElementTag) {
            String lower = CoreUtilities.toLowerCase(determinationObj.toString());
            if (lower.startsWith("pickup:")) {
                event.setPickupCancelled(!(new ElementTag(lower.substring("pickup:".length())).asBoolean()));
                return true;
            }
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(entity);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "vehicle": return vehicle;
            case "entity": return entity.getDenizenObject();
            case "pickup": return new ElementTag(!event.isPickupCancelled());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onVehicleCollidesEntity(VehicleEntityCollisionEvent event) {
        entity = new EntityTag(event.getEntity());
        vehicle = new EntityTag(event.getVehicle());
        this.event = event;
        fire(event);
    }
}
