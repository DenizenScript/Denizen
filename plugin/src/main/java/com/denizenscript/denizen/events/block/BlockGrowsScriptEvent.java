package com.denizenscript.denizen.events.block;

import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockGrowEvent;

public class BlockGrowsScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // block grows
    // <block> grows
    //
    // @Regex ^on [^\s]+ grows$
    //
    // @Group Block
    //
    // @Switch in <area>
    //
    // @Cancellable true
    //
    // @Triggers when a block grows naturally in the world, EG, when wheat, sugar canes, cacti, watermelons or pumpkins grow.
    // @Context
    // <context.location> returns the LocationTag of the block that grew.
    // <context.material> returns the MaterialTag of the block that grew.
    //
    // -->

    public BlockGrowsScriptEvent() {
        instance = this;
    }

    public static BlockGrowsScriptEvent instance;
    public LocationTag location;
    public MaterialTag material;
    public BlockGrowEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!path.eventArgLowerAt(1).equals("grows")) {
            return false;
        }
        String block = path.eventArgLowerAt(0);
        if (block.equals("block")) {
            return true;
        }
        MaterialTag mat = MaterialTag.valueOf(block);
        return mat != null && !mat.isStructure();
    }

    @Override
    public boolean matches(ScriptPath path) {
        String mat = path.eventArgLowerAt(0);
        if (!tryMaterial(material, mat)) {
            return false;
        }
        if (material.isStructure()) {
            return false;
        }
        return runInCheck(path, location);
    }

    @Override
    public String getName() {
        return "BlockGrows";
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("location")) {
            return location;
        }
        else if (name.equals("material")) {
            return material;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onBlockGrows(BlockGrowEvent event) {
        location = new LocationTag(event.getBlock().getLocation());
        material = new MaterialTag(event.getNewState());
        this.event = event;
        fire(event);
    }
}
