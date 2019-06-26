package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.objects.dInventory;
import net.aufdemrand.denizencore.objects.TagRunnable;
import net.aufdemrand.denizencore.tags.Attribute;
import net.aufdemrand.denizencore.tags.ReplaceableTagEvent;
import net.aufdemrand.denizencore.tags.TagManager;
import net.aufdemrand.denizencore.utilities.CoreUtilities;

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

        dInventory inventory = null;

        if (event.hasNameContext()) {
            inventory = dInventory.valueOf(event.getNameContext(), event.getAttributes().context);
        }

        if (inventory == null) {
            return;
        }

        Attribute attribute = event.getAttributes();
        event.setReplacedObject(CoreUtilities.autoAttrib(inventory, attribute.fulfill(1)));

    }
}
