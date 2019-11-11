package com.denizenscript.denizen.events.block;

import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;

public class PistonExtendsScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // piston extends
    // <block> extends
    //
    // @Regex ^on [^\s]+ extends$
    //
    // @Group Block
    //
    // @Switch in:<area> to only process the event if it occurred within a specified area.
    //
    // @Cancellable true
    //
    // @Triggers when a piston extends.
    //
    // @Context
    // <context.location> returns the LocationTag of the piston.
    // <context.material> returns the MaterialTag of the piston.
    // <context.length> returns an ElementTag of the number of blocks that will be moved by the piston.
    // <context.blocks> returns a ListTag of all block locations about to be moved.
    // <context.sticky> returns an ElementTag of whether the piston is sticky.
    // <context.relative> returns a LocationTag of the block in front of the piston.
    //
    // -->

    public PistonExtendsScriptEvent() {
        instance = this;
    }

    public static PistonExtendsScriptEvent instance;
    public LocationTag location;
    public MaterialTag material;
    public BlockPistonExtendEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        return path.eventArgLowerAt(1).equals("extends");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String mat = path.eventArgLowerAt(0);
        if (!runInCheck(path, location)) {
            return false;
        }
        if (!mat.equals("piston") && !tryMaterial(material, mat)) {
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return "PistonExtends";
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("location")) {
            return location;
        }
        else if (name.equals("material")) {
            return material;
        }
        else if (name.equals("sticky")) {
            return new ElementTag(event.isSticky());
        }
        else if (name.equals("relative")) {
            return new LocationTag(event.getBlock().getRelative(event.getDirection()).getLocation());
        }
        else if (name.equals("blocks")) {
            ListTag blocks = new ListTag();
            for (Block block : event.getBlocks()) {
                blocks.addObject(new LocationTag(block.getLocation()));
            }
            return blocks;
        }
        else if (name.equals("length")) {
            return new ElementTag(event.getBlocks().size());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPistonExtends(BlockPistonExtendEvent event) {
        location = new LocationTag(event.getBlock().getLocation());
        material = new MaterialTag(event.getBlock());
        this.event = event;
        fire(event);
    }
}
