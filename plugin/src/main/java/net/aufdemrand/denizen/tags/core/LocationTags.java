package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.objects.dLocation;
import com.denizenscript.denizencore.objects.TagRunnable;
import com.denizenscript.denizencore.tags.ReplaceableTagEvent;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.debugging.SlowWarning;

public class LocationTags {

    public LocationTags() {
        TagManager.registerTagHandler(new TagRunnable.RootForm() {
            @Override
            public void run(ReplaceableTagEvent event) {
                locationTags(event);
            }
        }, "location", "l");
    }

    public SlowWarning locationShorthand = new SlowWarning("Short-named tags are hard to read. Please use 'location' instead of 'l' as a root tag.");

    public void locationTags(ReplaceableTagEvent event) {

        if (!event.matches("location", "l") || event.replaced()) {
            return;
        }

        if (event.matches("l")) {
            locationShorthand.warn(event.getScriptEntry());
        }

        // Stage the location
        dLocation loc = null;

        // Check name context for a specified location, or check
        // the ScriptEntry for a 'location' context
        String context = event.getNameContext();
        if (event.hasNameContext() && dLocation.matches(context)) {
            loc = dLocation.valueOf(context, event.getAttributes().context);
        }
        else if (event.getScriptEntry().hasObject("location")) {
            loc = (dLocation) event.getScriptEntry().getObject("location");
        }

        // Check if location is null, return null if it is
        if (loc == null) {
            return;
        }

        // Build and fill attributes
        event.setReplacedObject(CoreUtilities.autoAttrib(loc, event.getAttributes().fulfill(1)));

    }
}
