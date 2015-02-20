package net.aufdemrand.denizen.events.core;

import net.aufdemrand.denizen.scripts.containers.core.BukkitWorldScriptHelper;
import net.aufdemrand.denizencore.events.OldSmartEvent;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizencore.objects.*;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ItemScrollSmartEvent implements OldSmartEvent, Listener {


    ///////////////////
    // SMARTEVENT METHODS
    ///////////////


    @Override
    public boolean shouldInitialize(Set<String> events) {

        // Loop through event names from loaded world script events
        for (String event : events) {

            // Use a regex pattern to narrow down matches
            Matcher m = Pattern.compile("on player (scrolls their hotbar|holds item)", Pattern.CASE_INSENSITIVE)
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
        dB.log("Loaded Item Scroll SmartEvent.");
    }


    @Override
    public void breakDown() {
        // Unregister events or any other temporary links your event created in _intialize()
        PlayerItemHeldEvent.getHandlerList().unregister(this);
    }

    //////////////
    //  MECHANICS
    ///////////

    // <--[event]
    // @Events
    // player scrolls their hotbar
    // player holds item
    //
    // @Triggers when a player scrolls through their hotbar.
    // @Context
    // <context.new_slot> returns the number of the new inventory slot.
    // <context.previous_slot> returns the number of the old inventory slot.
    //
    // @Determine
    // "CANCELLED" to stop the player from scrolling.
    //
    // -->
    @EventHandler
    public void playerScrollsHotbar(PlayerItemHeldEvent event) {

        Map<String, dObject> context = new HashMap<String, dObject>();

        List<String> events = Arrays.asList("player scrolls their hotbar",
                "player holds item");

        context.put("new_slot", new Element(event.getNewSlot() + 1));
        context.put("previous_slot", new Element(event.getPreviousSlot() + 1));

        String determination = BukkitWorldScriptHelper.doEvents(events,
                null, dEntity.getPlayerFrom(event.getPlayer()), context, true);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }
}
