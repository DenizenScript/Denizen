package com.denizenscript.denizen.events.block;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;

public class BlockFallsScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // block falls
    // <material> falls
    //
    // @Regex ^on [^\s]+ falls$
    //
    // @Group Block
    //
    // @Switch in:<area> to only process the event if it occurred within a specified area.
    //
    // @Cancellable true
    //
    // @Triggers when a block falls.
    //
    // @Context
    // <context.location> returns the location of the block.
    // <context.entity> returns the entity of the block that fell.
    //
    // -->

    public BlockFallsScriptEvent() {
        instance = this;
    }

    public static BlockFallsScriptEvent instance;

    public LocationTag location;
    public MaterialTag material;
    public EntityChangeBlockEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        return path.eventArgLowerAt(1).equals("falls");
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, location)) {
            return false;
        }

        String mat = path.eventArgLowerAt(0);
        return tryMaterial(material, mat);
    }

    @Override
    public String getName() {
        return "BlockFalls";
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("location")) {
            return location;
        }
        else if (name.equals("entity")) {
            return new EntityTag(event.getEntity());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onBlockFalls(EntityChangeBlockEvent event) {
        location = new LocationTag(event.getBlock().getLocation());
        material = new MaterialTag(event.getBlock());
        this.event = event;
        fire(event);
    }
}
