package com.denizenscript.denizen.tags.core;

import com.denizenscript.denizen.objects.EllipsoidTag;
import com.denizenscript.denizencore.tags.TagRunnable;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ReplaceableTagEvent;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.CoreUtilities;

public class EllipsoidTagBase {

    public EllipsoidTagBase() {

        // <--[tag]
        // @attribute <ellipsoid[<ellipsoid>]>
        // @returns EllipsoidTag
        // @description
        // Returns an ellipsoid object constructed from the input value.
        // Refer to <@link language EllipsoidTag objects>.
        // -->
        TagManager.registerTagHandler(new TagRunnable.RootForm() {
            @Override
            public void run(ReplaceableTagEvent event) {
                ellipsoidTags(event);
            }
        }, "ellipsoid");
    }

    public void ellipsoidTags(ReplaceableTagEvent event) {

        if (!event.matches("ellipsoid") || event.replaced()) {
            return;
        }

        EllipsoidTag ellipsoid = null;

        if (event.hasNameContext()) {
            ellipsoid = EllipsoidTag.valueOf(event.getNameContext(), event.getAttributes().context);
        }

        if (ellipsoid == null) {
            return;
        }

        Attribute attribute = event.getAttributes();
        event.setReplacedObject(CoreUtilities.autoAttrib(ellipsoid, attribute.fulfill(1)));

    }
}
