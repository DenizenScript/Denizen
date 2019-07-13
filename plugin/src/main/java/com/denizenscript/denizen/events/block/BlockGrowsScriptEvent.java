package com.denizenscript.denizen.events.block;

import com.denizenscript.denizen.objects.dLocation;
import com.denizenscript.denizen.objects.dMaterial;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.dObject;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
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
    // @Switch in <area>
    //
    // @Cancellable true
    //
    // @Triggers when a block grows naturally in the world, EG, when wheat, sugar canes, cacti, watermelons or pumpkins grow.
    // @Context
    // <context.location> returns the dLocation of the block that grew.
    // <context.material> returns the dMaterial of the block that grew.
    //
    // -->

    public BlockGrowsScriptEvent() {
        instance = this;
    }

    public static BlockGrowsScriptEvent instance;
    public dLocation location;
    public dMaterial material;
    public BlockGrowEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String cmd = CoreUtilities.getXthArg(1, lower);
        if (!cmd.equals("grows")) {
            return false;
        }
        String block = CoreUtilities.getXthArg(0, lower);
        if (block.equals("block")) {
            return true;
        }
        dMaterial mat = dMaterial.valueOf(block);
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
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public dObject getContext(String name) {
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
        location = new dLocation(event.getBlock().getLocation());
        material = new dMaterial(event.getNewState());
        this.event = event;
        fire(event);
    }
}
