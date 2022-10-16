package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.block.data.type.Stairs;

public class MaterialShape implements Property {

    public static boolean describes(ObjectTag material) {
        return material instanceof MaterialTag
                && ((MaterialTag) material).hasModernData()
                && ((MaterialTag) material).getModernData() instanceof Stairs;
    }

    public static MaterialShape getFrom(ObjectTag _material) {
        if (!describes(_material)) {
            return null;
        }
        else {
            return new MaterialShape((MaterialTag) _material);
        }
    }

    public static final String[] handledMechs = new String[] {
            "shape"
    };

    private MaterialShape(MaterialTag _material) {
        material = _material;
    }

    MaterialTag material;

    public static void registerTags() {

        // <--[tag]
        // @attribute <MaterialTag.shape>
        // @returns ElementTag
        // @mechanism MaterialTag.shape
        // @group properties
        // @description
        // Returns the shape of a block.
        // For stairs, output is the corner shape as INNER_LEFT, INNER_RIGHT, OUTER_LEFT, OUTER_RIGHT, or STRAIGHT.
        // -->
        PropertyParser.registerStaticTag(MaterialShape.class, ElementTag.class, "shape", (attribute, material) -> {
            return new ElementTag(material.getStairs().getShape());
        });
    }

    public Stairs getStairs() {
        return (Stairs) material.getModernData();
    }

    @Override
    public String getPropertyString() {
        return getStairs().getShape().name();
    }

    @Override
    public String getPropertyId() {
        return "shape";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object MaterialTag
        // @name shape
        // @input ElementTag
        // @description
        // Sets the shape of a block.
        // For stairs, input is the corner shape as INNER_LEFT, INNER_RIGHT, OUTER_LEFT, OUTER_RIGHT, or STRAIGHT.
        // @tags
        // <MaterialTag.shape>
        // -->
        if (mechanism.matches("shape") && mechanism.requireEnum(Stairs.Shape.class)) {
            getStairs().setShape(Stairs.Shape.valueOf(mechanism.getValue().asString().toUpperCase()));
        }
    }
}
