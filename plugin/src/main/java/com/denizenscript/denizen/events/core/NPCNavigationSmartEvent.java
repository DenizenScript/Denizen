package com.denizenscript.denizen.events.core;

import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.events.OldSmartEvent;
import org.bukkit.event.Listener;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handler event, actual events are fired in NPCTags.java
 */
public class NPCNavigationSmartEvent implements OldSmartEvent, Listener {

    ///////////////////
    // SMARTEVENT METHODS
    ///////////////

    @Override
    public boolean shouldInitialize(Set<String> events) {

        // Loop through event names from loaded world script events
        for (String event : events) {

            // Use a regex pattern to narrow down matches
            Matcher m = Pattern.compile("on npc ((begins|completes|cancels) navigation|stuck)", Pattern.CASE_INSENSITIVE)
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
        Debug.log("Loaded NPC Navigation SmartEvent.");
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
