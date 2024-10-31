package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Repeater;

public class MaterialLocked extends MaterialProperty<ElementTag> {

    // <--[property]
    // @object MaterialTag
    // @name is_locked
    // @input ElementTag(Boolean)
    // @description
    // Controls whether this redstone repeater material is locked.
    // -->

    public static boolean describes(MaterialTag material) {
        BlockData data = material.getModernData();
        return data instanceof Repeater;
    }

    MaterialTag material;

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(isLocked());
    }

    @Override
    public String getPropertyId() {
        return "is_locked";
    }

    @Override
    public void setPropertyValue(ElementTag value, Mechanism mechanism) {
        if (mechanism.requireBoolean()) {
            getRepeater().setLocked(value.asBoolean());
        }
    }

    public static void register() {
        autoRegister("is_locked", MaterialLocked.class, ElementTag.class, true);
    }

    public Repeater getRepeater() {
        return (Repeater) material.getModernData();
    }

    public boolean isLocked() {
        return getRepeater().isLocked();
    }
}
