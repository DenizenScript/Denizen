package com.denizenscript.denizen.paper.properties;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.core.ElementTag;

public class PaperItemExtensions {

    public static void register() {

        // <--[tag]
        // @attribute <ItemTag.rarity>
        // @returns ElementTag
        // @group paper
        // @Plugin Paper
        // @description
        // Returns the rarity of an item, as "common", "uncommon", "rare", or "epic".
        // -->
        ItemTag.tagProcessor.registerTag(ElementTag.class, "rarity", (attribute, item) -> {
            return new ElementTag(item.getItemStack().getRarity());
        });
    }
}
