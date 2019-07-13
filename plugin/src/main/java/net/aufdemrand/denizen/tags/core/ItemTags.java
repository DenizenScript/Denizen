package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.objects.dItem;
import com.denizenscript.denizencore.objects.TagRunnable;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ReplaceableTagEvent;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.CoreUtilities;

public class ItemTags {

    public ItemTags() {
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
