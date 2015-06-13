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
import org.bukkit.event.entity.EntityChangeBlockEvent;

import java.util.HashMap;

public class BlockFallsScriptEvent extends ScriptEvent implements Listener {

    // <--[event]
    // @Events
    // block falls
    // <material> falls
    //
    // @Cancellable true
    //
    // @Triggers when a block falls.
    //
    // @Context
    // <context.location> returns the location of the block.
    //
    // -->

    public BlockFallsScriptEvent() {
        instance = this;
    }

    public static BlockFallsScriptEvent instance;

    public dLocation location;
    public EntityChangeBlockEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String mat = CoreUtilities.getXthArg(0, lower);
        return lower.equals("block falls")
                || (lower.equals(mat + " falls") && dMaterial.matches(mat));
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return lower.equals("block falls")
                || CoreUtilities.getXthArg(0,lower).equals(event.getBlock().getType().name().toLowerCase());
    }

    @Override
    public String getName() {
        return "BlockFalls";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        EntityChangeBlockEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public HashMap<String, dObject> getContext() {
        HashMap<String, dObject> context = super.getContext();
        context.put("location", location);
        return context;
    }

    @EventHandler
    public void onBlockFalls(EntityChangeBlockEvent event) {
        location = new dLocation(event.getBlock().getLocation());
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
    }
}
