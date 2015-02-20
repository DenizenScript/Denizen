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

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayerJumpSmartEvent implements OldSmartEvent, Listener {


    ///////////////////
    // SMARTEVENT METHODS
    ///////////////


    @Override
    public boolean shouldInitialize(Set<String> events) {

        // Loop through event names from loaded world script events
        for (String event : events) {

            // Use a regex pattern to narrow down matches
            Matcher m = Pattern.compile("on player jumps", Pattern.CASE_INSENSITIVE)
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
        dB.log("Loaded Player Jump SmartEvent.");
    }


    @Override
    public void breakDown() {
        PlayerMoveEvent.getHandlerList().unregister(this);
    }

    //////////////
    //  MECHANICS
    ///////////

    // <--[event]
    // @Events
    // player jumps
    //
    // @Triggers when a player jumps.
    // @Context
    // <context.location> returns the location the player jumped at.
    //
    // -->
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // Check that the block level changed (Upward)
        if (event.getTo().getBlockY() > event.getFrom().getBlockY()
                // and also that the player has a high velocity (jump instead of walking up stairs)
                && Math.abs(event.getPlayer().getVelocity().getY()) > 0.1
                // and that the player isn't in any form of fast moving vehicle
                && event.getPlayer().getVehicle() == null) {
            // Not perfect checking, but close enough until Bukkit adds a proper event
            Map<String, dObject> context = new HashMap<String, dObject>();
            context.put("location", new dLocation(event.getTo()));
            BukkitWorldScriptHelper.doEvents(Arrays.asList("player jumps"), null,
                    dEntity.getPlayerFrom(event.getPlayer()), context);
        }
    }
}
