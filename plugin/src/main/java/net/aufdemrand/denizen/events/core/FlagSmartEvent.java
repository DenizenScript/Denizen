package net.aufdemrand.denizen.events.core;

import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.events.OldSmartEvent;
import org.bukkit.event.Listener;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handler event, actual events are fired in FlagManager.java
 */
public class FlagSmartEvent implements OldSmartEvent, Listener {


    ///////////////////
    // SMARTEVENT METHODS
    ///////////////


    @Override
    public boolean shouldInitialize(Set<String> events) {

        // Loop through event names from loaded world script events
        for (String event : events) {

            // Use a regex pattern to narrow down matches
            Matcher m = Pattern.compile("on ((player|npc|server) )?flag( \\w+)? (cleared|changed|expires)", Pattern.CASE_INSENSITIVE)
                    .matcher(event);

            if (m.matches()) {
                return true;
            }
        }
        return false;
    }


    @Override
    public void _initialize() {
        active = true;
        dB.log("Loaded Flag SmartEvent.");
    }


    @Override
    public void breakDown() {
        active = false;
    }

    //////////////
    //  MECHANICS
    ///////////

    static boolean active = false;

    public static boolean IsActive() {
        return active;
    }
}
