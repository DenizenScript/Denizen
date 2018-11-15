package net.aufdemrand.denizen.objects;

import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.objects.*;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.objects.properties.PropertyParser;
import net.aufdemrand.denizencore.tags.Attribute;
import net.aufdemrand.denizencore.tags.TagContext;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Color;
import org.bukkit.DyeColor;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class dColor implements dObject {

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
            return new dColor(aH.getIntegerFrom(m.group(1)),
                    aH.getIntegerFrom(m.group(2)),
                    aH.getIntegerFrom(m.group(3)));
        }

        Field colorField = null;

        try {
            colorField = Color.class.getField(string.toUpperCase());
        }
        catch (SecurityException e1) {
            dB.echoError("Security exception getting color field!");
        }
        catch (NoSuchFieldException e1) {
            dB.echoError("No such color field '" + string + "'!");
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
            dB.echoError("Exception trying to fetch color!");
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
    public String debug() {
        return (prefix + "='<A>" + identify() + "<G>'  ");
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
                dB.echoError("Exception trying to fetch color: " + e.getClass().getCanonicalName() + ": " + e.getMessage());
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
    public dObject setPrefix(String prefix) {
        if (prefix != null) {
            this.prefix = prefix;
        }
        return this;
    }

    public static void registerTags() {

        // <--[tag]
        // @attribute <co@color.red>
        // @returns Element(Number)
        // @description
        // Returns the red value of this color.
        // -->
        registerTag("red", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dColor) object).color.getRed()).getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <co@color.green>
        // @returns Element(Number)
        // @description
        // Returns the green value of this color.
        // -->
        registerTag("green", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dColor) object).color.getGreen()).getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <co@color.blue>
        // @returns Element(Number)
        // @description
        // Returns the blue value of this color.
        // -->
        registerTag("blue", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dColor) object).color.getBlue()).getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <co@color.rgb>
        // @returns Element
        // @description
        // Returns the RGB value of this color.
        // EG, 255,0,255
        // -->
        registerTag("rgb", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                Color color = ((dColor) object).color;
                return new Element(color.getRed() + "," + color.getGreen() + "," + color.getBlue()).getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <co@color.hue>
        // @returns Element(Number)
        // @description
        // Returns the hue value of this color.
        // -->
        registerTag("hue", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dColor) object).ToHSB()[0]).getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <co@color.saturation>
        // @returns Element(Number)
        // @description
        // Returns the saturation value of this color.
        // -->
        registerTag("saturation", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dColor) object).ToHSB()[1]).getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <co@color.brightness>
        // @returns Element(Number)
        // @description
        // Returns the brightness value of this color.
        // -->
        registerTag("brightness", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dColor) object).ToHSB()[2]).getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <co@color.hsv>
        // @returns Element
        // @description
        // Returns the HSV value of this color.
        // EG, 100,100,255
        // -->
        registerTag("hsv", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                int[] HSV = ((dColor) object).ToHSB();
                return new Element(HSV[1] + "," + HSV[1] + "," + HSV[2]).getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <co@color.name>
        // @returns Element
        // @description
        // Returns the name of this color (or red,green,blue if none).
        // -->
        registerTag("name", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(object.identify().substring(3)).getAttribute(attribute.fulfill(1));
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
            public String run(Attribute attribute, dObject object) {
                if (!attribute.hasContext(1)) {
                    dB.echoError("The tag li@list.insert[...] must have a value.");
                    return null;
                }
                dColor mixed_with = dColor.valueOf(attribute.getContext(1));
                if (mixed_with != null) {
                    return new dColor(((dColor) object).color.mixColors(mixed_with.getColor())).getAttribute(attribute.fulfill(1));
                }
                else {
                    dB.echoError("'" + attribute.getContext(1) + "' is not a valid color!");
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
            public String run(Attribute attribute, dObject object) {
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
        // @returns Element
        // @description
        // Always returns 'Color' for dColor objects. All objects fetchable by the Object Fetcher will return the
        // type of object that is fulfilling this attribute.
        // -->
        registerTag("type", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element("Color").getAttribute(attribute.fulfill(1));
            }
        });

    }

    public static HashMap<String, TagRunnable> registeredTags = new HashMap<String, TagRunnable>();

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
                net.aufdemrand.denizencore.utilities.debugging.dB.echoError(attribute.getScriptEntry() != null ? attribute.getScriptEntry().getResidingQueue() : null,
                        "Using deprecated form of tag '" + tr.name + "': '" + attrLow + "'.");
            }
            return tr.run(attribute, this);
        }

        // Iterate through this object's properties' attributes
        for (Property property : PropertyParser.getProperties(this)) {
            String returned = property.getAttribute(attribute);
            if (returned != null) {
                return returned;
            }
        }

        return new Element(identify()).getAttribute(attribute);
    }

    int[] ToHSB() {
        float[] base = java.awt.Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        return new int[]{(int) (base[0] * 255), (int) (base[1] * 255), (int) (base[2] * 255)};
    }
}
