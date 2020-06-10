package com.denizenscript.denizen.tags.core;

import com.denizenscript.denizen.objects.ColorTag;
import com.denizenscript.denizencore.tags.TagRunnable;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ReplaceableTagEvent;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.CoreUtilities;

public class ColorTagBase {

    public ColorTagBase() {

        // <--[tag]
        // @attribute <color[<color>]>
        // @returns ColorTag
        // @description
        // Returns a color object constructed from the input value.
        // Refer to <@link language ColorTag objects>.
        // -->
        TagManager.registerTagHandler(new TagRunnable.RootForm() {
            @Override
            public void run(ReplaceableTagEvent event) {
                colorTags(event);
            }
        }, "color");
    }

    public void colorTags(ReplaceableTagEvent event) {

        if (!event.matches("color") || event.replaced()) {
            return;
        }

        ColorTag color = null;

        if (event.hasNameContext()) {
            color = ColorTag.valueOf(event.getNameContext(), event.getAttributes().context);
        }

        if (color == null) {
            return;
        }

        Attribute attribute = event.getAttributes();
        event.setReplacedObject(CoreUtilities.autoAttrib(color, attribute.fulfill(1)));

    }
}
