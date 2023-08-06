package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.objects.ObjectTag;
import io.papermc.paper.event.entity.EntityMoveEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class EntityStepsOnScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // entity steps on block
    // <entity> steps on <material>
    //
    // @Location true
    //
    // @Group Paper
    //
    // @Plugin Paper
    //
    // @Warning This event may fire very rapidly.
    //
    // @Cancellable true
    //
    // @Triggers when a non-player entity steps onto a specific block material. For players, use <@link event player steps on block>.
    //
    // @Context
    // <context.entity> returns an EntityTag of the entity stepping onto the block.
    // <context.location> returns a LocationTag of the block the entity is stepping on.
    // <context.previous_location> returns a LocationTag of where the entity was before stepping onto the block.
    // <context.new_location> returns a LocationTag of where the entity is now.
    //
    // @Example
    // # Announce the name of the entity stepping on the block and the material of block.
    // on entity steps on block:
    // - announce "<context.entity.name> stepped on a <context.location.material.name>!"
    //
    // @Example
    // # Announce the material of the block a sheep has stepped on.
    // on sheep steps on block:
    // - announce "A sheep has stepped on a <context.location.material.name>!"
    //
    // @Example
    // # Announce that a sheep has stepped on a diamond block.
    // on sheep steps on diamond_block:
    // - announce "A sheep has stepped on a diamond block! Must be a wealthy sheep!"
    // -->

    public EntityStepsOnScriptEvent() {
        registerCouldMatcher("<entity> steps on <block>");
    }

    public EntityMoveEvent event;
    public EntityTag entity;
    public LocationTag location;
    public MaterialTag material;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!super.couldMatch(path)) {
            return false;
        }
        if (path.eventLower.startsWith("player")) {
            return false;
        }
        return true;
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, location)) {
            return false;
        }
        if (!path.tryArgObject(0, entity)) {
            return false;
        }
        if (!path.tryArgObject(3, material)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "entity" -> entity;
            case "location" -> location;
            case "previous_location" -> new LocationTag(event.getFrom());
            case "new_location" -> new LocationTag(event.getTo());
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void entityStepsOnBlockEvent(EntityMoveEvent event) {
        if (!event.hasChangedBlock()) {
            return;
        }
        location = new LocationTag(event.getTo().clone().subtract(0.0, 0.05, 0.0));
        if (!Utilities.isLocationYSafe(location)) {
            return;
        }
        material = new MaterialTag(location.getBlock());
        entity = new EntityTag(event.getEntity());
        this.event = event;
        fire(event);
    }
}
