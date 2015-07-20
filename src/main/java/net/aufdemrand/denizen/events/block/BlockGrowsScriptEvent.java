package net.aufdemrand.denizen.events.block;

import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dMaterial;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockGrowEvent;

import java.util.HashMap;

public class BlockGrowsScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // block grows (in <area>)
    // <block> grows (in <area>)
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
        String block = CoreUtilities.getXthArg(0, lower);
        dMaterial mat = dMaterial.valueOf(block);
        return cmd.equals("grows")
                && (block.equals("block") || (mat != null && !mat.isStructure()));
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String mat = CoreUtilities.getXthArg(0, lower);
        if (!tryMaterial(material, mat)) {
            return false;
        }
        if (material.isStructure()) {
            return false;
        }
        return runInCheck(scriptContainer, s, lower, location);
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
        material = dMaterial.getMaterialFrom(event.getNewState().getType(), event.getNewState().getData().getData());
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
    }
}
