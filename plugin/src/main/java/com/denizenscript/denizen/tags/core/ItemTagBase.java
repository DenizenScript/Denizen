package com.denizenscript.denizen.tags.core;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.tags.TagRunnable;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ReplaceableTagEvent;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.CoreUtilities;

public class ItemTagBase {

    public ItemTagBase() {

        // <--[tag]
        // @attribute <item[<item>]>
        // @returns ItemTag
        // @description
        // Returns a item object constructed from the input value.
        // -->
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

        ItemTag item = null;

        if (event.hasNameContext()) {
            item = ItemTag.valueOf(event.getNameContext(), event.getAttributes().context);
        }

        if (item == null) {
            return;
        }

        Attribute attribute = event.getAttributes();
        event.setReplacedObject(CoreUtilities.autoAttrib(item, attribute.fulfill(1)));

    }
}
