package com.denizenscript.denizen.tags.core;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.TagRunnable;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ReplaceableTagEvent;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.CoreUtilities;

public class MaterialTagBase {

    public MaterialTagBase() {
        TagManager.registerTagHandler(new TagRunnable.RootForm() {
            @Override
            public void run(ReplaceableTagEvent event) {
                materialTags(event);
            }
        }, "material");
    }

    public void materialTags(ReplaceableTagEvent event) {

        if (!event.matches("material") || event.replaced()) {
            return;
        }

        MaterialTag material = null;

        if (event.hasNameContext()) {
            material = MaterialTag.valueOf(event.getNameContext(), event.getAttributes().context);
        }

        if (material == null) {
            return;
        }

        Attribute attribute = event.getAttributes();
        event.setReplacedObject(CoreUtilities.autoAttrib(material, attribute.fulfill(1)));

    }
}
