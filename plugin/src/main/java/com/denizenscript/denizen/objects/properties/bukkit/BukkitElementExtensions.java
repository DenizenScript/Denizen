package com.denizenscript.denizen.objects.properties.bukkit;

import com.denizenscript.denizen.objects.*;
import com.denizenscript.denizen.scripts.containers.core.FormatScriptContainer;
import com.denizenscript.denizen.scripts.containers.core.ItemScriptHelper;
import com.denizenscript.denizen.tags.core.CustomColorTagBase;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import com.denizenscript.denizen.utilities.FormattedTextHelper;
import com.denizenscript.denizen.utilities.TextWidthHelper;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.objects.core.ScriptTag;
import com.denizenscript.denizencore.utilities.AsciiMatcher;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.utilities.Deprecations;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Color;

import java.nio.charset.StandardCharsets;

public class BukkitElementExtensions {

    public static String replaceEssentialsHexColors(char prefix, String input) {
        int hex = input.indexOf(prefix + "#");
        while (hex != -1 && hex < input.length() + 8) {
            StringBuilder converted = new StringBuilder(10);
            converted.append(ChatColor.COLOR_CHAR).append("x");
            for (int i = 0; i < 6; i++) {
                char c = input.charAt(hex + 2 + i);
                if (!ArgumentHelper.HEX_MATCHER.isMatch(c)) {
                    return input;
                }
                converted.append(ChatColor.COLOR_CHAR).append(c);
            }
            input = input.substring(0, hex) + converted + input.substring(hex + 8);
            hex = input.indexOf(prefix + "#", hex + 2);
        }
        return input;
    }

    public static void register() {

        // <--[tag]
        // @attribute <ElementTag.as_biome>
        // @returns BiomeTag
        // @group conversion
        // @deprecated use as[biome]
        // @description
        // Deprecated in favor of <@link tag ObjectTag.as>
        // -->
        ElementTag.tagProcessor.registerStaticTag(BiomeTag.class, "as_biome", (attribute, object) -> {
            Deprecations.asXTags.warn(attribute.context);
            return ElementTag.handleNull(object.asString(), BiomeTag.valueOf(object.asString(), attribute.context), "BiomeTag", attribute.hasAlternative());
        });

        // <--[tag]
        // @attribute <ElementTag.as_chunk>
        // @returns ChunkTag
        // @group conversion
        // @deprecated use as[chunk]
        // @description
        // Deprecated in favor of <@link tag ObjectTag.as>
        // -->
        ElementTag.tagProcessor.registerTag(ChunkTag.class, "as_chunk", (attribute, object) -> {
            Deprecations.asXTags.warn(attribute.context);
            return ElementTag.handleNull(object.asString(), ChunkTag.valueOf(object.asString(), attribute.context), "ChunkTag", attribute.hasAlternative());
        }, "aschunk");

        // <--[tag]
        // @attribute <ElementTag.as_color>
        // @returns ColorTag
        // @group conversion
        // @deprecated use as[color]
        // @description
        // Deprecated in favor of <@link tag ObjectTag.as>
        // -->
        ElementTag.tagProcessor.registerStaticTag(ColorTag.class, "as_color", (attribute, object) -> {
            Deprecations.asXTags.warn(attribute.context);
            return ElementTag.handleNull(object.asString(), ColorTag.valueOf(object.asString(), attribute.context), "ColorTag", attribute.hasAlternative());
        }, "ascolor");

        // <--[tag]
        // @attribute <ElementTag.as_cuboid>
        // @returns CuboidTag
        // @group conversion
        // @deprecated use as[cuboid]
        // @description
        // Deprecated in favor of <@link tag ObjectTag.as>
        // -->
        ElementTag.tagProcessor.registerTag(CuboidTag.class, "as_cuboid", (attribute, object) -> {
            Deprecations.asXTags.warn(attribute.context);
            return ElementTag.handleNull(object.asString(), CuboidTag.valueOf(object.asString(), attribute.context), "CuboidTag", attribute.hasAlternative());
        }, "ascuboid");

        // <--[tag]
        // @attribute <ElementTag.as_ellipsoid>
        // @returns EllipsoidTag
        // @group conversion
        // @deprecated use as[ellipsoid]
        // @description
        // Deprecated in favor of <@link tag ObjectTag.as>
        // -->
        ElementTag.tagProcessor.registerTag(EllipsoidTag.class, "as_ellipsoid", (attribute, object) -> {
            Deprecations.asXTags.warn(attribute.context);
            return ElementTag.handleNull(object.asString(), EllipsoidTag.valueOf(object.asString(), attribute.context), "EllipsoidTag", attribute.hasAlternative());
        });

        // <--[tag]
        // @attribute <ElementTag.as_enchantment>
        // @returns EnchantmentTag
        // @group conversion
        // @deprecated use as[enchantment]
        // @description
        // Deprecated in favor of <@link tag ObjectTag.as>
        // -->
        ElementTag.tagProcessor.registerStaticTag(EnchantmentTag.class, "as_enchantment", (attribute, object) -> {
            Deprecations.asXTags.warn(attribute.context);
            return ElementTag.handleNull(object.asString(), EnchantmentTag.valueOf(object.asString(), attribute.context), "EnchantmentTag", attribute.hasAlternative());
        });

        // <--[tag]
        // @attribute <ElementTag.as_entity>
        // @returns EntityTag
        // @group conversion
        // @deprecated use as[entity]
        // @description
        // Deprecated in favor of <@link tag ObjectTag.as>
        // -->
        ElementTag.tagProcessor.registerTag(EntityTag.class, "as_entity", (attribute, object) -> {
            Deprecations.asXTags.warn(attribute.context);
            return ElementTag.handleNull(object.asString(), EntityTag.valueOf(object.asString(), attribute.context), "EntityTag", attribute.hasAlternative());
        }, "asentity");

        // <--[tag]
        // @attribute <ElementTag.as_inventory>
        // @returns InventoryTag
        // @group conversion
        // @deprecated use as[inventory]
        // @description
        // Deprecated in favor of <@link tag ObjectTag.as>
        // -->
        ElementTag.tagProcessor.registerTag(InventoryTag.class, "as_inventory", (attribute, object) -> {
            Deprecations.asXTags.warn(attribute.context);
            return ElementTag.handleNull(object.asString(), InventoryTag.valueOf(object.asString(), attribute.context), "InventoryTag", attribute.hasAlternative());
        }, "asinventory");

        // <--[tag]
        // @attribute <ElementTag.as_item>
        // @returns ItemTag
        // @group conversion
        // @deprecated use as[item]
        // @description
        // Deprecated in favor of <@link tag ObjectTag.as>
        // -->
        ElementTag.tagProcessor.registerTag(ItemTag.class, "as_item", (attribute, object) -> {
            Deprecations.asXTags.warn(attribute.context);
            return ElementTag.handleNull(object.asString(), ItemTag.valueOf(object.asString(), attribute.context), "ItemTag", attribute.hasAlternative());
        }, "asitem");

        // <--[tag]
        // @attribute <ElementTag.as_location>
        // @returns LocationTag
        // @group conversion
        // @deprecated use as[location]
        // @description
        // Deprecated in favor of <@link tag ObjectTag.as>
        // -->
        ElementTag.tagProcessor.registerTag(LocationTag.class, "as_location", (attribute, object) -> {
            Deprecations.asXTags.warn(attribute.context);
            return ElementTag.handleNull(object.asString(), LocationTag.valueOf(object.asString(), attribute.context), "LocationTag", attribute.hasAlternative());
        }, "aslocation");

        // <--[tag]
        // @attribute <ElementTag.as_material>
        // @returns MaterialTag
        // @group conversion
        // @deprecated use as[material]
        // @description
        // Deprecated in favor of <@link tag ObjectTag.as>
        // -->
        ElementTag.tagProcessor.registerStaticTag(MaterialTag.class, "as_material", (attribute, object) -> {
            Deprecations.asXTags.warn(attribute.context);
            return ElementTag.handleNull(object.asString(), MaterialTag.valueOf(object.asString(), attribute.context), "MaterialTag", attribute.hasAlternative());
        }, "asmaterial");

        // <--[tag]
        // @attribute <ElementTag.as_npc>
        // @returns NPCTag
        // @group conversion
        // @deprecated use as[npc]
        // @description
        // Deprecated in favor of <@link tag ObjectTag.as>
        // -->
        ElementTag.tagProcessor.registerTag(NPCTag.class, "as_npc", (attribute, object) -> {
            Deprecations.asXTags.warn(attribute.context);
            return ElementTag.handleNull(object.asString(), NPCTag.valueOf(object.asString(), attribute.context), "NPCTag", attribute.hasAlternative());
        }, "asnpc");

        // <--[tag]
        // @attribute <ElementTag.as_player>
        // @returns PlayerTag
        // @group conversion
        // @deprecated use as[player]
        // @description
        // Deprecated in favor of <@link tag ObjectTag.as>
        // -->
        ElementTag.tagProcessor.registerTag(PlayerTag.class, "as_player", (attribute, object) -> {
            Deprecations.asXTags.warn(attribute.context);
            return ElementTag.handleNull(object.asString(), PlayerTag.valueOf(object.asString(), attribute.context), "PlayerTag", attribute.hasAlternative());
        }, "asplayer");

        // <--[tag]
        // @attribute <ElementTag.as_plugin>
        // @returns PluginTag
        // @group conversion
        // @deprecated use as[plugin]
        // @description
        // Deprecated in favor of <@link tag ObjectTag.as>
        // -->
        ElementTag.tagProcessor.registerStaticTag(PluginTag.class, "as_plugin", (attribute, object) -> {
            Deprecations.asXTags.warn(attribute.context);
            return ElementTag.handleNull(object.asString(), PluginTag.valueOf(object.asString(), attribute.context), "PluginTag", attribute.hasAlternative());
        }, "asplugin");

        // <--[tag]
        // @attribute <ElementTag.as_polygon>
        // @returns PolygonTag
        // @group conversion
        // @deprecated use as[polygon]
        // @description
        // Deprecated in favor of <@link tag ObjectTag.as>
        // -->
        ElementTag.tagProcessor.registerTag(PolygonTag.class, "as_polygon", (attribute, object) -> {
            Deprecations.asXTags.warn(attribute.context);
            return ElementTag.handleNull(object.asString(), PolygonTag.valueOf(object.asString(), attribute.context), "PolygonTag", attribute.hasAlternative());
        });

        // <--[tag]
        // @attribute <ElementTag.as_trade>
        // @returns TradeTag
        // @group conversion
        // @deprecated use as[trade]
        // @description
        // Deprecated in favor of <@link tag ObjectTag.as>
        // -->
        ElementTag.tagProcessor.registerStaticTag(TradeTag.class, "as_trade", (attribute, object) -> {
            Deprecations.asXTags.warn(attribute.context);
            return ElementTag.handleNull(object.asString(), TradeTag.valueOf(object.asString(), attribute.context), "TradeTag", attribute.hasAlternative());
        });

        // <--[tag]
        // @attribute <ElementTag.as_world>
        // @returns WorldTag
        // @group conversion
        // @deprecated use as[world]
        // @description
        // Deprecated in favor of <@link tag ObjectTag.as>
        // -->
        ElementTag.tagProcessor.registerTag(WorldTag.class, "as_world", (attribute, object) -> {
            Deprecations.asXTags.warn(attribute.context);
            return ElementTag.handleNull(object.asString(), WorldTag.valueOf(object.asString(), attribute.context), "WorldTag", attribute.hasAlternative());
        }, "asworld");

        // <--[tag]
        // @attribute <ElementTag.format[<script>]>
        // @returns ElementTag
        // @group text manipulation
        // @description
        // Returns the text re-formatted according to a format script.
        // -->
        ElementTag.tagProcessor.registerTag(ElementTag.class, ScriptTag.class, "format", (attribute, object, format) -> {
            if (!(format.getContainer() instanceof FormatScriptContainer)) {
                attribute.echoError("Script '" + format + "' is not a format script.");
                return null;
            }
            else {
                return new ElementTag(((FormatScriptContainer) format.getContainer()).getFormattedText(object.asString(),
                        attribute.getScriptEntry() != null ? ((BukkitScriptEntryData) attribute.getScriptEntry().entryData).getNPC() : null,
                        attribute.getScriptEntry() != null ? ((BukkitScriptEntryData) attribute.getScriptEntry().entryData).getPlayer() : null));
            }
        });

        // <--[tag]
        // @attribute <ElementTag.split_lines_by_width[<#>]>
        // @returns ElementTag
        // @group element manipulation
        // @description
        // Returns the element split into separate lines based on a maximum width in pixels per line.
        // This uses character width, so for example 20 "W"s and 20 "i"s will be treated differently.
        // The width used is based on the vanilla minecraft font. This will not be accurate for other fonts.
        // This only currently supports ASCII symbols properly. Unicode symbols will be estimated as 6 pixels.
        // Spaces will be preferred to become newlines, unless a line does not contain any spaces.
        // -->
        ElementTag.tagProcessor.registerStaticTag(ElementTag.class, ElementTag.class, "split_lines_by_width", (attribute, object, widthText) -> {
            return new ElementTag(TextWidthHelper.splitLines(object.asString(), widthText.asInt()));
        });

        // <--[tag]
        // @attribute <ElementTag.text_width>
        // @returns ElementTag(Number)
        // @group element manipulation
        // @description
        // Returns the width, in pixels, of the text.
        // The width used is based on the vanilla minecraft font. This will not be accurate for other fonts.
        // This only currently supports ASCII symbols properly. Unicode symbols will be estimated as 6 pixels.
        // If the element contains newlines, will return the widest line width.
        // -->
        ElementTag.tagProcessor.registerStaticTag(ElementTag.class, "text_width", (attribute, object) -> {
            return new ElementTag(TextWidthHelper.getWidth(object.asString()));
        });

        // <--[tag]
        // @attribute <ElementTag.lines_to_colored_list>
        // @returns ListTag
        // @group element manipulation
        // @description
        // Returns a list of lines in the element, with colors spread over the lines manually.
        // Useful for things like item lore.
        // -->
        ElementTag.tagProcessor.registerStaticTag(ListTag.class, "lines_to_colored_list", (attribute, object) -> {
            ListTag output = new ListTag();
            String colors = "";
            for (String line : CoreUtilities.split(object.asString(), '\n')) {
                output.add(colors + line);
                colors = org.bukkit.ChatColor.getLastColors(colors + line);
            }
            return output;
        });

        // <--[tag]
        // @attribute <ElementTag.last_color>
        // @returns ElementTag
        // @group text checking
        // @description
        // Returns the ChatColors used last in an element.
        // -->
        ElementTag.tagProcessor.registerStaticTag(ElementTag.class, "last_color", (attribute, object) -> {
            return new ElementTag(org.bukkit.ChatColor.getLastColors(object.asString()));
        });

        // <--[tag]
        // @attribute <ElementTag.strip_color>
        // @returns ElementTag
        // @group text manipulation
        // @description
        // Returns the element with all color encoding stripped.
        // -->
        ElementTag.tagProcessor.registerStaticTag(ElementTag.class, "strip_color", (attribute, object) -> {
            return new ElementTag(FormattedTextHelper.parse(object.asString(), ChatColor.WHITE)[0].toPlainText());
        });

        // <--[tag]
        // @attribute <ElementTag.parse_color[(<prefix>)]>
        // @returns ElementTag
        // @group text manipulation
        // @description
        // Returns the element with all color codes parsed.
        // Optionally, specify a character to prefix the color ids. Defaults to '&' if not specified.
        // This allows old-style colors like '&b', or Essentials-style hex codes like '&#ff00ff'
        // -->
        ElementTag.tagProcessor.registerStaticTag(ElementTag.class, "parse_color", (attribute, object) -> {
            char prefix = '&';
            if (attribute.hasParam()) {
                prefix = attribute.getParam().charAt(0);
            }
            String parsed = ChatColor.translateAlternateColorCodes(prefix, object.asString());
            parsed = replaceEssentialsHexColors(prefix, parsed);
            return new ElementTag(parsed);
        });

        // <--[tag]
        // @attribute <ElementTag.to_itemscript_hash>
        // @returns ElementTag
        // @group conversion
        // @description
        // Shortens the element down to an itemscript hash ID, made of invisible color codes.
        // This is considered a historical system, no longer relevant to modern Denizen.
        // -->
        ElementTag.tagProcessor.registerTag(ElementTag.class, "to_itemscript_hash", (attribute, object) -> {
            return new ElementTag(ItemScriptHelper.createItemScriptID(object.asString()));
        });

        // <--[tag]
        // @attribute <ElementTag.to_secret_colors>
        // @returns ElementTag
        // @group conversion
        // @description
        // Hides the element's text in invisible color codes.
        // Inverts <@link tag ElementTag.from_secret_colors>.
        // -->
        ElementTag.tagProcessor.registerStaticTag(ElementTag.class, "to_secret_colors", (attribute, object) -> {
            String text = object.asString();
            byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
            String hex = CoreUtilities.hexEncode(bytes);
            StringBuilder colors = new StringBuilder(text.length() * 2);
            for (int i = 0; i < hex.length(); i++) {
                colors.append(ChatColor.COLOR_CHAR).append(hex.charAt(i));
            }
            return new ElementTag(colors.toString());
        });

        // <--[tag]
        // @attribute <ElementTag.from_secret_colors>
        // @returns ElementTag
        // @group conversion
        // @description
        // Un-hides the element's text from invisible color codes back to normal text.
        // Inverts <@link tag ElementTag.to_secret_colors>.
        // -->
        ElementTag.tagProcessor.registerStaticTag(ElementTag.class, "from_secret_colors", (attribute, object) -> {
            String text = object.asString().replace(String.valueOf(ChatColor.COLOR_CHAR), "");
            byte[] bytes = CoreUtilities.hexDecode(text);
            return new ElementTag(new String(bytes, StandardCharsets.UTF_8));
        });

        // <--[tag]
        // @attribute <ElementTag.to_raw_json>
        // @returns ElementTag
        // @group conversion
        // @description
        // Converts normal colored text to Minecraft-style "raw JSON" format.
        // Inverts <@link tag ElementTag.from_raw_json>.
        // -->
        ElementTag.tagProcessor.registerStaticTag(ElementTag.class, "to_raw_json", (attribute, object) -> {
            return new ElementTag(ComponentSerializer.toString(FormattedTextHelper.parse(object.asString(), ChatColor.WHITE)));
        });

        // <--[tag]
        // @attribute <ElementTag.from_raw_json>
        // @returns ElementTag
        // @group conversion
        // @description
        // Un-hides the element's text from invisible color codes back to normal text.
        // Inverts <@link tag ElementTag.to_raw_json>.
        // -->
        ElementTag.tagProcessor.registerStaticTag(ElementTag.class, "from_raw_json", (attribute, object) -> {
            return new ElementTag(FormattedTextHelper.stringify(ComponentSerializer.parse(object.asString())));
        });

        // <--[tag]
        // @attribute <ElementTag.hover_item[<item>]>
        // @returns ElementTag
        // @group text manipulation
        // @description
        // Adds a hover message to the element, which makes the element display the input ItemTag when the mouse is left over it.
        // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
        // @example
        // - narrate "You can <element[hover here].custom_color[emphasis].hover_item[<player.item_in_hand>]> to see what you held!"
        // -->
        ElementTag.tagProcessor.registerStaticTag(ElementTag.class, ItemTag.class, "hover_item", (attribute, object, item) -> {
            return new ElementTag(ChatColor.COLOR_CHAR + "[hover=SHOW_ITEM;" + FormattedTextHelper.escape(item.identify()) + "]" + object.asString() + ChatColor.COLOR_CHAR + "[/hover]");
        });

        // <--[tag]
        // @attribute <ElementTag.on_hover[<message>]>
        // @returns ElementTag
        // @group text manipulation
        // @description
        // Adds a hover message to the element, which makes the element display the input hover text when the mouse is left over it.
        // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
        // -->
        ElementTag.tagProcessor.registerTag(ElementTag.class, ElementTag.class, "on_hover", (attribute, object, hoverText) -> { // non-static due to hacked sub-tag
            String type = "SHOW_TEXT";

            // <--[tag]
            // @attribute <ElementTag.on_hover[<message>].type[<type>]>
            // @returns ElementTag
            // @group text manipulation
            // @description
            // Adds a hover message to the element, which makes the element display the input hover text when the mouse is left over it.
            // Available hover types: SHOW_TEXT, SHOW_ACHIEVEMENT, SHOW_ITEM, or SHOW_ENTITY.
            // Note: for "SHOW_ITEM", replace the text with a valid ItemTag. For "SHOW_ENTITY", replace the text with a valid spawned EntityTag (requires F3+H to see entities).
            // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
            // For show_text, prefer <@link tag ElementTag.on_hover>
            // For show_item, prefer <@link tag ElementTag.hover_item>
            // -->
            if (attribute.startsWith("type", 2)) {
                type = attribute.getContext(2);
                attribute.fulfill(1);
            }
            return new ElementTag(ChatColor.COLOR_CHAR + "[hover=" + type + ";" + FormattedTextHelper.escape(hoverText.toString()) + "]"
                    + object.asString() + ChatColor.COLOR_CHAR + "[/hover]");
        });

        // <--[tag]
        // @attribute <ElementTag.click_url[<url>]>
        // @returns ElementTag
        // @group text manipulation
        // @description
        // Adds a click command to the element, which makes the element open the given URL when clicked.
        // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
        // @example
        // - narrate "You can <element[click here].custom_color[emphasis].on_hover[Click me!].click_url[https://denizenscript.com]> to learn about Denizen!"
        // -->
        ElementTag.tagProcessor.registerStaticTag(ElementTag.class, ElementTag.class, "click_url", (attribute, object, url) -> {
            return new ElementTag(ChatColor.COLOR_CHAR + "[click=OPEN_URL;" + FormattedTextHelper.escape(url.toString()) + "]" + object.asString() + ChatColor.COLOR_CHAR + "[/click]");
        });

        // <--[tag]
        // @attribute <ElementTag.click_chat[<message>]>
        // @returns ElementTag
        // @group text manipulation
        // @description
        // Adds a click command to the element, which makes the element pseudo-chat the input message when clicked, for activating interact script chat triggers (<@link language Chat Triggers>).
        // This internally uses the command "/denizenclickable chat SOME MESSAGE HERE" (requires players have permission "denizen.clickable")
        // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
        // @example
        // - narrate "You can <element[click here].click_chat[hello]> to say hello to an NPC's interact script!"
        // -->
        ElementTag.tagProcessor.registerStaticTag(ElementTag.class, ElementTag.class, "click_chat", (attribute, object, chat) -> {
            return new ElementTag(ChatColor.COLOR_CHAR + "[click=RUN_COMMAND;/denizenclickable chat " + FormattedTextHelper.escape(chat.toString()) + "]" + object.asString() + ChatColor.COLOR_CHAR + "[/click]");
        });

        // <--[tag]
        // @attribute <ElementTag.on_click[<command>]>
        // @returns ElementTag
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
            return new ElementTag(ChatColor.COLOR_CHAR + "[click=" + type + ";" + FormattedTextHelper.escape(command.asString()) + "]"
                    + object.asString() + ChatColor.COLOR_CHAR + "[/click]");
        });

        // <--[tag]
        // @attribute <ElementTag.with_insertion[<message>]>
        // @returns ElementTag
        // @group text manipulation
        // @description
        // Adds an insertion message to the element, which makes the element insert the input message to chat when shift-clicked.
        // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
        // -->
        ElementTag.tagProcessor.registerStaticTag(ElementTag.class, ElementTag.class, "with_insertion", (attribute, object, insertion) -> {
            return new ElementTag(ChatColor.COLOR_CHAR + "[insertion="  + FormattedTextHelper.escape(insertion.asString()) + "]"
                    + object.asString() + ChatColor.COLOR_CHAR + "[/insertion]");
        });

        // <--[tag]
        // @attribute <ElementTag.no_reset>
        // @returns ElementTag
        // @group text manipulation
        // @description
        // Makes a color code (&0123456789abcdef) not reset other formatting details.
        // Use like '<&c.no_reset>' or '<red.no_reset>'.
        // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
        // -->
        ElementTag.tagProcessor.registerStaticTag(ElementTag.class, "no_reset", (attribute, object) -> {
            if (object.asString().length() == 2 && object.asString().charAt(0) == ChatColor.COLOR_CHAR) {
                return new ElementTag(ChatColor.COLOR_CHAR + "[color=" + object.asString().charAt(1) + "]");
            }
            return null;
        });

        // <--[tag]
        // @attribute <ElementTag.end_format>
        // @returns ElementTag
        // @group text manipulation
        // @description
        // Makes a chat format code (&klmno, or &[font=...]) be the end of a format, as opposed to the start.
        // Use like '<&o.end_format>' or '<italic.end_format>'.
        // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
        // -->
        ElementTag.tagProcessor.registerStaticTag(ElementTag.class, "end_format", (attribute, object) -> {
            if (object.asString().length() == 2 && object.asString().charAt(0) == ChatColor.COLOR_CHAR) {
                return new ElementTag(ChatColor.COLOR_CHAR + "[reset=" + object.asString().charAt(1) + "]");
            }
            else if (object.asString().startsWith(ChatColor.COLOR_CHAR + "[font=") && object.asString().endsWith("]")) {
                return new ElementTag(ChatColor.COLOR_CHAR + "[reset=font]");
            }
            return null;
        });

        // <--[tag]
        // @attribute <ElementTag.italicize>
        // @returns ElementTag
        // @group text manipulation
        // @description
        // Makes the input text italic. Equivalent to "<&o><ELEMENT_HERE><&o.end_format>"
        // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
        // -->
        ElementTag.tagProcessor.registerStaticTag(ElementTag.class, "italicize", (attribute, object) -> {
            return new ElementTag(ChatColor.ITALIC + object.asString() + ChatColor.COLOR_CHAR + "[reset=o]");
        });

        // <--[tag]
        // @attribute <ElementTag.bold>
        // @returns ElementTag
        // @group text manipulation
        // @description
        // Makes the input text bold. Equivalent to "<&l><ELEMENT_HERE><&l.end_format>"
        // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
        // -->
        ElementTag.tagProcessor.registerStaticTag(ElementTag.class, "bold", (attribute, object) -> {
            return new ElementTag(ChatColor.BOLD + object.asString() + ChatColor.COLOR_CHAR + "[reset=l]");
        });

        // <--[tag]
        // @attribute <ElementTag.underline>
        // @returns ElementTag
        // @group text manipulation
        // @description
        // Makes the input text underlined. Equivalent to "<&n><ELEMENT_HERE><&n.end_format>"
        // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
        // -->
        ElementTag.tagProcessor.registerStaticTag(ElementTag.class, "underline", (attribute, object) -> {
            return new ElementTag(ChatColor.UNDERLINE + object.asString() + ChatColor.COLOR_CHAR + "[reset=n]");
        });

        // <--[tag]
        // @attribute <ElementTag.strikethrough>
        // @returns ElementTag
        // @group text manipulation
        // @description
        // Makes the input text struck-through. Equivalent to "<&m><ELEMENT_HERE><&m.end_format>"
        // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
        // -->
        ElementTag.tagProcessor.registerStaticTag(ElementTag.class, "strikethrough", (attribute, object) -> {
            return new ElementTag(ChatColor.STRIKETHROUGH + object.asString() + ChatColor.COLOR_CHAR + "[reset=m]");
        });

        // <--[tag]
        // @attribute <ElementTag.obfuscate>
        // @returns ElementTag
        // @group text manipulation
        // @description
        // Makes the input text obfuscated. Equivalent to "<&k><ELEMENT_HERE><&k.end_format>"
        // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
        // -->
        ElementTag.tagProcessor.registerStaticTag(ElementTag.class, "obfuscate", (attribute, object) -> {
            return new ElementTag(ChatColor.MAGIC + object.asString() + ChatColor.COLOR_CHAR + "[reset=k]");
        });

        // <--[tag]
        // @attribute <ElementTag.custom_color[<name>]>
        // @returns ElementTag
        // @group text manipulation
        // @description
        // Makes the input text colored by the custom color value based on the common base color names defined in the Denizen config file.
        // If the color name is unrecognized, returns the value of color named 'default'.
        // Default color names are 'base', 'emphasis', 'warning', 'error'.
        // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
        // -->
        ElementTag.tagProcessor.registerStaticTag(ElementTag.class, ElementTag.class, "custom_color", (attribute, object, name) -> {
            return new ElementTag(ChatColor.COLOR_CHAR + "[color=f]" + CustomColorTagBase.getColor(name.asLowerString(), attribute.context) + object.asString() + ChatColor.COLOR_CHAR + "[reset=color]");
        });

        // <--[tag]
        // @attribute <ElementTag.color[<color>]>
        // @returns ElementTag
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
                ChatColor color = ChatColor.getByChar(colorName.charAt(0));
                if (color != null) {
                    colorOut = color.toString();
                }
            }
            else if (colorName.length() == 7 && colorName.startsWith("#")) {
                return new ElementTag(ChatColor.COLOR_CHAR + "[color=" + colorName + "]" + object.asString() + ChatColor.COLOR_CHAR + "[reset=color]");
            }
            else if (colorName.length() == 14 && colorName.startsWith(ChatColor.COLOR_CHAR + "x")) {
                return new ElementTag(ChatColor.COLOR_CHAR + "[color=#" + CoreUtilities.replace(colorName.substring(2), String.valueOf(ChatColor.COLOR_CHAR), "") + "]" + object.asString() + ChatColor.COLOR_CHAR + "[reset=color]");
            }
            else if (colorName.startsWith("co@")) {
                ColorTag color = ColorTag.valueOf(colorName, attribute.context);
                StringBuilder hex = new StringBuilder(Integer.toHexString(color.getColor().asRGB()));
                while (hex.length() < 6) {
                    hex.insert(0, "0");
                }
                return new ElementTag(ChatColor.COLOR_CHAR + "[color=#" + hex + "]" + object.asString() + ChatColor.COLOR_CHAR + "[reset=color]");
            }
            if (colorOut == null) {
                try {
                    ChatColor color = ChatColor.of(colorName.toUpperCase());
                    String colorStr = color.toString().replace(String.valueOf(ChatColor.COLOR_CHAR), "").replace("x", "#");
                    colorOut = ChatColor.COLOR_CHAR + "[color=" + colorStr + "]";
                }
                catch (IllegalArgumentException ex) {
                    attribute.echoError("Color '" + colorName + "' doesn't exist (for ElementTag.color[...]).");
                    return null;
                }
            }
            return new ElementTag(colorOut + object.asString() + ChatColor.COLOR_CHAR + "[reset=color]");
        });

        // <--[tag]
        // @attribute <ElementTag.font[<font>]>
        // @returns ElementTag
        // @group text manipulation
        // @description
        // Makes the input text display with the input font name. Equivalent to "<&font[new-font]><ELEMENT_HERE><&font[new-font].end_format>"
        // The default font is "minecraft:default".
        // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
        // -->
        ElementTag.tagProcessor.registerStaticTag(ElementTag.class, ElementTag.class, "font", (attribute, object, fontName) -> {
            return new ElementTag(ChatColor.COLOR_CHAR + "[font=" + fontName + "]" + object.asString() + ChatColor.COLOR_CHAR + "[reset=font]");
        });

        // <--[tag]
        // @attribute <ElementTag.rainbow[(<pattern>)]>
        // @returns ElementTag
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
                output.append(ChatColor.COLOR_CHAR).append(pattern.charAt(i % pattern.length())).append(str.charAt(i));
            }
            return new ElementTag(output.toString());
        });

        // <--[tag]
        // @attribute <ElementTag.hex_rainbow[(<length>)]>
        // @returns ElementTag
        // @group text manipulation
        // @description
        // Returns the element with RGB rainbow colors applied.
        // Optionally, specify a length (how many characters before the colors repeat). If unspecified, will use the input element length.
        // If the element starts with a hex color code, that will be used as the starting color of the rainbow.
        // -->
        ElementTag.tagProcessor.registerStaticTag(ElementTag.class, "hex_rainbow", (attribute, object) -> {
            String str = object.asString();
            int[] HSB = new int[] { 0, 255, 255 };
            if (str.startsWith(ChatColor.COLOR_CHAR + "x") && str.length() > 14) {
                char[] colors = new char[6];
                for (int i = 0; i < 6; i++) {
                    colors[i] = str.charAt(3 + (i * 2));
                }
                int rgb = Integer.parseInt(new String(colors), 16);
                HSB = new ColorTag(Color.fromRGB(rgb)).toHSB();
                str = str.substring(14);
            }
            float hue = HSB[0] / 255f;
            int length = ChatColor.stripColor(str).length();
            if (length == 0) {
                return new ElementTag("");
            }
            if (attribute.hasParam()) {
                length = attribute.getIntParam();
            }
            float increment = 1.0f / length;
            String addedFormat = "";
            StringBuilder output = new StringBuilder(str.length() * 8);
            for (int i = 0; i < str.length(); i++) {
                char c = str.charAt(i);
                if (c == ChatColor.COLOR_CHAR && i + 1 < str.length()) {
                    char c2 = str.charAt(i + 1);
                    if (FORMAT_CODES_MATCHER.isMatch(c2)) {
                        addedFormat += String.valueOf(ChatColor.COLOR_CHAR) + c2;
                    }
                    else {
                        addedFormat = "";
                    }
                    i++;
                    continue;
                }
                String hex = Integer.toHexString(ColorTag.fromHSB(HSB).getColor().asRGB());
                output.append(FormattedTextHelper.stringifyRGBSpigot(hex)).append(addedFormat).append(c);
                hue += increment;
                HSB[0] = Math.round(hue * 255f);
            }
            return new ElementTag(output.toString());
        });

        // <--[tag]
        // @attribute <ElementTag.color_gradient[from=<color>;to=<color>;(style={RGB}/HSB)]>
        // @returns ElementTag
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
            if (!style.matchesEnum(BukkitElementExtensions.GradientStyle.class)) {
                attribute.echoError("Invalid gradient style '" + style + "'");
                return null;
            }
            String res = doGradient(object.asString(), fromColor, toColor, style.asEnum(GradientStyle.class));
            if (res == null) {
                return null;
            }
            return new ElementTag(res);
        });

        // <--[tag]
        // @attribute <ElementTag.hsb_color_gradient[from=<color>;to=<color>]>
        // @returns ElementTag
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
            return new ElementTag(res);
        });
    }

    public enum GradientStyle { RGB, HSB }

    public static String doGradient(String str, ColorTag fromColor, ColorTag toColor, GradientStyle style) {
        int length = FormattedTextHelper.parse(str, ChatColor.WHITE)[0].toPlainText().length();
        if (length == 0) {
            return "";
        }
        if (fromColor == null || toColor == null) {
            return null;
        }
        float r, g, b, x = 0, rMove, gMove, bMove, xMove = 0, toR, toG, toB;
        int[] hsbHelper = null;
        if (style == GradientStyle.RGB) {
            r = ColorTag.fromSRGB(fromColor.getColor().getRed());
            g = ColorTag.fromSRGB(fromColor.getColor().getGreen());
            b = ColorTag.fromSRGB(fromColor.getColor().getBlue());
            x = (float) Math.pow(r + g + b, 0.43);
            toR = ColorTag.fromSRGB(toColor.getColor().getRed());
            toG = ColorTag.fromSRGB(toColor.getColor().getGreen());
            toB = ColorTag.fromSRGB(toColor.getColor().getBlue());
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
            if (c == ChatColor.COLOR_CHAR && i + 1 < str.length()) {
                char c2 = str.charAt(i + 1);
                if (FORMAT_CODES_MATCHER.isMatch(c2)) {
                    addedFormat += String.valueOf(ChatColor.COLOR_CHAR) + c2;
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
                hex = Integer.toHexString(currentColor.getColor().asRGB());
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
