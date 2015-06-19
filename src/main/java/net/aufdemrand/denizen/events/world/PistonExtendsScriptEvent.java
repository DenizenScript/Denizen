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
import org.bukkit.event.block.BlockPistonExtendEvent;

import java.util.HashMap;

public class PistonExtendsScriptEvent extends ScriptEvent implements Listener {

    // <--[event]
    // @Events
    // piston extends
    // <block> extends
    //
    // @Cancellable true
    //
    // @Triggers when a piston extends.
    //
    // @Context
    // <context.location> returns the dLocation of the piston.
    // <context.material> returns the dMaterial of the piston.
    // <context.length> returns an Element of the number of blocks that will be moved by the piston.
    // <context.blocks> returns a dList of all block locations about to be moved.
    // <context.sticky> returns an Element of whether the piston is sticky.
    // <context.relative> returns a dLocation of the block in front of the piston.
    //
    // -->

    public PistonExtendsScriptEvent() {
        instance = this;
    }
    public static PistonExtendsScriptEvent instance;
    public dLocation location;
    public dMaterial material;
    public Element length;
    public dList blocks;
    public Element sticky;
    public dLocation relative;
    public BlockPistonExtendEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String mat = CoreUtilities.getXthArg(0, lower);
        return lower.contains("piston extends")
                || (lower.equals(mat + " extends") && dMaterial.matches(mat));
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
        return "PistonExtends";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        BlockPistonExtendEvent.getHandlerList().unregister(this);
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
        context.put("length", length);
        return context;
    }

    @EventHandler
    public void onPistonExtends(BlockPistonExtendEvent event) {
        location = new dLocation(event.getBlock().getLocation());
        material = dMaterial.getMaterialFrom(event.getBlock().getType(), event.getBlock().getData());
        sticky = new Element(event.isSticky() ? "true": "false");
        relative = new dLocation(event.getBlock().getRelative(event.getDirection()).getLocation());
        blocks = new dList();
        for (Block block: event.getBlocks()) {
            blocks.add(new dLocation(block.getLocation()).identify());
        }
        length = new Element(blocks.size());
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
    }
}
