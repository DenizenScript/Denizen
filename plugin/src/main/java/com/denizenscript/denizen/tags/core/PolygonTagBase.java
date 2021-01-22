package com.denizenscript.denizen.tags.core;

import com.denizenscript.denizen.objects.PolygonTag;
import com.denizenscript.denizencore.tags.TagManager;

public class PolygonTagBase {

    public PolygonTagBase() {

        // <--[tag]
        // @attribute <polygon[<polygon>]>
        // @returns PolygonTag
        // @description
        // Returns a polygon object constructed from the input value.
        // Refer to <@link language PolygonTag objects>.
        // -->
        TagManager.registerTagHandler("polygon", (attribute) -> {
            if (!attribute.hasContext(1)) {
                attribute.echoError("Polygon tag base must have input.");
                return null;
            }
            return PolygonTag.valueOf(attribute.getContext(1), attribute.context);
        });
    }
}
