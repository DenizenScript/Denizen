package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.block.data.type.DaylightDetector;

public class MaterialDaylightDetector implements Property {

    public static boolean describes(ObjectTag material) {
        return material instanceof MaterialTag
                && ((MaterialTag) material).hasModernData()
                && ((MaterialTag) material).getModernData().data instanceof DaylightDetector;
    }

    public static MaterialDaylightDetector getFrom(ObjectTag _material) {
        if (!describes(_material)) {
            return null;
        }
        else {
            return new MaterialDaylightDetector((MaterialTag) _material);
        }
    }

    public static final String[] handledMechs = new String[] {
            "inverted"
    };

    private MaterialDaylightDetector(MaterialTag _material) {
        material = _material;
    }

    MaterialTag material;

    public static void registerTags() {

        // <--[tag]
        // @attribute <MaterialTag.inverted>
        // @returns ElementTag(Boolean)
        // @mechanism MaterialTag.inverted
        // @group properties
        // @description
        // Returns whether a daylight detector block is inverted, or not.
        // -->
        PropertyParser.<MaterialDaylightDetector>registerTag("inverted", (attribute, material) -> {
            return new ElementTag(material.getDaylightDetector().isInverted());
        });
    }

    public DaylightDetector getDaylightDetector() {
        return (DaylightDetector) material.getModernData().data;
    }

    @Override
    public String getPropertyString() {
        return String.valueOf(getDaylightDetector().isInverted());
    }

    @Override
    public String getPropertyId() {
        return "inverted";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object MaterialTag
        // @name inverted
        // @input ElementTag(Boolean)
        // @description
        // Sets whether a daylight detector block is inverted, or not.
        // @tags
        // <MaterialTag.inverted>
        // -->
        if (mechanism.matches("inverted") && mechanism.requireBoolean()) {
            getDaylightDetector().setInverted(mechanism.getValue().asBoolean());
        }
    }
}

