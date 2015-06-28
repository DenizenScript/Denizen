package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizencore.tags.Attribute;
import net.aufdemrand.denizencore.tags.ReplaceableTagEvent;
import net.aufdemrand.denizencore.tags.TagManager;
import org.bukkit.event.Listener;


/**
 * Location tag is a starting point for getting attributes for a
 */

public class LocationTags implements Listener {

    public LocationTags(Denizen denizen) {
        denizen.getServer().getPluginManager().registerEvents(this, denizen);
        TagManager.registerTagEvents(this);
    }

    @TagManager.TagEvents
    public void locationTags(ReplaceableTagEvent event) {

        if (!event.matches("location", "l") || event.replaced()) return;

        // Stage the location
        dLocation loc = null;

        // Check name context for a specified location, or check
        // the ScriptEntry for a 'location' context
        String context = event.getNameContext();
        if (event.hasNameContext() && dLocation.matches(context))
            loc = dLocation.valueOf(context);
        else if (event.getScriptEntry().hasObject("location"))
            loc = (dLocation) event.getScriptEntry().getObject("location");

        // Check if location is null, return null if it is
        if (loc == null) {
            return;
        }

        // Build and fill attributes
        Attribute attribute = event.getAttributes();
        event.setReplaced(loc.getAttribute(attribute.fulfill(1)));

    }
}
