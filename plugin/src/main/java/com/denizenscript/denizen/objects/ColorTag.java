package com.denizenscript.denizen.objects;

import com.denizenscript.denizen.utilities.debugging.Debug;
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
    // @format
    // The identity format for colors is <red>,<green>,<blue> or the name of a color.
    // For example, 'co@50,64,128' or 'co@red'.
    //
    // @description
    // A ColorTag represents an RGB color code.
    //
    // Note that a ColorTag is NOT a base dye color (used by wool, etc). That is handled by a separate naming system.
    //
    // Constructing a ColorTag also accepts 'random' to pick a random RGB color, or hex code like '#FF00FF', or valid minecraft chat color codes (hex or historical codes).
    //
    // A list of accepted color names can be found at <@link tag server.color_names>.
    //
    // Red/green/blue values are each from 0 to 255.
    //
    // -->

    //////////////////
    //    OBJECT FETCHER
    ////////////////

    @Deprecated
    public static ColorTag valueOf(String string) {
        return valueOf(string, null);
    }


    public static HashMap<Color, String> nameByColor = new HashMap<>();
    public static HashMap<String, Color> colorsByName = new HashMap<>();

    static {
        for (Field field : Color.class.getDeclaredFields()) {
            if (field.getType() == Color.class) {
                try {
                    Color color = (Color) field.get(null);
                    colorsByName.put(CoreUtilities.toLowerCase(field.getName()), color);
                    nameByColor.put(color, CoreUtilities.toLowerCase(field.getName()));
                }
                catch (Exception ex) {
                    Debug.echoError(ex);
                }
            }
        }
    }

    public static AsciiMatcher HEX_MATCHER = new AsciiMatcher("0123456789abcdefABCDEF");

    /**
     * Gets a Color Object from a string form.
     *
     * @param string the string
     * @return a Color, or null if incorrectly formatted
     */
    @Fetchable("co")
    public static ColorTag valueOf(String string, TagContext context) {
        string = CoreUtilities.toLowerCase(string);
        if (string.startsWith("co@")) {
            string = string.substring("co@".length());
        }
        if (string.equals("random")) {
            // Get a color using random RGB values
            return new ColorTag(CoreUtilities.getRandom().nextInt(256),
                    CoreUtilities.getRandom().nextInt(256),
                    CoreUtilities.getRandom().nextInt(256));
        }
        if (string.startsWith("#") && string.length() == 7 && HEX_MATCHER.isOnlyMatches(string.substring(1))) {
            return new ColorTag(Color.fromRGB(Integer.parseInt(string.substring(1), 16)));
        }
        if (string.startsWith(ChatColor.COLOR_CHAR + "x") && string.length() == 14) {
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
        if (split.size() == 3) {
            if (!ArgumentHelper.matchesInteger(split.get(0)) || !ArgumentHelper.matchesInteger(split.get(1)) || !ArgumentHelper.matchesInteger(split.get(2))) {
                return null;
            }
            return new ColorTag(Integer.parseInt(split.get(0)), Integer.parseInt(split.get(1)), Integer.parseInt(split.get(2)));
        }
        Color col = colorsByName.get(string);
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

    public ColorTag(int red, int green, int blue) {
        color = Color.fromRGB(red, green, blue);
    }

    public ColorTag(Color color) {
        this.color = color;
    }

    public ColorTag(DyeColor dyeColor) {
        this.color = dyeColor.getColor();
    }

    /////////////////////
    //   INSTANCE FIELDS/METHODS
    /////////////////

    // Associated with Bukkit Color

    private Color color;

    public Color getColor() {
        return color;
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
    public String getObjectType() {
        return "Color";
    }

    @Override
    public String identify() {
        String name = nameByColor.get(getColor());
        if (name != null) {
            return "co@" + name;
        }
        return "co@" + getColor().getRed() + "," + getColor().getGreen() + "," + getColor().getBlue();
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

    public static void registerTags() {

        // <--[tag]
        // @attribute <ColorTag.hex>
        // @returns ElementTag
        // @description
        // Returns a hex code formatting of this color, like '#ff00ff'.
        // -->
        tagProcessor.registerTag(ElementTag.class, "hex", (attribute, object) -> {
            return new ElementTag("#" + CoreUtilities.hexEncode(new byte[] { (byte) object.color.getRed(), (byte) object.color.getGreen(), (byte) object.color.getBlue() }));
        });

        // <--[tag]
        // @attribute <ColorTag.red>
        // @returns ElementTag(Number)
        // @description
        // Returns the red value of this color (0 to 255).
        // -->
        tagProcessor.registerTag(ElementTag.class, "red", (attribute, object) -> {
            return new ElementTag(object.color.getRed());
        });

        // <--[tag]
        // @attribute <ColorTag.green>
        // @returns ElementTag(Number)
        // @description
        // Returns the green value of this color (0 to 255).
        // -->
        tagProcessor.registerTag(ElementTag.class, "green", (attribute, object) -> {
            return new ElementTag(object.color.getGreen());
        });

        // <--[tag]
        // @attribute <ColorTag.blue>
        // @returns ElementTag(Number)
        // @description
        // Returns the blue value of this color (0 to 255).
        // -->
        tagProcessor.registerTag(ElementTag.class, "blue", (attribute, object) -> {
            return new ElementTag(object.color.getBlue());
        });

        // <--[tag]
        // @attribute <ColorTag.rgb>
        // @returns ElementTag
        // @description
        // Returns the RGB value of this color.
        // EG, 255,0,255
        // -->
        tagProcessor.registerTag(ElementTag.class, "rgb", (attribute, object) -> {
            Color color = object.color;
            return new ElementTag(color.getRed() + "," + color.getGreen() + "," + color.getBlue());
        });

        // <--[tag]
        // @attribute <ColorTag.hue>
        // @returns ElementTag(Number)
        // @description
        // Returns the hue value of this color (0 to 255).
        // -->
        tagProcessor.registerTag(ElementTag.class, "hue", (attribute, object) -> {
            return new ElementTag(object.toHSB()[0]);
        });

        // <--[tag]
        // @attribute <ColorTag.saturation>
        // @returns ElementTag(Number)
        // @description
        // Returns the saturation value of this color (0 to 255).
        // -->
        tagProcessor.registerTag(ElementTag.class, "saturation", (attribute, object) -> {
            return new ElementTag(object.toHSB()[1]);
        });

        // <--[tag]
        // @attribute <ColorTag.brightness>
        // @returns ElementTag(Number)
        // @description
        // Returns the brightness value of this color (0 to 255).
        // -->
        tagProcessor.registerTag(ElementTag.class, "brightness", (attribute, object) -> {
            return new ElementTag(object.toHSB()[2]);
        });

        // <--[tag]
        // @attribute <ColorTag.hsv>
        // @returns ElementTag
        // @description
        // Returns the HSV value of this color.
        // EG, 100,100,255
        // -->
        tagProcessor.registerTag(ElementTag.class, "hsv", (attribute, object) -> {
            int[] HSV = object.toHSB();
            return new ElementTag(HSV[0] + "," + HSV[1] + "," + HSV[2]);
        });

        // <--[tag]
        // @attribute <ColorTag.with_red[<red>]>
        // @returns ColorTag
        // @description
        // Returns a copy of this color object with a different red value (0 to 255).
        // -->
        tagProcessor.registerTag(ColorTag.class, "with_red", (attribute, object) -> {
            return new ColorTag(object.color.setRed(attribute.getIntParam()));
        });

        // <--[tag]
        // @attribute <ColorTag.with_green[<green>]>
        // @returns ColorTag
        // @description
        // Returns a copy of this color object with a different green value (0 to 255).
        // -->
        tagProcessor.registerTag(ColorTag.class, "with_green", (attribute, object) -> {
            return new ColorTag(object.color.setGreen(attribute.getIntParam()));
        });

        // <--[tag]
        // @attribute <ColorTag.with_blue[<blue>]>
        // @returns ColorTag
        // @description
        // Returns a copy of this color object with a different blue value (0 to 255).
        // -->
        tagProcessor.registerTag(ColorTag.class, "with_blue", (attribute, object) -> {
            return new ColorTag(object.color.setBlue(attribute.getIntParam()));
        });

        // <--[tag]
        // @attribute <ColorTag.with_hue[<hue>]>
        // @returns ColorTag
        // @description
        // Returns a copy of this color object with a different hue value (0 to 255).
        // -->
        tagProcessor.registerTag(ColorTag.class, "with_hue", (attribute, object) -> {
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
        tagProcessor.registerTag(ColorTag.class, "with_saturation", (attribute, object) -> {
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
        tagProcessor.registerTag(ColorTag.class, "with_brightness", (attribute, object) -> {
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
        tagProcessor.registerTag(ElementTag.class, "name", (attribute, object) -> {
            return new ElementTag(object.identify().substring(3));
        });

        // <--[tag]
        // @attribute <ColorTag.mix[<color>]>
        // @returns ColorTag
        // @description
        // Returns the color that results if you mix this color with another.
        // -->
        tagProcessor.registerTag(ColorTag.class, "mix", (attribute, object) -> {
            if (!attribute.hasParam()) {
                Debug.echoError("The tag ListTag.insert[...] must have a value.");
                return null;
            }
            ColorTag mixed_with = attribute.paramAsType(ColorTag.class);
            if (mixed_with != null) {
                return new ColorTag(object.color.mixColors(mixed_with.getColor()));
            }
            else {
                Debug.echoError("'" + attribute.getParam() + "' is not a valid color!");
                return null;
            }
        });

        // <--[tag]
        // @attribute <ColorTag.to_particle_offset>
        // @returns LocationTag
        // @description
        // Returns the color as a particle offset, for use with <@link command playeffect>.
        // -->
        tagProcessor.registerTag(LocationTag.class, "to_particle_offset", (attribute, object) -> {
            Color valid = object.color;
            if (valid.asRGB() == 0) {
                valid = Color.fromRGB(1, 0, 0);
            }
            return new LocationTag(null, valid.getRed() / 255F, valid.getGreen() / 255F, valid.getBlue() / 255F);
        });
    }

    public static ObjectTagProcessor<ColorTag> tagProcessor = new ObjectTagProcessor<>();

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {
        return tagProcessor.getObjectAttribute(this, attribute);
    }

    public static ColorTag fromHSB(int[] hsv) {
        int rgb = java.awt.Color.HSBtoRGB(hsv[0] / 255f, hsv[1] / 255f, hsv[2] / 255f) & 0x00ffffff;
        return new ColorTag(Color.fromRGB(rgb));
    }

    public int[] toHSB() {
        float[] base = java.awt.Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
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
