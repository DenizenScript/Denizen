package net.aufdemrand.denizen.events.scriptevents;

import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dMaterial;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.events.ScriptEvent;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockGrowEvent;

import java.util.HashMap;

public class BlockGrowsScriptEvent extends ScriptEvent implements Listener {

    // <--[event]
    // @Events
    // block grows
    // <block> grows
    //
    // @Cancellable true
    //
    // @Triggers when a block grows naturally in the world,
    //           e.g. when wheat, sugar canes, cacti,
    //           watermelons or pumpkins grow
    // @Context
    // <context.location> returns the dLocation the block.
    // <context.material> returns the dMaterial of the block.
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
        String mat = CoreUtilities.getXthArg(0, lower);
        return lower.contains("block grows")
                || (lower.equals(mat + " grows") && dMaterial.matches(mat));
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String mat = CoreUtilities.getXthArg(0, lower);
        return mat.equals("block")
                || (material.identifySimpleNoIdentifier().toLowerCase().equals(mat));
    }

    @Override
    public String getName() {
        return "BlockGrows";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        BlockGrowEvent.getHandlerList().unregister(this);
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
        return context;
    }

    @EventHandler
    public void onBlockGrows(BlockGrowEvent event) {
        location = new dLocation(event.getBlock().getLocation());
        material = dMaterial.getMaterialFrom(event.getBlock().getType(), event.getBlock().getData());
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
    }
}
