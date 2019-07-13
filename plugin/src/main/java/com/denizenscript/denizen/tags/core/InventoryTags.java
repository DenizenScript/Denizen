package com.denizenscript.denizen.tags.core;

import com.denizenscript.denizen.objects.InventoryTag;
import com.denizenscript.denizencore.objects.TagRunnable;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ReplaceableTagEvent;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.CoreUtilities;

public class InventoryTags {

    public InventoryTags() {
        TagManager.registerTagHandler(new TagRunnable.RootForm() {
            @Override
            public void run(ReplaceableTagEvent event) {
                inventoryTags(event);
            }
        }, "inventory");
    }

    public void inventoryTags(ReplaceableTagEvent event) {

        if (!event.matches("inventory") || event.replaced()) {
            return;
        }

        InventoryTag inventory = null;

        if (event.hasNameContext()) {
            inventory = InventoryTag.valueOf(event.getNameContext(), event.getAttributes().context);
        }

        if (inventory == null) {
            return;
        }

        Attribute attribute = event.getAttributes();
        event.setReplacedObject(CoreUtilities.autoAttrib(inventory, attribute.fulfill(1)));

    }
}
