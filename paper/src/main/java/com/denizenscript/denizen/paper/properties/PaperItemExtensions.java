package com.denizenscript.denizen.paper.properties;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.core.ElementTag;

public class PaperItemExtensions {

    public static void register() {

        if (NMSHandler.getVersion().isAtMost(NMSVersion.v1_20)) {
            ItemTag.tagProcessor.registerTag(ElementTag.class, "rarity", (attribute, item) -> {
                return new ElementTag(item.getItemStack().getRarity());
            });
        }
    }
}
