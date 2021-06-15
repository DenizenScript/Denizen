package com.denizenscript.denizen.tags.core;

import com.denizenscript.denizen.objects.InventoryTag;
import com.denizenscript.denizencore.tags.TagManager;

public class InventoryTagBase {

    public InventoryTagBase() {

        // <--[tag]
        // @attribute <inventory[<inventory>]>
        // @returns InventoryTag
        // @description
        // Returns an inventory object constructed from the input value.
        // Refer to <@link objecttype InventoryTag>.
        // -->
        TagManager.registerTagHandler("inventory", (attribute) -> {
            if (!attribute.hasContext(1)) {
                attribute.echoError("Inventory tag base must have input.");
                return null;
            }
            return InventoryTag.valueOf(attribute.getContext(1), attribute.context);
        });
    }
}
