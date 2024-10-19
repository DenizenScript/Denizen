package com.denizenscript.denizen.tags.core;

import com.denizenscript.denizen.objects.properties.bukkit.BukkitElementExtensions;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import com.denizenscript.denizen.utilities.FormattedTextHelper;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ColorTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.tags.core.EscapeTagUtil;
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
        // @attribute <&hover[<hover_text>]>
        // @returns ElementTag
        // @description
        // Returns a special chat code that makes the following text display the input hover text when the mouse is left over it.
        // This tag must be followed by an <&end_hover> tag.
        // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
        // @example
        // # Shows the text "you found it!" when hovering over the word "secret" in chat.
        // - narrate "There is a <&hover[you found it!]>secret<&end_hover> in this message!"
        // -->
        TagManager.registerTagHandler(ElementTag.class, "&hover", (attribute) -> { // Cannot be static due to hacked sub-tag
              if (!attribute.hasParam()) {
                  return null;
              }
              String hoverText = attribute.getParam();

              // <--[tag]
              // @attribute <&hover[<hover_text>].type[<type>]>
              // @returns ElementTag
              // @description
              // Returns a special chat code that makes the following text display the input hover text when the mouse is left over it.
              // This tag must be followed by an <&end_hover> tag.
              // Available hover types: SHOW_TEXT, SHOW_ACHIEVEMENT, SHOW_ITEM, or SHOW_ENTITY.
              // Note: for "SHOW_ITEM", replace the text with a valid <@link objecttype ItemTag>. 
              // Note: for "SHOW_ENTITY", replace the text with a valid <@link objecttype EntityTag> (use F3+H to see entities).
              // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
              // @example
              // # Shows the text "you found it!" when hovering over the word "secret" in chat.
              // - narrate "There is a <&hover[you found it!].type[SHOW_TEXT]>secret<&end_hover> in this message!"
              // @example
              // # Shows the name, lore, and attack damage of a diamond sword when hovering over the word "here" in chat.
              // - narrate "The item in your hand can be found <&hover[<item[diamond_sword].type[SHOW_ITEM]>here<&end_hover>."
              // -->
              String type = "SHOW_TEXT";
              if (attribute.startsWith("type", 2)) {
                  type = attribute.getContext(2);
                  attribute.fulfill(1);
              }
              return new ElementTag(ChatColor.COLOR_CHAR + "[hover=" + type + ";" + FormattedTextHelper.escape(hoverText) + "]");
          });

        // <--[tag]
        // @attribute <&click[<click_command>]>
        // @returns ElementTag
        // @description
        // Returns a special chat code that makes the following text execute the input command line value when clicked.
        // To execute a command "/" should be used at the start. Otherwise, it will display as chat.
        // This tag must be followed by <@link tag &end_click>.
        // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
        // @example
        // # Sends the message "wow" when clicking on "click here" in chat.
        // - narrate "You can <&click[wow]>click here<&end_click> to say wow!"
        // @example
        // # Sends the command "/help" when clicking on "click here" in chat.
        // - narrate "You can <&click[/help]>click here<&end_click> for help!"
        // -->
        TagManager.registerTagHandler(ElementTag.class, "&click", (attribute) -> { // Cannot be static due to hacked sub-tag
            if (!attribute.hasParam()) {
                return null;
            }
            String clickText = attribute.getParam();

            // <--[tag]
            // @attribute <&click[<click_command>].type[<type>]>
            // @returns ElementTag
            // @description
            // Returns a special chat code that makes the following text execute the input command when clicked.
            // This tag must be followed by <@link tag &end_click>.
            // Available command types: OPEN_URL, OPEN_FILE, RUN_COMMAND, SUGGEST_COMMAND, COPY_TO_CLIPBOARD, or CHANGE_PAGE.
            // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
            // @example
            // # Sends the player to https://denizenscript.com when clicking on "click here" in chat.
            // - narrate "You can <&click[https://denizenscript.com].type[OPEN_URL]>click here<&end_click> to learn about Denizen!"
            // @example
            // # Sets the player's suggested command to "/msg mcmonkey4eva I think you're the best." when clicking on "gratitude".
            // - narrate "If you want to thank the author of this plugin, you can show him some <&click[/msg mcmonkey4eva I think you're the best.].type[SUGGEST_COMMAND]>gratitude<&end_click>."
            // -->
            String type = "RUN_COMMAND";
            if (attribute.startsWith("type", 2)) {
                type = attribute.getContext(2);
                attribute.fulfill(1);
            }
            return new ElementTag(ChatColor.COLOR_CHAR + "[click=" + type + ";" + FormattedTextHelper.escape(clickText) + "]");
        });

        // <--[tag]
        // @attribute <&insertion[<message>]>
        // @returns ElementTag
        // @description
        // Returns a special chat code that makes the following text insert the input message to chat when shift-clicked.
        // This tag must be followed by <@link tag &end_insertion>.
        // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
        // @example
        // # Adds "wow" to the end of the player's open chat message when shift-clicking on the words "click here".
        // - narrate "You can <&insertion[wow]>click here<&end_insertion> to add 'wow' to your chat!"
        // -->
        TagManager.registerStaticTagBaseHandler(ElementTag.class, "&insertion", (attribute) -> {
            if (!attribute.hasParam()) {
                return null;
            }
            String insertText = attribute.getParam();
            return new ElementTag(ChatColor.COLOR_CHAR + "[insertion=" + FormattedTextHelper.escape(insertText) + "]");
        });

        // <--[tag]
        // @attribute <&end_click>
        // @returns ElementTag
        // @description
        // Returns a special chat code that ends a <@link tag click[<click_command>]>.
        // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
        // @example
        // # Sends the message "wow" when clicking on "click here" in chat.
        // - narrate "You can <&click[wow]>click here<&end_click> to say wow!"
        // -->
        TagManager.registerStaticTagBaseHandler(ElementTag.class, "&end_click", (attribute) -> {
            return new ElementTag(ChatColor.COLOR_CHAR + "[/click]");
        });

        // <--[tag]
        // @attribute <&end_hover>
        // @returns ElementTag
        // @description
        // Returns a special chat code that ends a <@link tag hover[<hover_text>]>.
        // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
        // @example
        // # Shows the text "you found it!" when hovering over the word "secret" in chat.
        // - narrate "There is a <&hover[you found it!]>secret<&end_hover> in this message!"
        // -->
        TagManager.registerStaticTagBaseHandler(ElementTag.class, "&end_hover", (attribute) -> {
            return new ElementTag(ChatColor.COLOR_CHAR + "[/hover]");
        });

        // <--[tag]
        // @attribute <&end_insertion>
        // @returns ElementTag
        // @description
        // Returns a special chat code that ends a <@link tag insertion[<message>]>.
        // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
        // @example
        // # Adds "wow" to the end of the player's open chat message when shift-clicking on the words "click here".
        // - narrate "You can <&insertion[wow]>click here<&end_insertion> to add 'wow' to your chat!"
        // -->
        TagManager.registerStaticTagBaseHandler(ElementTag.class, "&end_insertion", (attribute) -> {
            return new ElementTag(ChatColor.COLOR_CHAR + "[/insertion]");
        });

        // <--[tag]
        // @attribute <&keybind[<key>]>
        // @returns ElementTag
        // @description
        // Returns a special chat code that displays a keybind.
        // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
        // @example
        // # Narrates "Press your Space key!" if the player's jump key is set to the space bar.
        // - narrate "Press your <&keybind[key.jump]> key!"
        // -->
        TagManager.registerStaticTagBaseHandler(ElementTag.class, "&keybind", (attribute) -> {
            if (!attribute.hasParam()) {
                return null;
            }
            String keybindText = attribute.getParam();
            return new ElementTag(ChatColor.COLOR_CHAR + "[keybind=" + FormattedTextHelper.escape(keybindText) + "]");
        });

        // <--[tag]
        // @attribute <&selector[<key>]>
        // @returns ElementTag
        // @description
        // Returns a special chat code that displays a vanilla selector.
        // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
        // -->
        TagManager.registerStaticTagBaseHandler(ElementTag.class, "&selector", (attribute) -> {
            if (!attribute.hasParam()) {
                return null;
            }
            String selectorText = attribute.getParam();
            return new ElementTag(ChatColor.COLOR_CHAR + "[selector=" + FormattedTextHelper.escape(selectorText) + "]");
        });

        // <--[tag]
        // @attribute <&translate[key=<key>;(fallback=<fallback>);(with=<text>|...)]>
        // @returns ElementTag
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
                // @deprecated Use '<&translate[key=<key>;with=<text>|...]>'.
                // @description
                // Deprecated in favor of <@link tag &translate>.
                // -->
                if (attribute.startsWith("with", 2)) {
                    translateMap.putObject("with", new ListTag(attribute.contextAsType(2, ListTag.class), with -> new ElementTag(EscapeTagUtil.unEscape(with), true)));
                    attribute.fulfill(1);
                }
            }
            return new ElementTag(ChatColor.COLOR_CHAR + "[translate=" + FormattedTextHelper.escape(translateMap.savable()) + ']', true);
        });

        // <--[tag]
        // @attribute <&score[<name>|<objective>(|<value>)]>
        // @returns ElementTag
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
            return new ElementTag(ChatColor.COLOR_CHAR + "[score=" + name + ";" + objective + ";" + value + "]");
        });

        // <--[tag]
        // @attribute <&color[<color>]>
        // @returns ElementTag
        // @description
        // Returns a chat code that makes the following text be the specified color.
        // Color can be a color name, color code, hex, or <@link ObjectType ColorTag>.
        // The ColorTag input option can be used for dynamic color effects, such as automatic rainbows.
        // @example
        // # Narrates "Look at this cool message!" in gold.
        // - narrate "<&color[gold]>Look at this cool message!"
        // - narrate "<&color[6]>Look at this cool message!"
        // - narrate "<&color[#AABB00]>Look at this cool message!"
        // -->
        TagManager.registerStaticTagBaseHandler(ElementTag.class, "&color", (attribute) -> {
            if (!attribute.hasParam()) {
                return null;
            }
            String colorName = attribute.getParam();
            String colorOut = null;
            if (colorName.length() == 1) {
                ChatColor color = ChatColor.getByChar(colorName.charAt(0));
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
                try {
                    ChatColor color = ChatColor.valueOf(CoreUtilities.toUpperCase(colorName));
                    colorOut = color.toString();
                }
                catch (IllegalArgumentException ex) {
                    attribute.echoError("Color '" + colorName + "' doesn't exist (for tag &color[...]).");
                    return null;
                }
            }
            return new ElementTag(colorOut);
        });

        // <--[tag]
        // @attribute <&gradient[from=<color>;to=<color>;(style={RGB}/HSB)]>
        // @returns ElementTag
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
            if (!style.matchesEnum(BukkitElementExtensions.GradientStyle.class)) {
                attribute.echoError("Invalid gradient style '" + style + "'");
                return null;
            }
            return new ElementTag(ChatColor.COLOR_CHAR + "[gradient=" + fromColor + ";" + toColor + ";" + style + "]");
        });

        // <--[tag]
        // @attribute <&font[<font>]>
        // @returns ElementTag
        // @description
        // Returns a chat code that makes the following text display with the specified font.
        // The default font is "minecraft:default".
        // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
        // -->
        TagManager.registerStaticTagBaseHandler(ElementTag.class, "&font", (attribute) -> {
            if (!attribute.hasParam()) {
                return null;
            }
            return new ElementTag(ChatColor.COLOR_CHAR + "[font=" + attribute.getParam() + "]");
        });

        // <--[tag]
        // @attribute <&optimize>
        // @returns ElementTag
        // @description
        // Returns a chat code that tells the formatted text parser to try to produce mininalist JSON text.
        // This is useful in particular for very long text or where text is being sent rapidly/repeatedly.
        // It is not needed in most normal messages.
        // It will produce incompatibility issues if used in items or other locations where raw JSON matching is required.
        // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
        // -->
        TagManager.registerStaticTagBaseHandler(ElementTag.class, "&optimize", (attribute) -> {
            return new ElementTag(ChatColor.COLOR_CHAR + "[optimize=true]", true);
        });

        // <--[tag]
        // @attribute <&0>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters Black.
        // Same as <@link tag black>
        // @example
        // # Narrates "Look at this cool message!" in black.
        // - narrate "<&0>Look at this cool message!"
        // -->

        // <--[tag]
        // @attribute <&1>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters Dark Blue.
        // Same as <@link tag dark_blue>
        // @example
        // # Narrates "Look at this cool message!" in dark blue.
        // - narrate "<&1>Look at this cool message!"
        // -->

        // <--[tag]
        // @attribute <&2>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters Dark Green.
        // Same as <@link tag dark_green>
        // @example
        // # Narrates "Look at this cool message!" in dark green.
        // - narrate "<&2>Look at this cool message!"
        // -->

        // <--[tag]
        // @attribute <&3>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters Dark Cyan.
        // Same as <@link tag dark_aqua>
        // @example
        // # Narrates "Look at this cool message!" in dark cyan.
        // - narrate "<&3>Look at this cool message!"
        // -->

        // <--[tag]
        // @attribute <&4>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters Dark Red.
        // Same as <@link tag dark_red>
        // @example
        // # Narrates "Look at this cool message!" in dark red.
        // - narrate "<&4>Look at this cool message!"
        // -->

        // <--[tag]
        // @attribute <&5>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters Dark Purple.
        // Same as <@link tag dark_purple>
        // @example
        // # Narrates "Look at this cool message!" in dark purple.
        // - narrate "<&5>Look at this cool message!"
        // -->

        // <--[tag]
        // @attribute <&6>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters Gold.
        // Same as <@link tag gold>
        // @example
        // # Narrates "Look at this cool message!" in gold.
        // - narrate "<&6>Look at this cool message!"
        // -->

        // <--[tag]
        // @attribute <&7>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters Light Gray.
        // Same as <@link tag gray>
        // @example
        // # Narrates "Look at this cool message!" in light gray.
        // - narrate "<&7>Look at this cool message!"
        // -->

        // <--[tag]
        // @attribute <&8>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters Dark Gray.
        // Same as <@link tag dark_gray>
        // @example
        // # Narrates "Look at this cool message!" in dark gray.
        // - narrate "<&8>Look at this cool message!"
        // -->

        // <--[tag]
        // @attribute <&9>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters Blue.
        // Same as <@link tag blue>
        // @example
        // # Narrates "Look at this cool message!" in blue.
        // - narrate "<&9>Look at this cool message!"
        // -->

        // <--[tag]
        // @attribute <&a>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters Light Green.
        // Same as <@link tag green>
        // @example
        // # Narrates "Look at this cool message!" in light green.
        // - narrate "<&a>Look at this cool message!"
        // -->

        // <--[tag]
        // @attribute <&b>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters Light Blue.
        // Same as <@link tag aqua>
        // @example
        // # Narrates "Look at this cool message!" in light blue.
        // - narrate "<&b>Look at this cool message!"
        // -->

        // <--[tag]
        // @attribute <&c>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters Light Red.
        // Same as <@link tag red>
        // @example
        // # Narrates "Look at this cool message!" in light red.
        // - narrate "<&c>Look at this cool message!"
        // -->

        // <--[tag]
        // @attribute <&d>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters Light Purple.
        // Same as <@link tag light_purple>
        // @example
        // # Narrates "Look at this cool message!" in light purple.
        // - narrate "<&d>Look at this cool message!"
        // -->

        // <--[tag]
        // @attribute <&e>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters Yellow.
        // Same as <@link tag yellow>
        // @example
        // # Narrates "Look at this cool message!" in yellow.
        // - narrate "<&e>Look at this cool message!"
        // -->

        // <--[tag]
        // @attribute <&f>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters White.
        // Same as <@link tag white>
        // @example
        // # Narrates "Look at this cool message!" in white.
        // - narrate "<&f>Look at this cool message!"
        // -->

        // <--[tag]
        // @attribute <&k>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters obfuscated.
        // Same as <@link tag magic>
        // @example
        // # Narrates "Look at this cool message!" obfuscated.
        // - narrate "<&k>Look at this cool message!"
        // -->

        // <--[tag]
        // @attribute <&l>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters bold.
        // Same as <@link tag bold>
        // @example
        // # Narrates "Look at this cool message!" bolded.
        // - narrate "<&l>Look at this cool message!"
        // -->

        // <--[tag]
        // @attribute <&m>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters have a strike-through.
        // Same as <@link tag strikethrough>
        // @example
        // # Narrates "Look at this cool message!" with a strike-through.
        // - narrate "<&m>Look at this cool message!"
        // -->

        // <--[tag]
        // @attribute <&n>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters have an underline.
        // Same as <@link tag underline>
        // @example
        // # Narrates "Look at this cool message!" underlined.
        // - narrate "<&n>Look at this cool message!"
        // -->

        // <--[tag]
        // @attribute <&o>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters italicized.
        // Same as <@link tag italic>
        // @example
        // # Narrates "Look at this cool message!" italicized.
        // - narrate "<&o>Look at this cool message!"
        // -->

        // <--[tag]
        // @attribute <&r>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that resets the following characters to normal.
        // Same as <@link tag reset>
        // @example
        // # Narrates just "this" in light red, but the exclamation point afterward in the default color.
        // - narrate "Look at <&c>this<&r>!"
        // -->

        // <--[tag]
        // @attribute <black>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters Black.
        // Same as <@link tag &0>
        // @example
        // # Narrates "Look at this cool message!" in black.
        // - narrate "<black>Look at this cool message!"
        // -->

        // <--[tag]
        // @attribute <dark_blue>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters Dark Blue.
        // Same as <@link tag &1>
        // @example
        // # Narrates "Look at this cool message!" in dark blue.
        // - narrate "<dark_blue>Look at this cool message!"
        // -->

        // <--[tag]
        // @attribute <dark_green>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters Dark Green.
        // Same as <@link tag &2>
        // @example
        // # Narrates "Look at this cool message!" in dark green.
        // - narrate "<dark_green>Look at this cool message!"
        // -->

        // <--[tag]
        // @attribute <dark_aqua>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters Dark Cyan.
        // Same as <@link tag &3>
        // @example
        // # Narrates "Look at this cool message!" in dark cyan.
        // - narrate "<dark_cyan>Look at this cool message!"
        // -->

        // <--[tag]
        // @attribute <dark_red>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters Dark Red.
        // Same as <@link tag &4>
        // @example
        // # Narrates "Look at this cool message!" in dark red.
        // - narrate "<dark_red>Look at this cool message!"
        // -->

        // <--[tag]
        // @attribute <dark_purple>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters Dark Purple.
        // Same as <@link tag &5>
        // @example
        // # Narrates "Look at this cool message!" in dark purple.
        // - narrate "<dark_purple>Look at this cool message!"
        // -->

        // <--[tag]
        // @attribute <gold>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters Gold.
        // Same as <@link tag &6>
        // @example
        // # Narrates "Look at this cool message!" in gold.
        // - narrate "<gold>Look at this cool message!"
        // -->

        // <--[tag]
        // @attribute <gray>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters Light Gray.
        // Same as <@link tag &7>
        // @example
        // # Narrates "Look at this cool message!" in light gray.
        // - narrate "<gray>Look at this cool message!"
        // -->

        // <--[tag]
        // @attribute <dark_gray>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters Dark Gray.
        // Same as <@link tag &8>
        // @example
        // # Narrates "Look at this cool message!" in dark gray.
        // - narrate "<dark_gray>Look at this cool message!"
        // -->

        // <--[tag]
        // @attribute <blue>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters Blue.
        // Same as <@link tag &9>
        // @example
        // # Narrates "Look at this cool message!" in blue.
        // - narrate "<blue>Look at this cool message!"
        // -->

        // <--[tag]
        // @attribute <green>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters Light Green.
        // Same as <@link tag &a>
        // @example
        // # Narrates "Look at this cool message!" in light green.
        // - narrate "<green>Look at this cool message!"
        // -->

        // <--[tag]
        // @attribute <aqua>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters Light Blue.
        // Same as <@link tag &b>
        // @example
        // # Narrates "Look at this cool message!" in light blue.
        // - narrate "<aqua>Look at this cool message!"
        // -->

        // <--[tag]
        // @attribute <red>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters Light Red.
        // Same as <@link tag &c>
        // @example
        // # Narrates "Look at this cool message!" in light red.
        // - narrate "<red>Look at this cool message!"
        // -->

        // <--[tag]
        // @attribute <light_purple>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters Light Purple.
        // Same as <@link tag &d>
        // @example
        // # Narrates "Look at this cool message!" in light purple.
        // - narrate "<light_purple>Look at this cool message!"
        // -->

        // <--[tag]
        // @attribute <yellow>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters Yellow.
        // Same as <@link tag &e>
        // @example
        // # Narrates "Look at this cool message!" in yellow.
        // - narrate "<yellow>Look at this cool message!"
        // -->

        // <--[tag]
        // @attribute <white>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters White.
        // Same as <@link tag &f>
        // @example
        // # Narrates "Look at this cool message!" in white.
        // - narrate "<white>Look at this cool message!"
        // -->

        // <--[tag]
        // @attribute <magic>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters obfuscated.
        // Same as <@link tag &k>
        // @example
        // # Narrates "Look at this cool message!" obfuscated.
        // - narrate "<magic>Look at this cool message!"
        // -->

        // <--[tag]
        // @attribute <bold>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters bold.
        // Same as <@link tag &l>
        // @example
        // # Narrates "Look at this cool message!" bolded.
        // - narrate "<bold>Look at this cool message!"
        // -->

        // <--[tag]
        // @attribute <strikethrough>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters have a strike-through.
        // Same as <@link tag &m>
        // @example
        // # Narrates "Look at this cool message!" with a strike-through.
        // - narrate "<strikethrough>Look at this cool message!"
        // -->

        // <--[tag]
        // @attribute <underline>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters have an underline.
        // Same as <@link tag &n>
        // @example
        // # Narrates "Look at this cool message!" underlined.
        // - narrate "<underline>Look at this cool message!"
        // -->

        // <--[tag]
        // @attribute <italic>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that makes the following characters italicized.
        // Same as <@link tag &o>
        // @example
        // # Narrates "Look at this cool message!" italicized.
        // - narrate "<italic>Look at this cool message!"
        // -->

        // <--[tag]
        // @attribute <reset>
        // @returns ElementTag
        // @description
        // Returns the ChatColor that resets the following characters to normal.
        // Same as <@link tag &r>
        // @example
        // # Narrates just "this" in light red, but the exclamation point afterward in the default color.
        // - narrate "Look at <light_red>this<reset>!"
        // -->

        for (ChatColor color : ChatColor.values()) {
            final String nameVal = CoreUtilities.toLowerCase(color.name());
            final String retVal = color.toString();
            TagManager.registerStaticTagBaseHandler(ElementTag.class, nameVal, (attribute) -> new ElementTag(retVal));
            TagManager.registerStaticTagBaseHandler(ElementTag.class, "&" + color.getChar(), (attribute) -> new ElementTag(retVal));
        }
    }
}
