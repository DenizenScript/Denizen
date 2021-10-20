package com.denizenscript.denizen.tags.core;

import com.denizenscript.denizen.objects.ColorTag;
import com.denizenscript.denizencore.tags.TagManager;

public class ColorTagBase {

    public ColorTagBase() {

        // <--[tag]
        // @attribute <color[<color>]>
        // @returns ColorTag
        // @description
        // Returns a color object constructed from the input value.
        // Refer to <@link objecttype ColorTag>.
        // -->
        TagManager.registerTagHandler(ColorTag.class, "color", (attribute) -> {
            if (!attribute.hasContext(1)) {
                attribute.echoError("Color tag base must have input.");
                return null;
            }
            return ColorTag.valueOf(attribute.getContext(1), attribute.context);
        });
    }
}
