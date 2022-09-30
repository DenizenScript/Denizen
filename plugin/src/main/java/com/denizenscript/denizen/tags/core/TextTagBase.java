package com.denizenscript.denizen.tags.core;

import com.denizenscript.denizen.objects.ColorTag;
import com.denizenscript.denizen.objects.properties.bukkit.BukkitElementExtensions;
import com.denizenscript.denizen.utilities.FormattedTextHelper;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.tags.TagRunnable;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ReplaceableTagEvent;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.tags.core.EscapeTagBase;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import org.bukkit.ChatColor;

public class TextTagBase {

    public TextTagBase() {
        TagManager.registerTagHandler(new TagRunnable.RootForm() {
            @Override
            public void run(ReplaceableTagEvent event) {
                String lower = CoreUtilities.toLowerCase(event.getName());
                Attribute attribute = event.getAttributes();
                if (event.getName().equals("&auml")) {
                    BukkitImplDeprecations.pointlessTextTags.warn(event.getScriptEntry());
                    event.setReplacedObject(new ElementTag("ä").getObjectAttribute(attribute.fulfill(1)));
                }
                else if (event.getName().equals("&Auml")) {
                    BukkitImplDeprecations.pointlessTextTags.warn(event.getScriptEntry());
                    event.setReplacedObject(new ElementTag("Ä").getObjectAttribute(attribute.fulfill(1)));
                }
                else if (event.getName().equals("&ouml")) {
                    BukkitImplDeprecations.pointlessTextTags.warn(event.getScriptEntry());
                    event.setReplacedObject(new ElementTag("ö").getObjectAttribute(attribute.fulfill(1)));
                }
                else if (event.getName().equals("&Ouml")) {
                    BukkitImplDeprecations.pointlessTextTags.warn(event.getScriptEntry());
                    event.setReplacedObject(new ElementTag("Ö").getObjectAttribute(attribute.fulfill(1)));
                }
                else if (event.getName().equals("&uuml")) {
                    BukkitImplDeprecations.pointlessTextTags.warn(event.getScriptEntry());
                    event.setReplacedObject(new ElementTag("ü").getObjectAttribute(attribute.fulfill(1)));
                }
                else if (event.getName().equals("&Uuml")) {
                    BukkitImplDeprecations.pointlessTextTags.warn(event.getScriptEntry());
                    event.setReplacedObject(new ElementTag("Ü").getObjectAttribute(attribute.fulfill(1)));
                }
                else if (lower.equals("&amp")) {
                    BukkitImplDeprecations.pointlessTextTags.warn(event.getScriptEntry());
                    event.setReplacedObject(new ElementTag("&").getObjectAttribute(attribute.fulfill(1)));
                }
                else if (lower.equals("&cm")) {
                    BukkitImplDeprecations.pointlessTextTags.warn(event.getScriptEntry());
                    event.setReplacedObject(new ElementTag(",").getObjectAttribute(attribute.fulfill(1)));
                }
                else if (lower.equals("&sc")) {
                    BukkitImplDeprecations.pointlessTextTags.warn(event.getScriptEntry());
                    event.setReplacedObject(new ElementTag(";").getObjectAttribute(attribute.fulfill(1)));
                }
                else if (lower.equals("&pipe")) {
                    BukkitImplDeprecations.pointlessTextTags.warn(event.getScriptEntry());
                    event.setReplacedObject(new ElementTag("|").getObjectAttribute(attribute.fulfill(1)));
                }
                else if (lower.equals("&ds")) {
                    BukkitImplDeprecations.pointlessTextTags.warn(event.getScriptEntry());
                    event.setReplacedObject(new ElementTag("$").getObjectAttribute(attribute.fulfill(1)));
                }
                else if (lower.equals("&dot")) {
                    BukkitImplDeprecations.pointlessTextTags.warn(event.getScriptEntry());
                    event.setReplacedObject(new ElementTag(".").getObjectAttribute(attribute.fulfill(1)));
                }
                else if (lower.equals("&hrt")) {
                    BukkitImplDeprecations.pointlessTextTags.warn(event.getScriptEntry());
                    event.setReplacedObject(new ElementTag("\u2665").getObjectAttribute(attribute.fulfill(1)));
                }
            }
        }, "&auml", "&Auml", "&ouml", "&Ouml", "&uuml", "&Uuml", "&amp", "&cm", "&sc", "&pipe", "&ds", "&at", "&dot", "&hrt");

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
        // For example: - narrate "There is a <&hover[you found it!]>secret<&end_hover> in this message!"
        // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
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
              // For example: - narrate "There is a <&hover[you found it!].type[SHOW_TEXT]>secret<&end_hover> in this message!"
              // Note: for "SHOW_ITEM", replace the text with a valid ItemTag. For "SHOW_ENTITY", replace the text with a valid spawned EntityTag (requires F3+H to see entities).
              // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
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
            return new ElementTag(ChatColor.COLOR_CHAR + "[click=" + type + ";" + FormattedTextHelper.escape(clickText) + "]");
        });

        // <--[tag]
        // @attribute <&insertion[<message>]>
        // @returns ElementTag
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
            return new ElementTag(ChatColor.COLOR_CHAR + "[insertion=" + FormattedTextHelper.escape(insertText) + "]");
        });

        // <--[tag]
        // @attribute <&end_click>
        // @returns ElementTag
        // @description
        // Returns a special chat code that ends a '&click' tag.
        // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
        // -->
        TagManager.registerStaticTagBaseHandler(ElementTag.class, "&end_click", (attribute) -> {
            return new ElementTag(ChatColor.COLOR_CHAR + "[/click]");
        });

        // <--[tag]
        // @attribute <&end_hover>
        // @returns ElementTag
        // @description
        // Returns a special chat code that ends a '&hover' tag.
        // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
        // -->
        TagManager.registerStaticTagBaseHandler(ElementTag.class, "&end_hover", (attribute) -> {
            return new ElementTag(ChatColor.COLOR_CHAR + "[/hover]");
        });

        // <--[tag]
        // @attribute <&end_insertion>
        // @returns ElementTag
        // @description
        // Returns a special chat code that ends an '&insertion' tag.
        // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
        // -->
        TagManager.registerStaticTagBaseHandler(ElementTag.class, "&end_insertion", (attribute) -> {
            return new ElementTag(ChatColor.COLOR_CHAR + "[/insertion]");
        });

        // <--[tag]
        // @attribute <&keybind[<key>]>
        // @returns ElementTag
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
        // @attribute <&translate[<key>]>
        // @returns ElementTag
        // @description
        // Returns a special chat code that displays an autotranslated message.
        // For example: - narrate "Reward: <&translate[item.minecraft.diamond_sword]>"
        // Be warned that language keys change between Minecraft versions.
        // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
        // -->
        TagManager.registerTagHandler(ElementTag.class, "&translate", (attribute) -> { // Cannot be static due to hacked sub-tag
            if (!attribute.hasParam()) {
                return null;
            }
            String translateText = attribute.getParam();

            // <--[tag]
            // @attribute <&translate[<key>].with[<text>|...]>
            // @returns ElementTag
            // @description
            // Returns a special chat code that displays an autotranslated message.
            // Optionally, specify a list of escaped text values representing input data for the translatable message.
            // Be aware that missing 'with' values will cause exceptions in your console.
            // For example: - narrate "<&translate[commands.give.success.single].with[32|<&translate[item.minecraft.diamond_sword].escaped>|<player.name.escaped>]>"
            // Be warned that language keys change between Minecraft versions.
            // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
            // -->
            StringBuilder with = new StringBuilder();
            if (attribute.startsWith("with", 2)) {
                ListTag withList = attribute.contextAsType(2, ListTag.class);
                attribute.fulfill(1);
                for (String str : withList) {
                    with.append(";").append(FormattedTextHelper.escape(EscapeTagBase.unEscape(str)));
                }
            }
            return new ElementTag(ChatColor.COLOR_CHAR + "[translate=" + FormattedTextHelper.escape(translateText) + with + "]");
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
            String name = FormattedTextHelper.escape(EscapeTagBase.unEscape(scoreList.get(0)));
            String objective = FormattedTextHelper.escape(EscapeTagBase.unEscape(scoreList.get(1)));
            String value = scoreList.size() >= 3 ? FormattedTextHelper.escape(EscapeTagBase.unEscape(scoreList.get(2))) : "";
            return new ElementTag(ChatColor.COLOR_CHAR + "[score=" + name + ";" + objective + ";" + value + "]");
        });

        // <--[tag]
        // @attribute <&color[<color>]>
        // @returns ElementTag
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
                String hex = Integer.toHexString(color.getColor().asRGB());
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
