package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.block.data.type.Comparator;

public class MaterialMode implements Property {

    public static boolean describes(ObjectTag material) {
        return material instanceof MaterialTag
                && ((MaterialTag) material).hasModernData()
                && ((MaterialTag) material).getModernData().data instanceof Comparator;
    }

    public static MaterialMode getFrom(ObjectTag _material) {
        if (!describes(_material)) {
            return null;
        }
        else {
            return new MaterialMode((MaterialTag) _material);
        }
    }

    public static final String[] handledMechs = new String[] {
            "mode"
    };

    private MaterialMode(MaterialTag _material) {
        material = _material;
    }

    MaterialTag material;

    public static void registerTags() {

        // <--[tag]
        // @attribute <MaterialTag.mode>
        // @returns ElementTag
        // @mechanism MaterialTag.mode
        // @group properties
        // @description
        // Returns a comparator's mode.
        // Output is COMPARISON or SUBTRACTION.
        // -->
        PropertyParser.<MaterialMode>registerTag("mode", (attribute, material) -> {
            return new ElementTag(material.getComparator().getMode().name());
        });
    }

    public Comparator getComparator() {
        return (Comparator) material.getModernData().data;
    }

    public void setMode(String mode) {
        getComparator().setMode(Comparator.Mode.valueOf(mode));
    }

    @Override
    public String getPropertyString() {
        return getComparator().getMode().name();
    }

    @Override
    public String getPropertyId() { return "mode"; }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object MaterialTag
        // @name mode
        // @input ElementTag
        // @description
        // Sets comparator's mode between comparison and subtraction.
        // @tags
        // <MaterialTag.mode>
        // -->
        if (mechanism.matches("mode") && mechanism.requireEnum(false, Comparator.Mode.values())) {
            setMode(mechanism.getValue().asString().toUpperCase());
        }
    }
}
