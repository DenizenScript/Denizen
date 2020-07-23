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
        // Refer to <@link language LocationTag objects>.
        // -->
        TagManager.registerTagHandler("location", (attribute) -> {
            if (!attribute.hasContext(1)) {
                attribute.echoError("Location tag base must have input.");
                return null;
            }
            return LocationTag.valueOf(attribute.getContext(1), attribute.context);
        });
    }
}
