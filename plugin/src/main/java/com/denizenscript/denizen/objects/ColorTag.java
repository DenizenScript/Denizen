package com.denizenscript.denizen.objects;

import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.AsciiMatcher;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;

public class ColorTag implements ObjectTag {

    // <--[ObjectType]
    // @name ColorTag
    // @prefix co
    // @base ElementTag
    // @ExampleTagBase color[red]
    // @ExampleValues red,green,blue
    // @ExampleForReturns
    // - inventory adjust slot:hand color:%VALUE%
    // @ExampleForReturns
    // - narrate "<&color[%VALUE%]>hello there!"
    // @format
    // The identity format for colors is <red>,<green>,<blue> or <red>,<green>,<blue>,<alpha> or the name of a color.
    // When using the numeric form, the number must be between 0 and 255, where 0 is least bright and 255 is most bright.
    // For example, 'co@50,64,128' or 'co@255,0,0,128' or 'co@red'.
    //
    // @description
    // A ColorTag represents an RGBA color code.
    //
    // Note that a ColorTag is NOT a base dye color (used by wool, etc). That is handled by a separate naming system.
    //
    // Constructing a ColorTag also accepts 'random' to pick a random RGB color,
    //     or 'transparent' to return 0,0,0,0
    //     or RGB hex code like '#FF00FF',
    //     or RGBA hex codes like '#FF00FF80',
    //     or valid minecraft chat color codes (hex or historical codes).
    //
    // A list of accepted color names can be found at <@link tag server.color_names>.
    //
    // Red/green/blue/alpha values are each from 0 to 255.
    //
    // -->

    //////////////////
    //    OBJECT FETCHER
    ////////////////

    @Deprecated
    public static ColorTag valueOf(String string) {
        return valueOf(string, null);
    }


    public static HashMap<ColorTag, String> nameByColor = new HashMap<>();
    public static HashMap<String, ColorTag> colorsByName = new HashMap<>();

    static {
        for (Field field : Color.class.getDeclaredFields()) {
            if (field.getType() == Color.class) {
                try {
                    Color color = (Color) field.get(null);
                    colorsByName.put(CoreUtilities.toLowerCase(field.getName()), new ColorTag(color));
                    nameByColor.put(new ColorTag(color), CoreUtilities.toLowerCase(field.getName()));
                }
                catch (Exception ex) {
                    Debug.echoError(ex);
                }
            }
        }
        nameByColor.put(new ColorTag(0, 0, 0, 0), "transparent");
        colorsByName.put("transparent", new ColorTag(0, 0, 0, 0));
    }

    public static AsciiMatcher HEX_MATCHER = new AsciiMatcher("0123456789abcdefABCDEF");

    @Fetchable("co")
    public static ColorTag valueOf(String string, TagContext context) {
        string = CoreUtilities.toLowerCase(string);
        if (string.startsWith("co@")) {
            string = string.substring("co@".length());
        }
        if (string.equals("random")) {
            if (TagManager.isStaticParsing) {
                return null;
            }
            // Get a color using random RGB values
            return new ColorTag(CoreUtilities.getRandom().nextInt(256),
                    CoreUtilities.getRandom().nextInt(256),
                    CoreUtilities.getRandom().nextInt(256));
        }
        if (string.startsWith("#") && string.length() == 7 && HEX_MATCHER.isOnlyMatches(string.substring(1))) {
            return new ColorTag(Color.fromRGB(Integer.parseInt(string.substring(1), 16)));
        }
        else if (string.startsWith("#") && string.length() == 9 && HEX_MATCHER.isOnlyMatches(string.substring(1))) {
            ColorTag result = new ColorTag(Color.fromRGB(Integer.parseInt(string.substring(1, 7), 16)));
            result.alpha = Integer.parseInt(string.substring(7, 9), 16);
            return result;
        }
        else if (string.startsWith(ChatColor.COLOR_CHAR + "#") && string.length() == 8 && HEX_MATCHER.isOnlyMatches(string.substring(2))) {
            return new ColorTag(Color.fromRGB(Integer.parseInt(string.substring(2), 16)));
        }
        else if (string.startsWith(ChatColor.COLOR_CHAR + "x") && string.length() == 14) {
            String pieces = string.substring(2).replace(String.valueOf(ChatColor.COLOR_CHAR), "");
            if (pieces.length() == 6 && HEX_MATCHER.isOnlyMatches(pieces)) {
                return new ColorTag(Color.fromRGB(Integer.parseInt(pieces, 16)));
            }
        }
        else if (string.startsWith(String.valueOf(ChatColor.COLOR_CHAR)) && string.length() == 2 && HEX_MATCHER.isOnlyMatches(string.substring(1))) {
            // the & is because 'getRGB' actually returns ARGB, so strip the Alpha
            return new ColorTag(Color.fromRGB(ChatColor.getByChar(string.charAt(1)).asBungee().getColor().getRGB() & 0xFFFFFF));
        }
        List<String> split = CoreUtilities.split(string, ',');
        if (split.size() == 3 || split.size() == 4) {
            if (!ArgumentHelper.matchesInteger(split.get(0)) || !ArgumentHelper.matchesInteger(split.get(1)) || !ArgumentHelper.matchesInteger(split.get(2))) {
                if (context == null || context.showErrors()) {
                    Debug.echoError("Cannot construct ColorTag - Red, Green, or Blue input not a number.");
                }
                return null;
            }
            if (split.size() == 4 && !ArgumentHelper.matchesInteger(split.get(3))) {
                if (context == null || context.showErrors()) {
                    Debug.echoError("Cannot construct ColorTag - alpha not a number.");
                }
                return null;
            }
            int red = Integer.parseInt(split.get(0));
            int green = Integer.parseInt(split.get(1));
            int blue = Integer.parseInt(split.get(2));
            int alpha = split.size() == 4 ? Integer.parseInt(split.get(3)) : 255;
            if (red < 0 || red > 255 || green < 0 || green > 255 || blue < 0 || blue > 255 || alpha < 0 || alpha > 255) {
                if (context == null || context.showErrors()) {
                    Debug.echoError("Cannot construct ColorTag - values out of range, must be between 0 and 255.");
                }
                return null;
            }
            return new ColorTag(red, green, blue, alpha);
        }
        ColorTag col = colorsByName.get(string);
        if (col != null) {
            return new ColorTag(col);
        }
        // No match
        return null;
    }

    /**
     * Determines whether a string is a valid color.
     *
     * @param arg the string
     * @return true if matched, otherwise false
     */
    public static boolean matches(String arg) {
        if (arg.startsWith("co@")) {
            return true;
        }
        if (valueOf(arg, CoreUtilities.noDebugContext) != null) {
            return true;
        }
        return false;
    }

    ///////////////
    //   Constructors
    /////////////

    public ColorTag(int red, int green, int blue, int alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }

    public ColorTag(int red, int green, int blue) {
        this(red, green, blue, 255);
    }

    public ColorTag(Color color) {
        this(color.getRed(), color.getGreen(), color.getBlue(), 255);
    }

    public ColorTag(DyeColor dyeColor) {
        this(dyeColor.getColor());
    }

    public ColorTag(ColorTag color) {
        this(color.red, color.green, color.blue, color.alpha);
    }

    /////////////////////
    //   INSTANCE FIELDS/METHODS
    /////////////////

    public int red, green, blue, alpha;

    public Color getColor() {
        return Color.fromRGB(red, green, blue);
    }

    public java.awt.Color getAWTColor() {
        return new java.awt.Color(red, green, blue, alpha);
    }

    String prefix = "color";

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public boolean isUnique() {
        return false;
    }

    @Override
    public String identify() {
        String name = nameByColor.get(this);
        if (name != null) {
            return "co@" + name;
        }
        if (alpha != 255) {
            return "co@" + red + "," + green + "," + blue + "," + alpha;
        }
        return "co@" + red + "," + green + "," + blue;
    }

    @Override
    public String identifySimple() {
        return identify();
    }

    @Override
    public String toString() {
        return identify();
    }

    @Override
    public ObjectTag setPrefix(String prefix) {
        if (prefix != null) {
            this.prefix = prefix;
        }
        return this;
    }

    @Override
    public int hashCode() {
        return red + green << 8 + blue << 16 + alpha << 24;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ColorTag)) {
            return false;
        }
        ColorTag otherColor = (ColorTag) other;
        return red == otherColor.red && blue == otherColor.blue && green == otherColor.green && alpha == otherColor.alpha;
    }

    public static void registerTags() {

        // <--[tag]
        // @attribute <ColorTag.hex>
        // @returns ElementTag
        // @description
        // Returns a hex code formatting of this color, like '#ff00ff'.
        // -->
        tagProcessor.registerStaticTag(ElementTag.class, "hex", (attribute, object) -> {
            if (object.alpha != 255) {
                return new ElementTag("#" + CoreUtilities.hexEncode(new byte[] { (byte) object.red, (byte) object.green, (byte) object.blue, (byte)object.alpha }));
            }
            else {
                return new ElementTag("#" + CoreUtilities.hexEncode(new byte[] { (byte) object.red, (byte) object.green, (byte) object.blue }));
            }
        });

        // <--[tag]
        // @attribute <ColorTag.rgb_integer>
        // @returns ElementTag(Number)
        // @description
        // Returns the Red, Green, and Blue values of this ColorTag as an integer number, equivalent to an integer reparse of <@link tag ColorTag.hex>.
        // Highest order bits are red, then green, then lowest is blue.
        // This is a rare special case encoding usually avoided by most systems, but may be necessary for some obscure tools.
        // -->
        tagProcessor.registerStaticTag(ElementTag.class, "rgb_integer", (attribute, object) -> {
            return new ElementTag((object.red << 16) | (object.green << 8) | (object.blue));
        });

        // <--[tag]
        // @attribute <ColorTag.argb_integer>
        // @returns ElementTag(Number)
        // @description
        // Returns the Alpha, Red, Green, and Blue values of this ColorTag as an integer number, equivalent to an integer reparse of <@link tag ColorTag.hex>.
        // Highest order bits are alpha, then red, then green, then lowest is blue.
        // This is a rare special case encoding usually avoided by most systems, but may be necessary for some obscure tools.
        // -->
        tagProcessor.registerStaticTag(ElementTag.class, "argb_integer", (attribute, object) -> {
            return new ElementTag(((long) object.alpha << 24L) | ((long) object.red << 16L) | ((long) object.green << 8L) | ((long) object.blue));
        });

        // <--[tag]
        // @attribute <ColorTag.red>
        // @returns ElementTag(Number)
        // @description
        // Returns the red value of this color (0 to 255).
        // -->
        tagProcessor.registerStaticTag(ElementTag.class, "red", (attribute, object) -> {
            return new ElementTag(object.red);
        });

        // <--[tag]
        // @attribute <ColorTag.green>
        // @returns ElementTag(Number)
        // @description
        // Returns the green value of this color (0 to 255).
        // -->
        tagProcessor.registerStaticTag(ElementTag.class, "green", (attribute, object) -> {
            return new ElementTag(object.green);
        });

        // <--[tag]
        // @attribute <ColorTag.blue>
        // @returns ElementTag(Number)
        // @description
        // Returns the blue value of this color (0 to 255).
        // -->
        tagProcessor.registerStaticTag(ElementTag.class, "blue", (attribute, object) -> {
            return new ElementTag(object.blue);
        });

        // <--[tag]
        // @attribute <ColorTag.alpha>
        // @returns ElementTag(Number)
        // @description
        // Returns the alpha value of this color (0 to 255).
        // -->
        tagProcessor.registerStaticTag(ElementTag.class, "alpha", (attribute, object) -> {
            return new ElementTag(object.alpha);
        });

        // <--[tag]
        // @attribute <ColorTag.rgb>
        // @returns ElementTag
        // @description
        // Returns the RGB value of this color.
        // EG, 255,0,255
        // -->
        tagProcessor.registerStaticTag(ElementTag.class, "rgb", (attribute, object) -> {
            return new ElementTag(object.red + "," + object.green + "," + object.blue);
        });

        // <--[tag]
        // @attribute <ColorTag.rgba>
        // @returns ElementTag
        // @description
        // Returns the RGBA value of this color.
        // EG, 255,0,255,255
        // -->
        tagProcessor.registerStaticTag(ElementTag.class, "rgba", (attribute, object) -> {
            return new ElementTag(object.red + "," + object.green + "," + object.blue + "," + object.alpha);
        });

        // <--[tag]
        // @attribute <ColorTag.hue>
        // @returns ElementTag(Number)
        // @description
        // Returns the hue value of this color (0 to 255).
        // -->
        tagProcessor.registerStaticTag(ElementTag.class, "hue", (attribute, object) -> {
            return new ElementTag(object.toHSB()[0]);
        });

        // <--[tag]
        // @attribute <ColorTag.saturation>
        // @returns ElementTag(Number)
        // @description
        // Returns the saturation value of this color (0 to 255).
        // -->
        tagProcessor.registerStaticTag(ElementTag.class, "saturation", (attribute, object) -> {
            return new ElementTag(object.toHSB()[1]);
        });

        // <--[tag]
        // @attribute <ColorTag.brightness>
        // @returns ElementTag(Number)
        // @description
        // Returns the brightness value of this color (0 to 255).
        // -->
        tagProcessor.registerStaticTag(ElementTag.class, "brightness", (attribute, object) -> {
            return new ElementTag(object.toHSB()[2]);
        });

        // <--[tag]
        // @attribute <ColorTag.hsv>
        // @returns ElementTag
        // @description
        // Returns the HSV value of this color.
        // EG, 100,100,255
        // -->
        tagProcessor.registerStaticTag(ElementTag.class, "hsv", (attribute, object) -> {
            int[] HSV = object.toHSB();
            return new ElementTag(HSV[0] + "," + HSV[1] + "," + HSV[2]);
        });

        // <--[tag]
        // @attribute <ColorTag.with_red[<red>]>
        // @returns ColorTag
        // @description
        // Returns a copy of this color object with a different red value (0 to 255).
        // -->
        tagProcessor.registerStaticTag(ColorTag.class, "with_red", (attribute, object) -> {
            return new ColorTag(attribute.getIntParam(), object.green, object.blue, object.alpha);
        });

        // <--[tag]
        // @attribute <ColorTag.with_green[<green>]>
        // @returns ColorTag
        // @description
        // Returns a copy of this color object with a different green value (0 to 255).
        // -->
        tagProcessor.registerStaticTag(ColorTag.class, "with_green", (attribute, object) -> {
            return new ColorTag(object.red, attribute.getIntParam(), object.blue, object.alpha);
        });

        // <--[tag]
        // @attribute <ColorTag.with_blue[<blue>]>
        // @returns ColorTag
        // @description
        // Returns a copy of this color object with a different blue value (0 to 255).
        // -->
        tagProcessor.registerStaticTag(ColorTag.class, "with_blue", (attribute, object) -> {
            return new ColorTag(object.red, object.green, attribute.getIntParam(), object.alpha);
        });

        // <--[tag]
        // @attribute <ColorTag.with_alpha[<alpha>]>
        // @returns ColorTag
        // @description
        // Returns a copy of this color object with a different alpha value (0 to 255).
        // -->
        tagProcessor.registerStaticTag(ColorTag.class, "with_alpha", (attribute, object) -> {
            return new ColorTag(object.red, object.green, object.alpha, attribute.getIntParam());
        });

        // <--[tag]
        // @attribute <ColorTag.with_hue[<hue>]>
        // @returns ColorTag
        // @description
        // Returns a copy of this color object with a different hue value (0 to 255).
        // -->
        tagProcessor.registerStaticTag(ColorTag.class, "with_hue", (attribute, object) -> {
            int[] HSB = object.toHSB();
            HSB[0] = attribute.getIntParam();
            return fromHSB(HSB);
        });

        // <--[tag]
        // @attribute <ColorTag.with_saturation[<saturation>]>
        // @returns ColorTag
        // @description
        // Returns a copy of this color object with a different saturation value (0 to 255).
        // -->
        tagProcessor.registerStaticTag(ColorTag.class, "with_saturation", (attribute, object) -> {
            int[] HSB = object.toHSB();
            HSB[1] = attribute.getIntParam();
            return fromHSB(HSB);
        });

        // <--[tag]
        // @attribute <ColorTag.with_brightness[<brightness>]>
        // @returns ColorTag
        // @description
        // Returns a copy of this color object with a different brightness value (0 to 255).
        // -->
        tagProcessor.registerStaticTag(ColorTag.class, "with_brightness", (attribute, object) -> {
            int[] HSB = object.toHSB();
            HSB[2] = attribute.getIntParam();
            return fromHSB(HSB);
        });

        // <--[tag]
        // @attribute <ColorTag.name>
        // @returns ElementTag
        // @description
        // Returns the name of this color (or red,green,blue if none).
        // -->
        tagProcessor.registerStaticTag(ElementTag.class, "name", (attribute, object) -> {
            return new ElementTag(object.identify().substring("co@".length()));
        });

        // <--[tag]
        // @attribute <ColorTag.mix[<color>]>
        // @returns ColorTag
        // @description
        // Returns the color that results if you mix this color with another.
        // -->
        tagProcessor.registerTag(ColorTag.class, ColorTag.class, "mix", (attribute, object, mixWith) -> { // Temporarily non-static because the input could be 'random'
            return new ColorTag(object.mixWith(mixWith));
        });

        // <--[tag]
        // @attribute <ColorTag.to_particle_offset>
        // @returns LocationTag
        // @description
        // Returns the color as a particle offset, for use with <@link command playeffect>.
        // -->
        tagProcessor.registerStaticTag(LocationTag.class, "to_particle_offset", (attribute, object) -> {
            if (object.red + object.green + object.blue == 0) {
                return new LocationTag(null, 1 / 255f, 0, 0);
            }
            return new LocationTag(null, object.red / 255F, object.green / 255F, object.blue / 255F);
        });
    }

    public static ObjectTagProcessor<ColorTag> tagProcessor = new ObjectTagProcessor<>();

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {
        return tagProcessor.getObjectAttribute(this, attribute);
    }

    // Based on Bukkit's Color#mix
    public ColorTag mixWith(ColorTag two) {
        int totalRed = red + two.red;
        int totalGreen = green + two.green;
        int totalBlue = blue + two.blue;
        int totalAlpha = alpha + two.alpha;
        int totalMax = Math.max(Math.max(red, green), blue) + Math.max(Math.max(two.red, two.green), two.blue);
        float averageRed = (float)(totalRed / 2);
        float averageGreen = (float)(totalGreen / 2);
        float averageBlue = (float)(totalBlue / 2);
        float averageAlpha = (float)(totalAlpha / 2);
        float averageMax = (float)(totalMax / 2);
        float maximumOfAverages = Math.max(Math.max(averageRed, averageGreen), averageBlue);
        float gainFactor = averageMax / maximumOfAverages;
        return new ColorTag((int)(averageRed * gainFactor), (int)(averageGreen * gainFactor), (int)(averageBlue * gainFactor), (int)averageAlpha);
    }

    public static ColorTag fromHSB(int[] hsv) {
        int rgb = java.awt.Color.HSBtoRGB(hsv[0] / 255f, hsv[1] / 255f, hsv[2] / 255f) & 0x00ffffff;
        return new ColorTag(Color.fromRGB(rgb));
    }

    public int[] toHSB() {
        float[] base = java.awt.Color.RGBtoHSB(red, green, blue, null);
        return new int[] {Math.round(base[0] * 255f), Math.round(base[1] * 255f), Math.round(base[2] * 255f)};
    }

    // Based on https://stackoverflow.com/questions/22607043/color-gradient-algorithm/49321304#49321304
    public static float fromSRGB(float x) {
        x /= 255f;
        if (x <= 0.04045) {
            return x / 12.92f;
        }
        return (float) Math.pow((x + 0.055) / 1.055, 2.4);
    }

    public static float toSRGB(float x) {
        return 255.9999f * (x <= 0.0031308f ? 12.92f * x : (1.055f * ((float) Math.pow(x, 1f / 2.4f)) - 0.055f));
    }
}
