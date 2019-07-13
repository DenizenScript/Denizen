package net.aufdemrand.denizen.tags.core;

import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.TagRunnable;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ReplaceableTagEvent;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.ChatColor;

public class TextTags {

    public TextTags() {
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
                    event.setReplacedObject(new Element(retVal).getObjectAttribute(event.getAttributes().fulfill(1)));
                }
            }, nameVal, codeVal);
        }
    }

    // <--[tag]
    // @attribute <&0>
    // @returns Element
    // @description
    // Returns the ChatColor that makes the following characters Black.
    // -->

    // <--[tag]
    // @attribute <&1>
    // @returns Element
    // @description
    // Returns the ChatColor that makes the following characters Dark Blue.
    // -->

    // <--[tag]
    // @attribute <&2>
    // @returns Element
    // @description
    // Returns the ChatColor that makes the following characters Dark Green.
    // -->

    // <--[tag]
    // @attribute <&3>
    // @returns Element
    // @description
    // Returns the ChatColor that makes the following characters Dark Cyan.
    // -->

    // <--[tag]
    // @attribute <&4>
    // @returns Element
    // @description
    // Returns the ChatColor that makes the following characters Dark Red.
    // -->

    // <--[tag]
    // @attribute <&5>
    // @returns Element
    // @description
    // Returns the ChatColor that makes the following characters Dark Magenta.
    // -->

    // <--[tag]
    // @attribute <&6>
    // @returns Element
    // @description
    // Returns the ChatColor that makes the following characters Gold.
    // -->

    // <--[tag]
    // @attribute <&7>
    // @returns Element
    // @description
    // Returns the ChatColor that makes the following characters Light Gray.
    // -->

    // <--[tag]
    // @attribute <&8>
    // @returns Element
    // @description
    // Returns the ChatColor that makes the following characters Dark Gray.
    // -->

    // <--[tag]
    // @attribute <&9>
    // @returns Element
    // @description
    // Returns the ChatColor that makes the following characters Light Blue.
    // -->

    // <--[tag]
    // @attribute <&a>
    // @returns Element
    // @description
    // Returns the ChatColor that makes the following characters Light Green.
    // -->

    // <--[tag]
    // @attribute <&b>
    // @returns Element
    // @description
    // Returns the ChatColor that makes the following characters Cyan.
    // -->

    // <--[tag]
    // @attribute <&c>
    // @returns Element
    // @description
    // Returns the ChatColor that makes the following characters Light Red.
    // -->

    // <--[tag]
    // @attribute <&d>
    // @returns Element
    // @description
    // Returns the ChatColor that makes the following characters Magenta.
    // -->

    // <--[tag]
    // @attribute <&e>
    // @returns Element
    // @description
    // Returns the ChatColor that makes the following characters Yellow.
    // -->

    // <--[tag]
    // @attribute <&f>
    // @returns Element
    // @description
    // Returns the ChatColor that makes the following characters White.
    // -->

    // <--[tag]
    // @attribute <&k>
    // @returns Element
    // @description
    // Returns the ChatColor that makes the following characters obfuscated.
    // -->

    // <--[tag]
    // @attribute <&l>
    // @returns Element
    // @description
    // Returns the ChatColor that makes the following characters bold.
    // -->

    // <--[tag]
    // @attribute <&m>
    // @returns Element
    // @description
    // Returns the ChatColor that makes the following characters have a strike-through.
    // -->

    // <--[tag]
    // @attribute <&n>
    // @returns Element
    // @description
    // Returns the ChatColor that makes the following characters have an underline.
    // -->

    // <--[tag]
    // @attribute <&o>
    // @returns Element
    // @description
    // Returns the ChatColor that makes the following characters italicized.
    // -->

    // <--[tag]
    // @attribute <&r>
    // @returns Element
    // @description
    // Returns the ChatColor that resets the following characters to normal.
    // -->

    // <--[tag]
    // @attribute <black>
    // @returns Element
    // @description
    // Returns the ChatColor that makes the following characters Black.
    // -->

    // <--[tag]
    // @attribute <dark_blue>
    // @returns Element
    // @description
    // Returns the ChatColor that makes the following characters Dark Blue.
    // -->

    // <--[tag]
    // @attribute <dark_green>
    // @returns Element
    // @description
    // Returns the ChatColor that makes the following characters Dark Green.
    // -->

    // <--[tag]
    // @attribute <dark_aqua>
    // @returns Element
    // @description
    // Returns the ChatColor that makes the following characters Dark Cyan.
    // -->

    // <--[tag]
    // @attribute <dark_red>
    // @returns Element
    // @description
    // Returns the ChatColor that makes the following characters Dark Red.
    // -->

    // <--[tag]
    // @attribute <dark_purple>
    // @returns Element
    // @description
    // Returns the ChatColor that makes the following characters Dark Magenta.
    // -->

    // <--[tag]
    // @attribute <gold>
    // @returns Element
    // @description
    // Returns the ChatColor that makes the following characters Gold.
    // -->

    // <--[tag]
    // @attribute <gray>
    // @returns Element
    // @description
    // Returns the ChatColor that makes the following characters Light Gray.
    // -->

    // <--[tag]
    // @attribute <dark_gray>
    // @returns Element
    // @description
    // Returns the ChatColor that makes the following characters Dark Gray.
    // -->

    // <--[tag]
    // @attribute <blue>
    // @returns Element
    // @description
    // Returns the ChatColor that makes the following characters Light Blue.
    // -->

    // <--[tag]
    // @attribute <green>
    // @returns Element
    // @description
    // Returns the ChatColor that makes the following characters Light Green.
    // -->

    // <--[tag]
    // @attribute <aqua>
    // @returns Element
    // @description
    // Returns the ChatColor that makes the following characters Cyan.
    // -->

    // <--[tag]
    // @attribute <red>
    // @returns Element
    // @description
    // Returns the ChatColor that makes the following characters Light Red.
    // -->

    // <--[tag]
    // @attribute <light_purple>
    // @returns Element
    // @description
    // Returns the ChatColor that makes the following characters Magenta.
    // -->

    // <--[tag]
    // @attribute <yellow>
    // @returns Element
    // @description
    // Returns the ChatColor that makes the following characters Yellow.
    // -->

    // <--[tag]
    // @attribute <white>
    // @returns Element
    // @description
    // Returns the ChatColor that makes the following characters White.
    // -->

    // <--[tag]
    // @attribute <magic>
    // @returns Element
    // @description
    // Returns the ChatColor that makes the following characters obfuscated.
    // -->

    // <--[tag]
    // @attribute <bold>
    // @returns Element
    // @description
    // Returns the ChatColor that makes the following characters bold.
    // -->

    // <--[tag]
    // @attribute <strikethrough>
    // @returns Element
    // @description
    // Returns the ChatColor that makes the following characters have a strike-through.
    // -->

    // <--[tag]
    // @attribute <underline>
    // @returns Element
    // @description
    // Returns the ChatColor that makes the following characters have an underline.
    // -->

    // <--[tag]
    // @attribute <italic>
    // @returns Element
    // @description
    // Returns the ChatColor that makes the following characters italicized.
    // -->

    // <--[tag]
    // @attribute <reset>
    // @returns Element
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

        // TODO: Handle case-sensitivity stuff better here!

        if (event.getName().equals("&auml")) {
            event.setReplaced(new Element("ä").getAttribute(attribute.fulfill(1)));
        }
        else if (event.getName().equals("&Auml")) {
            event.setReplaced(new Element("Ä").getAttribute(attribute.fulfill(1)));
        }
        else if (event.getName().equals("&ouml")) {
            event.setReplaced(new Element("ö").getAttribute(attribute.fulfill(1)));
        }
        else if (event.getName().equals("&Ouml")) {
            event.setReplaced(new Element("Ö").getAttribute(attribute.fulfill(1)));
        }
        else if (event.getName().equals("&uuml")) {
            event.setReplaced(new Element("ü").getAttribute(attribute.fulfill(1)));
        }
        else if (event.getName().equals("&Uuml")) {
            event.setReplaced(new Element("Ü").getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <&nl>
        // @returns Element
        // @description
        // Returns a newline symbol.
        // -->
        if (lower.equals("&nl")) {
            event.setReplaced(new Element("\n").getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <&amp>
        // @returns Element
        // @description
        // Returns an ampersand symbol: &
        // -->
        else if (lower.equals("&amp")) {
            event.setReplaced(new Element("&").getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <&cm>
        // @returns Element
        // @description
        // Returns a comma symbol: ,
        // -->
        else if (lower.equals("&cm")) {
            event.setReplaced(new Element(",").getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <&ss>
        // @returns Element
        // @description
        // Returns an internal coloring symbol: §
        // -->
        else if (lower.equals("&ss")) {
            event.setReplaced(new Element("§").getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <&sq>
        // @returns Element
        // @description
        // Returns a single-quote symbol: '
        // -->
        else if (lower.equals("&sq")) {
            event.setReplaced(new Element("'").getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <&sp>
        // @returns Element
        // @description
        // Returns a space symbol.
        // -->
        else if (lower.equals("&sp")) {
            event.setReplaced(new Element(String.valueOf(' ')).getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <&nbsp>
        // @returns Element
        // @description
        // Returns a non-breaking space symbol.
        // -->
        else if (lower.equals("&nbsp")) {
            event.setReplaced(new Element(String.valueOf((char) 0x00A0)).getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <&dq>
        // @returns Element
        // @description
        // Returns a double-quote symbol: "
        // -->
        else if (lower.equals("&dq")) {
            event.setReplaced(new Element("\"").getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <&co>
        // @returns Element
        // @description
        // Returns a colon symbol: :
        // -->
        else if (lower.equals("&co")) {
            event.setReplaced(new Element(":").getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <&sc>
        // @returns Element
        // @description
        // Returns a semicolon symbol: ;
        // -->
        else if (lower.equals("&sc")) {
            event.setReplaced(new Element(String.valueOf((char) 0x2011)).getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <&rb>
        // @returns Element
        // @description
        // Returns a right-bracket symbol: ]
        // -->
        else if (lower.equals("&rb")) {
            event.setReplaced(new Element("]").getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <&lb>
        // @returns Element
        // @description
        // Returns a left-bracket symbol: [
        // -->
        else if (lower.equals("&lb")) {
            event.setReplaced(new Element("[").getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <&rc>
        // @returns Element
        // @description
        // Returns a right-brace symbol: }
        // -->
        else if (lower.equals("&rc")) {
            event.setReplaced(new Element("}").getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <&lc>
        // @returns Element
        // @description
        // Returns a left-brace symbol: {
        // -->
        else if (lower.equals("&lc")) {
            event.setReplaced(new Element("{").getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <&ns>
        // @returns Element
        // @description
        // Returns a hash symbol: #
        // -->
        else if (lower.equals("&ns")) {
            event.setReplaced(new Element("#").getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <&pc>
        // @returns Element
        // @description
        // Returns a percent symbol: %
        // -->
        else if (lower.equals("&pc")) {
            event.setReplaced(new Element("%").getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <&pipe>
        // @returns Element
        // @description
        // Returns a pipe symbol: |
        // -->
        else if (lower.equals("&pipe")) {
            event.setReplaced(new Element("|").getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <&ds>
        // @returns Element
        // @description
        // Returns a dollar sign: $
        // -->
        else if (lower.equals("&ds")) {
            event.setReplaced(new Element("$").getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <&lt>
        // @returns Element
        // @description
        // Returns a less than symbol: <
        // -->
        else if (lower.equals("&lt")) {
            event.setReplaced(new Element(String.valueOf((char) 0x01)).getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <&gt>
        // @returns Element
        // @description
        // Returns a greater than symbol: >
        // -->
        else if (lower.equals("&gt")) {
            event.setReplaced(new Element(String.valueOf((char) 0x02)).getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <&bs>
        // @returns Element
        // @description
        // Returns a backslash symbol: \
        // -->
        else if (lower.equals("&bs")) {
            event.setReplaced(new Element("\\").getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <&at>
        // @returns Element
        // @description
        // Returns an at symbol: @
        // -->
        else if (lower.equals("&at")) {
            event.setReplaced(new Element("@").getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <&dot>
        // @returns Element
        // @description
        // Returns a dot symbol: .
        // -->
        else if (lower.equals("&dot")) {
            event.setReplaced(new Element(".").getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <&hrt>
        // @returns Element
        // @description
        // Returns a heart symbol: ♥
        // -->
        else if (lower.equals("&hrt")) {
            event.setReplaced(new Element("\u2665").getAttribute(attribute.fulfill(1)));
        }

        // <--[tag]
        // @attribute <&chr[<character>]>
        // @returns Element
        // @description
        // Returns the Unicode character specified. e.g. <&chr[2665]> returns a heart.
        // -->
        if (attribute.startsWith("&chr") && attribute.hasContext(1)) {
            event.setReplaced(String.valueOf((char) Integer.parseInt(attribute.getContext(1), 16)));
        }

    }
}
