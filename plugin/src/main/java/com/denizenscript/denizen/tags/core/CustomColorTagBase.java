package com.denizenscript.denizen.tags.core;

import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.tags.TagManager;
import net.md_5.bungee.api.ChatColor;

import java.util.HashMap;

public class CustomColorTagBase {

    public static HashMap<String, String> customColorsRaw = new HashMap<>();

    public  static String defaultColorRaw = ChatColor.WHITE.toString();

    public static HashMap<String, String> customColors = new HashMap<>();

    public  static String defaultColor = null;

    public static String getColor(String nameLow, TagContext context) {
        String result = customColors.get(nameLow);
        if (result != null) {
            return result;
        }
        String unparsed = customColorsRaw.get(nameLow);
        if (unparsed != null) {
            result = TagManager.tag(unparsed, context);
            customColors.put(nameLow, result);
            return result;
        }
        if (TagManager.isStaticParsing) {
            return null;
        }
        if (defaultColor == null) {
            defaultColor = TagManager.tag(defaultColorRaw, context);
        }
        return defaultColor;
    }

    public CustomColorTagBase() {

        // <--[tag]
        // @attribute <&[<color>]>
        // @returns ElementTag
        // @description
        // Returns a custom color value based on the common base color names defined in the Denizen config file.
        // If the color name is unrecognized, returns the value of color named 'default'.
        // Default color names are 'base', 'emphasis', 'warning', 'error'.
        // -->
        TagManager.registerStaticTagBaseHandler(ElementTag.class, ElementTag.class, "&", (attribute, name) -> {
            String color = getColor(name.asLowerString(), attribute.context);
            if (color == null) {
                return null;
            }
            return new ElementTag(color, true);
        });
    }
}
