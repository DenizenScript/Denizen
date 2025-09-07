package com.denizenscript.denizen.paper.tags;

import com.denizenscript.denizen.paper.properties.PaperElementExtensions;
import com.denizenscript.denizen.paper.utilities.FormattedTextHelper;
import com.denizenscript.denizen.paper.utilities.FormattedTextHelper.LegacyColor;
import com.denizenscript.denizen.paper.utilities.HoverFormatHelper;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ColorTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.tags.core.EscapeTagUtil;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;

public class TextFormattingTags {

    public TextFormattingTags() {

        // <--[tag]
        // @attribute <&hover[<hover_text>]>
        // @returns ElementTag
        // @Plugin Paper
        // @description
        // Returns a special chat code that makes the following text display the input hover text when the mouse is left over it.
        // This tag must be followed by an <&end_hover> tag.
        // For example: - narrate "There is a <&hover[you found it!]>secret<&end_hover> in this message!"
        // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
        // -->
        TagManager.registerTagHandler(ElementTag.class, ObjectTag.class, "&hover", (attribute, hover) -> { // Cannot be static due to hacked sub-tag

            // <--[tag]
            // @attribute <&hover[<hover_text>].type[<type>]>
            // @returns ElementTag
            // @Plugin Paper
            // @description
            // Returns a special chat code that makes the following text display the input hover text when the mouse is left over it.
            // This tag must be followed by an <&end_hover> tag.
            // Available hover types: SHOW_TEXT, SHOW_ITEM, or SHOW_ENTITY.
            // For example: - narrate "There is a <&hover[you found it!].type[SHOW_TEXT]>secret<&end_hover> in this message!"
            // Note: for "SHOW_ITEM", replace the text with a valid ItemTag. For "SHOW_ENTITY", replace the text with a valid spawned EntityTag (requires F3+H to see entities).
            // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
            // -->
            HoverEvent.Action<?> type = HoverEvent.Action.SHOW_TEXT;
            if (attribute.startsWith("type", 2)) {
                attribute.fulfill(1);
                if (!attribute.hasParam()) {
                    attribute.echoError("Must specify an hover type.");
                    return null;
                }
                type = HoverEvent.Action.NAMES.value(CoreUtilities.toLowerCase(attribute.getParam()));
                if (type == null) {
                    attribute.echoError("Invalid hover type specified.");
                    return null;
                }
            }
            String hoverData = HoverFormatHelper.parseObjectToHover(hover, type, attribute);
            if (hoverData == null) {
                return null;
            }
            return new ElementTag(FormattedTextHelper.LEGACY_SECTION + "[hover=" + type + ';' + FormattedTextHelper.escape(hoverData) + ']', true);
        });

        // <--[tag]
        // @attribute <&click[<click_command>]>
        // @returns ElementTag
        // @Plugin Paper
        // @description
        // Returns a special chat code that makes the following text execute the input command line value when clicked.
        // To execute a command "/" should be used at the start. Otherwise, it will display as chat.
        // This tag must be followed by an <&end_click> tag.
        // For example: - narrate "You can <&click[wow]>click here<&end_click> to say wow!"
        // For example: - narrate "You can <&click[/help]>click here<&end_click> for help!"
        // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
        // -->
        TagManager.registerTagHandler(ElementTag.class, "&click", (attribute) -> { // Cannot be static due to hacked sub-tag
            if (!attribute.hasParam()) {
                return null;
            }
            String clickText = attribute.getParam();

            // <--[tag]
            // @attribute <&click[<click_command>].type[<type>]>
            // @returns ElementTag
            // @Plugin Paper
            // @description
            // Returns a special chat code that makes the following text execute the input command when clicked.
            // This tag must be followed by an <&end_click> tag.
            // Available command types: OPEN_URL, OPEN_FILE, RUN_COMMAND, SUGGEST_COMMAND, COPY_TO_CLIPBOARD, or CHANGE_PAGE.
            // For example: - narrate "You can <&click[https://denizenscript.com].type[OPEN_URL]>click here<&end_click> to learn about Denizen!"
            // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
            // -->
            String type = "RUN_COMMAND";
            if (attribute.startsWith("type", 2)) {
                type = attribute.getContext(2);
                attribute.fulfill(1);
            }
            return new ElementTag(FormattedTextHelper.LEGACY_SECTION + "[click=" + type + ";" + FormattedTextHelper.escape(clickText) + "]", true);
        });

        // <--[tag]
        // @attribute <&insertion[<message>]>
        // @returns ElementTag
        // @Plugin Paper
        // @description
        // Returns a special chat code that makes the following text insert the input message to chat when shift-clicked.
        // This tag must be followed by an <&end_insertion> tag.
        // For example: - narrate "You can <&insertion[wow]>click here<&end_insertion> to add 'wow' to your chat!"
        // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
        // -->
        TagManager.registerStaticTagBaseHandler(ElementTag.class, "&insertion", (attribute) -> {
            if (!attribute.hasParam()) {
                return null;
            }
            String insertText = attribute.getParam();
            return new ElementTag(FormattedTextHelper.LEGACY_SECTION + "[insertion=" + FormattedTextHelper.escape(insertText) + "]", true);
        });

        // <--[tag]
        // @attribute <&end_click>
        // @returns ElementTag
        // @Plugin Paper
        // @description
        // Returns a special chat code that ends a '&click' tag.
        // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
        // -->
        TagManager.registerStaticTagBaseHandler(ElementTag.class, "&end_click", (attribute) -> {
            return new ElementTag(FormattedTextHelper.LEGACY_SECTION + "[/click]", true);
        });

        // <--[tag]
        // @attribute <&end_hover>
        // @returns ElementTag
        // @Plugin Paper
        // @description
        // Returns a special chat code that ends a '&hover' tag.
        // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
        // -->
        TagManager.registerStaticTagBaseHandler(ElementTag.class, "&end_hover", (attribute) -> {
            return new ElementTag(FormattedTextHelper.LEGACY_SECTION + "[/hover]", true);
        });

        // <--[tag]
        // @attribute <&end_insertion>
        // @returns ElementTag
        // @Plugin Paper
        // @description
        // Returns a special chat code that ends an '&insertion' tag.
        // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
        // -->
        TagManager.registerStaticTagBaseHandler(ElementTag.class, "&end_insertion", (attribute) -> {
            return new ElementTag(FormattedTextHelper.LEGACY_SECTION + "[/insertion]", true);
        });

        // <--[tag]
        // @attribute <&keybind[<key>]>
        // @returns ElementTag
        // @Plugin Paper
        // @description
        // Returns a special chat code that displays a keybind.
        // For example: - narrate "Press your <&keybind[key.jump]> key!"
        // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
        // -->
        TagManager.registerStaticTagBaseHandler(ElementTag.class, "&keybind", (attribute) -> {
            if (!attribute.hasParam()) {
                return null;
            }
            String keybindText = attribute.getParam();
            return new ElementTag(FormattedTextHelper.LEGACY_SECTION + "[keybind=" + FormattedTextHelper.escape(keybindText) + "]", true);
        });

        // <--[tag]
        // @attribute <&selector[<key>]>
        // @returns ElementTag
        // @Plugin Paper
        // @description
        // Returns a special chat code that displays a vanilla selector.
        // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
        // -->
        TagManager.registerStaticTagBaseHandler(ElementTag.class, "&selector", (attribute) -> {
            if (!attribute.hasParam()) {
                return null;
            }
            String selectorText = attribute.getParam();
            return new ElementTag(FormattedTextHelper.LEGACY_SECTION + "[selector=" + FormattedTextHelper.escape(selectorText) + "]", true);
        });

        // <--[tag]
        // @attribute <&translate[key=<key>;(fallback=<fallback>);(with=<text>|...)]>
        // @returns ElementTag
        // @Plugin Paper
        // @description
        // Returns a special chat code that is read by the client to display an auto-translated message.
        // "key" is the translation key.
        // Optionally specify "fallback" as text to display when the client can't find a translation for the key.
        // Optionally specify "with" as a list of input data for the translatable message (parts of the message that are dynamic).
        // Be warned that language keys can change between Minecraft versions.
        // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
        // You can use <@link tag ElementTag.strip_color> to convert the translated output to plain text (pre-translated).
        // @example
        // # Narrates a translatable of a diamond sword's name.
        // - narrate "Reward: <&translate[key=item.minecraft.diamond_sword]>"
        // @example
        // # Narrates a translatable with some input data.
        // - narrate <&translate[key=commands.give.success.single;with=32|<&translate[key=item.minecraft.diamond_sword]>|<player.name>]>
        // @example
        // # Narrates a custom translatable (from something like a resource pack), with a fallback in case it can't be translated.
        // - narrate <&translate[key=my.custom.translation;fallback=Please use the resource pack!]>
        // -->
        TagManager.registerTagHandler(ElementTag.class, ObjectTag.class, "&translate", (attribute, param) -> { // Cannot be static due to hacked sub-tag
            MapTag translateMap = param.asType(MapTag.class, CoreUtilities.noDebugContext);
            if (translateMap == null) {
                BukkitImplDeprecations.translateLegacySyntax.warn(attribute.context);
                translateMap = new MapTag();
                translateMap.putObject("key", param);

                // <--[tag]
                // @attribute <&translate[<key>].with[<text>|...]>
                // @returns ElementTag
                // @Plugin Paper
                // @deprecated Use '<&translate[key=<key>;with=<text>|...]>'.
                // @description
                // Deprecated in favor of <@link tag &translate>.
                // -->
                if (attribute.startsWith("with", 2)) {
                    translateMap.putObject("with", new ListTag(attribute.contextAsType(2, ListTag.class), with -> new ElementTag(EscapeTagUtil.unEscape(with), true)));
                    attribute.fulfill(1);
                }
            }
            return new ElementTag(FormattedTextHelper.LEGACY_SECTION + "[translate=" + FormattedTextHelper.escape(translateMap.savable()) + ']', true);
        });

        // <--[tag]
        // @attribute <&score[<name>|<objective>(|<value>)]>
        // @returns ElementTag
        // @Plugin Paper
        // @description
        // Returns a special chat code that displays a scoreboard entry. Input is an escaped list of:
        // Name of the relevant entity, name of the objective, then optionally a value (if unspecified, will use current scoreboard value).
        // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
        //
        // -->
        TagManager.registerStaticTagBaseHandler(ElementTag.class, "&score", (attribute) -> {
            if (!attribute.hasParam()) {
                return null;
            }
            ListTag scoreList = attribute.paramAsType(ListTag.class);
            if (scoreList.size() < 2) {
                return null;
            }
            String name = FormattedTextHelper.escape(EscapeTagUtil.unEscape(scoreList.get(0)));
            String objective = FormattedTextHelper.escape(EscapeTagUtil.unEscape(scoreList.get(1)));
            String value = scoreList.size() >= 3 ? FormattedTextHelper.escape(EscapeTagUtil.unEscape(scoreList.get(2))) : "";
            return new ElementTag(FormattedTextHelper.LEGACY_SECTION + "[score=" + name + ";" + objective + ";" + value + "]", true);
        });

        // <--[tag]
        // @attribute <&color[<color>]>
        // @returns ElementTag
        // @Plugin Paper
        // @description
        // Returns a chat code that makes the following text be the specified color.
        // Color can be a color name, color code, hex, or ColorTag... that is: "&color[gold]", "&color[6]", and "&color[#AABB00]" are all valid.
        // The ColorTag input option can be used for dynamic color effects, such as automatic rainbows.
        // -->
        TagManager.registerStaticTagBaseHandler(ElementTag.class, "&color", (attribute) -> {
            if (!attribute.hasParam()) {
                return null;
            }
            String colorName = attribute.getParam();
            String colorOut = null;
            if (colorName.length() == 1) {
                LegacyColor color = LegacyColor.legacyFromChar(colorName.charAt(0));
                if (color != null) {
                    colorOut = color.toString();
                }
            }
            else if (colorName.length() == 7 && colorName.startsWith("#")) {
                colorOut = FormattedTextHelper.stringifyRGBSpigot(colorName.substring(1));
            }
            else if (colorName.startsWith("co@") || colorName.lastIndexOf(',') > colorName.indexOf(',')) {
                ColorTag color = ColorTag.valueOf(colorName, attribute.context);
                if (color == null && TagManager.isStaticParsing) {
                    return null;
                }
                String hex = Integer.toHexString(color.asRGB());
                colorOut = FormattedTextHelper.stringifyRGBSpigot(hex);
            }
            if (colorOut == null) {
                NamedTextColor color = NamedTextColor.NAMES.value(CoreUtilities.toLowerCase(colorName));
                if (color == null) {
                    attribute.echoError("Color '" + colorName + "' doesn't exist (for tag &color[...]).");
                    return null;
                }
                colorOut = LegacyColor.fromModern(color).toString();
            }
            return new ElementTag(colorOut, true);
        });

        // <--[tag]
        // @attribute <&gradient[from=<color>;to=<color>;(style={RGB}/HSB)]>
        // @returns ElementTag
        // @Plugin Paper
        // @description
        // Returns a chat code that makes the following text be the specified color.
        // Input works equivalently to <@link tag ElementTag.color_gradient>, return to that tag for more documentation detail and input examples.
        // The gradient runs from whatever text is after this gradient, until the next color tag (0-9, a-f, 'r' reset, or an RGB code. Does not get stop at formatting codes, they will be included in the gradient).
        // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
        // @example
        // - narrate "<&gradient[from=black;to=white]>these are the shades of gray <white>that solidifies to pure white"
        // -->
        TagManager.registerStaticTagBaseHandler(ElementTag.class, MapTag.class, "&gradient", (attribute, inputMap) -> {
            ColorTag fromColor = inputMap.getRequiredObjectAs("from", ColorTag.class, attribute);
            ColorTag toColor = inputMap.getRequiredObjectAs("to", ColorTag.class, attribute);
            ElementTag style = inputMap.getElement("style", "RGB");
            if (fromColor == null || toColor == null) {
                return null;
            }
            if (!style.matchesEnum(PaperElementExtensions.GradientStyle.class)) {
                attribute.echoError("Invalid gradient style '" + style + "'");
                return null;
            }
            return new ElementTag(FormattedTextHelper.LEGACY_SECTION + "[gradient=" + fromColor + ";" + toColor + ";" + style + "]", true);
        });

        // <--[tag]
        // @attribute <&font[<font>]>
        // @returns ElementTag
        // @Plugin Paper
        // @description
        // Returns a chat code that makes the following text display with the specified font.
        // The default font is "minecraft:default".
        // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
        // -->
        TagManager.registerStaticTagBaseHandler(ElementTag.class, "&font", (attribute) -> {
            if (!attribute.hasParam()) {
                return null;
            }
            return new ElementTag(FormattedTextHelper.LEGACY_SECTION + "[font=" + attribute.getParam() + "]", true);
        });

        // <--[tag]
        // @attribute <&optimize>
        // @returns ElementTag
        // @Plugin Paper
        // @description
        // Returns a chat code that tells the formatted text parser to try to produce mininalist JSON text.
        // This is useful in particular for very long text or where text is being sent rapidly/repeatedly.
        // It is not needed in most normal messages.
        // It will produce incompatibility issues if used in items or other locations where raw JSON matching is required.
        // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
        // -->
        TagManager.registerStaticTagBaseHandler(ElementTag.class, "&optimize", (attribute) -> {
            return new ElementTag(FormattedTextHelper.LEGACY_SECTION + "[optimize=true]", true);
        });
    }
}
