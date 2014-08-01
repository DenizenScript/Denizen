package net.aufdemrand.denizen.events.core;

import net.aufdemrand.denizen.events.EventManager;
import net.aufdemrand.denizen.events.SmartEvent;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BlockPhysicsSmartEvent implements SmartEvent, Listener {


    ///////////////////
    // SMARTEVENT METHODS
    ///////////////


    @Override
    public boolean shouldInitialize(Set<String> events) {

        // Loop through event names from loaded world script events
        for (String event : events) {

            // Use a regex pattern to narrow down matches
            Matcher m = Pattern.compile("on (m@)?\\w+ physics( in (cu@)?\\w+)?", Pattern.CASE_INSENSITIVE)
                    .matcher(event);

            if (m.matches()) {
                // Event names are simple enough to just go ahead and pass on any match.
                return true;
            }
        }
        // No matches at all, just fail.
        return false;
    }


    @Override
    public void _initialize() {
        DenizenAPI.getCurrentInstance().getServer().getPluginManager()
                .registerEvents(this, DenizenAPI.getCurrentInstance());
        dB.log("Loaded Block Physics SmartEvent.");
    }


    @Override
    public void breakDown() {
        BlockPhysicsEvent.getHandlerList().unregister(this);
    }

    //////////////
    //  MECHANICS
    ///////////

    // <--[event]
    // @Events
    // block physics (in <notable cuboid>)
    // <material> physics (in <notable cuboid>)
    //
    // @Warning This event may fire very rapidly.
    //
    // @Regex on (m@)?\w+ physics( in (cu@)?\w+)?
    //
    // @Triggers when a block's physics update.
    // @Context
    // <context.location> returns a dLocation of the block the physics is affecting.
    // <context.new_material> returns a dMaterial of what the block is becoming.
    //
    // @Determine
    // "CANCELLED" to move the player back off the block (useless if the player jumped onto it!)
    //
    // -->
    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent event) {
        List<String> events = new ArrayList<String>();
        Map<String, dObject> context = new HashMap<String, dObject>();
        // Add the default location context + default event
        dLocation block = new dLocation(event.getBlock().getLocation());
        dMaterial mat = dMaterial.getMaterialFrom(block.getBlock().getType(), block.getBlock().getData());
        context.put("location", block);
        context.put("new_material", dMaterial.getMaterialFrom(event.getChangedType()));
        events.add("block physics");
        events.add(mat.identifySimple() + " physics");
        // Add all relevant cuboids
        List<dCuboid> cuboids = dCuboid.getNotableCuboidsContaining(block);
        dList cuboid_context = new dList();
        for (dCuboid cuboid : cuboids) {
            events.add("block physics in " + cuboid.identifySimple());
            events.add(mat.identifySimple() + " physics in " + cuboid.identifySimple());
        }
        // Add in cuboids context, with either the cuboids or an empty list
        context.put("cuboids", cuboid_context);
        // Fire event
        String Determination = EventManager.doEvents(events, null, null, context, true);
        if (Determination.equalsIgnoreCase("CANCELLED"))
            event.setCancelled(true);
    }
}
