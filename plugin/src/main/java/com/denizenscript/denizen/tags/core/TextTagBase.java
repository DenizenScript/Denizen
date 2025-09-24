package com.denizenscript.denizen.tags.core;

import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.ChatColor;

public class TextTagBase {

    public TextTagBase() {
        TagManager.registerStaticTagBaseHandler(ElementTag.class, "&amp", (attribute) -> { BukkitImplDeprecations.pointlessTextTags.warn(attribute.context); return new ElementTag("&"); });
        TagManager.registerStaticTagBaseHandler(ElementTag.class, "&cm", (attribute) -> { BukkitImplDeprecations.pointlessTextTags.warn(attribute.context); return new ElementTag(","); });
        TagManager.registerStaticTagBaseHandler(ElementTag.class, "&sc", (attribute) -> { BukkitImplDeprecations.pointlessTextTags.warn(attribute.context); return new ElementTag(";"); });
        TagManager.registerStaticTagBaseHandler(ElementTag.class, "&pipe", (attribute) -> { BukkitImplDeprecations.pointlessTextTags.warn(attribute.context); return new ElementTag("|"); });
        TagManager.registerStaticTagBaseHandler(ElementTag.class, "&dot", (attribute) -> { BukkitImplDeprecations.pointlessTextTags.warn(attribute.context); return new ElementTag("."); });

        // <--[tag]
        // @attribute <p>
        // @returns ElementTag
        // @description
        // Returns a paragraph, for use in books.
        // -->
        TagManager.registerStaticTagBaseHandler(ElementTag.class, "p", (attribute) -> new ElementTag("\n " + ChatColor.RESET + " \n"));

        // <--[tag]
        // @attribute <&0>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters Black.
        // -->

        // <--[tag]
        // @attribute <&1>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters Dark Blue.
        // -->

        // <--[tag]
        // @attribute <&2>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters Dark Green.
        // -->

        // <--[tag]
        // @attribute <&3>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters Dark Cyan.
        // -->

        // <--[tag]
        // @attribute <&4>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters Dark Red.
        // -->

        // <--[tag]
        // @attribute <&5>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters Dark Magenta.
        // -->

        // <--[tag]
        // @attribute <&6>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters Gold.
        // -->

        // <--[tag]
        // @attribute <&7>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters Light Gray.
        // -->

        // <--[tag]
        // @attribute <&8>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters Dark Gray.
        // -->

        // <--[tag]
        // @attribute <&9>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters Light Blue.
        // -->

        // <--[tag]
        // @attribute <&a>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters Light Green.
        // -->

        // <--[tag]
        // @attribute <&b>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters Cyan.
        // -->

        // <--[tag]
        // @attribute <&c>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters Light Red.
        // -->

        // <--[tag]
        // @attribute <&d>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters Magenta.
        // -->

        // <--[tag]
        // @attribute <&e>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters Yellow.
        // -->

        // <--[tag]
        // @attribute <&f>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters White.
        // -->

        // <--[tag]
        // @attribute <&k>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters obfuscated.
        // -->

        // <--[tag]
        // @attribute <&l>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters bold.
        // -->

        // <--[tag]
        // @attribute <&m>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters have a strike-through.
        // -->

        // <--[tag]
        // @attribute <&n>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters have an underline.
        // -->

        // <--[tag]
        // @attribute <&o>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters italicized.
        // -->

        // <--[tag]
        // @attribute <&r>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that resets the following characters to normal.
        // -->

        // <--[tag]
        // @attribute <black>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters Black.
        // -->

        // <--[tag]
        // @attribute <dark_blue>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters Dark Blue.
        // -->

        // <--[tag]
        // @attribute <dark_green>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters Dark Green.
        // -->

        // <--[tag]
        // @attribute <dark_aqua>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters Dark Cyan.
        // -->

        // <--[tag]
        // @attribute <dark_red>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters Dark Red.
        // -->

        // <--[tag]
        // @attribute <dark_purple>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters Dark Magenta.
        // -->

        // <--[tag]
        // @attribute <gold>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters Gold.
        // -->

        // <--[tag]
        // @attribute <gray>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters Light Gray.
        // -->

        // <--[tag]
        // @attribute <dark_gray>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters Dark Gray.
        // -->

        // <--[tag]
        // @attribute <blue>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters Light Blue.
        // -->

        // <--[tag]
        // @attribute <green>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters Light Green.
        // -->

        // <--[tag]
        // @attribute <aqua>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters Cyan.
        // -->

        // <--[tag]
        // @attribute <red>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters Light Red.
        // -->

        // <--[tag]
        // @attribute <light_purple>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters Magenta.
        // -->

        // <--[tag]
        // @attribute <yellow>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters Yellow.
        // -->

        // <--[tag]
        // @attribute <white>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters White.
        // -->

        // <--[tag]
        // @attribute <magic>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters obfuscated.
        // -->

        // <--[tag]
        // @attribute <bold>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters bold.
        // -->

        // <--[tag]
        // @attribute <strikethrough>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters have a strike-through.
        // -->

        // <--[tag]
        // @attribute <underline>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters have an underline.
        // -->

        // <--[tag]
        // @attribute <italic>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters italicized.
        // -->

        // <--[tag]
        // @attribute <reset>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that resets the following characters to normal.
        // -->

        for (ChatColor color : ChatColor.values()) {
            final String nameVal = CoreUtilities.toLowerCase(color.name());
            final String retVal = color.toString();
            TagManager.registerStaticTagBaseHandler(ElementTag.class, nameVal, (attribute) -> new ElementTag(retVal));
            TagManager.registerStaticTagBaseHandler(ElementTag.class, "&" + color.getChar(), (attribute) -> new ElementTag(retVal));
        }
    }
}
