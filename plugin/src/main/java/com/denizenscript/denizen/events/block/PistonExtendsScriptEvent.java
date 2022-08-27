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
    // @Group Block
    //
    // @Location true
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
    // <context.direction> returns a vector location of the direction that blocks will move.
    //
    // -->

    public PistonExtendsScriptEvent() {
        registerCouldMatcher("piston extends");
        registerCouldMatcher("<block> extends");
    }

    public LocationTag location;
    public MaterialTag material;
    public BlockPistonExtendEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        String mat = path.eventArgLowerAt(0);
        if (!runInCheck(path, location)) {
            return false;
        }
        if (!mat.equals("piston") && !material.tryAdvancedMatcher(mat)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "location": return location;
            case "material": return material;
            case "sticky": return new ElementTag(event.isSticky());
            case "direction": return new LocationTag(event.getDirection().getDirection());
            case "relative": return new LocationTag(event.getBlock().getRelative(event.getDirection()).getLocation()); // Silently deprecated
            case "blocks": {
                ListTag blocks = new ListTag();
                for (Block block : event.getBlocks()) {
                    blocks.addObject(new LocationTag(block.getLocation()));
                }
                return blocks;
            }
            case "length": return new ElementTag(event.getBlocks().size());
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
