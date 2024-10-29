package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;

public class MaterialWaterlogged extends MaterialProperty<ElementTag> {

    // <--[property]
    // @object MaterialTag
    // @name waterlogged
    // @input ElementTag(Boolean)
    // @description
    // Controls whether this block is waterlogged.
    // -->

    public static boolean describes(MaterialTag material) {
        BlockData data = material.getModernData();
        return data instanceof Waterlogged;
    }

    MaterialTag material;

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(((Waterlogged) material.getModernData()).isWaterlogged());
    }

    public static void register() {
        autoRegister("waterlogged", MaterialWaterlogged.class, ElementTag.class, true);
    }

    @Override
    public void setPropertyValue(ElementTag val, Mechanism mechanism) {
        if (!mechanism.requireBoolean()) {
            return;
        }
        ((Waterlogged) material.getModernData()).setWaterlogged(val.asBoolean());
    }

    @Override
    public String getPropertyId() {
        return "waterlogged";
    }
}
