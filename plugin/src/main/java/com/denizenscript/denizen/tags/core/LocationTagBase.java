package com.denizenscript.denizen.tags.core;

import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.tags.TagRunnable;
import com.denizenscript.denizencore.tags.ReplaceableTagEvent;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.Deprecations;

public class LocationTagBase {

    public LocationTagBase() {
        TagManager.registerTagHandler(new TagRunnable.RootForm() {
            @Override
            public void run(ReplaceableTagEvent event) {
                locationTags(event);
            }
        }, "location", "l");
    }

    public void locationTags(ReplaceableTagEvent event) {

        if (!event.matches("location", "l") || event.replaced()) {
            return;
        }

        if (event.matches("l")) {
            Deprecations.locationShorthand.warn(event.getScriptEntry());
        }

        // Stage the location
        LocationTag loc = null;

        // Check name context for a specified location, or check
        // the ScriptEntry for a 'location' context
        String context = event.getNameContext();
        if (event.hasNameContext() && LocationTag.matches(context)) {
            loc = LocationTag.valueOf(context, event.getAttributes().context);
        }
        else if (event.getScriptEntry().hasObject("location")) {
            loc = (LocationTag) event.getScriptEntry().getObject("location");
        }

        // Check if location is null, return null if it is
        if (loc == null) {
            return;
        }

        // Build and fill attributes
        event.setReplacedObject(CoreUtilities.autoAttrib(loc, event.getAttributes().fulfill(1)));

    }
}
