package net.aufdemrand.denizen.events.world;

import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dMaterial;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.events.ScriptEvent;
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

import java.util.HashMap;

public class PistonRetractsScriptEvent extends ScriptEvent implements Listener {

    // <--[event]
    // @Events
    // piston retracts
    // <block> retracts
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
        String mat = CoreUtilities.getXthArg(0, lower);
        return lower.contains("piston retracts")
                || (lower.equals(mat + " retracts") && dMaterial.matches(mat));
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String mat = CoreUtilities.getXthArg(0, lower);
        return mat.equals("piston")
                || (material.identifySimpleNoIdentifier().toLowerCase().equals(mat));
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
    public HashMap<String, dObject> getContext() {
        HashMap<String, dObject> context = super.getContext();
        context.put("location", location);
        context.put("material", material);
        context.put("sticky", sticky);
        context.put("relative", relative);
        context.put("blocks", blocks);
        context.put("retract_location", retract_location);
        return context;
    }

    @EventHandler
    public void onPistonRetracts(BlockPistonRetractEvent event) {
        location = new dLocation(event.getBlock().getLocation());
        material = dMaterial.getMaterialFrom(event.getBlock().getType(), event.getBlock().getData());
        sticky = new Element(event.isSticky() ? "true": "false");
        relative = new dLocation(event.getBlock().getRelative(event.getDirection()).getLocation());
        blocks = new dList();
        for (Block block: event.getBlocks()) {
            blocks.add(new dLocation(block.getLocation()).identify());
        }
        retract_location = new dLocation(event.getBlock().getRelative(event.getDirection(), 2).getLocation());
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
    }
}
