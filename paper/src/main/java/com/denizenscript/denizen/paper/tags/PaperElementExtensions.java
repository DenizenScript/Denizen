package com.denizenscript.denizen.paper.tags;

import com.denizenscript.denizen.paper.PaperModule;
import com.denizenscript.denizencore.objects.core.ElementTag;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.md_5.bungee.api.ChatColor;

public class PaperElementExtensions {


    public static void register() {

        // <--[tag]
        // @attribute <ElementTag.parse_minimessage>
        // @returns ElementTag
        // @Plugin Paper
        // @description
        // Returns the element with all MiniMessage tags parsed.
        // -->
        ElementTag.tagProcessor.registerTag(ElementTag.class, "parse_minimessage", (attribute, object) -> {
            return new ElementTag(PaperModule.stringifyComponent(MiniMessage.miniMessage().deserialize(object.asString())));
        });

        // <--[tag]
        // @attribute <ElementTag.to_minimessage>
        // @returns ElementTag
        // @Plugin Paper
        // @description
        // Returns the element with all text formatting parsed into MiniMessage format.
        // -->
        ElementTag.tagProcessor.registerTag(ElementTag.class, "to_minimessage", (attribute, object) -> {
            return new ElementTag(MiniMessage.miniMessage().serialize(PaperModule.parseFormattedText(object.asString(), ChatColor.WHITE, false)));
        });
    }
}
