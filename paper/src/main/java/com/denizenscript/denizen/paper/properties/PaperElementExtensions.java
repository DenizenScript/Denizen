package com.denizenscript.denizen.paper.properties;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.paper.PaperModule;
import com.denizenscript.denizen.paper.utilities.FormattedTextHelper;
import com.denizenscript.denizen.paper.utilities.FormattedTextHelper.LegacyColor;
import com.denizenscript.denizen.paper.utilities.FormattedTextHelper.LegacyFormatting;
import com.denizenscript.denizen.paper.utilities.HoverFormatHelper;
import com.denizenscript.denizen.tags.core.CustomColorTagBase;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import com.denizenscript.denizen.utilities.PaperAPITools;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ColorTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.AsciiMatcher;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class PaperElementExtensions {


    public static void register() {

        // <--[tag]
        // @attribute <ElementTag.strip_color>
        // @returns ElementTag
        // @Plugin Paper
        // @group text manipulation
        // @description
        // Returns the element with all color encoding stripped.
        // This will remove any/all colors, formats (bold/italic/etc), advanced formats (fonts/clickables/etc), and translate any translatables (&translate, &score, etc).
        // This will automatically translate translatable sections
        // -->
        ElementTag.tagProcessor.registerStaticTag(ElementTag.class, "strip_color", (attribute, object) -> {
            return new ElementTag(PlainTextComponentSerializer.plainText().serialize(FormattedTextHelper.parse(object.asString(), NamedTextColor.WHITE)), true);
        });

        // <--[tag]
        // @attribute <ElementTag.to_raw_json>
        // @returns ElementTag
        // @Plugin Paper
        // @group conversion
        // @description
        // Converts normal colored text to Minecraft-style "raw JSON" format.
        // Inverts <@link tag ElementTag.from_raw_json>.
        // -->
        ElementTag.tagProcessor.registerStaticTag(ElementTag.class, "to_raw_json", (attribute, object) -> {
            return new ElementTag(PaperModule.componentToJson(FormattedTextHelper.parse(object.asString(), NamedTextColor.WHITE)), true);
        });

        // <--[tag]
        // @attribute <ElementTag.from_raw_json>
        // @returns ElementTag
        // @Plugin Paper
        // @group conversion
        // @description
        // Un-hides the element's text from invisible color codes back to normal text.
        // Inverts <@link tag ElementTag.to_raw_json>.
        // -->
        ElementTag.tagProcessor.registerStaticTag(ElementTag.class, "from_raw_json", (attribute, object) -> {
            return new ElementTag(FormattedTextHelper.stringify(PaperModule.jsonToComponent(object.asString())));
        });

        // <--[tag]
        // @attribute <ElementTag.optimize_json>
        // @returns ElementTag
        // @Plugin Paper
        // @group conversion
        // @description
        // Tells the formatted text parser to try to produce mininalist JSON text.
        // This is useful in particular for very long text or where text is being sent rapidly/repeatedly.
        // It is not needed in most normal messages.
        // It will produce incompatibility issues if used in items or other locations where raw JSON matching is required.
        // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
        // -->
        ElementTag.tagProcessor.registerStaticTag(ElementTag.class, "optimize_json", (attribute, object) -> {
            String opti = FormattedTextHelper.LEGACY_SECTION + "[optimize=true]";
            if (object.asString().contains(opti)) {
                return object;
            }
            return new ElementTag(opti + object.asString(), true);
        });

        // <--[tag]
        // @attribute <ElementTag.hover_item[<item>]>
        // @returns ElementTag
        // @Plugin Paper
        // @group text manipulation
        // @description
        // Adds a hover message to the element, which makes the element display the input ItemTag when the mouse is left over it.
        // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
        // @example
        // - narrate "You can <element[hover here].custom_color[emphasis].hover_item[<player.item_in_hand>]> to see what you held!"
        // -->
        ElementTag.tagProcessor.registerStaticTag(ElementTag.class, ItemTag.class, "hover_item", (attribute, object, item) -> {
            return new ElementTag(FormattedTextHelper.LEGACY_SECTION + "[hover=SHOW_ITEM;" + FormattedTextHelper.escape(item.identify()) + "]" + object.asString() + FormattedTextHelper.LEGACY_SECTION + "[/hover]", true);
        });

        // <--[tag]
        // @attribute <ElementTag.on_hover[<message>]>
        // @returns ElementTag
        // @Plugin Paper
        // @group text manipulation
        // @description
        // Adds a hover message to the element, which makes the element display the input hover text when the mouse is left over it.
        // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
        // -->
        ElementTag.tagProcessor.registerTag(ElementTag.class, ObjectTag.class, "on_hover", (attribute, object, hover) -> { // non-static due to hacked sub-tag
            HoverEvent.Action<?> type = HoverEvent.Action.SHOW_TEXT;

            // <--[tag]
            // @attribute <ElementTag.on_hover[<message>].type[<type>]>
            // @returns ElementTag
            // @Plugin Paper
            // @group text manipulation
            // @description
            // Adds a hover message to the element, which makes the element display the input hover text when the mouse is left over it.
            // Available hover types: SHOW_TEXT, SHOW_ITEM, or SHOW_ENTITY.
            // Note: for "SHOW_ITEM", replace the text with a valid ItemTag. For "SHOW_ENTITY", replace the text with a valid spawned EntityTag (requires F3+H to see entities).
            // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
            // For show_text, prefer <@link tag ElementTag.on_hover>
            // For show_item, prefer <@link tag ElementTag.hover_item>
            // -->
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
            return new ElementTag(FormattedTextHelper.LEGACY_SECTION + "[hover=" + type + ';' + FormattedTextHelper.escape(hoverData) + ']'
                    + object.asString() + FormattedTextHelper.LEGACY_SECTION + "[/hover]", true);
        });

        // <--[tag]
        // @attribute <ElementTag.click_url[<url>]>
        // @returns ElementTag
        // @Plugin Paper
        // @group text manipulation
        // @description
        // Adds a click command to the element, which makes the element open the given URL when clicked.
        // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
        // @example
        // - narrate "You can <element[click here].custom_color[emphasis].on_hover[Click me!].click_url[https://denizenscript.com]> to learn about Denizen!"
        // -->
        ElementTag.tagProcessor.registerStaticTag(ElementTag.class, ElementTag.class, "click_url", (attribute, object, url) -> {
            return new ElementTag(FormattedTextHelper.LEGACY_SECTION + "[click=OPEN_URL;" + FormattedTextHelper.escape(url.asString()) + "]" + object.asString() + FormattedTextHelper.LEGACY_SECTION + "[/click]", true);
        });

        // <--[tag]
        // @attribute <ElementTag.click_chat[<message>]>
        // @returns ElementTag
        // @Plugin Paper
        // @group text manipulation
        // @description
        // Adds a click command to the element, which makes the element pseudo-chat the input message when clicked, for activating interact script chat triggers (<@link language Chat Triggers>).
        // This internally uses the command "/denizenclickable chat SOME MESSAGE HERE" (requires players have permission "denizen.clickable")
        // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
        // @example
        // - narrate "You can <element[click here].click_chat[hello]> to say hello to an NPC's interact script!"
        // -->
        ElementTag.tagProcessor.registerStaticTag(ElementTag.class, ElementTag.class, "click_chat", (attribute, object, chat) -> {
            return new ElementTag(FormattedTextHelper.LEGACY_SECTION + "[click=RUN_COMMAND;/denizenclickable chat " + FormattedTextHelper.escape(chat.asString()) + "]" + object.asString() + FormattedTextHelper.LEGACY_SECTION + "[/click]", true);
        });

        // <--[tag]
        // @attribute <ElementTag.on_click[<command>]>
        // @returns ElementTag
        // @Plugin Paper
        // @group text manipulation
        // @description
        // Adds a click command to the element, which makes the element execute the input command when clicked.
        // To execute a command "/" should be used at the start. Prior to 1.19, leaving off the "/" would display the text as chat. This feature was removed as part of the 1.19 secure chat system.
        // For activating interact script chat triggers (<@link language Chat Triggers>), you can use the command "/denizenclickable chat SOME MESSAGE HERE" (requires players have permission "denizen.clickable")
        // For that, instead prefer <@link tag ElementTag.click_chat>
        // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
        // @example
        // - narrate "You can <element[click here].on_click[/help]> for help!"
        // @example
        // - narrate "You can <element[click here].on_click[/denizenclickable chat hello]> to say hello to an NPC's interact script!"
        // -->
        ElementTag.tagProcessor.registerTag(ElementTag.class, ElementTag.class, "on_click", (attribute, object, command) -> { // non-static due to hacked sub-tag
            String type = "RUN_COMMAND";

            // <--[tag]
            // @attribute <ElementTag.on_click[<message>].type[<type>]>
            // @returns ElementTag
            // @Plugin Paper
            // @group text manipulation
            // @description
            // Adds a click command to the element, which makes the element execute the input command when clicked.
            // Available command types: OPEN_URL, OPEN_FILE, RUN_COMMAND, SUGGEST_COMMAND, COPY_TO_CLIPBOARD, or CHANGE_PAGE.
            // For example: - narrate "You can <element[click here].on_click[https://denizenscript.com].type[OPEN_URL]> to learn about Denizen!"
            // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
            // For run_command, prefer <@link tag ElementTag.on_click>
            // For chat, prefer <@link tag ElementTag.click_chat>
            // For URLs, prefer <@link tag ElementTag.click_url>
            // -->
            if (attribute.startsWith("type", 2)) {
                type = attribute.getContext(2);
                attribute.fulfill(1);
            }
            return new ElementTag(FormattedTextHelper.LEGACY_SECTION + "[click=" + type + ";" + FormattedTextHelper.escape(command.asString()) + "]"
                    + object.asString() + FormattedTextHelper.LEGACY_SECTION + "[/click]", true);
        });

        // <--[tag]
        // @attribute <ElementTag.with_insertion[<message>]>
        // @returns ElementTag
        // @Plugin Paper
        // @group text manipulation
        // @description
        // Adds an insertion message to the element, which makes the element insert the input message to chat when shift-clicked.
        // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
        // -->
        ElementTag.tagProcessor.registerStaticTag(ElementTag.class, ElementTag.class, "with_insertion", (attribute, object, insertion) -> {
            return new ElementTag(FormattedTextHelper.LEGACY_SECTION + "[insertion="  + FormattedTextHelper.escape(insertion.asString()) + "]"
                    + object.asString() + FormattedTextHelper.LEGACY_SECTION + "[/insertion]", true);
        });

        // <--[tag]
        // @attribute <ElementTag.no_reset>
        // @returns ElementTag
        // @Plugin Paper
        // @group text manipulation
        // @description
        // Makes a color code (&0123456789abcdef) not reset other formatting details.
        // Use like '<&c.no_reset>' or '<red.no_reset>'.
        // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
        // -->
        ElementTag.tagProcessor.registerStaticTag(ElementTag.class, "no_reset", (attribute, object) -> {
            if (object.asString().length() == 2 && object.asString().charAt(0) == FormattedTextHelper.LEGACY_SECTION) {
                return new ElementTag(FormattedTextHelper.LEGACY_SECTION + "[color=" + object.asString().charAt(1) + "]", true);
            }
            return null;
        });

        // <--[tag]
        // @attribute <ElementTag.end_format>
        // @returns ElementTag
        // @Plugin Paper
        // @group text manipulation
        // @description
        // Makes a chat format code (&klmno, or &[font=...]) be the end of a format, as opposed to the start.
        // Use like '<&o.end_format>' or '<italic.end_format>'.
        // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
        // -->
        ElementTag.tagProcessor.registerStaticTag(ElementTag.class, "end_format", (attribute, object) -> {
            if (object.asString().length() == 2 && object.asString().charAt(0) == FormattedTextHelper.LEGACY_SECTION) {
                return new ElementTag(FormattedTextHelper.LEGACY_SECTION + "[reset=" + object.asString().charAt(1) + "]", true);
            }
            else if (object.asString().startsWith(FormattedTextHelper.LEGACY_SECTION + "[font=") && object.asString().endsWith("]")) {
                return new ElementTag(FormattedTextHelper.LEGACY_SECTION + "[reset=font]", true);
            }
            return null;
        });

        // <--[tag]
        // @attribute <ElementTag.italicize>
        // @returns ElementTag
        // @Plugin Paper
        // @group text manipulation
        // @description
        // Makes the input text italic. Equivalent to "<&o><ELEMENT_HERE><&o.end_format>"
        // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
        // -->
        ElementTag.tagProcessor.registerStaticTag(ElementTag.class, "italicize", (attribute, object) -> {
            return new ElementTag(LegacyFormatting.ITALIC + object.asString() + FormattedTextHelper.LEGACY_SECTION + "[reset=o]", true);
        });

        // <--[tag]
        // @attribute <ElementTag.bold>
        // @returns ElementTag
        // @Plugin Paper
        // @group text manipulation
        // @description
        // Makes the input text bold. Equivalent to "<&l><ELEMENT_HERE><&l.end_format>"
        // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
        // -->
        ElementTag.tagProcessor.registerStaticTag(ElementTag.class, "bold", (attribute, object) -> {
            return new ElementTag(LegacyFormatting.BOLD + object.asString() + FormattedTextHelper.LEGACY_SECTION + "[reset=l]", true);
        });

        // <--[tag]
        // @attribute <ElementTag.underline>
        // @returns ElementTag
        // @Plugin Paper
        // @group text manipulation
        // @description
        // Makes the input text underlined. Equivalent to "<&n><ELEMENT_HERE><&n.end_format>"
        // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
        // -->
        ElementTag.tagProcessor.registerStaticTag(ElementTag.class, "underline", (attribute, object) -> {
            return new ElementTag(LegacyFormatting.UNDERLINE + object.asString() + FormattedTextHelper.LEGACY_SECTION + "[reset=n]", true);
        });

        // <--[tag]
        // @attribute <ElementTag.strikethrough>
        // @returns ElementTag
        // @Plugin Paper
        // @group text manipulation
        // @description
        // Makes the input text struck-through. Equivalent to "<&m><ELEMENT_HERE><&m.end_format>"
        // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
        // -->
        ElementTag.tagProcessor.registerStaticTag(ElementTag.class, "strikethrough", (attribute, object) -> {
            return new ElementTag(LegacyFormatting.STRIKETHROUGH + object.asString() + FormattedTextHelper.LEGACY_SECTION + "[reset=m]", true);
        });

        // <--[tag]
        // @attribute <ElementTag.obfuscate>
        // @returns ElementTag
        // @Plugin Paper
        // @group text manipulation
        // @description
        // Makes the input text obfuscated. Equivalent to "<&k><ELEMENT_HERE><&k.end_format>"
        // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
        // -->
        ElementTag.tagProcessor.registerStaticTag(ElementTag.class, "obfuscate", (attribute, object) -> {
            return new ElementTag(LegacyFormatting.OBFUSCATED + object.asString() + FormattedTextHelper.LEGACY_SECTION + "[reset=k]", true);
        });

        // <--[tag]
        // @attribute <ElementTag.custom_color[<custom_color_name>]>
        // @returns ElementTag
        // @Plugin Paper
        // @group text manipulation
        // @description
        // Makes the input text colored by the custom color value based on the common base color names defined in the Denizen config file.
        // If the color name is unrecognized, returns the value of color named 'default'.
        // Default color names are 'base', 'emphasis', 'warning', 'error'.
        // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
        // -->
        ElementTag.tagProcessor.registerStaticTag(ElementTag.class, ElementTag.class, "custom_color", (attribute, object, name) -> {
            String color = CustomColorTagBase.getColor(name.asLowerString(), attribute.context);
            if (color == null) {
                return null;
            }
            return new ElementTag(FormattedTextHelper.LEGACY_SECTION + "[color=f]" + color + object.asString() + FormattedTextHelper.LEGACY_SECTION + "[reset=color]", true);
        });

        // <--[tag]
        // @attribute <ElementTag.color[<color>]>
        // @returns ElementTag
        // @Plugin Paper
        // @group text manipulation
        // @description
        // Makes the input text colored by the input color. Equivalent to "<COLOR><ELEMENT_HERE><COLOR.end_format>"
        // Color can be a color name, color code, hex, or ColorTag... that is: ".color[gold]", ".color[6]", and ".color[#AABB00]" are all valid.
        // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
        // -->
        ElementTag.tagProcessor.registerStaticTag(ElementTag.class, ElementTag.class, "color", (attribute, object, colorElement) -> {
            String colorName = colorElement.asString();
            String colorOut = null;
            if (colorName.length() == 1) {
                LegacyColor color = LegacyColor.legacyFromChar(colorName.charAt(0));
                if (color != null) {
                    colorOut = color.toString();
                }
            }
            else if (colorName.length() == 7 && colorName.startsWith("#")) {
                return new ElementTag(FormattedTextHelper.LEGACY_SECTION + "[color=" + colorName + "]" + object.asString() + FormattedTextHelper.LEGACY_SECTION + "[reset=color]", true);
            }
            else if (colorName.length() == 14 && colorName.startsWith(FormattedTextHelper.LEGACY_SECTION + "x")) {
                return new ElementTag(FormattedTextHelper.LEGACY_SECTION + "[color=#" + CoreUtilities.replace(colorName.substring(2), String.valueOf(FormattedTextHelper.LEGACY_SECTION), "") + "]" + object.asString() + FormattedTextHelper.LEGACY_SECTION + "[reset=color]", true);
            }
            else if (colorName.startsWith("co@")) {
                ColorTag color = ColorTag.valueOf(colorName, attribute.context);
                if (color == null && TagManager.isStaticParsing) {
                    return null;
                }
                StringBuilder hex = new StringBuilder(Integer.toHexString(color.asRGB()));
                while (hex.length() < 6) {
                    hex.insert(0, "0");
                }
                return new ElementTag(FormattedTextHelper.LEGACY_SECTION + "[color=#" + hex + "]" + object.asString() + FormattedTextHelper.LEGACY_SECTION + "[reset=color]", true);
            }
            if (colorOut == null) {
                NamedTextColor namedColor = NamedTextColor.NAMES.value(CoreUtilities.toLowerCase(colorName));
                if (namedColor == null) {
                    ColorTag color = ColorTag.valueOf(colorName, attribute.context);
                    if (color != null) {
                        StringBuilder hex = new StringBuilder(Integer.toHexString(color.asRGB()));
                        while (hex.length() < 6) {
                            hex.insert(0, "0");
                        }
                        return new ElementTag(FormattedTextHelper.LEGACY_SECTION + "[color=#" + hex + "]" + object.asString() + FormattedTextHelper.LEGACY_SECTION + "[reset=color]", true);
                    }
                    if (!TagManager.isStaticParsing) {
                        attribute.echoError("Color '" + colorName + "' doesn't exist (for ElementTag.color[...]).");
                    }
                    return null;
                }
                colorOut = FormattedTextHelper.LEGACY_SECTION + "[color=" + LegacyColor.fromModern(namedColor).colorChar + "]";

            }
            return new ElementTag(colorOut + object.asString() + FormattedTextHelper.LEGACY_SECTION + "[reset=color]", true);
        });

        // <--[tag]
        // @attribute <ElementTag.font[<font>]>
        // @returns ElementTag
        // @Plugin Paper
        // @group text manipulation
        // @description
        // Makes the input text display with the input font name. Equivalent to "<&font[new-font]><ELEMENT_HERE><&font[new-font].end_format>"
        // The default font is "minecraft:default".
        // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
        // -->
        ElementTag.tagProcessor.registerStaticTag(ElementTag.class, ElementTag.class, "font", (attribute, object, fontName) -> {
            return new ElementTag(FormattedTextHelper.LEGACY_SECTION + "[font=" + fontName + "]" + object.asString() + FormattedTextHelper.LEGACY_SECTION + "[reset=font]", true);
        });

        // <--[tag]
        // @attribute <ElementTag.rainbow[(<pattern>)]>
        // @returns ElementTag
        // @Plugin Paper
        // @group text manipulation
        // @description
        // Returns the element with rainbow colors applied.
        // Optionally, specify a color pattern to follow. By default, this is "4c6e2ab319d5".
        // That is, a repeating color of: Red, Orange, Yellow, Green, Cyan, Blue, Purple.
        // -->
        ElementTag.tagProcessor.registerStaticTag(ElementTag.class, "rainbow", (attribute, object) -> {
            String str = object.asString();
            String pattern = "4c6e2ab319d5";
            if (attribute.hasParam()) {
                pattern = attribute.getParam();
            }
            StringBuilder output = new StringBuilder(str.length() * 3);
            for (int i = 0; i < str.length(); i++) {
                output.append(FormattedTextHelper.LEGACY_SECTION).append(pattern.charAt(i % pattern.length())).append(str.charAt(i));
            }
            return new ElementTag(output.toString(), true);
        });

        // <--[tag]
        // @attribute <ElementTag.hex_rainbow[(<length>)]>
        // @returns ElementTag
        // @Plugin Paper
        // @group text manipulation
        // @description
        // Returns the element with RGB rainbow colors applied.
        // Optionally, specify a length (how many characters before the colors repeat). If unspecified, will use the input element length.
        // If the element starts with a hex color code, that will be used as the starting color of the rainbow.
        // -->
        ElementTag.tagProcessor.registerStaticTag(ElementTag.class, "hex_rainbow", (attribute, object) -> {
            String str = object.asString();
            int[] HSB = new int[] { 0, 255, 255 };
            if (str.startsWith(FormattedTextHelper.LEGACY_SECTION + "x") && str.length() > 14) {
                char[] colors = new char[6];
                for (int i = 0; i < 6; i++) {
                    colors[i] = str.charAt(3 + (i * 2));
                }
                int rgb = Integer.parseInt(new String(colors), 16);
                HSB = ColorTag.fromRGB(rgb).toHSB();
                str = str.substring(14);
            }
            float hue = HSB[0] / 255f;
            int length = PlainTextComponentSerializer.plainText().serialize(FormattedTextHelper.parse(str, NamedTextColor.WHITE)).length();
            if (length == 0) {
                return new ElementTag("", true);
            }
            if (attribute.hasParam()) {
                length = attribute.getIntParam();
            }
            float increment = 1.0f / length;
            String addedFormat = "";
            StringBuilder output = new StringBuilder(str.length() * 8);
            for (int i = 0; i < str.length(); i++) {
                char c = str.charAt(i);
                if (c == FormattedTextHelper.LEGACY_SECTION && i + 1 < str.length()) {
                    char c2 = str.charAt(i + 1);
                    if (FORMAT_CODES_MATCHER.isMatch(c2)) {
                        addedFormat += String.valueOf(FormattedTextHelper.LEGACY_SECTION) + c2;
                    }
                    else {
                        addedFormat = "";
                    }
                    i++;
                    continue;
                }
                String hex = Integer.toHexString(ColorTag.fromHSB(HSB).asRGB());
                output.append(FormattedTextHelper.stringifyRGBSpigot(hex)).append(addedFormat).append(c);
                hue += increment;
                HSB[0] = Math.round(hue * 255f);
            }
            return new ElementTag(output.toString(), true);
        });

        // <--[tag]
        // @attribute <ElementTag.color_gradient[from=<color>;to=<color>;(style={RGB}/HSB)]>
        // @returns ElementTag
        // @Plugin Paper
        // @group text manipulation
        // @description
        // Returns the element with an RGB color gradient applied, with a unique color per character.
        // Specify the input as a map with keys 'from' and 'to' both set to hex colors (or any valid ColorTag).
        // You can also choose a style (defaults to RGB):
        // "style=RGB" tends to produce smooth gradients,
        // "style=HSB" tends to produce bright rainbow-like color patterns.
        // @example
        // - narrate "<element[these are the shades of gray].color_gradient[from=white;to=black]>"
        // @example
        // - narrate "<element[this looks kinda like fire doesn't it].color_gradient[from=#FF0000;to=#FFFF00]>"
        // @example
        // - narrate "<element[this also looks like fire with a different spread].color_gradient[from=#FF0000;to=#FFFF00;style=hsb]>"
        // @example
        // - narrate "<element[what a beautiful rainbow this line is].color_gradient[from=#FF0000;to=#0000FF;style=hsb]>"
        // -->
        ElementTag.tagProcessor.registerStaticTag(ElementTag.class, MapTag.class, "color_gradient", (attribute, object, inputMap) -> {
            ColorTag fromColor = inputMap.getRequiredObjectAs("from", ColorTag.class, attribute);
            ColorTag toColor = inputMap.getRequiredObjectAs("to", ColorTag.class, attribute);
            ElementTag style = inputMap.getElement("style", "RGB");
            if (fromColor == null || toColor == null) {
                return null;
            }
            if (!style.matchesEnum(GradientStyle.class)) {
                attribute.echoError("Invalid gradient style '" + style + "'");
                return null;
            }
            String res = doGradient(object.asString(), fromColor, toColor, style.asEnum(GradientStyle.class));
            if (res == null) {
                return null;
            }
            return new ElementTag(res, true);
        });

        // <--[tag]
        // @attribute <ElementTag.hsb_color_gradient[from=<color>;to=<color>]>
        // @returns ElementTag
        // @Plugin Paper
        // @group text manipulation
        // @deprecated use color_gradient[from=color;to=color;style=HSB]
        // @description
        // Deprecated in favor of using <@link tag ElementTag.color_gradient> with "style=hsb"
        // -->
        ElementTag.tagProcessor.registerStaticTag(ElementTag.class, MapTag.class, "hsb_color_gradient", (attribute, object, inputMap) -> {
            BukkitImplDeprecations.hsbColorGradientTag.warn(attribute.context);
            ColorTag fromColor = inputMap.getRequiredObjectAs("from", ColorTag.class, attribute);
            ColorTag toColor = inputMap.getRequiredObjectAs("to", ColorTag.class, attribute);
            if (fromColor == null || toColor == null) {
                return null;
            }
            String res = doGradient(object.asString(), fromColor, toColor, GradientStyle.HSB);
            if (res == null) {
                return null;
            }
            return new ElementTag(res, true);
        });

        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_18)) {

            // <--[tag]
            // @attribute <ElementTag.parse_minimessage>
            // @returns ElementTag
            // @Plugin Paper
            // @group paper
            // @description
            // Returns the element with all MiniMessage tags parsed, see <@link url https://docs.adventure.kyori.net/minimessage/format.html> for more information.
            // This may be useful for reading data from external plugins, but should not be used in normal scripts.
            // -->
            ElementTag.tagProcessor.registerTag(ElementTag.class, "parse_minimessage", (attribute, object) -> {
                return new ElementTag(FormattedTextHelper.stringify(MiniMessage.miniMessage().deserialize(object.asString())));
            });

            // <--[tag]
            // @attribute <ElementTag.to_minimessage>
            // @returns ElementTag
            // @Plugin Paper
            // @group paper
            // @description
            // Returns the element with all text formatting parsed into MiniMessage format.
            // This may be useful for sending data to external plugins, but should not be used in normal scripts.
            // -->
            ElementTag.tagProcessor.registerTag(ElementTag.class, "to_minimessage", (attribute, object) -> {
                return new ElementTag(PaperAPITools.instance.convertTextToMiniMessage(object.asString(), false));
            });
        }
    }

    public enum GradientStyle { RGB, HSB }

    public static String doGradient(String str, ColorTag fromColor, ColorTag toColor, GradientStyle style) {
        int length = PlainTextComponentSerializer.plainText().serialize(FormattedTextHelper.parse(str, NamedTextColor.WHITE)).length();
        if (length == 0) {
            return "";
        }
        if (fromColor == null || toColor == null) {
            return null;
        }
        float r, g, b, x = 0, rMove, gMove, bMove, xMove = 0, toR, toG, toB;
        int[] hsbHelper = null;
        if (style == GradientStyle.RGB) {
            r = ColorTag.fromSRGB(fromColor.red);
            g = ColorTag.fromSRGB(fromColor.green);
            b = ColorTag.fromSRGB(fromColor.blue);
            x = (float) Math.pow(r + g + b, 0.43);
            toR = ColorTag.fromSRGB(toColor.red);
            toG = ColorTag.fromSRGB(toColor.green);
            toB = ColorTag.fromSRGB(toColor.blue);
            float toBrightness = (float) Math.pow(toR + toG + toB, 0.43);
            xMove = (toBrightness - x) / length;
        }
        else {
            hsbHelper = fromColor.toHSB();
            int[] toHSB = toColor.toHSB();
            r = hsbHelper[0];
            g = hsbHelper[1];
            b = hsbHelper[2];
            toR = toHSB[0];
            toG = toHSB[1];
            toB = toHSB[2];
        }
        rMove = (toR - r) / length;
        gMove = (toG - g) / length;
        bMove = (toB - b) / length;
        String addedFormat = "";
        StringBuilder output = new StringBuilder(str.length() * 15);
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == FormattedTextHelper.LEGACY_SECTION && i + 1 < str.length()) {
                char c2 = str.charAt(i + 1);
                if (FORMAT_CODES_MATCHER.isMatch(c2)) {
                    addedFormat += String.valueOf(FormattedTextHelper.LEGACY_SECTION) + c2;
                }
                else if (c2 == '[') {
                    int endBracket = str.indexOf(']', i);
                    if (endBracket != -1) {
                        addedFormat += str.substring(i, endBracket + 1);
                        i = endBracket - 1;
                    }
                }
                else {
                    addedFormat = "";
                }
                i++;
                continue;
            }
            String hex;
            if (style == GradientStyle.RGB) {
                // Based on https://stackoverflow.com/questions/22607043/color-gradient-algorithm/49321304#49321304
                float newRed = r, newGreen = g, newBlue = b;
                float sum = newRed + newGreen + newBlue;
                if (sum > 0) {
                    float multiplier = (float) Math.pow(x, 1f / 0.43f) / sum;
                    newRed *= multiplier;
                    newGreen *= multiplier;
                    newBlue *= multiplier;
                }
                newRed = ColorTag.toSRGB(newRed);
                newGreen = ColorTag.toSRGB(newGreen);
                newBlue = ColorTag.toSRGB(newBlue);
                hex = Integer.toHexString((((int) newRed) << 16) | (((int) newGreen) << 8) | ((int) newBlue));
                x += xMove;
            }
            else {
                hsbHelper[0] = (int)r;
                hsbHelper[1] = (int)g;
                hsbHelper[2] = (int)b;
                ColorTag currentColor = ColorTag.fromHSB(hsbHelper);
                hex = Integer.toHexString(currentColor.asRGB());
            }
            output.append(FormattedTextHelper.stringifyRGBSpigot(hex)).append(addedFormat).append(str.charAt(i));
            r += rMove;
            g += gMove;
            b += bMove;
        }
        return output.toString();
    }

    public static AsciiMatcher FORMAT_CODES_MATCHER = new AsciiMatcher("klmnoKLMNO");
}
