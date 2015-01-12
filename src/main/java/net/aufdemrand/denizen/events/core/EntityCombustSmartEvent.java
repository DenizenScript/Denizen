package net.aufdemrand.denizen.events.core;

import net.aufdemrand.denizen.scripts.containers.core.BukkitWorldScriptHelper;
import net.aufdemrand.denizencore.events.OldEventManager;
import net.aufdemrand.denizencore.events.OldSmartEvent;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizencore.objects.*;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class EntityCombustSmartEvent implements OldSmartEvent, Listener {


    ///////////////////
    // SMARTEVENT METHODS
    ///////////////


    @Override
    public boolean shouldInitialize(Set<String> events) {

        // Loop through event names from loaded world script events
        for (String event : events) {

            // Use a regex pattern to narrow down matches
            Matcher m = Pattern.compile("on (e@)?\\w+ combusts", Pattern.CASE_INSENSITIVE)
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
        dB.log("Loaded Entity Combust SmartEvent.");
    }


    @Override
    public void breakDown() {
        // Unregister events or any other temporary links your event created in _intialize()
        EntityCombustEvent.getHandlerList().unregister(this);
    }

    //////////////
    //  MECHANICS
    ///////////

    // <--[event]
    // @Events
    // entity combusts
    // <entity> combusts
    //
    // @Triggers when an entity combusts.
    // @Context
    // <context.duration> returns how long the entity takes to combust.
    // <context.entity> returns the dEntity that combusted.
    //
    // @Determine
    // "CANCELLED" to stop the entity from combusting.
    //
    // -->
    @EventHandler
    public void entityCombust(EntityCombustEvent event) {

        Map<String, dObject> context = new HashMap<String, dObject>();
        dEntity entity = new dEntity(event.getEntity());

        context.put("entity", entity.getDenizenObject());
        context.put("duration", new Duration((long) event.getDuration()));

        String determination = BukkitWorldScriptHelper.doEvents(Arrays.asList
                        ("entity combusts",
                                entity.identifySimple() + " combusts",
                                entity.identifyType() + " combusts"),
                null, null, context);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }
}
