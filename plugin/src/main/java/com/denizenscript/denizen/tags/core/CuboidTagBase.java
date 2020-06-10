package com.denizenscript.denizen.tags.core;

import com.denizenscript.denizen.objects.CuboidTag;
import com.denizenscript.denizencore.tags.TagRunnable;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ReplaceableTagEvent;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.CoreUtilities;

public class CuboidTagBase {

    public CuboidTagBase() {

        // <--[tag]
        // @attribute <cuboid[<cuboid>]>
        // @returns CuboidTag
        // @description
        // Returns a cuboid object constructed from the input value.
        // Refer to <@link language CuboidTag objects>.
        // -->
        TagManager.registerTagHandler(new TagRunnable.RootForm() {
            @Override
            public void run(ReplaceableTagEvent event) {
                cuboidTags(event);
            }
        }, "cuboid");
    }

    public void cuboidTags(ReplaceableTagEvent event) {

        if (!event.matches("cuboid") || event.replaced()) {
            return;
        }

        CuboidTag cuboid = null;

        if (event.hasNameContext()) {
            cuboid = CuboidTag.valueOf(event.getNameContext(), event.getAttributes().context);
        }

        // Check if cuboid is null, return if it is
        if (cuboid == null) {
            return;
        }

        // Build and fill attributes
        Attribute attribute = event.getAttributes();
        event.setReplacedObject(CoreUtilities.autoAttrib(cuboid, attribute.fulfill(1)));

    }
}
