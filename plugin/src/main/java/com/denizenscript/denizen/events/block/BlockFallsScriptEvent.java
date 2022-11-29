package com.denizenscript.denizen.events.block;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;

public class BlockFallsScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // <block> falls
    //
    // @Group Block
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when a block begins to fall. Generic form "block falls" (with a material) also fires when the block lands.
    //
    // @Context
    // <context.location> returns the location of the block.
    // <context.entity> returns the entity of the block that fell.
    // <context.old_material> returns the material that was at the location (eg 'sand' when falling, or 'air' when landing).
    // <context.new_material> returns the material that will be at the location (eg 'air' when falling, or 'sand' when landing).
    //
    // -->

    public BlockFallsScriptEvent() {
        registerCouldMatcher("<block> falls");
    }


    public LocationTag location;
    public MaterialTag material;
    public EntityChangeBlockEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, location)) {
            return false;
        }
        if (!path.tryArgObject(0, material)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "location": return location;
            case "entity": return new EntityTag(event.getEntity()).getDenizenObject();
            case "old_material": return material;
            case "new_material": return new MaterialTag(event.getBlockData());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onBlockFalls(EntityChangeBlockEvent event) {
        if (event.getEntityType() != EntityType.FALLING_BLOCK) {
            return;
        }
        location = new LocationTag(event.getBlock().getLocation());
        material = new MaterialTag(event.getBlock());
        this.event = event;
        fire(event);
    }
}
