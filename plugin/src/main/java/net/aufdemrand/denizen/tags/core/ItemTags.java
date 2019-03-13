package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizencore.objects.TagRunnable;
import net.aufdemrand.denizencore.tags.Attribute;
import net.aufdemrand.denizencore.tags.ReplaceableTagEvent;
import net.aufdemrand.denizencore.tags.TagManager;
import net.aufdemrand.denizencore.utilities.CoreUtilities;

public class ItemTags {

    public ItemTags(Denizen denizen) {
        TagManager.registerTagHandler(new TagRunnable.RootForm() {
            @Override
            public void run(ReplaceableTagEvent event) {
                itemTags(event);
            }
        }, "item");
    }

    public void itemTags(ReplaceableTagEvent event) {

        if (!event.matches("item") || event.replaced()) {
            return;
        }

        dItem item = null;

        if (event.hasNameContext()) {
            item = dItem.valueOf(event.getNameContext(), event.getAttributes().context);
        }

        if (item == null) {
            return;
        }

        Attribute attribute = event.getAttributes();
        event.setReplacedObject(CoreUtilities.autoAttrib(item, attribute.fulfill(1)));

    }
}
