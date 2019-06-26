package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.objects.dChunk;
import net.aufdemrand.denizencore.objects.TagRunnable;
import net.aufdemrand.denizencore.tags.Attribute;
import net.aufdemrand.denizencore.tags.ReplaceableTagEvent;
import net.aufdemrand.denizencore.tags.TagManager;
import net.aufdemrand.denizencore.utilities.CoreUtilities;

public class ChunkTags {

    public ChunkTags() {
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

        dChunk chunk = null;

        if (event.hasNameContext()) {
            chunk = dChunk.valueOf(event.getNameContext(), event.getAttributes().context);
        }

        if (chunk == null) {
            return;
        }

        Attribute attribute = event.getAttributes();
        event.setReplacedObject(CoreUtilities.autoAttrib(chunk, attribute.fulfill(1)));

    }
}
