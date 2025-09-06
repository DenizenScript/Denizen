package com.denizenscript.denizen.objects.properties.bukkit;

import com.denizenscript.denizen.objects.*;
import com.denizenscript.denizen.objects.properties.item.ItemRawNBT;
import com.denizenscript.denizen.scripts.containers.core.ItemScriptHelper;
import com.denizenscript.denizen.utilities.TextWidthHelper;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.core.ColorTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.utilities.CoreConfiguration;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.Deprecations;
import net.md_5.bungee.api.ChatColor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class BukkitElementExtensions {

    public static String replaceEssentialsHexColors(char prefix, String input) {
        int hex = input.indexOf(prefix + "#");
        while (hex != -1 && hex + 7 < input.length()) {
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
        // @attribute <ElementTag.parse_color[(<prefix>)]>
        // @returns ElementTag
        // @group text manipulation
        // @description
        // Returns the element with all color codes parsed.
        // Optionally, specify a character to prefix the color ids. Defaults to '&' if not specified.
        // This allows old-style colors like '&b', or Essentials-style hex codes like '&#ff00ff'
        // See also <@link tag ElementTag.parse_minimessage>
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
        // @attribute <ElementTag.snbt_to_map>
        // @returns MapTag
        // @description
        // Parses a raw SNBT string into an NBT MapTag.
        // See <@link language Raw NBT Encoding> for more information on the returned MapTag.
        // See <@link url https://minecraft.wiki/w/NBT_format#SNBT_format> for more information on SNBT.
        // @example
        // # Use to set certain SNBT data onto an entity.
        // - adjust <[entity]> raw_nbt:<[snbt].snbt_to_map>
        // -->
        ElementTag.tagProcessor.registerStaticTag(MapTag.class, "snbt_to_map", (attribute, object) -> {
            try {
                return (MapTag) ItemRawNBT.nbtTagToObject(ItemRawNBT.SNBT_PARSER.asCompound(object.asString()));
            }
            catch (IOException e) {
                attribute.echoError("Element '<Y>" + object + "<W>' isn't valid SNBT: " + e.getMessage());
                if (CoreConfiguration.debugVerbose) {
                    attribute.echoError(e);
                }
                return null;
            }
        });
    }
}
