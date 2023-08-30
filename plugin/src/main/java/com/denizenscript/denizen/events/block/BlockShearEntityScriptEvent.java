package com.denizenscript.denizen.events.block;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockShearEntityEvent;

public class BlockShearEntityScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // <block> shears <entity>
    //
    // @Group Block
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when a dispenser shears a nearby sheep.
    //
    // @Context
    // <context.location> returns the LocationTag of the dispenser.
    // <context.tool> returns the ItemTag of the item used to shear the entity.
    // <context.entity> returns the EntityTag of the sheared entity.
    //
    // -->

    public BlockShearEntityScriptEvent() {
        registerCouldMatcher("<block> shears <entity>");
    }

    public LocationTag location;
    public EntityTag entity;
    public BlockShearEntityEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, location)) {
            return false;
        }
        if (!path.tryArgObject(0, location)) {
            return false;
        }
        if (!path.tryArgObject(2, entity)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "location" -> location;
            case "tool" -> new ItemTag(event.getTool());
            case "entity" -> entity;
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void onShear(BlockShearEntityEvent event) {
        location = new LocationTag(event.getBlock().getLocation());
        entity = new EntityTag(event.getEntity());
        this.event = event;
        fire(event);
    }
}
