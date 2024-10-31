package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Stairs;

public class MaterialShape extends MaterialProperty<ElementTag> {

    // <--[property]
    // @object MaterialTag
    // @name shape
    // @input ElementTag
    // @description
    // Controls the shape of a block.
    // For stairs, the corner shape can be INNER_LEFT, INNER_RIGHT, OUTER_LEFT, OUTER_RIGHT, or STRAIGHT.
    // -->

    public static boolean describes(MaterialTag material) {
        BlockData data = material.getModernData();
        return data instanceof Stairs;
    }

    MaterialTag material;

    @Override
    public String getPropertyId() {
        return "shape";
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(getStairs().getShape());
    }

    @Override
    public void setPropertyValue(ElementTag value, Mechanism mechanism) {
        if (mechanism.requireEnum(Stairs.Shape.class)) {
            getStairs().setShape(Stairs.Shape.valueOf(value.asString().toUpperCase()));
        }
    }

    public static void register() {
        autoRegister("shape", MaterialShape.class, ElementTag.class, true);
    }

    public Stairs getStairs() {
        return (Stairs) material.getModernData();
    }
}
