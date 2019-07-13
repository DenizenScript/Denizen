package com.denizenscript.denizen.events.world;

import com.denizenscript.denizen.objects.dLocation;
import com.denizenscript.denizen.objects.dMaterial;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.dList;
import com.denizenscript.denizencore.objects.dObject;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonRetractEvent;

public class PistonRetractsScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // piston retracts
    // <block> retracts
    //
    // @Regex ^on [^\s]+ retracts$
    // @Switch in <area>
    //
    // @Cancellable true
    //
    // @Triggers when a piston retracts.
    //
    // @Context
    // <context.location> returns the dLocation of the piston.
    // <context.retract_location> returns the new dLocation of the block that
    //                            will be moved by the piston if it is sticky.
    // <context.blocks> returns a dList of all block locations about to be moved.
    // <context.material> returns the dMaterial of the piston.
    // <context.sticky> returns an Element of whether the piston is sticky.
    // <context.relative> returns a dLocation of the block in front of the piston.
    //
    // -->

    public PistonRetractsScriptEvent() {
        instance = this;
    }

    public static PistonRetractsScriptEvent instance;
    public dLocation location;
    public dMaterial material;
    public dLocation retract_location;
    public dList blocks;
    public Element sticky;
    public dLocation relative;
    public BlockPistonRetractEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String cmd = CoreUtilities.getXthArg(1, lower);
        return cmd.equals("retracts");

    }

    @Override
    public boolean matches(ScriptPath path) {
        String mat = path.eventArgLowerAt(0);
        return (mat.equals("piston") || tryMaterial(material, mat))
                && runInCheck(path, location);
    }

    @Override
    public String getName() {
        return "PistonRetracts";
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
        else if (name.equals("sticky")) {
            return sticky;
        }
        else if (name.equals("relative")) {
            return relative;
        }
        else if (name.equals("blocks")) {
            return blocks;
        }
        else if (name.equals("retract_location")) {
            return retract_location;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPistonRetracts(BlockPistonRetractEvent event) {
        location = new dLocation(event.getBlock().getLocation());
        material = new dMaterial(event.getBlock());
        sticky = new Element(event.isSticky() ? "true" : "false");
        relative = new dLocation(event.getBlock().getRelative(event.getDirection().getOppositeFace()).getLocation());
        blocks = new dList();
        for (Block block : event.getBlocks()) {
            blocks.add(new dLocation(block.getLocation()).identify());
        }
        retract_location = new dLocation(event.getBlock().getRelative(event.getDirection().getOppositeFace(), 2).getLocation());
        this.event = event;
        fire(event);
    }
}
