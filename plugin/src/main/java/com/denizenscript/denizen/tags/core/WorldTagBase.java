package com.denizenscript.denizen.tags.core;

import com.denizenscript.denizen.objects.WorldTag;
import com.denizenscript.denizencore.tags.TagManager;

public class WorldTagBase {

    public WorldTagBase() {

        // <--[tag]
        // @attribute <world[<world>]>
        // @returns WorldTag
        // @description
        // Returns a world object constructed from the input value.
        // Refer to <@link objecttype WorldTag>.
        // -->
        TagManager.registerTagHandler("world", (attribute) -> {
            if (!attribute.hasContext(1)) {
                attribute.echoError("World tag base must have input.");
                return null;
            }
            return WorldTag.valueOf(attribute.getContext(1), attribute.context);
        });
    }
}
