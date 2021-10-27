package com.denizenscript.denizen.tags.core;

import com.denizenscript.denizen.objects.ChunkTag;
import com.denizenscript.denizencore.tags.TagManager;

public class ChunkTagBase {

    public ChunkTagBase() {

        // <--[tag]
        // @attribute <chunk[<chunk>]>
        // @returns ChunkTag
        // @description
        // Returns a chunk object constructed from the input value.
        // Refer to <@link objecttype ChunkTag>.
        // -->
        TagManager.registerStaticTagBaseHandler(ChunkTag.class, "chunk", (attribute) -> {
            if (!attribute.hasContext(1)) {
                attribute.echoError("Chunk tag base must have input.");
                return null;
            }
            return ChunkTag.valueOf(attribute.getContext(1), attribute.context);
        });
    }
}
