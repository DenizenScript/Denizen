package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.block.data.Lightable;

public class MaterialLightable implements Property {

    public static boolean describes(ObjectTag material) {
        return material instanceof MaterialTag
                && ((MaterialTag) material).hasModernData()
                && ((MaterialTag) material).getModernData().data instanceof Lightable;
    }

    public static MaterialLightable getFrom(ObjectTag _material) {
        if (!describes(_material)) {
            return null;
        }
        else {
            return new MaterialLightable((MaterialTag) _material);
        }
    }

    public static final String[] handledTags = new String[] {
            "lit"
    };

    public static final String[] handledMechs = new String[] {
            "lit"
    };


    private MaterialLightable(MaterialTag _material) {
        material = _material;
    }

    MaterialTag material;

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <MaterialTag.lit>
        // @returns ElementTag(Boolean)
        // @mechanism MaterialTag.lit
        // @group properties
        // @description
        // Returns whether a lightable material (such as a redstone torch) is lit currently.
        // -->
        if (attribute.startsWith("lit")) {
            return new ElementTag(getLightable().isLit()).getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    public Lightable getLightable() {
        return (Lightable) material.getModernData().data;
    }

    @Override
    public String getPropertyString() {
        return getLightable().isLit() ? "true" : "false";
    }

    @Override
    public String getPropertyId() {
        return "lit";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object MaterialTag
        // @name lit
        // @input Element(Boolean)
        // @description
        // Sets whether a lightable material (such as a redstone torch) is lit currently.
        // @tags
        // <MaterialTag.lit>
        // -->
        if (mechanism.matches("lit") && mechanism.requireBoolean()) {
            getLightable().setLit(mechanism.getValue().asBoolean());
        }
    }
}
