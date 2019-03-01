package net.aufdemrand.denizen.events.world;

import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dMaterial;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonRetractEvent;

public class PistonRetractsScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // piston retracts (in <area>)
    // <block> retracts (in <area>)
    //
    // @Regex ^on [^\s]+ retracts( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
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
        String s = path.event;
        String lower = path.eventLower;
        String mat = CoreUtilities.getXthArg(0, lower);
        return (mat.equals("piston") || tryMaterial(material, mat))
                && runInCheck(path, location);
    }

    @Override
    public String getName() {
        return "PistonRetracts";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        BlockPistonRetractEvent.getHandlerList().unregister(this);
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
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
    }
}
