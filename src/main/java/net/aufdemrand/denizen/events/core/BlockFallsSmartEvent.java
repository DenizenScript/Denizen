package net.aufdemrand.denizen.events.core;

import net.aufdemrand.denizen.events.EventManager;
import net.aufdemrand.denizen.events.SmartEvent;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dMaterial;
import net.aufdemrand.denizen.objects.dObject;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BlockFallsSmartEvent implements SmartEvent, Listener {


    ///////////////////
    // SMARTEVENT METHODS
    ///////////////


    @Override
    public boolean shouldInitialize(Set<String> events) {

        for (String event : events) {

            Matcher m = Pattern.compile("on (m@)?\\w+ falls", Pattern.CASE_INSENSITIVE)
                    .matcher(event);

            if (m.matches()) {
                // Straight-forward enough, just pass from any match.
                return true;
            }
        }
        return false;
    }


    @Override
    public void _initialize() {
        DenizenAPI.getCurrentInstance().getServer().getPluginManager()
                .registerEvents(this, DenizenAPI.getCurrentInstance());
        dB.log("Loaded Block Falls SmartEvent.");
    }


    @Override
    public void breakDown() {
        EntityChangeBlockEvent.getHandlerList().unregister(this);
    }



    //////////////
    //  MECHANICS
    ///////////

    // <--[event]
    // @Events
    // block falls
    // <material> falls
    //
    // @Regex on (m@)?\w+ falls
    //
    // @Triggers when a block falls.
    // @Context
    // <context.location> returns the location of the block.
    //
    // @Determine
    // "CANCELLED" to stop the block from falling.
    //
    // -->
    @EventHandler
    public void onBlockPhysics(EntityChangeBlockEvent event) {
        if (event.getBlock().getType().hasGravity() // Must have gravity (sand, gravel, ...)
                && event.getBlock().getRelative(BlockFace.DOWN).getType().equals(Material.AIR)) { // Must be above air

            Map<String, dObject> context = new HashMap<String, dObject>();

            context.put("location", new dLocation(event.getBlock().getLocation()));

            String determination = EventManager.doEvents(Arrays.asList("block falls",
                    dMaterial.getMaterialFrom(event.getBlock().getType(),
                            event.getBlock().getData()).identifySimple() + " falls"), null, null, context, true);

            if (determination.equalsIgnoreCase("CANCELLED"))
                event.setCancelled(true);
        }
    }


}
