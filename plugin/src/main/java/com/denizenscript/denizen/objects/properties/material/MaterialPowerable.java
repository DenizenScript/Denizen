package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.block.data.Powerable;

public class MaterialPowerable extends MaterialProperty<ElementTag> {

    // <--[property]
    // @object MaterialTag
    // @name powered
    // @input ElementTag(Boolean)
    // @description
    // Controls whether the material is powered.
    // -->

    public static boolean describes(MaterialTag material) {
        return material.getModernData() instanceof Powerable;
    }

    public MaterialPowerable(MaterialTag material) {
        super(material);
    }

    @Override
    public ElementTag getPropertyValue() {
        if (getBlockData() instanceof Powerable powerable) {
            return new ElementTag(powerable.isPowered());
        }
        return null;
    }

    @Override
    public String getPropertyId() {
        return "powered";
    }

    @Override
    public void setPropertyValue(ElementTag elementTag, Mechanism mechanism) {
        if (getBlockData() instanceof Powerable powerable && mechanism.requireBoolean()) {
            powerable.setPowered(elementTag.asBoolean());
        }
    }

    public static void register() {
        autoRegister("powered", MaterialPowerable.class, ElementTag.class, false);
    }
}
