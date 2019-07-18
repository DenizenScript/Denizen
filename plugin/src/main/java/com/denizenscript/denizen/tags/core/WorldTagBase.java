package com.denizenscript.denizen.tags.core;

import com.denizenscript.denizen.objects.WorldTag;
import com.denizenscript.denizencore.objects.TagRunnable;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ReplaceableTagEvent;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.CoreUtilities;

public class WorldTagBase {

    public WorldTagBase() {
        TagManager.registerTagHandler(new TagRunnable.RootForm() {
            @Override
            public void run(ReplaceableTagEvent event) {
                worldTags(event);
            }
        }, "world");
    }

    public void worldTags(ReplaceableTagEvent event) {

        if (!event.matches("world") || event.replaced()) {
            return;
        }

        WorldTag world = null;

        if (event.hasNameContext()) {
            world = WorldTag.valueOf(event.getNameContext(), event.getAttributes().context);
        }

        if (world == null) {
            return;
        }

        Attribute attribute = event.getAttributes();
        event.setReplacedObject(CoreUtilities.autoAttrib(world, attribute.fulfill(1)));

    }
}
