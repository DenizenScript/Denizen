package net.aufdemrand.denizen.events.core;

import net.aufdemrand.denizen.events.EventManager;
import net.aufdemrand.denizen.events.SmartEvent;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityInteractEvent;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class EntityInteractSmartEvent implements SmartEvent, Listener {


    ///////////////////
    // SMARTEVENT METHODS
    ///////////////


    @Override
    public boolean shouldInitialize(Set<String> events) {

        // Loop through event names from loaded world script events
        for (String event : events) {

            // Use a regex pattern to narrow down matches
            Matcher m = Pattern.compile("on \\w+ interacts with \\w+( in \\w+)?", Pattern.CASE_INSENSITIVE)
                    .matcher(event);

            if (m.matches()) {
                // Any match is sufficient
                return true;
            }
        }
        // No matches at all, so return false.
        return false;
    }


    @Override
    public void _initialize() {
        // Yay! Your event is in use! Register it here.
        DenizenAPI.getCurrentInstance().getServer().getPluginManager()
                .registerEvents(this, DenizenAPI.getCurrentInstance());
        // Record that you loaded in the debug.
        dB.log("Loaded Entity Interact SmartEvent.");
    }


    @Override
    public void breakDown() {
        // Unregister events or any other temporary links your event created in _intialize()
        EntityInteractEvent.getHandlerList().unregister(this);
    }

    //////////////
    //  MECHANICS
    ///////////

    // <--[event]
    // @Events
    // <entity> interacts with <material> (in <notable cuboid>)
    // entity interacts with <material> (in <notable cuboid>)
    // <entity> interacts with block (in <notable cuboid>)
    // entity interacts with block (in <notable cuboid>)
    //
    // @Regex on \w+ interacts with \w+( in \w+)?
    //
    // @Triggers when an entity interacts with a block (EG an arrow hits a button)
    // @Context
    // <context.location> returns a dLocation of the block being interacted with.
    // <context.cuboids> returns a dList of all cuboids the block is inside.
    // <context.entity> returns a dEntity of the entity doing the interaction.
    //
    // @Determine
    // "CANCELLED" to block the interaction
    //
    // -->
    @EventHandler
    public void onEntityInteract(EntityInteractEvent event) {
        Map<String, dObject> context = new HashMap<String, dObject>();
        List<String> events = new ArrayList<String>();
        dEntity entity = new dEntity(event.getEntity());
        dLocation location = new dLocation(event.getBlock().getLocation());
        dMaterial block = dMaterial.getMaterialFrom(event.getBlock().getType(), event.getBlock().getData());
        context.put("entity", entity);
        context.put("location", location);
        events.add("entity interacts with block");
        events.add("entity interacts with " + block.identifySimple());
        events.add(entity.identifySimple() + " interacts with " + block.identifySimple());
        events.add(entity.identifyType() + " interacts with " + block.identifySimple());
        // Add all relevant cuboids
        List<dCuboid> cuboids = dCuboid.getNotableCuboidsContaining(location);
        dList cuboid_context = new dList();
        List<String> cuboidevents = new ArrayList<String>();
        for (dCuboid cuboid : cuboids) {
            for (String evt: events)
                cuboidevents.add(evt + " in " + cuboid.identifySimple());
        }
        // Add in cuboids context, with either the cuboids or an empty list
        context.put("cuboids", cuboid_context);
        events.addAll(cuboidevents);
        // Fire event
        String Determination = EventManager.doEvents(events, null, null, context, true);
        if (Determination.equalsIgnoreCase("CANCELLED"))
            event.setCancelled(true);
    }
}
