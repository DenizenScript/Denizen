package com.denizenscript.denizen.tags.core;

import com.denizenscript.denizen.objects.ChunkTag;
import com.denizenscript.denizencore.tags.TagRunnable;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ReplaceableTagEvent;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.CoreUtilities;

public class ChunkTagBase {

    public ChunkTagBase() {
        TagManager.registerTagHandler(new TagRunnable.RootForm() {
            @Override
            public void run(ReplaceableTagEvent event) {
                chunkTags(event);
            }
        }, "chunk");
    }

    public void chunkTags(ReplaceableTagEvent event) {

        if (!event.matches("chunk") || event.replaced()) {
            return;
        }

        ChunkTag chunk = null;

        if (event.hasNameContext()) {
            chunk = ChunkTag.valueOf(event.getNameContext(), event.getAttributes().context);
        }

        if (chunk == null) {
            return;
        }

        Attribute attribute = event.getAttributes();
        event.setReplacedObject(CoreUtilities.autoAttrib(chunk, attribute.fulfill(1)));

    }
}
