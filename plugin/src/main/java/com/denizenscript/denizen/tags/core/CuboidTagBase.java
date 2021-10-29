package com.denizenscript.denizen.tags.core;

import com.denizenscript.denizen.objects.CuboidTag;
import com.denizenscript.denizencore.tags.TagManager;

public class CuboidTagBase {

    public CuboidTagBase() {

        // <--[tag]
        // @attribute <cuboid[<cuboid>]>
        // @returns CuboidTag
        // @description
        // Returns a cuboid object constructed from the input value.
        // Refer to <@link objecttype CuboidTag>.
        // -->
        TagManager.registerTagHandler(CuboidTag.class, "cuboid", (attribute) -> { // non-static due to notes
            if (!attribute.hasParam()) {
                attribute.echoError("Cuboid tag base must have input.");
                return null;
            }
            return CuboidTag.valueOf(attribute.getParam(), attribute.context);
        });
    }
}
