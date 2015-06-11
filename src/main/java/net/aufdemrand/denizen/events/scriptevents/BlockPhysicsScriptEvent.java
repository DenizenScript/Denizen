package net.aufdemrand.denizen.events.scriptevents;

import net.aufdemrand.denizen.objects.dCuboid;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dMaterial;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.events.ScriptEvent;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;

import java.util.HashMap;

public class BlockPhysicsScriptEvent extends ScriptEvent implements Listener {

    // <--[event]
    // @Events
    // block physics (in <notable cuboid>)
    // <material> physics (in <notable cuboid>)
    //
    // @Warning This event may fire very rapidly.
    //
    // @Cancellable true
    //
    // @Triggers when a block's physics update.
    //
    // @Context
    // <context.location> returns a dLocation of the block the physics is affecting.
    // <context.new_material> returns a dMaterial of what the block is becoming.
    //
    // -->

    public BlockPhysicsScriptEvent() {
        instance = this;
    }
    public static BlockPhysicsScriptEvent instance;
    public dLocation location;
    public dMaterial new_material;
    public BlockPhysicsEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return lower.contains("physics");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);

        if (lower.equals("block physics")) {
            return true;
        }

        Boolean blockvalid = true;
        if (!lower.startsWith(new_material.identifySimple())) {
            blockvalid = false;
        }

        Boolean cuboidvalid = true;
        if (lower.contains(" in ")) {
            dList cuboids = new dList();
            for (dCuboid cuboid: dCuboid.getNotableCuboidsContaining(location)) {
                cuboids.add(cuboid.identify());
            }

            if (!cuboids.contains(lower.substring(lower.lastIndexOf("in ") + 3))) {
                cuboidvalid = false;
            }
        }

        return blockvalid && cuboidvalid;
    }

    @Override
    public String getName() {
        return "ChunkLoads";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        BlockPhysicsEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public HashMap<String, dObject> getContext() {
        HashMap<String, dObject> context = super.getContext();
        context.put("location", location);
        context.put("new_material", new_material); // Deprecated in favor of context.chunk.world
        return context;
    }

    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent event) {
        location = new dLocation(event.getBlock().getLocation());
        new_material = dMaterial.getMaterialFrom(location.getBlock().getType(), location.getBlock().getData());
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
    }
}
