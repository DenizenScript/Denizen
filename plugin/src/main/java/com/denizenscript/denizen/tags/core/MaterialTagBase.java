package com.denizenscript.denizen.tags.core;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.tags.TagManager;

public class MaterialTagBase {

    public MaterialTagBase() {

        // <--[tag]
        // @attribute <material[<material>]>
        // @returns MaterialTag
        // @description
        // Returns a material object constructed from the input value.
        // Refer to <@link objecttype MaterialTag>.
        // -->
        TagManager.registerTagHandler(MaterialTag.class, "material", (attribute) -> {
            if (!attribute.hasContext(1)) {
                attribute.echoError("Material tag base must have input.");
                return null;
            }
            return MaterialTag.valueOf(attribute.getContext(1), attribute.context);
        });
    }
}
