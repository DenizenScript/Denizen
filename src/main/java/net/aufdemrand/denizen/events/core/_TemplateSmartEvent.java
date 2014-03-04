package net.aufdemrand.denizen.events.core;

import net.aufdemrand.denizen.events.EventManager;
import net.aufdemrand.denizen.events.SmartEvent;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dObject;
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

/*
 * Template for a Denizen Smart Event.
 * Remember to register the smart event!
 * (In EventManager if you're a Denizen developer, or in an
 * onEnable() if you're developing a plugin that depends on
 * Denizen)
 */
public class _TemplateSmartEvent implements SmartEvent, Listener {


    ///////////////////
    // SMARTEVENT METHODS
    ///////////////


    @Override
    public boolean shouldInitialize(Set<String> events) {

        // Loop through event names from loaded world script events
        for (String event : events) {

            // Use a regex pattern to narrow down matches
            Matcher m = Pattern.compile("on x or y or \\w+", Pattern.CASE_INSENSITIVE)
                    .matcher(event);

            if (m.matches()) {
                // Check the data in the 'event' string here, make sure it /really/ is valid
                // (EG, see if a material is a valid material type)
                return true; // If it's valid, return true... otherwise, do nothing.
                // In case a different event is valid.
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
        dB.log("Loaded Player Jump SmartEvent.");
    }


    @Override
    public void breakDown() {
        // Unregister events or any other temporary links your event created in _intialize()
        PlayerMoveEvent.getHandlerList().unregister(this);
    }



    //////////////
    //  MECHANICS
    ///////////

    // Don't forget to add [event] meta information here!
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // Your code here (Or in a different event, of course! Just remember to add any events to breakDown() )
        // Create a context map
        Map<String, dObject> context = new HashMap<String, dObject>();
        // Add some things to it
        context.put("location", new dLocation(event.getTo()));
        // Fire the event!
        String determination = EventManager.doEvents(Arrays.asList("x or y or z"), null /* NPC */, new dPlayer(event.getPlayer()), context, 1);
        // Parse the determination and edit the event accordingly here
    }


}
