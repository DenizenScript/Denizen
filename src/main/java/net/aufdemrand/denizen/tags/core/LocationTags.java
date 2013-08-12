package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.events.ReplaceableTagEvent;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.utilities.entity.Rotation;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.aH;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;


/**
 * Location tag is a starting point for getting attributes for a
 *
 */

public class LocationTags implements Listener {

    public LocationTags(Denizen denizen) {
        denizen.getServer().getPluginManager().registerEvents(this, denizen);
    }

    @EventHandler
    public void locationTags(ReplaceableTagEvent event) {
    	
        if (!event.matches("location, l") || event.replaced()) return;

        // Stage the location
        dLocation loc = null;

        // Check name context for a specified location, or check
        // the ScriptEntry for a 'location' context
        if (event.hasNameContext() && dLocation.matches(event.getNameContext()))
            loc = dLocation.valueOf(event.getNameContext());
        else if (event.getScriptEntry().hasObject("location"))
            loc = (dLocation) event.getScriptEntry().getObject("location");

        // Check if location is null, return null if it is
        if (loc == null) { event.setReplaced("null"); return; }

        // Build and fill attributes
        Attribute attribute = new Attribute(event.raw_tag, event.getScriptEntry());
        event.setReplaced(loc.getAttribute(attribute.fulfill(1)));

    }

}