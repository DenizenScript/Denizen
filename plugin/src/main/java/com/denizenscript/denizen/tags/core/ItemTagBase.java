package com.denizenscript.denizen.tags.core;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.tags.TagManager;

public class ItemTagBase {

    public ItemTagBase() {

        // <--[tag]
        // @attribute <item[<item>]>
        // @returns ItemTag
        // @description
        // Returns an item object constructed from the input value.
        // Refer to <@link objecttype ItemTag>.
        // -->
        TagManager.registerTagHandler(ItemTag.class, "item", (attribute) -> { // non-static as item scripts can contain tags
            if (!attribute.hasParam()) {
                attribute.echoError("Item tag base must have input.");
                return null;
            }
            return ItemTag.valueOf(attribute.getParam(), attribute.context);
        });
    }
}
