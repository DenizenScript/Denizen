package com.denizenscript.denizen.objects;

import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.Color;
import org.bukkit.DyeColor;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class dColor implements ObjectTag {

    // <--[language]
    // @name dColor
    // @group Object System
    // @description
    // A dColor represents an RGB color code.
    //
    // Note that a dColor is NOT a base dye color (used by wool, etc). That is handled by a separate naming system.
    //
    // For format info, see <@link language co@>
    //
    // -->

    // <--[language]
    // @name co@
    // @group Object Fetcher System
    // @description
    // co@ refers to the 'object identifier' of a dColor. The 'co@' is notation for Denizen's Object
    // Fetcher. The constructor for a dColor is <red>,<green>,<blue>, or the name of a color.
    // For example, 'co@50,64,128' or 'co@red'.
    //
    // A list of accepted color names can be found at
    // <@link url https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Color.html>
    //
    // Red/green/blue values are each from 0 to 256.
    //
    // For general info, see <@link language dColor>
    //
    // -->

    final static Pattern rgbPattern = Pattern.compile("(\\d+)[,:](\\d+)[,:](\\d+)");

    //////////////////
    //    OBJECT FETCHER
    ////////////////

    public static dColor valueOf(String string) {
        return valueOf(string, null);
    }

    /**
     * Gets a Color Object from a string form.
     *
     * @param string the string
     * @return a Color, or null if incorrectly formatted
     */
    @Fetchable("co")
    public static dColor valueOf(String string, TagContext context) {

        string = string.toUpperCase().replace("CO@", "");

        if (string.matches("RANDOM")) {

            // Get a color using random RGB values
            return new dColor(CoreUtilities.getRandom().nextInt(256),
                    CoreUtilities.getRandom().nextInt(256),
                    CoreUtilities.getRandom().nextInt(256));
        }

        Matcher m = rgbPattern.matcher(string);

        if (m.matches()) {
            return new dColor(ArgumentHelper.getIntegerFrom(m.group(1)),
                    ArgumentHelper.getIntegerFrom(m.group(2)),
                    ArgumentHelper.getIntegerFrom(m.group(3)));
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
            return new dColor(colorField);
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

    public dColor(int red, int green, int blue) {
        color = Color.fromRGB(red, green, blue);
    }

    public dColor(Field field) {
        try {
            color = (Color) field.get(null);
        }
        catch (Exception e) {
            Debug.echoError("Exception trying to fetch color!");
        }
    }

    public dColor(Color color) {
        this.color = color;
    }

    public dColor(DyeColor dyeColor) {
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
        // @attribute <co@color.red>
        // @returns ElementTag(Number)
        // @description
        // Returns the red value of this color.
        // -->
        registerTag("red", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((dColor) object).color.getRed()).getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <co@color.green>
        // @returns ElementTag(Number)
        // @description
        // Returns the green value of this color.
        // -->
        registerTag("green", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((dColor) object).color.getGreen()).getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <co@color.blue>
        // @returns ElementTag(Number)
        // @description
        // Returns the blue value of this color.
        // -->
        registerTag("blue", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((dColor) object).color.getBlue()).getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <co@color.rgb>
        // @returns ElementTag
        // @description
        // Returns the RGB value of this color.
        // EG, 255,0,255
        // -->
        registerTag("rgb", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                Color color = ((dColor) object).color;
                return new ElementTag(color.getRed() + "," + color.getGreen() + "," + color.getBlue()).getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <co@color.hue>
        // @returns ElementTag(Number)
        // @description
        // Returns the hue value of this color.
        // -->
        registerTag("hue", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((dColor) object).ToHSB()[0]).getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <co@color.saturation>
        // @returns ElementTag(Number)
        // @description
        // Returns the saturation value of this color.
        // -->
        registerTag("saturation", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((dColor) object).ToHSB()[1]).getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <co@color.brightness>
        // @returns ElementTag(Number)
        // @description
        // Returns the brightness value of this color.
        // -->
        registerTag("brightness", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((dColor) object).ToHSB()[2]).getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <co@color.hsv>
        // @returns ElementTag
        // @description
        // Returns the HSV value of this color.
        // EG, 100,100,255
        // -->
        registerTag("hsv", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                int[] HSV = ((dColor) object).ToHSB();
                return new ElementTag(HSV[1] + "," + HSV[1] + "," + HSV[2]).getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <co@color.name>
        // @returns ElementTag
        // @description
        // Returns the name of this color (or red,green,blue if none).
        // -->
        registerTag("name", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                return new ElementTag(object.identify().substring(3)).getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <co@color.mix[<color>]>
        // @returns dColor
        // @description
        // Returns the color that results if you mix this color with another.
        // -->
        registerTag("mix", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                if (!attribute.hasContext(1)) {
                    Debug.echoError("The tag ListTag.insert[...] must have a value.");
                    return null;
                }
                dColor mixed_with = dColor.valueOf(attribute.getContext(1));
                if (mixed_with != null) {
                    return new dColor(((dColor) object).color.mixColors(mixed_with.getColor())).getAttribute(attribute.fulfill(1));
                }
                else {
                    Debug.echoError("'" + attribute.getContext(1) + "' is not a valid color!");
                    return null;
                }
            }
        });

        // <--[tag]
        // @attribute <co@color.to_particle_offset>
        // @returns dLocation
        // @description
        // Returns the color as a particle offset, for use with PlayEffect.
        // -->
        registerTag("to_particle_offset", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                Color valid = ((dColor) object).color;
                if (valid.asRGB() == 0) {
                    valid = Color.fromRGB(1, 0, 0);
                }
                return new dLocation(null, valid.getRed() / 255F, valid.getGreen() / 255F, valid.getBlue() / 255F)
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <co@color.type>
        // @returns ElementTag
        // @description
        // Always returns 'Color' for dColor objects. All objects fetchable by the Object Fetcher will return the
        // type of object that is fulfilling this attribute.
        // -->
        registerTag("type", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                return new ElementTag("Color").getAttribute(attribute.fulfill(1));
            }
        });

    }

    public static HashMap<String, TagRunnable> registeredTags = new HashMap<>();

    public static void registerTag(String name, TagRunnable runnable) {
        if (runnable.name == null) {
            runnable.name = name;
        }
        registeredTags.put(name, runnable);
    }

    @Override
    public String getAttribute(Attribute attribute) {
        // TODO: Scrap getAttribute, make this functionality a core system
        String attrLow = CoreUtilities.toLowerCase(attribute.getAttributeWithoutContext(1));
        TagRunnable tr = registeredTags.get(attrLow);
        if (tr != null) {
            if (!tr.name.equals(attrLow)) {
                com.denizenscript.denizencore.utilities.debugging.Debug.echoError(attribute.getScriptEntry() != null ? attribute.getScriptEntry().getResidingQueue() : null,
                        "Using deprecated form of tag '" + tr.name + "': '" + attrLow + "'.");
            }
            return tr.run(attribute, this);
        }

        String returned = CoreUtilities.autoPropertyTag(this, attribute);
        if (returned != null) {
            return returned;
        }

        return new ElementTag(identify()).getAttribute(attribute);
    }

    int[] ToHSB() {
        float[] base = java.awt.Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        return new int[] {(int) (base[0] * 255), (int) (base[1] * 255), (int) (base[2] * 255)};
    }
}
