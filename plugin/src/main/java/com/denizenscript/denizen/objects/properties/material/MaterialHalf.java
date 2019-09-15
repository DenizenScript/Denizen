package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.block.data.Bisected;

public class MaterialHalf implements Property {

    public static boolean describes(ObjectTag material) {
        return material instanceof MaterialTag
                && ((MaterialTag) material).hasModernData()
                && ((MaterialTag) material).getModernData().data instanceof Bisected;
    }

    public static MaterialHalf getFrom(ObjectTag _material) {
        if (!describes(_material)) {
            return null;
        }
        else {
            return new MaterialHalf((MaterialTag) _material);
        }
    }

    public static final String[] handledTags = new String[] {
            "half"
    };

    public static final String[] handledMechs = new String[] {
            "half"
    };


    private MaterialHalf(MaterialTag _material) {
        material = _material;
    }

    MaterialTag material;

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <MaterialTag.half>
        // @returns ElementTag
        // @mechanism MaterialTag.half
        // @group properties
        // @description
        // Returns the current half for a bisected material (like stairs).
        // Output is "BOTTOM" or "TOP".
        // -->
        if (attribute.startsWith("half")) {
            return new ElementTag(getBisected().getHalf().name()).getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    public Bisected getBisected() {
        return (Bisected) material.getModernData().data;
    }

    @Override
    public String getPropertyString() {
        return String.valueOf(getBisected().getHalf());
    }

    @Override
    public String getPropertyId() {
        return "half";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object MaterialTag
        // @name half
        // @input Element
        // @description
        // Sets the current half for a bisected material (like stairs).
        // @tags
        // <MaterialTag.half>
        // -->
        if (mechanism.matches("half") && mechanism.requireEnum(false, Bisected.Half.values())) {
            getBisected().setHalf(Bisected.Half.valueOf(mechanism.getValue().asString().toUpperCase()));
        }
    }
}
