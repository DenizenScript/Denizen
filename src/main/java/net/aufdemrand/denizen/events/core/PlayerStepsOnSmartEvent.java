package net.aufdemrand.denizen.events.core;

import net.aufdemrand.denizen.scripts.containers.core.BukkitWorldScriptHelper;
import net.aufdemrand.denizencore.events.OldSmartEvent;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizencore.objects.*;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayerStepsOnSmartEvent implements OldSmartEvent, Listener {


    ///////////////////
    // SMARTEVENT METHODS
    ///////////////


    @Override
    public boolean shouldInitialize(Set<String> events) {

        // Loop through event names from loaded world script events
        for (String event : events) {

            // Use a regex pattern to narrow down matches
            Matcher m = Pattern.compile("on player steps on (m@)?\\w+( in (cu@)?\\w+)?", Pattern.CASE_INSENSITIVE)
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
        dB.log("Loaded Player Steps On SmartEvent.");
    }


    @Override
    public void breakDown() {
        PlayerMoveEvent.getHandlerList().unregister(this);
        PlayerTeleportEvent.getHandlerList().unregister(this);
    }

    //////////////
    //  MECHANICS
    ///////////

    // <--[event]
    // @Events
    // player steps on block (in <notable cuboid>)
    // player steps on <material> (in <notable cuboid>)
    //
    // @Warning This event may fire very rapidly.
    //
    // @Regex on player steps on (m@)?\w+( in (cu@)?\w+)?
    //
    // @Triggers when a player steps onto a material.
    // @Context
    // <context.location> returns a dLocation of the block the player is stepping on.
    // <context.cuboids> returns a dList of all cuboids the player is inside.
    // <context.previous_location> returns a dLocation of where the player was before stepping onto the block.
    // <context.new_location> returns a dLocation of where the player is now.
    //
    // @Determine
    // "CANCELLED" to move the player back off the block (useless if the player jumped onto it!)
    //
    // -->
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (dEntity.isNPC(event.getPlayer())) {
            return;
        }
        PlayerMoveEvent evt = new PlayerMoveEvent(event.getPlayer(), event.getFrom(), event.getTo());
        onPlayerMove(evt);
        event.setCancelled(evt.isCancelled());
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // Check that the block position changed (X/Y/Z)
        if ((event.getFrom().getBlockX() != event.getTo().getBlockX()
                || event.getFrom().getBlockY() != event.getTo().getBlockY()
                || event.getFrom().getBlockZ() != event.getTo().getBlockZ())) {
            // Required event lists
            List<String> events = new ArrayList<String>();
            Map<String, dObject> context = new HashMap<String, dObject>();
            // Add the default location context + default event
            dLocation block = new dLocation(event.getTo().clone().subtract(0, 1, 0));
            dMaterial mat = dMaterial.getMaterialFrom(block.getBlock().getType(), block.getBlock().getData());
            context.put("location", block);
            context.put("previous_location", new dLocation(event.getFrom()));
            context.put("new_location", new dLocation(event.getTo()));
            events.add("player steps on block");
            events.add("player steps on " + mat.identifySimple());
            // Add all relevant cuboids
            List<dCuboid> cuboids = dCuboid.getNotableCuboidsContaining(block);
            dList cuboid_context = new dList();
            for (dCuboid cuboid : cuboids) {
                events.add("player steps on block in " + cuboid.identifySimple());
                events.add("player steps on " + mat.identifySimple() + " in " + cuboid.identifySimple());
                cuboid_context.add(cuboid.identify());
            }
            // Add in cuboids context, with either the cuboids or an empty list
            context.put("cuboids", cuboid_context);
            // Fire event
            String Determination = BukkitWorldScriptHelper.doEvents(events, null,
                    dEntity.getPlayerFrom(event.getPlayer()), context, true);
            if (Determination.equalsIgnoreCase("CANCELLED"))
                event.setCancelled(true);
        }
    }
}
