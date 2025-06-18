package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Snowable;

public class MaterialSnowable extends MaterialProperty<ElementTag> {

    // <--[tag]
    // @object MaterialTag
    // @name snowy
    // @input ElementTag(Boolean)
    // @description
    // Controls whether this material is covered in snow.
    // -->

    public static boolean describes(MaterialTag material) {
        BlockData data = material.getModernData();
        return data instanceof Snowable;
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(getSnowable().isSnowy());
    }

    @Override
    public String getPropertyId() {
        return "snowy";
    }

    @Override
    public void setPropertyValue(ElementTag value, Mechanism mechanism) {
        if (mechanism.requireBoolean()) {
            getSnowable().setSnowy(value.asBoolean());
        }
    }

    public static void register() {
        autoRegister("snowy", MaterialSnowable.class, ElementTag.class, true);
    }

    public Snowable getSnowable() {
        return (Snowable) getBlockData();
    }
}
