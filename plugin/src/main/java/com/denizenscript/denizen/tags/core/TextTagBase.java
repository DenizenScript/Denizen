package com.denizenscript.denizen.tags.core;

import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.TagRunnable;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ReplaceableTagEvent;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.Deprecations;
import org.bukkit.ChatColor;

public class TextTagBase {

    public TextTagBase() {
        TagManager.registerTagHandler(new TagRunnable.RootForm() {
            @Override
            public void run(ReplaceableTagEvent event) {
                specialCharacterTags(event);
            }
        }, "&auml", "&Auml", "&ouml", "&Ouml", "&uuml", "&Uuml", "&nl", "&amp", "&cm", "&ss", "&sq", "&sp", "&nbsp",
                "&dq", "&co", "&sc", "&rb", "&lb", "&rc", "&lc", "&ns", "&pc", "&pipe",
                "&ds", "&lt", "&gt", "&bs", "&at", "&dot", "&hrt", "&chr");
        for (ChatColor color : ChatColor.values()) {
            final String nameVal = CoreUtilities.toLowerCase(color.name());
            final String codeVal = "&" + String.valueOf(color.getChar());
            final String retVal = color.toString();
            TagManager.registerTagHandler(new TagRunnable.RootForm() {
                @Override
                public void run(ReplaceableTagEvent event) {
                    event.setReplacedObject(new ElementTag(retVal).getObjectAttribute(event.getAttributes().fulfill(1)));
                }
            }, nameVal, codeVal);
        }
    }

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


    @TagManager.TagEvents
    public void specialCharacterTags(ReplaceableTagEvent event) {
        if (!event.getName().startsWith("&")) {
            return;
        }
        String lower = CoreUtilities.toLowerCase(event.getName());
        Attribute attribute = event.getAttributes();

        if (event.getName().equals("&auml")) {
            Deprecations.pointlessTextTags.warn(event.getScriptEntry());
            event.setReplaced(new ElementTag("ä").getAttribute(attribute.fulfill(1)));
        }
        else if (event.getName().equals("&Auml")) {
            Deprecations.pointlessTextTags.warn(event.getScriptEntry());
            event.setReplaced(new ElementTag("Ä").getAttribute(attribute.fulfill(1)));
        }
        else if (event.getName().equals("&ouml")) {
            Deprecations.pointlessTextTags.warn(event.getScriptEntry());
            event.setReplaced(new ElementTag("ö").getAttribute(attribute.fulfill(1)));
        }
        else if (event.getName().equals("&Ouml")) {
            Deprecations.pointlessTextTags.warn(event.getScriptEntry());
            event.setReplaced(new ElementTag("Ö").getAttribute(attribute.fulfill(1)));
        }
        else if (event.getName().equals("&uuml")) {
            Deprecations.pointlessTextTags.warn(event.getScriptEntry());
            event.setReplaced(new ElementTag("ü").getAttribute(attribute.fulfill(1)));
        }
        else if (event.getName().equals("&Uuml")) {
            Deprecations.pointlessTextTags.warn(event.getScriptEntry());
            event.setReplaced(new ElementTag("Ü").getAttribute(attribute.fulfill(1)));
        }
        else if (lower.equals("&amp")) {
            Deprecations.pointlessTextTags.warn(event.getScriptEntry());
            event.setReplaced(new ElementTag("&").getAttribute(attribute.fulfill(1)));
        }
        else if (lower.equals("&cm")) {
            Deprecations.pointlessTextTags.warn(event.getScriptEntry());
            event.setReplaced(new ElementTag(",").getAttribute(attribute.fulfill(1)));
        }
        else if (lower.equals("&sc")) {
            Deprecations.pointlessTextTags.warn(event.getScriptEntry());
            event.setReplaced(new ElementTag(String.valueOf((char) 0x2011)).getAttribute(attribute.fulfill(1)));
        }
        else if (lower.equals("&pipe")) {
            Deprecations.pointlessTextTags.warn(event.getScriptEntry());
            event.setReplaced(new ElementTag("|").getAttribute(attribute.fulfill(1)));
        }
        else if (lower.equals("&ds")) {
            Deprecations.pointlessTextTags.warn(event.getScriptEntry());
            event.setReplaced(new ElementTag("$").getAttribute(attribute.fulfill(1)));
        }
        else if (lower.equals("&at")) {
            Deprecations.pointlessTextTags.warn(event.getScriptEntry());
            event.setReplaced(new ElementTag("@").getAttribute(attribute.fulfill(1)));
        }
        else if (lower.equals("&dot")) {
            Deprecations.pointlessTextTags.warn(event.getScriptEntry());
            event.setReplaced(new ElementTag(".").getAttribute(attribute.fulfill(1)));
        }
        else if (lower.equals("&hrt")) {
            Deprecations.pointlessTextTags.warn(event.getScriptEntry());
            event.setReplaced(new ElementTag("\u2665").getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <&pc>
        // @returns ElementTag
        // @description
        // Returns a percent symbol: %
        // -->
        else if (lower.equals("&pc")) {
            event.setReplaced(new ElementTag("%").getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <&nl>
        // @returns ElementTag
        // @description
        // Returns a newline symbol.
        // -->
        if (lower.equals("&nl")) {
            event.setReplaced(new ElementTag("\n").getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <&ss>
        // @returns ElementTag
        // @description
        // Returns an internal coloring symbol: §
        // -->
        else if (lower.equals("&ss")) {
            event.setReplaced(new ElementTag("§").getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <&sq>
        // @returns ElementTag
        // @description
        // Returns a single-quote symbol: '
        // -->
        else if (lower.equals("&sq")) {
            event.setReplaced(new ElementTag("'").getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <&sp>
        // @returns ElementTag
        // @description
        // Returns a space symbol.
        // -->
        else if (lower.equals("&sp")) {
            event.setReplaced(new ElementTag(String.valueOf(' ')).getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <&nbsp>
        // @returns ElementTag
        // @description
        // Returns a non-breaking space symbol.
        // -->
        else if (lower.equals("&nbsp")) {
            event.setReplaced(new ElementTag(String.valueOf((char) 0x00A0)).getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <&dq>
        // @returns ElementTag
        // @description
        // Returns a double-quote symbol: "
        // -->
        else if (lower.equals("&dq")) {
            event.setReplaced(new ElementTag("\"").getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <&co>
        // @returns ElementTag
        // @description
        // Returns a colon symbol: :
        // -->
        else if (lower.equals("&co")) {
            event.setReplaced(new ElementTag(":").getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <&rb>
        // @returns ElementTag
        // @description
        // Returns a right-bracket symbol: ]
        // -->
        else if (lower.equals("&rb")) {
            event.setReplaced(new ElementTag("]").getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <&lb>
        // @returns ElementTag
        // @description
        // Returns a left-bracket symbol: [
        // -->
        else if (lower.equals("&lb")) {
            event.setReplaced(new ElementTag("[").getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <&rc>
        // @returns ElementTag
        // @description
        // Returns a right-brace symbol: }
        // -->
        else if (lower.equals("&rc")) {
            event.setReplaced(new ElementTag("}").getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <&lc>
        // @returns ElementTag
        // @description
        // Returns a left-brace symbol: {
        // -->
        else if (lower.equals("&lc")) {
            event.setReplaced(new ElementTag("{").getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <&ns>
        // @returns ElementTag
        // @description
        // Returns a hash symbol: #
        // -->
        else if (lower.equals("&ns")) {
            event.setReplaced(new ElementTag("#").getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <&lt>
        // @returns ElementTag
        // @description
        // Returns a less than symbol: <
        // -->
        else if (lower.equals("&lt")) {
            event.setReplaced(new ElementTag(String.valueOf((char) 0x01)).getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <&gt>
        // @returns ElementTag
        // @description
        // Returns a greater than symbol: >
        // -->
        else if (lower.equals("&gt")) {
            event.setReplaced(new ElementTag(String.valueOf((char) 0x02)).getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <&bs>
        // @returns ElementTag
        // @description
        // Returns a backslash symbol: \
        // -->
        else if (lower.equals("&bs")) {
            event.setReplaced(new ElementTag("\\").getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <&chr[<character>]>
        // @returns ElementTag
        // @description
        // Returns the Unicode character specified. e.g. <&chr[2665]> returns a heart.
        // -->
        if (attribute.startsWith("&chr") && attribute.hasContext(1)) {
            event.setReplaced(String.valueOf((char) Integer.parseInt(attribute.getContext(1), 16)));
        }

    }
}
