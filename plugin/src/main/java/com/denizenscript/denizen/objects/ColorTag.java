package com.denizenscript.denizen.objects;

import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.tags.TagRunnable;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.Color;
import org.bukkit.DyeColor;

import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorTag implements ObjectTag {

    // <--[language]
    // @name ColorTag Objects
    // @group Object System
    // @description
    // A ColorTag represents an RGB color code.
    //
    // Note that a ColorTag is NOT a base dye color (used by wool, etc). That is handled by a separate naming system.
    //
    // For format info, see <@link language co@>
    //
    // -->

    // <--[language]
    // @name co@
    // @group Object Fetcher System
    // @description
    // co@ refers to the 'object identifier' of a ColorTag. The 'co@' is notation for Denizen's Object
    // Fetcher. The constructor for a ColorTag is <red>,<green>,<blue>, or the name of a color.
    // For example, 'co@50,64,128' or 'co@red'.
    //
    // A list of accepted color names can be found at
    // <@link url https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Color.html>
    //
    // Red/green/blue values are each from 0 to 256.
    //
    // For general info, see <@link language ColorTag Objects>
    //
    // -->

    final static Pattern rgbPattern = Pattern.compile("(\\d+)[,:](\\d+)[,:](\\d+)");

    //////////////////
    //    OBJECT FETCHER
    ////////////////

    public static ColorTag valueOf(String string) {
        return valueOf(string, null);
    }

    /**
     * Gets a Color Object from a string form.
     *
     * @param string the string
     * @return a Color, or null if incorrectly formatted
     */
    @Fetchable("co")
    public static ColorTag valueOf(String string, TagContext context) {

        string = string.toUpperCase().replace("CO@", "");

        if (string.matches("RANDOM")) {

            // Get a color using random RGB values
            return new ColorTag(CoreUtilities.getRandom().nextInt(256),
                    CoreUtilities.getRandom().nextInt(256),
                    CoreUtilities.getRandom().nextInt(256));
        }

        Matcher m = rgbPattern.matcher(string);

        if (m.matches()) {
            if (!ArgumentHelper.matchesInteger(m.group(1)) || !ArgumentHelper.matchesInteger(m.group(2)) || !ArgumentHelper.matchesInteger(m.group(3))) {
                return null;
            }
            return new ColorTag(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3)));
        }

        Field colorField = null;

        try {
            colorField = Color.class.getField(string.toUpperCase());
        }
        catch (SecurityException e1) {
            Debug.echoError("Security exception getting color field!");
        }
        catch (NoSuchFieldException e1) {
            Debug.echoError("No such color field '" + string + "'!");
        }

        if (colorField != null) {
            return new ColorTag(colorField);
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

        arg = arg.toUpperCase().replace("CO@", "");

        if (arg.toUpperCase().matches("RANDOM")) {
            return true;
        }

        Matcher m = rgbPattern.matcher(arg);

        if (m.matches()) {
            return true;
        }

        for (Field field : Color.class.getFields()) {
            if (arg.toUpperCase().matches(field.getName())) {
                return true;
            }
        }

        return false;
    }

    ///////////////
    //   Constructors
    /////////////

    public ColorTag(int red, int green, int blue) {
        color = Color.fromRGB(red, green, blue);
    }

    public ColorTag(Field field) {
        try {
            color = (Color) field.get(null);
        }
        catch (Exception e) {
            Debug.echoError("Exception trying to fetch color!");
        }
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
        for (Field field : Color.class.getFields()) {
            try {
                if (Color.class.isAssignableFrom(field.getType()) && ((Color) field.get(null)).asRGB() == getColor().asRGB()) {
                    return "co@" + field.getName();
                }
            }
            catch (Exception e) {
                Debug.echoError("Exception trying to fetch color: " + e.getClass().getCanonicalName() + ": " + e.getMessage());
            }
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
        // @attribute <ColorTag.red>
        // @returns ElementTag(Number)
        // @description
        // Returns the red value of this color.
        // -->
        registerTag("red", (attribute, object) -> {
            return new ElementTag(object.color.getRed());
        });

        // <--[tag]
        // @attribute <ColorTag.green>
        // @returns ElementTag(Number)
        // @description
        // Returns the green value of this color.
        // -->
        registerTag("green", (attribute, object) -> {
            return new ElementTag(object.color.getGreen());
        });

        // <--[tag]
        // @attribute <ColorTag.blue>
        // @returns ElementTag(Number)
        // @description
        // Returns the blue value of this color.
        // -->
        registerTag("blue", (attribute, object) -> {
            return new ElementTag(object.color.getBlue());
        });

        // <--[tag]
        // @attribute <ColorTag.rgb>
        // @returns ElementTag
        // @description
        // Returns the RGB value of this color.
        // EG, 255,0,255
        // -->
        registerTag("rgb", (attribute, object) -> {
            Color color = object.color;
            return new ElementTag(color.getRed() + "," + color.getGreen() + "," + color.getBlue());
        });

        // <--[tag]
        // @attribute <ColorTag.hue>
        // @returns ElementTag(Number)
        // @description
        // Returns the hue value of this color.
        // -->
        registerTag("hue", (attribute, object) -> {
            return new ElementTag(object.ToHSB()[0]);
        });

        // <--[tag]
        // @attribute <ColorTag.saturation>
        // @returns ElementTag(Number)
        // @description
        // Returns the saturation value of this color.
        // -->
        registerTag("saturation", (attribute, object) -> {
            return new ElementTag(object.ToHSB()[1]);
        });

        // <--[tag]
        // @attribute <ColorTag.brightness>
        // @returns ElementTag(Number)
        // @description
        // Returns the brightness value of this color.
        // -->
        registerTag("brightness", (attribute, object) -> {
            return new ElementTag(object.ToHSB()[2]);
        });

        // <--[tag]
        // @attribute <ColorTag.hsv>
        // @returns ElementTag
        // @description
        // Returns the HSV value of this color.
        // EG, 100,100,255
        // -->
        registerTag("hsv", (attribute, object) -> {
            int[] HSV = object.ToHSB();
            return new ElementTag(HSV[1] + "," + HSV[1] + "," + HSV[2]);
        });

        // <--[tag]
        // @attribute <ColorTag.name>
        // @returns ElementTag
        // @description
        // Returns the name of this color (or red,green,blue if none).
        // -->
        registerTag("name", (attribute, object) -> {
            return new ElementTag(object.identify().substring(3));
        });

        // <--[tag]
        // @attribute <ColorTag.mix[<color>]>
        // @returns ColorTag
        // @description
        // Returns the color that results if you mix this color with another.
        // -->
        registerTag("mix", (attribute, object) -> {
            if (!attribute.hasContext(1)) {
                Debug.echoError("The tag ListTag.insert[...] must have a value.");
                return null;
            }
            ColorTag mixed_with = ColorTag.valueOf(attribute.getContext(1));
            if (mixed_with != null) {
                return new ColorTag(object.color.mixColors(mixed_with.getColor()));
            }
            else {
                Debug.echoError("'" + attribute.getContext(1) + "' is not a valid color!");
                return null;
            }
        });

        // <--[tag]
        // @attribute <ColorTag.to_particle_offset>
        // @returns LocationTag
        // @description
        // Returns the color as a particle offset, for use with PlayEffect.
        // -->
        registerTag("to_particle_offset", (attribute, object) -> {
            Color valid = object.color;
            if (valid.asRGB() == 0) {
                valid = Color.fromRGB(1, 0, 0);
            }
            return new LocationTag(null, valid.getRed() / 255F, valid.getGreen() / 255F, valid.getBlue() / 255F);
        });

        // <--[tag]
        // @attribute <ColorTag.type>
        // @returns ElementTag
        // @description
        // Always returns 'Color' for ColorTag objects. All objects fetchable by the Object Fetcher will return the
        // type of object that is fulfilling this attribute.
        // -->
        registerTag("type", (attribute, object) -> {
            return new ElementTag("Color");
        });
    }

    public static ObjectTagProcessor<ColorTag> tagProcessor = new ObjectTagProcessor<>();

    public static void registerTag(String name, TagRunnable.ObjectInterface<ColorTag> runnable, String... variants) {
        tagProcessor.registerTag(name, runnable, variants);
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {
        return tagProcessor.getObjectAttribute(this, attribute);
    }

    int[] ToHSB() {
        float[] base = java.awt.Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        return new int[] {(int) (base[0] * 255), (int) (base[1] * 255), (int) (base[2] * 255)};
    }
}
