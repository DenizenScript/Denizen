package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.objects.dChunk;
import com.denizenscript.denizencore.objects.TagRunnable;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ReplaceableTagEvent;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.CoreUtilities;

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
