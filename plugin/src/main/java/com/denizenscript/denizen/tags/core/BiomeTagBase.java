package com.denizenscript.denizen.tags.core;

import com.denizenscript.denizen.objects.BiomeTag;
import com.denizenscript.denizencore.tags.TagManager;

public class BiomeTagBase {

    public BiomeTagBase() {

        // <--[tag]
        // @attribute <biome[<biome>]>
        // @returns BiomeTag
        // @description
        // Returns a biome object constructed from the input value.
        // Refer to <@link objecttype BiomeTag>.
        // -->
        TagManager.registerTagHandler(BiomeTag.class, "biome", (attribute) -> {
            if (!attribute.hasContext(1)) {
                attribute.echoError("Biome tag base must have input.");
                return null;
            }
            return BiomeTag.valueOf(attribute.getContext(1), attribute.context);
        });
    }
}
