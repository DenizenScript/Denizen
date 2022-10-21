package com.denizenscript.denizen.paper.properties;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.paper.PaperModule;
import com.denizenscript.denizen.utilities.FormattedTextHelper;
import com.denizenscript.denizencore.objects.core.ElementTag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.chat.ComponentSerializer;

public class PaperElementExtensions {


    public static void register() {
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_18)) {

            // <--[tag]
            // @attribute <ElementTag.parse_minimessage>
            // @returns ElementTag
            // @Plugin Paper
            // @description
            // Returns the element with all MiniMessage tags parsed, see <@link url https://docs.adventure.kyori.net/minimessage/format.html> for more information.
            // This may be useful for reading data from external plugins, but should not be used in normal scripts.
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
            // This may be useful for sending data to external plugins, but should not be used in normal scripts.
            // -->
            ElementTag.tagProcessor.registerTag(ElementTag.class, "to_minimessage", (attribute, object) -> {
                Component parsed = PaperModule.jsonToComponent(ComponentSerializer.toString(FormattedTextHelper.parse(object.asString(), ChatColor.WHITE, false)));
                return new ElementTag(MiniMessage.miniMessage().serialize(parsed));
            });
        }
    }
}
