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
        TagManager.registerTagHandler("item", (attribute) -> {
            if (!attribute.hasContext(1)) {
                attribute.echoError("Item tag base must have input.");
                return null;
            }
            return ItemTag.valueOf(attribute.getContext(1), attribute.context);
        });
    }
}
