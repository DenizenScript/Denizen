package com.denizenscript.denizen.tags.core;

import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.tags.TagManager;

public class LocationTagBase {

    public LocationTagBase() {

        // <--[tag]
        // @attribute <location[<location>]>
        // @returns LocationTag
        // @description
        // Returns a location object constructed from the input value.
        // Refer to <@link objecttype LocationTag>.
        // -->
        TagManager.registerTagHandler(LocationTag.class, "location", (attribute) -> { // non-static due to notes
            if (!attribute.hasContext(1)) {
                attribute.echoError("Location tag base must have input.");
                return null;
            }
            return LocationTag.valueOf(attribute.getContext(1), attribute.context);
        });
    }
}
