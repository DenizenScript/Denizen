package net.aufdemrand.denizen.events.core;

import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.scripts.containers.core.BukkitWorldScriptHelper;
import net.aufdemrand.denizencore.events.OldSmartEvent;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayerWalkSmartEvent implements OldSmartEvent, Listener {


    ///////////////////
    // SMARTEVENT METHODS
    ///////////////


    @Override
    public boolean shouldInitialize(Set<String> events) {

        for (String event : events) {

            Matcher m = Pattern.compile("on player walks", Pattern.CASE_INSENSITIVE)
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
        dB.log("Loaded PlayerWalk SmartEvent.");
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
    // player walks
    //
    // @Warning This event fires very very rapidly!
    //
    // @Triggers when a player moves in the slightest.
    // @Context
    // <context.old_location> returns the location of where the player was.
    // <context.new_location> returns the location of where the player is.
    //
    // @Determine
    // "CANCELLED" to stop the player from moving.
    //
    // -->
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Map<String, dObject> context = new HashMap<String, dObject>();
        context.put("old_location", new dLocation(event.getFrom()));
        context.put("new_location", new dLocation(event.getTo()));
        String determination = BukkitWorldScriptHelper.doEvents(Arrays.asList("player walks"), null,
                dEntity.getPlayerFrom(event.getPlayer()), context, true);
        if (determination.equalsIgnoreCase("CANCELLED"))
            event.setCancelled(true);
    }
}
