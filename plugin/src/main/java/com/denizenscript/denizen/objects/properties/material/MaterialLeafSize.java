package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Bamboo;

public class MaterialLeafSize extends MaterialProperty<ElementTag> {

    // <--[property]
    // @object MaterialTag
    // @name leaf_size
    // @input ElementTag
    // @description
    // Controls the size of the leaves for this bamboo block.
    // Valid values are SMALL, LARGE, or NONE.
    // -->

    public static boolean describes(MaterialTag material) {
        BlockData data = material.getModernData();
        return data instanceof Bamboo;
    }

    MaterialTag material;

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(getBamboo().getLeaves());
    }

    @Override
    public String getPropertyId() {
        return "leaf_size";
    }

    @Override
    public void setPropertyValue(ElementTag value, Mechanism mechanism) {
        if (mechanism.requireEnum(Bamboo.Leaves.class)) {
            getBamboo().setLeaves(value.asEnum(Bamboo.Leaves.class));
        }
    }

    public static void register() {
        autoRegister("leaf_size", MaterialLeafSize.class, ElementTag.class, false);
    }

    public Bamboo getBamboo() {
        return (Bamboo) material.getModernData();
    }
}
