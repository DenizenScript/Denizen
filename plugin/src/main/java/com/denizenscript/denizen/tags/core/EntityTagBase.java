package com.denizenscript.denizen.tags.core;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.tags.TagManager;

public class EntityTagBase {

    public EntityTagBase() {

        // <--[tag]
        // @attribute <entity[<entity>]>
        // @returns EntityTag
        // @description
        // Returns an entity object constructed from the input value.
        // Refer to <@link objecttype EntityTag>.
        // -->
        TagManager.registerTagHandler(EntityTag.class, "entity", (attribute) -> {
            if (!attribute.hasParam()) {
                attribute.echoError("Entity tag base must have input.");
                return null;
            }
            return EntityTag.valueOf(attribute.getParam(), attribute.context);
        });
    }
}
