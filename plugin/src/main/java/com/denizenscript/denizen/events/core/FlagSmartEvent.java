package com.denizenscript.denizen.events.core;

import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.events.OldSmartEvent;
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
                Debug.echoError("You are using flag events ('" + event + "'). These are only available for experimental dev reasons, and should ABSOLUTELY NOT be used in any live scripts.");
                return true;
            }
        }
        return false;
    }


    @Override
    public void _initialize() {
        active = true;
        Debug.log("Loaded Flag SmartEvent.");
    }


    @Override
    public void breakDown() {
        active = false;
    }

    //////////////
    //  MECHANICS
    ///////////

    static boolean active = false;

    public static boolean isActive() {
        return active;
    }
}
