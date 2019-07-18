package com.denizenscript.denizen.tags.core;

import com.denizenscript.denizen.objects.BiomeTag;
import com.denizenscript.denizencore.objects.TagRunnable;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ReplaceableTagEvent;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.CoreUtilities;

public class BiomeTagBase {

    public BiomeTagBase() {
        TagManager.registerTagHandler(new TagRunnable.RootForm() {
            @Override
            public void run(ReplaceableTagEvent event) {
                biomeTags(event);
            }
        }, "biome");
    }

    public void biomeTags(ReplaceableTagEvent event) {

        if (!event.matches("biome") || event.replaced()) {
            return;
        }

        BiomeTag biome = null;

        if (event.hasNameContext()) {
            biome = BiomeTag.valueOf(event.getNameContext(), event.getAttributes().context);
        }

        if (biome == null) {
            return;
        }

        Attribute attribute = event.getAttributes();
        event.setReplacedObject(CoreUtilities.autoAttrib(biome, attribute.fulfill(1)));

    }
}
